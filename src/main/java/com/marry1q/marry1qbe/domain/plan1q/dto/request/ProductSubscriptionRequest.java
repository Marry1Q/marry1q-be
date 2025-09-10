package com.marry1q.marry1qbe.domain.plan1q.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSubscriptionRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    
    @Positive(message = "가입 기간은 0보다 커야 합니다.")
    private Integer periodMonths;
    
    @NotNull(message = "월 납입금은 필수입니다.")
    @Positive(message = "월 납입금은 0보다 커야 합니다.")
    private BigDecimal monthlyAmount;
    
    @NotBlank(message = "출금 계좌번호는 필수입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{6}-\\d{6}$", message = "계좌번호 형식이 올바르지 않습니다. (예: 110-123456-789012)")
    private String sourceAccountNumber;
    
    @NotBlank(message = "납부일은 필수입니다.")
    @Pattern(regexp = "^(매월 [1-9]|[12][0-9]|3[01]일|매주 [월화수목금토일]요일)$", 
             message = "납부일 형식이 올바르지 않습니다. (예: 매월 25일, 매주 월요일)")
    private String paymentDate;
}
