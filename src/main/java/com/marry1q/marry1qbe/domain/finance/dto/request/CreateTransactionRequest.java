package com.marry1q.marry1qbe.domain.finance.dto.request;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "거래 내역 생성 요청")
public class CreateTransactionRequest {
    
    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0.01 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "금액은 최대 10자리 정수와 2자리 소수점까지 허용됩니다.")
    @Schema(description = "거래 금액", example = "500000", minimum = "0.01")
    private BigDecimal amount;
    
    @NotNull(message = "거래 타입은 필수입니다.")
    @Schema(description = "거래 타입", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
    private FinanceTransaction.TransactionType transactionType;
    
    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 100, message = "설명은 최대 100자까지 입력 가능합니다.")
    @Schema(description = "거래 설명", example = "웨딩홀 예약금", maxLength = 100)
    private String description;
    
    @Size(max = 500, message = "메모는 최대 500자까지 입력 가능합니다.")
    @Schema(description = "거래 메모", example = "신라호텔 예약금", maxLength = 500, required = false)
    private String memo;
    
    @NotNull(message = "거래 날짜는 필수입니다.")
    @Schema(description = "거래 날짜", example = "2024-01-15")
    private LocalDate transactionDate;
    
    @Schema(description = "거래 시간", example = "14:30:00", required = false)
    private LocalTime transactionTime;
    
    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
}
