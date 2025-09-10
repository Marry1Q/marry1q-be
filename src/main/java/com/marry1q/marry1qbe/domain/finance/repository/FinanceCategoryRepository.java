package com.marry1q.marry1qbe.domain.finance.repository;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, Long> {
    
    /**
     * 커플 ID로 카테고리 목록 조회
     */
    List<FinanceCategory> findByCoupleIdOrderByNameAsc(Long coupleId);
    
    /**
     * 커플 ID와 카테고리 이름으로 조회
     */
    Optional<FinanceCategory> findByCoupleIdAndName(Long coupleId, String name);
    
    /**
     * 커플 ID와 카테고리 이름으로 존재 여부 확인
     */
    boolean existsByCoupleIdAndName(Long coupleId, String name);
    
    /**
     * 예산이 설정되지 않은 카테고리 목록 조회
     */
    @Query("SELECT fc FROM FinanceCategory fc WHERE fc.coupleId = :coupleId " +
           "AND fc.financeCategoryId NOT IN " +
           "(SELECT cb.financeCategory.financeCategoryId FROM CategoryBudget cb WHERE cb.coupleId = :coupleId) " +
           "ORDER BY fc.name ASC")
    List<FinanceCategory> findUnassignedBudgetCategoriesByCoupleId(@Param("coupleId") Long coupleId);
}
