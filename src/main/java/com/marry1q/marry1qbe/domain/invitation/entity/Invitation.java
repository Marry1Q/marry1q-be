package com.marry1q.marry1qbe.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "marry1q_invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "inviatation_message", columnDefinition = "TEXT")
    private String invitationMessage;
    
    @Column(name = "wedding_date", nullable = false)
    private LocalDate weddingDate;
    
    @Column(name = "wedding_time", nullable = false)
    private LocalTime weddingTime;
    
    @Column(name = "wedding_hall", nullable = false, length = 255)
    private String weddingHall;
    
    @Column(name = "venue_address", nullable = false, columnDefinition = "TEXT")
    private String venueAddress;
    
    @Column(name = "venue_latitude")
    private Double venueLatitude;
    
    @Column(name = "venue_longitude")
    private Double venueLongitude;
    
    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;
    
    @Column(name = "account_message", columnDefinition = "TEXT")
    private String accountMessage;
    
    @Column(name = "total_views", nullable = false)
    private Integer totalViews = 0;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 신랑 정보
    @Column(name = "groom_name", nullable = false, length = 100)
    private String groomName;
    
    @Column(name = "groom_phone", length = 20)
    private String groomPhone;
    
    @Column(name = "groom_father_name", length = 100)
    private String groomFatherName;
    
    @Column(name = "groom_mother_name", length = 100)
    private String groomMotherName;
    
    @Column(name = "groom_account", length = 100)
    private String groomAccount;
    
    // 신부 정보
    @Column(name = "bride_name", nullable = false, length = 100)
    private String brideName;
    
    @Column(name = "bride_phone", length = 20)
    private String bridePhone;
    
    @Column(name = "bride_father_name", length = 100)
    private String brideFatherName;
    
    @Column(name = "bride_mother_name", length = 100)
    private String brideMotherName;
    
    @Column(name = "bride_account", length = 100)
    private String brideAccount;
    
    // 모임통장 정보
    @Column(name = "meeting_account_info", length = 255)
    private String meetingAccountInfo;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 생성자
    public Invitation(Long coupleId, String title, String invitationMessage, LocalDate weddingDate, 
                     LocalTime weddingTime, String weddingHall, String venueAddress, Double venueLatitude, Double venueLongitude, String accountMessage,
                     String groomName, String groomPhone, String groomFatherName, String groomMotherName, String groomAccount,
                     String brideName, String bridePhone, String brideFatherName, String brideMotherName, String brideAccount,
                     String mainImageUrl, String meetingAccountInfo) {
        this.coupleId = coupleId;
        this.title = title;
        this.invitationMessage = invitationMessage;
        this.weddingDate = weddingDate;
        this.weddingTime = weddingTime;
        this.weddingHall = weddingHall;
        this.venueAddress = venueAddress;
        this.venueLatitude = venueLatitude;
        this.venueLongitude = venueLongitude;
        this.mainImageUrl = mainImageUrl;
        this.accountMessage = accountMessage;
        this.groomName = groomName;
        this.groomPhone = groomPhone;
        this.groomFatherName = groomFatherName;
        this.groomMotherName = groomMotherName;
        this.groomAccount = groomAccount;
        this.brideName = brideName;
        this.bridePhone = bridePhone;
        this.brideFatherName = brideFatherName;
        this.brideMotherName = brideMotherName;
        this.brideAccount = brideAccount;
        this.meetingAccountInfo = meetingAccountInfo;
    }
    
    // 대표 청첩장 설정 메서드
    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }
    
    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.totalViews++;
    }
    
    // 결혼일 이후 접근 가능 여부 확인
    public boolean isAccessibleAfterWedding() {
        return LocalDate.now().isBefore(weddingDate);
    }
    
    // 메인 이미지 URL 설정
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }
    
    // 청첩장 정보 업데이트
    public void update(String title, String invitationMessage, LocalDate weddingDate, 
                      LocalTime weddingTime, String weddingHall, String venueAddress, Double venueLatitude, Double venueLongitude, String accountMessage,
                      String groomName, String groomPhone, String groomFatherName, String groomMotherName, String groomAccount,
                      String brideName, String bridePhone, String brideFatherName, String brideMotherName, String brideAccount,
                      String mainImageUrl, String meetingAccountInfo) {
        this.title = title;
        this.invitationMessage = invitationMessage;
        this.weddingDate = weddingDate;
        this.weddingTime = weddingTime;
        this.weddingHall = weddingHall;
        this.venueAddress = venueAddress;
        this.venueLatitude = venueLatitude;
        this.venueLongitude = venueLongitude;
        this.mainImageUrl = mainImageUrl;
        this.accountMessage = accountMessage;
        this.groomName = groomName;
        this.groomPhone = groomPhone;
        this.groomFatherName = groomFatherName;
        this.groomMotherName = groomMotherName;
        this.groomAccount = groomAccount;
        this.brideName = brideName;
        this.bridePhone = bridePhone;
        this.brideFatherName = brideFatherName;
        this.brideMotherName = brideMotherName;
        this.brideAccount = brideAccount;
        this.meetingAccountInfo = meetingAccountInfo;
    }
}
