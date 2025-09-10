package com.marry1q.marry1qbe.domain.finance.dto.response;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "카테고리 응답")
public class CategoryResponse {
    @Schema(description = "카테고리 ID", example = "1")
    private Long financeCategoryId;
    
    @Schema(description = "카테고리명", example = "웨딩홀")
    private String name;
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "카테고리 아이콘명", example = "Heart")
    private String iconName;
    
    @Schema(description = "카테고리 색상명", example = "red")
    private String colorName;
    
    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    private LocalDateTime updatedAt;
    
    public static CategoryResponse from(FinanceCategory category) {
        return CategoryResponse.builder()
                .financeCategoryId(category.getFinanceCategoryId())
                .name(category.getName())
                .coupleId(category.getCoupleId())
                .iconName(category.getIconName())
                .colorName(category.getColorName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
