package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProfileResponse {
    
    private Long investmentProfileId;
    private String profileType;
    private String profileTypeName;
    private String riskLevel;
    private String riskLevelName;
    private Integer score;
    private String description;
    private LocalDate expiredAt;
    private Boolean isExpired;
    private LocalDateTime createdAt;
    
    public static InvestmentProfileResponse from(InvestmentProfile profile, 
                                                com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return InvestmentProfileResponse.builder()
                .investmentProfileId(profile.getInvestmentProfileId())
                .profileType(profile.getProfileType())
                .profileTypeName(profile.getProfileTypeName(commonCodeService))
                .riskLevel(determineRiskLevel(profile.getProfileType()))
                .riskLevelName(commonCodeService.getCodeName("RISK_LEVEL", determineRiskLevel(profile.getProfileType())))
                .score(profile.getScore())
                .description(profile.getDescription())
                .expiredAt(profile.getExpiredDate())
                .isExpired(profile.isExpired())
                .createdAt(profile.getCreatedAt())
                .build();
    }
    
    /**
     * 투자성향 타입에 따른 위험도 결정
     */
    private static String determineRiskLevel(String profileType) {
        switch (profileType) {
            case "conservative":
                return "low";
            case "neutral":
                return "medium";
            case "aggressive":
                return "high";
            default:
                return "low";
        }
    }
}
