package com.marry1q.marry1qbe.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequest {
    
    @Schema(description = "사용자 고유 번호", example = "U001")
    @NotBlank(message = "사용자 고유 번호는 필수입니다.")
    @Size(max = 20, message = "사용자 고유 번호는 20자 이하여야 합니다.")
    private String userSeqNo;
    
    @Schema(description = "사용자 CI", example = "abc123def456")
    @NotBlank(message = "사용자 CI는 필수입니다.")
    @Size(max = 88, message = "사용자 CI는 88자 이하여야 합니다.")
    private String userCi;
    
    @Schema(description = "고객명", example = "홍길동")
    @NotBlank(message = "고객명은 필수입니다.")
    @Size(max = 100, message = "고객명은 100자 이하여야 합니다.")
    private String customerName;
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String customerPhone;
    
    @Schema(description = "고객 정보", example = "INFO1234")
    @NotBlank(message = "고객 정보는 필수입니다.")
    @Size(max = 8, message = "고객 정보는 8자 이하여야 합니다.")
    private String customerInfo;
    
    @Schema(description = "고객 PIN", example = "123456")
    @NotBlank(message = "고객 PIN은 필수입니다.")
    @Size(max = 100, message = "고객 PIN은 100자 이하여야 합니다.")
    private String customerPin;
    
    @Schema(description = "이메일", example = "kim@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String customerEmail;
    
    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.")
    private String customerPw;
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
}
