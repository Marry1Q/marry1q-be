package com.marry1q.marry1qbe.domain.plan1q.dto.hanabank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSubscriptionRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    
    @NotBlank(message = "사용자 CI는 필수입니다.")
    private String userCi;
    
    @NotNull(message = "가입 금액은 필수입니다.")
    @Positive(message = "가입 금액은 0보다 커야 합니다.")
    private BigDecimal amount;
    
    private Integer periodMonths;
    
    private BigDecimal monthlyAmount;
    
    private String sourceAccountNumber;
}
