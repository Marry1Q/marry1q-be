package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.service.Plan1QProductStatusService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan1q/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan1Q 상품 관리", description = "Plan1Q 상품 가입 상태 관리 API")
public class Plan1QProductController {
    
    private final Plan1QProductStatusService plan1QProductStatusService;
    
    /**
     * 상품 가입 상태 변경
     */
    @PutMapping("/{productId}/subscription-status")
    @Operation(summary = "상품 가입 상태 변경", description = "Plan1Q 상품의 가입 상태를 변경합니다.")
    public ResponseEntity<CustomApiResponse<String>> updateProductSubscriptionStatus(
            @PathVariable Long productId,
            @RequestParam Boolean subscribed,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("-----------------------------------------------------");
        log.info("🔄 [PLAN1Q-PRODUCT-CONTROLLER] 상품 가입 상태 변경 요청");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", userDetails.getUsername());
        log.info("📦 상품ID: {}", productId);
        log.info("✅ 가입상태: {}", subscribed);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            plan1QProductStatusService.updateProductSubscriptionStatus(productId, subscribed);
            
            String message = subscribed ? "상품 가입이 완료되었습니다." : "상품 가입이 취소되었습니다.";
            
            log.info("✅ 상품 가입 상태 변경 완료 - 상품ID: {}", productId);
            
            return ResponseEntity.ok(CustomApiResponse.success(message));
            
        } catch (Exception e) {
            log.error("❌ 상품 가입 상태 변경 실패 - 상품ID: {}, 오류: {}", productId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error("상품 가입 상태 변경에 실패했습니다.", e.getMessage()));
        }
    }
    
    /**
     * 목표별 상품 가입 진행률 조회
     */
    @GetMapping("/goals/{goalId}/subscription-progress")
    @Operation(summary = "목표별 상품 가입 진행률 조회", description = "특정 목표의 상품 가입 진행률을 조회합니다.")
    public ResponseEntity<CustomApiResponse<Double>> getGoalSubscriptionProgress(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📊 목표별 상품 가입 진행률 조회 - 목표ID: {}, 사용자: {}", goalId, userDetails.getUsername());
        
        try {
            double progress = plan1QProductStatusService.getGoalSubscriptionProgress(goalId);
            
            log.info("✅ 목표별 상품 가입 진행률 조회 완료 - 목표ID: {}, 진행률: {}%", goalId, progress);
            
            return ResponseEntity.ok(CustomApiResponse.success(progress));
            
        } catch (Exception e) {
            log.error("❌ 목표별 상품 가입 진행률 조회 실패 - 목표ID: {}, 오류: {}", goalId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error("목표별 상품 가입 진행률 조회에 실패했습니다.", e.getMessage()));
        }
    }
}
