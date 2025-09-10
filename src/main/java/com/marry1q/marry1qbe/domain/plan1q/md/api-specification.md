# Plan1Q API 명세서

## 개요
Plan1Q는 하나은행의 커플 맞춤형 투자 상품 서비스로, 사용자의 투자성향을 분석하여 맞춤형 포트폴리오를 추천하고 관리하는 API입니다.

## 기본 정보
- **Base URL**: `/api/plan1q`
- **Content-Type**: `application/json`
- **인증**: JWT Bearer Token
- **응답 형식**: `CustomApiResponse<T>`

## 공통 응답 형식
```json
{
  "success": true,
  "data": {},
  "message": "성공 메시지",
  "error": null,
  "timestamp": "2024-01-01T00:00:00"
}
```

## 1. 투자성향 검사 API

### 1.1 투자성향 검사 제출
**POST** `/investment-profile`

투자성향 검사를 제출하고 결과를 저장합니다.

#### Request Body
```json
{
  "answers": [
    {
      "questionId": 1,
      "answer": "A"
    },
    {
      "questionId": 2,
      "answer": "B"
    }
  ]
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "investmentProfileId": 1,
    "profileType": "conservative",
    "profileTypeName": "안정형",
    "riskLevel": "low",
    "riskLevelName": "낮음",
    "score": 25,
    "expiredAt": "2025-01-01T00:00:00",
    "isExpired": false
  },
  "message": "투자성향 검사가 완료되었습니다."
}
```

### 1.2 투자성향 검사 결과 조회
**GET** `/investment-profile`

현재 사용자의 투자성향 검사 결과를 조회합니다.

#### Response
```json
{
  "success": true,
  "data": {
    "investmentProfileId": 1,
    "profileType": "conservative",
    "profileTypeName": "안정형",
    "riskLevel": "low",
    "riskLevelName": "낮음",
    "score": 25,
    "expiredAt": "2025-01-01T00:00:00",
    "isExpired": false
  },
  "message": "투자성향 검사 결과를 조회했습니다."
}
```

## 2. Plan1Q 목표 관리 API

### 2.1 Plan1Q 목표 생성
**POST** `/goals`

새로운 Plan1Q 목표를 생성합니다.

#### Request Body
```json
{
  "goalTitle": "신혼집 마련",
  "detailedGoal": "3년 내에 신혼집을 마련하고 싶습니다.",
  "targetAmount": 50000000,
  "targetPeriod": 36,
  "priority": "high"
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "goalId": 1,
    "goalName": "신혼집 마련",
    "goalDescription": "3년 내에 신혼집을 마련하고 싶습니다.",
    "targetAmount": 50000000,
    "targetPeriod": 36,
    "monthlyAmount": 1388889,
    "maturityDate": "2027-01-01",
    "status": "in_progress",
    "statusName": "진행중",
    "riskLevel": "low",
    "riskLevelName": "낮음",
    "createdAt": "2024-01-01T00:00:00",
    "recommendedProducts": [
      {
        "productId": 1,
        "productName": "하나은행 안정형 적금",
        "productType": "savings",
        "productTypeName": "적금",
        "investmentRatio": 70.0,
        "investmentAmount": 35000000,
        "returnRate": 3.5,
        "monthlyAmount": 972222,
        "riskLevel": "low",
        "riskLevelName": "낮음"
      }
    ]
  },
  "message": "Plan1Q 목표가 생성되었습니다."
}
```

### 2.2 Plan1Q 목표 목록 조회
**GET** `/goals`

사용자의 Plan1Q 목표 목록을 조회합니다.

#### Query Parameters
- `status` (optional): 목표 상태 필터 (in_progress, completed, paused)
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 10)
- `sort` (optional): 정렬 기준 (createdAt, targetAmount, maturityDate)

