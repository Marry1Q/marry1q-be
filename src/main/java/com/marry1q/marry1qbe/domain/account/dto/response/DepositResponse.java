package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "입금 응답")
public class DepositResponse {
    
    @Schema(description = "거래 ID", example = "1")
    private Long transactionId;
    
    @Schema(description = "계좌 번호", example = "1234567890123456")
    private String accountNumber;
    
    @Schema(description = "입금 금액", example = "100000")
    private BigDecimal amount;
    
    @Schema(description = "입금 후 잔액", example = "1100000")
    private BigDecimal balanceAfterTransaction;
    
    @Schema(description = "입금 설명", example = "월급 입금")
    private String description;
    
    @Schema(description = "메모", example = "12월 월급")
    private String memo;
    
    @Schema(description = "거래 날짜", example = "2024-12-01")
    private String transactionDate;
    
    @Schema(description = "거래 시간", example = "14:30:00")
    private String transactionTime;
    
    @Schema(description = "보낸 사람", example = "김철수")
    private String fromName;
    
    @Schema(description = "받는 사람", example = "이영희")
    private String toName;
    
    @Schema(description = "거래 상태", example = "SUCCESS")
    private String status;
    
    @Schema(description = "거래 완료 시간")
    private LocalDateTime completedAt;
}
