-- 하나은행 상품 정보 저장을 위한 SQL 스키마
-- 하나은행 백엔드 서버에서 사용할 상품 정보 테이블

-- 1. 상품 카테고리 테이블 (상품 분류를 위한 마스터 테이블)
CREATE TABLE hana_bank_product_categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(20) NOT NULL UNIQUE COMMENT '카테고리 코드 (예: DEPOSIT, SAVINGS, FUND, BOND 등)',
    category_name VARCHAR(100) NOT NULL COMMENT '카테고리명 (예: 예금, 적금, 펀드, 채권 등)',
    category_description TEXT COMMENT '카테고리 설명',
    risk_level VARCHAR(20) COMMENT '위험도 (LOW, MEDIUM, HIGH)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_code (category_code),
    INDEX idx_risk_level (risk_level)
) COMMENT '하나은행 상품 카테고리 마스터';

-- 2. 하나은행 상품 정보 테이블 (메인 상품 테이블)
CREATE TABLE hana_bank_products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    external_product_id VARCHAR(50) NOT NULL UNIQUE COMMENT '하나은행 외부 상품 ID',
    product_code VARCHAR(50) NOT NULL COMMENT '상품 코드',
    product_name VARCHAR(200) NOT NULL COMMENT '상품명',
    product_sub_name VARCHAR(100) COMMENT '상품 부제목',
    category_id BIGINT NOT NULL COMMENT '카테고리 ID',
    product_type VARCHAR(50) NOT NULL COMMENT '상품 유형 (DEPOSIT, SAVINGS, FUND, BOND, STOCK, IRP 등)',
    
    -- 수익률 관련
    base_rate DECIMAL(5,2) COMMENT '기본 금리 (%)',
    max_rate DECIMAL(5,2) COMMENT '최고 금리 (%)',
    expected_return_rate DECIMAL(5,2) COMMENT '예상 수익률 (%)',
    actual_return_rate DECIMAL(5,2) COMMENT '실제 수익률 (%)',
    
    -- 투자 관련
    min_investment_amount DECIMAL(15,2) COMMENT '최소 투자 금액',
    max_investment_amount DECIMAL(15,2) COMMENT '최대 투자 금액',
    monthly_investment_amount DECIMAL(15,2) COMMENT '월 투자 금액 (적금 등)',
    
    -- 기간 관련
    min_period_months INT COMMENT '최소 가입 기간 (월)',
    max_period_months INT COMMENT '최대 가입 기간 (월)',
    maturity_period_months INT COMMENT '만기 기간 (월)',
    
    -- 위험도 및 특성
    risk_level VARCHAR(20) NOT NULL COMMENT '위험도 (LOW, MEDIUM, HIGH)',
    risk_score INT COMMENT '위험도 점수 (1-10)',
    volatility_level VARCHAR(20) COMMENT '변동성 수준 (LOW, MEDIUM, HIGH)',
    
    -- 상품 특성
    is_tax_free BOOLEAN DEFAULT FALSE COMMENT '비과세 여부',
    is_guaranteed BOOLEAN DEFAULT FALSE COMMENT '원금 보장 여부',
    is_compound_interest BOOLEAN DEFAULT TRUE COMMENT '복리 여부',
    is_monthly_interest BOOLEAN DEFAULT FALSE COMMENT '월 이자 지급 여부',
    
    -- 상품 상태
    product_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '상품 상태 (ACTIVE, INACTIVE, SUSPENDED)',
    sales_start_date DATE COMMENT '판매 시작일',
    sales_end_date DATE COMMENT '판매 종료일',
    
    -- 상품 설명
    product_description TEXT COMMENT '상품 설명',
    product_features TEXT COMMENT '상품 특징',
    investment_strategy TEXT COMMENT '투자 전략',
    
    -- 메타데이터
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP COMMENT '마지막 동기화 시간',
    
    -- 인덱스
    INDEX idx_external_product_id (external_product_id),
    INDEX idx_product_code (product_code),
    INDEX idx_category_id (category_id),
    INDEX idx_product_type (product_type),
    INDEX idx_risk_level (risk_level),
    INDEX idx_product_status (product_status),
    INDEX idx_expected_return_rate (expected_return_rate),
    
    -- 외래키
    FOREIGN KEY (category_id) REFERENCES hana_bank_product_categories(category_id)
) COMMENT '하나은행 상품 정보';

-- 3. 상품 수익률 히스토리 테이블 (수익률 변화 추적)
CREATE TABLE hana_bank_product_rate_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '상품 ID',
    rate_date DATE NOT NULL COMMENT '수익률 기준일',
    base_rate DECIMAL(5,2) COMMENT '기본 금리 (%)',
    max_rate DECIMAL(5,2) COMMENT '최고 금리 (%)',
    actual_return_rate DECIMAL(5,2) COMMENT '실제 수익률 (%)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_product_id (product_id),
    INDEX idx_rate_date (rate_date),
    UNIQUE KEY uk_product_date (product_id, rate_date),
    
    FOREIGN KEY (product_id) REFERENCES hana_bank_products(product_id)
) COMMENT '상품 수익률 히스토리';

-- 초기 데이터 삽입

-- 상품 카테고리 초기 데이터
INSERT INTO hana_bank_product_categories (category_code, category_name, category_description, risk_level) VALUES
('DEPOSIT', '예금', '원금 보장형 예금 상품', 'LOW'),
('SAVINGS', '적금', '정기 적금 상품', 'LOW'),
('FUND', '펀드', '투자신탁 상품', 'MEDIUM'),
('BOND', '채권', '채권 투자 상품', 'LOW'),
('STOCK', '주식', '주식 투자 상품', 'HIGH'),
('IRP', 'IRP', '개인형퇴직연금 상품', 'MEDIUM'),
('ETF', 'ETF', '상장지수펀드 상품', 'MEDIUM'),
('REIT', 'REIT', '부동산투자신탁 상품', 'MEDIUM');

