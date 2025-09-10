package com.marry1q.marry1qbe.grobal.openBankingToken;

import com.marry1q.marry1qbe.grobal.openBankingToken.entity.OpenBankingToken;
import com.marry1q.marry1qbe.grobal.openBankingToken.repository.OpenBankingTokenRepository;
import com.marry1q.marry1qbe.grobal.openBankingToken.dto.TokenResponse;
import com.marry1q.marry1qbe.grobal.config.ExternalApiConfig;
import com.marry1q.marry1qbe.grobal.dto.ExternalApiResponse;
import com.marry1q.marry1qbe.grobal.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpenBankingTokenService {
    
    private final OpenBankingTokenRepository openBankingTokenRepository;
    private final ExternalApiConfig externalApiConfig;
    private final RestTemplate restTemplate;
    
    /**
     * 유효한 토큰 조회
     */
    @Transactional(readOnly = true)
    public Optional<String> getValidToken() {
        LocalDateTime now = LocalDateTime.now();
        Optional<OpenBankingToken> token = openBankingTokenRepository.findLatestValidToken(now);
        
        if (token.isPresent()) {
            log.debug("유효한 오픈뱅킹 토큰 조회 성공 - 만료시간: {}", token.get().getExpiresAt());
            return Optional.of(token.get().getAccessToken());
        } else {
            log.debug("유효한 오픈뱅킹 토큰이 없습니다.");
            return Optional.empty();
        }
    }
    
    /**
     * 토큰 발급 및 저장
     */
    public String issueAndSaveToken() {
        try {
            log.info("=== 오픈뱅킹 토큰 발급 프로세스 시작 ===");
            
            ExternalApiResponse<TokenResponse> response = issueToken();
            
            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.error("토큰 발급 응답이 유효하지 않습니다: {}", response);
                throw new RuntimeException("토큰 발급 응답이 유효하지 않습니다");
            }
            
            TokenResponse tokenResponse = response.getData();
            log.info("토큰 발급 성공 - accessToken: {}", 
                    tokenResponse.getAuthAccessToken().substring(0, Math.min(20, tokenResponse.getAuthAccessToken().length())) + "...");
            
            OpenBankingToken token = convertToOpenBankingToken(tokenResponse);
            
            log.info("토큰 저장 시작 - tokenId: {}, expiresAt: {}", token.getTokenId(), token.getExpiresAt());
            
            saveToken(token);
            
            log.info("=== 오픈뱅킹 토큰 발급 및 저장 완료 ===");
            
            return token.getAccessToken();
            
        } catch (Exception e) {
            log.error("토큰 발급 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("오픈뱅킹 토큰 발급에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 토큰 발급 요청
     */
    private ExternalApiResponse<TokenResponse> issueToken() {
        String endpoint = externalApiConfig.getAuthBackend().getEndpoints().get("token");
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("clientId", externalApiConfig.getAuthBackend().getClientId());
        requestBody.put("clientSecret", externalApiConfig.getAuthBackend().getClientSecret());
        requestBody.put("scope", "sa");
        requestBody.put("grantType", "client_credentials");
        
        String baseUrl = externalApiConfig.getAuthBackend().getUrl();
        String fullUrl = baseUrl + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        log.info("오픈뱅킹 토큰 발급 요청 - URL: {}", fullUrl);
        
        try {
            ResponseEntity<ExternalApiResponse<TokenResponse>> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ExternalApiResponse<TokenResponse>>() {}
            );
            
            ExternalApiResponse<TokenResponse> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.isSuccess()) {
                log.info("오픈뱅킹 토큰 발급 성공");
                return responseBody;
            } else {
                String errorMessage = responseBody != null ? responseBody.getMessage() : "Unknown error";
                log.error("오픈뱅킹 토큰 발급 실패 - Error: {}", errorMessage);
                throw new ExternalApiException("AuthBackend", "오픈뱅킹 토큰 발급 실패", errorMessage, response.getStatusCodeValue());
            }
            
        } catch (Exception e) {
            log.error("오픈뱅킹 토큰 발급 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new ExternalApiException("AuthBackend", "오픈뱅킹 토큰 발급 중 오류 발생", e.getMessage());
        }
    }
    
    /**
     * TokenResponse를 OpenBankingToken으로 변환
     */
    private OpenBankingToken convertToOpenBankingToken(TokenResponse tokenResponse) {
        try {
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusSeconds(tokenResponse.getExpiresIn());
            
            log.info("토큰 변환 완료 - accessToken: {}, expiresIn: {}, scope: {}", 
                    tokenResponse.getAuthAccessToken().substring(0, Math.min(20, tokenResponse.getAuthAccessToken().length())) + "...", 
                    tokenResponse.getExpiresIn(), tokenResponse.getScope());
            
            return OpenBankingToken.builder()
                .accessToken(tokenResponse.getAuthAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .scope(tokenResponse.getScope())
                .clientUseCode(tokenResponse.getClientUseCode())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
                
        } catch (Exception e) {
            log.error("토큰 변환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 변환에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 토큰 저장
     */
    private void saveToken(OpenBankingToken token) {
        try {
            Optional<OpenBankingToken> existingToken = openBankingTokenRepository.findLatestToken();
            
            if (existingToken.isPresent()) {
                OpenBankingToken updatedToken = existingToken.get();
                updatedToken = OpenBankingToken.builder()
                        .tokenId(updatedToken.getTokenId())
                        .accessToken(token.getAccessToken())
                        .tokenType(token.getTokenType())
                        .expiresIn(token.getExpiresIn())
                        .scope(token.getScope())
                        .clientUseCode(token.getClientUseCode())
                        .issuedAt(token.getIssuedAt())
                        .expiresAt(token.getExpiresAt())
                        .createdAt(updatedToken.getCreatedAt())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                OpenBankingToken savedToken = openBankingTokenRepository.save(updatedToken);
                log.info("오픈뱅킹 토큰 업데이트 완료 - tokenId: {}, 만료시간: {}", 
                        savedToken.getTokenId(), savedToken.getExpiresAt());
            } else {
                OpenBankingToken savedToken = openBankingTokenRepository.save(token);
                log.info("오픈뱅킹 토큰 저장 완료 - tokenId: {}, 만료시간: {}", 
                        savedToken.getTokenId(), savedToken.getExpiresAt());
            }
        } catch (Exception e) {
            log.error("토큰 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 저장에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 토큰 만료 확인
     */
    @Transactional(readOnly = true)
    public boolean isTokenExpired(String token) {
        LocalDateTime now = LocalDateTime.now();
        Optional<OpenBankingToken> tokenEntity = openBankingTokenRepository.findLatestToken();
        
        if (tokenEntity.isPresent()) {
            return tokenEntity.get().getExpiresAt().isBefore(now);
        }
        return true; // 토큰이 없으면 만료된 것으로 간주
    }
    
    /**
     * 만료된 토큰 정리
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        openBankingTokenRepository.deleteExpiredTokens(now);
        log.debug("만료된 오픈뱅킹 토큰 정리 완료");
    }
    
    /**
     * 토큰 정보 조회 (디버깅용)
     */
    @Transactional(readOnly = true)
    public Optional<OpenBankingToken> getLatestToken() {
        return openBankingTokenRepository.findLatestToken();
    }
}
