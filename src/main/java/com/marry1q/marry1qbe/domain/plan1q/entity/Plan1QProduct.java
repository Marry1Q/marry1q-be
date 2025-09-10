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

@Entity
@Table(name = "plan1q_product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan1q_product_id")
    private Long plan1qProductId;
    
    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;
    
    @Column(name = "product_type", length = 20, nullable = false)
    private String productType; // CommonCode 값: 'fund', 'savings'
    
    @Column(name = "investment_ratio", precision = 5, scale = 2, nullable = false)
    private BigDecimal investmentRatio;
    
    @Column(name = "investment_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal investmentAmount;
    
    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;
    
    @Column(name = "monthly_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal monthlyAmount;
    
    @Column(name = "subscribed", nullable = false)
    private Boolean subscribed;
    

    

    
    @Column(name = "contract_date")
    private LocalDate contractDate;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;
    
    @Column(name = "contract", length = 255)
    private String contract;
    
    @Column(name = "account_number", length = 50)
    private String accountNumber;
    
    @Column(name = "source_account_number", length = 50)
    private String sourceAccountNumber;
    
    @Column(name = "risk_level", length = 50)
    private String riskLevel;
    
    @Column(name = "risk_type", length = 50)
    private String riskType;
    
    @Column(name = "asset_class", length = 50)
    private String assetClass;
    
    @Column(name = "strategy", columnDefinition = "TEXT")
    private String strategy;
    

    
    @Column(name = "period", length = 50)
    private String period;
    
    @Column(name = "hana_bank_product_id")
    private Long hanaBankProductId;
    
    @Column(name = "hana_bank_subscription_id", length = 100)
    private String hanaBankSubscriptionId;
    
    @Column(name = "auto_transfer_id")
    private Long autoTransferId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan1q_goal_id", nullable = false)
    private Plan1QGoal plan1QGoal;
    
    // CommonCode를 통한 코드명 조회 메서드
    public String getProductTypeName(com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return commonCodeService.getCodeName("PLAN1Q_PRODUCT_TYPE", this.productType);
    }
    
    // Plan1Q 목표 ID 조회 메서드
    public Long getPlan1qGoalId() {
        return this.plan1QGoal != null ? this.plan1QGoal.getPlan1qGoalId() : null;
    }
    
    // 상품 가입 상태 설정 메서드
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }
    
    // 계좌번호 설정 메서드
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    // 하나은행 가입 ID 설정 메서드
    public void setHanaBankSubscriptionId(String hanaBankSubscriptionId) {
        this.hanaBankSubscriptionId = hanaBankSubscriptionId;
    }
    
    // 계약일 설정 메서드
    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }
    
    // 상품 가입 정보 업데이트 메서드
    public void updateSubscription(Boolean subscribed, String hanaBankSubscriptionId, 
                                 String accountNumber, LocalDate contractDate) {
        this.subscribed = subscribed;
        this.hanaBankSubscriptionId = hanaBankSubscriptionId;
        this.accountNumber = accountNumber;
        this.contractDate = contractDate;
    }
    
    // 상품 가입 정보 업데이트 메서드 (sourceAccountNumber 포함)
    public void updateSubscription(Boolean subscribed, String hanaBankSubscriptionId, 
                                 String accountNumber, String sourceAccountNumber, LocalDate contractDate) {
        this.subscribed = subscribed;
        this.hanaBankSubscriptionId = hanaBankSubscriptionId;
        this.accountNumber = accountNumber;
        this.sourceAccountNumber = sourceAccountNumber;
        this.contractDate = contractDate;
    }
    
    // 상품 가입 정보 업데이트 메서드 (자동이체 ID 포함)
    public void updateSubscriptionWithAutoTransfer(Boolean subscribed, String hanaBankSubscriptionId, 
                                                 String accountNumber, String sourceAccountNumber, 
                                                 LocalDate contractDate, Long autoTransferId) {
        this.subscribed = subscribed;
        this.hanaBankSubscriptionId = hanaBankSubscriptionId;
        this.accountNumber = accountNumber;
        this.sourceAccountNumber = sourceAccountNumber;
        this.contractDate = contractDate;
        this.autoTransferId = autoTransferId;
    }
}
