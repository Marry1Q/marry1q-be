package com.marry1q.marry1qbe.grobal.openBankingToken.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "openbanking_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenBankingToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
    
    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;
    
    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType;
    
    @Column(name = "expires_in", nullable = false)
    private Long expiresIn;
    
    @Column(name = "scope", nullable = false, length = 50)
    private String scope;
    
    @Column(name = "client_use_code", nullable = false, length = 50)
    private String clientUseCode;
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
