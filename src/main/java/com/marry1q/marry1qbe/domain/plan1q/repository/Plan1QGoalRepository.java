package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface Plan1QGoalRepository extends JpaRepository<Plan1QGoal, Long> {
    
    /**
     * 커플 ID로 목표 목록 조회 (생성일시 내림차순)
     */
    List<Plan1QGoal> findByCoupleIdOrderByCreatedAtDesc(Long coupleId);
    
    /**
     * 목표 ID와 커플 ID로 특정 목표 조회
     */
    Optional<Plan1QGoal> findByPlan1qGoalIdAndCoupleId(Long goalId, Long coupleId);
    
    /**
     * 사용자 ID로 목표 목록 조회
     */
    List<Plan1QGoal> findByUserSeqNoOrderByCreatedAtDesc(String userSeqNo);
    
    /**
     * 커플 ID와 상태로 목표 목록 조회
     */
    List<Plan1QGoal> findByCoupleIdAndStatusOrderByCreatedAtDesc(Long coupleId, String status);

    /**
     * 만기일 이전이고 특정 상태인 목표 목록 조회 (만기 처리용)
     */
    List<Plan1QGoal> findByMaturityDateBeforeAndStatus(LocalDate date, String status);
}
