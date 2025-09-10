package com.marry1q.marry1qbe.domain.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "자동이체 등록 요청")
public class AutoTransferCreateRequest {
    
    @NotBlank(message = "출금 계좌번호가 입력되지 않았습니다.")
    @Schema(description = "출금 계좌번호", example = "110-123456-789012")
    private String fromAccountNumber;
    
    @NotBlank(message = "계좌번호가 입력되지 않았습니다.")
    @Schema(description = "이체 대상 계좌번호", example = "110-123456-789012")
    private String toAccountNumber;
    
    @NotBlank(message = "계좌주명이 입력되지 않았습니다.")
    @Schema(description = "이체 대상 계좌주명", example = "김철수")
    private String toAccountName;
    
    @NotBlank(message = "은행코드가 입력되지 않았습니다.")
    @Schema(description = "이체 대상 은행코드", example = "081")
    private String toBankCode;
    
    @NotNull(message = "이체 금액이 입력되지 않았습니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    @Schema(description = "이체 금액", example = "500000")
    private BigDecimal amount;
    
    @NotBlank(message = "이체 주기가 선택되지 않았습니다.")
    @Schema(description = "이체 주기", example = "매월 25일", allowableValues = {"매월 1일", "매월 15일", "매월 25일", "매주 월요일"})
    private String frequency;
    
    @Schema(description = "메모", example = "월급 이체")
    private String memo;
    
    @NotNull(message = "총 납입 기간은 필수입니다.")
    @Positive(message = "총 납입 기간은 0보다 커야 합니다.")
    @Schema(description = "총 납입 기간 (개월)", example = "12")
    private Integer periodMonths;
    
    @Schema(description = "초기 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "PENDING"})
    private String initialStatus;
    
    @Schema(description = "현재 회차", example = "1")
    private Integer currentInstallment;
    
    @Schema(description = "남은 회차", example = "11")
    private Integer remainingInstallments;
    
    @Schema(description = "마지막 실행일", example = "2024-01-25")
    private LocalDate lastExecutionDate;
}
