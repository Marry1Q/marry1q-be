package com.marry1q.marry1qbe.grobal.util;

import com.marry1q.marry1qbe.grobal.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 현재 로그인한 사용자의 userSeqNo를 반환합니다.
     */
    public String getCurrentUserSeqNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증되지 않은 사용자입니다.");
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        String userSeqNo = authentication.getName();
        log.debug("현재 사용자 userSeqNo: {}", userSeqNo);
        
        return userSeqNo;
    }
    
    /**
     * JWT 토큰에서 userSeqNo를 추출합니다.
     */
    public String getUserSeqNoFromToken(String token) {
        try {
            String userSeqNo = jwtTokenProvider.getUserSeqNoFromToken(token);
            log.debug("JWT 토큰에서 추출한 userSeqNo: {}", userSeqNo);
            return userSeqNo;
        } catch (Exception e) {
            log.error("JWT 토큰에서 userSeqNo 추출 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("JWT 토큰에서 사용자 정보를 추출할 수 없습니다.", e);
        }
    }
}
