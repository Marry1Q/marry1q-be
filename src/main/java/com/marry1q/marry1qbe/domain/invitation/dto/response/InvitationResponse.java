package com.marry1q.marry1qbe.domain.invitation.dto.response;

import com.marry1q.marry1qbe.domain.invitation.entity.Invitation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@Schema(description = "청첩장 응답")
public class InvitationResponse {
    
    @Schema(description = "청첩장 ID", example = "1")
    private Long invitationId;
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "청첩장 제목", example = "김민수 ♥ 이지은 결혼식에 초대합니다")
    private String title;
    
    @Schema(description = "초대 메시지")
    private String invitationMessage;
    
    @Schema(description = "결혼일", example = "2024-12-31")
    private LocalDate weddingDate;
    
    @Schema(description = "결혼 시간", example = "14:00")
    private LocalTime weddingTime;
    
    @Schema(description = "결혼식장명", example = "그랜드 호텔 그랜드볼룸")
    private String weddingHall;
    
    @Schema(description = "결혼식장 주소")
    private String venueAddress;
    
    @Schema(description = "메인 이미지 URL")
    private String mainImageUrl;
    
    @Schema(description = "마음 전할 곳 메시지")
    private String accountMessage;
    
    @Schema(description = "총 조회수", example = "150")
    private Integer totalViews;
    
    @Schema(description = "대표 청첩장 여부", example = "true")
    private Boolean isRepresentative;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
    
    // 신랑 정보
    @Schema(description = "신랑 이름", example = "김민수")
    private String groomName;
    
    @Schema(description = "신랑 연락처", example = "010-1234-5678")
    private String groomPhone;
    
    @Schema(description = "신랑 아버지 이름", example = "김철수")
    private String groomFatherName;
    
    @Schema(description = "신랑 어머니 이름", example = "이영희")
    private String groomMotherName;
    
    @Schema(description = "신랑 계좌번호", example = "123-456-789012")
    private String groomAccount;
    
    // 신부 정보
    @Schema(description = "신부 이름", example = "이지은")
    private String brideName;
    
    @Schema(description = "신부 연락처", example = "010-9876-5432")
    private String bridePhone;
    
    @Schema(description = "신부 아버지 이름", example = "이영수")
    private String brideFatherName;
    
    @Schema(description = "신부 어머니 이름", example = "박미영")
    private String brideMotherName;
    
    @Schema(description = "신부 계좌번호", example = "987-654-321098")
    private String brideAccount;
    
    public static InvitationResponse from(Invitation invitation) {
        return InvitationResponse.builder()
                .invitationId(invitation.getInvitationId())
                .coupleId(invitation.getCoupleId())
                .title(invitation.getTitle())
                .invitationMessage(invitation.getInvitationMessage())
                .weddingDate(invitation.getWeddingDate())
                .weddingTime(invitation.getWeddingTime())
                .weddingHall(invitation.getWeddingHall())
                .venueAddress(invitation.getVenueAddress())
                .mainImageUrl(invitation.getMainImageUrl())
                .accountMessage(invitation.getAccountMessage())
                .totalViews(invitation.getTotalViews())
                .isRepresentative(invitation.getIsRepresentative())
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt())
                .groomName(invitation.getGroomName())
                .groomPhone(invitation.getGroomPhone())
                .groomFatherName(invitation.getGroomFatherName())
                .groomMotherName(invitation.getGroomMotherName())
                .groomAccount(invitation.getGroomAccount())
                .brideName(invitation.getBrideName())
                .bridePhone(invitation.getBridePhone())
                .brideFatherName(invitation.getBrideFatherName())
                .brideMotherName(invitation.getBrideMotherName())
                .brideAccount(invitation.getBrideAccount())
                .build();
    }
}
