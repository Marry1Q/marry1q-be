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
@Schema(description = "상품별 자동이체 납입 정보 응답")
public class ProductPaymentInfoResponse {
    
    @Schema(description = "자동이체 ID", example = "1")
    private Long autoTransferId;
    
    @Schema(description = "출금 계좌번호", example = "110-987-654321")
    private String fromAccountNumber;
    
    @Schema(description = "입금 계좌번호", example = "110-123-456789")
    private String toAccountNumber;
    
    @Schema(description = "이체 금액", example = "500000")
    private BigDecimal amount;
    
    @Schema(description = "다음 납입일", example = "2024-02-25")
    private LocalDate nextPaymentDate;
    
    @Schema(description = "현재 회차", example = "3")
    private Integer currentInstallment;
    
    @Schema(description = "총 회차", example = "12")
    private Integer totalInstallments;
    
    @Schema(description = "남은 회차", example = "9")
    private Integer remainingInstallments;
    
    @Schema(description = "납입 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "PENDING", "FAILED"})
    private String paymentStatus;
    
    @Schema(description = "1회차 여부", example = "false")
    private Boolean isFirstInstallment;
    
    @Schema(description = "최종 실행일", example = "2024-01-25")
    private LocalDate lastExecutionDate;
}
