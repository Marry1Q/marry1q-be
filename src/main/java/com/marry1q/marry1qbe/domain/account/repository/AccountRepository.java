package com.marry1q.marry1qbe.domain.account.repository;

import com.marry1q.marry1qbe.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * 계좌번호로 계좌 조회
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * 사용자 고유번호로 계좌 목록 조회
     */
    List<Account> findByUserSeqNo(String userSeqNo);
    
    /**
     * 모임통장 여부로 계좌 목록 조회
     */
    List<Account> findByIsCoupleAccount(Boolean isCoupleAccount);
    
    /**
     * 사용자 고유번호와 모임통장 여부로 계좌 목록 조회
     */
    List<Account> findByUserSeqNoAndIsCoupleAccount(String userSeqNo, Boolean isCoupleAccount);
    
    /**
     * 사용자 고유번호와 계좌 타입으로 계좌 조회
     */
    Optional<Account> findByUserSeqNoAndAccountType(String userSeqNo, String accountType);
    
    /**
     * Plan1Q 상품 ID로 계좌 조회 (중복 검증용)
     */
    Optional<Account> findByPlan1qProductId(Long plan1qProductId);
}
