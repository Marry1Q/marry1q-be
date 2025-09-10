package com.marry1q.marry1qbe.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedAccountListResponse {
    private List<Account> resList;  // auth-backend 응답 구조에 맞춤
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        private String listNum;           // auth-backend는 String 타입
        private String bankCodeStd;
        private String activityType;
        private String accountType;
        private String accountNum;
        private String accountNumMasked;
        private String accountSeq;
        private String accountHolderName;
        private String accountIssueDate;
        private String lastTranDate;
        private String productName;
        private String productSubName;
        private String dormancyYn;
        private Long balanceAmt;
        private Long depositAmt;
        private String balanceCalcBasis1;
        private String balanceCalcBasis2;
        private String investmentLinkedYn;
        private String bankLinkedYn;
        private String balanceAfterCancelYn;
        private String savingsBankCode;
    }
}
