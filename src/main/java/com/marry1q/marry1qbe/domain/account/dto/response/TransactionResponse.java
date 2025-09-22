package com.marry1q.marry1qbe.domain.account.dto.response;

import com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래내역 응답")
public class TransactionResponse {
    
    @Schema(description = "거래 ID", example = "1")
    private Long id;
    
    @Schema(description = "거래 타입", example = "deposit", allowableValues = {"deposit", "withdraw"})
    private String type;
    
    @Schema(description = "거래 설명", example = "월급 입금")
    private String description;
    
    @Schema(description = "거래 금액", example = "3000000")
    private BigDecimal amount;
    
    @Schema(description = "거래 날짜", example = "2024-01-15")
    private LocalDate date;
    
    @Schema(description = "거래 시간", example = "14:30:00")
    private LocalTime time;
    
    @Schema(description = "보낸 사람", example = "회사")
    private String fromName;
    
    @Schema(description = "받는 사람", example = "김민수")
    private String toName;
    
    @Schema(description = "리뷰 상태", example = "pending", allowableValues = {"pending", "reviewed"})
    private String reviewStatus;
    
    @Schema(description = "카테고리 정보")
    private CategoryInfo category;
    
    @Schema(description = "메모", example = "1월 급여")
    private String memo;
    
    @Schema(description = "거래 후 잔액", example = "5450000")
    private BigDecimal balanceAfterTransaction;

    @Schema(description = "안심계좌 입금 여부", example = "true")
    private Boolean isSafeAccountDeposit;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리 정보")
    public static class CategoryInfo {
        @Schema(description = "카테고리 ID", example = "1")
        private Long id;
        
        @Schema(description = "카테고리명", example = "급여")
        private String name;
    }
    
    public static TransactionResponse fromEntity(CoupleAccountTransaction entity) {
        CategoryInfo categoryInfo = null;
        if (entity.getFinanceCategoryId() != null) {
            categoryInfo = CategoryInfo.builder()
                    .id(entity.getFinanceCategoryId())
                    .name("카테고리명") // TODO: 실제 카테고리명 조회 필요
                    .build();
        }
        
        return TransactionResponse.builder()
                .id(entity.getAccountTransactionId())
                .type(entity.getType().name().toLowerCase())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .date(entity.getTransactionDate())
                .time(entity.getTransactionTime())
                .fromName(entity.getFromName())
                .toName(entity.getToName())
                .reviewStatus(entity.getReviewStatus().name().toLowerCase())
                .category(categoryInfo)
                .memo(entity.getMemo())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .isSafeAccountDeposit(entity.getIsSafeAccountDeposit())
                .build();
    }
    
    public static TransactionResponse fromEntity(CoupleAccountTransaction entity, 
                                                com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        CategoryInfo categoryInfo = null;
        if (entity.getFinanceCategoryId() != null) {
            categoryInfo = CategoryInfo.builder()
                    .id(entity.getFinanceCategoryId())
                    .name("카테고리명") // TODO: 실제 카테고리명 조회 필요
                    .build();
        }
        
        return TransactionResponse.builder()
                .id(entity.getAccountTransactionId())
                .type(entity.getType().name().toLowerCase())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .date(entity.getTransactionDate())
                .time(entity.getTransactionTime())
                .fromName(entity.getFromName())
                .toName(entity.getToName())
                .reviewStatus(entity.getReviewStatus().name().toLowerCase())
                .category(categoryInfo)
                .memo(entity.getMemo())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .isSafeAccountDeposit(entity.getIsSafeAccountDeposit())
                .build();
    }
}
