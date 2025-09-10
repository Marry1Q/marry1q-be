package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class Plan1QGoalDetailResponse {
    private Long goalId;
    private String goalName;
    private String goalDescription;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal totalExpectedReturn;
    private BigDecimal actualReturnRate;       // 실제 계산된 수익률
    private Integer targetPeriod;
    private LocalDate maturityDate;
    private BigDecimal monthlyAmount;
    private String status;
    private String statusName;
    private BigDecimal subscriptionProgress;
    private String riskLevel;
    private String riskLevelName;
    private String icon;
    private String color;
    private String userSeqNo;
    private Long coupleId;
    private Long investmentProfileId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Plan1QProductResponse> products;
    private String errorMessage; // 실시간 데이터 조회 실패 시 메시지
    
    // AI 추천 결과 (프론트엔드 전달용, DB에 저장되지 않음)
    private Double aiTotalExpectedReturn;
    private Integer totalRiskScore;
    private String riskAssessment;
    private String aiExplanation;
    
    public static Plan1QGoalDetailResponse from(Plan1QGoal goal, CommonCodeService commonCodeService) {
        List<Plan1QProductResponse> productResponses = goal.getProducts() != null ?
            goal.getProducts().stream()
                .map(product -> Plan1QProductResponse.from(product, commonCodeService))
                .collect(Collectors.toList()) :
            List.of();
        
        return Plan1QGoalDetailResponse.builder()
            .goalId(goal.getPlan1qGoalId())
            .goalName(goal.getGoalName())
            .goalDescription(goal.getGoalDescription())
            .targetAmount(goal.getTargetAmount())
            .currentAmount(goal.getCurrentAmount())
            .totalExpectedReturn(goal.getTotalExpectedReturn())
            .actualReturnRate(goal.getActualReturnRate())
            .targetPeriod(goal.getTargetPeriod())
            .maturityDate(goal.getMaturityDate())
            .monthlyAmount(goal.getMonthlyAmount())
            .status(goal.getStatus())
            .statusName(goal.getStatusName(commonCodeService))
            .subscriptionProgress(goal.getSubscriptionProgress())
            .riskLevel(goal.getRiskLevel())
            .riskLevelName(goal.getRiskLevelName(commonCodeService))
            .icon(goal.getIcon())
            .color(goal.getColor())
            .userSeqNo(goal.getUserSeqNo())
            .coupleId(goal.getCoupleId())
            .investmentProfileId(goal.getInvestmentProfileId())
            .createdAt(goal.getCreatedAt())
            .updatedAt(goal.getUpdatedAt())
            .products(productResponses)
            .errorMessage(null) // 기본값은 null
            .aiTotalExpectedReturn(null) // DB에서 가져온 목표에는 AI 추천 결과가 없음
            .totalRiskScore(null)
            .riskAssessment(null)
            .aiExplanation(null)
            .build();
    }
    
    /**
     * AI 추천 결과를 포함한 응답 생성 (프론트엔드 전달용)
     */
    public static Plan1QGoalDetailResponse fromWithAIRecommendation(
            Plan1QGoal goal, 
            CommonCodeService commonCodeService,
            com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse aiRecommendation) {
        
        log.info("🚀 fromWithAIRecommendation 메서드 호출됨");
        log.info("📊 목표 정보 - 목표ID: {}, 목표명: {}", goal.getPlan1qGoalId(), goal.getGoalName());
        log.info("🤖 AI 추천 정보 - null 여부: {}, 추천 상품 수: {}", 
            aiRecommendation == null ? "null" : "not null",
            aiRecommendation != null && aiRecommendation.getRecommendedProducts() != null ? 
                aiRecommendation.getRecommendedProducts().size() : 0);
        
        List<Plan1QProductResponse> productResponses = goal.getProducts() != null ?
            goal.getProducts().stream()
                .map(product -> Plan1QProductResponse.from(product, commonCodeService))
                .collect(Collectors.toList()) :
            List.of();
        
        // AI 추천 결과에서 추천 이유를 상품 응답에 매핑
        if (aiRecommendation != null && aiRecommendation.getRecommendedProducts() != null) {
            log.info("🔍 AI 추천 상품 매칭 시작 - AI 추천 상품 수: {}, DB 상품 수: {}", 
                aiRecommendation.getRecommendedProducts().size(), productResponses.size());
            
            // AI 추천 상품 목록 로그
            for (var aiProduct : aiRecommendation.getRecommendedProducts()) {
                log.info("🤖 AI 추천 상품 - 상품ID: {}, 상품명: {}, 추천 이유: '{}'", 
                    aiProduct.getProductId(), aiProduct.getProductName(), aiProduct.getRecommendationReason());
            }
            
            // AI 추천 상품과 DB 저장된 상품을 하나은행 상품 ID로 매칭
            for (var responseProduct : productResponses) {
                log.info("🔍 매칭 시도 - DB 상품ID: {}, 하나은행 상품ID: {}, 상품명: {}", 
                    responseProduct.getProductId(), responseProduct.getHanaBankProductId(), responseProduct.getProductName());
                
                // AI 추천 상품 중에서 하나은행 상품 ID가 일치하는 것을 찾기
                var matchingAiProduct = aiRecommendation.getRecommendedProducts().stream()
                    .filter(aiProduct -> aiProduct.getProductId().equals(responseProduct.getHanaBankProductId()))
                    .findFirst()
                    .orElse(null);
                
                if (matchingAiProduct != null) {
                    log.info("✅ 매칭 성공 - 상품ID: {}, 추천 이유: '{}'", 
                        responseProduct.getProductId(), matchingAiProduct.getRecommendationReason());
                    // 추천 이유를 포함한 새로운 응답 생성
                    int productIndex = productResponses.indexOf(responseProduct);
                    productResponses.set(productIndex, Plan1QProductResponse.builder()
                        .productId(responseProduct.getProductId())
                        .productName(responseProduct.getProductName())
                        .productType(responseProduct.getProductType())
                        .productTypeName(responseProduct.getProductTypeName())
                        .investmentRatio(responseProduct.getInvestmentRatio())
                        .investmentAmount(responseProduct.getInvestmentAmount())

                        .monthlyAmount(responseProduct.getMonthlyAmount())
                        .subscribed(responseProduct.getSubscribed())

                        .contractDate(responseProduct.getContractDate())
                        .maturityDate(responseProduct.getMaturityDate())
                        .terms(responseProduct.getTerms())
                        .contract(responseProduct.getContract())
                        .accountNumber(responseProduct.getAccountNumber())
                        .riskLevel(responseProduct.getRiskLevel())
                        .riskType(responseProduct.getRiskType())
                        .assetClass(responseProduct.getAssetClass())
                        .strategy(responseProduct.getStrategy())

                        .period(responseProduct.getPeriod())
                        .hanaBankProductId(responseProduct.getHanaBankProductId())
                        .hanaBankSubscriptionId(responseProduct.getHanaBankSubscriptionId())
                        .createdAt(responseProduct.getCreatedAt())
                        .updatedAt(responseProduct.getUpdatedAt())
                        .plan1qGoalId(responseProduct.getPlan1qGoalId())
                        .recommendationReason(matchingAiProduct.getRecommendationReason()) // AI 추천 이유 포함
                        .build());
                } else {
                    log.warn("❌ 매칭 실패 - 상품ID: {}, 하나은행 상품ID: {}, 상품명: {}", 
                        responseProduct.getProductId(), responseProduct.getHanaBankProductId(), responseProduct.getProductName());
                }
            }
        }
        
        return Plan1QGoalDetailResponse.builder()
            .goalId(goal.getPlan1qGoalId())
            .goalName(goal.getGoalName())
            .goalDescription(goal.getGoalDescription())
            .targetAmount(goal.getTargetAmount())
            .currentAmount(goal.getCurrentAmount())
            .totalExpectedReturn(goal.getTotalExpectedReturn())
            .targetPeriod(goal.getTargetPeriod())
            .maturityDate(goal.getMaturityDate())
            .monthlyAmount(goal.getMonthlyAmount())
            .status(goal.getStatus())
            .statusName(goal.getStatusName(commonCodeService))
            .subscriptionProgress(goal.getSubscriptionProgress())
            .riskLevel(goal.getRiskLevel())
            .riskLevelName(goal.getRiskLevelName(commonCodeService))
            .icon(goal.getIcon())
            .color(goal.getColor())
            .userSeqNo(goal.getUserSeqNo())
            .coupleId(goal.getCoupleId())
            .investmentProfileId(goal.getInvestmentProfileId())
            .createdAt(goal.getCreatedAt())
            .updatedAt(goal.getUpdatedAt())
            .products(productResponses)
            .errorMessage(null) // 기본값은 null
            .aiTotalExpectedReturn(aiRecommendation != null ? aiRecommendation.getTotalExpectedReturn() : null)
            .totalRiskScore(aiRecommendation != null ? aiRecommendation.getTotalRiskScore() : null)
            .riskAssessment(aiRecommendation != null ? aiRecommendation.getRiskAssessment() : null)
            .aiExplanation(aiRecommendation != null ? aiRecommendation.getAiExplanation() : null)
            .build();
    }
}
