package com.marry1q.marry1qbe.domain.giftMoney.repository;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiftMoneyRepository extends JpaRepository<GiftMoney, Long>, GiftMoneyRepositoryCustom {
    
    // 커플별 축의금 목록 조회
    List<GiftMoney> findByCoupleIdOrderByGiftDateDesc(Long coupleId);
    
    // 커플별 축의금 개수 조회
    long countByCoupleId(Long coupleId);
    
    // 커플별 총 축의금 금액 조회
    @Query("SELECT COALESCE(SUM(g.amount), 0) FROM GiftMoney g WHERE g.coupleId = :coupleId")
    BigDecimal sumAmountByCoupleId(@Param("coupleId") Long coupleId);
    
    // 커플별 감사 연락 미완료 개수 조회
    long countByCoupleIdAndThanksSentFalse(Long coupleId);
    
    // 커플별 관계별 축의금 금액 조회
    @Query("SELECT g.relationship, COALESCE(SUM(g.amount), 0) FROM GiftMoney g WHERE g.coupleId = :coupleId GROUP BY g.relationship")
    List<Object[]> sumAmountByRelationshipAndCoupleId(@Param("coupleId") Long coupleId);
    
    // 커플별 관계별 축의금 개수 조회
    @Query("SELECT g.relationship, COUNT(g) FROM GiftMoney g WHERE g.coupleId = :coupleId GROUP BY g.relationship")
    List<Object[]> countByRelationshipAndCoupleId(@Param("coupleId") Long coupleId);
    
    // 커플별 최고 후원자 조회
    @Query("SELECT g FROM GiftMoney g WHERE g.coupleId = :coupleId ORDER BY g.amount DESC")
    List<GiftMoney> findTopDonorByCoupleId(@Param("coupleId") Long coupleId);
    
    // 커플별 금액대별 개수 조회
    @Query("SELECT " +
           "SUM(CASE WHEN g.amount < 30000 THEN 1 ELSE 0 END), " +                      // 30만원 미만: < 30,000
           "SUM(CASE WHEN g.amount >= 30000 AND g.amount < 50000 THEN 1 ELSE 0 END), " +  // 30-50만원: 30,000 ≤ amount < 50,000
           "SUM(CASE WHEN g.amount >= 50000 AND g.amount < 100000 THEN 1 ELSE 0 END), " + // 50-100만원: 50,000 ≤ amount < 100,000
           "SUM(CASE WHEN g.amount >= 100000 AND g.amount < 200000 THEN 1 ELSE 0 END), " + // 100-200만원: 100,000 ≤ amount < 200,000
           "SUM(CASE WHEN g.amount >= 200000 AND g.amount <= 500000 THEN 1 ELSE 0 END), " + // 200-500만원: 200,000 ≤ amount ≤ 500,000
           "SUM(CASE WHEN g.amount > 500000 THEN 1 ELSE 0 END) " +                      // 500만원 이상: > 500,000
           "FROM GiftMoney g WHERE g.coupleId = :coupleId")
    Object[] countByAmountRangeAndCoupleId(@Param("coupleId") Long coupleId);
}
