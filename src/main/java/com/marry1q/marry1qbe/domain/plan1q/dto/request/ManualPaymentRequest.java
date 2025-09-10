package com.marry1q.marry1qbe.domain.plan1q.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Plan1Q 상품 수동 납입 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Plan1Q 상품 수동 납입 요청")
public class ManualPaymentRequest {
    
    @NotNull(message = "자동이체 ID는 필수입니다.")
    @Schema(description = "자동이체 ID", example = "123")
    private Long autoTransferId;
    
    @NotNull(message = "납입 금액은 필수입니다.")
    @Positive(message = "납입 금액은 0보다 커야 합니다.")
    @Schema(description = "납입 금액", example = "100000")
    private BigDecimal amount;
    
    @Schema(description = "메모", example = "12월 수동납입")
    private String memo;
}
