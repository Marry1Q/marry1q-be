package com.marry1q.marry1qbe.domain.account.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedAccountListRequest {
    private String userName;
    private String userNum;
    private String userEmail;
    private String ainfoAgreeYn;
    private String inquiryBankType;
    private String traceNo;
    private String inquiryRecordCnt;
}
