package com.marry1q.marry1qbe.domain.giftMoney.dto.response;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축의금 통계 응답")
public class GiftMoneyStatisticsResponse {
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "총 축의금", example = "1500000")
    private BigDecimal totalAmount;
    
    @Schema(description = "총 인원", example = "25")
    private Integer totalCount;
    
    @Schema(description = "감사 연락 미완료 인원", example = "5")
    private Integer thanksNotSentCount;
    
    // 관계별 통계
    @Schema(description = "가족 축의금", example = "500000")
    private BigDecimal familyAmount;
    
    @Schema(description = "가족 인원", example = "8")
    private Integer familyCount;
    
    @Schema(description = "친척 축의금", example = "300000")
    private BigDecimal relativeAmount;
    
    @Schema(description = "친척 인원", example = "5")
    private Integer relativeCount;
    
    @Schema(description = "친구 축의금", example = "400000")
    private BigDecimal friendAmount;
    
    @Schema(description = "친구 인원", example = "7")
    private Integer friendCount;
    
    @Schema(description = "회사동료 축의금", example = "200000")
    private BigDecimal colleagueAmount;
    
    @Schema(description = "회사동료 인원", example = "3")
    private Integer colleagueCount;
    
    @Schema(description = "지인 축의금", example = "80000")
    private BigDecimal acquaintanceAmount;
    
    @Schema(description = "지인 인원", example = "1")
    private Integer acquaintanceCount;
    
    @Schema(description = "기타 축의금", example = "20000")
    private BigDecimal otherAmount;
    
    @Schema(description = "기타 인원", example = "1")
    private Integer otherCount;
    
    // 금액대별 통계
    @Schema(description = "3만원 미만 인원", example = "5")
    private Integer amountUnder30kCount;
    
    @Schema(description = "3-5만원 미만 인원", example = "8")
    private Integer amount30kTo50kCount;
    
    @Schema(description = "5-10만원 미만 인원", example = "7")
    private Integer amount50kTo100kCount;
    
    @Schema(description = "10-20만원 미만 인원", example = "3")
    private Integer amount100kTo200kCount;
    
    @Schema(description = "20-50만원 미만 인원", example = "1")
    private Integer amount200kTo500kCount;
    
    @Schema(description = "50만원 이상 인원", example = "1")
    private Integer amountOver500kCount;
    
    // 최고 후원자 정보
    @Schema(description = "최고 후원자 이름", example = "김철수")
    private String topDonorName;
    
    @Schema(description = "최고 후원자 금액", example = "100000")
    private BigDecimal topDonorAmount;
    
    @Schema(description = "최고 후원자 축의금 ID", example = "15")
    private Long topDonorGiftMoneyId;
    
    // 추가 통계 정보
    @Schema(description = "평균 축의금", example = "60000")
    private BigDecimal averageAmount;
    
    @Schema(description = "첫 번째 축의 날짜", example = "2024-01-01")
    private LocalDate firstGiftDate;
    
    @Schema(description = "마지막 축의 날짜", example = "2024-01-15")
    private LocalDate lastGiftDate;
    
    @Schema(description = "축의 받은 일수", example = "15")
    private Integer giftDaysCount;
    
    @Schema(description = "일평균 축의금", example = "100000")
    private BigDecimal dailyAverageAmount;
    
    // 관계별 통계 맵 (API 응답용)
    @Schema(description = "관계별 통계 맵")
    private Map<String, RelationshipStatistics> relationshipStatistics;
    
    // 금액대별 통계 맵 (API 응답용)
    @Schema(description = "금액대별 통계 맵")
    private Map<String, AmountRangeStatistics> amountRangeStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "관계별 통계")
    public static class RelationshipStatistics {
        @Schema(description = "관계명", example = "가족")
        private String relationshipName;
        
        @Schema(description = "축의금", example = "500000")
        private BigDecimal amount;
        
        @Schema(description = "인원", example = "8")
        private Integer count;
        
        @Schema(description = "비율", example = "33.3")
        private Double percentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "금액대별 통계")
    public static class AmountRangeStatistics {
        @Schema(description = "금액대명", example = "3만원 미만")
        private String rangeName;
        
        @Schema(description = "인원", example = "5")
        private Integer count;
        
        @Schema(description = "비율", example = "20.0")
        private Double percentage;
    }
    
    public static GiftMoneyStatisticsResponse from(GiftMoneyStats stats) {
        return GiftMoneyStatisticsResponse.builder()
                .coupleId(stats.getCoupleId())
                .totalAmount(stats.getTotalAmount())
                .totalCount(stats.getTotalCount())
                .thanksNotSentCount(stats.getThanksNotSentCount())
                .familyAmount(stats.getFamilyAmount())
                .familyCount(stats.getFamilyCount())
                .relativeAmount(stats.getRelativeAmount())
                .relativeCount(stats.getRelativeCount())
                .friendAmount(stats.getFriendAmount())
                .friendCount(stats.getFriendCount())
                .colleagueAmount(stats.getColleagueAmount())
                .colleagueCount(stats.getColleagueCount())
                .acquaintanceAmount(stats.getAcquaintanceAmount())
                .acquaintanceCount(stats.getAcquaintanceCount())
                .otherAmount(stats.getOtherAmount())
                .otherCount(stats.getOtherCount())
                .amountUnder30kCount(stats.getAmountUnder30kCount())
                .amount30kTo50kCount(stats.getAmount30kTo50kCount())
                .amount50kTo100kCount(stats.getAmount50kTo100kCount())
                .amount100kTo200kCount(stats.getAmount100kTo200kCount())
                .amount200kTo500kCount(stats.getAmount200kTo500kCount())
                .amountOver500kCount(stats.getAmountOver500kCount())
                .topDonorName(stats.getTopDonorName())
                .topDonorAmount(stats.getTopDonorAmount())
                .topDonorGiftMoneyId(stats.getTopDonorGiftMoneyId())
                .averageAmount(calculateAverage(stats))
                .firstGiftDate(stats.getFirstGiftDate())
                .lastGiftDate(stats.getLastGiftDate())
                .giftDaysCount(stats.getGiftDaysCount())
                .dailyAverageAmount(stats.getDailyAverageAmount())
                .build();
    }
    
    private static BigDecimal calculateAverage(GiftMoneyStats stats) {
        if (stats.getTotalCount() == null || stats.getTotalCount() == 0) {
            return BigDecimal.ZERO;
        }
        return stats.getTotalAmount().divide(BigDecimal.valueOf(stats.getTotalCount()), 0, RoundingMode.HALF_UP);
    }
    
    /**
     * 빈 통계 객체 생성 (통계 데이터가 없을 때 사용)
     */
    public static GiftMoneyStatisticsResponse createEmpty(Long coupleId) {
        return GiftMoneyStatisticsResponse.builder()
                .coupleId(coupleId)
                .totalAmount(BigDecimal.ZERO)
                .totalCount(0)
                .thanksNotSentCount(0)
                .familyAmount(BigDecimal.ZERO)
                .familyCount(0)
                .relativeAmount(BigDecimal.ZERO)
                .relativeCount(0)
                .friendAmount(BigDecimal.ZERO)
                .friendCount(0)
                .colleagueAmount(BigDecimal.ZERO)
                .colleagueCount(0)
                .acquaintanceAmount(BigDecimal.ZERO)
                .acquaintanceCount(0)
                .otherAmount(BigDecimal.ZERO)
                .otherCount(0)
                .amountUnder30kCount(0)
                .amount30kTo50kCount(0)
                .amount50kTo100kCount(0)
                .amount100kTo200kCount(0)
                .amount200kTo500kCount(0)
                .amountOver500kCount(0)
                .topDonorName(null)
                .topDonorAmount(BigDecimal.ZERO)
                .topDonorGiftMoneyId(null)
                .averageAmount(BigDecimal.ZERO)
                .firstGiftDate(null)
                .lastGiftDate(null)
                .giftDaysCount(0)
                .dailyAverageAmount(BigDecimal.ZERO)
                .build();
    }
}
