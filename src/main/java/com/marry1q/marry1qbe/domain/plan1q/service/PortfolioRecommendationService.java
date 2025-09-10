package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.Plan1QRecommendationRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.HanaBankProductResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QProductRepository;
import com.marry1q.marry1qbe.grobal.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioRecommendationService {
    
    private final HanaBankApiService hanaBankApiService;
    private final GeminiAIService geminiAIService;
    private final Plan1QProductRepository plan1QProductRepository;
    
    /**
     * AI 포트폴리오 추천만 수행 (DB 저장 없음)
     */
    public PortfolioRecommendationResponse getRecommendationOnly(InvestmentProfile profile, Plan1QRecommendationRequest request) {
        log.info("-----------------------------------------------------");
        log.info("🎯 [PORTFOLIO-RECOMMENDATION] AI 포트폴리오 추천 시작");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", profile.getUserSeqNo());
        log.info("🎯 목표: {} ({}원, {}개월)", request.getGoalTitle(), request.getTargetAmount(), request.getTargetPeriod());
        log.info("📊 투자성향: {} (점수: {})", profile.getProfileType(), profile.getScore());
        log.info("⏰ 추천 시작 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. 하나은행 상품 정보 조회
            log.info("🏦 하나은행 상품 정보 조회 시작...");
            List<HanaBankProductResponse> availableProducts = hanaBankApiService.getAllProducts();
            log.info("✅ 하나은행 상품 정보 조회 완료 - 상품 수: {}", availableProducts.size());
            
            // 2. Gemini AI 포트폴리오 추천
            log.info("🤖 Gemini AI 포트폴리오 추천 시작...");
            PortfolioRecommendationResponse recommendation = geminiAIService.generatePortfolioRecommendation(
                profile, request, availableProducts);
            log.info("✅ Gemini AI 포트폴리오 추천 완료");
            
            // 3. 월 납입금 계산 및 추가
            log.info("💰 월 납입금 계산 중...");
            BigDecimal monthlyAmount = calculateMonthlyAmount(request.getTargetAmount(), request.getTargetPeriod());
            recommendation = PortfolioRecommendationResponse.builder()
                .totalExpectedReturn(recommendation.getTotalExpectedReturn())
                .achievementProbability(recommendation.getAchievementProbability())
                .totalRiskScore(recommendation.getTotalRiskScore())
                .riskAssessment(recommendation.getRiskAssessment())
                .aiExplanation(recommendation.getAiExplanation())
                .monthlyAmount(monthlyAmount)
                .recommendedProducts(recommendation.getRecommendedProducts())
                .build();
            log.info("✅ 월 납입금 계산 완료: {}원", monthlyAmount);
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PORTFOLIO-RECOMMENDATION] AI 포트폴리오 추천 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 최종 추천 상품 수: {}", recommendation.getRecommendedProducts().size());
            log.info("💰 총 예상 수익률: {}%", recommendation.getTotalExpectedReturn());
            log.info("🎯 목표 달성 가능성: {}%", recommendation.getAchievementProbability());
            log.info("💰 월 납입금: {}원", recommendation.getMonthlyAmount());
            log.info("⚠️ 총 위험도 점수: {}", recommendation.getTotalRiskScore());
            log.info("📝 AI 설명: {}", recommendation.getAiExplanation());
            log.info("⏰ 추천 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return recommendation;
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PORTFOLIO-RECOMMENDATION] AI 포트폴리오 추천 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", profile.getUserSeqNo());
            log.error("🎯 목표: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            
            throw new ExternalApiException("PortfolioRecommendation", "AI 포트폴리오 추천에 실패했습니다. 다시 시도해주세요.", e.getMessage());
        }
    }
    
    /**
     * 추천 결과를 Plan1Q 상품으로 저장 (AI 호출 없음)
     */
    @Transactional
    public List<Plan1QProduct> saveRecommendedProducts(Plan1QGoal goal, PortfolioRecommendationResponse recommendation) {
        log.info("-----------------------------------------------------");
        log.info("💾 [PORTFOLIO-RECOMMENDATION] 추천 상품 DB 저장 시작");
        log.info("-----------------------------------------------------");
        log.info("🎯 목표: {} (목표ID: {})", goal.getGoalName(), goal.getPlan1qGoalId());
        log.info("📊 저장할 추천 상품 수: {}", recommendation.getRecommendedProducts().size());
        log.info("⏰ 저장 시작 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            List<Plan1QProduct> products = new java.util.ArrayList<>();
            
            for (PortfolioRecommendationResponse.RecommendedProduct recommendedProduct : 
                 recommendation.getRecommendedProducts()) {
                
                log.info("📦 상품 저장 중: {} (투자 비율: {}%)", 
                    recommendedProduct.getProductName(), recommendedProduct.getInvestmentRatio());
                
                // 월 납입금이 소수점 첫째자리에서 반올림되었는지 확인하고 필요시 재계산
                BigDecimal monthlyAmount = BigDecimal.valueOf(recommendedProduct.getMonthlyAmount());
                BigDecimal calculatedMonthlyAmount = BigDecimal.valueOf(recommendedProduct.getInvestmentAmount())
                    .divide(BigDecimal.valueOf(goal.getTargetPeriod()), 0, java.math.RoundingMode.HALF_UP);
                
                // AI가 계산한 월 납입금과 실제 계산값이 다르면 로그 출력
                if (!monthlyAmount.equals(calculatedMonthlyAmount)) {
                    log.warn("⚠️ AI 월 납입금 계산 차이 - 상품: {}, AI 계산: {}, 실제 계산: {}", 
                        recommendedProduct.getProductName(), monthlyAmount, calculatedMonthlyAmount);
                }
                
                Plan1QProduct product = Plan1QProduct.builder()
                    .productName(recommendedProduct.getProductName())
                    .productType(recommendedProduct.getProductType())
                    .investmentRatio(BigDecimal.valueOf(recommendedProduct.getInvestmentRatio()))
                    .investmentAmount(BigDecimal.valueOf(recommendedProduct.getInvestmentAmount()))
                    .expectedReturnRate(BigDecimal.valueOf(recommendedProduct.getExpectedReturnRate()))
                    .monthlyAmount(calculatedMonthlyAmount) // 실제 계산값 사용

                    .subscribed(false)
                    .maturityDate(goal.getMaturityDate())
                    .plan1QGoal(goal)
                    .hanaBankProductId(recommendedProduct.getProductId())
                    .build();
                
                Plan1QProduct savedProduct = plan1QProductRepository.save(product);
                products.add(savedProduct);
                
                log.info("✅ 상품 저장 완료: {} (상품ID: {})", 
                    savedProduct.getProductName(), savedProduct.getPlan1qProductId());
            }
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PORTFOLIO-RECOMMENDATION] 추천 상품 DB 저장 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 저장된 상품 수: {}", products.size());
            log.info("💰 총 예상 수익률: {}%", recommendation.getTotalExpectedReturn());
            log.info("⏰ 저장 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return products;
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PORTFOLIO-RECOMMENDATION] 추천 상품 DB 저장 실패");
            log.error("-----------------------------------------------------");
            log.error("🎯 목표: {}", goal.getGoalName());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            
            throw new ExternalApiException("PortfolioRecommendation", "추천 상품 저장에 실패했습니다.", e.getMessage());
        }
    }
    
    /**
     * 월 납입금액 계산
     */
    private BigDecimal calculateMonthlyAmount(BigDecimal targetAmount, Integer targetPeriod) {
        BigDecimal monthlyAmount = targetAmount.divide(BigDecimal.valueOf(targetPeriod), 0, java.math.RoundingMode.HALF_UP);
        log.info("💰 월 납입금액 계산: {}원 ÷ {}개월 = {}원", targetAmount, targetPeriod, monthlyAmount);
        return monthlyAmount;
    }
}
