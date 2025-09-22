package com.marry1q.marry1qbe.domain.giftMoney.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "안심계좌 거래내역 리뷰 상태 변경 요청")
public class UpdateSafeAccountTransactionReviewStatusRequest {
    
    @NotBlank(message = "리뷰 상태는 필수입니다.")
    @Schema(description = "리뷰 상태", example = "reviewed")
    private String reviewStatus;
    
    @Schema(description = "메모", example = "축의금으로 등록 완료")
    private String memo;
}
