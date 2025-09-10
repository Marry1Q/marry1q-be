package com.marry1q.marry1qbe.domain.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "예산 대시보드 응답")
public class BudgetOverviewResponse {
    
    @Schema(description = "총 예산", example = "50000000")
    private BigDecimal totalBudget;
    
    @Schema(description = "총 사용 금액", example = "25000000")
    private BigDecimal totalSpent;
    
    @Schema(description = "남은 예산", example = "25000000")
    private BigDecimal remainingBudget;
    
    @Schema(description = "총 사용률 (%)", example = "50.0")
    private double totalUsageRate;
    
    @Schema(description = "카테고리별 예산 목록")
    private List<CategoryBudgetResponse> categoryBudgets;
}
