package com.marry1q.marry1qbe.domain.couple.repository;

import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Marry1qCouple, Long> {
    
    /**
     * 커플 ID로 조회
     */
    Optional<Marry1qCouple> findByCoupleId(Long coupleId);
    
    /**
     * URL 슬러그로 조회
     */
    Optional<Marry1qCouple> findByUrlSlug(String urlSlug);
    
    /**
     * 커플 존재 여부 확인
     */
    boolean existsByCoupleId(Long coupleId);
    
}
