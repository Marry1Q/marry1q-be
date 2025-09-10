package com.marry1q.marry1qbe.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterRequest {
    // auth-backend로 전송할 필드들
    private String bankTranId;
    private String bankCodeStd;
    private String registerAccountNum;
    private String userInfo;
    private String userName;
    private String userEmail;
    private String userCi;
    private String scope;
    private String accountAlias;  // 계좌별칭 추가
    
    // marry1q DB 저장용 추가 필드들 (프론트에서 입력)
    private String accountName;
    private String accountType;
    private boolean isCoupleAccount;
}
