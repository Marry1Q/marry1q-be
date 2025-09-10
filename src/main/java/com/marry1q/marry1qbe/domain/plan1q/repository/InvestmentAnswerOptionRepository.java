package com.marry1q.marry1qbe.domain.plan1q.repository;

import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentAnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentAnswerOptionRepository extends JpaRepository<InvestmentAnswerOption, Long> {
    
    /**
     * 질문 ID로 활성화된 답변 옵션 목록 조회
     */
    @Query("SELECT iao FROM InvestmentAnswerOption iao " +
           "WHERE iao.question.questionId = :questionId AND iao.isActive = true " +
           "ORDER BY iao.sortOrder")
    List<InvestmentAnswerOption> findByQuestionIdAndActive(@Param("questionId") Long questionId);
    
    /**
     * 질문 ID와 옵션 값으로 답변 옵션 조회
     */
    @Query("SELECT iao FROM InvestmentAnswerOption iao " +
           "WHERE iao.question.questionId = :questionId AND iao.optionValue = :optionValue AND iao.isActive = true")
    Optional<InvestmentAnswerOption> findByQuestionIdAndOptionValue(@Param("questionId") Long questionId, 
                                                                   @Param("optionValue") String optionValue);
    
    /**
     * 질문 ID로 모든 답변 옵션 조회 (활성화 여부 상관없이)
     */
    List<InvestmentAnswerOption> findByQuestionQuestionIdOrderBySortOrder(Long questionId);
}
