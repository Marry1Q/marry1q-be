package com.marry1q.marry1qbe.domain.couple.controller;

import com.marry1q.marry1qbe.domain.couple.dto.response.CoupleResponse;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import com.marry1q.marry1qbe.domain.couple.dto.request.UpdateCoupleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "커플", description = "커플 정보 관련 API")
@RestController
@RequestMapping("/api/couple")
@RequiredArgsConstructor
@Slf4j
public class CoupleController {
    
    private final CoupleService coupleService;
    
    @Operation(summary = "내 커플 정보 조회", description = "현재 로그인한 사용자의 커플 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "커플 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "coupleId": 1,
                        "weddingDate": "2024-12-31",
                        "totalBudget": 50000000,
                        "coupleAccount": "123-456-789012",
                        "coupleCardNumber": "1234-5678-9012-3456",
                        "currentSpent": 15000000,
                        "urlSlug": "kim-lee-wedding",
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00",
                        "daysUntilWedding": 220,
                        "memberNames": ["김민수", "이지은"]
                      },
                      "message": "성공적으로 처리되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "UNAUTHORIZED",
                        "message": "인증되지 않은 사용자입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "커플 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "NO_COUPLE",
                        "message": "속해있는 커플이 없습니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/info")
    public CustomApiResponse<CoupleResponse> getCurrentCoupleInfo() {
        log.info("현재 사용자의 커플 정보 조회 요청");
        CoupleResponse coupleInfo = coupleService.getCurrentCoupleInfo();
        log.info("커플 정보 조회 완료 - coupleId: {}", coupleInfo.getCoupleId());
        return CustomApiResponse.success(coupleInfo);
    }
    
    @Operation(summary = "커플 정보 수정", description = "현재 로그인한 사용자의 커플 정보를 수정합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "커플 정보 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "coupleId": 1,
                        "weddingDate": "2024-12-31",
                        "totalBudget": 50000000,
                        "coupleAccount": "123-456-789012",
                        "coupleCardNumber": "1234-5678-9012-3456",
                        "currentSpent": 15000000,
                        "urlSlug": "kim-lee-wedding",
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00",
                        "daysUntilWedding": 220,
                        "memberNames": ["김민수", "이지은"]
                      },
                      "message": "성공적으로 처리되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVALID_REQUEST",
                        "message": "잘못된 요청입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "UNAUTHORIZED",
                        "message": "인증되지 않은 사용자입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "커플 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "NO_COUPLE",
                        "message": "속해있는 커플이 없습니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/info")
    public CustomApiResponse<CoupleResponse> updateCurrentCoupleInfo(@Valid @RequestBody UpdateCoupleRequest request) {
        log.info("현재 사용자의 커플 정보 수정 요청 - weddingDate: {}, totalBudget: {}", request.getWeddingDate(), request.getTotalBudget());
        CoupleResponse updatedCoupleInfo = coupleService.updateCurrentCoupleInfo(request);
        log.info("커플 정보 수정 완료 - coupleId: {}", updatedCoupleInfo.getCoupleId());
        return CustomApiResponse.success(updatedCoupleInfo);
    }
}
