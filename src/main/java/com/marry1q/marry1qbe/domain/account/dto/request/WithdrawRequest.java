package com.marry1q.marry1qbe.domain.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 모임통장에서 보내기 요청 DTO
 * 
 * 사용자가 모임통장에서 다른 개인 계좌로 돈을 이체하는 요청을 처리합니다.
 * 
 * 처리 과정:
 * 1. 출금이체: 모임통장에서 출금
 * 2. 입금이체: 개인계좌로 입금
 * 
 * @see WithdrawService#processWithdraw(WithdrawRequest)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "모임통장에서 보내기 요청")
public class WithdrawRequest {
    
    /**
     * 받는 사람 은행 코드
     * 
     * 돈을 받을 개인 계좌가 속한 은행의 표준 코드입니다.
     * auth-backend의 입금이체 API에서 bankCodeStd 필드로 전달됩니다.
     */
    @NotBlank(message = "받는 사람 은행 코드는 필수입니다.")
    @Schema(description = "받는 사람 은행 코드 (081: 하나은행, 088: 신한은행 등)", example = "081")
    private String depositBankCode;
    
    /**
     * 받는 사람 계좌 번호
     * 
     * 돈을 받을 개인 계좌번호입니다.
     * auth-backend의 입금이체 API에서 accountNum 필드로 전달됩니다.
     */
    @NotBlank(message = "받는 사람 계좌 번호는 필수입니다.")
    @Schema(description = "받는 사람 계좌 번호 (돈을 받을 개인 계좌)", example = "110-123456-789012")
    private String depositAccountNumber;
    
    /**
     * 받는 사람 계좌주명
     * 
     * 돈을 받을 개인 계좌의 계좌주명입니다.
     * auth-backend의 입금이체 API에서 accountHolderName 필드로 전달됩니다.
     */
    @NotBlank(message = "받는 사람 계좌주명은 필수입니다.")
    @Schema(description = "받는 사람 계좌주명", example = "김철수")
    private String depositAccountHolderName;
    
    /**
     * 이체 금액
     * 
     * 모임통장에서 개인 계좌로 이체할 금액입니다.
     * 출금이체와 입금이체 모두 동일한 금액으로 처리됩니다.
     */
    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    @Schema(description = "이체 금액 (모임통장 → 개인 계좌)", example = "100000")
    private BigDecimal amount;
    
    /**
     * 개인계좌 입금 설명
     * 
     * 개인계좌 거래 내역에 표시될 설명입니다.
     * 입금이체에서 printContent 필드로 전달됩니다.
     */
    @Schema(description = "개인계좌에 표시될 설명", example = "용돈 보내기")
    private String depositDescription;
    
    /**
     * 모임통장 출금 설명
     * 
     * 모임통장 거래 내역에 표시될 설명입니다.
     * 출금이체에서 dpsPrintContent 필드로 전달됩니다.
     */
    @Schema(description = "모임통장에 표시될 설명", example = "용돈 보내기")
    private String withdrawDescription;
    
    /**
     * 메모
     * 
     * marry1q 내부에서만 사용되는 메모입니다.
     * 외부 API로 전달되지 않고 marry1q DB에만 저장됩니다.
     */
    @Schema(description = "내부 메모 (외부 API로 전달되지 않음)", example = "12월 용돈")
    private String memo;
    
    /**
     * 보낸 사람 이름
     * 
     * 모임통장의 소유자 이름입니다.
     * auth-backend의 출금이체 API에서 reqClientName 필드로 전달됩니다.
     */
    @Schema(description = "보낸 사람 이름 (모임통장 소유자)", example = "이영희")
    private String fromName;
    
    /**
     * 받는 사람 이름
     * 
     * 개인 계좌의 소유자 이름입니다.
     * auth-backend의 입금이체 API에서 accountHolderName 필드로 전달됩니다.
     */
    @Schema(description = "받는 사람 이름 (개인 계좌 소유자)", example = "김철수")
    private String toName;
}
