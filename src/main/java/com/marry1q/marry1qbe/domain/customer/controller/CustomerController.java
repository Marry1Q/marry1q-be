package com.marry1q.marry1qbe.domain.customer.controller;

import com.marry1q.marry1qbe.domain.customer.dto.CustomerInfoResponse;
import com.marry1q.marry1qbe.domain.customer.dto.LoginRequest;
import com.marry1q.marry1qbe.domain.customer.dto.LoginResponse;
import com.marry1q.marry1qbe.domain.customer.dto.SignUpRequest;
import com.marry1q.marry1qbe.domain.customer.service.CustomerService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PreAuthorize("permitAll()")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": null,
                      "message": "성공적으로 처리되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "회원가입 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "DUPLICATE_EMAIL",
                        "message": "이미 사용 중인 이메일입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/signup")
    public CustomApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        customerService.signUp(request);
        return CustomApiResponse.success(null, "회원가입이 완료되었습니다.");
    }
    
    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다.")
    @PreAuthorize("permitAll()")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 2592000
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
            description = "로그인 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVALID_PASSWORD",
                        "message": "비밀번호가 일치하지 않습니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/login")
    public CustomApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = customerService.login(request);
        return CustomApiResponse.success(response, "로그인이 완료되었습니다.");
    }
    
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 토큰을 블랙리스트에 추가합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": null,
                      "message": "성공적으로 처리되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/logout")
    public CustomApiResponse<Void> logout(Authentication authentication) {
        customerService.logout(authentication.getName());
        return CustomApiResponse.success(null, "로그아웃이 완료되었습니다.");
    }
    
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "내 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "userSeqNo": "1234567890",
                        "customerName": "홍길동",
                        "customerPhone": "010-1234-5678",
                        "customerEmail": "kim@example.com",
                        "coupleId": 1,
                        "coupleSlug": "couple-123",
                        "createdAt": "2024-01-15T10:30:00",
                        "updatedAt": "2024-01-15T10:30:00"
                      },
                      "message": "성공적으로 처리되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/me")
    public CustomApiResponse<CustomerInfoResponse> getCustomerInfo(Authentication authentication) {
        CustomerInfoResponse response = customerService.getCustomerInfo(authentication.getName());
        return CustomApiResponse.success(response);
    }
    
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @PreAuthorize("permitAll()")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 2592000
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
            description = "토큰 갱신 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVALID_TOKEN",
                        "message": "유효하지 않은 토큰입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/refresh")
    public CustomApiResponse<LoginResponse> refreshToken(
            @Parameter(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestParam String refreshToken) {
        LoginResponse response = customerService.refreshToken(refreshToken);
        return CustomApiResponse.success(response, "토큰이 갱신되었습니다.");
    }
}
