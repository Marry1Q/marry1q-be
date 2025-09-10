package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HanaBankProductResponse {
    private Long productId;
    private String externalProductId;
    private String productCode;
    private String productName;
    private String productSubName;
    private String productType;
    private Double baseRate;
    private Double maxRate;
    private Double expectedReturnRate;
    private Long minInvestmentAmount;
    private Long maxInvestmentAmount;
    private Long monthlyInvestmentAmount;
    private Integer minPeriodMonths;
    private Integer maxPeriodMonths;
    private Integer maturityPeriodMonths;
    private String riskLevel;
    private Integer riskScore;
    private Boolean isTaxFree;
    private Boolean isGuaranteed;
    private String productDescription;
}
