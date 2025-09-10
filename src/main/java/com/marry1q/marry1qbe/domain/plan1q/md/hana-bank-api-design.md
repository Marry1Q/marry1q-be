# 하나은행 백엔드 API 설계 및 Gemini AI 포트폴리오 추천 흐름

## 1. 하나은행 백엔드 API 설계

### 1.1 상품 조회 API

#### GET /api/v1/products
**목적**: 모든 금융상품 조회

**Query Parameters**:
- `category` (optional): 상품 카테고리 (DEPOSIT, SAVINGS, FUND, BOND, IRP)
- `riskLevel` (optional): 위험도 (LOW, MEDIUM, HIGH)
- `productType` (optional): 상품 유형
- `minAmount` (optional): 최소 투자금액
- `maxAmount` (optional): 최대 투자금액
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

**Response**:
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "productId": 1,
        "externalProductId": "HANA_SAVINGS_001",
        "productCode": "SAV001",
        "productName": "하나 프리미엄 적금",
        "productSubName": "신혼부부 특별",
        "category": {
          "categoryId": 2,
          "categoryCode": "SAVINGS",
          "categoryName": "적금"
        },
        "productType": "SAVINGS",
        "baseRate": 3.2,
        "maxRate": 4.1,
        "expectedReturnRate": 3.65,
        "minInvestmentAmount": 100000,
        "maxInvestmentAmount": 1000000,
        "monthlyInvestmentAmount": 500000,
        "minPeriodMonths": 12,
        "maxPeriodMonths": 60,
        "maturityPeriodMonths": 36,
        "riskLevel": "LOW",
        "riskScore": 2,
        "isTaxFree": true,
        "isGuaranteed": true,
        "productDescription": "신혼부부를 위한 특별 적금 상품으로 비과세 혜택 제공"
      }
    ],
    "totalElements": 18,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

#### GET /api/v1/products/{productId}
**목적**: 특정 상품 상세 조회

**Response**:
```json
{
  "success": true,
  "data": {
    "productId": 1,
    "externalProductId": "HANA_SAVINGS_001",
    "productCode": "SAV001",
    "productName": "하나 프리미엄 적금",
    "productSubName": "신혼부부 특별",
    "category": {
      "categoryId": 2,
      "categoryCode": "SAVINGS",
      "categoryName": "적금"
    },
    "productType": "SAVINGS",
    "baseRate": 3.2,
    "maxRate": 4.1,
    "expectedReturnRate": 3.65,
    "actualReturnRate": 3.62,
    "minInvestmentAmount": 100000,
    "maxInvestmentAmount": 1000000,
    "monthlyInvestmentAmount": 500000,
    "minPeriodMonths": 12,
    "maxPeriodMonths": 60,
    "maturityPeriodMonths": 36,
    "riskLevel": "LOW",
    "riskScore": 2,
    "volatilityLevel": "LOW",
    "isTaxFree": true,
    "isGuaranteed": true,
    "isCompoundInterest": true,
    "isMonthlyInterest": false,
    "productStatus": "ACTIVE",
    "salesStartDate": "2024-01-01",
    "salesEndDate": "2024-12-31",
    "productDescription": "신혼부부를 위한 특별 적금 상품으로 비과세 혜택 제공",
    "productFeatures": "비과세 혜택, 복리 효과, 안정적인 수익",
    "investmentStrategy": "정기 적금을 통한 안정적인 자산 형성"
  }
}
```

#### GET /api/v1/categories
**목적**: 상품 카테고리 조회

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "categoryId": 1,
      "categoryCode": "DEPOSIT",
      "categoryName": "예금",
      "categoryDescription": "원금 보장형 예금 상품",
      "riskLevel": "LOW"
    }
  ]
}
```

## 2. Gemini AI 포트폴리오 추천 흐름

### 2.1 전체 흐름도

```
1. 사용자 Plan1Q 목표 생성
   ↓
2. 투자성향 검사 결과 조회
   ↓
3. 하나은행 상품 정보 조회 API 호출
   ↓
4. Gemini AI 포트폴리오 추천 요청
   ↓
5. 추천 결과를 Plan1Q 목표에 저장
   ↓
