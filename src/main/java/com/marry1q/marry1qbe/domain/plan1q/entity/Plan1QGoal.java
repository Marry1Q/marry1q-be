package com.marry1q.marry1qbe.domain.plan1q.entity;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan1q_goal")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan1q_goal_id")
    private Long plan1qGoalId;
    
    @Column(name = "goal_name", length = 100, nullable = false)
    private String goalName;
    
    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;
    
    @Column(name = "target_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal targetAmount;
    
    @Column(name = "current_amount", precision = 12, scale = 0)
    private BigDecimal currentAmount;
    
    @Column(name = "total_expected_return", precision = 5, scale = 2)
    private BigDecimal totalExpectedReturn;
    
    @Column(name = "actual_return_rate", precision = 5, scale = 2)
    private BigDecimal actualReturnRate;        // 실제 계산된 전체 수익률
    
    @Column(name = "target_period", nullable = false)
    private Integer targetPeriod;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "monthly_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal monthlyAmount;
    
    @Column(name = "status", length = 30, nullable = false)
    private String status; // CommonCode 값: 'in_progress', 'subscription_in_progress', 'completed', 'cancelled'
    
    @Column(name = "subscription_progress", precision = 5, scale = 2)
    private BigDecimal subscriptionProgress;
    
    @Column(name = "risk_level", length = 10, nullable = false)
    private String riskLevel; // CommonCode 값: 'low', 'medium', 'high'
    
    @Column(name = "icon", length = 50)
    private String icon;
    
    @Column(name = "color", length = 50)
    private String color;
    
    @Column(name = "user_seq_no", length = 20, nullable = false)
    private String userSeqNo;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @Column(name = "investment_profile_id")
    private Long investmentProfileId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "plan1QGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Plan1QProduct> products = new ArrayList<>();
    
    // CommonCode를 통한 코드명 조회 메서드들
    public String getStatusName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("PLAN1Q_GOAL_STATUS", this.status);
    }
    
    public String getRiskLevelName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("RISK_LEVEL", this.riskLevel);
    }
    
    // 상품 목록 설정 메서드
    public void setProducts(List<Plan1QProduct> products) {
        this.products = products;
    }
    
    // 목표 상태 설정 메서드
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 목표 정보 업데이트 메서드
     */
    public void updateGoal(String goalName, String goalDescription, BigDecimal targetAmount, 
                          Integer targetPeriod, BigDecimal monthlyAmount, LocalDate maturityDate) {
        this.goalName = goalName;
        this.goalDescription = goalDescription;
        this.targetAmount = targetAmount;
        this.targetPeriod = targetPeriod;
        this.monthlyAmount = monthlyAmount;
        this.maturityDate = maturityDate;
    }
    
    /**
     * 현재 금액 업데이트 메서드 (실시간 계좌 정보 기반)
     */
    public void updateCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    /**
     * 실제 수익률 업데이트 메서드
     */
    public void updateActualReturnRate(BigDecimal actualReturnRate) {
        this.actualReturnRate = actualReturnRate;
    }
}
