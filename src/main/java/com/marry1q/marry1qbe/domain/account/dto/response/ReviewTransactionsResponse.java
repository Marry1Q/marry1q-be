package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 대기 거래내역 응답")
public class ReviewTransactionsResponse {
    
    @Schema(description = "거래내역 목록")
    private List<TransactionResponse> data;
    
    @Schema(description = "요약 정보")
    private SummaryInfo summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "요약 정보")
    public static class SummaryInfo {
        @Schema(description = "총 건수", example = "5")
        private Integer totalCount;
        
        @Schema(description = "총 금액", example = "8500000")
        private BigDecimal totalAmount;
    }
}
