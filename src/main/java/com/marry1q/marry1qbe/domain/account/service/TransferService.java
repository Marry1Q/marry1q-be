package com.marry1q.marry1qbe.domain.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 공통 이체 서비스
 * 
 * 기능: 출금이체와 입금이체의 공통 로직을 제공하는 서비스
 * 
 * 주요 기능:
 * - 낙관적 락을 통한 동시성 제어 (재시도 로직 포함)
 * - 출금이체 요청 본문 생성 (auth-backend API 호출용)
 * - 입금이체 요청 본문 생성 (auth-backend API 호출용)
 * 
 * 사용처:
 * - DepositService: 채우기 기능 (개인계좌 → 모임통장)
 * - WithdrawService: 보내기 기능 (모임통장 → 개인계좌)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    /**
     * 낙관적 락을 적용한 작업 처리
     * 
     * 동시에 같은 계좌에서 여러 거래가 발생할 때 발생하는 충돌을 처리합니다.
     * 충돌 발생 시 지수 백오프(Exponential Backoff)를 사용하여 재시도합니다.
     * 
     * 재시도 전략:
     * - 1차 재시도: 100ms 대기
     * - 2차 재시도: 200ms 대기  
     * - 3차 재시도: 400ms 대기
     * 
     * @param operation 실행할 작업 (Supplier 함수)
     * @param maxRetries 최대 재시도 횟수
     * @param <T> 반환 타입
     * @return 작업 결과
     * @throws RuntimeException 최대 재시도 횟수 초과 시
     */
    public <T> T processWithOptimisticLock(Supplier<T> operation, int maxRetries) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                return operation.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("다른 거래가 진행 중입니다. 잠시 후 다시 시도해주세요.");
                }
                log.warn("낙관적 락 충돌 발생, 재시도 {}회", retryCount);
                
                // 지수 백오프 (Exponential Backoff)
                try {
                    Thread.sleep(100 * (1 << retryCount)); // 100ms, 200ms, 400ms...
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("거래가 중단되었습니다. 다시 시도해주세요.");
                }
            }
        }
        throw new RuntimeException("거래 처리에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
    
    /**
     * 공통 출금이체 요청 본문 생성
     * 
     * auth-backend의 출금이체 API 호출을 위한 요청 데이터를 구성합니다.
     * 
     * API 호출 흐름: marry1q-be → auth-backend → hanaBank-backend
     * 
     * @param wdAccountNum 출금 계좌번호 (돈이 빠져나갈 계좌)
     * @param wdBankCodeStd 출금 은행 코드 (081: 하나은행, 088: 신한은행 등)
     * @param amount 거래금액 (문자열 형태)
     * @param userSeqNo 사용자 시퀀스 번호 (Open Banking 인증용)
     * @param bankTranId 거래고유번호 (출금이체와 입금이체를 연결하는 키)
     * @param description 거래 설명 (출금 통장에 표시될 메모)
     * @param reqClientName 요청자 이름 (출금자 이름)
     * @return auth-backend 출금이체 API 호출용 요청 본문
     */
    public Map<String, Object> createCommonWithdrawRequestBody(
            String wdAccountNum, String wdBankCodeStd, String amount, 
            String userSeqNo, String bankTranId, String description, String reqClientName) {
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // ===== 출금 계좌 정보 =====
        requestBody.put("wdAccountNum", wdAccountNum);  // 출금 계좌번호
        requestBody.put("wdBankCodeStd", wdBankCodeStd);  // 출금 은행 코드
        
        // ===== 거래 정보 =====
        requestBody.put("tranAmt", amount);  // 거래금액
        requestBody.put("userSeqNo", userSeqNo);  // 사용자 시퀀스 번호
        requestBody.put("bankTranId", bankTranId);  // 거래고유번호
        requestBody.put("tranDtime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));  // 거래일시
        
        // ===== 거래 메모 및 요청자 정보 =====
        requestBody.put("dpsPrintContent", description != null ? description : "이체");  // 출금 통장 메모
        requestBody.put("reqClientName", reqClientName != null ? reqClientName : "요청자");  // 요청자 이름
        
        return requestBody;
    }
    
    /**
     * 공통 입금이체 요청 본문 생성
     * 
     * auth-backend의 입금이체 API 호출을 위한 요청 데이터를 구성합니다.
     * 
     * 중요: reqClientNum은 입금받을 계좌번호와 동일해야 합니다.
     * hanaBank-backend에서 reqClientNum을 입금 계좌번호로 해석하기 때문입니다.
     * 
     * API 호출 흐름: marry1q-be → auth-backend → hanaBank-backend
     * 
     * @param accountNum 입금받을 계좌번호 (돈을 받을 계좌)
     * @param accountHolderName 입금받을 계좌의 계좌주명
     * @param amount 거래금액 (문자열 형태)
     * @param bankTranId 거래고유번호 (출금이체와 입금이체를 연결하는 키)
     * @param reqClientNum 요청고객번호 (입금받을 계좌번호와 동일해야 함)
     * @param description 거래 설명 (입금 통장에 표시될 메모)
     * @param bankCodeStd 입금받을 계좌의 은행코드 (프론트에서 전달받은 값)
     * @return auth-backend 입금이체 API 호출용 요청 본문
     */
    public Map<String, Object> createCommonDepositRequestBody(
            String accountNum, String accountHolderName, String amount,
            String bankTranId, String reqClientNum, String description, String bankCodeStd) {
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // ===== 기본 거래 정보 =====
        requestBody.put("nameCheckOption", "on");  // 성명 확인 옵션 (기본값: on)
        requestBody.put("tranDtime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));  // 거래일시
        requestBody.put("tranNo", "00001");  // 거래번호 (기본값: 00001)
        requestBody.put("bankTranId", bankTranId);  // 거래고유번호
        requestBody.put("bankCodeStd", bankCodeStd);  // 은행 표준코드 (프론트에서 전달받은 값)
        
        // ===== 입금 계좌 정보 (돈을 받을 계좌) =====
        requestBody.put("accountNum", accountNum);  // 입금받을 계좌번호
        requestBody.put("accountHolderName", accountHolderName);  // 입금받을 계좌의 계좌주명
        
        // ===== 요청고객 정보 =====
        requestBody.put("reqClientNum", reqClientNum);  // 요청고객번호 (입금받을 계좌번호와 동일)
        
        // ===== 거래 금액 및 메모 =====
        requestBody.put("tranAmt", amount);  // 거래금액
        requestBody.put("printContent", description != null ? description : "이체");  // 입금통장메모
        requestBody.put("transferPurpose", "TR");  // 이체목적 (TR: 일반이체)
        
        return requestBody;
    }
}
