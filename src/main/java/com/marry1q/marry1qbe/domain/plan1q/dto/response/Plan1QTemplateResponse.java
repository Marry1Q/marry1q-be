package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QTemplateResponse {
    
    private Long templateId;
    private String title;
    private String description;
    private String iconName; // 프론트엔드에서 iconMapping[iconName]으로 경로 조회
    private LocalDateTime createdAt;
    
    public static Plan1QTemplateResponse from(Plan1QTemplate template) {
        return Plan1QTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .title(template.getTitle())
                .description(template.getDescription())
                .iconName(template.getIconName())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
