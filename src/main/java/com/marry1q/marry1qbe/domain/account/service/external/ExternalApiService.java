package com.marry1q.marry1qbe.domain.account.service.external;

import com.marry1q.marry1qbe.grobal.dto.ExternalApiResponse;

public interface ExternalApiService {
    
    /**
     * 외부 API 호출
     */
    <T> ExternalApiResponse<T> callApi(String endpoint, Object requestBody, Class<T> responseType);
    
    /**
     * 외부 API 호출 (헤더 포함)
     */
    <T> ExternalApiResponse<T> callApi(String endpoint, Object requestBody, String authorizationHeader, Class<T> responseType);
}
