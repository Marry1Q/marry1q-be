package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자동이체 응답")
public class AutoTransferResponse {
    
    @Schema(description = "자동이체 ID", example = "1")
    private Long autoTransferId;
    
    @Schema(description = "이체 대상 계좌번호", example = "110-123456-789012")
    private String toAccountNumber;
    
    @Schema(description = "이체 대상 계좌주명", example = "김철수")
    private String toAccountName;
    
    @Schema(description = "이체 대상 은행코드", example = "081")
    private String toBankCode;
    
    @Schema(description = "이체 금액", example = "500000")
    private BigDecimal amount;
    
    @Schema(description = "이체 주기", example = "매월 25일")
    private String schedule;
    
    @Schema(description = "다음 이체 예정일", example = "2024-02-25")
    private LocalDate nextTransferDate;
    
    @Schema(description = "메모", example = "Plan1Q 자동이체")
    private String memo;
    
    @Schema(description = "상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;
    
    @Schema(description = "생성일", example = "2024-01-01")
    private LocalDate createdAt;
    
    @Schema(description = "수정일", example = "2024-01-01")
    private LocalDate updatedAt;
    
    @Schema(description = "마지막 실행 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "PENDING"})
    private String lastExecutionStatus;
    
    @Schema(description = "사용자 CI", example = "CI123456789")
    private String userCi;
    
    @Schema(description = "총 납입 회차", example = "12")
    private Integer totalInstallments;
    
    @Schema(description = "현재 회차", example = "3")
    private Integer currentInstallment;
    
    @Schema(description = "남은 회차", example = "9")
    private Integer remainingInstallments;
    
    @Schema(description = "마지막 실행일", example = "2024-01-25")
    private LocalDate lastExecutionDate;
}
