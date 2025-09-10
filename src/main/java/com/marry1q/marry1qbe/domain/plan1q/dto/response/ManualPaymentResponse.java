package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Plan1Q 상품 수동 납입 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Plan1Q 상품 수동 납입 응답")
public class ManualPaymentResponse {
    
    @Schema(description = "거래 ID", example = "TXN123456789")
    private String transactionId;
    
    @Schema(description = "납입 금액", example = "100000")
    private BigDecimal amount;
    
    @Schema(description = "거래 후 잔액", example = "500000")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "현재 회차", example = "3")
    private Integer currentInstallment;
    
    @Schema(description = "남은 회차", example = "9")
    private Integer remainingInstallments;
    
    @Schema(description = "처리 상태", example = "SUCCESS")
    private String status;
    
    @Schema(description = "완료 시간")
    private LocalDateTime completedAt;
}
