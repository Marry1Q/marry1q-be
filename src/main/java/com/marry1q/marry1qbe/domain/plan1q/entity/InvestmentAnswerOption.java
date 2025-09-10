package com.marry1q.marry1qbe.domain.plan1q.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "investment_answer_option")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentAnswerOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InvestmentQuestion question;
    
    @Column(name = "option_text", length = 255, nullable = false)
    private String optionText;
    
    @Column(name = "option_value", length = 10, nullable = false)
    private String optionValue; // A, B, C, D
    
    @Column(name = "score", nullable = false)
    private Integer score; // 1-5점
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
