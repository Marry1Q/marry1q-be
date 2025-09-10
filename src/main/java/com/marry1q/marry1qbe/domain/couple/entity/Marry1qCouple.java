package com.marry1q.marry1qbe.domain.couple.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "marry1q_couple")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marry1qCouple {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private Long coupleId;
    
    @Column(name = "wedding_date")
    private LocalDate weddingDate;
    
    @Column(name = "total_budget", precision = 12, scale = 0)
    private BigDecimal totalBudget;
    
    @Column(name = "couple_account", length = 20, nullable = false)
    private String coupleAccount;
    
    @Column(name = "couple_card_number", length = 19)
    private String coupleCardNumber;
    
    @Column(name = "current_spent", precision = 12, scale = 0)
    private BigDecimal currentSpent;
    
    @Column(name = "url_slug", length = 100, nullable = false)
    private String urlSlug;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 커플 정보 업데이트 메서드
    public void update(LocalDate weddingDate, BigDecimal totalBudget) {
        this.weddingDate = weddingDate;
        this.totalBudget = totalBudget;
    }
    
    // 현재 지출 금액 업데이트 메서드
    public void updateCurrentSpent(BigDecimal currentSpent) {
        this.currentSpent = currentSpent;
    }
}
