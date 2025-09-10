package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계좌 정보 응답")
public class AccountResponse {
    
    @Schema(description = "계좌 ID", example = "1")
    private Long id;
    
    @Schema(description = "은행명", example = "하나은행")
    private String bank;
    
    @Schema(description = "계좌번호", example = "110-987-654321")
    private String accountNumber;
    
    @Schema(description = "계좌명", example = "김민수 급여통장")
    private String accountName;
    
    @Schema(description = "잔액", example = "3500000")
    private BigDecimal balance;
    
    @Schema(description = "기본 계좌 여부", example = "false")
    private Boolean isDefault;
}
