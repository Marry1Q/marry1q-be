package com.marry1q.marry1qbe.domain.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_budget")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBudget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_budget")
    private Long categoryBudgetId;
    
    @Column(name = "budget_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal budgetAmount;
    
    @Column(name = "spent_amount", precision = 12, scale = 0)
    private BigDecimal spentAmount;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_category_id", nullable = false)
    private FinanceCategory financeCategory;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 예산 대비 사용률 계산
    public double getUsageRate() {
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    // 남은 예산 계산
    public BigDecimal getRemainingAmount() {
        return budgetAmount.subtract(spentAmount);
    }
    
    // 지출 금액 증가
    public void increaseSpentAmount(BigDecimal amount) {
        this.spentAmount = this.spentAmount.add(amount);
    }
    
    // 지출 금액 감소
    public void decreaseSpentAmount(BigDecimal amount) {
        this.spentAmount = this.spentAmount.subtract(amount);
    }
    
    // 예산 수정
    public void updateBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
