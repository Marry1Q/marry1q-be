package com.marry1q.marry1qbe.domain.account.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marry1q.marry1qbe.grobal.config.ExternalApiConfig;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HanaBankAutoTransferApiClient {
    
    private final RestTemplate restTemplate;
    private final ExternalApiConfig externalApiConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * 자동이체 등록
     */
    public Map<String, Object> createAutoTransfer(Object requestBody) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-create");
        return callApi(endpoint, requestBody, Map.class);
    }
    
    /**
     * 자동이체 목록 조회
     */
    public Map<String, Object> listAutoTransfers(String fromAccountNumber) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-list");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint + "?fromAccountNumber=" + fromAccountNumber;
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] 자동이체 목록 조회");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 파라미터: fromAccountNumber={}", fromAccountNumber);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                new HttpEntity<>(createJsonHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] 자동이체 목록 조회 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] 자동이체 목록 조회 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 자동이체 목록 조회 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 자동이체 목록 조회 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    /**
     * 자동이체 상세 조회
     */
    public Map<String, Object> getAutoTransferDetail(Long autoTransferId) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-detail");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint.replace("{autoTransferId}", autoTransferId.toString());
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] 자동이체 상세 조회");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 파라미터: autoTransferId={}", autoTransferId);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                new HttpEntity<>(createJsonHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] 자동이체 상세 조회 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] 자동이체 상세 조회 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 자동이체 상세 조회 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 자동이체 상세 조회 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    /**
     * 자동이체 수정
     */
    public Map<String, Object> updateAutoTransfer(Long autoTransferId, Object requestBody) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-update");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint.replace("{autoTransferId}", autoTransferId.toString());
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] 자동이체 수정");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 데이터: {}", requestBody);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, createJsonHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] 자동이체 수정 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] 자동이체 수정 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 자동이체 수정 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 자동이체 수정 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    /**
     * 자동이체 삭제
     */
    public Map<String, Object> deleteAutoTransfer(Long autoTransferId) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-delete");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint.replace("{autoTransferId}", autoTransferId.toString());
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] 자동이체 삭제");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 파라미터: autoTransferId={}", autoTransferId);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(createJsonHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] 자동이체 삭제 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] 자동이체 삭제 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 자동이체 삭제 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 자동이체 삭제 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    /**
     * 상품별 자동이체 납입 정보 조회
     */
    public Map<String, Object> getIncomingAutoTransfers(String toAccountNumber) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-incoming");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint + "?toAccountNumber=" + toAccountNumber;
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] 상품별 자동이체 납입 정보 조회");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 파라미터: toAccountNumber={}", toAccountNumber);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                new HttpEntity<>(createJsonHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] 상품별 자동이체 납입 정보 조회 성공");
                log.info("-----------------------------------------------------");
                log.info("📥 응답 데이터: {}", responseBody.get("data"));
                log.info("💬 메시지: {}", responseBody.get("message"));
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] 상품별 자동이체 납입 정보 조회 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 상품별 자동이체 납입 정보 조회 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 상품별 자동이체 납입 정보 조회 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    /**
     * 공통 API 호출 메서드
     */
    private <T> T callApi(String endpoint, Object requestBody, Class<T> responseType) {
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("-----------------------------------------------------");
        log.info("🔗 [MARRY1Q-BE → HANA-BANK] API 요청");
        log.info("-----------------------------------------------------");
        log.info("🎯 URL: {}", fullUrl);
        log.info("📤 요청 데이터: {}", requestBody);
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, createJsonHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("-----------------------------------------------------");
                log.info("✅ [HANA-BANK → MARRY1Q-BE] API 응답 성공");
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
                
                return convertedData;
            } else {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.error("-----------------------------------------------------");
                log.error("❌ [HANA-BANK → MARRY1Q-BE] API 응답 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 URL: {}", fullUrl);
                log.error("💬 에러 메시지: {}", errorMessage);
                log.error("📥 전체 응답: {}", responseBody);
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 API 호출 실패: " + errorMessage);
            }
            
        } catch (Exception e) {
            log.error("하나은행 API 호출 중 예외 발생 - URL: {}, Error: {}", fullUrl, e.getMessage(), e);
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "하나은행 서버가 다운되었습니다.");
        }
    }
    
    
    /**
     * JSON 헤더 생성
     */
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
