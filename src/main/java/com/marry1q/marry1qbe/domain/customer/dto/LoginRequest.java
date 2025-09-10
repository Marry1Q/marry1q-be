package com.marry1q.marry1qbe.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    
    @Schema(description = "이메일", example = "kim@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String customerEmail;
    
    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String customerPw;
}
