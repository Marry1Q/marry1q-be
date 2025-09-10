package com.marry1q.marry1qbe.grobal.openBankingToken.repository;

import com.marry1q.marry1qbe.grobal.openBankingToken.entity.OpenBankingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OpenBankingTokenRepository extends JpaRepository<OpenBankingToken, Long> {
    
    /**
     * 가장 최근의 유효한 토큰 조회
     */
    @Query("SELECT t FROM OpenBankingToken t WHERE t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<OpenBankingToken> findLatestValidToken(@Param("now") LocalDateTime now);
    
    /**
     * 만료되지 않은 토큰이 있는지 확인
     */
    @Query("SELECT COUNT(t) > 0 FROM OpenBankingToken t WHERE t.expiresAt > :now")
    boolean existsValidToken(@Param("now") LocalDateTime now);
    
    /**
     * 모든 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM OpenBankingToken t WHERE t.expiresAt <= :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * 가장 최근 토큰 조회 (만료 여부 상관없이)
     */
    @Query("SELECT t FROM OpenBankingToken t ORDER BY t.createdAt DESC")
    Optional<OpenBankingToken> findLatestToken();
}
