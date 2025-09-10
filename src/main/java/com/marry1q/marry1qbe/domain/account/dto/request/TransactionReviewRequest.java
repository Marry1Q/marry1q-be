package com.marry1q.marry1qbe.domain.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래내역 리뷰 상태 변경 요청")
public class TransactionReviewRequest {
    
    @Schema(description = "리뷰 상태 (기본값: reviewed)", example = "reviewed", allowableValues = {"reviewed"})
    @Pattern(regexp = "^(reviewed)$", message = "리뷰 상태는 'reviewed'만 가능합니다.")
    private String reviewStatus;
    
    @Schema(description = "가계부 카테고리 ID (선택사항)", example = "1")
    private Long categoryId;
    
    @Schema(description = "메모 (선택사항)", example = "월급 입금 메모")
    private String memo;
}
