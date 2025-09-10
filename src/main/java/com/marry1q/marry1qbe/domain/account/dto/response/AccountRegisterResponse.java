package com.marry1q.marry1qbe.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterResponse {
    private String userSeqNo;
    private String accountNum;
    private String accountName;        // 프론트에서 입력한 값
    private String accountType;         // 프론트에서 입력한 값
    private boolean isCoupleAccount;    // 프론트에서 입력한 값
}
