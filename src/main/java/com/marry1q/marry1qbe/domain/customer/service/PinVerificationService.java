package com.marry1q.marry1qbe.domain.customer.service;

import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.domain.customer.dto.PinVerificationResponse;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PinVerificationService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 핀 번호 검증 (평문과 암호화된 핀 번호 모두 지원)
     * @param userSeqNo 사용자 고유 번호
     * @param inputPin 입력된 핀 번호
     * @return 검증 결과
     */
    @Transactional(readOnly = true)
    public PinVerificationResponse verifyPin(String userSeqNo, String inputPin) {
        log.info("핀 번호 검증 시작 - 사용자: {}", userSeqNo);
        
        try {
            // 1. 사용자 조회
            Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
            
            String storedPin = customer.getCustomerPin();
            boolean isValid = false;
            
            // 2. 핀 번호 검증 (평문과 암호화된 핀 번호 모두 지원)
            if (storedPin != null) {
                // BCrypt로 암호화된 핀 번호인지 확인
                if (storedPin.startsWith("$2a$") || storedPin.startsWith("$2b$") || storedPin.startsWith("$2y$")) {
                    // 암호화된 핀 번호인 경우
                    isValid = passwordEncoder.matches(inputPin, storedPin);
                    log.debug("암호화된 핀 번호 검증 - 사용자: {}, 결과: {}", userSeqNo, isValid);
                } else {
                    // 평문 핀 번호인 경우
                    isValid = inputPin.equals(storedPin);
                    log.debug("평문 핀 번호 검증 - 사용자: {}, 결과: {}", userSeqNo, isValid);
                }
            }
            
            // 3. 로그 기록 (보안)
            if (isValid) {
                log.info("핀 번호 검증 성공 - 사용자: {}", userSeqNo);
            } else {
                log.warn("핀 번호 검증 실패 - 사용자: {}", userSeqNo);
            }
            
            return PinVerificationResponse.builder()
                .isValid(isValid)
                .message(isValid ? "핀 번호가 올바릅니다." : "핀 번호가 올바르지 않습니다.")
                .verifiedAt(LocalDateTime.now())
                .build();
                
        } catch (CustomException e) {
            log.error("핀 번호 검증 중 사용자 조회 실패 - 사용자: {}, 오류: {}", userSeqNo, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("핀 번호 검증 중 예상치 못한 오류 발생 - 사용자: {}, 오류: {}", userSeqNo, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "핀 번호 검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 핀 번호 검증 (간단한 버전 - 기존 API에서 사용)
     * @param userSeqNo 사용자 고유 번호
     * @param inputPin 입력된 핀 번호
     * @return 검증 성공 여부
     */
    @Transactional(readOnly = true)
    public boolean isValidPin(String userSeqNo, String inputPin) {
        try {
            PinVerificationResponse response = verifyPin(userSeqNo, inputPin);
            return response.isValid();
        } catch (Exception e) {
            log.error("핀 번호 검증 실패 - 사용자: {}, 오류: {}", userSeqNo, e.getMessage());
            return false;
        }
    }
}
