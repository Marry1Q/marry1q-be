package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.Plan1QRecommendationRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.service.InvestmentProfileService;
import com.marry1q.marry1qbe.domain.plan1q.service.PortfolioRecommendationService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.domain.couple.exception.NoCoupleException;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan1q")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan1Q 추천 관리", description = "Plan1Q AI 포트폴리오 추천 API")
@SecurityRequirement(name = "Bearer Authentication")
public class Plan1QRecommendationController {
    
    private final InvestmentProfileService investmentProfileService;
    private final PortfolioRecommendationService portfolioRecommendationService;
    private final CoupleService coupleService;
    
    /**
     * AI 포트폴리오 추천 조회
     */
    @PostMapping("/recommendations")
    @Operation(
        summary = "AI 포트폴리오 추천 조회", 
        description = "사용자의 투자성향과 목표를 바탕으로 AI 포트폴리오 추천을 받습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "추천 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PortfolioRecommendationResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                      "success": true,
                      "data": {
                        "totalExpectedReturn": 5.38,
                        "totalRiskScore": 25,
                        "riskAssessment": "낮음",
                        "aiExplanation": "사용자님의 투자 성향을 고려하여...",
                        "recommendedProducts": [
                          {
                            "productId": 1,
                            "productName": "하나 프리미엄 적금",
                            "productType": "savings",
                            "investmentRatio": 40.0,
                            "investmentAmount": 40000000,
                            "monthlyAmount": 3333333,
                            "recommendationReason": "신혼부부 대상 비과세 혜택과..."
                          }
                        ]
                      },
                      "message": "AI 포트폴리오 추천이 성공적으로 생성되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "에러 응답 예시",
                    value = """
                    {
                      "success": false,
                      "data": null,
                      "message": "투자성향 검사가 필요합니다.",
                      "error": {
                        "code": "INSUFFICIENT_INVESTMENT_PROFILE",
                        "message": "투자성향 검사가 필요합니다."
                      },
                      "timestamp": "2024-01-01T12:00:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CustomApiResponse<PortfolioRecommendationResponse>> getRecommendation(
            @Valid @RequestBody Plan1QRecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        
        log.info("-----------------------------------------------------");
        log.info("🎯 [PLAN1Q-RECOMMENDATION] AI 추천 요청");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", currentUserSeqNo);
        log.info("📝 목표명: {}", request.getGoalTitle());
        log.info("💰 목표 금액: {}원", request.getTargetAmount());
        log.info("⏰ 목표 기간: {}개월", request.getTargetPeriod());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. 투자성향 검사 필요 여부 확인
            log.info("🔍 투자성향 검사 결과 확인 중...");
            InvestmentProfile profile = investmentProfileService.getProfileEntity(currentUserSeqNo);
            if (profile == null || profile.isExpired()) {
                log.error("❌ 투자성향 검사가 필요하거나 만료되었습니다.");
                throw new CustomException(ErrorCode.INSUFFICIENT_INVESTMENT_PROFILE, "투자성향 검사가 필요합니다.");
            }
            log.info("✅ 투자성향 검사 확인 완료 - 타입: {}, 점수: {}", profile.getProfileType(), profile.getScore());
            
            // 2. 커플 ID 조회 (예외 처리 포함)
            Long coupleId;
            try {
                coupleId = coupleService.getCurrentCoupleId();
                log.info("✅ 커플 ID 조회 완료: {}", coupleId);
            } catch (NoCoupleException e) {
                log.error("❌ 사용자가 커플에 속해있지 않습니다: {}", e.getMessage());
                throw new CustomException(ErrorCode.INSUFFICIENT_INVESTMENT_PROFILE, "커플 정보가 필요합니다. 먼저 커플을 생성해주세요.");
            }
            
            // 3. AI 포트폴리오 추천
            log.info("🤖 AI 포트폴리오 추천 시작...");
            PortfolioRecommendationResponse recommendation = portfolioRecommendationService.getRecommendationOnly(
                profile, request);
            log.info("✅ AI 포트폴리오 추천 완료 - 추천 상품 수: {}", recommendation.getRecommendedProducts().size());
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PLAN1Q-RECOMMENDATION] AI 추천 완료");
            log.info("-----------------------------------------------------");
            log.info("📊 추천 상품 수: {}", recommendation.getRecommendedProducts().size());
            log.info("💰 총 예상 수익률: {}%", recommendation.getTotalExpectedReturn());
            log.info("⚠️ 총 위험도 점수: {}", recommendation.getTotalRiskScore());
            log.info("📝 AI 설명: {}", recommendation.getAiExplanation());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return ResponseEntity.ok(CustomApiResponse.success(recommendation, "AI 포트폴리오 추천이 성공적으로 생성되었습니다."));
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-RECOMMENDATION] AI 추천 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", currentUserSeqNo);
            log.error("📝 목표명: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }
}
