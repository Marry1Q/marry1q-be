package com.marry1q.marry1qbe.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {
    private String userSeqNo;
    private String fintechUseNum;  // 버리는 값이지만 auth-backend 응답에 포함됨
    private String accountNum;
}
