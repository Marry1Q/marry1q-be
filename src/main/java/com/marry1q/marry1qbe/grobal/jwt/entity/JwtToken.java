package com.marry1q.marry1qbe.grobal.jwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "marry1q_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
    
    @Column(name = "user_seq_no", length = 20, nullable = false)
    private String userSeqNo;
    
    @Column(name = "access_token", length = 1000, nullable = false)
    private String accessToken;
    
    @Column(name = "refresh_token", length = 1000, nullable = false)
    private String refreshToken;
    
    @Column(name = "token_type", length = 20, nullable = false)
    private String tokenType;
    
    @Column(name = "access_token_expired_at", nullable = false)
    private LocalDateTime accessTokenExpiredAt;
    
    @Column(name = "refresh_token_expired_at", nullable = false)
    private LocalDateTime refreshTokenExpiredAt;
    
    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (tokenType == null) {
            tokenType = "JWT";
        }
        if (isBlacklisted == null) {
            isBlacklisted = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
