package com.marry1q.marry1qbe.domain.plan1q.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.GeminiAIRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.GeminiAIResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.request.Plan1QRecommendationRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.HanaBankProductResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.PortfolioRecommendationResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import com.marry1q.marry1qbe.grobal.config.ExternalApiConfig;
import com.marry1q.marry1qbe.grobal.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {
    
    private final RestTemplate restTemplate;
    private final ExternalApiConfig externalApiConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * Gemini AI 포트폴리오 추천 요청
     */
    public PortfolioRecommendationResponse generatePortfolioRecommendation(
            InvestmentProfile profile, 
            Plan1QRecommendationRequest request, 
            List<HanaBankProductResponse> availableProducts) {
        
        log.info("-----------------------------------------------------");
        log.info("🤖 [MARRY1Q-BE → GEMINI-AI] 포트폴리오 추천 요청");
        log.info("-----------------------------------------------------");
        log.info("👤 사용자: {}", profile.getUserSeqNo());
        log.info("🎯 목표: {} ({}원, {}개월)", request.getGoalTitle(), request.getTargetAmount(), request.getTargetPeriod());
        log.info("📊 투자성향: {} (점수: {})", profile.getProfileType(), profile.getScore());
        log.info("🏦 사용 가능한 상품 수: {}", availableProducts.size());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("-----------------------------------------------------");
        
        try {
            // 1. AI 요청 데이터 구성
            GeminiAIRequest geminiRequest = buildGeminiRequest(profile, request, availableProducts);
            
            log.info("📤 AI 요청 데이터 구성 완료");
            log.info("📝 프롬프트 길이: {} 문자", geminiRequest.getContents().get(0).getParts().get(0).getText().length());
            
            // 2. Gemini AI API 호출
            String apiUrl = externalApiConfig.getGeminiAi().getUrl();
            String apiKey = externalApiConfig.getGeminiAi().getApiKey();
            
            log.info("🎯 API URL: {}", apiUrl);
            log.info("🔑 API Key: {}...{}", 
                apiKey.substring(0, Math.min(10, apiKey.length())),
                apiKey.substring(apiKey.length() - 5));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);
            
            HttpEntity<GeminiAIRequest> entity = new HttpEntity<>(geminiRequest, headers);
            
            log.info("🚀 Gemini AI API 호출 시작...");
            ResponseEntity<GeminiAIResponse> response = restTemplate.postForEntity(
                apiUrl, entity, GeminiAIResponse.class);
            
            log.info("📥 Gemini AI API 응답 수신 완료");
            log.info("📊 HTTP 상태 코드: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && 
                response.getBody() != null && 
                response.getBody().getCandidates() != null && 
                !response.getBody().getCandidates().isEmpty()) {
                
                // 3. AI 응답 파싱
                String aiResponse = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
                
                log.info("-----------------------------------------------------");
                log.info("✅ [GEMINI-AI → MARRY1Q-BE] 포트폴리오 추천 성공");
                log.info("-----------------------------------------------------");
                log.info("📝 AI 응답 길이: {} 문자", aiResponse.length());
                log.info("📄 AI 응답 내용: {}", aiResponse);
                log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.info("-----------------------------------------------------");
                
                PortfolioRecommendationResponse recommendation = parseAIResponse(aiResponse);
                
                log.info("📊 추천 결과 요약:");
                log.info("   - 총 예상 수익률: {}%", recommendation.getTotalExpectedReturn());
                log.info("   - 총 위험도 점수: {}", recommendation.getTotalRiskScore());
                log.info("   - 위험도 평가: {}", recommendation.getRiskAssessment());
                log.info("   - 추천 상품 수: {}", recommendation.getRecommendedProducts().size());
                
                return recommendation;
                
            } else {
                log.error("-----------------------------------------------------");
                log.error("❌ [GEMINI-AI → MARRY1Q-BE] 포트폴리오 추천 실패");
                log.error("-----------------------------------------------------");
                log.error("🎯 API URL: {}", apiUrl);
                log.error("📊 HTTP 상태 코드: {}", response.getStatusCode());
                log.error("📥 응답 바디: {}", response.getBody());
                log.error("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
                log.error("-----------------------------------------------------");
                throw new ExternalApiException("GeminiAI", "Gemini AI 포트폴리오 추천 실패", "AI 응답이 올바르지 않습니다.");
            }
            
        } catch (Exception e) {
            log.error("-----------------------------------------------------");
            log.error("❌ [MARRY1Q-BE → GEMINI-AI] 포트폴리오 추천 중 예외 발생");
            log.error("-----------------------------------------------------");
            log.error("👤 사용자: {}", profile.getUserSeqNo());
            log.error("🎯 목표: {}", request.getGoalTitle());
            log.error("💬 에러 메시지: {}", e.getMessage());
            log.error("⏰ 에러 시간: {}", java.time.LocalDateTime.now());
            log.error("-----------------------------------------------------");
            throw new ExternalApiException("GeminiAI", "Gemini AI 포트폴리오 추천 중 오류 발생", e.getMessage());
        }
    }
    
    /**
     * Gemini AI 요청 데이터 구성
     */
    private GeminiAIRequest buildGeminiRequest(
            InvestmentProfile profile, 
            Plan1QRecommendationRequest request, 
            List<HanaBankProductResponse> availableProducts) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문적인 금융 포트폴리오 설계자입니다. ");
        prompt.append("사용자의 투자성향, 목표, 그리고 사용 가능한 금융상품 정보를 바탕으로 ");
        prompt.append("최적의 포트폴리오를 추천해주세요.\n");
        prompt.append("⚠️ 매우 중요한 제약사항: 제공된 상품 정보의 분류(productType)를 절대 변경하지 마세요!\n\n");
        
        prompt.append("[사용자 정보]\n");
        prompt.append("- 투자성향: ").append(profile.getProfileType()).append("\n");
        prompt.append("- 위험도 점수: ").append(profile.getScore()).append("/100\n");
        prompt.append("- 목표: ").append(request.getGoalTitle()).append("\n");
        prompt.append("- 목표 금액: ").append(request.getTargetAmount()).append("원\n");
        prompt.append("- 목표 기간: ").append(request.getTargetPeriod()).append("개월\n\n");
        
        prompt.append("[사용 가능한 상품 목록]\n");
        prompt.append("⚠️ 각 상품의 분류(productType)는 절대 변경하지 마세요!\n\n");
        for (HanaBankProductResponse product : availableProducts) {
            prompt.append("- 상품ID: ").append(product.getProductId()).append("\n");
            prompt.append("  상품명: ").append(product.getProductName()).append("\n");
            prompt.append("  상품타입: ").append(product.getProductType()).append(" (이 분류를 그대로 유지해야 함)\n");
            prompt.append("  예상 수익률: ").append(product.getExpectedReturnRate()).append("%\n");
            prompt.append("  위험도: ").append(product.getRiskLevel()).append("\n");
            prompt.append("  최소 투자금액: ").append(product.getMinInvestmentAmount()).append("원\n");
            prompt.append("  최대 투자금액: ").append(product.getMaxInvestmentAmount()).append("원\n");
            prompt.append("  상품 설명: ").append(product.getProductDescription()).append("\n\n");
        }
        
        prompt.append("[추천 기준]\n");
        prompt.append("1. 사용자의 투자성향과 위험도에 맞는 상품 선택\n");
        prompt.append("2. 목표 금액과 기간을 고려한 현실적인 투자 계획\n");
        prompt.append("3. 상품 간 위험 분산을 통한 포트폴리오 최적화\n");
        prompt.append("4. 목표 달성 가능성을 높이는 상품 조합\n");
        prompt.append("5. 제공된 상품 정보의 분류를 그대로 유지 (상품타입 변경 금지)\n");
        prompt.append("6. 월 납입금은 투자금액을 목표기간으로 나누고 소수점 첫째자리에서 반올림하여 계산\n\n");
        
        prompt.append("[중요한 제약사항]\n");
        prompt.append("- 제공된 상품 정보의 상품타입(productType)을 절대 변경하지 마세요\n");
        prompt.append("- 적금 상품은 반드시 적금(SAVINGS) 분류로 유지\n");
        prompt.append("- 예금 상품은 반드시 예금(DEPOSIT) 분류로 유지\n");
        prompt.append("- 펀드 상품은 반드시 펀드(FUND) 분류로 유지\n");
        prompt.append("- 채권 상품은 반드시 채권(BOND) 분류로 유지\n");
        prompt.append("- 상품의 원래 분류를 임의로 변경하지 말고 제공된 정보만을 사용하세요\n");
        prompt.append("- 상품 분류는 대문자로 응답해주세요 (SAVINGS, FUND, DEPOSIT, BOND)\n");
        prompt.append("- 월 납입금(monthlyAmount)은 반드시 정수로 계산하고 소수점 첫째자리에서 반올림하세요\n");
        prompt.append("- 제공된 상품의 예상 수익률(expectedReturnRate)을 그대로 반환해주세요\n");
        prompt.append("- 상품 정보를 임의로 변경하지 말고 제공된 정보만을 사용하세요\n\n");
        
        prompt.append("[응답 형식]\n");
        prompt.append("JSON 형태로 다음 정보를 포함하여 응답해주세요:\n");
        prompt.append("- totalExpectedReturn: 총 예상 수익률 (연간 수익률 %)\n");
        prompt.append("- achievementProbability: 목표 달성 가능성 (0-100 사이의 정수)\n");
        prompt.append("- totalRiskScore: 총 위험도 점수 (1-10 사이의 정수)\n");
        prompt.append("- riskAssessment: 위험도 평가 (낮음/보통/높음)\n");
        prompt.append("- aiExplanation: AI 설명 (목표 달성 가능성과 포트폴리오 특징에 대한 설명)\n");
        prompt.append("- recommendedProducts: 추천 상품 목록\n");
        prompt.append("  * 각 상품의 productId, productName, productType은 제공된 정보와 동일하게 유지\n");
        prompt.append("  * ⚠️ 상품 분류(productType)를 절대 변경하지 마세요 - 원본 그대로 사용\n");
        prompt.append("  * 각 상품별 투자 비율, 금액, 월 투자액, 추천 이유 포함\n");
        prompt.append("  * ⚠️ 추천 이유는 반드시 'reason' 필드명으로 응답해주세요\n");
        prompt.append("  * ⚠️ 월 납입금(monthlyAmount)은 반드시 소수점 첫째자리에서 반올림하여 정수로 계산해주세요\n");
        prompt.append("  * ⚠️ 상품 분류는 대문자로 응답해주세요 (SAVINGS, FUND, DEPOSIT, BOND)\n");
        prompt.append("  * 예시: 적금 상품은 반드시 SAVINGS, 펀드 상품은 반드시 FUND로 응답\n");
        prompt.append("  * 예시 JSON 구조:\n");
        prompt.append("    {\n");
        prompt.append("      \"productId\": 1,\n");
        prompt.append("      \"productName\": \"하나 프리미엄 적금\",\n");
        prompt.append("      \"productType\": \"SAVINGS\",\n");
        prompt.append("      \"investmentRatio\": 60.0,\n");
        prompt.append("      \"investmentAmount\": 30000000,\n");
        prompt.append("      \"monthlyAmount\": 833333,\n");
        prompt.append("      \"expectedReturnRate\": 3.2,\n");
        prompt.append("      \"reason\": \"안정적인 수익과 비과세 혜택으로 목표 달성에 적합\"\n");
        prompt.append("    }\n");
        
        return GeminiAIRequest.builder()
            .contents(List.of(GeminiAIRequest.Content.builder()
                .parts(List.of(GeminiAIRequest.Part.builder()
                    .text(prompt.toString())
                    .build()))
                .build()))
            .build();
    }
    
    /**
     * AI 응답 파싱
     */
    private PortfolioRecommendationResponse parseAIResponse(String aiResponse) {
        try {
            log.info("�� AI 응답 파싱 시작...");
            log.info("📄 원본 AI 응답 길이: {} 문자", aiResponse.length());
            
            // AI 응답에서 JSON 부분만 추출 (마크다운 코드 블록 제거)
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            // JSON 응답에서 필요한 부분만 추출
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            
            log.info("📊 파싱된 JSON 구조: {}", jsonNode.toPrettyString());
            
            // 필수 필드 검증
            validateRequiredFields(jsonNode);
            
            // 필드별 상세 로깅
            Double totalExpectedReturn = getDoubleValue(jsonNode, "totalExpectedReturn");
            Integer achievementProbability = getIntValue(jsonNode, "achievementProbability");
            Integer totalRiskScore = getIntValue(jsonNode, "totalRiskScore");
            String riskAssessment = getStringValue(jsonNode, "riskAssessment");
            String aiExplanation = getStringValue(jsonNode, "aiExplanation");
            
            log.info("🔍 AI 응답 필드 파싱 결과:");
            log.info("   - totalExpectedReturn: {} (원본: {})", totalExpectedReturn, jsonNode.get("totalExpectedReturn"));
            log.info("   - achievementProbability: {} (원본: {})", achievementProbability, jsonNode.get("achievementProbability"));
            log.info("   - totalRiskScore: {} (원본: {})", totalRiskScore, jsonNode.get("totalRiskScore"));
            log.info("   - riskAssessment: '{}' (원본: {})", riskAssessment, jsonNode.get("riskAssessment"));
            log.info("   - aiExplanation: '{}' (원본: {})", aiExplanation, jsonNode.get("aiExplanation"));
            
            PortfolioRecommendationResponse recommendation = PortfolioRecommendationResponse.builder()
                .totalExpectedReturn(totalExpectedReturn)
                .achievementProbability(achievementProbability)
                .totalRiskScore(totalRiskScore)
                .riskAssessment(riskAssessment)
                .aiExplanation(aiExplanation)
                .recommendedProducts(parseRecommendedProducts(jsonNode.get("recommendedProducts")))
                .build();
            
            log.info("✅ AI 응답 파싱 완료");
            return recommendation;
            
        } catch (Exception e) {
            log.error("❌ AI 응답 파싱 중 오류 발생: {}", e.getMessage());
            log.error("📄 원본 AI 응답: {}", aiResponse);
            throw new ExternalApiException("GeminiAI", "AI 응답 파싱에 실패했습니다.", e.getMessage());
        }
    }
    
    /**
     * AI 응답에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String aiResponse) {
        // 마크다운 코드 블록 제거
        if (aiResponse.contains("```json")) {
            int startIndex = aiResponse.indexOf("```json") + 7;
            int endIndex = aiResponse.lastIndexOf("```");
            if (endIndex > startIndex) {
                return aiResponse.substring(startIndex, endIndex).trim();
            }
        } else if (aiResponse.contains("```")) {
            int startIndex = aiResponse.indexOf("```") + 3;
            int endIndex = aiResponse.lastIndexOf("```");
            if (endIndex > startIndex) {
                return aiResponse.substring(startIndex, endIndex).trim();
            }
        }
        
        return aiResponse.trim();
    }
    
    /**
     * 필수 필드 검증
     */
    private void validateRequiredFields(JsonNode jsonNode) {
        String[] requiredFields = {"totalExpectedReturn", "achievementProbability", "totalRiskScore", "riskAssessment", "aiExplanation", "recommendedProducts"};
        
        for (String field : requiredFields) {
            if (!jsonNode.has(field) || jsonNode.get(field).isNull()) {
                throw new IllegalArgumentException("필수 필드가 누락되었습니다: " + field);
            }
        }
        
        if (!jsonNode.get("recommendedProducts").isArray() || jsonNode.get("recommendedProducts").size() == 0) {
            throw new IllegalArgumentException("추천 상품 목록이 비어있습니다.");
        }
    }
    
    /**
     * 안전한 Double 값 추출
     */
    private Double getDoubleValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node == null || node.isNull()) {
            return 0.0;
        }
        return node.asDouble();
    }
    
    /**
     * 안전한 Integer 값 추출
     */
    private Integer getIntValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node == null || node.isNull()) {
            return 0;
        }
        return node.asInt();
    }
    
    /**
     * 안전한 String 값 추출
     */
    private String getStringValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node == null || node.isNull()) {
            return "";
        }
        return node.asText();
    }
    
    private List<PortfolioRecommendationResponse.RecommendedProduct> parseRecommendedProducts(JsonNode productsNode) {
        List<PortfolioRecommendationResponse.RecommendedProduct> products = new java.util.ArrayList<>();
        
        for (JsonNode productNode : productsNode) {
            try {
                Long productId = getLongValue(productNode, "productId");
                String productName = getStringValue(productNode, "productName");
                String productType = getStringValue(productNode, "productType");
                
                // 상품 분류 검증 로그
                log.info("🔍 상품 분류 검증 - 상품ID: {}, 상품명: {}, 분류: {}", productId, productName, productType);
                
                // 상품 분류 유효성 검증
                validateProductType(productId, productName, productType);
                
                // reason 또는 recommendationReason 필드에서 추천 이유 가져오기
                String recommendationReason = getStringValue(productNode, "reason");
                if (recommendationReason.isEmpty()) {
                    recommendationReason = getStringValue(productNode, "recommendationReason");
                }
                
                // 추천 이유 파싱 로그
                log.info("🔍 추천 이유 파싱 - 상품ID: {}, 상품명: {}, reason: '{}', recommendationReason: '{}', 최종: '{}'", 
                    productId, productName, 
                    getStringValue(productNode, "reason"), 
                    getStringValue(productNode, "recommendationReason"), 
                    recommendationReason);
                
                PortfolioRecommendationResponse.RecommendedProduct product = PortfolioRecommendationResponse.RecommendedProduct.builder()
                    .productId(productId)
                    .productName(productName)
                    .productType(productType) // 상품 분류 정보 포함
                    .investmentRatio(getDoubleValue(productNode, "investmentRatio"))
                    .investmentAmount(getLongValue(productNode, "investmentAmount"))
                    .monthlyAmount(getLongValue(productNode, "monthlyAmount"))
                    .recommendationReason(recommendationReason)
                    .expectedReturnRate(getDoubleValue(productNode, "expectedReturnRate"))
                    .build();
                
                products.add(product);
                
            } catch (Exception e) {
                log.warn("⚠️ 상품 파싱 중 오류 발생, 해당 상품 건너뜀: {}", e.getMessage());
            }
        }
        
        log.info("📦 추천 상품 파싱 완료: {}개 상품", products.size());
        return products;
    }
    
    /**
     * 상품 분류 유효성 검증
     */
    private void validateProductType(Long productId, String productName, String productType) {
        // 유효한 상품 분류 목록 (대문자로 변경)
        List<String> validProductTypes = List.of("DEPOSIT", "SAVINGS", "FUND", "BOND", "ETF");
        
        if (productType == null || productType.trim().isEmpty()) {
            log.warn("⚠️ 상품 분류가 누락됨 - 상품ID: {}, 상품명: {}", productId, productName);
            return;
        }
        
        String normalizedProductType = productType.trim().toUpperCase();
        
        if (!validProductTypes.contains(normalizedProductType)) {
            log.warn("⚠️ 유효하지 않은 상품 분류 - 상품ID: {}, 상품명: {}, 분류: {}", 
                productId, productName, productType);
        }
        
        // 특정 상품명 패턴에 따른 분류 검증
        if (productName != null) {
            String lowerProductName = productName.toLowerCase();
            
            // 적금 상품이 다른 분류로 잘못 분류된 경우 경고
            if (lowerProductName.contains("적금") && !normalizedProductType.equals("SAVINGS")) {
                log.warn("⚠️ 적금 상품이 잘못 분류됨 - 상품ID: {}, 상품명: {}, 현재분류: {}, 예상분류: SAVINGS", 
                    productId, productName, productType);
            }
            
            // 예금 상품이 다른 분류로 잘못 분류된 경우 경고
            if (lowerProductName.contains("예금") && !normalizedProductType.equals("DEPOSIT")) {
                log.warn("⚠️ 예금 상품이 잘못 분류됨 - 상품ID: {}, 상품명: {}, 현재분류: {}, 예상분류: DEPOSIT", 
                    productId, productName, productType);
            }
            
            // 펀드 상품이 다른 분류로 잘못 분류된 경우 경고
            if (lowerProductName.contains("펀드") && !normalizedProductType.equals("FUND")) {
                log.warn("⚠️ 펀드 상품이 잘못 분류됨 - 상품ID: {}, 상품명: {}, 현재분류: {}, 예상분류: FUND", 
                    productId, productName, productType);
            }
        }
    }
    
    /**
     * 안전한 Long 값 추출
     */
    private Long getLongValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node == null || node.isNull()) {
            return 0L;
        }
        return node.asLong();
    }
}
