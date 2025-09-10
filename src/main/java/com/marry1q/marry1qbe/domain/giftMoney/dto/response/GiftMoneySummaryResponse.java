package com.marry1q.marry1qbe.domain.giftMoney.dto.response;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축의금 요약 통계 응답")
public class GiftMoneySummaryResponse {
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "총 축의금", example = "1500000")
    private BigDecimal totalAmount;
    
    @Schema(description = "총 인원", example = "25")
    private Integer totalCount;
    
    @Schema(description = "감사 연락 미완료 인원", example = "5")
    private Integer thanksNotSentCount;
    
    @Schema(description = "평균 축의금", example = "60000")
    private BigDecimal averageAmount;
    
    public static GiftMoneySummaryResponse from(GiftMoneyStats stats) {
        return GiftMoneySummaryResponse.builder()
                .coupleId(stats.getCoupleId())
                .totalAmount(stats.getTotalAmount())
                .totalCount(stats.getTotalCount())
                .thanksNotSentCount(stats.getThanksNotSentCount())
                .averageAmount(calculateAverage(stats))
                .build();
    }
    
    private static BigDecimal calculateAverage(GiftMoneyStats stats) {
        if (stats.getTotalCount() == null || stats.getTotalCount() == 0) {
            return BigDecimal.ZERO;
        }
        return stats.getTotalAmount().divide(BigDecimal.valueOf(stats.getTotalCount()), 0, RoundingMode.HALF_UP);
    }
    
    /**
     * 빈 요약 통계 객체 생성 (통계 데이터가 없을 때 사용)
     */
    public static GiftMoneySummaryResponse createEmpty(Long coupleId) {
        return GiftMoneySummaryResponse.builder()
                .coupleId(coupleId)
                .totalAmount(BigDecimal.ZERO)
                .totalCount(0)
                .thanksNotSentCount(0)
                .averageAmount(BigDecimal.ZERO)
                .build();
    }
}
