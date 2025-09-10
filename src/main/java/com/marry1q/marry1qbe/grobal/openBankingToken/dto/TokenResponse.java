package com.marry1q.marry1qbe.grobal.openBankingToken.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    private String authAccessToken;
    private String tokenType;
    private long expiresIn;
    private String scope;
    private String clientUseCode;
}
