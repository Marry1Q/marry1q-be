# AI 포트폴리오 추천 기능 개발 계획

## 개요
Plan1Q 목표 생성 시 사용자의 투자성향과 목표 정보를 바탕으로 Gemini AI를 활용하여 맞춤형 포트폴리오를 추천하는 기능을 개발합니다.

## 현재 상황 분석

### 프론트엔드 현황
- `Plan1QDetails` 컴포넌트에서 목표 정보만 수집
- 투자성향 정보가 누락되어 AI 추천에 필요한 데이터 부족
- "포트폴리오 추천받기" 버튼 클릭 시 투자성향 검증 없음

### 백엔드 현황
- Plan1Q 목표 생성 API 존재하지만 AI 추천 로직 없음
- 하나은행 백엔드 연동 미구현
- Gemini AI 연동 미구현

## 개발 계획

### Phase 1: 프론트엔드 즉시 해결 (1순위)

#### 1단계: Plan1QDetails 컴포넌트 수정
**목표**: 투자성향 정보를 포함한 완전한 데이터 수집

**작업 내용**:
- `usePlan1QStore` 훅 추가하여 투자성향 정보 조회
- 컴포넌트 마운트 시 투자성향 정보 자동 조회
- "포트폴리오 추천받기" 버튼 클릭 시 투자성향 검증
- 투자성향 검사 미완료/만료 시 사용자 안내

**예상 소요 시간**: 2-3시간

#### 2단계: 데이터 검증 및 전달 로직
**목표**: 완전한 정보를 백엔드로 전달

**작업 내용**:
- 투자성향 정보 + 목표 정보 통합 검증
- API 요청 시 필요한 모든 데이터 포함
- 에러 처리 및 사용자 피드백 개선

**예상 소요 시간**: 1-2시간

### Phase 2: 백엔드 핵심 서비스 개발 (2순위)

#### 3단계: 하나은행 백엔드 연동 서비스
**목표**: 하나은행 상품 정보 조회

**작업 내용**:
```java
// 개발할 서비스
- HanaBankProductService
  - 상품 목록 조회 API 연동
  - 상품 상세 정보 조회 API 연동
  - 상품 카테고리별 분류 로직
  - 에러 처리 및 재시도 로직
```

**예상 소요 시간**: 4-6시간

#### 4단계: Gemini AI 연동 서비스
**목표**: AI 포트폴리오 추천 엔진

**작업 내용**:
```java
// 개발할 서비스
- GeminiAIService
  - Gemini API 클라이언트 구현
  - 프롬프트 엔지니어링
  - 응답 파싱 및 검증 로직
  - 투자성향 기반 포트폴리오 추천 알고리즘
```

**예상 소요 시간**: 6-8시간

#### 5단계: Plan1Q 목표 생성 API 개선
**목표**: AI 추천 로직을 포함한 통합 API

**작업 내용**:
```java
// 기존 API 개선
- Plan1QGoalService.updateGoalCreationLogic()
  - 투자성향 검사 결과 조회
  - 하나은행 상품 정보 조회
  - Gemini AI 포트폴리오 추천 호출
  - 추천 결과를 포함한 응답 생성
```

**예상 소요 시간**: 3-4시간

### Phase 3: 프론트엔드 완성 (3순위)

#### 6단계: API 호출 로직 구현
**목표**: 백엔드 API와의 완전한 연동

**작업 내용**:
- Plan1Q 목표 생성 API 호출
- AI 추천 포트폴리오 응답 처리
- 로딩 상태 및 에러 처리
- 사용자 피드백 개선

**예상 소요 시간**: 2-3시간

#### 7단계: 포트폴리오 추천 결과 UI
**목표**: 추천 포트폴리오 시각화

**작업 내용**:
- 추천 포트폴리오 UI 컴포넌트 개발
- 상품별 투자 비율 시각화 (차트, 프로그레스 바)
- 상품 가입/해지 기능 연동
- AI 추천 이유 표시

**예상 소요 시간**: 4-6시간

## 기술적 구현 세부사항

### AI 프롬프트 설계
```
입력 데이터:
- 사용자 투자성향 (profileType, riskLevel, score)
- 목표 정보 (targetAmount, targetPeriod, goalTitle)
- 사용 가능한 상품 목록 (하나은행 상품 정보)

출력 형식:
{
  "recommendedProducts": [
    {
      "productId": 1,
      "investmentRatio": 70.0,
      "investmentAmount": 35000000,
      "monthlyAmount": 972222,
      "recommendationReason": "안정적인 수익 추구를 위한 적금 상품"
    }
  ],
  "totalExpectedReturn": 3.5,
  "riskAssessment": "낮음",
  "aiExplanation": "사용자의 보수적 투자성향을 고려하여..."
}
```

### 데이터베이스 스키마 확장
```sql
-- 상품 정보 테이블
CREATE TABLE hana_bank_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    return_rate DECIMAL(5,2),
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AI 추천 결과 테이블
CREATE TABLE ai_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goal_id BIGINT NOT NULL,
    recommendation_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES plan1q_goals(id)
);
```

### 에러 처리 전략
1. **하나은행 API 장애**: 캐시된 상품 정보 사용 또는 기본 추천 제공
2. **Gemini API 장애**: 규칙 기반 포트폴리오 추천으로 대체
3. **투자성향 검사 미완료**: 검사 완료 후 진행 안내

## 성능 최적화 방안

### 캐싱 전략
- 하나은행 상품 정보: 1시간 캐시
- AI 추천 결과: 30분 캐시
- 투자성향 검사 결과: 세션 기간 캐시

### 비동기 처리
- AI 추천 요청을 비동기로 처리
- 사용자에게 즉시 피드백 제공
- 추천 완료 시 실시간 업데이트

## 테스트 계획

### 단위 테스트
- 각 서비스별 독립적 테스트
- AI 프롬프트 검증
- 에러 케이스 테스트

### 통합 테스트
- 하나은행 API 연동 테스트
- Gemini AI 응답 품질 검증
- 전체 플로우 테스트

### 성능 테스트
- API 응답 시간 측정
- 동시 요청 처리 능력 테스트
- 메모리 사용량 모니터링

## 배포 계획

### 1차 배포 (Phase 1 완료)
- 프론트엔드 투자성향 정보 수집 기능
- 기본 데이터 검증 로직

### 2차 배포 (Phase 2 완료)
- 백엔드 AI 추천 서비스
- 하나은행 연동 기능

### 3차 배포 (Phase 3 완료)
- 완전한 AI 포트폴리오 추천 기능
- UI/UX 개선사항

## 예상 개발 기간
- **전체 개발 기간**: 3-4주
- **Phase 1**: 1주
- **Phase 2**: 2주
- **Phase 3**: 1주

## 리스크 및 대응 방안

### 기술적 리스크
1. **Gemini API 응답 품질**: 프롬프트 엔지니어링 반복 개선
2. **하나은행 API 안정성**: 장애 대응 로직 구현
3. **성능 이슈**: 캐싱 및 비동기 처리 최적화

### 비즈니스 리스크
1. **사용자 만족도**: 단계별 배포로 피드백 수집
2. **규제 준수**: 금융 상품 추천 관련 규정 검토
3. **보안**: API 키 관리 및 데이터 암호화

## 성공 지표

### 기술적 지표
- API 응답 시간 < 3초
- AI 추천 정확도 > 80%
- 시스템 가용성 > 99%

### 비즈니스 지표
- 사용자 포트폴리오 가입률 > 60%
- 사용자 만족도 > 4.0/5.0
- 상품 추천 클릭률 > 40%
