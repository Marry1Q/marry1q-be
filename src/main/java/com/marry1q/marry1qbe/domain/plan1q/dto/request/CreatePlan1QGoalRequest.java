package com.marry1q.marry1qbe.domain.plan1q.dto.request;

import com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse;
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
public class CreatePlan1QGoalRequest {
    private String goalTitle;
    private String detailedGoal;
    private BigDecimal targetAmount;
    private Integer targetPeriod;
    private BigDecimal monthlyAmount; // 월 납입금 추가
    
    // 아이콘 관련 필드
    private String icon; // 아이콘 이름 (iconMapping의 키값)
    
    // 추천 결과 관련 필드들 (선택적)
    private List<PortfolioRecommendationResponse.RecommendedProduct> recommendedProducts;
    private Double totalExpectedReturn;
    private Integer achievementProbability; // 목표 달성 가능성 추가
    private Integer totalRiskScore;
    private String riskAssessment;
    private String aiExplanation;
}
