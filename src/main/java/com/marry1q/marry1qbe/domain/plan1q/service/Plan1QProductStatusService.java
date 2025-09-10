package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QGoalRepository;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QProductRepository;
import com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class Plan1QProductStatusService {
    
    private final Plan1QProductRepository plan1QProductRepository;
    private final Plan1QGoalRepository plan1QGoalRepository;
    private final CommonCodeService commonCodeService;
    
    /**
     * 상품 가입 상태 변경
     */
    @Transactional
    public void updateProductSubscriptionStatus(Long productId, Boolean subscribed) {
        log.info("-----------------------------------------------------");
        log.info("🔄 [PLAN1Q-PRODUCT-STATUS] 상품 가입 상태 변경");
        log.info("-----------------------------------------------------");
        log.info("📦 상품ID: {}", productId);
        log.info("✅ 가입상태: {}", subscribed);
        log.info("⏰ 변경 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        Plan1QProduct product = plan1QProductRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. 상품ID: " + productId));
        
        product.setSubscribed(subscribed);
        plan1QProductRepository.save(product);
        
        log.info("✅ 상품 가입 상태 변경 완료 - 상품명: {}", product.getProductName());
        
        // 목표 활성화 상태 확인
        checkAndUpdateGoalActivationStatus(product.getPlan1QGoal().getPlan1qGoalId());
    }
    
    /**
     * 목표 활성화 상태 확인 및 업데이트
     */
    @Transactional
    public void checkAndUpdateGoalActivationStatus(Long goalId) {
        log.info("🔍 목표 활성화 상태 확인 - 목표ID: {}", goalId);
        
        Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
            .orElseThrow(() -> new EntityNotFoundException("목표를 찾을 수 없습니다. 목표ID: " + goalId));
        
        List<Plan1QProduct> products = plan1QProductRepository.findByPlan1QGoal(goal);
        
        log.info("📊 목표별 상품 수: {}개", products.size());
        
        // 모든 상품이 가입 완료되었는지 확인
        long subscribedCount = products.stream()
            .filter(Plan1QProduct::getSubscribed)
            .count();

        log.info("✅ 가입 완료된 상품 수: {}/{}", subscribedCount, products.size());

        if (products.size() > 0 && subscribedCount == products.size()) {
            // 모든 상품이 가입 완료된 경우 목표 상태를 'in_progress'로 변경 (운용중)
            goal.setStatus("in_progress");
            plan1QGoalRepository.save(goal);
            log.info("🎉 모든 상품 가입 완료 → 목표 상태 'in_progress'로 전환 - 목표ID: {}", goalId);
        } else {
            // 일부라도 미가입이 있으면 'subscription_in_progress'
            goal.setStatus("subscription_in_progress");
            plan1QGoalRepository.save(goal);
            log.info("⏳ 일부 상품 미가입 → 목표 상태 'subscription_in_progress' - 목표ID: {}", goalId);
        }
    }
    
    /**
     * 목표별 상품 가입 진행률 조회
     */
    @Transactional(readOnly = true)
    public double getGoalSubscriptionProgress(Long goalId) {
        Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
            .orElseThrow(() -> new EntityNotFoundException("목표를 찾을 수 없습니다. 목표ID: " + goalId));
        
        List<Plan1QProduct> products = plan1QProductRepository.findByPlan1QGoal(goal);
        
        if (products.isEmpty()) {
            return 0.0;
        }
        
        long subscribedCount = products.stream()
            .filter(Plan1QProduct::getSubscribed)
            .count();
        
        return (double) subscribedCount / products.size() * 100;
    }
    
    /**
     * 목표별 미가입 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Plan1QProduct> getUnsubscribedProducts(Long goalId) {
        Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
            .orElseThrow(() -> new EntityNotFoundException("목표를 찾을 수 없습니다. 목표ID: " + goalId));
        
        return plan1QProductRepository.findByPlan1QGoalAndSubscribedFalse(goal);
    }
}
