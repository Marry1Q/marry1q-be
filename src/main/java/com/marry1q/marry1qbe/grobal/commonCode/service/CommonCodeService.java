package com.marry1q.marry1qbe.grobal.commonCode.service;

import com.marry1q.marry1qbe.grobal.commonCode.entity.CommonCode;
import com.marry1q.marry1qbe.grobal.commonCode.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonCodeService {
    
    private final CommonCodeRepository commonCodeRepository;
    
    /**
     * 코드 그룹별 코드 목록 조회 (캐시 적용)
     */
    @Cacheable(value = "commonCodes", key = "#codeGroup")
    @Transactional(readOnly = true)
    public List<CommonCode> getCodesByGroup(String codeGroup) {
        log.info("공통 코드 조회 - 그룹: {}", codeGroup);
        return commonCodeRepository.findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(codeGroup);
    }
    
    /**
     * 코드 그룹별 코드 목록을 Map으로 조회
     */
    @Transactional(readOnly = true)
    public Map<String, String> getCodeMapByGroup(String codeGroup) {
        List<CommonCode> codes = getCodesByGroup(codeGroup);
        return codes.stream()
                .collect(Collectors.toMap(
                        CommonCode::getCodeValue,
                        CommonCode::getCodeName
                ));
    }
    
    /**
     * 특정 코드값의 코드명 조회
     */
    @Transactional(readOnly = true)
    public String getCodeName(String codeGroup, String codeValue) {
        return commonCodeRepository.findByCodeGroupAndCodeValueAndIsActiveTrue(codeGroup, codeValue)
                .map(CommonCode::getCodeName)
                .orElse(codeValue);
    }
    
    /**
     * 거래 타입 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getTransactionTypes() {
        return getCodesByGroup("TRANSACTION_TYPE");
    }
    
    /**
     * 리뷰 상태 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getReviewStatuses() {
        return getCodesByGroup("REVIEW_STATUS");
    }
    
    /**
     * 가계부 거래 타입 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getFinanceTransactionTypes() {
        return getCodesByGroup("FINANCE_TRANSACTION_TYPE");
    }
    
    /**
     * 은행 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getBankCodes() {
        return getCodesByGroup("BANK_CODE");
    }
    
    // 기존 메서드들
    public Optional<CommonCode> getCodeById(String codeId) {
        return commonCodeRepository.findByCodeIdAndIsActiveTrue(codeId);
    }
    
    public String getMessageByCodeId(String codeId) {
        return commonCodeRepository.findByCodeIdAndIsActiveTrue(codeId)
                .map(CommonCode::getCodeValue)
                .orElse("알 수 없는 오류가 발생했습니다.");
    }
    
    public boolean existsByCodeId(String codeId) {
        return commonCodeRepository.existsByCodeIdAndIsActiveTrue(codeId);
    }
    
    // Plan1Q 관련 메서드들
    
    /**
     * 투자성향 타입 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getInvestmentProfileTypes() {
        return getCodesByGroup("INVESTMENT_PROFILE_TYPE");
    }
    
    /**
     * Plan1Q 목표 상태 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getPlan1QGoalStatuses() {
        return getCodesByGroup("PLAN1Q_GOAL_STATUS");
    }
    
    /**
     * Plan1Q 상품 타입 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getPlan1QProductTypes() {
        return getCodesByGroup("PLAN1Q_PRODUCT_TYPE");
    }
    
    /**
     * 위험도 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getRiskLevels() {
        return getCodesByGroup("RISK_LEVEL");
    }
    
    /**
     * 투자성향 검사 질문 타입 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getInvestmentQuestionTypes() {
        return getCodesByGroup("INVESTMENT_QUESTION_TYPE");
    }
    
    /**
     * 투자성향 검사 질문 카테고리 코드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommonCode> getInvestmentQuestionCategories() {
        return getCodesByGroup("INVESTMENT_QUESTION_CATEGORY");
    }
    
    // 코드값으로 코드명 조회 메서드들
    
    /**
     * 투자성향 타입 코드명 조회
     */
    public String getInvestmentProfileTypeName(String codeValue) {
        return getCodeName("INVESTMENT_PROFILE_TYPE", codeValue);
    }
    
    /**
     * Plan1Q 목표 상태 코드명 조회
     */
    public String getPlan1QGoalStatusName(String codeValue) {
        return getCodeName("PLAN1Q_GOAL_STATUS", codeValue);
    }
    
    /**
     * Plan1Q 상품 타입 코드명 조회
     */
    public String getPlan1QProductTypeName(String codeValue) {
        return getCodeName("PLAN1Q_PRODUCT_TYPE", codeValue);
    }
    
    /**
     * 위험도 코드명 조회
     */
    public String getRiskLevelName(String codeValue) {
        return getCodeName("RISK_LEVEL", codeValue);
    }
    
    /**
     * 투자성향 검사 질문 타입 코드명 조회
     */
    public String getInvestmentQuestionTypeName(String codeValue) {
        return getCodeName("INVESTMENT_QUESTION_TYPE", codeValue);
    }
    
    /**
     * 투자성향 검사 질문 카테고리 코드명 조회
     */
    public String getInvestmentQuestionCategoryName(String codeValue) {
        return getCodeName("INVESTMENT_QUESTION_CATEGORY", codeValue);
    }
}