#### Response
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "goalId": 1,
        "goalName": "신혼집 마련",
        "targetAmount": 50000000,
        "currentAmount": 15000000,
        "progress": 30.0,
        "targetPeriod": 36,
        "remainingPeriod": 24,
        "status": "in_progress",
        "statusName": "진행중",
        "maturityDate": "2027-01-01",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0,
    "size": 10
  },
  "message": "Plan1Q 목표 목록을 조회했습니다."
}
```

### 2.3 Plan1Q 목표 상세 조회
**GET** `/goals/{goalId}`

특정 Plan1Q 목표의 상세 정보를 조회합니다.

#### Response
```json
{
  "success": true,
  "data": {
    "goalId": 1,
    "goalName": "신혼집 마련",
    "goalDescription": "3년 내에 신혼집을 마련하고 싶습니다.",
    "targetAmount": 50000000,
    "currentAmount": 15000000,
    "progress": 30.0,
    "targetPeriod": 36,
    "remainingPeriod": 24,
    "monthlyAmount": 1388889,
    "maturityDate": "2027-01-01",
    "status": "in_progress",
    "statusName": "진행중",
    "riskLevel": "low",
    "riskLevelName": "낮음",
    "createdAt": "2024-01-01T00:00:00",
    "products": [
      {
        "productId": 1,
        "productName": "하나은행 안정형 적금",
        "productType": "savings",
        "productTypeName": "적금",
        "investmentRatio": 70.0,
        "investmentAmount": 35000000,
        "returnRate": 3.5,
        "monthlyAmount": 972222,
        "subscribed": true,
        "currentValue": 10500000,
        "profit": 350000,
        "contractDate": "2024-01-01",
        "accountNumber": "123-456789",
        "riskLevel": "low",
        "riskLevelName": "낮음"
      }
    ],
    "autoTransfer": {
      "autoTransferId": 1,
      "nextPaymentDate": "2024-02-01",
      "remainingPayments": 24,
      "autoTransferAccount": "123-456789",
      "isPaymentCompleted": false
    }
  },
  "message": "Plan1Q 목표 상세 정보를 조회했습니다."
}
```

### 2.4 Plan1Q 목표 수정
**PUT** `/goals/{goalId}`

Plan1Q 목표 정보를 수정합니다.

#### Request Body
```json
{
  "goalTitle": "신혼집 마련 (수정)",
  "detailedGoal": "3년 내에 신혼집을 마련하고 싶습니다. (수정된 내용)",
  "targetAmount": 60000000,
  "targetPeriod": 48
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "goalId": 1,
    "goalName": "신혼집 마련 (수정)",
    "goalDescription": "3년 내에 신혼집을 마련하고 싶습니다. (수정된 내용)",
    "targetAmount": 60000000,
    "targetPeriod": 48,
    "monthlyAmount": 1250000,
    "maturityDate": "2028-01-01",
    "status": "in_progress",
    "statusName": "진행중"
  },
  "message": "Plan1Q 목표가 수정되었습니다."
}
```

### 2.5 Plan1Q 목표 삭제
**DELETE** `/goals/{goalId}`

Plan1Q 목표를 삭제합니다.

#### Response
```json
{
  "success": true,
  "data": null,
  "message": "Plan1Q 목표가 삭제되었습니다."
}
```

## 3. Plan1Q 상품 관리 API

### 3.1 Plan1Q 상품 가입 (하나은행 백엔드 연동)
**POST** `/goals/{goalId}/products/{productId}/subscribe`

Plan1Q 상품에 가입합니다.

#### Request Body
```json
{
  "accountNumber": "123-456789",
  "agreeToTerms": true
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "productId": 1,
    "productName": "하나은행 안정형 적금",
    "productType": "savings",
    "productTypeName": "적금",
    "investmentRatio": 70.0,
    "investmentAmount": 35000000,
    "returnRate": 3.5,
    "monthlyAmount": 972222,
    "subscribed": true,
    "contractDate": "2024-01-01",
    "accountNumber": "123-456789",
    "hanaBankSubscriptionId": "HANA_SUB_001",
    "riskLevel": "low",
    "riskLevelName": "낮음"
  },
  "message": "상품 가입이 완료되었습니다."
}
```

### 3.2 Plan1Q 상품 해지 (하나은행 백엔드 연동)
**DELETE** `/goals/{goalId}/products/{productId}/subscribe`

Plan1Q 상품을 해지합니다.

#### Response
```json
{
  "success": true,
  "data": null,
  "message": "상품 해지가 완료되었습니다."
}
```

### 3.3 Plan1Q 상품 상세 조회
**GET** `/goals/{goalId}/products/{productId}`

Plan1Q 상품의 상세 정보를 조회합니다.

#### Response
```json
{
  "success": true,
  "data": {
    "productId": 1,
    "productName": "하나은행 안정형 적금",
    "productType": "savings",
    "productTypeName": "적금",
    "investmentRatio": 70.0,
    "investmentAmount": 35000000,
    "returnRate": 3.5,
    "monthlyAmount": 972222,
    "subscribed": true,
    "currentValue": 10500000,
    "profit": 350000,
    "contractDate": "2024-01-01",
    "maturityDate": "2027-01-01",
    "terms": "상품 약관 내용...",
    "contract": "계약서명.pdf",
    "accountNumber": "123-456789",
    "hanaBankSubscriptionId": "HANA_SUB_001",
    "riskLevel": "low",
    "riskLevelName": "낮음",
    "assetClass": "fixed_income",
    "strategy": "안정적인 수익을 위한 투자 전략",
    "interestRate": "3.5%",
    "period": "36개월"
  },
  "message": "Plan1Q 상품 상세 정보를 조회했습니다."
}
```

## 4. 자동이체 관리 API

### 4.1 자동이체 등록 (하나은행 백엔드 연동)
**POST** `/goals/{goalId}/auto-transfer`

자동이체를 등록합니다.

#### Request Body
```json
{
  "autoTransferAccount": "123-456789",
  "transferAmount": 1388889,
  "transferDate": 1,
  "startDate": "2024-02-01"
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "autoTransferId": 1,
    "nextPaymentDate": "2024-02-01",
    "remainingPayments": 36,
    "autoTransferAccount": "123-456789",
    "transferAmount": 1388889,
    "isPaymentCompleted": false,
    "hanaBankAutoTransferId": "HANA_AUTO_001"
  },
  "message": "자동이체가 등록되었습니다."
}
```

### 4.2 자동이체 수정 (하나은행 백엔드 연동)
**PUT** `/goals/{goalId}/auto-transfer`

자동이체 정보를 수정합니다.

#### Request Body
```json
{
  "autoTransferAccount": "123-456789",
  "transferAmount": 1500000,
  "transferDate": 1,
  "startDate": "2024-02-01"
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "autoTransferId": 1,
    "nextPaymentDate": "2024-02-01",
    "remainingPayments": 36,
    "autoTransferAccount": "123-456789",
    "transferAmount": 1500000,
    "isPaymentCompleted": false,
    "hanaBankAutoTransferId": "HANA_AUTO_001"
  },
  "message": "자동이체가 수정되었습니다."
}
```

### 4.3 자동이체 해지 (하나은행 백엔드 연동)
**DELETE** `/goals/{goalId}/auto-transfer`

자동이체를 해지합니다.

#### Response
```json
{
  "success": true,
  "data": null,
  "message": "자동이체가 해지되었습니다."
}
```

## 5. Plan1Q 대시보드 API

### 5.1 Plan1Q 대시보드 통계
**GET** `/dashboard`

Plan1Q 대시보드의 통계 정보를 조회합니다.

#### Response
```json
{
  "success": true,
  "data": {
    "totalGoals": 3,
    "activeGoals": 2,
    "completedGoals": 1,
    "totalInvestment": 150000000,
    "currentValue": 165000000,
    "totalProfit": 15000000,
    "averageReturnRate": 3.2,
    "monthlyAmount": 2500000,
    "investmentProfile": {
      "profileType": "conservative",
      "profileTypeName": "안정형",
      "riskLevel": "low",
      "riskLevelName": "낮음"
    },
    "recentGoals": [
      {
        "goalId": 1,
        "goalName": "신혼집 마련",
        "progress": 30.0,
        "status": "in_progress",
        "statusName": "진행중"
      }
    ],
    "portfolioDistribution": [
      {
        "productType": "savings",
        "productTypeName": "적금",
        "amount": 105000000,
        "ratio": 70.0
      },
      {
        "productType": "fund",
        "productTypeName": "펀드",
        "amount": 45000000,
        "ratio": 30.0
      }
    ]
  },
  "message": "Plan1Q 대시보드 정보를 조회했습니다."
}
```

## 6. Plan1Q 템플릿 API

### 6.1 Plan1Q 템플릿 목록 조회
**GET** `/templates`

Plan1Q 템플릿 목록을 조회합니다.

#### Response
```json
{
  "success": true,
  "data": [
    {
      "templateId": 1,
      "title": "신혼집 마련",
      "description": "신혼집 마련을 위한 맞춤형 투자 계획",
      "iconName": "house_deposit"
    },
    {
      "templateId": 2,
      "title": "결혼식 준비",
      "description": "완벽한 결혼식을 위한 자금 마련 계획",
      "iconName": "wedding_hall"
    }
  ],
  "message": "Plan1Q 템플릿 목록을 조회했습니다."
}
```



## 7. 에러 응답

### 7.1 공통 에러 응답 형식
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "error": {
    "code": "ERROR_CODE",
    "message": "상세 에러 메시지"
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

### 7.2 주요 에러 코드
- `PLAN1Q_GOAL_NOT_FOUND`: Plan1Q 목표를 찾을 수 없습니다.
- `INVESTMENT_PROFILE_NOT_FOUND`: 투자성향 검사 결과를 찾을 수 없습니다.
- `INVESTMENT_PROFILE_EXPIRED`: 투자성향 검사가 만료되었습니다.
- `PLAN1Q_PRODUCT_NOT_FOUND`: Plan1Q 상품을 찾을 수 없습니다.
- `INSUFFICIENT_INVESTMENT_PROFILE`: 투자성향 검사가 필요합니다.
- `FORBIDDEN`: 권한이 없습니다.
- `EXTERNAL_API_ERROR`: 하나은행 백엔드 연동 오류

## 8. 인증 및 권한

### 8.1 인증 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

### 8.2 권한 요구사항
- 모든 API는 JWT 토큰 인증 필요
- 목표 조회/수정/삭제는 해당 커플 멤버만 가능
- 상품 가입/해지는 목표 소유자만 가능

## 9. 페이징 및 정렬

### 9.1 페이징 파라미터
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본값: 10, 최대: 100)

### 9.2 정렬 파라미터
- `sort`: 정렬 기준 (예: `createdAt,desc`, `targetAmount,asc`)

## 10. 하나은행 백엔드 연동

### 10.1 연동 방식
- 모든 금융 거래는 하나은행 백엔드를 통해 처리
- marry1q-be는 중계 역할만 수행
- 실제 상품 가입/해지, 자동이체 등록/해지는 하나은행 백엔드에서 처리

### 10.2 데이터 동기화
- `hana_bank_subscription_id`: 하나은행 상품 가입 ID
- `hana_bank_auto_transfer_id`: 하나은행 자동이체 ID
- 실제 금융 데이터는 하나은행 백엔드에서 관리
- marry1q-be는 메타데이터만 관리
