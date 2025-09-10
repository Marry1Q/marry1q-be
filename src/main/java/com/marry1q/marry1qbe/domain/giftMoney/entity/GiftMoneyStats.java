package com.marry1q.marry1qbe.domain.giftMoney.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "gift_money_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftMoneyStats {
    
    @Id
    @Column(name = "gift_money_stats_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long giftMoneyStatsId;
    
    @Column(name = "couple_id")
    private Long coupleId;
    
    @Column(name = "total_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "total_count")
    @Builder.Default
    private Integer totalCount = 0;
    
    @Column(name = "thanks_not_sent_count")
    @Builder.Default
    private Integer thanksNotSentCount = 0;
    
    @Column(name = "family_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal familyAmount = BigDecimal.ZERO;
    
    @Column(name = "family_count")
    @Builder.Default
    private Integer familyCount = 0;
    
    @Column(name = "relative_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal relativeAmount = BigDecimal.ZERO;
    
    @Column(name = "relative_count")
    @Builder.Default
    private Integer relativeCount = 0;
    
    @Column(name = "friend_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal friendAmount = BigDecimal.ZERO;
    
    @Column(name = "friend_count")
    @Builder.Default
    private Integer friendCount = 0;
    
    @Column(name = "colleague_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal colleagueAmount = BigDecimal.ZERO;
    
    @Column(name = "colleague_count")
    @Builder.Default
    private Integer colleagueCount = 0;
    
    @Column(name = "acquaintance_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal acquaintanceAmount = BigDecimal.ZERO;
    
    @Column(name = "acquaintance_count")
    @Builder.Default
    private Integer acquaintanceCount = 0;
    
    @Column(name = "other_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal otherAmount = BigDecimal.ZERO;
    
    @Column(name = "other_count")
    @Builder.Default
    private Integer otherCount = 0;
    
    @Column(name = "amount_under_30k_count")
    @Builder.Default
    private Integer amountUnder30kCount = 0;
    
    @Column(name = "amount_30k_to_50k_count")
    @Builder.Default
    private Integer amount30kTo50kCount = 0;
    
    @Column(name = "amount_50k_to_100k_count")
    @Builder.Default
    private Integer amount50kTo100kCount = 0;
    
    @Column(name = "amount_100k_to_200k_count")
    @Builder.Default
    private Integer amount100kTo200kCount = 0;
    
    @Column(name = "amount_200k_to_500k_count")
    @Builder.Default
    private Integer amount200kTo500kCount = 0;
    
    @Column(name = "amount_over_500k_count")
    @Builder.Default
    private Integer amountOver500kCount = 0;
    
    @Column(name = "top_donor_name", length = 100)
    private String topDonorName;
    
    @Column(name = "top_donor_amount", precision = 10, scale = 0)
    @Builder.Default
    private BigDecimal topDonorAmount = BigDecimal.ZERO;
    
    @Column(name = "top_donor_gift_money_id")
    private Long topDonorGiftMoneyId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 추가 통계 필드들
    @Column(name = "first_gift_date")
    private LocalDate firstGiftDate;
    
    @Column(name = "last_gift_date")
    private LocalDate lastGiftDate;
    
    @Column(name = "gift_days_count")
    private Integer giftDaysCount;
    
    @Column(name = "daily_average_amount", precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal dailyAverageAmount = BigDecimal.ZERO;
    
    // 증분 업데이트를 위한 메서드들
    public void addAmount(BigDecimal amount) {
        this.totalAmount = this.totalAmount.add(amount);
    }
    
    public void subtractAmount(BigDecimal amount) {
        this.totalAmount = this.totalAmount.subtract(amount);
    }
    
    public void incrementCount() {
        this.totalCount++;
    }
    
    public void decrementCount() {
        this.totalCount--;
    }
    
    public void incrementThanksNotSentCount() {
        this.thanksNotSentCount++;
    }
    
    public void decrementThanksNotSentCount() {
        this.thanksNotSentCount--;
    }
    
    // 날짜 통계 관련 메서드들
    public void updateDateStats(LocalDate giftDate) {
        if (this.firstGiftDate == null || giftDate.isBefore(this.firstGiftDate)) {
            this.firstGiftDate = giftDate;
        }
        if (this.lastGiftDate == null || giftDate.isAfter(this.lastGiftDate)) {
            this.lastGiftDate = giftDate;
        }
        calculateGiftDays();
    }
    
    public void calculateGiftDays() {
        if (this.firstGiftDate != null && this.lastGiftDate != null) {
            this.giftDaysCount = (int) ChronoUnit.DAYS.between(this.firstGiftDate, this.lastGiftDate) + 1;
        }
    }
    
    public void calculateDailyAverage() {
        if (this.giftDaysCount != null && this.giftDaysCount > 0) {
            this.dailyAverageAmount = this.totalAmount.divide(BigDecimal.valueOf(this.giftDaysCount), 0, RoundingMode.HALF_UP);
        }
    }
}
