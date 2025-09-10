package com.marry1q.marry1qbe.domain.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "finance_transaction_id")
    private Long financeTransactionId;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "transaction_time")
    private LocalTime transactionTime;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @Column(name = "user_seq_no", nullable = false, length = 20)
    private String userSeqNo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_category_id", nullable = false)
    private FinanceCategory financeCategory;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TransactionType {
        INCOME, EXPENSE
    }
    
    // 거래 내역 수정 메서드
    public void update(String description, String memo, LocalDate transactionDate, 
                      LocalTime transactionTime, BigDecimal amount, TransactionType transactionType, 
                      FinanceCategory financeCategory) {
        this.description = description;
        this.memo = memo;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.amount = amount;
        this.transactionType = transactionType;
        this.financeCategory = financeCategory;
    }
}
