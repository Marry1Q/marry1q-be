package com.marry1q.marry1qbe.domain.plan1q.dto.hanabank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSubscriptionResponse {
    
    private String accountNumber;
    
    private String subscriptionId;
    
    private String productName;
    
    private String productType;
    
    private BigDecimal amount;
    
    private BigDecimal monthlyAmount;
    
    private LocalDate contractDate;
    
    private LocalDate maturityDate;
    
    private BigDecimal interestRate;
    
    private BigDecimal returnRate;
    
    private String status;
}
