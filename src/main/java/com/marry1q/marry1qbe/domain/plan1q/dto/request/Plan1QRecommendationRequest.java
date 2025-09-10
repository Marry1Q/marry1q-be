package com.marry1q.marry1qbe.domain.plan1q.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QRecommendationRequest {
    private String goalTitle;
    private String detailedGoal;
    private BigDecimal targetAmount;
    private Integer targetPeriod;
}
