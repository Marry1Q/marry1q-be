package com.marry1q.marry1qbe.domain.account.service.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marry1q.marry1qbe.domain.account.dto.request.AccountRegisterRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.IntegratedAccountListRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.IntegratedAccountListResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.UserRegisterResponse;
import com.marry1q.marry1qbe.grobal.config.ExternalApiConfig;
import com.marry1q.marry1qbe.grobal.dto.ExternalApiResponse;
import com.marry1q.marry1qbe.grobal.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthBackendApiService implements ExternalApiService {
    
    private final RestTemplate restTemplate;
    private final ExternalApiConfig externalApiConfig;
    private final ObjectMapper objectMapper;
    
    @Override
    public <T> ExternalApiResponse<T> callApi(String endpoint, Object requestBody, Class<T> responseType) {
        return callApi(endpoint, requestBody, null, responseType);
    }
    
    @Override
    public <T> ExternalApiResponse<T> callApi(String endpoint, Object requestBody, String authorizationHeader, Class<T> responseType) {
        String baseUrl = externalApiConfig.getAuthBackend().getUrl();
        String fullUrl = baseUrl + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (authorizationHeader != null) {
            headers.setBearerAuth(authorizationHeader);
        }
        
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → AUTH-BACKEND] API 요청");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 데이터: {}", requestBody);
        log.info("🔑 토큰: {}...{}", 
                authorizationHeader != null ? authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) : "null",
                authorizationHeader != null && authorizationHeader.length() > 20 ? authorizationHeader.substring(authorizationHeader.length() - 10) : "");
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [AUTH-BACKEND → MARRY1Q-BE] API 응답 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                // data 필드를 responseType으로 변환
                Object data = responseBody.get("data");
                T convertedData = null;
                
                if (data != null && responseType != Object.class) {
                    if (data instanceof Map) {
                        convertedData = objectMapper.convertValue(data, responseType);
                    } else {
                        convertedData = objectMapper.convertValue(data, responseType);
                    }
                } else if (data != null) {
                    convertedData = (T) data;
                }
                
                ExternalApiResponse<T> externalApiResponse = new ExternalApiResponse<>();
                externalApiResponse.setSuccess(true);
                externalApiResponse.setMessage((String) responseBody.get("message"));
                externalApiResponse.setData(convertedData);
                
                return externalApiResponse;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [AUTH-BACKEND → MARRY1Q-BE] API 응답 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new ExternalApiException("AuthBackend", "오픈뱅킹 API 호출 실패", errorMessage, response.getStatusCodeValue());
            }
            
        } catch (Exception e) {
            log.error("오픈뱅킹 API 호출 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            throw new ExternalApiException("AuthBackend", "오픈뱅킹 API 호출 중 오류 발생", e.getMessage());
        }
    }
    
    /**
     * 출금이체 요청
     */
    public ExternalApiResponse<Object> requestWithdrawTransfer(Object requestBody, String token) {
        String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("withdraw-transfer");
        return callApi(endpoint, requestBody, token, Object.class);
    }
    
    /**
     * 입금이체 요청
     */
    public ExternalApiResponse<Object> requestDepositTransfer(Object requestBody, String token) {
        String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("deposit-transfer");
        return callApi(endpoint, requestBody, token, Object.class);
    }
    
    /**
     * 계좌 잔액 조회
     */
    public ExternalApiResponse<BalanceResponse> getAccountBalance(BalanceRequest requestBody, String token) {
        String endpoint = "/v2.0/account/balance/acnt_num";
        return callApi(endpoint, requestBody, token, BalanceResponse.class);
    }
    
    /**
     * 거래내역 조회
     */
    public ExternalApiResponse<TransactionListResponse> getTransactionHistory(TransactionListRequest requestBody, String token) {
        String endpoint = "/v2.0/account/transaction_list/acnt_num";
        return callApi(endpoint, requestBody, token, TransactionListResponse.class);
    }
    
    // DTO 클래스들
    public static class BalanceRequest {
        private String userSeqNo;
        private String bankCodeStd;
        private String accountNum;
        
        // 생성자, getter, setter
        public BalanceRequest() {}
        
        public BalanceRequest(String userSeqNo, String bankCodeStd, String accountNum) {
            this.userSeqNo = userSeqNo;
            this.bankCodeStd = bankCodeStd;
            this.accountNum = accountNum;
        }
        
        public String getUserSeqNo() { return userSeqNo; }
        public void setUserSeqNo(String userSeqNo) { this.userSeqNo = userSeqNo; }
        
        public String getBankCodeStd() { return bankCodeStd; }
        public void setBankCodeStd(String bankCodeStd) { this.bankCodeStd = bankCodeStd; }
        
        public String getAccountNum() { return accountNum; }
        public void setAccountNum(String accountNum) { this.accountNum = accountNum; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalanceResponse {
        private String bankName;
        private String accountNum;
        private String balanceAmt;
        
        // 생성자, getter, setter
        public BalanceResponse() {}
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getAccountNum() { return accountNum; }
        public void setAccountNum(String accountNum) { this.accountNum = accountNum; }
        
        public String getBalanceAmt() { return balanceAmt; }
        public void setBalanceAmt(String balanceAmt) { this.balanceAmt = balanceAmt; }
        
        public BigDecimal getBalanceAsBigDecimal() {
            return new BigDecimal(balanceAmt);
        }
    }
    
    public static class TransactionListRequest {
        private String userSeqNo;
        private String bankCodeStd;
        private String accountNum;
        private String startDate;
        private String endDate;
        private String tranType;
        private String sortOrder;
        private String pageIndex;
        private String pageSize;
        
        // 생성자, getter, setter
        public TransactionListRequest() {}
        
        public TransactionListRequest(String userSeqNo, String bankCodeStd, String accountNum, 
                                    String startDate, String endDate) {
            this.userSeqNo = userSeqNo;
            this.bankCodeStd = bankCodeStd;
            this.accountNum = accountNum;
            this.startDate = startDate;
            this.endDate = endDate;
            this.tranType = "A";
            this.sortOrder = "D";
            this.pageIndex = "001";
            this.pageSize = "100";
        }
        
        // getter, setter 메서드들
        public String getUserSeqNo() { return userSeqNo; }
        public void setUserSeqNo(String userSeqNo) { this.userSeqNo = userSeqNo; }
        
        public String getBankCodeStd() { return bankCodeStd; }
        public void setBankCodeStd(String bankCodeStd) { this.bankCodeStd = bankCodeStd; }
        
        public String getAccountNum() { return accountNum; }
        public void setAccountNum(String accountNum) { this.accountNum = accountNum; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public String getTranType() { return tranType; }
        public void setTranType(String tranType) { this.tranType = tranType; }
        
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
        
        public String getPageIndex() { return pageIndex; }
        public void setPageIndex(String pageIndex) { this.pageIndex = pageIndex; }
        
        public String getPageSize() { return pageSize; }
        public void setPageSize(String pageSize) { this.pageSize = pageSize; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionListResponse {
        private String bankName;
        private String accountNum;
        private String balanceAmt;
        private List<TransactionItem> resList;
        
        // 생성자, getter, setter
        public TransactionListResponse() {}
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getAccountNum() { return accountNum; }
        public void setAccountNum(String accountNum) { this.accountNum = accountNum; }
        
        public String getBalanceAmt() { return balanceAmt; }
        public void setBalanceAmt(String balanceAmt) { this.balanceAmt = balanceAmt; }
        
        public List<TransactionItem> getResList() { return resList; }
        public void setResList(List<TransactionItem> resList) { this.resList = resList; }
    }
    
    public static class TransactionItem {
        private String tranDate;
        private String tranTime;
        private String inoutType;
        private String tranType;
        private String printedContent;
        private String tranAmt;
        private String afterBalanceAmt;
        private String branchName;
        private String bankTranId;
        private Boolean isSafeAccountDeposit;
        
        // 생성자, getter, setter
        public TransactionItem() {}
        
        public String getTranDate() { return tranDate; }
        public void setTranDate(String tranDate) { this.tranDate = tranDate; }
        
        public String getTranTime() { return tranTime; }
        public void setTranTime(String tranTime) { this.tranTime = tranTime; }
        
        public String getInoutType() { return inoutType; }
        public void setInoutType(String inoutType) { this.inoutType = inoutType; }
        
        public String getTranType() { return tranType; }
        public void setTranType(String tranType) { this.tranType = tranType; }
        
        public String getPrintedContent() { return printedContent; }
        public void setPrintedContent(String printedContent) { this.printedContent = printedContent; }
        
        public String getTranAmt() { return tranAmt; }
        public void setTranAmt(String tranAmt) { this.tranAmt = tranAmt; }
        
        public String getAfterBalanceAmt() { return afterBalanceAmt; }
        public void setAfterBalanceAmt(String afterBalanceAmt) { this.afterBalanceAmt = afterBalanceAmt; }
        
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        
        public String getBankTranId() { return bankTranId; }
        public void setBankTranId(String bankTranId) { this.bankTranId = bankTranId; }
        
        public Boolean getIsSafeAccountDeposit() { return isSafeAccountDeposit; }
        public void setIsSafeAccountDeposit(Boolean isSafeAccountDeposit) { this.isSafeAccountDeposit = isSafeAccountDeposit; }
    }
    
    /**
     * 계좌주명 조회
     * 
     * auth-backend의 계좌 상세정보 조회 API를 호출하여 계좌주명을 조회합니다.
     * 
     * @param accessToken 오픈뱅킹 액세스 토큰
     * @param bankCode 은행 코드 (예: "081")
     * @param accountNumber 계좌번호
     * @return 계좌주명
     * @throws IllegalArgumentException 존재하지 않는 계좌인 경우
     * @throws ExternalApiException 외부 API 호출 실패
     */
    public String getAccountHolderName(String accessToken, String bankCode, String accountNumber) {
        log.info("계좌주명 조회 API 호출 시작 - 은행코드: {}, 계좌번호: {}", bankCode, accountNumber);
        
        try {
            // 간단한 계좌주명 조회 요청 바디 구성 (은행코드, 계좌번호만)
            Map<String, Object> requestBody = Map.of(
                "bankCode", bankCode,
                "accountNumber", accountNumber
            );
            
            // auth-backend 간단한 계좌주명 조회 API 호출
            String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("account-holder-name");
            ExternalApiResponse<AccountHolderNameResponse> response = callApi(
                endpoint,
                requestBody,
                accessToken,
                AccountHolderNameResponse.class
            );
            
            if (!response.isSuccess() || response.getData() == null) {
                log.warn("계좌주명 조회 실패 - 응답: {}", response);
                throw new IllegalArgumentException("계좌 정보를 조회할 수 없습니다.");
            }
            
            AccountHolderNameResponse accountHolderData = response.getData();
            String accountHolderName = accountHolderData.getAccountHolderName();
            
            if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
                log.warn("계좌주명이 비어있음 - 응답 데이터: {}", accountHolderData);
                throw new IllegalArgumentException("계좌주명을 확인할 수 없습니다.");
            }
            
            log.info("계좌주명 조회 성공 - 계좌주명: {}", accountHolderName);
            return accountHolderName.trim();
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("계좌주명 조회 API 호출 실패 - 은행코드: {}, 계좌번호: {}, 오류: {}", 
                     bankCode, accountNumber, e.getMessage(), e);
            throw new ExternalApiException("AuthBackend", "계좌주명 조회에 실패했습니다.", e.getMessage());
        }
    }
    
    /**
     * 계좌주명 조회 응답 DTO (auth-backend의 AccountHolderNameResponse와 일치)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountHolderNameResponse {
        private String accountHolderName;
        
        public AccountHolderNameResponse() {}
        
        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
        
        @Override
        public String toString() {
            return "AccountHolderNameResponse{" +
                    "accountHolderName='" + accountHolderName + '\'' +
                    '}';
        }
    }
    
    /**
     * 계좌통합조회 요청
     */
    public ExternalApiResponse<IntegratedAccountListResponse> getIntegratedAccountList(
            IntegratedAccountListRequest requestBody, String token) {
        String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("account-info");
        return callApi(endpoint, requestBody, token, IntegratedAccountListResponse.class);
    }
    
    /**
     * 사용자 계좌 등록 요청
     */
    public ExternalApiResponse<UserRegisterResponse> registerUserAccount(
            AccountRegisterRequest requestBody, String token) {
        String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("user-register");
        return callApi(endpoint, requestBody, token, UserRegisterResponse.class);
    }
}
