package com.marry1q.marry1qbe.domain.finance.repository;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {
    
    /**
     * 커플 ID로 거래내역 조회 (페이징)
     */
    Page<FinanceTransaction> findByCoupleIdOrderByTransactionDateDescTransactionTimeDesc(
            Long coupleId, Pageable pageable);
    
    /**
     * 커플 ID와 거래 타입으로 거래내역 조회
     */
    List<FinanceTransaction> findByCoupleIdAndTransactionTypeOrderByTransactionDateDescTransactionTimeDesc(
            Long coupleId, FinanceTransaction.TransactionType transactionType);
    
    /**
     * 커플 ID와 날짜 범위로 거래내역 조회
     */
    @Query("SELECT t FROM FinanceTransaction t WHERE t.coupleId = :coupleId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<FinanceTransaction> findByCoupleIdAndTransactionDateBetween(
            @Param("coupleId") Long coupleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 커플 ID로 최근 거래내역 조회 (최대 10건)
     */
    @Query("SELECT t FROM FinanceTransaction t WHERE t.coupleId = :coupleId " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<FinanceTransaction> findRecentTransactionsByCoupleId(@Param("coupleId") Long coupleId);
    

    
    /**
     * 검색 조건으로 거래내역 조회 (사용자 이름, 카테고리 정보 포함)
     */
    @Query("SELECT t, c.customerName, fc.name, fc.iconName, fc.colorName FROM FinanceTransaction t " +
           "JOIN com.marry1q.marry1qbe.domain.customer.entity.Customer c ON t.userSeqNo = c.userSeqNo " +
           "JOIN t.financeCategory fc " +
           "WHERE t.coupleId = :coupleId " +
           "AND (:categoryId IS NULL OR fc.financeCategoryId = :categoryId) " +
           "AND (:userSeqNo IS NULL OR t.userSeqNo = :userSeqNo) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:searchTerm IS NULL OR t.description LIKE %:searchTerm% OR t.memo LIKE %:searchTerm%) " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    Page<Object[]> findBySearchConditionsWithUserName(
            @Param("coupleId") Long coupleId,
            @Param("categoryId") Long categoryId,
            @Param("userSeqNo") String userSeqNo,
            @Param("transactionType") FinanceTransaction.TransactionType transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    /**
     * 카테고리별 총 지출 금액 조회
     */
    @Query("SELECT t.financeCategory.financeCategoryId, SUM(t.amount) " +
           "FROM FinanceTransaction t " +
           "WHERE t.coupleId = :coupleId AND t.transactionType = 'EXPENSE' " +
           "GROUP BY t.financeCategory.financeCategoryId")
    List<Object[]> findTotalSpentByCategory(@Param("coupleId") Long coupleId);
}
