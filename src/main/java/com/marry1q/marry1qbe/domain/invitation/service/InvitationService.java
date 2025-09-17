package com.marry1q.marry1qbe.domain.invitation.service;

import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import com.marry1q.marry1qbe.domain.couple.repository.CoupleRepository;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.domain.invitation.dto.request.CreateInvitationRequest;
import com.marry1q.marry1qbe.domain.invitation.dto.request.UpdateInvitationRequest;
import com.marry1q.marry1qbe.domain.invitation.dto.response.InvitationResponse;

import com.marry1q.marry1qbe.domain.invitation.entity.Invitation;
import com.marry1q.marry1qbe.domain.invitation.exception.*;
import com.marry1q.marry1qbe.domain.invitation.repository.InvitationRepository;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {
    
    private final InvitationRepository invitationRepository;
    private final CoupleRepository coupleRepository;
    private final CustomerRepository customerRepository;
    private final S3Service s3Service;
    private final CoupleService coupleService;
    
    // 청첩장 목록 조회 (userSeqNo로)
    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitationsByUserSeqNo(String userSeqNo) {
        log.info("청첩장 목록 조회 시작 - userSeqNo: {}", userSeqNo);
        
        // 1. 사용자 정보 조회하여 coupleId 가져오기
        Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userSeqNo: " + userSeqNo));
        
        Long coupleId = customer.getCoupleId();
        if (coupleId == null) {
            throw new IllegalArgumentException("사용자가 커플에 속해있지 않습니다. userSeqNo: " + userSeqNo);
        }
        
        // 2. 커플 ID로 청첩장 목록 조회
        List<Invitation> invitations = invitationRepository.findByCoupleIdOrderByUpdatedAtDesc(coupleId);
        
        List<InvitationResponse> responses = invitations.stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
        
        log.info("청첩장 목록 조회 완료 - userSeqNo: {}, coupleId: {}, 개수: {}", userSeqNo, coupleId, responses.size());
        
        return responses;
    }
    
    // 청첩장 목록 조회
    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitations(Long coupleId) {
        log.info("청첩장 목록 조회 시작 - coupleId: {}", coupleId);
        
        List<Invitation> invitations = invitationRepository.findByCoupleIdOrderByUpdatedAtDesc(coupleId);
        
        List<InvitationResponse> responses = invitations.stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
        
        log.info("청첩장 목록 조회 완료 - coupleId: {}, 개수: {}", coupleId, responses.size());
        
        return responses;
    }
    
    // 청첩장 생성
    public InvitationResponse createInvitation(CreateInvitationRequest request) {
        log.info("청첩장 생성 시작");
        
        // 1. 현재 사용자의 coupleId 가져오기
        Long coupleId = coupleService.getCurrentCoupleId();
        
        // 2. 커플 정보 확인
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다."));
        
        // 3. 청첩장 엔티티 생성
        Invitation invitation = new Invitation(
                coupleId,
                request.getTitle(),
                request.getInvitationMessage(),
                request.getWeddingDate(),
                request.getWeddingTime(),
                request.getWeddingHall(),
                request.getVenueAddress(),
                request.getVenueLatitude(),
                request.getVenueLongitude(),
                request.getAccountMessage(),
                request.getGroomName(),
                request.getGroomPhone(),
                request.getGroomFatherName(),
                request.getGroomMotherName(),
                request.getGroomAccount(),
                request.getBrideName(),
                request.getBridePhone(),
                request.getBrideFatherName(),
                request.getBrideMotherName(),
                request.getBrideAccount(),
                null // mainImageUrl은 별도로 설정
        );
        
        // 4. 첫 번째 청첩장이면 대표 청첩장으로 설정
        long representativeCount = invitationRepository.countByCoupleIdAndIsRepresentativeTrue(coupleId);
        if (representativeCount == 0) {
            invitation.setRepresentative(true);
            log.info("첫 번째 청첩장을 대표 청첩장으로 설정 - coupleId: {}", coupleId);
        }
        
        // 5. 저장
        Invitation savedInvitation = invitationRepository.save(invitation);
        
        log.info("청첩장 생성 완료 - invitationId: {}", savedInvitation.getInvitationId());
        
        return InvitationResponse.from(savedInvitation);
    }
    
    // 청첩장 생성 (이미지 포함)
    public InvitationResponse createInvitationWithImage(CreateInvitationRequest request, MultipartFile mainImage) {
        log.info("=== InvitationService: 청첩장 생성 시작 (이미지 포함) ===");
        log.info("요청 데이터 상세 정보:");
        log.info("  - 제목: {}", request.getTitle());
        log.info("  - 결혼일: {}", request.getWeddingDate());
        log.info("  - 결혼시간: {}", request.getWeddingTime());
        log.info("  - 결혼식장: {}", request.getWeddingHall());
        log.info("  - 주소: {}", request.getVenueAddress());
        log.info("  - 위도: {}", request.getVenueLatitude());
        log.info("  - 경도: {}", request.getVenueLongitude());
        log.info("  - 신랑: {}", request.getGroomName());
        log.info("  - 신부: {}", request.getBrideName());
        log.info("  - 신랑 부: {}", request.getGroomFatherName());
        log.info("  - 신랑 모: {}", request.getGroomMotherName());
        log.info("  - 신부 부: {}", request.getBrideFatherName());
        log.info("  - 신부 모: {}", request.getBrideMotherName());
        log.info("  - 초대 메시지: {}", request.getInvitationMessage());
        log.info("  - 계좌 메시지: {}", request.getAccountMessage());
        log.info("메인 이미지: {}", mainImage != null ? "존재 (크기: " + mainImage.getSize() + " bytes)" : "없음");
        
        // 1. 현재 사용자의 coupleId 가져오기
        Long coupleId = coupleService.getCurrentCoupleId();
        log.info("현재 사용자 coupleId: {}", coupleId);
        
        // 2. 커플 정보 확인
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다."));
        
        // 3. 이미지 업로드 (있는 경우)
        String mainImageUrl = null;
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                mainImageUrl = uploadMainImageToS3(mainImage, coupleId);
                log.info("메인 이미지 업로드 완료 - URL: {}", mainImageUrl);
            } catch (Exception e) {
                log.error("메인 이미지 업로드 실패", e);
                throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }
        
        // 4. 청첩장 엔티티 생성
        Invitation invitation = new Invitation(
                coupleId,
                request.getTitle(),
                request.getInvitationMessage(),
                request.getWeddingDate(),
                request.getWeddingTime(),
                request.getWeddingHall(),
                request.getVenueAddress(),
                request.getVenueLatitude(),
                request.getVenueLongitude(),
                request.getAccountMessage(),
                request.getGroomName(),
                request.getGroomPhone(),
                request.getGroomFatherName(),
                request.getGroomMotherName(),
                request.getGroomAccount(),
                request.getBrideName(),
                request.getBridePhone(),
                request.getBrideFatherName(),
                request.getBrideMotherName(),
                request.getBrideAccount(),
                mainImageUrl
        );
        
        // 5. 첫 번째 청첩장이면 대표 청첩장으로 설정
        long representativeCount = invitationRepository.countByCoupleIdAndIsRepresentativeTrue(coupleId);
        if (representativeCount == 0) {
            invitation.setRepresentative(true);
            log.info("첫 번째 청첩장을 대표 청첩장으로 설정 - coupleId: {}", coupleId);
        }
        
        // 6. 저장
        Invitation savedInvitation = invitationRepository.save(invitation);
        
        log.info("청첩장 생성 완료 (이미지 포함) - invitationId: {}, mainImageUrl: {}", 
                 savedInvitation.getInvitationId(), mainImageUrl);
        
        // 특별 로깅: 사용자가 요청한 웨딩 데이터 추적
        logWeddingDataReceived(savedInvitation, "CREATE");
        
        return InvitationResponse.from(savedInvitation);
    }
    
    // S3에 메인 이미지 업로드 (private 메서드)
    private String uploadMainImageToS3(MultipartFile file, Long coupleId) {
        try {
            // UUID 기반 파일명으로 통일
            String uuid = UUID.randomUUID().toString();
            String extension = getFileExtension(file.getOriginalFilename());
            
            String fileName = String.format("invitations/%d/%s.%s", coupleId, uuid, extension);
            
            // S3에 업로드
            return s3Service.uploadFile(file, fileName);
            
        } catch (Exception e) {
            log.error("S3 이미지 업로드 실패", e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 청첩장 상세 조회
    @Transactional(readOnly = true)
    public InvitationResponse getInvitation(Long invitationId) {
        log.info("청첩장 상세 조회 시작 - invitationId: {}", invitationId);
        
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다."));
        
        log.info("청첩장 상세 조회 완료 - invitationId: {}", invitationId);
        
        return InvitationResponse.from(invitation);
    }
    
    // 청첩장 수정 (이미지 포함)
    public InvitationResponse updateInvitationWithImage(Long invitationId, UpdateInvitationRequest request, MultipartFile mainImage) {
        log.info("=== InvitationService: 청첩장 수정 시작 (이미지 포함) ===");
        log.info("수정 대상 invitationId: {}", invitationId);
        log.info("수정 요청 데이터 상세 정보:");
        log.info("  - 제목: {}", request.getTitle());
        log.info("  - 결혼일: {}", request.getWeddingDate());
        log.info("  - 결혼시간: {}", request.getWeddingTime());
        log.info("  - 결혼식장: {}", request.getWeddingHall());
        log.info("  - 주소: {}", request.getVenueAddress());
        log.info("  - 위도: {}", request.getVenueLatitude());
        log.info("  - 경도: {}", request.getVenueLongitude());
        log.info("  - 신랑: {}", request.getGroomName());
        log.info("  - 신부: {}", request.getBrideName());
        log.info("  - 신랑 부: {}", request.getGroomFatherName());
        log.info("  - 신랑 모: {}", request.getGroomMotherName());
        log.info("  - 신부 부: {}", request.getBrideFatherName());
        log.info("  - 신부 모: {}", request.getBrideMotherName());
        log.info("  - 초대 메시지: {}", request.getInvitationMessage());
        log.info("  - 계좌 메시지: {}", request.getAccountMessage());
        log.info("메인 이미지: {}", mainImage != null ? "존재 (크기: " + mainImage.getSize() + " bytes)" : "없음");
        
        // 1. 청첩장 조회
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다."));
        
        log.info("기존 청첩장 정보:");
        log.info("  - coupleId: {}", invitation.getCoupleId());
        log.info("  - 기존 제목: {}", invitation.getTitle());
        log.info("  - 기존 결혼일: {}", invitation.getWeddingDate());
        log.info("  - 기존 결혼시간: {}", invitation.getWeddingTime());
        log.info("  - 기존 결혼식장: {}", invitation.getWeddingHall());
        log.info("  - 기존 주소: {}", invitation.getVenueAddress());
        log.info("  - 기존 위도: {}", invitation.getVenueLatitude());
        log.info("  - 기존 경도: {}", invitation.getVenueLongitude());
        log.info("  - 기존 메인 이미지 URL: {}", invitation.getMainImageUrl());
        
        // 2. 기존 이미지 URL 저장 (롤백용)
        String originalImageUrl = invitation.getMainImageUrl();
        String newImageUrl = originalImageUrl;
        
        // 3. 새 이미지 업로드 (있는 경우)
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                newImageUrl = uploadMainImageToS3(mainImage, invitation.getCoupleId());
                log.info("새 메인 이미지 업로드 완료 - invitationId: {}, URL: {}", invitationId, newImageUrl);
            } catch (Exception e) {
                log.error("새 메인 이미지 업로드 실패 - invitationId: {}, error: {}", invitationId, e.getMessage());
                throw new RuntimeException("청첩장 수정 실패: 이미지 업로드에 실패했습니다. " + e.getMessage());
            }
        }
        
        // 4. 청첩장 정보 업데이트
        try {
            invitation.update(
                    request.getTitle(),
                    request.getInvitationMessage(),
                    request.getWeddingDate(),
                    request.getWeddingTime(),
                    request.getWeddingHall(),
                    request.getVenueAddress(),
                    request.getVenueLatitude(),
                    request.getVenueLongitude(),
                    request.getAccountMessage(),
                    request.getGroomName(),
                    request.getGroomPhone(),
                    request.getGroomFatherName(),
                    request.getGroomMotherName(),
                    request.getGroomAccount(),
                    request.getBrideName(),
                    request.getBridePhone(),
                    request.getBrideFatherName(),
                    request.getBrideMotherName(),
                    request.getBrideAccount(),
                    newImageUrl
            );
            
            Invitation updatedInvitation = invitationRepository.save(invitation);
            
            // 5. 기존 이미지 삭제 (새 이미지가 업로드된 경우)
            if (mainImage != null && !mainImage.isEmpty() && originalImageUrl != null) {
                log.info("=== 기존 이미지 삭제 프로세스 시작 ===");
                log.info("새 이미지 파일명: {}", mainImage.getOriginalFilename());
                log.info("새 이미지 크기: {} bytes", mainImage.getSize());
                log.info("기존 이미지 URL: {}", originalImageUrl);
                
                try {
                    log.info("기존 이미지 삭제 시도 - URL: {}", originalImageUrl);
                    String s3Key = extractS3KeyFromUrl(originalImageUrl);
                    log.info("S3Service.deleteFile() 호출 - s3Key: {}", s3Key);
                    s3Service.deleteFile(s3Key);
                    log.info("기존 메인 이미지 S3 삭제 완료 - invitationId: {}, s3Key: {}", invitationId, s3Key);
                } catch (Exception e) {
                    log.error("기존 메인 이미지 S3 삭제 실패 - invitationId: {}, URL: {}, error: {}", 
                            invitationId, originalImageUrl, e.getMessage());
                    log.error("삭제 실패 상세 정보:", e);
                    // 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
                }
            } else {
                log.info("기존 이미지 삭제 건너뜀 - mainImage: {}, originalImageUrl: {}", 
                        mainImage != null ? "존재" : "null", originalImageUrl);
            }
            
            log.info("청첩장 수정 완료 (이미지 포함) - invitationId: {}", invitationId);
            
            // 특별 로깅: 사용자가 요청한 웨딩 데이터 추적
            logWeddingDataReceived(updatedInvitation, "UPDATE");
            
            return InvitationResponse.from(updatedInvitation);
            
        } catch (Exception e) {
            // 6. 청첩장 정보 업데이트 실패 시 새로 업로드된 이미지 삭제
            if (mainImage != null && !mainImage.isEmpty() && !newImageUrl.equals(originalImageUrl)) {
                log.info("=== 롤백 프로세스 시작 ===");
                log.info("원본 이미지 URL: {}", originalImageUrl);
                log.info("새 이미지 URL: {}", newImageUrl);
                log.info("URL 비교 결과: {}", !newImageUrl.equals(originalImageUrl));
                
                try {
                    log.info("롤백: 새로 업로드된 이미지 삭제 시도 - URL: {}", newImageUrl);
                    String s3Key = extractS3KeyFromUrl(newImageUrl);
                    log.info("롤백: S3Service.deleteFile() 호출 - s3Key: {}", s3Key);
                    s3Service.deleteFile(s3Key);
                    log.info("롤백: 새로 업로드된 이미지 S3 삭제 완료 - invitationId: {}, s3Key: {}", invitationId, s3Key);
                } catch (Exception deleteException) {
                    log.error("롤백: 새로 업로드된 이미지 S3 삭제 실패 - invitationId: {}, URL: {}, error: {}", 
                            invitationId, newImageUrl, deleteException.getMessage());
                    log.error("롤백 삭제 실패 상세 정보:", deleteException);
                    // 롤백 실패는 로그만 남기고 계속 진행
                }
            } else {
                log.info("롤백 건너뜀 - mainImage: {}, newImageUrl: {}, originalImageUrl: {}, URL 동일: {}", 
                        mainImage != null ? "존재" : "null", newImageUrl, originalImageUrl, 
                        newImageUrl != null && newImageUrl.equals(originalImageUrl));
            }
            
            log.error("청첩장 수정 실패 - invitationId: {}, error: {}", invitationId, e.getMessage());
            throw new RuntimeException("청첩장 수정 실패: " + e.getMessage());
        }
    }
    
    // 청첩장 수정 (기존 메서드 - JSON만 처리)
    public InvitationResponse updateInvitation(Long invitationId, UpdateInvitationRequest request) {
        log.info("청첩장 수정 시작 - invitationId: {}", invitationId);
        
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다."));
        
        // 부분 업데이트
        if (request.getTitle() != null) invitation.update(
                request.getTitle(),
                request.getInvitationMessage(),
                request.getWeddingDate(),
                request.getWeddingTime(),
                request.getWeddingHall(),
                request.getVenueAddress(),
                request.getVenueLatitude(),
                request.getVenueLongitude(),
                request.getAccountMessage(),
                request.getGroomName(),
                request.getGroomPhone(),
                request.getGroomFatherName(),
                request.getGroomMotherName(),
                request.getGroomAccount(),
                request.getBrideName(),
                request.getBridePhone(),
                request.getBrideFatherName(),
                request.getBrideMotherName(),
                request.getBrideAccount(),
                request.getMainImageUrl()
        );
        
        Invitation updatedInvitation = invitationRepository.save(invitation);
        
        log.info("청첩장 수정 완료 - invitationId: {}", invitationId);
        
        return InvitationResponse.from(updatedInvitation);
    }
    
    // 청첩장 삭제
    public void deleteInvitation(Long invitationId) {
        log.info("청첩장 삭제 시작 - invitationId: {}", invitationId);
        
        // 1. 청첩장 조회
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다."));
        
        // 2. 삭제할 청첩장이 대표 청첩장인지 확인
        boolean isRepresentative = invitation.getIsRepresentative();
        
        // 3. S3 이미지 삭제 (있는 경우)
        String mainImageUrl = invitation.getMainImageUrl();
        if (mainImageUrl != null && !mainImageUrl.trim().isEmpty()) {
            try {
                log.info("청첩장 삭제: S3 이미지 삭제 시도 - invitationId: {}, URL: {}", invitationId, mainImageUrl);
                String s3Key = extractS3KeyFromUrl(mainImageUrl);
                s3Service.deleteFile(s3Key);
                log.info("청첩장 삭제: S3 이미지 삭제 완료 - invitationId: {}, s3Key: {}", invitationId, s3Key);
            } catch (Exception e) {
                log.error("청첩장 삭제: S3 이미지 삭제 실패 - invitationId: {}, URL: {}, error: {}", 
                        invitationId, mainImageUrl, e.getMessage());
                log.error("S3 이미지 삭제 실패 상세 정보:", e);
                // S3 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
            }
        } else {
            log.info("청첩장 삭제: 삭제할 S3 이미지가 없음 - invitationId: {}", invitationId);
        }
        
        // 4. 청첩장 삭제
        invitationRepository.delete(invitation);
        
        // 5. 삭제된 청첩장이 대표 청첩장이었다면 최근 수정된 청첩장으로 자동 설정
        if (isRepresentative) {
            try {
                setLatestInvitationAsRepresentative(invitation.getCoupleId());
                log.info("대표 청첩장 자동 설정 완료 - coupleId: {}", invitation.getCoupleId());
            } catch (Exception e) {
                log.info("대표 청첩장 자동 설정 실패 - coupleId: {}, error: {}", 
                        invitation.getCoupleId(), e.getMessage());
                throw new InvitationNotFoundException("대표 청첩장 자동 설정에 실패했습니다.");
            }
        }
        
        log.info("청첩장 삭제 완료 - invitationId: {}", invitationId);
    }
    
    // 메인 이미지 업로드
    public String uploadMainImage(MultipartFile file) {
        log.info("메인 이미지 업로드 시작");
        
        // 1. 현재 사용자의 coupleId 가져오기
        Long coupleId = coupleService.getCurrentCoupleId();
        
        // 2. 기존 이미지 삭제 (있다면)
        try {
            deleteMainImageInternal(coupleId);
        } catch (Exception e) {
            log.warn("기존 이미지 삭제 실패 (무시) - coupleId: {}, error: {}", coupleId, e.getMessage());
        }
        
        // 3. 새 이미지 업로드
        String imageUrl = uploadMainImageInternal(coupleId, file);
        
        log.info("메인 이미지 업로드 완료 - coupleId: {}, imageUrl: {}", coupleId, imageUrl);
        
        return imageUrl;
    }
    
    // 메인 이미지 삭제
    public void deleteMainImage() {
        log.info("메인 이미지 삭제 시작");
        
        // 1. 현재 사용자의 coupleId 가져오기
        Long coupleId = coupleService.getCurrentCoupleId();
        
        // 2. 이미지 삭제
        deleteMainImageInternal(coupleId);
        
        log.info("메인 이미지 삭제 완료 - coupleId: {}", coupleId);
    }
    
    // 대표 청첩장 설정
    public void setRepresentative(Long invitationId) {
        log.info("대표 청첩장 설정 시작 - invitationId: {}", invitationId);
        
        // 1. 청첩장 조회
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다."));
        
        // 2. 기존 대표 청첩장 해제
        List<Invitation> existingRepresentatives = invitationRepository.findByCoupleIdOrderByUpdatedAtDesc(invitation.getCoupleId())
                .stream()
                .filter(Invitation::getIsRepresentative)
                .collect(Collectors.toList());
        
        for (Invitation existing : existingRepresentatives) {
            existing.setRepresentative(false);
            invitationRepository.save(existing);
        }
        
        // 3. 새로운 대표 청첩장 설정
        invitation.setRepresentative(true);
        invitationRepository.save(invitation);
        
        log.info("대표 청첩장 설정 완료 - invitationId: {}", invitationId);
    }
    
    // 공개 청첩장 조회 (조회수 자동 증가)
    @Transactional
    public InvitationResponse getPublicInvitationWithViewIncrement(String coupleSlug) {
        try {
            // 1. 커플 정보 조회
            Marry1qCouple couple = coupleRepository.findByUrlSlug(coupleSlug)
                    .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다. URL 슬러그: " + coupleSlug));
            
            // 2. 대표 청첩장 조회
            Invitation invitation = invitationRepository.findByCoupleIdAndIsRepresentativeTrue(couple.getCoupleId())
                    .orElseThrow(() -> new InvitationNotFoundException("청첩장이 존재하지 않습니다. 커플 ID: " + couple.getCoupleId()));
            
            // 3. 결혼일 이후 접근 제한 확인
            if (!invitation.isAccessibleAfterWedding()) {
                throw new InvitationAccessDeniedException("결혼식이 종료되었습니다. 결혼일: " + invitation.getWeddingDate());
            }
            
            // 4. 조회수 증가 (동기 처리)
            try {
                int previousViews = invitation.getTotalViews();
                invitation.incrementViewCount();
                invitationRepository.save(invitation);
                log.info("조회수 증가 성공 - invitationId: {}, 이전 조회수: {}, 현재 조회수: {}", 
                        invitation.getInvitationId(), previousViews, invitation.getTotalViews());
                
                // 특별 로깅: 조회수 증가 시 웨딩 데이터 추적
                logWeddingDataReceived(invitation, "VIEW_INCREMENT");
            } catch (Exception e) {
                // 조회수 증가 실패 시 로그만 남기고 계속 진행
                log.info("조회수 증가 실패 - invitationId: {}, error: {}", 
                        invitation.getInvitationId(), e.getMessage());
            }
            
            log.info("공개 청첩장 조회 성공 - coupleSlug: {}, invitationId: {}", 
                    coupleSlug, invitation.getInvitationId());
            
            return InvitationResponse.from(invitation);
            
        } catch (Exception e) {
            log.info("공개 청첩장 조회 실패 - coupleSlug: {}, error: {}", coupleSlug, e.getMessage());
            throw e;
        }
    }
    

    
    // 최근 수정된 청첩장을 대표로 설정
    private void setLatestInvitationAsRepresentative(Long coupleId) {
        Invitation latestInvitation = invitationRepository
                .findTopByCoupleIdOrderByUpdatedAtDesc(coupleId)
                .orElseThrow(() -> new InvitationNotFoundException("설정할 청첩장이 없습니다."));
        
        latestInvitation.setRepresentative(true);
        invitationRepository.save(latestInvitation);
    }
    
    // S3 URL에서 키 추출 (개선된 로직)
    private String extractS3KeyFromUrl(String imageUrl) {
        log.info("=== S3 키 추출 시작 ===");
        log.info("입력된 이미지 URL: {}", imageUrl);
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.error("이미지 URL이 null이거나 비어있음");
            throw new S3UrlInvalidException("이미지 URL이 비어있습니다.");
        }
        
        try {
            URI uri = new URI(imageUrl);
            String path = uri.getPath();
            log.info("파싱된 URI 경로: {}", path);
            log.info("전체 URI 정보 - scheme: {}, host: {}, port: {}, path: {}", 
                    uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
            
            String extractedKey = null;
            
            // S3 직접 URL: https://bucket.s3.region.amazonaws.com/invitations/1/main-image.jpg
            if (imageUrl.contains("s3.amazonaws.com") || imageUrl.contains(".s3.")) {
                log.info("S3 직접 URL 형식 감지됨");
                String[] pathParts = path.split("/");
                log.info("경로 분할 결과 - 개수: {}, 내용: {}", pathParts.length, Arrays.toString(pathParts));
                
                if (pathParts.length > 1) {
                    extractedKey = String.join("/", Arrays.copyOfRange(pathParts, 1, pathParts.length));
                    log.info("S3 직접 URL에서 추출된 키: {}", extractedKey);
                } else {
                    log.error("S3 직접 URL 경로가 너무 짧음 - pathParts.length: {}", pathParts.length);
                }
            } 
            // CloudFront URL: https://d123.cloudfront.net/invitations/1/main-image.jpg
            else if (imageUrl.contains("cloudfront.net")) {
                log.info("CloudFront URL 형식 감지됨");
                extractedKey = path.substring(1); // 첫 번째 '/' 제거
                log.info("CloudFront URL에서 추출된 키: {}", extractedKey);
            }
            else {
                log.error("지원하지 않는 URL 형식 - URL: {}", imageUrl);
                throw new S3UrlInvalidException("지원하지 않는 URL 형식입니다: " + imageUrl);
            }
            
            if (extractedKey == null || extractedKey.trim().isEmpty()) {
                log.error("추출된 S3 키가 null이거나 비어있음");
                throw new S3UrlInvalidException("S3 키 추출에 실패했습니다: " + imageUrl);
            }
            
            log.info("=== S3 키 추출 완료: {} ===", extractedKey);
            return extractedKey;
            
        } catch (URISyntaxException e) {
            log.error("URL 파싱 실패 - URL: {}, error: {}", imageUrl, e.getMessage());
            throw new S3UrlInvalidException("잘못된 URL 형식입니다: " + imageUrl);
        }
    }
    
    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg"; // 기본값
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // 내부 메서드: 메인 이미지 업로드
    private String uploadMainImageInternal(Long coupleId, MultipartFile file) {
        // UUID 생성
        String uuid = UUID.randomUUID().toString();
        
        // S3 키 생성: invitations/{coupleId}/{uuid}.{확장자}
        String newS3Key = String.format("invitations/%d/%s.%s", 
                coupleId, uuid, getFileExtension(file.getOriginalFilename()));
        
        try {
            String imageUrl = s3Service.uploadFile(file, newS3Key);
            log.info("메인 이미지 S3 업로드 완료 - coupleId: {}, s3Key: {}", coupleId, newS3Key);
            return imageUrl;
        } catch (Exception e) {
            log.error("메인 이미지 S3 업로드 실패 - coupleId: {}, s3Key: {}, error: {}", 
                    coupleId, newS3Key, e.getMessage());
            throw new S3UploadException("이미지 업로드에 실패했습니다. S3 연결 오류: " + e.getMessage());
        }
    }
    
    // 내부 메서드: 메인 이미지 삭제
    private void deleteMainImageInternal(Long coupleId) {
        log.info("=== 메인 이미지 삭제 프로세스 시작 - coupleId: {} ===", coupleId);
        
        // 현재 커플의 대표 청첩장 조회
        Invitation representativeInvitation = invitationRepository.findByCoupleIdAndIsRepresentativeTrue(coupleId)
                .orElse(null);
        
        log.info("대표 청첩장 조회 결과 - 존재: {}, invitationId: {}", 
                representativeInvitation != null, 
                representativeInvitation != null ? representativeInvitation.getInvitationId() : "N/A");
        
        if (representativeInvitation != null && representativeInvitation.getMainImageUrl() != null) {
            log.info("대표 청첩장 이미지 URL: {}", representativeInvitation.getMainImageUrl());
            
            try {
                log.info("대표 청첩장 이미지 삭제 시도 - coupleId: {}, URL: {}", coupleId, representativeInvitation.getMainImageUrl());
                String s3Key = extractS3KeyFromUrl(representativeInvitation.getMainImageUrl());
                log.info("S3Service.deleteFile() 호출 - s3Key: {}", s3Key);
                s3Service.deleteFile(s3Key);
                log.info("기존 메인 이미지 S3 삭제 완료 - coupleId: {}, s3Key: {}", coupleId, s3Key);
                
                // DB에서 이미지 URL 제거
                log.info("DB에서 이미지 URL 제거 시도 - coupleId: {}", coupleId);
                representativeInvitation.setMainImageUrl(null);
                invitationRepository.save(representativeInvitation);
                log.info("DB 메인 이미지 URL 제거 완료 - coupleId: {}", coupleId);
            } catch (Exception e) {
                log.error("기존 메인 이미지 삭제 실패 - coupleId: {}, URL: {}, error: {}", 
                        coupleId, representativeInvitation.getMainImageUrl(), e.getMessage());
                log.error("삭제 실패 상세 정보:", e);
                // 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
            }
        } else {
            log.info("삭제할 대표 청첩장 이미지가 없음 - coupleId: {}, representativeInvitation: {}, mainImageUrl: {}", 
                    coupleId, 
                    representativeInvitation != null ? "존재" : "null",
                    representativeInvitation != null ? representativeInvitation.getMainImageUrl() : "N/A");
        }
        
        log.info("=== 메인 이미지 삭제 프로세스 완료 - coupleId: {} ===", coupleId);
    }
    
    /**
     * 사용자가 요청한 웨딩 데이터를 특별히 추적하는 로깅 메서드
     * venueLatitude, venueLongitude, views, weddingDate, weddingHall, weddingLocation, weddingTime 데이터를 로깅
     */
    private void logWeddingDataReceived(Invitation invitation, String operation) {
        log.info("🎉 === 웨딩 데이터 수신 추적 [{}] ===", operation);
        log.info("📊 invitationId: {}", invitation.getInvitationId());
        log.info("📊 coupleId: {}", invitation.getCoupleId());
        log.info("📍 venueLatitude: {}", invitation.getVenueLatitude());
        log.info("📍 venueLongitude: {}", invitation.getVenueLongitude());
        log.info("👀 views (totalViews): {}", invitation.getTotalViews());
        log.info("📅 weddingDate: {}", invitation.getWeddingDate());
        log.info("🏛️ weddingHall: {}", invitation.getWeddingHall());
        log.info("🏛️ weddingLocation (weddingHall과 동일): {}", invitation.getWeddingHall());
        log.info("⏰ weddingTime: {}", invitation.getWeddingTime());
        log.info("🏠 venueAddress: {}", invitation.getVenueAddress());
        log.info("👨 groomName: {}", invitation.getGroomName());
        log.info("👩 brideName: {}", invitation.getBrideName());
        log.info("🎉 === 웨딩 데이터 수신 추적 완료 [{}] ===", operation);
    }
}
