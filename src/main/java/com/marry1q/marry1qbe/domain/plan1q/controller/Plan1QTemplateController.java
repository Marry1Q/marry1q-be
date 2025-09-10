package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.dto.response.Plan1QTemplateResponse;
import com.marry1q.marry1qbe.domain.plan1q.service.Plan1QTemplateService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plan1q")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan1Q", description = "Plan1Q 관련 API")
@SecurityRequirement(name = "Bearer Authentication")
public class Plan1QTemplateController {
    
    private final Plan1QTemplateService plan1QTemplateService;
    
    /**
     * Plan1Q 템플릿 목록 조회
     */
    @GetMapping("/templates")
    @Operation(summary = "Plan1Q 템플릿 목록 조회", description = "목표 선택 화면에서 사용할 Plan1Q 템플릿 목록을 조회합니다.")
    public ResponseEntity<CustomApiResponse<List<Plan1QTemplateResponse>>> getTemplates(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        log.info("Plan1Q 템플릿 목록 조회 API 호출 - 사용자: {}", currentUserSeqNo);
        
        List<Plan1QTemplateResponse> templates = plan1QTemplateService.getActiveTemplates();
        
        log.info("Plan1Q 템플릿 목록 조회 완료 - 템플릿 수: {}", templates.size());
        return ResponseEntity.ok(CustomApiResponse.success(templates, "Plan1Q 템플릿 목록을 조회했습니다."));
    }
}
