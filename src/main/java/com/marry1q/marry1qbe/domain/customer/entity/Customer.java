package com.marry1q.marry1qbe.domain.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "marry1q_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @Column(name = "user_seq_no", length = 20)
    private String userSeqNo;
    
    @Column(name = "user_ci", length = 88, nullable = false)
    private String userCi;
    
    @Column(name = "customer_name", length = 100, nullable = false)
    private String customerName;
    
    @Column(name = "customer_phone", length = 20, nullable = false)
    private String customerPhone;
    
    @Column(name = "customer_info", length = 8, nullable = false)
    private String customerInfo;
    
    @Column(name = "customer_pin", length = 100, nullable = false)
    private String customerPin;
    
    @Column(name = "customer_email", length = 100, nullable = false, unique = true)
    private String customerEmail;
    
    @Column(name = "customer_num", length = 20)
    private String customerNum;
    
    @Column(name = "couple_id")
    private Long coupleId;
    
    @Column(name = "customer_pw", length = 255, nullable = false)
    private String customerPw;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
