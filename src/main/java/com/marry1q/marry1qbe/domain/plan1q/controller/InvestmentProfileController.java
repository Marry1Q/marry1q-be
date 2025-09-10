package com.marry1q.marry1qbe.domain.plan1q.controller;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.InvestmentProfileSubmitRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.InvestmentProfileResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.InvestmentQuestionResponse;
import com.marry1q.marry1qbe.domain.plan1q.service.InvestmentProfileService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan1q/investment-profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "투자성향 검사", description = "투자성향 검사 관련 API")
@SecurityRequirement(name = "Bearer Authentication")
public class InvestmentProfileController {
    
    private final InvestmentProfileService investmentProfileService;
    
    /**
     * 투자성향 검사 질문 목록 조회
     */
    @GetMapping("/questions")
    @Operation(summary = "투자성향 검사 질문 목록 조회", description = "투자성향 검사에 필요한 질문과 답변 옵션을 조회합니다.")
    public ResponseEntity<CustomApiResponse<List<InvestmentQuestionResponse>>> getQuestions() {
        log.info("투자성향 검사 질문 목록 조회 API 호출");
        
        List<InvestmentQuestionResponse> questions = investmentProfileService.getQuestions();
        
        return ResponseEntity.ok(CustomApiResponse.success(questions, "투자성향 검사 질문 목록을 성공적으로 조회했습니다."));
    }
    
    /**
     * 투자성향 검사 결과 제출
     */
    @PostMapping("/submit")
    @Operation(summary = "투자성향 검사 결과 제출", description = "투자성향 검사 답변을 제출하고 결과를 저장합니다.")
    public ResponseEntity<CustomApiResponse<InvestmentProfileResponse>> submitProfile(
            @Valid @RequestBody InvestmentProfileSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        log.info("투자성향 검사 결과 제출 API 호출 - 사용자: {}", currentUserSeqNo);
        
        InvestmentProfileResponse profile = investmentProfileService.submitProfile(request, currentUserSeqNo);
        
        return ResponseEntity.ok(CustomApiResponse.success(profile, "투자성향 검사가 완료되었습니다."));
    }
    
    /**
     * 투자성향 프로필 조회
     */
    @GetMapping
    @Operation(summary = "투자성향 프로필 조회", description = "현재 사용자의 투자성향 프로필을 조회합니다.")
    public ResponseEntity<CustomApiResponse<InvestmentProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        log.info("투자성향 프로필 조회 API 호출 - 사용자: {}", currentUserSeqNo);
        
        InvestmentProfileResponse profile = investmentProfileService.getProfile(currentUserSeqNo);
        
        if (profile == null) {
            return ResponseEntity.ok(CustomApiResponse.success(null, "투자성향 검사 결과가 없습니다."));
        }
        
        return ResponseEntity.ok(CustomApiResponse.success(profile, "투자성향 프로필을 성공적으로 조회했습니다."));
    }
}
