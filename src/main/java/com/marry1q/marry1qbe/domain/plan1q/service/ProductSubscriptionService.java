package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountSubscriptionRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountSubscriptionResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.ProductSubscriptionRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.ProductSubscriptionResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QProductRepository;
import com.marry1q.marry1qbe.domain.account.service.AutoTransferService;
import com.marry1q.marry1qbe.domain.account.service.WithdrawService;
import com.marry1q.marry1qbe.domain.account.service.AccountService;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferCreateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferUpdateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.WithdrawRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.AutoTransferResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.WithdrawResponse;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSubscriptionService {
    
    private final HanaBankApiService hanaBankApiService;
    private final CustomerRepository customerRepository;
    private final Plan1QProductStatusService plan1QProductStatusService;
    private final Plan1QProductRepository plan1QProductRepository;
    
    // 새로 추가할 의존성들
    private final AutoTransferService autoTransferService;
    private final WithdrawService withdrawService;
    private final AccountService accountService;
    
    /**
     * 상품 가입 및 계좌 개설
     */
    @Transactional
    public ProductSubscriptionResponse subscribeProduct(String username, ProductSubscriptionRequest request) {
        log.info("-----------------------------------------------------");
        log.info("💳 [PRODUCT-SUBSCRIPTION-SERVICE] 상품 가입 처리 시작");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", username);
        log.info("📦 상품ID: {}", request.getProductId());
        log.info("💰 월 납입금: {}", request.getMonthlyAmount());
        log.info("📅 기간: {} 개월", request.getPeriodMonths());
        log.info("📅 납부일: {}", request.getPaymentDate());
        log.info("💳 출금계좌: {}", request.getSourceAccountNumber());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. 사용자 정보 조회 (username은 실제로는 userSeqNo입니다)
            Customer customer = customerRepository.findById(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
            
            log.info("✅ 사용자 정보 조회 완료 - CI: {}", customer.getUserCi());
            
            // 2. Plan1Q 상품 정보 조회 (하나은행 상품 ID 가져오기)
            Plan1QProduct plan1QProduct = plan1QProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN1Q_PRODUCT_NOT_FOUND, "존재하지 않는 상품입니다."));
            
            if (plan1QProduct.getHanaBankProductId() == null) {
                log.error("❌ 하나은행 상품 ID가 없습니다 - Plan1Q 상품ID: {}", request.getProductId());
                throw new CustomException(ErrorCode.INVALID_REQUEST, "하나은행 상품 정보가 연결되지 않은 상품입니다.");
            }
            
            log.info("✅ Plan1Q 상품 정보 조회 완료 - 하나은행 상품ID: {}", plan1QProduct.getHanaBankProductId());
            
            // 3. 하나은행 계좌 개설
            AccountSubscriptionResponse hanaBankResponse = createHanaBankAccount(plan1QProduct, customer, request);
            
            log.info("✅ 하나은행 백엔드 상품 가입 완료 - 계좌번호: {}", hanaBankResponse.getAccountNumber());
            
            // 4. marry1q account 테이블에 계좌 정보 저장 (새로 추가)
            Account savedAccount = accountService.savePlan1qAccount(hanaBankResponse, customer, plan1QProduct);
            log.info("✅ marry1q account 테이블 저장 완료 - 계좌ID: {}, Plan1Q상품ID: {}", 
                     savedAccount.getAccountId(), savedAccount.getPlan1qProductId());
            
            // 5. Plan1Q 상품 가입 정보 업데이트 (계좌번호 및 출금계좌번호 포함)
            plan1QProduct.updateSubscription(
                true, 
                hanaBankResponse.getSubscriptionId(), 
                hanaBankResponse.getAccountNumber(),
                request.getSourceAccountNumber(),
                hanaBankResponse.getContractDate()
            );
            plan1QProductRepository.save(plan1QProduct);
            
            log.info("✅ Plan1Q 상품 가입 정보 업데이트 완료 - 계좌번호: {}", hanaBankResponse.getAccountNumber());
            
            // 6. 초기 납입금 이체
            WithdrawResponse withdrawResponse = null;
            try {
                withdrawResponse = processInitialDeposit(
                    request.getSourceAccountNumber(),     // 모임통장
                    hanaBankResponse.getAccountNumber(),   // 새로 개설된 계좌
                    request.getMonthlyAmount(),           // 월 납입금과 동일한 금액
                    customer.getCustomerName(),           // 사용자 이름
                    hanaBankResponse.getProductName()     // 상품명
                );
                log.info("✅ 초기 납입금 이체 완료");
            } catch (Exception e) {
                log.warn("⚠️ 초기 납입금 이체 실패 (상품 가입은 성공): {}", e.getMessage());
            }
            
            // 7. 초기 납입 결과에 따른 자동이체 등록
            AutoTransferResponse autoTransferResponse = null;
            String autoTransferStatus = "FAILED";
            String lastExecutionStatus = "FAILED";
            
            if (withdrawResponse != null && "SUCCESS".equals(withdrawResponse.getStatus())) {
                // 성공 시: SUCCESS 상태로 자동이체 등록
                try {
                    autoTransferResponse = registerAutoTransfer(
                        hanaBankResponse.getAccountNumber(),  // 새로 개설된 계좌
                        request.getSourceAccountNumber(),      // 모임통장
                        request.getMonthlyAmount(),           // 월 납입금
                        hanaBankResponse.getProductName(),     // 상품명
                        request.getPaymentDate(),              // 납부일
                        request.getPeriodMonths(),             // 사용자 설정 기간
                        "SUCCESS",                             // 초기 상태
                        1,                                     // 현재 회차
                        request.getPeriodMonths() - 1,         // 남은 회차
                        LocalDate.now()                        // 실행일
                    );
                    autoTransferStatus = "ACTIVE";
                    lastExecutionStatus = "SUCCESS";
                    log.info("✅ 자동이체 등록 완료 (초기 납입 성공) - 자동이체ID: {}", autoTransferResponse.getAutoTransferId());
                } catch (Exception e) {
                    log.warn("⚠️ 자동이체 등록 실패 (초기 납입은 성공): {}", e.getMessage());
                    autoTransferStatus = "FAILED";
                    lastExecutionStatus = "SUCCESS"; // 초기 납입은 성공했으므로
                }
            } else {
                // 실패 시: FAILED 상태로 자동이체 등록
                try {
                    autoTransferResponse = registerAutoTransfer(
                        hanaBankResponse.getAccountNumber(),  // 새로 개설된 계좌
                        request.getSourceAccountNumber(),      // 모임통장
                        request.getMonthlyAmount(),           // 월 납입금
                        hanaBankResponse.getProductName(),     // 상품명
                        request.getPaymentDate(),              // 납부일
                        request.getPeriodMonths(),             // 사용자 설정 기간
                        "FAILED",                              // 초기 상태
                        0,                                     // 현재 회차 (실패)
                        request.getPeriodMonths(),             // 남은 회차 (전체)
                        null                                   // 실행일 없음
                    );
                    autoTransferStatus = "FAILED";
                    lastExecutionStatus = "FAILED";
                    log.info("✅ 자동이체 등록 완료 (초기 납입 실패) - 자동이체ID: {}", autoTransferResponse.getAutoTransferId());
                } catch (Exception e) {
                    log.warn("⚠️ 자동이체 등록 실패 (초기 납입도 실패): {}", e.getMessage());
                    autoTransferStatus = "FAILED";
                    lastExecutionStatus = "FAILED";
                }
            }
            
            // 8. 응답 생성
            ProductSubscriptionResponse response = createSuccessResponse(
                hanaBankResponse, autoTransferResponse, withdrawResponse, plan1QProduct, 
                autoTransferStatus, lastExecutionStatus);
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PRODUCT-SUBSCRIPTION-SERVICE] 상품 가입 처리 완료");
            log.info("-----------------------------------------------------");
            log.info("📦 상품명: {}", response.getProductName());
            log.info("💳 계좌번호: {}", response.getAccountNumber());
            log.info("🔄 자동이체ID: {}", response.getAutoTransferId());
            log.info("🔄 자동이체상태: {}", response.getAutoTransferStatus());
            log.info("💰 초기납입거래ID: {}", response.getInitialDepositTransactionId());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return response;
            
        } catch (CustomException e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PRODUCT-SUBSCRIPTION-SERVICE] 상품 가입 처리 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", username);
            log.error("📦 상품ID: {}", request.getProductId());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 실패 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PRODUCT-SUBSCRIPTION-SERVICE] 상품 가입 처리 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", username);
            log.error("📦 상품ID: {}", request.getProductId());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 실패 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 가입 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 하나은행 계좌 개설
     */
    private AccountSubscriptionResponse createHanaBankAccount(Plan1QProduct plan1QProduct, Customer customer, ProductSubscriptionRequest request) {
        // 총 가입 금액 계산 (월 납입금 × 가입 기간)
        BigDecimal totalAmount = request.getMonthlyAmount().multiply(BigDecimal.valueOf(request.getPeriodMonths()));
        
        AccountSubscriptionRequest hanaBankRequest = AccountSubscriptionRequest.builder()
            .productId(plan1QProduct.getHanaBankProductId())  // 하나은행 실제 상품 ID 사용
            .userCi(customer.getUserCi())
            .amount(totalAmount)                              // 총 가입 금액 (월 납입금 × 기간)
            .periodMonths(request.getPeriodMonths())
            .monthlyAmount(request.getMonthlyAmount())
            .sourceAccountNumber(request.getSourceAccountNumber())
            .build();
        
        return hanaBankApiService.subscribeProduct(hanaBankRequest);
    }
    
    /**
     * 자동이체 등록
     */
    private AutoTransferResponse registerAutoTransfer(String toAccountNumber, String fromAccountNumber, 
                                                     BigDecimal amount, String productName, String paymentDate,
                                                     Integer periodMonths, String initialStatus,
                                                     Integer currentInstallment, Integer remainingInstallments,
                                                     LocalDate lastExecutionDate) {
        AutoTransferCreateRequest request = AutoTransferCreateRequest.builder()
            .fromAccountNumber(fromAccountNumber)  // 모임통장
            .toAccountNumber(toAccountNumber)     // 새로 개설된 계좌
            .toAccountName(productName)           // 상품명
            .toBankCode("081")                   // 하나은행
            .amount(amount)
            .frequency(paymentDate)              // 프론트엔드에서 받은 납부일
            .memo(productName + " 자동이체")      // 기본값 사용
            .periodMonths(periodMonths)          // 사용자 설정 기간 전달
            .initialStatus(initialStatus)        // 초기 상태 (SUCCESS/FAILED)
            .currentInstallment(currentInstallment)  // 현재 회차
            .remainingInstallments(remainingInstallments)  // 남은 회차
            .lastExecutionDate(lastExecutionDate)  // 실행일
            .build();
        
        return autoTransferService.createAutoTransfer(request);
    }
    
    /**
     * 초기 납입금 이체
     */
    private WithdrawResponse processInitialDeposit(String fromAccountNumber, String toAccountNumber, 
                                                   BigDecimal amount, String customerName, String productName) {
        WithdrawRequest request = WithdrawRequest.builder()
            .depositAccountNumber(toAccountNumber)     // 새로 개설된 계좌
            .depositBankCode("081")                   // 하나은행
            .depositAccountHolderName(customerName)   // 현재 로그인한 사용자 이름
            .amount(amount)                          // 월 납입금과 동일한 금액
            .depositDescription("초기 납입금")         // 요청대로 "초기 납입금"
            .withdrawDescription(productName + " 상품 가입")  // 요청대로 "{상품명} 상품 가입"
            .fromName("모임통장")
            .toName(productName)                     // 요청대로 "{상품명}"
            .build();
        
        return withdrawService.processWithdraw(request);
    }
    
    /**
     * 성공 응답 생성
     */
    private ProductSubscriptionResponse createSuccessResponse(AccountSubscriptionResponse hanaBankResponse, 
                                                             AutoTransferResponse autoTransferResponse, 
                                                             WithdrawResponse withdrawResponse, 
                                                             Plan1QProduct plan1QProduct,
                                                             String autoTransferStatus, 
                                                             String lastExecutionStatus) {
        return ProductSubscriptionResponse.builder()
            .accountNumber(hanaBankResponse.getAccountNumber())
            .subscriptionId(hanaBankResponse.getSubscriptionId())
            .productName(hanaBankResponse.getProductName())
            .productType(hanaBankResponse.getProductType())
            .amount(hanaBankResponse.getAmount())
            .monthlyAmount(hanaBankResponse.getMonthlyAmount())
            .contractDate(hanaBankResponse.getContractDate())
            .maturityDate(hanaBankResponse.getMaturityDate())
            .interestRate(hanaBankResponse.getInterestRate())
            .returnRate(hanaBankResponse.getReturnRate())
            .status(hanaBankResponse.getStatus())
            .message("상품 가입이 성공적으로 완료되었습니다.")
            // 새로 추가되는 필드들
            .autoTransferId(autoTransferResponse != null ? autoTransferResponse.getAutoTransferId() : null)
            .initialDepositTransactionId(withdrawResponse != null && withdrawResponse.getTransactionId() != null ? withdrawResponse.getTransactionId().toString() : null)
            .nextPaymentDate(autoTransferResponse != null ? autoTransferResponse.getNextTransferDate().toString() : null)
            .autoTransferStatus(autoTransferStatus)
            .lastExecutionStatus(lastExecutionStatus)
            .build();
    }
    
}
