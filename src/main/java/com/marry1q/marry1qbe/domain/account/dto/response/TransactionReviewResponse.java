package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래내역 리뷰 응답")
public class TransactionReviewResponse {
    
    @Schema(description = "거래 ID", example = "1")
    private Long id;
    
    @Schema(description = "리뷰 상태", example = "reviewed")
    private String reviewStatus;
    
    @Schema(description = "카테고리 정보")
    private CategoryInfo category;
    
    @Schema(description = "메모", example = "1월 급여")
    private String memo;
    
    @Schema(description = "수정 시간", example = "2024-01-15T14:30:25")
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리 정보")
    public static class CategoryInfo {
        @Schema(description = "카테고리 ID", example = "1")
        private Long id;
        
        @Schema(description = "카테고리명", example = "급여")
        private String name;
    }
}
