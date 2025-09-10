# Plan1Q 도메인 백엔드 설계 계획

## 1. 데이터베이스 스키마 설계

### 1.1 주요 엔티티 (기존 스키마 기반)

#### 1.1.1 InvestmentProfile (투자성향 검사)
```sql
CREATE TABLE `investment_profile` (
    `investment_profile_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '투자성향 검사 고유 ID',
    `user_seq_no` VARCHAR(20) NOT NULL COMMENT '사용자 고유 번호',
    `profile_type` VARCHAR(20) NOT NULL COMMENT '투자성향 타입 (CommonCode: INVESTMENT_PROFILE_TYPE)',
    `score` INT NOT NULL COMMENT '투자성향 점수 (0-100)',
    `description` TEXT NULL COMMENT '투자성향 설명',
    `expired_date` DATE NOT NULL COMMENT '만료일',
    `is_expired` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '만료 여부',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`investment_profile_id`),
    INDEX `idx_investment_profile_user_seq_no` (`user_seq_no`),
    INDEX `idx_investment_profile_expired_date` (`expired_date`)
);
```

#### 1.1.2 Plan1QGoal (Plan1Q 목표)
```sql
CREATE TABLE `plan1q_goal` (
    `plan1q_goal_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Plan1Q 목표 고유 ID',
    `goal_name` VARCHAR(100) NOT NULL COMMENT '목표명',
    `goal_description` TEXT NULL COMMENT '목표 상세 설명',
    `target_amount` DECIMAL(12, 0) NOT NULL COMMENT '목표 금액',
    `current_amount` DECIMAL(12, 0) NULL DEFAULT 0 COMMENT '현재 모인 금액',
    `expected_return` DECIMAL(5, 2) NULL COMMENT '예상 수익률 (%)',
    `target_period` INT NOT NULL COMMENT '목표 기간 (개월)',
    `maturity_date` DATE NOT NULL COMMENT '만기일',
    `monthly_payment` DECIMAL(12, 0) NOT NULL COMMENT '월 납입금액',
    `status` VARCHAR(30) NOT NULL DEFAULT 'in_progress' COMMENT '목표 상태 (CommonCode: PLAN1Q_GOAL_STATUS)',
    `subscription_progress` DECIMAL(5, 2) NULL DEFAULT 0 COMMENT '가입 진행률 (%)',
    `risk_level` VARCHAR(10) NOT NULL DEFAULT 'low' COMMENT '위험도 (CommonCode: RISK_LEVEL)',
    `icon` VARCHAR(50) NULL COMMENT '아이콘명',
    `color` VARCHAR(50) NULL COMMENT '색상 클래스',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `user_seq_no` VARCHAR(20) NOT NULL COMMENT '사용자 고유 번호',
    `couple_id` BIGINT NOT NULL COMMENT '커플 고유 ID',
    `investment_profile_id` BIGINT NULL COMMENT '투자성향 검사 ID',
    PRIMARY KEY (`plan1q_goal_id`),
    INDEX `idx_plan1q_goal_user_seq_no` (`user_seq_no`),
    INDEX `idx_plan1q_goal_couple_id` (`couple_id`),
    INDEX `idx_plan1q_goal_status` (`status`),
    INDEX `idx_plan1q_goal_created_at` (`created_at`),
    FOREIGN KEY (`investment_profile_id`) REFERENCES `investment_profile` (`investment_profile_id`)
);
```

#### 1.1.3 Plan1QProduct (Plan1Q 상품)
```sql
CREATE TABLE `plan1q_product` (
    `plan1q_product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Plan1Q 상품 고유 ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '상품명',
    `product_type` VARCHAR(20) NOT NULL COMMENT '상품 타입 (CommonCode: PLAN1Q_PRODUCT_TYPE)',
    `investment_ratio` DECIMAL(5, 2) NOT NULL COMMENT '투자 비율 (%)',
    `investment_amount` DECIMAL(12, 0) NOT NULL COMMENT '투자 금액',
    `return_rate` DECIMAL(5, 2) NULL COMMENT '수익률 (%)',
    `monthly_amount` DECIMAL(12, 0) NOT NULL COMMENT '월 납입금액',
    `subscribed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '가입 여부',
    `current_value` DECIMAL(12, 0) NULL DEFAULT 0 COMMENT '현재 가치',
    `profit` DECIMAL(12, 0) NULL DEFAULT 0 COMMENT '수익',
    `contract_date` DATE NULL COMMENT '계약일',
    `maturity_date` DATE NOT NULL COMMENT '만기일',
    `terms` TEXT NULL COMMENT '약관 내용',
    `contract` VARCHAR(255) NULL COMMENT '계약서명',
    `account_number` VARCHAR(50) NULL COMMENT '계좌번호',
    `risk_level` VARCHAR(50) NULL COMMENT '위험도',
    `risk_type` VARCHAR(50) NULL COMMENT '위험 타입',
    `asset_class` VARCHAR(50) NULL COMMENT '자산 클래스',
    `strategy` TEXT NULL COMMENT '투자 전략',
    `interest_rate` VARCHAR(50) NULL COMMENT '금리',
    `period` VARCHAR(50) NULL COMMENT '기간',
    `hana_bank_product_id` BIGINT NULL COMMENT '하나은행 상품 고유 ID',
    `hana_bank_subscription_id` VARCHAR(100) NULL COMMENT '하나은행 상품 가입 ID (하나은행 백엔드에서 관리)',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `plan1q_goal_id` BIGINT NOT NULL COMMENT 'Plan1Q 목표 고유 ID',
    PRIMARY KEY (`plan1q_product_id`),
    INDEX `idx_plan1q_product_goal_id` (`plan1q_goal_id`),
    INDEX `idx_plan1q_product_subscribed` (`subscribed`),
    INDEX `idx_plan1q_product_hana_subscription_id` (`hana_bank_subscription_id`),
    FOREIGN KEY (`plan1q_goal_id`) REFERENCES `plan1q_goal` (`plan1q_goal_id`),
    FOREIGN KEY (`hana_bank_product_id`) REFERENCES `hana_bank_product` (`product_id`)
);
```

#### 1.1.4 Plan1QTemplate (Plan1Q 템플릿)
```sql
CREATE TABLE `plan1q_template` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '템플릿 고유 ID',
    `title` VARCHAR(100) NOT NULL COMMENT '템플릿 제목',
    `description` TEXT NULL COMMENT '템플릿 설명',
    `icon` VARCHAR(50) NOT NULL COMMENT '아이콘명',
    `category` VARCHAR(50) NOT NULL COMMENT '카테고리',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`template_id`),
    INDEX `idx_plan1q_template_category` (`category`),
    INDEX `idx_plan1q_template_active` (`is_active`)
);
```

#### 1.1.5 Plan1QAutoTransfer (자동이체 설정)
```sql
CREATE TABLE `plan1q_auto_transfer` (
    `auto_transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '자동이체 고유 ID',
    `next_payment_date` DATE NOT NULL COMMENT '다음 납입일',
    `remaining_payments` INT NOT NULL COMMENT '남은 납입 횟수',
    `auto_transfer_account` VARCHAR(100) NOT NULL COMMENT '자동이체 계좌',
    `is_payment_completed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '납입 완료 여부',
    `hana_bank_auto_transfer_id` VARCHAR(100) NULL COMMENT '하나은행 자동이체 ID (하나은행 백엔드에서 관리)',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `plan1q_goal_id` BIGINT NOT NULL COMMENT 'Plan1Q 목표 고유 ID',
    PRIMARY KEY (`auto_transfer_id`),
    INDEX `idx_plan1q_auto_transfer_goal_id` (`plan1q_goal_id`),
    INDEX `idx_plan1q_auto_transfer_hana_id` (`hana_bank_auto_transfer_id`),
    FOREIGN KEY (`plan1q_goal_id`) REFERENCES `plan1q_goal` (`plan1q_goal_id`)
);
```

### 1.2 기존 테이블과의 관계
- `plan1q_goal` ↔ `marry1q_customer` (user_seq_no)
- `plan1q_goal` ↔ `marry1q_couple` (couple_id)
- `plan1q_product` ↔ `hana_bank_product` (product_id)
- `investment_profile` ↔ `marry1q_customer` (user_seq_no)

### 1.3 하나은행 백엔드 연동 구조
- **하나은행 백엔드**: 실제 상품 가입, 자동이체 등록, 계좌 관리 담당
- **marry1q-be**: 하나은행 백엔드 API 호출하여 결과를 프론트엔드에 전달하는 중계 역할
- **데이터 동기화**: 하나은행 백엔드에서 관리하는 실제 데이터와 marry1q-be의 메타데이터 동기화

### 1.4 CommonCode 데이터 추가
```sql
-- 투자성향 타입
INSERT INTO `common_code` VALUES
('CONSERVATIVE', '안정형', 'conservative', 'INVESTMENT_PROFILE_TYPE', 1, TRUE, '원금 보장과 안정적인 수익을 우선시하는 투자 성향', NOW(), NOW()),
('NEUTRAL', '중립형', 'neutral', 'INVESTMENT_PROFILE_TYPE', 2, TRUE, '안정성과 수익성의 균형을 추구하는 투자 성향', NOW(), NOW()),
('AGGRESSIVE', '공격형', 'aggressive', 'INVESTMENT_PROFILE_TYPE', 3, TRUE, '높은 수익을 추구하며 위험을 감수할 수 있는 투자 성향', NOW(), NOW());

-- Plan1Q 목표 상태
INSERT INTO `common_code` VALUES
('IN_PROGRESS', '진행중', 'in_progress', 'PLAN1Q_GOAL_STATUS', 1, TRUE, '목표 진행 중인 상태', NOW(), NOW()),
('SUBSCRIPTION_IN_PROGRESS', '가입진행중', 'subscription_in_progress', 'PLAN1Q_GOAL_STATUS', 2, TRUE, '상품 가입 진행 중인 상태', NOW(), NOW()),
('COMPLETED', '완료', 'completed', 'PLAN1Q_GOAL_STATUS', 3, TRUE, '목표 완료 상태', NOW(), NOW()),
('CANCELLED', '취소', 'cancelled', 'PLAN1Q_GOAL_STATUS', 4, TRUE, '목표 취소 상태', NOW(), NOW());

-- Plan1Q 상품 타입
INSERT INTO `common_code` VALUES
('FUND', '펀드', 'fund', 'PLAN1Q_PRODUCT_TYPE', 1, TRUE, '펀드 상품', NOW(), NOW()),
('SAVINGS', '적금', 'savings', 'PLAN1Q_PRODUCT_TYPE', 2, TRUE, '적금 상품', NOW(), NOW());

-- 위험도
INSERT INTO `common_code` VALUES
('LOW', '낮음', 'low', 'RISK_LEVEL', 1, TRUE, '낮은 위험도', NOW(), NOW()),
('MEDIUM', '중간', 'medium', 'RISK_LEVEL', 2, TRUE, '중간 위험도', NOW(), NOW()),
('HIGH', '높음', 'high', 'RISK_LEVEL', 3, TRUE, '높은 위험도', NOW(), NOW());
```

## 2. 백엔드 아키텍처 설계

### 2.1 패키지 구조
```
com.marry1q.marry1qbe.domain.plan1q/
├── controller/
│   ├── Plan1QController.java
│   ├── InvestmentProfileController.java
│   └── Plan1QTemplateController.java
├── service/
│   ├── Plan1QGoalService.java
│   ├── Plan1QProductService.java
│   ├── InvestmentProfileService.java
│   ├── Plan1QTemplateService.java
│   └── HanaBankApiService.java
├── repository/
│   ├── Plan1QGoalRepository.java
│   ├── Plan1QProductRepository.java
│   ├── InvestmentProfileRepository.java
│   └── Plan1QTemplateRepository.java
├── entity/
│   ├── Plan1QGoal.java
│   ├── Plan1QProduct.java
│   ├── InvestmentProfile.java
│   ├── Plan1QTemplate.java
│   └── Plan1QAutoTransfer.java
├── dto/
│   ├── request/
│   │   ├── CreatePlan1QGoalRequest.java
│   │   ├── UpdatePlan1QGoalRequest.java
│   │   ├── Plan1QGoalSearchRequest.java
│   │   ├── InvestmentProfileSubmitRequest.java
│   │   ├── Plan1QProductSubscribeRequest.java
│   │   └── Plan1QAutoTransferRequest.java
│   └── response/
│       ├── Plan1QGoalResponse.java
│       ├── Plan1QGoalDetailResponse.java
│       ├── Plan1QProductResponse.java
│       ├── InvestmentProfileResponse.java
│       ├── Plan1QDashboardResponse.java
│       ├── Plan1QTemplateResponse.java
│       └── Plan1QAutoTransferResponse.java
└── exception/
    ├── Plan1QGoalNotFoundException.java
    ├── InvestmentProfileNotFoundException.java
    └── Plan1QProductNotFoundException.java
```

### 2.2 공통 응답 구조 (기존 코드와 통일성 유지)

#### 2.2.1 CustomApiResponse 활용
```java
// 모든 API 응답에서 CustomApiResponse 사용
@GetMapping("/goals")
public ResponseEntity<CustomApiResponse<Page<Plan1QGoalResponse>>> getGoals(
        @AuthenticationPrincipal UserDetails userDetails,
        Plan1QGoalSearchRequest request,
        Pageable pageable) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    Page<Plan1QGoalResponse> goals = plan1QGoalService.getGoals(request, coupleId, pageable);
    
    return ResponseEntity.ok(CustomApiResponse.success(goals, "Plan1Q 목표 목록을 성공적으로 조회했습니다."));
}
```

#### 2.2.2 Pageable 객체 활용
```java
// 검색 요청 DTO에 Pageable 정보 포함
@Getter
@Setter
@Builder
public class Plan1QGoalSearchRequest {
    private String searchTerm;
    private String status;
    private String period;
    private String sortBy;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}

// Service에서 Pageable 처리
public Page<Plan1QGoalResponse> getGoals(Plan1QGoalSearchRequest request, Long coupleId, Pageable pageable) {
    PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
    
    // 정렬 조건 추가
    if ("수익률순".equals(request.getSortBy())) {
        pageRequest = PageRequest.of(request.getPage(), request.getSize(), Sort.by("expectedReturn").descending());
    } else if ("가입일순".equals(request.getSortBy())) {
        pageRequest = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());
    } else {
        pageRequest = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());
    }
    
    return plan1QGoalRepository.findBySearchConditions(request, coupleId, pageRequest);
}
```

### 2.3 예외 처리 (기존 코드와 통일성 유지)

#### 2.3.1 CustomException 상속
```java
public class Plan1QGoalNotFoundException extends CustomException {
    public Plan1QGoalNotFoundException(String message) {
        super(ErrorCode.PLAN1Q_GOAL_NOT_FOUND, message);
    }
}

public class InvestmentProfileNotFoundException extends CustomException {
    public InvestmentProfileNotFoundException(String message) {
        super(ErrorCode.INVESTMENT_PROFILE_NOT_FOUND, message);
    }
}
```

#### 2.3.2 ErrorCode 추가
```java
// ErrorCode enum에 Plan1Q 관련 에러 코드 추가
PLAN1Q_GOAL_NOT_FOUND("PLAN1Q_GOAL_NOT_FOUND", "Plan1Q 목표를 찾을 수 없습니다."),
INVESTMENT_PROFILE_NOT_FOUND("INVESTMENT_PROFILE_NOT_FOUND", "투자성향 검사 결과를 찾을 수 없습니다."),
INVESTMENT_PROFILE_EXPIRED("INVESTMENT_PROFILE_EXPIRED", "투자성향 검사가 만료되었습니다."),
PLAN1Q_PRODUCT_NOT_FOUND("PLAN1Q_PRODUCT_NOT_FOUND", "Plan1Q 상품을 찾을 수 없습니다."),
INSUFFICIENT_INVESTMENT_PROFILE("INSUFFICIENT_INVESTMENT_PROFILE", "투자성향 검사가 필요합니다.");
```

### 2.4 CommonCodeService 확장
```java
@Service
public class CommonCodeService {
    
    // Plan1Q 관련 메서드 추가
    public List<CommonCode> getInvestmentProfileTypes() {
        return getCodesByGroup("INVESTMENT_PROFILE_TYPE");
    }
    
    public List<CommonCode> getPlan1QGoalStatuses() {
        return getCodesByGroup("PLAN1Q_GOAL_STATUS");
    }
    
    public List<CommonCode> getPlan1QProductTypes() {
        return getCodesByGroup("PLAN1Q_PRODUCT_TYPE");
    }
    
    public List<CommonCode> getRiskLevels() {
        return getCodesByGroup("RISK_LEVEL");
    }
    
    // 코드값으로 코드명 조회
    public String getInvestmentProfileTypeName(String codeValue) {
        return getCodeName("INVESTMENT_PROFILE_TYPE", codeValue);
    }
    
    public String getPlan1QGoalStatusName(String codeValue) {
        return getCodeName("PLAN1Q_GOAL_STATUS", codeValue);
    }
    
    public String getPlan1QProductTypeName(String codeValue) {
        return getCodeName("PLAN1Q_PRODUCT_TYPE", codeValue);
    }
    
    public String getRiskLevelName(String codeValue) {
        return getCodeName("RISK_LEVEL", codeValue);
    }
}
```

## 3. API 설계

### 3.1 투자성향 검사 API

#### 3.1.1 투자성향 검사 문항 조회
```java
@GetMapping("/investment-profile/questions")
@Operation(summary = "투자성향 검사 문항 조회")
public ResponseEntity<CustomApiResponse<List<InvestmentQuestionResponse>>> getInvestmentQuestions() {
    List<InvestmentQuestionResponse> questions = investmentProfileService.getQuestions();
    return ResponseEntity.ok(CustomApiResponse.success(questions));
}
```

#### 3.1.2 투자성향 검사 결과 저장
```java
@PostMapping("/investment-profile/submit")
@Operation(summary = "투자성향 검사 결과 저장")
public ResponseEntity<CustomApiResponse<InvestmentProfileResponse>> submitInvestmentProfile(
        @RequestBody InvestmentProfileSubmitRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    InvestmentProfileResponse profile = investmentProfileService.submitProfile(request, currentUserSeqNo);
    return ResponseEntity.ok(CustomApiResponse.success(profile, "투자성향 검사가 완료되었습니다."));
}
```

#### 3.1.3 투자성향 프로필 조회
```java
@GetMapping("/investment-profile")
@Operation(summary = "투자성향 프로필 조회")
public ResponseEntity<CustomApiResponse<InvestmentProfileResponse>> getInvestmentProfile(
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    InvestmentProfileResponse profile = investmentProfileService.getProfile(currentUserSeqNo);
    return ResponseEntity.ok(CustomApiResponse.success(profile));
}
```

### 3.2 Plan1Q 목표 관리 API

#### 3.2.1 Plan1Q 목표 목록 조회
```java
@GetMapping("/goals")
@Operation(summary = "Plan1Q 목표 목록 조회")
public ResponseEntity<CustomApiResponse<Page<Plan1QGoalResponse>>> getGoals(
        @AuthenticationPrincipal UserDetails userDetails,
        Plan1QGoalSearchRequest request,
        Pageable pageable) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    Page<Plan1QGoalResponse> goals = plan1QGoalService.getGoals(request, coupleId, pageable);
    return ResponseEntity.ok(CustomApiResponse.success(goals));
}
```

#### 3.2.2 Plan1Q 목표 상세 조회
```java
@GetMapping("/goals/{goalId}")
@Operation(summary = "Plan1Q 목표 상세 조회")
public ResponseEntity<CustomApiResponse<Plan1QGoalDetailResponse>> getGoalDetail(
        @PathVariable Long goalId,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    Plan1QGoalDetailResponse goal = plan1QGoalService.getGoalDetail(goalId, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(goal));
}
```

#### 3.2.3 Plan1Q 목표 생성
```java
@PostMapping("/goals")
@Operation(summary = "Plan1Q 목표 생성")
public ResponseEntity<CustomApiResponse<Plan1QGoalDetailResponse>> createGoal(
        @RequestBody CreatePlan1QGoalRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    Plan1QGoalDetailResponse goal = plan1QGoalService.createGoal(request, currentUserSeqNo, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(goal, "Plan1Q 목표가 성공적으로 생성되었습니다."));
}
```

### 3.3 Plan1Q 상품 관리 API

#### 3.3.1 Plan1Q 상품 가입 (하나은행 백엔드 연동)
```java
@PostMapping("/goals/{goalId}/products/{productId}/subscribe")
@Operation(summary = "Plan1Q 상품 가입")
public ResponseEntity<CustomApiResponse<Plan1QProductResponse>> subscribeProduct(
        @PathVariable Long goalId,
        @PathVariable Long productId,
        @RequestBody Plan1QProductSubscribeRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    // 하나은행 백엔드에 상품 가입 요청
    Plan1QProductResponse product = plan1QProductService.subscribeProduct(goalId, productId, request, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(product, "상품 가입이 완료되었습니다."));
}
```

#### 3.3.2 Plan1Q 상품 해지 (하나은행 백엔드 연동)
```java
@DeleteMapping("/goals/{goalId}/products/{productId}/subscribe")
@Operation(summary = "Plan1Q 상품 해지")
public ResponseEntity<CustomApiResponse<Void>> unsubscribeProduct(
        @PathVariable Long goalId,
        @PathVariable Long productId,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    // 하나은행 백엔드에 상품 해지 요청
    plan1QProductService.unsubscribeProduct(goalId, productId, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(null, "상품 해지가 완료되었습니다."));
}
```

### 3.4 자동이체 관리 API

#### 3.4.1 자동이체 등록 (하나은행 백엔드 연동)
```java
@PostMapping("/goals/{goalId}/auto-transfer")
@Operation(summary = "자동이체 등록")
public ResponseEntity<CustomApiResponse<Plan1QAutoTransferResponse>> registerAutoTransfer(
        @PathVariable Long goalId,
        @RequestBody Plan1QAutoTransferRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    // 하나은행 백엔드에 자동이체 등록 요청
    Plan1QAutoTransferResponse autoTransfer = plan1QProductService.registerAutoTransfer(goalId, request, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(autoTransfer, "자동이체가 등록되었습니다."));
}
```

#### 3.4.2 자동이체 수정 (하나은행 백엔드 연동)
```java
@PutMapping("/goals/{goalId}/auto-transfer")
@Operation(summary = "자동이체 수정")
public ResponseEntity<CustomApiResponse<Plan1QAutoTransferResponse>> updateAutoTransfer(
        @PathVariable Long goalId,
        @RequestBody Plan1QAutoTransferRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    // 하나은행 백엔드에 자동이체 수정 요청
    Plan1QAutoTransferResponse autoTransfer = plan1QProductService.updateAutoTransfer(goalId, request, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(autoTransfer, "자동이체가 수정되었습니다."));
}
```

### 3.5 Plan1Q 대시보드 API

#### 3.5.1 Plan1Q 대시보드 통계
```java
@GetMapping("/dashboard")
@Operation(summary = "Plan1Q 대시보드 통계")
public ResponseEntity<CustomApiResponse<Plan1QDashboardResponse>> getDashboard(
        @AuthenticationPrincipal UserDetails userDetails) {
    
    String currentUserSeqNo = userDetails.getUsername();
    Long coupleId = coupleService.getCurrentCoupleId();
    
    Plan1QDashboardResponse dashboard = plan1QGoalService.getDashboard(currentUserSeqNo, coupleId);
    return ResponseEntity.ok(CustomApiResponse.success(dashboard));
}
```

## 4. 비즈니스 로직 설계

### 4.1 투자성향 검사 로직
```java
@Service
@RequiredArgsConstructor
public class InvestmentProfileService {
    
    private final InvestmentProfileRepository investmentProfileRepository;
    private final CommonCodeService commonCodeService;
    
    public InvestmentProfileResponse submitProfile(InvestmentProfileSubmitRequest request, String userSeqNo) {
        // 답변 점수 계산
        int totalScore = request.getAnswers().stream().mapToInt(Integer::intValue).sum();
        double avgScore = (double) totalScore / request.getAnswers().size();
        
        // 투자성향 타입 결정 (CommonCode 사용)
        String profileType = determineProfileType(avgScore);
        
        // 만료일 계산 (1년 후)
        LocalDate expiredDate = LocalDate.now().plusYears(1);
        
        // 프로필 저장
        InvestmentProfile profile = InvestmentProfile.builder()
                .userSeqNo(userSeqNo)
                .profileType(profileType) // CommonCode 값 사용
                .score((int) (avgScore * 20)) // 0-100 점수로 변환
                .description(getProfileDescription(profileType))
                .expiredDate(expiredDate)
                .isExpired(false)
                .build();
        
        InvestmentProfile savedProfile = investmentProfileRepository.save(profile);
        return InvestmentProfileResponse.from(savedProfile, commonCodeService);
    }
    
    private String determineProfileType(double avgScore) {
        if (avgScore >= 4) return "aggressive";
        if (avgScore >= 3) return "neutral";
        return "conservative";
    }
    
    private String getProfileDescription(String profileType) {
        return commonCodeService.getCodeName("INVESTMENT_PROFILE_TYPE", profileType);
    }
}
```

### 4.2 Plan1Q 목표 생성 로직
```java
@Service
@RequiredArgsConstructor
public class Plan1QGoalService {
    
    private final Plan1QGoalRepository plan1QGoalRepository;
    private final Plan1QProductRepository plan1QProductRepository;
    private final InvestmentProfileService investmentProfileService;
    private final PortfolioRecommendationService portfolioRecommendationService;
    private final CommonCodeService commonCodeService;
    
    @Transactional
    public Plan1QGoalDetailResponse createGoal(CreatePlan1QGoalRequest request, String userSeqNo, Long coupleId) {
        // 투자성향 검사 필요 여부 확인
        InvestmentProfile profile = investmentProfileService.getProfile(userSeqNo);
        if (profile == null || profile.isExpired()) {
            throw new InsufficientInvestmentProfileException("투자성향 검사가 필요합니다.");
        }
        
        // 목표 생성 (CommonCode 사용)
        Plan1QGoal goal = Plan1QGoal.builder()
                .goalName(request.getGoalTitle())
                .goalDescription(request.getDetailedGoal())
                .targetAmount(request.getTargetAmount())
                .targetPeriod(request.getTargetPeriod())
                .monthlyAmount(calculateMonthlyAmount(request.getTargetAmount(), request.getTargetPeriod()))
                .maturityDate(calculateMaturityDate(request.getTargetPeriod()))
                .status("in_progress") // CommonCode 값 사용
                .riskLevel("low") // CommonCode 값 사용
                .userSeqNo(userSeqNo)
                .coupleId(coupleId)
                .investmentProfileId(profile.getInvestmentProfileId())
                .build();
        
        Plan1QGoal savedGoal = plan1QGoalRepository.save(goal);
        
        // AI 포트폴리오 추천 (하나은행 상품 정보 기반)
        List<Plan1QProduct> recommendedProducts = portfolioRecommendationService.recommendPortfolio(
                savedGoal, profile);
        
        // 추천 상품 저장 (실제 가입은 사용자가 선택 후 진행)
        plan1QProductRepository.saveAll(recommendedProducts);
        
        return Plan1QGoalDetailResponse.from(savedGoal, recommendedProducts, commonCodeService);
    }
}
```

### 4.3 Plan1Q 상품 가입 로직 (하나은행 백엔드 연동)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class Plan1QProductService {
    
    private final Plan1QProductRepository plan1QProductRepository;
    private final Plan1QGoalRepository plan1QGoalRepository;
    private final HanaBankApiService hanaBankApiService;
    private final CommonCodeService commonCodeService;
    
    @Transactional
    public Plan1QProductResponse subscribeProduct(Long goalId, Long productId, 
                                                Plan1QProductSubscribeRequest request, Long coupleId) {
        // 목표 및 상품 조회
        Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
                .orElseThrow(() -> new Plan1QGoalNotFoundException("목표를 찾을 수 없습니다."));
        
        Plan1QProduct product = plan1QProductRepository.findById(productId)
                .orElseThrow(() -> new Plan1QProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!goal.getCoupleId().equals(coupleId)) {
            throw new ForbiddenException("해당 커플의 목표가 아닙니다.");
        }
        
        // 하나은행 백엔드에 상품 가입 요청
        HanaBankSubscriptionRequest hanaRequest = HanaBankSubscriptionRequest.builder()
                .productId(product.getHanaBankProductId())
                .userSeqNo(goal.getUserSeqNo())
                .amount(product.getInvestmentAmount())
                .period(goal.getTargetPeriod())
                .accountNumber(request.getAccountNumber())
                .build();
        
        HanaBankSubscriptionResponse hanaResponse = hanaBankApiService.subscribeProduct(hanaRequest);
        
        // 가입 성공 시 로컬 데이터 업데이트
        product.updateSubscription(
                true,
                hanaResponse.getSubscriptionId(),
                hanaResponse.getAccountNumber(),
                LocalDate.now(),
                hanaResponse.getContractNumber()
        );
        
        Plan1QProduct savedProduct = plan1QProductRepository.save(product);
        
        // 목표 상태 업데이트 (일부 상품 가입 시)
        updateGoalStatus(goal);
        
        return Plan1QProductResponse.from(savedProduct, commonCodeService);
    }
    
    @Transactional
    public void unsubscribeProduct(Long goalId, Long productId, Long coupleId) {
        // 상품 조회
        Plan1QProduct product = plan1QProductRepository.findById(productId)
                .orElseThrow(() -> new Plan1QProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 하나은행 백엔드에 상품 해지 요청
        HanaBankUnsubscriptionRequest hanaRequest = HanaBankUnsubscriptionRequest.builder()
                .subscriptionId(product.getHanaBankSubscriptionId())
                .build();
        
        hanaBankApiService.unsubscribeProduct(hanaRequest);
        
        // 해지 성공 시 로컬 데이터 업데이트
        product.updateSubscription(false, null, null, null, null);
        plan1QProductRepository.save(product);
        
        // 목표 상태 업데이트
        Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
                .orElseThrow(() -> new Plan1QGoalNotFoundException("목표를 찾을 수 없습니다."));
        updateGoalStatus(goal);
    }
    
    private void updateGoalStatus(Plan1QGoal goal) {
        List<Plan1QProduct> products = plan1QProductRepository.findByPlan1qGoalId(goal.getPlan1qGoalId());
        long subscribedCount = products.stream().filter(Plan1QProduct::getSubscribed).count();
        
        if (subscribedCount == 0) {
            goal.updateStatus("in_progress");
        } else if (subscribedCount == products.size()) {
            goal.updateStatus("completed");
        } else {
            goal.updateStatus("subscription_in_progress");
        }
        
        plan1QGoalRepository.save(goal);
    }
}
```

### 4.3 포트폴리오 추천 로직
```java
@Service
@RequiredArgsConstructor
public class PortfolioRecommendationService {
    
    private final HanaBankProductRepository hanaBankProductRepository;
    private final CommonCodeService commonCodeService;
    
    public List<Plan1QProduct> recommendPortfolio(Plan1QGoal goal, InvestmentProfile profile) {
        List<Plan1QProduct> products = new ArrayList<>();
        
        // 투자성향에 따른 포트폴리오 구성 (CommonCode 값 사용)
        switch (profile.getProfileType()) {
            case "conservative":
                products.addAll(recommendConservativePortfolio(goal));
                break;
            case "neutral":
                products.addAll(recommendNeutralPortfolio(goal));
                break;
            case "aggressive":
                products.addAll(recommendAggressivePortfolio(goal));
                break;
        }
        
        return products;
    }
    
    private List<Plan1QProduct> recommendConservativePortfolio(Plan1QGoal goal) {
        // 안정형: 적금 70%, 안정형 펀드 30%
        List<Plan1QProduct> products = new ArrayList<>();
        
        BigDecimal totalAmount = goal.getTargetAmount();
        BigDecimal savingsAmount = totalAmount.multiply(new BigDecimal("0.7"));
        BigDecimal fundAmount = totalAmount.multiply(new BigDecimal("0.3"));
        
        // 적금 상품 추천 (CommonCode 값 사용)
        HanaBankProduct savingsProduct = hanaBankProductRepository.findByTypeAndRiskLevel("savings", "low");
        products.add(createPlan1QProduct(goal, savingsProduct, savingsAmount, 70.0, "savings"));
        
        // 안정형 펀드 추천 (CommonCode 값 사용)
        HanaBankProduct fundProduct = hanaBankProductRepository.findByTypeAndRiskLevel("fund", "low");
        products.add(createPlan1QProduct(goal, fundProduct, fundAmount, 30.0, "fund"));
        
        return products;
    }
    
    private Plan1QProduct createPlan1QProduct(Plan1QGoal goal, HanaBankProduct hanaProduct, 
                                            BigDecimal amount, double ratio, String productType) {
        return Plan1QProduct.builder()
                .plan1qGoalId(goal.getPlan1qGoalId())
                .productName(hanaProduct.getName())
                .productType(productType) // CommonCode 값 사용
                .investmentRatio(new BigDecimal(ratio))
                .investmentAmount(amount)
                .returnRate(hanaProduct.getInterestRate())
                .monthlyAmount(amount.divide(new BigDecimal(goal.getTargetPeriod()), 0, RoundingMode.HALF_UP))
                .maturityDate(goal.getMaturityDate())
                .terms(hanaProduct.getDescription())
                .contract(hanaProduct.getName() + " 계약서")
                .hanaBankProductId(hanaProduct.getProductId())
                .build();
    }
}
```

### 4.5 하나은행 백엔드 연동 서비스
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class HanaBankApiService {
    
    private final RestTemplate restTemplate;
    private final ExternalApiConfig externalApiConfig;
    
    /**
     * 하나은행 백엔드에 상품 가입 요청
     */
    public HanaBankSubscriptionResponse subscribeProduct(HanaBankSubscriptionRequest request) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("product-subscribe");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("하나은행 백엔드 상품 가입 요청 - URL: {}, 요청: {}", fullUrl, request);
        
        try {
            ResponseEntity<HanaBankSubscriptionResponse> response = restTemplate.postForEntity(
                fullUrl, request, HanaBankSubscriptionResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("하나은행 백엔드 상품 가입 성공 - 응답: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("하나은행 백엔드 상품 가입 실패 - 상태코드: {}", response.getStatusCode());
                throw new ExternalApiException("HanaBank", "상품 가입 실패", "하나은행 백엔드 응답 오류");
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 상품 가입 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new ExternalApiException("HanaBank", "상품 가입 중 오류 발생", e.getMessage());
        }
    }
    
    /**
     * 하나은행 백엔드에 자동이체 등록 요청
     */
    public HanaBankAutoTransferResponse registerAutoTransfer(HanaBankAutoTransferRequest request) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("auto-transfer-register");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint;
        
        log.info("하나은행 백엔드 자동이체 등록 요청 - URL: {}, 요청: {}", fullUrl, request);
        
        try {
            ResponseEntity<HanaBankAutoTransferResponse> response = restTemplate.postForEntity(
                fullUrl, request, HanaBankAutoTransferResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("하나은행 백엔드 자동이체 등록 성공 - 응답: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("하나은행 백엔드 자동이체 등록 실패 - 상태코드: {}", response.getStatusCode());
                throw new ExternalApiException("HanaBank", "자동이체 등록 실패", "하나은행 백엔드 응답 오류");
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 자동이체 등록 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new ExternalApiException("HanaBank", "자동이체 등록 중 오류 발생", e.getMessage());
        }
    }
    
    /**
     * 하나은행 백엔드에서 상품 정보 조회
     */
    public HanaBankProductDetailResponse getProductDetail(String productId) {
        String endpoint = externalApiConfig.getHanaBank().getEndpoints().get("product-detail");
        String fullUrl = externalApiConfig.getHanaBank().getUrl() + endpoint + "/" + productId;
        
        log.info("하나은행 백엔드 상품 정보 조회 - URL: {}", fullUrl);
        
        try {
            ResponseEntity<HanaBankProductDetailResponse> response = restTemplate.getForEntity(
                fullUrl, HanaBankProductDetailResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("하나은행 백엔드 상품 정보 조회 성공 - 응답: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("하나은행 백엔드 상품 정보 조회 실패 - 상태코드: {}", response.getStatusCode());
                throw new ExternalApiException("HanaBank", "상품 정보 조회 실패", "하나은행 백엔드 응답 오류");
            }
        } catch (Exception e) {
            log.error("하나은행 백엔드 상품 정보 조회 중 예외 발생 - Error: {}", e.getMessage(), e);
            throw new ExternalApiException("HanaBank", "상품 정보 조회 중 오류 발생", e.getMessage());
        }
    }
}
```

## 5. 엔티티 설계 (CommonCode 활용)

### 5.1 InvestmentProfile 엔티티
```java
@Entity
@Table(name = "investment_profile")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_profile_id")
    private Long investmentProfileId;
    
    @Column(name = "user_seq_no", length = 20, nullable = false)
    private String userSeqNo;
    
    @Column(name = "profile_type", length = 20, nullable = false)
    private String profileType; // CommonCode 값: 'conservative', 'neutral', 'aggressive'
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;
    
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // CommonCode를 통한 코드명 조회 메서드
    public String getProfileTypeName(CommonCodeService commonCodeService) {
        return commonCodeService.getInvestmentProfileTypeName(this.profileType);
    }
}
```

### 5.2 Plan1QGoal 엔티티
```java
@Entity
@Table(name = "plan1q_goal")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan1q_goal_id")
    private Long plan1qGoalId;
    
    @Column(name = "goal_name", length = 100, nullable = false)
    private String goalName;
    
    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;
    
    @Column(name = "target_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal targetAmount;
    
    @Column(name = "current_amount", precision = 12, scale = 0)
    private BigDecimal currentAmount;
    
    @Column(name = "expected_return", precision = 5, scale = 2)
    private BigDecimal expectedReturn;
    
    @Column(name = "target_period", nullable = false)
    private Integer targetPeriod;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "monthly_payment", precision = 12, scale = 0, nullable = false)
    private BigDecimal monthlyAmount;
    
    @Column(name = "status", length = 30, nullable = false)
    private String status; // CommonCode 값: 'in_progress', 'subscription_in_progress', 'completed', 'cancelled'
    
    @Column(name = "subscription_progress", precision = 5, scale = 2)
    private BigDecimal subscriptionProgress;
    
    @Column(name = "risk_level", length = 10, nullable = false)
    private String riskLevel; // CommonCode 값: 'low', 'medium', 'high'
    
    @Column(name = "icon", length = 50)
    private String icon;
    
    @Column(name = "color", length = 50)
    private String color;
    
    @Column(name = "user_seq_no", length = 20, nullable = false)
    private String userSeqNo;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @Column(name = "investment_profile_id")
    private Long investmentProfileId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // CommonCode를 통한 코드명 조회 메서드들
    public String getStatusName(CommonCodeService commonCodeService) {
        return commonCodeService.getPlan1QGoalStatusName(this.status);
    }
    
    public String getRiskLevelName(CommonCodeService commonCodeService) {
        return commonCodeService.getRiskLevelName(this.riskLevel);
    }
}
```

### 5.3 Plan1QProduct 엔티티
```java
@Entity
@Table(name = "plan1q_product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan1q_product_id")
    private Long plan1qProductId;
    
    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;
    
    @Column(name = "product_type", length = 20, nullable = false)
    private String productType; // CommonCode 값: 'fund', 'savings'
    
    @Column(name = "investment_ratio", precision = 5, scale = 2, nullable = false)
    private BigDecimal investmentRatio;
    
    @Column(name = "investment_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal investmentAmount;
    
    @Column(name = "return_rate", precision = 5, scale = 2)
    private BigDecimal returnRate;
    
    @Column(name = "monthly_amount", precision = 12, scale = 0, nullable = false)
    private BigDecimal monthlyAmount;
    
    @Column(name = "subscribed", nullable = false)
    private Boolean subscribed;
    
    @Column(name = "current_value", precision = 12, scale = 0)
    private BigDecimal currentValue;
    
    @Column(name = "profit", precision = 12, scale = 0)
    private BigDecimal profit;
    
    @Column(name = "contract_date")
    private LocalDate contractDate;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;
    
    @Column(name = "contract", length = 255)
    private String contract;
    
    @Column(name = "account_number", length = 50)
    private String accountNumber;
    
    @Column(name = "plan1q_goal_id", nullable = false)
    private Long plan1qGoalId;
    
    @Column(name = "hana_bank_product_id")
    private Long hanaBankProductId;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // CommonCode를 통한 코드명 조회 메서드
    public String getProductTypeName(CommonCodeService commonCodeService) {
        return commonCodeService.getPlan1QProductTypeName(this.productType);
    }
}
```

## 6. 보안 및 인증

### 6.1 JWT 토큰 기반 인증
- 기존 `marry1q_token` 테이블 활용
- 모든 API에 `@SecurityRequirement(name = "Bearer Authentication")` 적용
- `@AuthenticationPrincipal UserDetails userDetails`로 사용자 정보 추출

### 6.2 권한 검증
```java
// 커플 멤버십 확인
private void validateCoupleMembership(Long coupleId, String userSeqNo) {
    if (!coupleService.isCoupleMember(coupleId, userSeqNo)) {
        throw new ForbiddenException("해당 커플의 멤버가 아닙니다.");
    }
}

// 목표 소유권 확인
private void validateGoalOwnership(Long goalId, Long coupleId) {
    Plan1QGoal goal = plan1QGoalRepository.findById(goalId)
            .orElseThrow(() -> new Plan1QGoalNotFoundException("목표를 찾을 수 없습니다."));
    
    if (!goal.getCoupleId().equals(coupleId)) {
        throw new ForbiddenException("해당 커플의 목표가 아닙니다.");
    }
}
```

## 7. 성능 최적화

### 7.1 인덱스 설계
```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_plan1q_goal_status_created_at ON plan1q_goal(status, created_at);
CREATE INDEX idx_plan1q_goal_couple_status ON plan1q_goal(couple_id, status);
CREATE INDEX idx_plan1q_product_goal_subscribed ON plan1q_product(plan1q_goal_id, subscribed);
CREATE INDEX idx_investment_profile_user_expired ON investment_profile(user_seq_no, is_expired);
```

### 7.2 페이징 처리
- 모든 목록 조회 API에 페이징 적용
- 기본 페이지 크기: 10개
- 정렬 조건: 최신순, 수익률순, 가입일순

### 7.3 CommonCode 캐싱 활용
```java
// CommonCodeService의 기존 캐싱 기능 활용
@Cacheable(value = "commonCodes", key = "#codeGroup")
public List<CommonCode> getCodesByGroup(String codeGroup) {
    // 캐시된 데이터 반환으로 DB 조회 최소화
}

// Plan1Q 관련 코드 그룹들도 자동으로 캐싱됨
// - INVESTMENT_PROFILE_TYPE
// - PLAN1Q_GOAL_STATUS  
// - PLAN1Q_PRODUCT_TYPE
// - RISK_LEVEL
```


### 8.3 로깅
```java
@Slf4j
@Service
public class Plan1QGoalService {
    
    public Plan1QGoalDetailResponse createGoal(CreatePlan1QGoalRequest request, String userSeqNo, Long coupleId) {
        log.info("Plan1Q 목표 생성 시작 - 사용자: {}, 커플: {}, 목표명: {}", 
                userSeqNo, coupleId, request.getGoalTitle());
        
        try {
            // 목표 생성 로직
            Plan1QGoalDetailResponse response = // ... 생성 로직
            
            log.info("Plan1Q 목표 생성 완료 - 목표ID: {}", response.getGoalId());
            return response;
        } catch (Exception e) {
            log.error("Plan1Q 목표 생성 실패 - 사용자: {}, 오류: {}", userSeqNo, e.getMessage(), e);
            throw e;
        }
    }
}
```

## 9. CommonCode 활용의 이점

### 9.1 중앙집중식 코드 관리
- 모든 Plan1Q 관련 코드를 `common_code` 테이블에서 중앙 관리
- 코드 추가/수정 시 데이터베이스만 변경하면 모든 시스템에 반영
- 하드코딩된 ENUM 값들을 제거하여 유연성 확보

### 9.2 다국어 지원 및 확장성
- `code_name`과 `code_value`를 분리하여 다국어 지원 가능
- `description` 필드로 상세 설명 관리
- `sort_order`로 정렬 순서 제어

### 9.3 캐싱을 통한 성능 최적화
- CommonCodeService의 `@Cacheable` 어노테이션으로 자동 캐싱
- 코드 조회 시 DB 접근 최소화
- Plan1Q 관련 모든 코드 그룹이 자동으로 캐싱됨

### 9.4 유지보수성 향상
- 코드 변경 시 애플리케이션 재배포 없이 DB만 수정
- 기존 시스템과의 일관성 유지
- 새로운 코드 그룹과 코드 추가가 용이

## 10. 하나은행 백엔드 연동의 이점

### 10.1 역할 분리 및 책임 명확화
- **하나은행 백엔드**: 실제 상품 가입, 자동이체 등록, 계좌 관리 등 핵심 금융 기능 담당
- **marry1q-be**: 사용자 인터페이스, 목표 관리, 포트폴리오 추천 등 비즈니스 로직 담당
- 각 시스템의 전문 영역에 집중하여 안정성과 확장성 확보

### 10.2 데이터 동기화 및 일관성
- `hana_bank_subscription_id`, `hana_bank_auto_transfer_id` 필드로 하나은행 백엔드와 연결
- 실제 금융 데이터는 하나은행 백엔드에서 관리, marry1q-be는 메타데이터만 관리
- 데이터 불일치 시 하나은행 백엔드 데이터를 기준으로 동기화

### 10.3 보안 및 규정 준수
- 실제 금융 거래는 하나은행 백엔드에서 처리하여 보안 강화
- 금융 규정 준수 책임을 하나은행 백엔드에 집중
- marry1q-be는 사용자 인증 및 권한 관리에만 집중

### 10.4 확장성 및 유지보수성
- 하나은행 상품 변경 시 하나은행 백엔드만 수정하면 됨
- marry1q-be는 비즈니스 로직 변경에만 집중 가능
- 마이크로서비스 아키텍처로 각 시스템 독립적 개발/배포 가능

이 설계 계획은 기존 marry1q-be 프로젝트의 아키텍처와 패턴을 따르며, Plan1Q 도메인의 모든 기능을 구현할 수 있는 완전한 백엔드 시스템을 제공합니다. 특히 하나은행 백엔드와의 연동을 통해 실제 금융 서비스의 안정성과 확장성을 확보합니다.
