package com.marry1q.marry1qbe.domain.plan1q.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProfileSubmitRequest {
    
    @NotEmpty(message = "답변 목록은 필수입니다.")
    private List<AnswerRequest> answers;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        
        @NotNull(message = "질문 ID는 필수입니다.")
        private Long questionId;
        
        @NotNull(message = "답변은 필수입니다.")
        private String answer; // A, B, C, D
    }
}
