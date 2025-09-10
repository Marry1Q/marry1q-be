package com.marry1q.marry1qbe.domain.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "카테고리 목록 응답")
public class CategoryListResponse {
    
    @Schema(description = "카테고리 목록")
    private List<CategoryResponse> categories;
    
    @Schema(description = "총 개수", example = "5")
    private int totalCount;
}
