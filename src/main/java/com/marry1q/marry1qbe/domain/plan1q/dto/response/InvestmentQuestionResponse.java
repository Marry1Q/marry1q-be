package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentQuestionResponse {
    
    private Long questionId;
    private String questionText;
    private String questionType;
    private String questionTypeName;
    private String category;
    private String categoryName;
    private Integer sortOrder;
    private List<AnswerOptionResponse> answerOptions;
    
    public static InvestmentQuestionResponse from(InvestmentQuestion question, 
                                                 com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService commonCodeService) {
        return InvestmentQuestionResponse.builder()
                .questionId(question.getQuestionId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .questionTypeName(question.getQuestionTypeName(commonCodeService))
                .category(question.getCategory())
                .categoryName(question.getCategoryName(commonCodeService))
                .sortOrder(question.getSortOrder())
                .answerOptions(question.getAnswerOptions().stream()
                        .filter(option -> option.getIsActive())
                        .map(option -> AnswerOptionResponse.from(option))
                        .collect(Collectors.toList()))
                .build();
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerOptionResponse {
        private Long optionId;
        private String optionText;
        private String optionValue;
        private Integer score;
        private Integer sortOrder;
        
        public static AnswerOptionResponse from(com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentAnswerOption option) {
            return AnswerOptionResponse.builder()
                    .optionId(option.getOptionId())
                    .optionText(option.getOptionText())
                    .optionValue(option.getOptionValue())
                    .score(option.getScore())
                    .sortOrder(option.getSortOrder())
                    .build();
        }
    }
}
