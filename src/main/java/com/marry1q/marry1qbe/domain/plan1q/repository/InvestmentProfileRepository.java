package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentProfileRepository extends JpaRepository<InvestmentProfile, Long> {
    
    /**
     * 사용자별 최신 투자성향 프로필 조회
     */
    @Query("SELECT ip FROM InvestmentProfile ip " +
           "WHERE ip.userSeqNo = :userSeqNo " +
           "ORDER BY ip.createdAt DESC")
    List<InvestmentProfile> findByUserSeqNoOrderByCreatedAtDesc(@Param("userSeqNo") String userSeqNo);
    
    /**
     * 사용자별 최신 활성 투자성향 프로필 조회
     */
    @Query("SELECT ip FROM InvestmentProfile ip " +
           "WHERE ip.userSeqNo = :userSeqNo AND ip.isExpired = false " +
           "ORDER BY ip.createdAt DESC")
    List<InvestmentProfile> findActiveByUserSeqNoOrderByCreatedAtDesc(@Param("userSeqNo") String userSeqNo);
    
    /**
     * 사용자별 최신 투자성향 프로필 조회 (단일)
     */
    Optional<InvestmentProfile> findFirstByUserSeqNoOrderByCreatedAtDesc(String userSeqNo);
    
    /**
     * 사용자별 최신 활성 투자성향 프로필 조회 (단일)
     */
    Optional<InvestmentProfile> findFirstByUserSeqNoAndIsExpiredFalseOrderByCreatedAtDesc(String userSeqNo);
    
    /**
     * 만료된 투자성향 프로필 목록 조회
     */
    @Query("SELECT ip FROM InvestmentProfile ip " +
           "WHERE ip.expiredDate < :currentDate AND ip.isExpired = false")
    List<InvestmentProfile> findExpiredProfiles(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 특정 기간 내 생성된 투자성향 프로필 개수 조회
     */
    @Query("SELECT COUNT(ip) FROM InvestmentProfile ip " +
           "WHERE ip.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
