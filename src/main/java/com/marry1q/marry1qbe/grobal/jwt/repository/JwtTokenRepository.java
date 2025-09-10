package com.marry1q.marry1qbe.grobal.jwt.repository;

import com.marry1q.marry1qbe.grobal.jwt.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    
    Optional<JwtToken> findByUserSeqNoAndIsBlacklistedFalse(String userSeqNo);
    
    Optional<JwtToken> findByRefreshTokenAndIsBlacklistedFalse(String refreshToken);
    
    List<JwtToken> findByUserSeqNo(String userSeqNo);
    
    boolean existsByAccessTokenAndIsBlacklistedFalse(String accessToken);
    
    boolean existsByRefreshTokenAndIsBlacklistedFalse(String refreshToken);
}