6. 사용자에게 추천 포트폴리오 제공
```

### 2.2 상세 흐름

#### Phase 1: 데이터 수집
1. **Plan1Q 목표 정보 수집**
   - 목표 금액 (targetAmount)
   - 목표 기간 (targetPeriod)
   - 목표 제목 (goalTitle)

2. **투자성향 정보 조회**
   - 투자성향 유형 (profileType)
   - 위험도 (riskLevel)
   - 투자성향 점수 (score)

3. **하나은행 상품 정보 조회**
   - 모든 활성 상품 목록
   - 상품별 상세 정보 (수익률, 위험도, 투자 조건 등)

#### Phase 2: Gemini AI 추천 요청

**AI 요청 데이터 구조**:
```json
{
  "userProfile": {
    "investmentProfileType": "CONSERVATIVE",
    "riskLevel": "LOW",
    "riskScore": 3
  },
  "goal": {
    "targetAmount": 50000000,
    "targetPeriodMonths": 36,
    "goalTitle": "첫집마련"
  },
  "availableProducts": [
    {
      "productId": 1,
      "productName": "하나 프리미엄 적금",
      "productType": "SAVINGS",
      "expectedReturnRate": 3.65,
      "riskLevel": "LOW",
      "riskScore": 2,
      "minInvestmentAmount": 100000,
      "maxInvestmentAmount": 1000000,
      "monthlyInvestmentAmount": 500000,
      "isTaxFree": true,
      "productDescription": "신혼부부를 위한 특별 적금 상품"
    }
  ]
}
```

**AI 응답 데이터 구조**:
```json
{
  "recommendedPortfolio": {
    "totalExpectedReturn": 4.2,
    "totalRiskScore": 3,
    "riskAssessment": "낮음",
    "aiExplanation": "사용자의 보수적 투자성향과 첫집마련 목표를 고려하여 안정적인 적금과 채권 중심의 포트폴리오를 추천합니다.",
    "recommendedProducts": [
      {
        "productId": 1,
        "productName": "하나 프리미엄 적금",
        "investmentRatio": 60.0,
        "investmentAmount": 30000000,
        "monthlyAmount": 833333,
        "recommendationReason": "안정적인 수익과 비과세 혜택으로 목표 달성에 적합"
      },
      {
        "productId": 11,
        "productName": "하나 국채 펀드",
        "investmentRatio": 25.0,
        "investmentAmount": 12500000,
        "monthlyAmount": 347222,
        "recommendationReason": "국가채권으로 원금 보장과 안정적 수익 제공"
      },
      {
        "productId": 6,
        "productName": "하나 글로벌 성장 펀드",
        "investmentRatio": 15.0,
        "investmentAmount": 7500000,
        "monthlyAmount": 208333,
        "recommendationReason": "성장 가능성을 통한 수익률 향상 기회 제공"
      }
    ]
  }
}
```

### 2.3 Gemini AI 프롬프트 설계

```
당신은 전문적인 금융 포트폴리오 설계자입니다. 
사용자의 투자성향, 목표, 그리고 사용 가능한 금융상품 정보를 바탕으로 
최적의 포트폴리오를 추천해주세요.

[사용자 정보]
- 투자성향: {investmentProfileType}
- 위험도: {riskLevel} (점수: {riskScore}/10)
- 목표: {goalTitle}
- 목표 금액: {targetAmount}원
- 목표 기간: {targetPeriodMonths}개월

[사용 가능한 상품 목록]
{availableProducts}

[추천 기준]
1. 사용자의 투자성향과 위험도에 맞는 상품 선택
2. 목표 금액과 기간을 고려한 현실적인 투자 계획
3. 상품 간 위험 분산을 통한 포트폴리오 최적화
4. 목표 달성 가능성을 높이는 상품 조합

[응답 형식]
JSON 형태로 다음 정보를 포함하여 응답해주세요:
- 총 예상 수익률
- 총 위험도 점수
- 위험도 평가 (낮음/보통/높음)
- AI 설명
- 추천 상품 목록 (각 상품별 투자 비율, 금액, 월 투자액, 추천 이유)
```

## 3. marry1q 백엔드 구현 계획

### 3.1 새로운 서비스 클래스

```java
// 하나은행 API 연동 서비스
@Service
public class HanaBankProductService {
    public List<ProductInfo> getAllProducts();
    public ProductInfo getProductById(Long productId);
    public List<ProductInfo> getProductsByCategory(String category);
}

// Gemini AI 연동 서비스
@Service
public class GeminiAIService {
    public PortfolioRecommendation generatePortfolioRecommendation(
        InvestmentProfile profile, 
        Plan1QGoal goal, 
        List<ProductInfo> availableProducts
    );
}

// AI 추천 결과 저장 서비스
@Service
public class AIRecommendationService {
    public void saveRecommendation(Long goalId, PortfolioRecommendation recommendation);
    public PortfolioRecommendation getRecommendationByGoalId(Long goalId);
}
```

### 3.2 Plan1Q 목표 생성 API 개선

```java
@PostMapping("/plan1q/goals")
public ResponseEntity<Plan1QGoalResponse> createGoal(@RequestBody Plan1QGoalRequest request) {
    // 1. 기존 목표 생성 로직
    Plan1QGoal goal = plan1QGoalService.createGoal(request);
    
    // 2. 투자성향 정보 조회
    InvestmentProfile profile = investmentProfileService.getProfileByUserId(request.getUserId());
    
    // 3. 하나은행 상품 정보 조회
    List<ProductInfo> products = hanaBankProductService.getAllProducts();
    
    // 4. Gemini AI 포트폴리오 추천
    PortfolioRecommendation recommendation = geminiAIService.generatePortfolioRecommendation(
        profile, goal, products
    );
    
    // 5. 추천 결과 저장
    aiRecommendationService.saveRecommendation(goal.getId(), recommendation);
    
    // 6. 추천 결과를 포함한 응답 반환
    return ResponseEntity.ok(Plan1QGoalResponse.builder()
        .goal(goal)
        .portfolioRecommendation(recommendation)
        .build());
}
```

## 4. 구현 우선순위

### Phase 1 (1주차): 하나은행 백엔드 API
1. 상품 엔티티 및 레포지토리 구현
2. 상품 조회 API 구현
3. API 테스트 및 검증

### Phase 2 (2주차): marry1q 백엔드 연동
1. 하나은행 API 클라이언트 구현
2. Gemini AI 서비스 구현
3. AI 추천 결과 저장 로직 구현

### Phase 3 (3주차): 통합 및 테스트
1. 전체 플로우 통합 테스트
2. AI 프롬프트 최적화
3. 성능 최적화 및 에러 처리

## 5. 에러 처리 및 대안 방안

### 5.1 하나은행 API 장애 시
- 캐시된 상품 정보 사용
- 기본 포트폴리오 추천 제공

### 5.2 Gemini AI 장애 시
- 규칙 기반 포트폴리오 추천으로 대체
- 투자성향과 위험도 기반 기본 추천

### 5.3 투자성향 검사 미완료 시
- 검사 완료 후 진행 안내
- 기본 투자성향으로 임시 추천
