package com.marry1q.marry1qbe.domain.customer.controller;

import com.marry1q.marry1qbe.domain.customer.dto.PinVerificationRequest;
import com.marry1q.marry1qbe.domain.customer.dto.PinVerificationResponse;
import com.marry1q.marry1qbe.domain.customer.service.PinVerificationService;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "핀 번호 검증", description = "핀 번호 검증 API")
public class PinVerificationController {
    
    private final PinVerificationService pinVerificationService;
    
    /**
     * 핀 번호 검증
     */
    @PostMapping("/verify-pin")
    @Operation(summary = "핀 번호 검증", description = "사용자의 핀 번호를 검증합니다.")
    public ResponseEntity<CustomApiResponse<PinVerificationResponse>> verifyPin(
            @Valid @RequestBody PinVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("핀 번호 검증 API 호출 - 사용자: {}", userDetails.getUsername());
        
        try {
            // JWT에서 사용자 정보 추출
            String userSeqNo = userDetails.getUsername();
            
            // 핀 번호 검증
            PinVerificationResponse response = pinVerificationService.verifyPin(
                userSeqNo, 
                request.getPinNumber()
            );
            
            if (response.isValid()) {
                log.info("핀 번호 검증 성공 - 사용자: {}", userSeqNo);
                return ResponseEntity.ok(CustomApiResponse.success(response, "핀 번호가 올바릅니다."));
            } else {
                log.warn("핀 번호 검증 실패 - 사용자: {}", userSeqNo);
                return ResponseEntity.ok(CustomApiResponse.success(response, "핀 번호가 올바르지 않습니다."));
            }
            
        } catch (CustomException e) {
            log.error("핀 번호 검증 API 오류 - 사용자: {}, 오류: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("핀 번호 검증 API 예상치 못한 오류 - 사용자: {}, 오류: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(CustomApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "핀 번호 검증 중 오류가 발생했습니다."));
        }
    }
}
