package com.marry1q.marry1qbe.domain.giftMoney.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "감사 연락 상태 변경 요청")
public class UpdateThanksStatusRequest {
    
    @NotNull(message = "감사 연락 완료 여부는 필수입니다.")
    @Schema(description = "감사 연락 완료 여부", example = "true")
    private Boolean thanksSent;
    
    @PastOrPresent(message = "감사 연락 날짜는 과거 또는 오늘 날짜여야 합니다.")
    @Schema(description = "감사 연락 날짜", example = "2024-01-16")
    private LocalDate thanksDate;
    
    @Size(max = 100, message = "감사 연락한 사람은 100자를 초과할 수 없습니다.")
    @Schema(description = "감사 연락한 사람", example = "신랑", maxLength = 100)
    private String thanksSentBy;
}
