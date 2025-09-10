package com.marry1q.marry1qbe.domain.finance.service;

import com.marry1q.marry1qbe.domain.finance.dto.request.CreateCategoryRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateCategoryRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryListResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse;
import com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory;
import com.marry1q.marry1qbe.domain.finance.exception.FinanceCategoryNotFoundException;
import com.marry1q.marry1qbe.domain.finance.repository.FinanceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceCategoryService {
    
    private final FinanceCategoryRepository financeCategoryRepository;
    
    /**
     * 카테고리 목록 조회
     */
    public CategoryListResponse getCategories(Long coupleId) {
        List<FinanceCategory> categories = financeCategoryRepository.findByCoupleIdOrderByNameAsc(coupleId);
        
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount(categoryResponses.size())
                .build();
    }
    
    /**
     * 예산이 설정되지 않은 카테고리 목록 조회
     */
    public CategoryListResponse getUnassignedBudgetCategories(Long coupleId) {
        List<FinanceCategory> categories = financeCategoryRepository.findUnassignedBudgetCategoriesByCoupleId(coupleId);
        
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount(categoryResponses.size())
                .build();
    }
    
    /**
     * 카테고리 단건 조회
     */
    public CategoryResponse getCategory(Long categoryId, Long coupleId) {
        FinanceCategory category = financeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!category.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리에 대한 권한이 없습니다.");
        }
        
        return convertToResponse(category);
    }
    
    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, Long coupleId) {
        // 카테고리명 중복 확인
        if (financeCategoryRepository.existsByCoupleIdAndName(coupleId, request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다.");
        }
        
        // 카테고리 생성
        FinanceCategory category = FinanceCategory.builder()
                .name(request.getName())
                .coupleId(coupleId)
                .iconName(request.getIconName())
                .colorName(request.getColorName())
                .build();
        
        FinanceCategory savedCategory = financeCategoryRepository.save(category);
        
        return convertToResponse(savedCategory);
    }
    
    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request, Long coupleId) {
        FinanceCategory category = financeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!category.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리에 대한 권한이 없습니다.");
        }
        
        // 카테고리명 중복 확인 (자신 제외)
        if (!category.getName().equals(request.getName()) && 
            financeCategoryRepository.existsByCoupleIdAndName(coupleId, request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다.");
        }
        
        // 카테고리 수정
        category = FinanceCategory.builder()
                .financeCategoryId(category.getFinanceCategoryId())
                .name(request.getName())
                .coupleId(category.getCoupleId())
                .iconName(request.getIconName())
                .colorName(request.getColorName())
                .createdAt(category.getCreatedAt())
                .build();
        
        FinanceCategory updatedCategory = financeCategoryRepository.save(category);
        
        return convertToResponse(updatedCategory);
    }
    
    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long coupleId) {
        FinanceCategory category = financeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!category.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 카테고리에 대한 권한이 없습니다.");
        }
        
        // TODO: 해당 카테고리를 사용하는 거래 내역이 있는지 확인
        // TODO: 거래 내역이 있다면 삭제 불가능하도록 처리
        
        financeCategoryRepository.delete(category);
    }
    
    /**
     * 카테고리 존재 여부 확인
     */
    public boolean existsByCategoryId(Long categoryId) {
        return financeCategoryRepository.existsById(categoryId);
    }
    
    /**
     * CategoryResponse로 변환
     */
    private CategoryResponse convertToResponse(FinanceCategory category) {
        return CategoryResponse.builder()
                .financeCategoryId(category.getFinanceCategoryId())
                .name(category.getName())
                .coupleId(category.getCoupleId())
                .iconName(category.getIconName())
                .colorName(category.getColorName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
