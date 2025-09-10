package com.marry1q.marry1qbe.grobal.jwt;

import com.marry1q.marry1qbe.grobal.jwt.entity.JwtToken;
import com.marry1q.marry1qbe.grobal.jwt.repository.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    
    private final JwtTokenRepository jwtTokenRepository;
    
    @Transactional
    public void blacklistToken(String userSeqNo) {
        Optional<JwtToken> existingToken = jwtTokenRepository.findByUserSeqNoAndIsBlacklistedFalse(userSeqNo);
        
        if (existingToken.isPresent()) {
            JwtToken token = existingToken.get();
            token.setIsBlacklisted(true);
            token.setUpdatedAt(LocalDateTime.now());
            jwtTokenRepository.save(token);
        }
    }
    
    @Transactional
    public void saveToken(String userSeqNo, String accessToken, String refreshToken, 
                         LocalDateTime accessTokenExpiredAt, LocalDateTime refreshTokenExpiredAt) {
        // 기존 토큰을 블랙리스트로 이동
        blacklistToken(userSeqNo);
        
        // 새 토큰 저장
        JwtToken newToken = JwtToken.builder()
                .userSeqNo(userSeqNo)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("JWT")
                .accessTokenExpiredAt(accessTokenExpiredAt)
                .refreshTokenExpiredAt(refreshTokenExpiredAt)
                .isBlacklisted(false)
                .build();
        
        jwtTokenRepository.save(newToken);
    }
    
    public boolean isTokenBlacklisted(String accessToken) {
        return jwtTokenRepository.existsByAccessTokenAndIsBlacklistedFalse(accessToken);
    }
    
    public Optional<JwtToken> getValidRefreshToken(String refreshToken) {
        return jwtTokenRepository.findByRefreshTokenAndIsBlacklistedFalse(refreshToken);
    }
}
