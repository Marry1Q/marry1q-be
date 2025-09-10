package com.marry1q.marry1qbe.domain.plan1q.dto.hanabank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProfitInfoResponse {
    
    private String accountNumber;
    
    private String productType;
    
    private BigDecimal currentBalance;
    
    private BigDecimal totalDeposit;
    
    private BigDecimal baseRate;        // 적금용 기준금리
    
    private BigDecimal profitRate;       // 펀드용 수익률
    
    private BigDecimal profit;
    
    private LocalDateTime lastUpdated;
}
