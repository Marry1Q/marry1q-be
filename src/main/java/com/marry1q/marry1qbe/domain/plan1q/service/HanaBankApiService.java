package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountSubscriptionRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountSubscriptionResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountProfitInfoResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.HanaBankProductResponse;
import com.marry1q.marry1qbe.grobal.config.ExternalApiConfig;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class HanaBankApiService {
    
    private final RestTemplate restTemplate;
    private final ExternalApiConfig externalApiConfig;
    
    /**
     * 상품 가입 및 계좌 생성
     */
    public AccountSubscriptionResponse subscribeProduct(AccountSubscriptionRequest request) {
        String endpoint = "/api/v1/accounts/subscribe";
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("하나은행 백엔드 상품 가입 요청 - URL: {}, 요청: {}", fullUrl, request);
        
        try {
            HttpEntity<AccountSubscriptionRequest> entity = new HttpEntity<>(request);
            ResponseEntity<ApiResponseWrapper<AccountSubscriptionResponse>> response = restTemplate.exchange(
                fullUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponseWrapper<AccountSubscriptionResponse>>() {});
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                AccountSubscriptionResponse data = response.getBody().getData();
                log.info("하나은행 백엔드 상품 가입 성공 - 응답: {}", data);
                return data;
            } else {
                String errorMessage = response.getBody() != null ? response.getBody().getMessage() : "알 수 없는 오류";
                log.error("하나은행 백엔드 상품 가입 실패 - 상태코드: {}, 메시지: {}", response.getStatusCode(), errorMessage);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 상품 가입 실패: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 상품 가입 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 상품 가입 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * 계좌별 실시간 수익 정보 조회
     */
    public AccountProfitInfoResponse getAccountProfitInfo(String accountNumber, String userCi) {
        String endpoint = "/api/v1/accounts/" + accountNumber + "/profit-info?userCi=" + userCi;
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("하나은행 백엔드 계좌 수익 정보 조회 - URL: {}", fullUrl);
        
        try {
            ResponseEntity<ApiResponseWrapper<AccountProfitInfoResponse>> response = restTemplate.exchange(
                fullUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponseWrapper<AccountProfitInfoResponse>>() {});
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                AccountProfitInfoResponse data = response.getBody().getData();
                log.info("하나은행 백엔드 계좌 수익 정보 조회 성공 - 응답: {}", data);
                return data;
            } else {
                String errorMessage = response.getBody() != null ? response.getBody().getMessage() : "알 수 없는 오류";
                log.error("하나은행 백엔드 계좌 수익 정보 조회 실패 - 상태코드: {}, 메시지: {}", response.getStatusCode(), errorMessage);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 계좌 수익 정보 조회 실패: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 계좌 수익 정보 조회 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 계좌 수익 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 모든 상품 조회
     */
    public List<HanaBankProductResponse> getAllProducts() {
        String endpoint = "/api/v1/products";
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("하나은행 백엔드 모든 상품 조회 - URL: {}", fullUrl);
        
        try {
            // Define the response type for the API response wrapper
            ParameterizedTypeReference<ApiResponseWrapper<ProductListResponseWrapper>> responseType = 
                new ParameterizedTypeReference<ApiResponseWrapper<ProductListResponseWrapper>>() {};
            
            ResponseEntity<ApiResponseWrapper<ProductListResponseWrapper>> response = 
                restTemplate.exchange(fullUrl, HttpMethod.GET, null, responseType);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
                List<ProductResponseWrapper> products = response.getBody().getData().getProducts();
                List<HanaBankProductResponse> hanaBankProducts = new ArrayList<>();
                
                for (ProductResponseWrapper product : products) {
                    HanaBankProductResponse hanaBankProduct = HanaBankProductResponse.builder()
                        .productId(product.getProductId())
                        .externalProductId(product.getExternalProductId())
                        .productCode(product.getProductCode())
                        .productName(product.getProductName())
                        .productSubName(product.getProductSubName())
                        .productType(product.getProductType())
                        .baseRate(product.getBaseRate() != null ? product.getBaseRate().doubleValue() : null)
                        .maxRate(product.getMaxRate() != null ? product.getMaxRate().doubleValue() : null)
                        .expectedReturnRate(product.getExpectedReturnRate() != null ? product.getExpectedReturnRate().doubleValue() : null)
                        .minInvestmentAmount(product.getMinInvestmentAmount() != null ? product.getMinInvestmentAmount().longValue() : null)
                        .maxInvestmentAmount(product.getMaxInvestmentAmount() != null ? product.getMaxInvestmentAmount().longValue() : null)
                        .monthlyInvestmentAmount(product.getMonthlyInvestmentAmount() != null ? product.getMonthlyInvestmentAmount().longValue() : null)
                        .minPeriodMonths(product.getMinPeriodMonths())
                        .maxPeriodMonths(product.getMaxPeriodMonths())
                        .maturityPeriodMonths(product.getMaturityPeriodMonths())
                        .riskLevel(product.getRiskLevel())
                        .riskScore(product.getRiskScore())
                        .isTaxFree(product.getIsTaxFree())
                        .isGuaranteed(product.getIsGuaranteed())
                        .productDescription(product.getProductDescription())
                        .build();
                    
                    hanaBankProducts.add(hanaBankProduct);
                }
                
                log.info("하나은행 백엔드 모든 상품 조회 성공 - 상품 수: {}", hanaBankProducts.size());
                return hanaBankProducts;
            } else {
                log.error("하나은행 백엔드 모든 상품 조회 실패 - 상태코드: {}", response.getStatusCode());
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 모든 상품 조회 실패");
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 모든 상품 조회 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 백엔드 모든 상품 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // Helper classes for API response mapping
    private static class ApiResponseWrapper<T> {
        private T data;
        private String message;
        private boolean success;
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
    
    private static class ProductListResponseWrapper {
        private List<ProductResponseWrapper> products;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        
        public List<ProductResponseWrapper> getProducts() { return products; }
        public void setProducts(List<ProductResponseWrapper> products) { this.products = products; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }
    
    private static class ProductResponseWrapper {
        private Long productId;
        private String externalProductId;
        private String productCode;
        private String productName;
        private String productSubName;
        private String productType;
        private BigDecimal baseRate;
        private BigDecimal maxRate;
        private BigDecimal expectedReturnRate;
        private BigDecimal minInvestmentAmount;
        private BigDecimal maxInvestmentAmount;
        private BigDecimal monthlyInvestmentAmount;
        private Integer minPeriodMonths;
        private Integer maxPeriodMonths;
        private Integer maturityPeriodMonths;
        private String riskLevel;
        private Integer riskScore;
        private Boolean isTaxFree;
        private Boolean isGuaranteed;
        private String productDescription;
        
        // Getters
        public Long getProductId() { return productId; }
        public String getExternalProductId() { return externalProductId; }
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public String getProductSubName() { return productSubName; }
        public String getProductType() { return productType; }
        public BigDecimal getBaseRate() { return baseRate; }
        public BigDecimal getMaxRate() { return maxRate; }
        public BigDecimal getExpectedReturnRate() { return expectedReturnRate; }
        public BigDecimal getMinInvestmentAmount() { return minInvestmentAmount; }
        public BigDecimal getMaxInvestmentAmount() { return maxInvestmentAmount; }
        public BigDecimal getMonthlyInvestmentAmount() { return monthlyInvestmentAmount; }
        public Integer getMinPeriodMonths() { return minPeriodMonths; }
        public Integer getMaxPeriodMonths() { return maxPeriodMonths; }
        public Integer getMaturityPeriodMonths() { return maturityPeriodMonths; }
        public String getRiskLevel() { return riskLevel; }
        public Integer getRiskScore() { return riskScore; }
        public Boolean getIsTaxFree() { return isTaxFree; }
        public Boolean getIsGuaranteed() { return isGuaranteed; }
        public String getProductDescription() { return productDescription; }
        
        // Setters
        public void setProductId(Long productId) { this.productId = productId; }
        public void setExternalProductId(String externalProductId) { this.externalProductId = externalProductId; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setProductSubName(String productSubName) { this.productSubName = productSubName; }
        public void setProductType(String productType) { this.productType = productType; }
        public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }
        public void setMaxRate(BigDecimal maxRate) { this.maxRate = maxRate; }
        public void setExpectedReturnRate(BigDecimal expectedReturnRate) { this.expectedReturnRate = expectedReturnRate; }
        public void setMinInvestmentAmount(BigDecimal minInvestmentAmount) { this.minInvestmentAmount = minInvestmentAmount; }
        public void setMaxInvestmentAmount(BigDecimal maxInvestmentAmount) { this.maxInvestmentAmount = maxInvestmentAmount; }
        public void setMonthlyInvestmentAmount(BigDecimal monthlyInvestmentAmount) { this.monthlyInvestmentAmount = monthlyInvestmentAmount; }
        public void setMinPeriodMonths(Integer minPeriodMonths) { this.minPeriodMonths = minPeriodMonths; }
        public void setMaxPeriodMonths(Integer maxPeriodMonths) { this.maxPeriodMonths = maxPeriodMonths; }
        public void setMaturityPeriodMonths(Integer maturityPeriodMonths) { this.maturityPeriodMonths = maturityPeriodMonths; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
        public void setIsTaxFree(Boolean isTaxFree) { this.isTaxFree = isTaxFree; }
        public void setIsGuaranteed(Boolean isGuaranteed) { this.isGuaranteed = isGuaranteed; }
        public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    }
}
