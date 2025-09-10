package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.ProductSubscriptionRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.ManualPaymentRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.ProductSubscriptionResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.ManualPaymentResponse;
import com.marry1q.marry1qbe.domain.plan1q.service.ProductSubscriptionService;
import com.marry1q.marry1qbe.domain.plan1q.service.ManualPaymentService;
import com.marry1q.marry1qbe.domain.account.service.AccountService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/plan1q/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan1Q 상품 가입", description = "Plan1Q 상품 가입 및 계좌 개설 API")
public class ProductSubscriptionController {
    
    private final ProductSubscriptionService productSubscriptionService;
    private final ManualPaymentService manualPaymentService;
    private final AccountService accountService;
    
    /**
     * 상품 가입 및 계좌 개설
     */
    @PostMapping("/subscribe")
    @Operation(summary = "상품 가입 및 계좌 개설", description = "Plan1Q 상품에 가입하고 하나은행에 계좌를 개설합니다.")
    public ResponseEntity<CustomApiResponse<ProductSubscriptionResponse>> subscribeProduct(
            @Valid @RequestBody ProductSubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("-----------------------------------------------------");
        log.info("🚀 [PRODUCT-SUBSCRIPTION-CONTROLLER] 상품 가입 요청");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", userDetails.getUsername());
        log.info("📦 상품ID: {}", request.getProductId());
        log.info("💰 월 납입금: {}", request.getMonthlyAmount());
        log.info("📅 기간: {} 개월", request.getPeriodMonths());
        log.info("📅 납부일: {}", request.getPaymentDate());
        log.info("💳 출금계좌: {}", request.getSourceAccountNumber());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ProductSubscriptionResponse response = productSubscriptionService.subscribeProduct(
                userDetails.getUsername(), request);
            
            // 거래 완료 후 즉시 동기화 실행 (거래내역 반영)
            try {
                accountService.syncTransactions();
                log.info("상품 가입 후 동기화 완료");
            } catch (Exception syncException) {
                log.warn("상품 가입 후 동기화 실패 (거래는 성공): {}", syncException.getMessage());
            }
            
            log.info("-----------------------------------------------------");
            log.info("✅ [PRODUCT-SUBSCRIPTION-CONTROLLER] 상품 가입 성공");
            log.info("📦 상품명: {}", response.getProductName());
            log.info("💳 계좌번호: {}", response.getAccountNumber());
            log.info("💰 가입금액: {}", response.getAmount());
            log.info("📅 만기일: {}", response.getMaturityDate());
            log.info("⏰ 완료 시간: {}", java.time.LocalDateTime.now());
            log.info("-----------------------------------------------------");
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "상품 가입이 성공적으로 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [PRODUCT-SUBSCRIPTION-CONTROLLER] 상품 가입 실패");
            log.error("👤 사용자: {}", userDetails.getUsername());
            log.error("📦 상품ID: {}", request.getProductId());
            log.error("❌ 오류: {}", e.getMessage());
            log.error("⏰ 실패 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error("SUBSCRIPTION_FAILED", "상품 가입에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * Plan1Q 상품 수동납입
     */
    @PostMapping("/manual-payment")
    @Operation(summary = "Plan1Q 상품 수동납입", description = "사용자가 Plan1Q 상품에 수동으로 납입하는 기능")
    public ResponseEntity<CustomApiResponse<ManualPaymentResponse>> manualPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ManualPaymentRequest request) {
        
        log.info("수동납입 요청 - 자동이체ID: {}, 금액: {}", request.getAutoTransferId(), request.getAmount());
        
        try {
            ManualPaymentResponse response = manualPaymentService.processManualPayment(request);
            
            String message = String.format("%,d원이 성공적으로 납입되었습니다. (회차: %d/%d)", 
                                         request.getAmount().intValue(),
                                         response.getCurrentInstallment(),
                                         response.getCurrentInstallment() + response.getRemainingInstallments());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, message));
            
        } catch (Exception e) {
            log.error("수동납입 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error("MANUAL_PAYMENT_FAILED", e.getMessage()));
        }
    }
}
