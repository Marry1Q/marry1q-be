package com.marry1q.marry1qbe.domain.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinVerificationResponse {
    
    /**
     * 핀 번호 검증 성공 여부
     */
    private boolean isValid;
    
    /**
     * 검증 결과 메시지
     */
    private String message;
    
    /**
     * 검증 시각
     */
    private LocalDateTime verifiedAt;
}
