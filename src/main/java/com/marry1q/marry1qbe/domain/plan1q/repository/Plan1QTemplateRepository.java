package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Plan1QTemplateRepository extends JpaRepository<Plan1QTemplate, Long> {
    
    /**
     * 활성화된 템플릿 목록 조회 (생성일시 내림차순)
     */
    @Query("SELECT pt FROM Plan1QTemplate pt WHERE pt.isActive = true ORDER BY pt.createdAt DESC")
    List<Plan1QTemplate> findAllActiveTemplates();
    
    /**
     * 활성화된 템플릿 개수 조회
     */
    @Query("SELECT COUNT(pt) FROM Plan1QTemplate pt WHERE pt.isActive = true")
    long countActiveTemplates();
    
    /**
     * 템플릿 ID로 활성화된 템플릿 조회
     */
    Optional<Plan1QTemplate> findByTemplateIdAndIsActiveTrue(Long templateId);
}
