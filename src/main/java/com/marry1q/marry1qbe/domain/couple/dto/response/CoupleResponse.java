package com.marry1q.marry1qbe.domain.couple.dto.response;

import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
@Schema(description = "커플 정보 응답")
public class CoupleResponse {
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "결혼 예정일", example = "2024-12-31")
    private LocalDate weddingDate;
    
    @Schema(description = "총 결혼 예산", example = "50000000")
    private BigDecimal totalBudget;
    
    @Schema(description = "커플 모임통장 계좌번호", example = "123-456-789012")
    private String coupleAccount;
    
    @Schema(description = "커플 카드 번호", example = "1234-5678-9012-3456")
    private String coupleCardNumber;
    
    @Schema(description = "현재 소비 금액", example = "15000000")
    private BigDecimal currentSpent;
    
    @Schema(description = "URL 슬러그", example = "couple-123")
    private String urlSlug;
    
    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private String createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    private String updatedAt;
    
    @Schema(description = "결혼까지 남은 일수", example = "220")
    private Long daysUntilWedding;
    
    @Schema(description = "커플 멤버 이름 목록", example = "[\"김민수\", \"이지은\"]")
    private List<String> memberNames;
    
    public static CoupleResponse from(Marry1qCouple couple, List<String> memberNames) {
        // 결혼까지 남은 일수 계산
        long daysUntilWedding = 0;
        if (couple.getWeddingDate() != null) {
            daysUntilWedding = ChronoUnit.DAYS.between(LocalDate.now(), couple.getWeddingDate());
        }
        
        return CoupleResponse.builder()
                .coupleId(couple.getCoupleId())
                .weddingDate(couple.getWeddingDate())
                .totalBudget(couple.getTotalBudget())
                .coupleAccount(couple.getCoupleAccount())
                .coupleCardNumber(couple.getCoupleCardNumber())
                .currentSpent(couple.getCurrentSpent())
                .urlSlug(couple.getUrlSlug())
                .createdAt(couple.getCreatedAt() != null ? couple.getCreatedAt().toString() : null)
                .updatedAt(couple.getUpdatedAt() != null ? couple.getUpdatedAt().toString() : null)
                .daysUntilWedding(daysUntilWedding)
                .memberNames(memberNames)
                .build();
    }
}

