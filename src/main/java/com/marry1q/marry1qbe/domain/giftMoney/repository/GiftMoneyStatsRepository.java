package com.marry1q.marry1qbe.domain.giftMoney.repository;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiftMoneyStatsRepository extends JpaRepository<GiftMoneyStats, Long> {
    
    /**
     * 커플 ID로 통계 조회
     */
    Optional<GiftMoneyStats> findByCoupleId(Long coupleId);
    
    /**
     * 커플 ID로 통계 존재 여부 확인
     */
    boolean existsByCoupleId(Long coupleId);
    
    /**
     * 커플 ID로 통계 삭제
     */
    void deleteByCoupleId(Long coupleId);
}
