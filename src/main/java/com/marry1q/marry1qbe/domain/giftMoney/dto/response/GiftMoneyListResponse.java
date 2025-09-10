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
@Schema(description = "축의금 목록 응답")
public class GiftMoneyListResponse {
    
    @Schema(description = "축의금 목록")
    private List<GiftMoneyResponse> content;
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int pageNumber;
    
    @Schema(description = "페이지 크기", example = "10")
    private int pageSize;
    
    @Schema(description = "전체 요소 개수", example = "25")
    private long totalElements;
    
    @Schema(description = "전체 페이지 개수", example = "3")
    private int totalPages;
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
    
    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private boolean first;
    
    @Schema(description = "현재 페이지의 요소 개수", example = "10")
    private int numberOfElements;
    
    public static GiftMoneyListResponse from(Page<GiftMoneyResponse> page) {
        return GiftMoneyListResponse.builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
