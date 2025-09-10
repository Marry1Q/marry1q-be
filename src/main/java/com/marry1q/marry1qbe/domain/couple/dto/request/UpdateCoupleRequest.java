package com.marry1q.marry1qbe.domain.couple.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "커플 정보 수정 요청")
public class UpdateCoupleRequest {
    
    @NotNull(message = "결혼 예정일은 필수입니다.")
    @Schema(description = "결혼 예정일", example = "2024-12-31")
    private LocalDate weddingDate;
    
    @NotNull(message = "총 결혼 예산은 필수입니다.")
    @Schema(description = "총 결혼 예산", example = "50000000")
    private BigDecimal totalBudget;
}

