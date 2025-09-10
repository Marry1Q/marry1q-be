package com.marry1q.marry1qbe.domain.customer.repository;

import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    Optional<Customer> findByCustomerEmail(String customerEmail);
    
    boolean existsByCustomerEmail(String customerEmail);
    
    Optional<Customer> findByCustomerEmailAndCustomerPw(String customerEmail, String customerPw);
    
    /**
     * 커플 ID로 고객 목록 조회
     */
    List<Customer> findByCoupleId(Long coupleId);
}
