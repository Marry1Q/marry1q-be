package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentQuestionRepository extends JpaRepository<InvestmentQuestion, Long> {
    
    /**
     * 활성화된 질문 목록을 카테고리별로 조회
     */
    @Query("SELECT iq FROM InvestmentQuestion iq " +
           "WHERE iq.isActive = true " +
           "ORDER BY iq.category, iq.sortOrder")
    List<InvestmentQuestion> findAllActiveQuestions();
    
    /**
     * 특정 카테고리의 활성화된 질문 목록 조회
     */
    @Query("SELECT iq FROM InvestmentQuestion iq " +
           "WHERE iq.category = :category AND iq.isActive = true " +
           "ORDER BY iq.sortOrder")
    List<InvestmentQuestion> findByCategoryAndActive(@Param("category") String category);
    
    /**
     * 활성화된 질문 개수 조회
     */
    @Query("SELECT COUNT(iq) FROM InvestmentQuestion iq WHERE iq.isActive = true")
    long countActiveQuestions();
    
    /**
     * 질문 ID로 활성화된 질문 조회
     */
    Optional<InvestmentQuestion> findByQuestionIdAndIsActiveTrue(Long questionId);
}
