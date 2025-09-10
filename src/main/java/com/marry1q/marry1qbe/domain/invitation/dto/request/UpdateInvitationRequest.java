package com.marry1q.marry1qbe.domain.invitation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "청첩장 수정 요청")
public class UpdateInvitationRequest {
    
    @Schema(description = "청첩장 제목", example = "김민수 ♥ 이지은 결혼식에 초대합니다")
    private String title;
    
    @Schema(description = "초대 메시지", example = "저희 두 사람이 사랑으로 하나가 되는 소중한 자리에 오셔서 축복해 주시면 더없는 기쁨이겠습니다.")
    private String invitationMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "결혼일", example = "2024-12-31")
    private LocalDate weddingDate;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "결혼 시간", example = "14:00")
    private LocalTime weddingTime;
    
    @Schema(description = "결혼식장명", example = "그랜드 호텔 그랜드볼룸")
    private String weddingHall;
    
    @Schema(description = "결혼식장 주소", example = "서울시 강남구 테헤란로 123")
    private String venueAddress;
    
    @Schema(description = "마음 전할 곳 메시지", example = "마음만 받겠습니다.")
    private String accountMessage;
    
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
    
    @Schema(description = "메인 이미지 URL", example = "https://marry1q-bucket.s3.ap-northeast-2.amazonaws.com/invitations/1/main-image.jpg")
    private String mainImageUrl;
}
