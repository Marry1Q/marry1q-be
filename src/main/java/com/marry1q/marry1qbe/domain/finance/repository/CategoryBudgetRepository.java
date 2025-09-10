package com.marry1q.marry1qbe.domain.finance.repository;

import com.marry1q.marry1qbe.domain.finance.entity.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    
    /**
     * 커플 ID로 카테고리별 예산 목록 조회
     */
    List<CategoryBudget> findByCoupleId(Long coupleId);
    
    /**
     * 카테고리 ID로 카테고리별 예산 조회
     */
    Optional<CategoryBudget> findByFinanceCategoryFinanceCategoryId(Long categoryId);
    
    /**
     * 커플 ID와 카테고리 ID로 카테고리별 예산 조회
     */
    Optional<CategoryBudget> findByCoupleIdAndFinanceCategoryFinanceCategoryId(Long coupleId, Long categoryId);
    
    /**
     * 카테고리별 예산 존재 여부 확인
     */
    boolean existsByFinanceCategoryFinanceCategoryId(Long categoryId);
    
    /**
     * 커플 ID와 카테고리 ID로 카테고리별 예산 존재 여부 확인
     */
    boolean existsByCoupleIdAndFinanceCategoryFinanceCategoryId(Long coupleId, Long categoryId);
    
    /**
     * 커플의 총 예산 조회
     */
    @Query("SELECT SUM(cb.budgetAmount) FROM CategoryBudget cb WHERE cb.coupleId = :coupleId")
    java.math.BigDecimal findTotalBudgetByCoupleId(@Param("coupleId") Long coupleId);
    
    /**
     * 커플의 총 지출 조회
     */
    @Query("SELECT SUM(cb.spentAmount) FROM CategoryBudget cb WHERE cb.coupleId = :coupleId")
    java.math.BigDecimal findTotalSpentByCoupleId(@Param("coupleId") Long coupleId);
}
