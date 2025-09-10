package com.marry1q.marry1qbe.domain.giftMoney.entity;

import com.marry1q.marry1qbe.domain.giftMoney.enums.Relationship;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Source;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_money")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftMoney {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_money_id")
    private Long giftMoneyId;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "amount", precision = 10, scale = 0, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false)
    private Relationship relationship;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Source source;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @Column(name = "gift_date", nullable = false)
    private LocalDate giftDate;
    
    @Column(name = "thanks_sent")
    private Boolean thanksSent;
    
    @Column(name = "thanks_date")
    private LocalDate thanksDate;
    
    @Column(name = "thanks_sent_by", length = 100)
    private String thanksSentBy;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (thanksSent == null) {
            thanksSent = false;
        }
    }
}
