package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모임통장 정보 응답")
public class AccountInfoResponse {
    
    @Schema(description = "계좌 ID", example = "1")
    private Long accountId;
    
    @Schema(description = "계좌번호", example = "110-123-456789")
    private String accountNumber;
    
    @Schema(description = "계좌명", example = "우리 모임통장")
    private String accountName;
    
    @Schema(description = "은행명", example = "하나은행")
    private String bankName;
    
    @Schema(description = "잔액", example = "2450000")
    private BigDecimal balance;
    
    @Schema(description = "카드번호", example = "5310-****-****-1234")
    private String cardNumber;
    
    @Schema(description = "일일 한도", example = "5000000")
    private BigDecimal dailyLimit;
    
    @Schema(description = "마지막 동기화 시간", example = "2024-01-15T14:30:25")
    private LocalDateTime lastSyncedAt;
    
    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;
    
    @Schema(description = "안심계좌번호", example = "11023232-2323")
    private String safeAccountNumber;
}
