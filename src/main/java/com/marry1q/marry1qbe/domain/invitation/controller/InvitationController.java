package com.marry1q.marry1qbe.domain.invitation.controller;

import com.marry1q.marry1qbe.domain.invitation.dto.request.CreateInvitationRequest;
import com.marry1q.marry1qbe.domain.invitation.dto.request.UpdateInvitationRequest;
import com.marry1q.marry1qbe.domain.invitation.dto.response.InvitationResponse;

import com.marry1q.marry1qbe.domain.invitation.service.InvitationService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import com.marry1q.marry1qbe.grobal.util.FileValidationUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "청첩장", description = "청첩장 관련 API")
public class InvitationController {
    
    private final InvitationService invitationService;
    private final CoupleService coupleService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;
    
    // 청첩장 목록 조회
    @GetMapping
    @Operation(summary = "청첩장 목록 조회", description = "로그인한 사용자의 커플 ID로 청첩장 목록을 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public CustomApiResponse<List<InvitationResponse>> getInvitations() {
        String userSeqNo = securityUtil.getCurrentUserSeqNo();
        List<InvitationResponse> invitations = invitationService.getInvitationsByUserSeqNo(userSeqNo);
        return CustomApiResponse.success(invitations);
    }
    
    // 청첩장 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "청첩장 생성", 
        description = "새로운 청첩장을 생성합니다. 청첩장 정보와 메인 이미지를 함께 업로드할 수 있습니다.\n\n" +
                     "📝 **사용 방법:**\n" +
                     "1. request: 청첩장 정보 (JSON 문자열)\n" +
                     "2. mainImage: 메인 이미지 파일 (선택사항)\n\n" +
                     "✅ **지원 이미지 형식:** JPG, JPEG, PNG, GIF (최대 10MB)\n" +
                     "✅ **권장 이미지 크기:** 1920x1080px 이상"
    )
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "청첩장 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "invitationId": 1,
                        "coupleId": 1,
                        "title": "김민수 ♥ 이지은 결혼식에 초대합니다",
                        "mainImageUrl": "https://marry1q-invitation-images.s3.ap-northeast-2.amazonaws.com/invitations/1/main-image.jpg",
                        "createdAt": "2024-01-15T10:30:00"
                      },
                      "message": "청첩장이 생성되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터 또는 파일 형식",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "VALIDATION_ERROR",
                        "message": "청첩장 제목은 필수입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public CustomApiResponse<InvitationResponse> createInvitation(
            @Parameter(
                description = "청첩장 정보 (JSON 문자열)",
                required = true,
                example = "{\"title\":\"김민수 ♥ 이지은 결혼식에 초대합니다\",\"weddingDate\":\"2024-12-31\",\"weddingTime\":\"14:00\",\"weddingHall\":\"그랜드 호텔\",\"venueAddress\":\"서울시 강남구\",\"groomName\":\"김민수\",\"brideName\":\"이지은\"}"
            ) @RequestParam("request") String requestJson,
            @Parameter(
                description = "메인 이미지 파일 (선택사항)\n\n" +
                             "**지원 형식:** JPG, JPEG, PNG, GIF\n" +
                             "**최대 크기:** 10MB\n" +
                             "**권장 크기:** 1920x1080px 이상",
                required = false
            ) @RequestParam(value = "mainImage", required = false) MultipartFile mainImage) {
        
        // JSON 문자열을 객체로 파싱
        CreateInvitationRequest request;
        try {
            request = objectMapper.readValue(requestJson, CreateInvitationRequest.class);
        } catch (Exception e) {
            return CustomApiResponse.error("INVALID_REQUEST_FORMAT", "청첩장 정보 형식이 올바르지 않습니다: " + e.getMessage());
        }
        
        // 이미지 파일 검증
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                FileValidationUtil.validateImageFile(mainImage);
            } catch (IllegalArgumentException e) {
                return CustomApiResponse.error("FILE_VALIDATION_ERROR", e.getMessage());
            }
        }
        
        InvitationResponse invitation = invitationService.createInvitationWithImage(request, mainImage);
        return CustomApiResponse.success(invitation, "청첩장이 생성되었습니다.");
    }
    
    // 청첩장 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "청첩장 상세 조회", description = "청첩장 ID로 상세 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public CustomApiResponse<InvitationResponse> getInvitation(@PathVariable("id") Long invitationId) {
        InvitationResponse invitation = invitationService.getInvitation(invitationId);
        return CustomApiResponse.success(invitation);
    }
    
    // 청첩장 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "청첩장 수정", 
        description = "청첩장 정보와 메인 이미지를 수정합니다.\n\n" +
                     "📝 **사용 방법:**\n" +
                     "1. request: 청첩장 정보 (JSON 문자열)\n" +
                     "2. mainImage: 메인 이미지 파일 (선택사항)\n\n" +
                     "✅ **지원 이미지 형식:** JPG, JPEG, PNG, GIF (최대 10MB)\n" +
                     "✅ **권장 이미지 크기:** 1920x1080px 이상\n\n" +
                     "🔄 **이미지 처리:**\n" +
                     "- 새 이미지가 제공되면 기존 이미지를 S3에서 삭제하고 새 이미지로 교체\n" +
                     "- 새 이미지가 없으면 기존 이미지 유지"
    )
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "청첩장 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "invitationId": 1,
                        "coupleId": 1,
                        "title": "김민수 ♥ 이지은 결혼식에 초대합니다",
                        "mainImageUrl": "https://marry1q-invitation-images.s3.ap-northeast-2.amazonaws.com/invitations/1/main-image.jpg",
                        "updatedAt": "2024-01-15T10:30:00"
                      },
                      "message": "청첩장이 수정되었습니다.",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터 또는 파일 형식",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "VALIDATION_ERROR",
                        "message": "청첩장 제목은 필수입니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public CustomApiResponse<InvitationResponse> updateInvitation(
            @PathVariable("id") Long invitationId,
            @Parameter(
                description = "청첩장 정보 (JSON 문자열)",
                required = true,
                example = "{\"title\":\"김민수 ♥ 이지은 결혼식에 초대합니다\",\"weddingDate\":\"2024-12-31\",\"weddingTime\":\"14:00\",\"weddingHall\":\"그랜드 호텔\",\"venueAddress\":\"서울시 강남구\",\"groomName\":\"김민수\",\"brideName\":\"이지은\"}"
            ) @RequestParam("request") String requestJson,
            @Parameter(
                description = "메인 이미지 파일 (선택사항)\n\n" +
                             "**지원 형식:** JPG, JPEG, PNG, GIF\n" +
                             "**최대 크기:** 10MB\n" +
                             "**권장 크기:** 1920x1080px 이상",
                required = false
            ) @RequestParam(value = "mainImage", required = false) MultipartFile mainImage) {
        
        // JSON 문자열을 객체로 파싱
        UpdateInvitationRequest request;
        try {
            request = objectMapper.readValue(requestJson, UpdateInvitationRequest.class);
        } catch (Exception e) {
            return CustomApiResponse.error("INVALID_REQUEST_FORMAT", "청첩장 정보 형식이 올바르지 않습니다: " + e.getMessage());
        }
        
        // 이미지 파일 검증
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                FileValidationUtil.validateImageFile(mainImage);
            } catch (IllegalArgumentException e) {
                return CustomApiResponse.error("FILE_VALIDATION_ERROR", e.getMessage());
            }
        }
        
        InvitationResponse invitation = invitationService.updateInvitationWithImage(invitationId, request, mainImage);
        return CustomApiResponse.success(invitation, "청첩장이 수정되었습니다.");
    }
    
    // 청첩장 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "청첩장 삭제", description = "청첩장을 삭제합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public CustomApiResponse<Void> deleteInvitation(@PathVariable("id") Long invitationId) {
        invitationService.deleteInvitation(invitationId);
        return CustomApiResponse.success(null, "청첩장이 삭제되었습니다.");
    }
    

    
    // 대표 청첩장 설정
    @PutMapping("/{id}/representative")
    @Operation(summary = "대표 청첩장 설정", description = "해당 청첩장을 대표 청첩장으로 설정합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public CustomApiResponse<Void> setRepresentative(@PathVariable("id") Long invitationId) {
        invitationService.setRepresentative(invitationId);
        return CustomApiResponse.success(null, "대표 청첩장이 설정되었습니다.");
    }
    
    // 공개 청첩장 조회
    @GetMapping("/public/{coupleSlug}")
    @Operation(
        summary = "공개 청첩장 조회", 
        description = "커플 슬러그로 공개 청첩장을 조회합니다. 조회 시 조회수가 자동으로 증가합니다."
    )
    @PreAuthorize("permitAll()")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "공개 청첩장 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "invitationId": 1,
                        "coupleId": 1,
                        "title": "김민수 ♥ 이지은 결혼식에 초대합니다",
                        "invitationMessage": "저희 두 사람이 사랑의 이름으로 지켜나가고자 합니다. 바쁘시더라도 잠시 시간을 내어 축복해 주시면 감사하겠습니다.",
                        "weddingDate": "2024-12-31",
                        "weddingTime": "14:00",
                        "weddingHall": "그랜드 호텔 그랜드볼룸",
                        "venueAddress": "서울특별시 강남구 테헤란로 123",
                        "mainImageUrl": "https://marry1q-bucket.s3.ap-northeast-2.amazonaws.com/invitations/1/main-image.jpg",
                        "accountMessage": "마음만으로도 충분하시나, 더욱 간절한 마음으로 정성스럽게 준비했습니다.",
                        "totalViews": 150,
                        "isRepresentative": true,
                        "createdAt": "2024-01-15T10:30:00",
                        "updatedAt": "2024-01-15T10:30:00",
                        "groomName": "김민수",
                        "groomPhone": "010-1234-5678",
                        "groomFatherName": "김철수",
                        "groomMotherName": "이영희",
                        "groomAccount": "123-456-789012",
                        "brideName": "이지은",
                        "bridePhone": "010-9876-5432",
                        "brideFatherName": "이영수",
                        "brideMotherName": "박미영",
                        "brideAccount": "987-654-321098"
                      },
                      "message": "공개 청첩장 조회 성공",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "청첩장을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVITATION_NOT_FOUND",
                        "message": "청첩장이 존재하지 않습니다."
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "결혼식 종료로 인한 접근 제한",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.marry1q.marry1qbe.grobal.dto.CustomApiResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "success": false,
                      "error": {
                        "code": "INVITATION_ACCESS_DENIED",
                        "message": "결혼식이 종료되었습니다. 결혼일: 2024-12-31"
                      },
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public CustomApiResponse<InvitationResponse> getPublicInvitation(
            @Parameter(
                description = "커플 슬러그 (공개 URL에서 사용되는 고유 식별자)",
                example = "kim-lee-wedding",
                required = true
            ) @PathVariable String coupleSlug) {
        InvitationResponse invitation = invitationService.getPublicInvitationWithViewIncrement(coupleSlug);
        return CustomApiResponse.success(invitation);
    }
}
