package com.marry1q.marry1qbe.domain.finance.service;

import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import com.marry1q.marry1qbe.domain.couple.repository.Marry1qCoupleRepository;
import com.marry1q.marry1qbe.domain.finance.dto.request.CreateCategoryBudgetRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateCategoryBudgetRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.BudgetOverviewResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryBudgetListResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryBudgetResponse;
import com.marry1q.marry1qbe.domain.finance.entity.CategoryBudget;
import com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory;
import com.marry1q.marry1qbe.domain.finance.exception.CategoryBudgetNotFoundException;
import com.marry1q.marry1qbe.domain.finance.exception.FinanceCategoryNotFoundException;
import com.marry1q.marry1qbe.domain.finance.repository.CategoryBudgetRepository;
import com.marry1q.marry1qbe.domain.finance.repository.FinanceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryBudgetService {
    
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final FinanceCategoryRepository financeCategoryRepository;
    private final Marry1qCoupleRepository marry1qCoupleRepository;
    
    /**
     * 카테고리별 예산 목록 조회
     */
    public CategoryBudgetListResponse getCategoryBudgets(Long coupleId) {
        List<CategoryBudget> categoryBudgets = categoryBudgetRepository.findByCoupleId(coupleId);
        
        List<CategoryBudgetResponse> responses = categoryBudgets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return CategoryBudgetListResponse.builder()
                .categoryBudgets(responses)
                .totalCount(responses.size())
                .build();
    }
    
    /**
     * 카테고리별 예산 단건 조회
     */
    public CategoryBudgetResponse getCategoryBudget(Long categoryBudgetId, Long coupleId) {
        CategoryBudget categoryBudget = categoryBudgetRepository.findById(categoryBudgetId)
                .orElseThrow(() -> new CategoryBudgetNotFoundException("카테고리별 예산을 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!categoryBudget.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리별 예산에 대한 권한이 없습니다.");
        }
        
        return convertToResponse(categoryBudget);
    }
    
    /**
     * 카테고리별 예산 생성
     */
    @Transactional
    public CategoryBudgetResponse createCategoryBudget(CreateCategoryBudgetRequest request, Long coupleId) {
        // 카테고리 존재 여부 확인
        FinanceCategory category = financeCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));
        
        // 카테고리가 해당 커플의 것인지 확인
        if (!category.getCoupleId().equals(coupleId)) {
            throw new FinanceCategoryNotFoundException("해당 커플의 카테고리가 아닙니다: " + request.getCategoryId());
        }
        
        // 이미 예산이 설정되어 있는지 확인
        if (categoryBudgetRepository.existsByCoupleIdAndFinanceCategoryFinanceCategoryId(coupleId, request.getCategoryId())) {
            throw new IllegalArgumentException("이미 예산이 설정된 카테고리입니다: " + request.getCategoryId());
        }
        
        // 카테고리별 예산 생성
        CategoryBudget categoryBudget = CategoryBudget.builder()
                .budgetAmount(request.getBudgetAmount())
                .spentAmount(BigDecimal.ZERO)
                .coupleId(coupleId)
                .financeCategory(category)
                .build();
        
        CategoryBudget savedCategoryBudget = categoryBudgetRepository.save(categoryBudget);
        
        return CategoryBudgetResponse.from(savedCategoryBudget);
    }
    
    /**
     * 카테고리별 예산 수정
     */
    @Transactional
    public CategoryBudgetResponse updateCategoryBudget(Long categoryBudgetId, UpdateCategoryBudgetRequest request, Long coupleId) {
        CategoryBudget categoryBudget = categoryBudgetRepository.findById(categoryBudgetId)
                .orElseThrow(() -> new CategoryBudgetNotFoundException("카테고리별 예산을 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!categoryBudget.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리별 예산에 대한 권한이 없습니다.");
        }
        
        // 예산 금액 수정
        categoryBudget.updateBudgetAmount(request.getBudgetAmount());
        
        CategoryBudget updatedCategoryBudget = categoryBudgetRepository.save(categoryBudget);
        
        return convertToResponse(updatedCategoryBudget);
    }
    
    /**
     * 카테고리별 예산 삭제
     */
    @Transactional
    public void deleteCategoryBudget(Long categoryBudgetId, Long coupleId) {
        CategoryBudget categoryBudget = categoryBudgetRepository.findById(categoryBudgetId)
                .orElseThrow(() -> new CategoryBudgetNotFoundException("카테고리별 예산을 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!categoryBudget.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리별 예산에 대한 권한이 없습니다.");
        }
        
        categoryBudgetRepository.delete(categoryBudget);
    }
    
    /**
     * 예산 대시보드 정보 조회
     */
    public BudgetOverviewResponse getBudgetOverview(Long coupleId) {
        // 전체 예산 조회
        Marry1qCouple couple = marry1qCoupleRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("커플 정보를 찾을 수 없습니다."));
        
        BigDecimal totalBudget = couple.getTotalBudget() != null ? couple.getTotalBudget() : BigDecimal.ZERO;
        
        // 카테고리별 예산 목록 조회
        List<CategoryBudget> categoryBudgets = categoryBudgetRepository.findByCoupleId(coupleId);
        
        // 총 지출 계산
        BigDecimal totalSpent = categoryBudgets.stream()
                .map(CategoryBudget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 남은 예산 계산
        BigDecimal remainingBudget = totalBudget.subtract(totalSpent);
        
        // 전체 사용률 계산
        double totalUsageRate = totalBudget.compareTo(BigDecimal.ZERO) > 0 
                ? totalSpent.divide(totalBudget, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : 0.0;
        
        // 카테고리별 예산 응답 변환
        List<CategoryBudgetResponse> categoryBudgetResponses = categoryBudgets.stream()
                .map(CategoryBudgetResponse::from)
                .collect(Collectors.toList());
        
        return BudgetOverviewResponse.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .remainingBudget(remainingBudget)
                .totalUsageRate(totalUsageRate)
                .categoryBudgets(categoryBudgetResponses)
                .build();
    }
    
    /**
     * 카테고리별 예산 존재 여부 확인
     */
    public boolean existsByCategoryId(Long categoryId) {
        return categoryBudgetRepository.existsByFinanceCategoryFinanceCategoryId(categoryId);
    }
    
    /**
     * 카테고리별 예산 조회
     */
    public Optional<CategoryBudget> findByCategoryId(Long categoryId) {
        return categoryBudgetRepository.findByFinanceCategoryFinanceCategoryId(categoryId);
    }
    
    /**
     * 지출 금액 증가
     */
    @Transactional
    public void increaseSpentAmount(Long categoryId, BigDecimal amount) {
        Optional<CategoryBudget> categoryBudgetOpt = categoryBudgetRepository.findByFinanceCategoryFinanceCategoryId(categoryId);
        
        if (categoryBudgetOpt.isPresent()) {
            CategoryBudget categoryBudget = categoryBudgetOpt.get();
            categoryBudget.increaseSpentAmount(amount);
            categoryBudgetRepository.save(categoryBudget);
        } else {
            log.info("카테고리 ID {}에 대한 예산이 설정되지 않았습니다. 지출 금액 증가를 건너뜁니다.", categoryId);
        }
    }
    
    /**
     * 지출 금액 감소
     */
    @Transactional
    public void decreaseSpentAmount(Long categoryId, BigDecimal amount) {
        Optional<CategoryBudget> categoryBudgetOpt = categoryBudgetRepository.findByFinanceCategoryFinanceCategoryId(categoryId);
        
        if (categoryBudgetOpt.isPresent()) {
            CategoryBudget categoryBudget = categoryBudgetOpt.get();
            categoryBudget.decreaseSpentAmount(amount);
            categoryBudgetRepository.save(categoryBudget);
        } else {
            log.info("카테고리 ID {}에 대한 예산이 설정되지 않았습니다. 지출 금액 감소를 건너뜁니다.", categoryId);
        }
    }
    
    /**
     * CategoryBudgetResponse로 변환
     */
    private CategoryBudgetResponse convertToResponse(CategoryBudget categoryBudget) {
        return CategoryBudgetResponse.from(categoryBudget);
    }
    
    /**
     * CategoryResponse로 변환
     */
    private com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse convertToCategoryResponse(FinanceCategory category) {
        return com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse.builder()
                .financeCategoryId(category.getFinanceCategoryId())
                .name(category.getName())
                .coupleId(category.getCoupleId())
                .build();
    }
}
