package com.marry1q.marry1qbe.domain.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "bank", nullable = false, length = 100)
    private String bank;
    
    @Column(name = "account_number", nullable = false, length = 50, unique = true)
    private String accountNumber;
    
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;
    
    @Transient
    private BigDecimal balance;
    
    @Column(name = "is_couple_account")
    private Boolean isCoupleAccount;
    
    @Column(name = "account_type", length = 20)
    private String accountType;
    
    @Column(name = "user_seq_no", nullable = false, length = 20)
    private String userSeqNo;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "plan1q_product_id")
    private Long plan1qProductId;
    
    /**
     * 실시간 잔액 설정
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    /**
     * 마지막 동기화 시간 업데이트
     */
    public void updateLastSyncedAt() {
        this.lastSyncedAt = LocalDateTime.now();
    }
    
    /**
     * Plan1Q 계좌 여부 확인
     */
    public boolean isPlan1qAccount() {
        return this.plan1qProductId != null;
    }
}