-- 실제 하나은행 상품 데이터 (2024년 기준)
INSERT INTO hana_bank_products (
    external_product_id, 
    product_code, 
    product_name, 
    product_sub_name, 
    category_id, 
    product_type,
    base_rate,
    max_rate,
    expected_return_rate,
    min_investment_amount,
    max_investment_amount,
    monthly_investment_amount,
    min_period_months,
    max_period_months,
    maturity_period_months,
    risk_level,
    risk_score,
    is_tax_free,
    is_guaranteed,
    product_description
) VALUES
-- 적금 상품들
('HANA_SAVINGS_001', 'SAV001', '하나 프리미엄 적금', '신혼부부 특별', 2, 'SAVINGS', 3.2, 4.1, 3.65, 100000, 1000000, 500000, 12, 60, 36, 'LOW', 2, TRUE, TRUE, '신혼부부를 위한 특별 적금 상품으로 비과세 혜택 제공'),
('HANA_SAVINGS_002', 'SAV002', '하나 꿈나무 적금', '자녀교육비 마련', 2, 'SAVINGS', 3.0, 3.8, 3.4, 100000, 1000000, 300000, 12, 60, 48, 'LOW', 2, TRUE, TRUE, '자녀교육비 마련을 위한 장기 적금 상품'),
('HANA_SAVINGS_003', 'SAV003', '하나 첫집마련 적금', '주택구입자금', 2, 'SAVINGS', 3.5, 4.3, 3.9, 100000, 2000000, 500000, 12, 60, 36, 'LOW', 2, TRUE, TRUE, '첫집마련을 위한 특별 적금 상품'),
('HANA_SAVINGS_004', 'SAV004', '하나 노후준비 적금', '연금저축', 2, 'SAVINGS', 3.8, 4.5, 4.15, 100000, 1000000, 400000, 12, 60, 60, 'LOW', 2, TRUE, TRUE, '노후준비를 위한 장기 적금 상품'),
('HANA_SAVINGS_005', 'SAV005', '하나 글로벌 적금', '외화적금', 2, 'SAVINGS', 2.8, 3.5, 3.15, 100000, 1000000, 300000, 12, 60, 24, 'LOW', 2, FALSE, TRUE, '달러 기준 외화 적금 상품'),

-- 펀드 상품들
('HANA_FUND_001', 'FUND001', '하나 글로벌 성장 펀드', '글로벌 주식형', 3, 'FUND', NULL, NULL, 8.5, 1000000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 6, FALSE, FALSE, '글로벌 성장 기업에 투자하는 주식형 펀드'),
('HANA_FUND_002', 'FUND002', '하나 테마주 펀드', '테마주형', 3, 'FUND', NULL, NULL, 7.2, 500000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 5, FALSE, FALSE, 'AI, 바이오 등 테마주에 투자하는 펀드'),
('HANA_FUND_003', 'FUND003', '하나 배당성장 펀드', '배당성장형', 3, 'FUND', NULL, NULL, 6.8, 1000000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 5, FALSE, FALSE, '안정적인 배당과 성장을 추구하는 펀드'),
('HANA_FUND_004', 'FUND004', '하나 ESG 펀드', 'ESG 투자형', 3, 'FUND', NULL, NULL, 6.5, 500000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 5, FALSE, FALSE, '환경, 사회, 지배구조를 고려한 ESG 투자 펀드'),
('HANA_FUND_005', 'FUND005', '하나 글로벌 밸류 펀드', '밸류 투자형', 3, 'FUND', NULL, NULL, 7.8, 1000000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 6, FALSE, FALSE, '저평가된 글로벌 기업에 투자하는 밸류 펀드'),

-- 채권 상품들
('HANA_BOND_001', 'BOND001', '하나 안정 채권형 펀드', '채권형', 4, 'BOND', NULL, NULL, 4.2, 500000, NULL, NULL, NULL, NULL, NULL, 'LOW', 3, FALSE, FALSE, '안정적인 수익을 추구하는 채권형 펀드'),
('HANA_BOND_002', 'BOND002', '하나 국채 펀드', '국채형', 4, 'BOND', NULL, NULL, 3.8, 300000, NULL, NULL, NULL, NULL, NULL, 'LOW', 2, FALSE, FALSE, '국가채권에 투자하는 안전한 채권 펀드'),
('HANA_BOND_003', 'BOND003', '하나 회사채 펀드', '회사채형', 4, 'BOND', NULL, NULL, 4.5, 500000, NULL, NULL, NULL, NULL, NULL, 'LOW', 3, FALSE, FALSE, '우량 회사채에 투자하는 채권 펀드'),
('HANA_BOND_004', 'BOND004', '하나 글로벌 채권 펀드', '글로벌 채권형', 4, 'BOND', NULL, NULL, 4.8, 1000000, NULL, NULL, NULL, NULL, NULL, 'MEDIUM', 4, FALSE, FALSE, '글로벌 채권에 투자하는 다각화 펀드'),
('HANA_BOND_005', 'BOND005', '하나 인플레이션 연동 채권', '인플레이션 연동', 4, 'BOND', NULL, NULL, 5.2, 500000, NULL, NULL, NULL, NULL, NULL, 'LOW', 3, FALSE, FALSE, '인플레이션에 연동된 수익률을 제공하는 채권'),