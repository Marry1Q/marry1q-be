package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRecommendationResponse {
    private Double totalExpectedReturn;
    private Integer achievementProbability; // 목표 달성 가능성 추가
    private Integer totalRiskScore;
    private String riskAssessment;
    private String aiExplanation;
    private BigDecimal monthlyAmount; // 월 납입금 추가
    private List<RecommendedProduct> recommendedProducts;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedProduct {
        private Long productId;
        private String productName;
        private String productType; // 상품 분류 정보 추가
        private Double investmentRatio;
        private Long investmentAmount;
        private Long monthlyAmount;
        private String recommendationReason;
        private Double expectedReturnRate;
    }
}
