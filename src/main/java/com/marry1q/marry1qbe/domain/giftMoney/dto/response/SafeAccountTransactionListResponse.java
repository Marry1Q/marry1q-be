package com.marry1q.marry1qbe.domain.giftMoney.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "안심계좌 입금 내역 목록 응답")
public class SafeAccountTransactionListResponse {
    
    @Schema(description = "안심계좌 입금 내역 목록")
    private List<SafeAccountTransactionResponse> content;
    
    @Schema(description = "페이지 정보")
    private PageInfo pageInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지 정보")
    public static class PageInfo {
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int pageNumber;
        
        @Schema(description = "페이지 크기", example = "10")
        private int pageSize;
        
        @Schema(description = "전체 요소 수", example = "25")
        private long totalElements;
        
        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;
        
        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private boolean first;
        
        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;
        
        @Schema(description = "현재 페이지 요소 수", example = "10")
        private int numberOfElements;
    }
    
    /**
     * Page<CoupleAccountTransaction>을 SafeAccountTransactionListResponse로 변환
     */
    public static SafeAccountTransactionListResponse from(Page<com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction> transactionPage) {
        List<SafeAccountTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(SafeAccountTransactionResponse::from)
                .toList();
        
        PageInfo pageInfo = PageInfo.builder()
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .first(transactionPage.isFirst())
                .last(transactionPage.isLast())
                .numberOfElements(transactionPage.getNumberOfElements())
                .build();
        
        return SafeAccountTransactionListResponse.builder()
                .content(content)
                .pageInfo(pageInfo)
                .build();
    }
}
