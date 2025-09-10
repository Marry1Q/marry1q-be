package com.marry1q.marry1qbe.domain.plan1q.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_profile")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_profile_id")
    private Long investmentProfileId;
    
    @Column(name = "user_seq_no", length = 20, nullable = false)
    private String userSeqNo;
    
    @Column(name = "profile_type", length = 20, nullable = false)
    private String profileType; // CommonCode 값: 'conservative', 'neutral', 'aggressive'
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;
    
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // CommonCode를 통한 코드명 조회 메서드
    public String getProfileTypeName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("INVESTMENT_PROFILE_TYPE", this.profileType);
    }
    
    // 만료 여부 확인 메서드
    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expiredDate);
    }
    
    // 만료일 업데이트 메서드
    public void updateExpiredStatus() {
        this.isExpired = isExpired();
    }
}
