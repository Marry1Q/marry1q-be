package com.marry1q.marry1qbe.domain.plan1q.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investment_question")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "question_type", length = 20, nullable = false)
    private String questionType; // CommonCode 값: 'multiple_choice', 'scale'
    
    @Column(name = "category", length = 50, nullable = false)
    private String category; // CommonCode 값: 'risk_tolerance', 'investment_period', 'investment_amount', 'financial_goal'
    
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
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvestmentAnswerOption> answerOptions = new ArrayList<>();
    
    // CommonCode를 통한 코드명 조회 메서드들
    public String getQuestionTypeName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("INVESTMENT_QUESTION_TYPE", this.questionType);
    }
    
    public String getCategoryName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("INVESTMENT_QUESTION_CATEGORY", this.category);
    }
}
