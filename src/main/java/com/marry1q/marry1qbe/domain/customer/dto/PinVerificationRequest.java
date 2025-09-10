package com.marry1q.marry1qbe.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "핀 번호 검증 요청")
public class PinVerificationRequest {
    
    @NotBlank(message = "핀 번호는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "핀 번호는 6자리 숫자여야 합니다.")
    @Schema(description = "6자리 핀 번호", example = "123456")
    private String pinNumber;
}
