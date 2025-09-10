package com.marry1q.marry1qbe.domain.account.service;

import com.marry1q.marry1qbe.domain.account.dto.request.DepositRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.DepositResponse;
import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction;
import com.marry1q.marry1qbe.domain.account.exception.DepositTransferException;
import com.marry1q.marry1qbe.domain.account.exception.WithdrawTransferException;
import com.marry1q.marry1qbe.domain.account.repository.AccountRepository;
import com.marry1q.marry1qbe.domain.account.repository.CoupleAccountTransactionRepository;
import com.marry1q.marry1qbe.grobal.openBankingToken.OpenBankingTokenService;
import com.marry1q.marry1qbe.grobal.util.BankTranIdGenerator;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import com.marry1q.marry1qbe.grobal.dto.ExternalApiResponse;
import com.marry1q.marry1qbe.domain.account.service.external.AuthBackendApiService;
import com.marry1q.marry1qbe.domain.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * 모임통장 채우기(입금) 서비스
 * 
 * 기능: 개인계좌에서 모임통장으로 금액을 이체하는 기능
 * 처리 흐름: 출금이체 → 입금이체 순차 처리 (원자성 보장)
 * 
 * 주요 특징:
 * - 낙관적 락을 통한 동시성 제어
 * - 출금이체와 입금이체의 순차 처리로 원자성 보장
 * - 실패 시 자동 롤백
 * - 상세한 로깅 및 모니터링
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final CoupleAccountTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OpenBankingTokenService openBankingTokenService;
    private final AuthBackendApiService authBackendApiService;
    private final SecurityUtil securityUtil;
    private final CustomerService customerService;
    private final TransferService transferService;
    
    /**
     * 채우기 처리 - 출금이체 → 입금이체 순차 처리 (낙관적 락 적용)
     * 
     * @param request 채우기 요청 정보 (출금할 계좌 정보, 금액, 설명 등)
     * @return 채우기 처리 결과 (거래ID, 잔액, 상태 등)
     */
    @Transactional
    public DepositResponse processDeposit(DepositRequest request) {
        return transferService.processWithOptimisticLock(
            () -> processDepositInternal(request), 3);
    }
    
    /**
     * 채우기 처리 내부 로직
     * 
     * 처리 순서:
     * 1. 토큰 관리 (Open Banking 토큰 발급/갱신)
     * 2. 모임통장 정보 조회 (계좌번호, 사용자 정보)
     * 3. 개인계좌에서 출금이체 (auth-backend → hanaBank-backend)
     * 4. 모임통장으로 입금이체 (auth-backend → hanaBank-backend)
     * 5. 거래내역 저장 (CoupleAccountTransaction)
     * 6. 응답 생성 및 잔액 업데이트
     * 
     * @param request 채우기 요청 정보
     * @return 채우기 처리 결과
     */
    private DepositResponse processDepositInternal(DepositRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("입금이체 처리 시작 - 출금계좌번호: {}, 출금은행: {}, 금액: {}", 
                 request.getWithdrawAccountNumber(), request.getWithdrawBankCode(), request.getAmount());
        
        // 1. 토큰 관리 (Open Banking API 호출을 위한 토큰)
        String token = openBankingTokenService.getValidToken()
            .orElseGet(() -> openBankingTokenService.issueAndSaveToken());
        
        // 2. 모임통장 정보 조회 (현재 로그인한 사용자의 모임통장)
        String coupleAccountNumber = customerService.getCurrentUserCoupleAccountNumber();  // 모임통장 계좌번호
        String userSeqNo = securityUtil.getCurrentUserSeqNo();  // 사용자 시퀀스 번호
        String bankTranId = BankTranIdGenerator.generateBankTranId();  // 거래 고유 ID
        
        try {
            // 3. 개인계좌에서 출금이체 (개인계좌 → 외부 시스템)
            processWithdrawTransfer(request, userSeqNo, bankTranId, token);
            
            // 4. 모임통장으로 입금이체 (외부 시스템 → 모임통장)
            processDepositTransfer(request, coupleAccountNumber, bankTranId, token);
            
            // 5. 응답 생성 및 잔액 업데이트 (거래내역은 동기화로 처리)
            DepositResponse response = createDepositResponseWithoutTransaction(request, coupleAccountNumber, bankTranId);
            
            // 7. 성공 로그 및 모니터링
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            log.info("입금이체 처리 완료 - 처리시간: {}ms, 출금계좌: {}, 입금계좌: {}, 금액: {}, bankTranId: {}", 
                     processingTime, request.getWithdrawAccountNumber(), coupleAccountNumber, 
                     request.getAmount(), bankTranId);
            
            return response;
            
        } catch (WithdrawTransferException | DepositTransferException e) {
            // 트랜잭션 롤백 (Spring의 @Transactional이 자동 처리)
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            log.error("입금이체 처리 실패 - 처리시간: {}ms, 오류: {}", processingTime, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            log.error("입금이체 처리 중 예상치 못한 오류 발생 - 처리시간: {}ms, 오류: {}", processingTime, e.getMessage(), e);
            throw new RuntimeException("입금이체 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * 출금이체 처리
     * 
     * 개인계좌에서 금액을 출금하는 외부 API를 호출합니다.
     * auth-backend → hanaBank-backend 순서로 처리됩니다.
     * 
     * @param request 채우기 요청 정보
     * @param userSeqNo 사용자 시퀀스 번호 (Open Banking 인증용)
     * @param bankTranId 거래 고유 ID (출금이체와 입금이체를 연결하는 키)
     * @param token Open Banking 토큰
     * @throws WithdrawTransferException 출금이체 실패 시
     */
    private void processWithdrawTransfer(DepositRequest request, String userSeqNo, String bankTranId, String token) {
        try {
            // 출금이체 요청 본문 생성
            Map<String, Object> withdrawRequestBody = transferService.createCommonWithdrawRequestBody(
                request.getWithdrawAccountNumber(),  // 출금 계좌번호 (개인 계좌)
                request.getWithdrawBankCode(),  // 출금 은행 코드
                request.getAmount().toString(),  // 출금 금액
                userSeqNo,  // 사용자 시퀀스 번호
                bankTranId,  // 거래 고유 ID
                request.getWithdrawDescription(),  // 출금 통장 메모
                request.getFromName()  // 출금자 이름
            );
            
            // auth-backend 출금이체 API 호출
            ExternalApiResponse<Object> withdrawResponse = authBackendApiService.requestWithdrawTransfer(withdrawRequestBody, token);
            
            log.info("출금이체 성공 - 출금계좌: {}, 출금은행: {}, 금액: {}, 거래ID: {}", 
                     request.getWithdrawAccountNumber(), request.getWithdrawBankCode(), request.getAmount(), bankTranId);
                     
        } catch (Exception e) {
            log.error("출금이체 실패 - 출금계좌: {}, 출금은행: {}, 금액: {}, 거래ID: {}, 오류: {}", 
                     request.getWithdrawAccountNumber(), request.getWithdrawBankCode(), request.getAmount(), bankTranId, e.getMessage());
            throw new WithdrawTransferException("출금이체 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
    
    /**
     * 입금이체 처리
     * 
     * 모임통장으로 금액을 입금하는 외부 API를 호출합니다.
     * auth-backend → hanaBank-backend 순서로 처리됩니다.
     * 
     * 중요: reqClientNum은 입금받을 계좌번호와 동일해야 합니다.
     * hanaBank-backend에서 reqClientNum을 입금 계좌번호로 해석하기 때문입니다.
     * 
     * @param request 채우기 요청 정보
     * @param coupleAccountNumber 모임통장 계좌번호 (입금받을 계좌)
     * @param bankTranId 거래 고유 ID (출금이체와 입금이체를 연결하는 키)
     * @param token Open Banking 토큰
     * @throws DepositTransferException 입금이체 실패 시
     */
    private void processDepositTransfer(DepositRequest request, String coupleAccountNumber, String bankTranId, String token) {
        try {
            // 입금이체 요청 본문 생성
            Map<String, Object> depositRequestBody = transferService.createCommonDepositRequestBody(
                coupleAccountNumber,  // 입금받을 계좌번호 (모임통장)
                request.getToName() != null ? request.getToName() : "입금자",  // 입금받을 계좌의 계좌주명
                request.getAmount().toString(),  // 입금 금액
                bankTranId,  // 거래 고유 ID
                coupleAccountNumber,  // 요청고객번호 (모임통장 계좌번호와 동일)
                request.getDepositDescription() != null ? request.getDepositDescription() : "모임통장 입금"  // 입금 통장 메모
            );
            
            // auth-backend 입금이체 API 호출
            ExternalApiResponse<Object> depositResponse = authBackendApiService.requestDepositTransfer(depositRequestBody, token);
            
            log.info("입금이체 성공 - 입금계좌: {}, 금액: {}, 거래ID: {}", 
                     coupleAccountNumber, request.getAmount(), bankTranId);
                     
        } catch (Exception e) {
            log.error("입금이체 실패 - 입금계좌: {}, 금액: {}, 거래ID: {}, 오류: {}", 
                     coupleAccountNumber, request.getAmount(), bankTranId, e.getMessage());
            throw new DepositTransferException("입금이체 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
    
    /**
     * 채우기 응답 생성 (거래내역 엔티티 없이)
     * 
     * 채우기 처리 결과를 응답 DTO로 생성합니다.
     * 거래내역은 동기화를 통해 저장되므로 여기서는 기본 정보만 응답합니다.
     * 
     * @param request 채우기 요청 정보
     * @param coupleAccountNumber 모임통장 계좌번호
     * @param bankTranId 은행 거래 ID
     * @return 채우기 응답 DTO
     */
    private DepositResponse createDepositResponseWithoutTransaction(DepositRequest request, String coupleAccountNumber, String bankTranId) {
        // 실제 계좌 조회
        Account account = accountRepository.findByAccountNumber(coupleAccountNumber)
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + coupleAccountNumber));
        
        // 실시간 잔액 조회
        BigDecimal currentBalance;
        try {
            currentBalance = getRealTimeBalance(account);
        } catch (Exception e) {
            log.error("실시간 잔액 조회 실패: {}", e.getMessage(), e);
            currentBalance = BigDecimal.ZERO;
        }
        
        // 응답 DTO 생성
        return DepositResponse.builder()
            .transactionId(null)  // 거래 ID는 동기화 후에 확인 가능
            .accountNumber(coupleAccountNumber)  // 모임통장 계좌번호
            .amount(request.getAmount())  // 이체 금액
            .balanceAfterTransaction(currentBalance)  // 실시간 잔액
            .description(request.getDepositDescription())  // 거래 설명
            .memo(request.getMemo())  // 내부 메모
            .transactionDate(LocalDate.now().toString())  // 거래 날짜
            .transactionTime(LocalTime.now().toString())  // 거래 시간
            .fromName(request.getFromName())  // 보낸 사람 이름
            .toName(request.getToName())  // 받는 사람 이름
            .status("SUCCESS")  // 처리 상태
            .completedAt(LocalDateTime.now())  // 완료 시간
            .build();
    }
    

    
    /**
     * 실시간 잔액 조회
     */
    private BigDecimal getRealTimeBalance(Account account) {
        // 오픈뱅킹 토큰 획득
        String token = openBankingTokenService.getValidToken()
                .orElseGet(() -> openBankingTokenService.issueAndSaveToken());
        
        // 소유주 user_seq_no 조회
        String ownerUserSeqNo = customerService.getCoupleAccountOwnerUserSeqNo();
        
        // 잔액 조회 요청 생성 (소유주 user_seq_no 사용)
        AuthBackendApiService.BalanceRequest request = new AuthBackendApiService.BalanceRequest(
                ownerUserSeqNo, // 소유주 user_seq_no 사용
                "081", // 하나은행
                account.getAccountNumber()
        );
        
        // 잔액 조회 API 호출
        var response = authBackendApiService.getAccountBalance(request, token);
        
        if (response != null && response.isSuccess() && response.getData() != null) {
            return response.getData().getBalanceAsBigDecimal();
        } else {
            throw new RuntimeException("잔액 조회에 실패했습니다.");
        }
    }
}
