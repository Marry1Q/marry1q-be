package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.CreatePlan1QGoalRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.Plan1QGoalDetailResponse;
import com.marry1q.marry1qbe.domain.plan1q.service.Plan1QGoalService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

@RestController
@RequestMapping("/api/plan1q/goals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan1Q 목표 관리", description = "Plan1Q 목표 생성, 조회, 수정, 삭제 API")
@SecurityRequirement(name = "Bearer Authentication")
public class Plan1QGoalController {
    
    private final Plan1QGoalService plan1QGoalService;
    private final CoupleService coupleService;
    
    /**
     * Plan1Q 목표 생성 (추천 결과 기반)
     */
    @PostMapping
    @Operation(
        summary = "Plan1Q 목표 생성 (추천 결과 기반)", 
        description = "AI 추천 결과를 포함하여 Plan1Q 목표를 생성합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "목표 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Plan1QGoalDetailResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                      "success": true,
                      "data": {
                        "goalId": 1,
                        "goalName": "신혼집 마련",
                        "goalDescription": "3년 내에 신혼집을 마련하고 싶습니다.",
                        "targetAmount": 100000000,
                        "currentAmount": 0,
                        "expectedReturn": 3.2,
                        "targetPeriod": 36,
                        "maturityDate": "2027-01-01",
                        "monthlyAmount": 2777778,
                        "status": "in_progress",
                        "statusName": "진행중",
                        "subscriptionProgress": 0.0,
                        "riskLevel": "low",
                        "riskLevelName": "낮음",
                        "icon": "house_deposit",
                        "color": "bg-blue-100 text-blue-600",
                        "userSeqNo": "F999999999",
                        "coupleId": 1,
                        "investmentProfileId": 1,
                        "createdAt": "2024-01-01T12:00:00",
                        "updatedAt": "2024-01-01T12:00:00",
                        "products": [
                          {
                            "productId": 1,
                            "productName": "하나은행 안정형 펀드",
                            "productType": "fund",
                            "productTypeName": "펀드",
                            "investmentRatio": 60.0,
                            "investmentAmount": 60000000,
                            "returnRate": 3.2,
                            "monthlyAmount": 1666667,
                            "subscribed": false,
                            "currentValue": 0,
                            "profit": 0,
                            "contractDate": null,
                            "maturityDate": "2027-01-01",
                            "terms": "펀드 투자 약관",
                            "contract": "펀드 계약서",
                            "accountNumber": null,
                            "riskLevel": "low",
                            "riskType": "conservative",
                            "assetClass": "bond",
                            "strategy": "안정형 투자",
                            "interestRate": "3.2%",
                            "period": "36개월",
                            "hanaBankProductId": 1001,
                            "hanaBankSubscriptionId": null,
                            "createdAt": "2024-01-01T12:00:00",
                            "updatedAt": "2024-01-01T12:00:00",
                            "plan1qGoalId": 1
                          },
                          {
                            "productId": 2,
                            "productName": "하나은행 정기예금",
                            "productType": "savings",
                            "productTypeName": "예금",
                            "investmentRatio": 40.0,
                            "investmentAmount": 40000000,
                            "returnRate": 3.2,
                            "monthlyAmount": 1111111,
                            "subscribed": false,
                            "currentValue": 0,
                            "profit": 0,
                            "contractDate": null,
                            "maturityDate": "2027-01-01",
                            "terms": "정기예금 약관",
                            "contract": "예금 계약서",
                            "accountNumber": null,
                            "riskLevel": "low",
                            "riskType": "conservative",
                            "assetClass": "deposit",
                            "strategy": "안정형 저축",
                            "interestRate": "3.2%",
                            "period": "36개월",
                            "hanaBankProductId": 1002,
                            "hanaBankSubscriptionId": null,
                            "createdAt": "2024-01-01T12:00:00",
                            "updatedAt": "2024-01-01T12:00:00",
                            "plan1qGoalId": 1
                          }
                        ]
                      },
                      "message": "Plan1Q 목표가 성공적으로 생성되었습니다."
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
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류 예시",
                    value = """
                    {
                      "success": false,
                      "data": null,
                      "message": "AI 포트폴리오 추천에 실패했습니다. 다시 시도해주세요.",
                      "error": {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "서버 내부 오류가 발생했습니다."
                      },
                      "timestamp": "2024-01-01T12:00:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CustomApiResponse<Plan1QGoalDetailResponse>> createGoal(
            @Valid @RequestBody CreatePlan1QGoalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        log.info("-----------------------------------------------------");
        log.info("🎯 [PLAN1Q-GOAL-CONTROLLER] 추천 결과 기반 목표 생성 API 호출");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", currentUserSeqNo);
        log.info("💑 커플: {}", coupleId);
        log.info("📝 목표명: {}", request.getGoalTitle());
        log.info("💰 목표 금액: {}원", request.getTargetAmount());
        log.info("⏰ 목표 기간: {}개월", request.getTargetPeriod());
        log.info("📊 추천 상품 수: {}", request.getRecommendedProducts() != null ? request.getRecommendedProducts().size() : 0);
        log.info("⏰ API 호출 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            Plan1QGoalDetailResponse goal = plan1QGoalService.createGoalFromRecommendation(request, currentUserSeqNo, coupleId);
            
            log.info("-----------------------------------------------------");
            log.info("🎉 [PLAN1Q-GOAL-CONTROLLER] 추천 결과 기반 목표 생성 API 성공");
            log.info("-----------------------------------------------------");
            log.info("📊 생성된 목표 ID: {}", goal.getGoalId());
            log.info("📦 저장된 상품 수: {}", goal.getProducts().size());
            log.info("⏰ API 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return ResponseEntity.ok(CustomApiResponse.success(goal, "Plan1Q 목표가 성공적으로 생성되었습니다."));
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PLAN1Q-GOAL-CONTROLLER] 추천 결과 기반 목표 생성 API 실패");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", currentUserSeqNo);
            log.error("💑 커플: {}", coupleId);
            log.error("📝 목표명: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ API 실패 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw e;
        }
    }
    
    /**
     * Plan1Q 목표 상세 조회
     */
    @GetMapping("/{goalId}")
    @Operation(
        summary = "Plan1Q 목표 상세 조회", 
        description = "특정 Plan1Q 목표의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "목표 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Plan1QGoalDetailResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "목표를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "목표 없음 예시",
                    value = """
                    {
                      "success": false,
                      "data": null,
                      "message": "Plan1Q 목표를 찾을 수 없습니다.",
                      "error": {
                        "code": "PLAN1Q_GOAL_NOT_FOUND",
                        "message": "Plan1Q 목표를 찾을 수 없습니다."
                      },
                      "timestamp": "2024-01-01T12:00:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CustomApiResponse<Plan1QGoalDetailResponse>> getGoalDetail(
            @Parameter(description = "목표 ID", example = "1") @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        log.info("Plan1Q 목표 상세 조회 API 호출 - 목표ID: {}, 사용자: {}, 커플: {}", 
                goalId, currentUserSeqNo, coupleId);
        
        Plan1QGoalDetailResponse goal = plan1QGoalService.getGoalDetail(goalId, coupleId);
        
        // 에러 메시지가 있으면 해당 메시지로 응답
        if (goal.getErrorMessage() != null) {
            return ResponseEntity.ok(CustomApiResponse.success(goal, goal.getErrorMessage()));
        }
        
        return ResponseEntity.ok(CustomApiResponse.success(goal, "Plan1Q 목표 상세 정보를 조회했습니다."));
    }
    
    /**
     * Plan1Q 목표 수정
     */
    @PutMapping("/{goalId}")
    @Operation(
        summary = "Plan1Q 목표 수정", 
        description = "Plan1Q 목표 정보를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "목표 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Plan1QGoalDetailResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "목표를 찾을 수 없음"
        )
    })
    public ResponseEntity<CustomApiResponse<Plan1QGoalDetailResponse>> updateGoal(
            @Parameter(description = "목표 ID", example = "1") @PathVariable Long goalId,
            @Valid @RequestBody CreatePlan1QGoalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        log.info("Plan1Q 목표 수정 API 호출 - 목표ID: {}, 사용자: {}, 커플: {}", 
                goalId, currentUserSeqNo, coupleId);
        
        Plan1QGoalDetailResponse goal = plan1QGoalService.updateGoal(goalId, coupleId, request);
        
        return ResponseEntity.ok(CustomApiResponse.success(goal, "Plan1Q 목표가 수정되었습니다."));
    }
    
    /**
     * Plan1Q 목표 삭제
     */
    @DeleteMapping("/{goalId}")
    @Operation(
        summary = "Plan1Q 목표 삭제", 
        description = "Plan1Q 목표를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "목표 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "목표를 찾을 수 없음"
        )
    })
    public ResponseEntity<CustomApiResponse<Void>> deleteGoal(
            @Parameter(description = "목표 ID", example = "1") @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        log.info("Plan1Q 목표 삭제 API 호출 - 목표ID: {}, 사용자: {}, 커플: {}", 
                goalId, currentUserSeqNo, coupleId);
        
        plan1QGoalService.deleteGoal(goalId, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(null, "Plan1Q 목표가 삭제되었습니다."));
    }
    
    /**
     * Plan1Q 목표 목록 조회
     */
    @GetMapping
    @Operation(
        summary = "Plan1Q 목표 목록 조회", 
        description = "현재 커플의 Plan1Q 목표 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "목록 조회 성공 예시",
                    value = """
                    {
                      "success": true,
                      "data": [
                        {
                          "goalId": 1,
                          "goalName": "신혼집 마련",
                          "targetAmount": 100000000,
                          "currentAmount": 0,
                          "expectedReturn": 3.2,
                          "targetPeriod": 36,
                          "maturityDate": "2027-01-01",
                          "monthlyAmount": 2777778,
                          "status": "in_progress",
                          "statusName": "진행중",
                          "subscriptionProgress": 0.0,
                          "riskLevel": "low",
                          "riskLevelName": "낮음",
                          "icon": "house_deposit",
                          "color": "bg-blue-100 text-blue-600",
                          "createdAt": "2024-01-01T12:00:00",
                          "updatedAt": "2024-01-01T12:00:00"
                        }
                      ],
                      "message": "Plan1Q 목표 목록을 조회했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CustomApiResponse<List<Plan1QGoalDetailResponse>>> getGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        log.info("Plan1Q 목표 목록 조회 API 호출 - 사용자: {}, 커플: {}", currentUserSeqNo, coupleId);
        
        List<Plan1QGoalDetailResponse> goals = plan1QGoalService.getGoals(coupleId);
        
        // 일부 목표에 에러 메시지가 있으면 전체 실패 메시지로 응답
        boolean hasError = goals.stream().anyMatch(goal -> goal.getErrorMessage() != null);
        String message = hasError ? "일부 목표의 실시간 데이터를 가져올 수 없습니다" : "Plan1Q 목표 목록을 조회했습니다.";
        
        return ResponseEntity.ok(CustomApiResponse.success(goals, message));
    }
}
