package com.marry1q.marry1qbe.domain.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 계좌주명 조회 요청 DTO
 * 
 * 계좌번호와 은행코드를 통해 해당 계좌의 예금주명을 조회하는 요청을 처리합니다.
 * auth-backend의 계좌 상세정보 조회 API를 통해 실제 계좌주명을 조회합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계좌주명 조회 요청")
public class AccountHolderNameRequest {
    
    /**
     * 은행 코드
     * 
     * 조회할 계좌가 속한 은행의 표준 코드입니다.
     * auth-backend의 계좌 상세정보 조회 API에서 bankCode 필드로 전달됩니다.
     */
    @NotBlank(message = "은행 코드는 필수입니다.")
    @Schema(description = "은행 코드 (081: 하나은행, 088: 신한은행 등)", example = "081")
    private String bankCode;
    
    /**
     * 계좌 번호
     * 
     * 계좌주명을 조회할 계좌번호입니다.
     * auth-backend의 계좌 상세정보 조회 API에서 accountNum 필드로 전달됩니다.
     */
    @NotBlank(message = "계좌 번호는 필수입니다.")
    @Schema(description = "계좌 번호", example = "110-123456-789012")
    private String accountNumber;
}
