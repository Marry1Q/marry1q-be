package com.marry1q.marry1qbe.domain.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "couple_account_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CoupleAccountTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_transaction_id")
    private Long accountTransactionId;
    
    @Column(name = "tran_id", length = 50, unique = true)
    private String tranId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Column(name = "amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal amount;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "transaction_time")
    private LocalTime transactionTime;
    
    @Column(name = "from_name", length = 100)
    private String fromName;
    
    @Column(name = "to_name", length = 100)
    private String toName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    @Builder.Default
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "finance_category_id")
    private Long financeCategoryId;
    
    @Column(name = "balance_after_transaction", precision = 18, scale = 0)
    private BigDecimal balanceAfterTransaction;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_category_id", insertable = false, updatable = false)
    private com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory financeCategory;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }
    
    public enum ReviewStatus {
        PENDING, REVIEWED
    }
    
    /**
     * 거래내역 중복 체크 (tranId 또는 거래일시+금액)
     */
    public boolean isDuplicate(CoupleAccountTransaction other) {
        // tranId가 있으면 tranId로 중복 체크
        if (this.tranId != null && other.tranId != null) {
            return this.tranId.equals(other.tranId);
        }
        
        // tranId가 없으면 거래일시+금액으로 중복 체크
        return this.transactionDate.equals(other.transactionDate) &&
               this.transactionTime.equals(other.transactionTime) &&
               this.amount.compareTo(other.amount) == 0;
    }
}
