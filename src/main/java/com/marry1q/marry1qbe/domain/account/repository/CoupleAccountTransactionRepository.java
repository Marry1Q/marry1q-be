package com.marry1q.marry1qbe.domain.account.repository;

import com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoupleAccountTransactionRepository extends JpaRepository<CoupleAccountTransaction, Long> {
    
    /**
     * 계좌 ID로 거래내역 조회 (페이징 지원)
     */
    Page<CoupleAccountTransaction> findByAccountIdOrderByTransactionDateDescTransactionTimeDesc(Long accountId, Pageable pageable);
    
    /**
     * 계좌 ID로 거래내역 조회
     */
    List<CoupleAccountTransaction> findByAccountIdOrderByTransactionDateDescTransactionTimeDesc(Long accountId);
    
    /**
     * 계좌 ID와 거래 타입으로 거래내역 조회
     */
    List<CoupleAccountTransaction> findByAccountIdAndTypeOrderByTransactionDateDescTransactionTimeDesc(
            Long accountId, CoupleAccountTransaction.TransactionType type);
    
    /**
     * 계좌 ID와 날짜 범위로 거래내역 조회
     */
    @Query("SELECT t FROM CoupleAccountTransaction t WHERE t.accountId = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<CoupleAccountTransaction> findByAccountIdAndTransactionDateBetween(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 계좌 ID로 최근 거래내역 조회 (최대 10건)
     */
    @Query("SELECT t FROM CoupleAccountTransaction t WHERE t.accountId = :accountId " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<CoupleAccountTransaction> findRecentTransactionsByAccountId(@Param("accountId") Long accountId);
    
    /**
     * tranId로 거래내역 존재 여부 확인
     */
    boolean existsByTranId(String tranId);
    
    /**
     * tranId로 거래내역 조회
     */
    Optional<CoupleAccountTransaction> findByTranId(String tranId);
    
    /**
     * 거래일시+금액으로 중복 체크
     */
    boolean existsByTransactionDateAndTransactionTimeAndAmountAndAccountId(
            LocalDate transactionDate, LocalTime transactionTime, BigDecimal amount, Long accountId);
    
    /**
     * 계좌 ID와 최근 동기화 시간 이후 거래내역 조회
     */
    @Query("SELECT t FROM CoupleAccountTransaction t WHERE t.accountId = :accountId " +
           "AND t.createdAt > :lastSyncedAt " +
           "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<CoupleAccountTransaction> findByAccountIdAndCreatedAtAfter(
            @Param("accountId") Long accountId,
            @Param("lastSyncedAt") java.time.LocalDateTime lastSyncedAt);
}
