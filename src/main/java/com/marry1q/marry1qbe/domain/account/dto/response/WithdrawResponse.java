package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 모임통장에서 보내기 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "모임통장에서 보내기 응답")
public class WithdrawResponse {
    
    /**
     * 거래 ID
     */
    @Schema(description = "거래 고유 ID", example = "TXN123456789")
    private Long transactionId;
    
    /**
     * 모임통장 계좌번호
     */
    @Schema(description = "모임통장 계좌번호", example = "110-654321-098765")
    private String accountNumber;
    
    /**
     * 이체 금액
     */
    @Schema(description = "이체 금액", example = "100000")
    private BigDecimal amount;
    
    /**
     * 이체 후 잔액
     */
    @Schema(description = "이체 후 모임통장 잔액", example = "400000")
    private BigDecimal balanceAfterTransaction;
    
    /**
     * 이체 설명
     */
    @Schema(description = "이체 설명", example = "용돈 보내기")
    private String description;
    
    /**
     * 메모
     */
    @Schema(description = "내부 메모", example = "12월 용돈")
    private String memo;
    
    /**
     * 거래 날짜
     */
    @Schema(description = "거래 날짜", example = "2024-01-15")
    private String transactionDate;
    
    /**
     * 거래 시간
     */
    @Schema(description = "거래 시간", example = "14:30:25")
    private String transactionTime;
    
    /**
     * 보낸 사람 이름
     */
    @Schema(description = "보낸 사람 이름", example = "이영희")
    private String fromName;
    
    /**
     * 받는 사람 이름
     */
    @Schema(description = "받는 사람 이름", example = "김철수")
    private String toName;
    
    /**
     * 처리 상태
     */
    @Schema(description = "처리 상태", example = "SUCCESS")
    private String status;
    
    /**
     * 완료 시간
     */
    @Schema(description = "거래 완료 시간", example = "2024-01-15T14:30:25")
    private LocalDateTime completedAt;
}
