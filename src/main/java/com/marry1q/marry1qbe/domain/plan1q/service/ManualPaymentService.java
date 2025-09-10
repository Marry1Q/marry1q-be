package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferUpdateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferCreateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.WithdrawRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.AutoTransferResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.WithdrawResponse;
import com.marry1q.marry1qbe.domain.account.service.AutoTransferService;
import com.marry1q.marry1qbe.domain.account.service.WithdrawService;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.ManualPaymentRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.ManualPaymentResponse;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Plan1Q 상품 수동납입 서비스
 * 
 * 기능: 사용자가 Plan1Q 상품에 수동으로 납입하는 기능
 * 처리 과정: 이체 처리 → 자동이체 정보 즉시 업데이트
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManualPaymentService {
    
    private final WithdrawService withdrawService;
    private final AutoTransferService autoTransferService;
    private final SecurityUtil securityUtil;
    private final CustomerRepository customerRepository;
    
    /**
     * 수동납입 처리
     * 
     * 처리 과정:
     * 1. 자동이체 정보 검증
     * 2. 기존 보내기 서비스로 이체 처리
     * 3. 자동이체 정보 즉시 업데이트
     */
    public ManualPaymentResponse processManualPayment(ManualPaymentRequest request) {
        log.info("수동납입 처리 시작 - 자동이체ID: {}, 금액: {}", 
                 request.getAutoTransferId(), request.getAmount());
        
        // 1. 자동이체 정보 검증
        AutoTransferResponse autoTransfer = validateAndGetAutoTransfer(request.getAutoTransferId());
        
        // 2. 납입 금액 검증 (자동이체 등록 금액과 동일해야 함)
        if (autoTransfer.getAmount().compareTo(request.getAmount()) != 0) {
            log.warn("납입 금액 불일치 - 자동이체 금액: {}, 요청 금액: {}", autoTransfer.getAmount(), request.getAmount());
            throw new IllegalArgumentException("납입 금액은 자동이체 등록 금액과 동일해야 합니다.");
        }
        
        try {
            // 3. 기존 보내기 서비스로 이체 처리 (거래내역 자동 생성됨)
            WithdrawRequest withdrawRequest = createWithdrawRequest(request, autoTransfer);
            WithdrawResponse withdrawResponse = withdrawService.processWithdraw(withdrawRequest);
            
            // 4. 자동이체 정보 즉시 업데이트
            updateAutoTransferAfterManualPayment(request, autoTransfer);
            
            // 5. 수동납입 응답 생성
            Integer currentInstallment = autoTransfer.getCurrentInstallment();
            Integer remainingInstallments = autoTransfer.getRemainingInstallments();
            
            // null 체크 및 기본값 설정
            int currentInstallmentValue = currentInstallment != null ? currentInstallment : 0;
            int remainingInstallmentsValue = remainingInstallments != null ? remainingInstallments : 0;
            
            return ManualPaymentResponse.builder()
                .transactionId(withdrawResponse.getTransactionId() != null ? withdrawResponse.getTransactionId().toString() : "UNKNOWN")
                .amount(withdrawResponse.getAmount())
                .balanceAfterTransaction(withdrawResponse.getBalanceAfterTransaction())
                .currentInstallment(currentInstallmentValue + 1)
                .remainingInstallments(Math.max(0, remainingInstallmentsValue - 1))
                .status("SUCCESS")
                .completedAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("수동납입 처리 실패: {}", e.getMessage(), e);
            // @Transactional이 자동으로 롤백 처리
            throw new RuntimeException("수동납입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 자동이체 정보 검증 및 조회
     */
    private AutoTransferResponse validateAndGetAutoTransfer(Long autoTransferId) {
        AutoTransferResponse autoTransfer = autoTransferService.getAutoTransferDetail(autoTransferId);
        
        if (autoTransfer == null) {
            throw new IllegalArgumentException("존재하지 않는 자동이체입니다.");
        }
        
        // 현재 사용자의 자동이체인지 확인
        String currentUserCi = getUserCiFromUserSeqNo(securityUtil.getCurrentUserSeqNo());
        log.info("권한 검증 - 현재 사용자 userCi: {}, 자동이체 userCi: {}", currentUserCi, autoTransfer.getUserCi());
        
        if (autoTransfer.getUserCi() == null || !currentUserCi.equals(autoTransfer.getUserCi())) {
            log.warn("권한 검증 실패 - 현재 사용자 userCi: {}, 자동이체 userCi: {}", currentUserCi, autoTransfer.getUserCi());
            throw new IllegalArgumentException("본인의 자동이체만 수동납입할 수 있습니다.");
        }
        
        // 자동이체가 활성 상태인지 확인
        if (!"ACTIVE".equals(autoTransfer.getStatus())) {
            throw new IllegalArgumentException("비활성 상태의 자동이체는 수동납입할 수 없습니다.");
        }
        
        // 남은 회차가 있는지 확인
        Integer remainingInstallments = autoTransfer.getRemainingInstallments();
        if (remainingInstallments == null || remainingInstallments <= 0) {
            throw new IllegalArgumentException("모든 회차가 완료된 자동이체는 수동납입할 수 없습니다.");
        }
        
        return autoTransfer;
    }
    
    /**
     * 보내기 요청 생성
     */
    private WithdrawRequest createWithdrawRequest(ManualPaymentRequest request, AutoTransferResponse autoTransfer) {
        return WithdrawRequest.builder()
            .depositBankCode("081")  // Plan1Q 상품은 항상 하나은행
            .depositAccountNumber(autoTransfer.getToAccountNumber())  // 자동이체에서 가져옴
            .depositAccountHolderName(autoTransfer.getToAccountName())  // 자동이체에서 가져옴
            .amount(request.getAmount())  // 수동납입 요청에서 가져옴
            .depositDescription(autoTransfer.getToAccountName() + " 수동납입")  // ← printContent로 사용
            .withdrawDescription(autoTransfer.getToAccountName() + " 수동납입")  // ← dpsPrintContent로 사용
            .memo(request.getMemo())  // 수동납입 요청에서 가져옴
            .fromName(securityUtil.getCurrentUserSeqNo())  // 현재 로그인 사용자
            .toName(autoTransfer.getToAccountName())  // 자동이체에서 가져옴
            .build();
    }
    
    /**
     * 수동납입 후 자동이체 정보 즉시 업데이트
     */
    private void updateAutoTransferAfterManualPayment(ManualPaymentRequest request, AutoTransferResponse autoTransfer) {
        try {
            log.info("수동납입 후 자동이체 정보 업데이트 시작 - 자동이체ID: {}", request.getAutoTransferId());
            
            // 수동납입의 경우 기존 자동이체를 삭제하고 새로운 자동이체를 등록
            // (회차 정보가 업데이트된 상태로)
            autoTransferService.deleteAutoTransfer(request.getAutoTransferId());
            
            // 새로운 자동이체 등록 (업데이트된 회차 정보로)
            // 출금 계좌는 현재 사용자의 모임통장으로 설정
            String fromAccountNumber = getCurrentUserAccountNumber();
            
            AutoTransferCreateRequest newAutoTransferRequest = AutoTransferCreateRequest.builder()
                .fromAccountNumber(fromAccountNumber)
                .toAccountNumber(autoTransfer.getToAccountNumber())
                .toAccountName(autoTransfer.getToAccountName())
                .toBankCode(autoTransfer.getToBankCode())
                .amount(autoTransfer.getAmount())
                .frequency(autoTransfer.getSchedule())  // schedule 필드 사용
                .memo(autoTransfer.getMemo())
                .periodMonths(autoTransfer.getTotalInstallments())
                .initialStatus("SUCCESS")
                .currentInstallment(autoTransfer.getCurrentInstallment() != null ? autoTransfer.getCurrentInstallment() + 1 : 2)
                .remainingInstallments(autoTransfer.getRemainingInstallments() != null ? autoTransfer.getRemainingInstallments() - 1 : 0)
                .lastExecutionDate(LocalDate.now())
                .build();
            
            autoTransferService.createAutoTransfer(newAutoTransferRequest);
            
            log.info("수동납입 후 자동이체 정보 업데이트 완료 - 자동이체ID: {}, 현재회차: {}, 남은회차: {}", 
                     request.getAutoTransferId(), 
                     autoTransfer.getCurrentInstallment() != null ? autoTransfer.getCurrentInstallment() + 1 : 2, 
                     autoTransfer.getRemainingInstallments() != null ? autoTransfer.getRemainingInstallments() - 1 : 0);
            
        } catch (Exception e) {
            log.error("수동납입 후 자동이체 정보 업데이트 실패 - 자동이체ID: {}, 오류: {}", 
                      request.getAutoTransferId(), e.getMessage(), e);
            // 수동납입은 성공했으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 현재 사용자의 모임통장 계좌번호 조회
     */
    private String getCurrentUserAccountNumber() {
        // 실제로는 사용자 정보에서 모임통장 계좌번호를 조회해야 함
        // 임시로 하드코딩된 값 사용 (실제 구현 시 수정 필요)
        return "110-123456-789012";  // 모임통장 계좌번호
    }
    
    /**
     * userSeqNo를 userCi로 변환
     */
    private String getUserCiFromUserSeqNo(String userSeqNo) {
        log.info("userSeqNo를 userCi로 변환 시작 - userSeqNo: {}", userSeqNo);
        
        Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userSeqNo: {}", userSeqNo);
                    return new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        String userCi = customer.getUserCi();
        log.info("userCi 변환 완료 - userSeqNo: {} -> userCi: {}", userSeqNo, userCi);
        
        return userCi;
    }
}
