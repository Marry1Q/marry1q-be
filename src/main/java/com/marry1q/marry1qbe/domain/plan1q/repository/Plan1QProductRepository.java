package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Plan1QProductRepository extends JpaRepository<Plan1QProduct, Long> {
    
    /**
     * 목표별 상품 목록 조회
     */
    List<Plan1QProduct> findByPlan1QGoal(Plan1QGoal plan1QGoal);
    
    /**
     * 목표별 미가입 상품 목록 조회
     */
    List<Plan1QProduct> findByPlan1QGoalAndSubscribedFalse(Plan1QGoal plan1QGoal);
    
    /**
     * 목표별 가입 완료 상품 목록 조회
     */
    List<Plan1QProduct> findByPlan1QGoalAndSubscribedTrue(Plan1QGoal plan1QGoal);
    
    /**
     * 목표별 상품 가입 완료 개수 조회
     */
    long countByPlan1QGoalAndSubscribedTrue(Plan1QGoal plan1QGoal);
    
    /**
     * 목표별 전체 상품 개수 조회
     */
    long countByPlan1QGoal(Plan1QGoal plan1QGoal);
}
