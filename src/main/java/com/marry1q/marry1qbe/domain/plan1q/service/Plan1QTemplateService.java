package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.dto.response.Plan1QTemplateResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QTemplate;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class Plan1QTemplateService {
    
    private final Plan1QTemplateRepository plan1QTemplateRepository;
    
    /**
     * 활성화된 템플릿 목록 조회
     */
    public List<Plan1QTemplateResponse> getActiveTemplates() {
        log.info("활성화된 Plan1Q 템플릿 목록 조회 시작");
        
        List<Plan1QTemplate> templates = plan1QTemplateRepository.findAllActiveTemplates();
        
        List<Plan1QTemplateResponse> responses = templates.stream()
                .map(Plan1QTemplateResponse::from)
                .collect(Collectors.toList());
        
        log.info("활성화된 Plan1Q 템플릿 목록 조회 완료 - 템플릿 수: {}", responses.size());
        return responses;
    }
    
    /**
     * 활성화된 템플릿 개수 조회
     */
    public long getActiveTemplateCount() {
        log.info("활성화된 Plan1Q 템플릿 개수 조회");
        long count = plan1QTemplateRepository.countActiveTemplates();
        log.info("활성화된 Plan1Q 템플릿 개수: {}", count);
        return count;
    }
}
