package com.marry1q.marry1qbe.domain.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "카테고리별 예산 생성 요청")
public class CreateCategoryBudgetRequest {
    
    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    
    @NotNull(message = "예산 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "예산 금액은 0.01 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "예산 금액은 최대 10자리 정수와 2자리 소수점까지 허용됩니다.")
    @Schema(description = "예산 금액", example = "10000000", minimum = "0.01")
    private BigDecimal budgetAmount;
}
