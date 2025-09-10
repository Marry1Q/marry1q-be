package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiAIResponse {
    private List<Candidate> candidates;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Candidate {
        private Content content;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private List<Part> parts;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {
        private String text;
    }
}
