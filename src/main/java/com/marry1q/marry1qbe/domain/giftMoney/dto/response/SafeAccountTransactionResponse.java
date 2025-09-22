package com.marry1q.marry1qbe.domain.giftMoney.dto.response;

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
@Schema(description = "안심계좌 입금 내역 응답")
public class SafeAccountTransactionResponse {
    
    @Schema(description = "거래 ID", example = "1")
    private Long transactionId;
    
    @Schema(description = "거래 타입", example = "deposit", allowableValues = {"deposit", "withdraw"})
    private String type;
    
    @Schema(description = "거래 설명", example = "축의금 입금")
    private String description;
    
    @Schema(description = "거래 금액", example = "100000")
    private BigDecimal amount;
    
    @Schema(description = "거래 날짜", example = "2024-01-15")
    private LocalDate transactionDate;
    
    @Schema(description = "거래 시간", example = "14:30:00")
    private LocalTime transactionTime;
    
    @Schema(description = "보낸 사람", example = "김철수")
    private String fromName;
    
    @Schema(description = "받는 사람", example = "김민수")
    private String toName;
    
    @Schema(description = "리뷰 상태", example = "pending", allowableValues = {"pending", "reviewed"})
    private String reviewStatus;
    
    @Schema(description = "메모", example = "축의금")
    private String memo;
    
    @Schema(description = "거래 후 잔액", example = "5000000")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "안심계좌 입금 여부", example = "true")
    private Boolean isSafeAccountDeposit;
    
    /**
     * CoupleAccountTransaction 엔티티를 SafeAccountTransactionResponse로 변환
     */
    public static SafeAccountTransactionResponse from(CoupleAccountTransaction entity) {
        return SafeAccountTransactionResponse.builder()
                .transactionId(entity.getAccountTransactionId())
                .type(entity.getType().name().toLowerCase())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .transactionDate(entity.getTransactionDate())
                .transactionTime(entity.getTransactionTime())
                .fromName(entity.getFromName())
                .toName(entity.getToName())
                .reviewStatus(entity.getReviewStatus().name().toLowerCase())
                .memo(entity.getMemo())
                .balanceAfterTransaction(entity.getBalanceAfterTransaction())
                .isSafeAccountDeposit(entity.getIsSafeAccountDeposit())
                .build();
    }
}
