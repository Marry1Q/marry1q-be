package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.CreatePlan1QGoalRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.Plan1QRecommendationRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.Plan1QGoalDetailResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QGoalRepository;
import com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountProfitInfoResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.Plan1QProductResponse;
import com.marry1q.marry1qbe.domain.plan1q.exception.Plan1QGoalNotFoundException;
import com.marry1q.marry1qbe.domain.plan1q.service.HanaBankApiService;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class Plan1QGoalService {
    
    private final Plan1QGoalRepository plan1QGoalRepository;
    private final InvestmentProfileService investmentProfileService;
    private final PortfolioRecommendationService portfolioRecommendationService;
    private final CommonCodeService commonCodeService;
    private final HanaBankApiService hanaBankApiService;
    private final CustomerRepository customerRepository;
    
    /**
     * Plan1Q 목표 생성 (AI 추천 포함)
     */
    @Transactional
    public Plan1QGoalDetailResponse createGoal(CreatePlan1QGoalRequest request, String userSeqNo, Long coupleId) {
        log.info("-----------------------------------------------------");
        log.info("🎯 [PLAN1Q-GOAL] 목표 생성 시작");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", userSeqNo);
        log.info("💑 커플: {}", coupleId);
        log.info("📝 목표명: {}", request.getGoalTitle());
        log.info("💰 목표 금액: {}원", request.getTargetAmount());
        log.info("⏰ 목표 기간: {}개월", request.getTargetPeriod());
        log.info("📄 상세 설명: {}", request.getDetailedGoal());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. 투자성향 검사 및 목표 생성
            InvestmentProfile profile = validateAndGetProfile(userSeqNo);
            Plan1QGoal savedGoal = createAndSaveGoal(request, userSeqNo, coupleId, profile);
            
            // 2. AI 포트폴리오 추천
            log.info("🤖 AI 포트폴리오 추천 시작...");
            // 목표 정보를 DTO로 변환하여 AI 추천 요청
            Plan1QRecommendationRequest recommendationRequest = Plan1QRecommendationRequest.builder()
                .goalTitle(request.getGoalTitle())
                .detailedGoal(request.getDetailedGoal())
                .targetAmount(request.getTargetAmount())
                .targetPeriod(request.getTargetPeriod())
                .build();
            
            PortfolioRecommendationResponse aiRecommendation = portfolioRecommendationService.getRecommendationOnly(
                profile, recommendationRequest);
            log.info("✅ AI 포트폴리오 추천 완료 - 추천 상품 수: {}", aiRecommendation.getRecommendedProducts().size());
            
            // 3. 추천 결과를 DB에 저장
            log.info("💾 추천 상품 DB 저장 시작...");
            List<Plan1QProduct> recommendedProducts = portfolioRecommendationService.saveRecommendedProducts(
                savedGoal, aiRecommendation);
            log.info("✅ 추천 상품 DB 저장 완료 - 저장된 상품 수: {}", recommendedProducts.size());
            
            // 4. 추천 상품을 목표에 연결
            savedGoal.setProducts(recommendedProducts);
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PLAN1Q-GOAL] 목표 생성 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 최종 결과:");
            log.info("   - 목표ID: {}", savedGoal.getPlan1qGoalId());
            log.info("   - 목표명: {}", savedGoal.getGoalName());
            log.info("   - 목표 금액: {}원", savedGoal.getTargetAmount());
            log.info("   - 월 납입금액: {}원", savedGoal.getMonthlyAmount());
            log.info("   - 만기일: {}", savedGoal.getMaturityDate());
            log.info("   - 추천 상품 수: {}", recommendedProducts.size());
            log.info("   - AI 설명: {}", aiRecommendation.getAiExplanation());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return Plan1QGoalDetailResponse.fromWithAIRecommendation(savedGoal, commonCodeService, aiRecommendation);
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL] 목표 생성 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", userSeqNo);
            log.error("💑 커플: {}", coupleId);
            log.error("📝 목표명: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }
    
    /**
     * 추천 결과를 포함한 Plan1Q 목표 생성 (AI 호출 없음)
     */
    @Transactional
    public Plan1QGoalDetailResponse createGoalFromRecommendation(CreatePlan1QGoalRequest request, String userSeqNo, Long coupleId) {
        log.info("-----------------------------------------------------");
        log.info("🎯 [PLAN1Q-GOAL] 추천 결과 기반 목표 생성 시작");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", userSeqNo);
        log.info("💑 커플: {}", coupleId);
        log.info("📝 목표명: {}", request.getGoalTitle());
        log.info("💰 목표 금액: {}원", request.getTargetAmount());
        log.info("⏰ 목표 기간: {}개월", request.getTargetPeriod());
        log.info("📊 추천 상품 수: {}", request.getRecommendedProducts() != null ? request.getRecommendedProducts().size() : 0);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. 투자성향 검사 및 목표 생성
            InvestmentProfile profile = validateAndGetProfile(userSeqNo);
            Plan1QGoal savedGoal = createAndSaveGoal(request, userSeqNo, coupleId, profile);
            
            // 2. 추천 결과를 DB에 저장 (AI 호출 없음)
            if (request.getRecommendedProducts() != null && !request.getRecommendedProducts().isEmpty()) {
                log.info("💾 추천 상품 DB 저장 시작...");
                PortfolioRecommendationResponse recommendation = PortfolioRecommendationResponse.builder()
                    .recommendedProducts(request.getRecommendedProducts())
                    .totalExpectedReturn(request.getTotalExpectedReturn())
                    .achievementProbability(request.getAchievementProbability())
                    .totalRiskScore(request.getTotalRiskScore())
                    .riskAssessment(request.getRiskAssessment())
                    .aiExplanation(request.getAiExplanation())
                    .build();
                
                List<Plan1QProduct> recommendedProducts = portfolioRecommendationService.saveRecommendedProducts(
                    savedGoal, recommendation);
                log.info("✅ 추천 상품 DB 저장 완료 - 저장된 상품 수: {}", recommendedProducts.size());
                
                // 3. 추천 상품을 목표에 연결
                savedGoal.setProducts(recommendedProducts);
            } else {
                log.warn("⚠️ 추천 상품이 없습니다.");
            }
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PLAN1Q-GOAL] 추천 결과 기반 목표 생성 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 최종 결과:");
            log.info("   - 목표ID: {}", savedGoal.getPlan1qGoalId());
            log.info("   - 목표명: {}", savedGoal.getGoalName());
            log.info("   - 목표 금액: {}원", savedGoal.getTargetAmount());
            log.info("   - 월 납입금액: {}원", savedGoal.getMonthlyAmount());
            log.info("   - 만기일: {}", savedGoal.getMaturityDate());
            log.info("   - 추천 상품 수: {}", savedGoal.getProducts() != null ? savedGoal.getProducts().size() : 0);
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return Plan1QGoalDetailResponse.from(savedGoal, commonCodeService);
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL] 추천 결과 기반 목표 생성 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", userSeqNo);
            log.error("💑 커플: {}", coupleId);
            log.error("📝 목표명: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }
    
    /**
     * 월 납입금액 계산
     */
    private BigDecimal calculateMonthlyAmount(BigDecimal targetAmount, Integer targetPeriod) {
        BigDecimal monthlyAmount = targetAmount.divide(BigDecimal.valueOf(targetPeriod), 0, RoundingMode.HALF_UP);
        log.info("💰 월 납입금액 계산: {}원 ÷ {}개월 = {}원", targetAmount, targetPeriod, monthlyAmount);
        return monthlyAmount;
    }
    
    /**
     * 상품별 실제 수익률 계산 (하나은행 API 데이터 기반)
     */
    private BigDecimal calculateProductReturnRate(Plan1QProductResponse product) {
        if (!Boolean.TRUE.equals(product.getSubscribed())) {
            return null; // 가입되지 않은 상품은 계산하지 않음
        }
        
        if ("SAVINGS".equals(product.getProductType())) {
            // 적금: baseRate 사용 (고정 금리)
            return product.getBaseRate() != null ? product.getBaseRate() : BigDecimal.ZERO;
            
        } else if ("FUND".equals(product.getProductType())) {
            // 펀드: 납입금 기반 실제 수익률 계산
            BigDecimal totalDeposit = product.getTotalDeposit(); // 총 납입금
            BigDecimal profit = product.getProfit(); // 수익금
            
            if (totalDeposit != null && totalDeposit.compareTo(BigDecimal.ZERO) > 0 && profit != null) {
                // 실제 수익률 = (수익금 / 총 납입금) × 100
                return profit.multiply(BigDecimal.valueOf(100))
                    .divide(totalDeposit, 2, RoundingMode.HALF_UP);
            } else {
                // 수익금이나 납입금 정보가 없으면 profitRate 사용
                return product.getProfitRate() != null ? product.getProfitRate() : BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 전체 포트폴리오 실제 수익률 계산 (성공한 상품만)
     */
    private BigDecimal calculatePortfolioActualReturnRate(List<Plan1QProductResponse> products) {
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        
        log.info("📊 전체 포트폴리오 수익률 계산 시작");
        
        for (Plan1QProductResponse product : products) {
            // 가입된 상품이면서 실시간 데이터가 있는 상품만 계산
            if (Boolean.TRUE.equals(product.getSubscribed()) && 
                product.getTotalDeposit() != null && product.getProfit() != null) {
                
                BigDecimal deposit = product.getTotalDeposit();
                BigDecimal profit = product.getProfit();
                
                totalDeposit = totalDeposit.add(deposit);
                totalProfit = totalProfit.add(profit);
                
                log.info("🏦 상품: {}, 납입금: {}원, 수익: {}원", 
                    product.getProductName(), deposit, profit);
            }
        }
        
        BigDecimal actualReturnRate = BigDecimal.ZERO;
        if (totalDeposit.compareTo(BigDecimal.ZERO) > 0) {
            actualReturnRate = totalProfit.multiply(BigDecimal.valueOf(100))
                .divide(totalDeposit, 2, RoundingMode.HALF_UP);
        }
        
        log.info("📈 전체 포트폴리오 수익률 계산 완료");
        log.info("   - 총 납입금: {}원", totalDeposit);
        log.info("   - 총 수익: {}원", totalProfit);
        log.info("   - 수익률: {}%", actualReturnRate);
        
        return actualReturnRate;
    }
    
    /**
     * 만기일 계산
     */
    private LocalDate calculateMaturityDate(Integer targetPeriod) {
        LocalDate maturityDate = LocalDate.now().plusMonths(targetPeriod);
        log.info("📅 만기일 계산: {} + {}개월 = {}", LocalDate.now(), targetPeriod, maturityDate);
        return maturityDate;
    }
    
    /**
     * 투자성향 검사 및 프로필 조회 (공통 메서드)
     */
    private InvestmentProfile validateAndGetProfile(String userSeqNo) {
        log.info("🔍 투자성향 검사 결과 확인 중...");
        InvestmentProfile profile = investmentProfileService.getProfileEntity(userSeqNo);
        if (profile == null || profile.isExpired()) {
            log.error("❌ 투자성향 검사가 필요하거나 만료되었습니다.");
            throw new CustomException(ErrorCode.INSUFFICIENT_INVESTMENT_PROFILE, "투자성향 검사가 필요합니다.");
        }
        log.info("✅ 투자성향 검사 확인 완료 - 타입: {}, 점수: {}", profile.getProfileType(), profile.getScore());
        return profile;
    }
    
    /**
     * Plan1Q 목표 생성 및 저장 (공통 메서드)
     */
    private Plan1QGoal createAndSaveGoal(CreatePlan1QGoalRequest request, String userSeqNo, Long coupleId, InvestmentProfile profile) {
        log.info("📝 Plan1Q 목표 엔티티 생성 중...");
        Plan1QGoal goal = Plan1QGoal.builder()
            .goalName(request.getGoalTitle())
            .goalDescription(request.getDetailedGoal())
            .targetAmount(request.getTargetAmount())
            .targetPeriod(request.getTargetPeriod())
            .monthlyAmount(request.getMonthlyAmount() != null ? request.getMonthlyAmount() : calculateMonthlyAmount(request.getTargetAmount(), request.getTargetPeriod()))
            .maturityDate(calculateMaturityDate(request.getTargetPeriod()))
            .status("subscription_in_progress")
            .subscriptionProgress(BigDecimal.ZERO) // 초기 구독 진행률을 0으로 설정
            .riskLevel("low")
            .icon(request.getIcon()) // 아이콘 설정
            .totalExpectedReturn(request.getTotalExpectedReturn() != null ? BigDecimal.valueOf(request.getTotalExpectedReturn()) : null) // 예상 수익률 설정
            .userSeqNo(userSeqNo)
            .coupleId(coupleId)
            .investmentProfileId(profile.getInvestmentProfileId())
            .build();
        
        log.info("💾 Plan1Q 목표 저장 중...");
        Plan1QGoal savedGoal = plan1QGoalRepository.save(goal);
        log.info("✅ Plan1Q 목표 저장 완료 - 목표ID: {}", savedGoal.getPlan1qGoalId());
        return savedGoal;
    }

    /**
     * Plan1Q 목표 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Plan1QGoalDetailResponse> getGoals(Long coupleId) {
        log.info("-----------------------------------------------------");
        log.info("📋 [PLAN1Q-GOAL] 목표 목록 조회 시작");
        log.info("-----------------------------------------------------");
        log.info("💑 커플: {}", coupleId);
        log.info("⏰ 조회 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            List<Plan1QGoal> goals = plan1QGoalRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
            
            List<Plan1QGoalDetailResponse> responses = goals.stream()
                .map(goal -> Plan1QGoalDetailResponse.from(goal, commonCodeService))
                .toList();
            
            // 각 목표에 대해 실시간 데이터 업데이트
            for (Plan1QGoalDetailResponse response : responses) {
                try {
                    updateGoalWithRealTimeData(response, response.getUserSeqNo());
                } catch (Exception e) {
                    log.warn("목표 실시간 데이터 업데이트 실패 - 목표ID: {}, 오류: {}", 
                        response.getGoalId(), e.getMessage());
                    response.setErrorMessage("실시간 데이터를 가져올 수 없습니다");
                }
            }
            
            log.info("-----------------------------------------------------");
            log.info("✅ [PLAN1Q-GOAL] 목표 목록 조회 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 조회 결과:");
            log.info("   - 총 목표 수: {}", responses.size());
            log.info("   - 목표 ID 목록: {}", responses.stream().map(Plan1QGoalDetailResponse::getGoalId).toList());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return responses;
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL] 목표 목록 조회 실패");
            log.error("-----------------------------------------------------");
            log.error("💑 커플: {}", coupleId);
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }

    /**
     * Plan1Q 목표 상세 조회
     */
    public Plan1QGoalDetailResponse getGoalDetail(Long goalId, Long coupleId) {
        log.info("Plan1Q 목표 상세 조회 시작 - 목표ID: {}, 커플ID: {}", goalId, coupleId);
        
        // 1. 목표 조회
        Plan1QGoal goal = plan1QGoalRepository.findByPlan1qGoalIdAndCoupleId(goalId, coupleId)
                .orElseThrow(() -> new Plan1QGoalNotFoundException("Plan1Q 목표를 찾을 수 없습니다."));
        
        // 2. 기본 응답 생성
        Plan1QGoalDetailResponse response = Plan1QGoalDetailResponse.from(goal, commonCodeService);
        
        // 3. 실시간 데이터 업데이트 및 수익률 계산
        try {
            updateGoalWithRealTimeData(response, goal.getUserSeqNo());
        } catch (Exception e) {
            log.warn("목표 실시간 데이터 업데이트 실패 - 목표ID: {}, 오류: {}", 
                goalId, e.getMessage());
            response.setErrorMessage("실시간 데이터를 가져올 수 없습니다");
        }
        
        log.info("Plan1Q 목표 상세 조회 완료 - 목표ID: {}, 상품 수: {}, currentAmount: {}원", 
            goalId, response.getProducts().size(), response.getCurrentAmount());
        return response;
    }

    /**
     * Plan1Q 목표 수정
     */
    @Transactional
    public Plan1QGoalDetailResponse updateGoal(Long goalId, Long coupleId, CreatePlan1QGoalRequest request) {
        log.info("-----------------------------------------------------");
        log.info("✏️ [PLAN1Q-GOAL] 목표 수정 시작");
        log.info("-----------------------------------------------------");
        log.info("🎯 목표ID: {}", goalId);
        log.info("💑 커플: {}", coupleId);
        log.info("📝 수정할 목표명: {}", request.getGoalTitle());
        log.info("💰 수정할 목표 금액: {}원", request.getTargetAmount());
        log.info("⏰ 수정할 목표 기간: {}개월", request.getTargetPeriod());
        log.info("⏰ 수정 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            Plan1QGoal goal = plan1QGoalRepository.findByPlan1qGoalIdAndCoupleId(goalId, coupleId)
                .orElseThrow(() -> {
                    log.error("❌ Plan1Q 목표를 찾을 수 없습니다. - 목표ID: {}, 커플: {}", goalId, coupleId);
                    return new CustomException(ErrorCode.PLAN1Q_GOAL_NOT_FOUND, "Plan1Q 목표를 찾을 수 없습니다.");
                });
            
            // 목표 정보 업데이트
            goal.updateGoal(
                request.getGoalTitle(),
                request.getDetailedGoal(),
                request.getTargetAmount(),
                request.getTargetPeriod(),
                request.getMonthlyAmount() != null ? request.getMonthlyAmount() : calculateMonthlyAmount(request.getTargetAmount(), request.getTargetPeriod()),
                calculateMaturityDate(request.getTargetPeriod())
            );
            
            Plan1QGoal savedGoal = plan1QGoalRepository.save(goal);
            Plan1QGoalDetailResponse response = Plan1QGoalDetailResponse.from(savedGoal, commonCodeService);
            
            log.info("-----------------------------------------------------");
            log.info("✅ [PLAN1Q-GOAL] 목표 수정 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 수정 결과:");
            log.info("   - 목표ID: {}", response.getGoalId());
            log.info("   - 목표명: {}", response.getGoalName());
            log.info("   - 목표 금액: {}원", response.getTargetAmount());
            log.info("   - 월 납입금액: {}원", response.getMonthlyAmount());
            log.info("   - 만기일: {}", response.getMaturityDate());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return response;
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL] 목표 수정 실패");
            log.error("-----------------------------------------------------");
            log.error("🎯 목표ID: {}", goalId);
            log.error("💑 커플: {}", coupleId);
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }

    /**
     * Plan1Q 목표 삭제
     */
    @Transactional
    public void deleteGoal(Long goalId, Long coupleId) {
        log.info("-----------------------------------------------------");
        log.info("🗑️ [PLAN1Q-GOAL] 목표 삭제 시작");
        log.info("-----------------------------------------------------");
        log.info("🎯 목표ID: {}", goalId);
        log.info("💑 커플: {}", coupleId);
        log.info("⏰ 삭제 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            Plan1QGoal goal = plan1QGoalRepository.findByPlan1qGoalIdAndCoupleId(goalId, coupleId)
                .orElseThrow(() -> {
                    log.error("❌ Plan1Q 목표를 찾을 수 없습니다. - 목표ID: {}, 커플: {}", goalId, coupleId);
                    return new CustomException(ErrorCode.PLAN1Q_GOAL_NOT_FOUND, "Plan1Q 목표를 찾을 수 없습니다.");
                });
            
            // 목표 삭제 (연관된 상품들도 함께 삭제됨 - Cascade 설정)
            plan1QGoalRepository.delete(goal);
            
            log.info("-----------------------------------------------------");
            log.info("✅ [PLAN1Q-GOAL] 목표 삭제 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 삭제 결과:");
            log.info("   - 삭제된 목표ID: {}", goalId);
            log.info("   - 삭제된 목표명: {}", goal.getGoalName());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL] 목표 삭제 실패");
            log.error("-----------------------------------------------------");
            log.error("🎯 목표ID: {}", goalId);
            log.error("💑 커플: {}", coupleId);
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }
    
    /**
     * userSeqNo를 userCi로 변환
     */
    private String getUserCiFromUserSeqNo(String userSeqNo) {
        log.info("userSeqNo를 userCi로 변환 시작 - userSeqNo: {}", userSeqNo);
        
        Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userSeqNo: {}", userSeqNo);
                    return new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        String userCi = customer.getUserCi();
        log.info("userCi 변환 완료 - userSeqNo: {} -> userCi: {}", userSeqNo, userCi);
        
        return userCi;
    }
    
    // ==================== 실시간 데이터 업데이트 공통 메서드들 ====================
    
    /**
     * 목표에 실시간 데이터 업데이트 및 수익률 계산 (공통 메서드)
     */
    private void updateGoalWithRealTimeData(Plan1QGoalDetailResponse response, String userSeqNo) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 가입된 상품에 대해 하나은행 API 호출하여 실시간 데이터 조회 (개별 처리)
            boolean allSuccess = updateProductsWithRealTimeData(response.getProducts(), userSeqNo);
            
            // 2. 현재 모은 금액 계산 및 업데이트 (성공한 상품만)
            calculateAndUpdateCurrentAmount(response);
            
            // 3. 실제 수익률 계산 및 업데이트 (성공한 상품만)
            calculateAndUpdateActualReturnRate(response);
            
            if (allSuccess) {
                log.info("✅ 실시간 데이터 업데이트 완료 - 목표ID: {}, 목표명: {}", 
                    response.getGoalId(), response.getGoalName());
            } else {
                log.info("⚠️ 일부 실시간 데이터 업데이트 실패 - 목표ID: {}, 목표명: {}", 
                    response.getGoalId(), response.getGoalName());
                response.setErrorMessage("실시간 데이터를 가져올 수 없습니다");
            }
            
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("⏱️ 실시간 데이터 업데이트 완료 - 목표ID: {}, 소요시간: {}ms", 
                response.getGoalId(), (endTime - startTime));
        }
    }
    
    /**
     * 상품 목록에 실시간 데이터 업데이트 (개별 처리)
     * @return 모든 API 호출이 성공했는지 여부
     */
    private boolean updateProductsWithRealTimeData(List<Plan1QProductResponse> products, String userSeqNo) {
        boolean allSuccess = true;
        
        for (Plan1QProductResponse product : products) {
            if (product.getSubscribed() && product.getAccountNumber() != null) {
                try {
                    updateProductWithRealTimeData(product, userSeqNo);
                    log.info("✅ 상품 실시간 데이터 업데이트 성공 - 계좌번호: {}, 상품명: {}", 
                        product.getAccountNumber(), product.getProductName());
                } catch (Exception e) {
                    log.info("❌ 상품 실시간 데이터 업데이트 실패 - 계좌번호: {}, 상품명: {}, 오류: {}", 
                        product.getAccountNumber(), product.getProductName(), e.getMessage());
                    // 실패한 상품만 null로 설정
                    setProductDataToNull(product);
                    allSuccess = false;
                }
            }
        }
        
        return allSuccess;
    }
    
    /**
     * 개별 상품에 실시간 데이터 업데이트
     */
    private void updateProductWithRealTimeData(Plan1QProductResponse product, String userSeqNo) {
        // userSeqNo를 userCi로 변환
        String userCi = getUserCiFromUserSeqNo(userSeqNo);
        
        // 하나은행 API 호출하여 실시간 데이터 조회
        AccountProfitInfoResponse hanaData = hanaBankApiService.getAccountProfitInfo(
            product.getAccountNumber(), 
            userCi
        );
        
        // 하나은행 API 응답을 상품 데이터에 설정
        setProductRealTimeData(product, hanaData);
    }
    
    /**
     * 하나은행 API 응답을 상품 데이터에 설정
     */
    private void setProductRealTimeData(Plan1QProductResponse product, AccountProfitInfoResponse hanaData) {
        log.info("-----------------------------------------------------");
        log.info("💰 [HANA-API-RESPONSE] 하나은행 API 응답 데이터 설정 시작");
        log.info("-----------------------------------------------------");
        log.info("🏦 계좌번호: {}", product.getAccountNumber());
        log.info("📊 상품명: {}", product.getProductName());
        log.info("🔄 설정 전 데이터:");
        log.info("   - 현재잔액: {}", product.getCurrentBalance());
        log.info("   - 총입금액: {}", product.getTotalDeposit());
        log.info("   - 수익금: {}", product.getProfit());
        log.info("   - 최종업데이트: {}", product.getLastUpdated());
        
        log.info("📥 하나은행 API 응답 데이터:");
        log.info("   - currentBalance: {}", hanaData.getCurrentBalance());
        log.info("   - totalDeposit: {}", hanaData.getTotalDeposit());
        log.info("   - profit: {}", hanaData.getProfit());
        log.info("   - lastUpdated: {}", hanaData.getLastUpdated());
        log.info("   - baseRate: {}", hanaData.getBaseRate());
        log.info("   - profitRate: {}", hanaData.getProfitRate());
        log.info("   - productType: {}", hanaData.getProductType());
        
        product.setCurrentBalance(hanaData.getCurrentBalance());
        product.setTotalDeposit(hanaData.getTotalDeposit());
        product.setProfit(hanaData.getProfit());
        product.setLastUpdated(hanaData.getLastUpdated());
        
        // 상품 타입별 수익률 설정
        if ("SAVINGS".equals(product.getProductType())) {
            // 적금: baseRate 사용
            product.setBaseRate(hanaData.getBaseRate());
            product.setReturnRate(hanaData.getBaseRate()); // 백워드 호환성
        } else if ("FUND".equals(product.getProductType())) {
            // 펀드: profitRate 사용
            product.setProfitRate(hanaData.getProfitRate());
            product.setReturnRate(hanaData.getProfitRate()); // 백워드 호환성
        }
        
        log.info("✅ 하나은행 API 응답 데이터 설정 완료");
        log.info("🔄 설정 후 데이터:");
        log.info("   - 현재잔액: {}", product.getCurrentBalance());
        log.info("   - 총입금액: {}", product.getTotalDeposit());
        log.info("   - 수익금: {}", product.getProfit());
        log.info("   - 최종업데이트: {}", product.getLastUpdated());
        log.info("-----------------------------------------------------");
    }
    
    /**
     * 실패한 상품의 데이터를 null로 설정
     */
    private void setProductDataToNull(Plan1QProductResponse product) {
        product.setCurrentBalance(null);
        product.setTotalDeposit(null);
        product.setProfit(null);
        product.setLastUpdated(null);
        product.setBaseRate(null);
        product.setProfitRate(null);
        product.setReturnRate(null);
    }
    
    /**
     * 현재 모은 금액 계산 및 업데이트
     */
    private void calculateAndUpdateCurrentAmount(Plan1QGoalDetailResponse response) {
        BigDecimal totalCurrentAmount = response.getProducts().stream()
            .filter(product -> Boolean.TRUE.equals(product.getSubscribed()))
            .filter(product -> product.getCurrentBalance() != null) // null이 아닌 상품만
            .map(product -> product.getCurrentBalance())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        response.setCurrentAmount(totalCurrentAmount);
        
        log.info("💰 현재 모은 금액 계산 완료 - 목표ID: {}, 현재 금액: {}원", 
            response.getGoalId(), totalCurrentAmount);
    }
    
    /**
     * 실제 수익률 계산 및 업데이트
     */
    private void calculateAndUpdateActualReturnRate(Plan1QGoalDetailResponse response) {
        boolean hasSubscribedProducts = response.getProducts().stream()
            .anyMatch(product -> Boolean.TRUE.equals(product.getSubscribed()));
        
        if (hasSubscribedProducts) {
            try {
                log.info("📊 가입 완료된 상품이 있으므로 실제 수익률 계산 시작 - 목표ID: {}", response.getGoalId());
                BigDecimal actualReturnRate = calculatePortfolioActualReturnRate(response.getProducts());
                response.setActualReturnRate(actualReturnRate);
                
                log.info("✅ 실제 수익률 계산 완료 - 목표ID: {}, 실제 수익률: {}%", 
                    response.getGoalId(), actualReturnRate);
            } catch (Exception e) {
                log.info("❌ 실제 수익률 계산 중 오류 발생 - 목표ID: {}, 오류: {}", 
                    response.getGoalId(), e.getMessage());
                response.setActualReturnRate(null);
            }
        } else {
            log.info("📊 가입 완료된 상품이 없으므로 AI 추천 예상 수익률 사용 - 목표ID: {}", response.getGoalId());
        }
    }
}

