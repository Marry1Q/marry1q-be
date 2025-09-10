package com.marry1q.marry1qbe.domain.couple.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "커플 생성 요청")
public class CreateCoupleRequest {
    
    @NotNull(message = "결혼 예정일은 필수입니다.")
    @Schema(description = "결혼 예정일", example = "2024-12-31")
    private LocalDate weddingDate;
    
    @NotNull(message = "총 결혼 예산은 필수입니다.")
    @Schema(description = "총 결혼 예산", example = "50000000")
    private BigDecimal totalBudget;
    
    @NotNull(message = "커플 모임통장 계좌번호는 필수입니다.")
    @Pattern(regexp = "^[0-9-]+$", message = "계좌번호는 숫자와 하이픈만 입력 가능합니다.")
    @Schema(description = "커플 모임통장 계좌번호", example = "123-456-789012")
    private String coupleAccount;
    
    @Schema(description = "커플 카드 번호", example = "1234-5678-9012-3456", required = false)
    private String coupleCardNumber;
    
    @NotNull(message = "URL 슬러그는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "URL 슬러그는 영문, 숫자, 하이픈, 언더스코어만 입력 가능합니다.")
    @Schema(description = "고정 URL 슬러그", example = "couple-123")
    private String urlSlug;
}

