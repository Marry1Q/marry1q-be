package com.marry1q.marry1qbe.domain.finance.dto.response;

import com.marry1q.marry1qbe.domain.finance.entity.CategoryBudget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "카테고리별 예산 응답")
public class CategoryBudgetResponse {
    
    @Schema(description = "카테고리별 예산 ID", example = "1")
    private Long categoryBudgetId;
    
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "카테고리명", example = "웨딩홀")
    private String categoryName;
    
    @Schema(description = "카테고리 아이콘명", example = "Heart")
    private String iconName;
    
    @Schema(description = "카테고리 색상명", example = "red")
    private String colorName;
    
    @Schema(description = "예산 금액", example = "10000000")
    private BigDecimal budgetAmount;
    
    @Schema(description = "사용 금액", example = "5000000")
    private BigDecimal spentAmount;
    
    @Schema(description = "남은 금액", example = "5000000")
    private BigDecimal remainingAmount;
    
    @Schema(description = "사용률 (%)", example = "50.0")
    private double usageRate;
    
    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private String createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    private String updatedAt;
    
    public static CategoryBudgetResponse from(CategoryBudget categoryBudget) {
        return CategoryBudgetResponse.builder()
                .categoryBudgetId(categoryBudget.getCategoryBudgetId())
                .categoryId(categoryBudget.getFinanceCategory().getFinanceCategoryId())
                .categoryName(categoryBudget.getFinanceCategory().getName())
                .iconName(categoryBudget.getFinanceCategory().getIconName())
                .colorName(categoryBudget.getFinanceCategory().getColorName())
                .budgetAmount(categoryBudget.getBudgetAmount())
                .spentAmount(categoryBudget.getSpentAmount())
                .remainingAmount(categoryBudget.getRemainingAmount())
                .usageRate(categoryBudget.getUsageRate())
                .createdAt(categoryBudget.getCreatedAt().toString())
                .updatedAt(categoryBudget.getUpdatedAt().toString())
                .build();
    }
}
