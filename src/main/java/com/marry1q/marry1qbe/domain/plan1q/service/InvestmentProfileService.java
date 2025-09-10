package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.dto.request.InvestmentProfileSubmitRequest;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.InvestmentProfileResponse;
import com.marry1q.marry1qbe.domain.plan1q.dto.response.InvestmentQuestionResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentAnswerOption;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentProfile;
import com.marry1q.marry1qbe.domain.plan1q.entity.InvestmentQuestion;
import com.marry1q.marry1qbe.domain.plan1q.repository.InvestmentAnswerOptionRepository;
import com.marry1q.marry1qbe.domain.plan1q.repository.InvestmentProfileRepository;
import com.marry1q.marry1qbe.domain.plan1q.repository.InvestmentQuestionRepository;
import com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentProfileService {
    
    private final InvestmentQuestionRepository investmentQuestionRepository;
    private final InvestmentAnswerOptionRepository investmentAnswerOptionRepository;
    private final InvestmentProfileRepository investmentProfileRepository;
    private final CommonCodeService commonCodeService;
    
    /**
     * 투자성향 검사 질문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InvestmentQuestionResponse> getQuestions() {
        log.info("투자성향 검사 질문 목록 조회 시작");
        
        List<InvestmentQuestion> questions = investmentQuestionRepository.findAllActiveQuestions();
        
        // 답변 옵션을 함께 로딩
        questions.forEach(question -> {
            List<InvestmentAnswerOption> options = investmentAnswerOptionRepository
                    .findByQuestionIdAndActive(question.getQuestionId());
            question.getAnswerOptions().clear();
            question.getAnswerOptions().addAll(options);
        });
        
        List<InvestmentQuestionResponse> responses = questions.stream()
                .map(question -> InvestmentQuestionResponse.from(question, commonCodeService))
                .collect(Collectors.toList());
        
        log.info("투자성향 검사 질문 목록 조회 완료 - 질문 수: {}", responses.size());
        return responses;
    }
    
    /**
     * 투자성향 검사 결과 제출 및 프로필 생성
     */
    @Transactional
    public InvestmentProfileResponse submitProfile(InvestmentProfileSubmitRequest request, String userSeqNo) {
        log.info("투자성향 검사 결과 제출 시작 - 사용자: {}", userSeqNo);
        
        // 기존 프로필 만료 처리
        expireExistingProfiles(userSeqNo);
        
        // 답변 점수 계산
        int totalScore = calculateTotalScore(request.getAnswers());
        
        // 투자성향 타입 결정
        String profileType = determineProfileType(totalScore);
        
        // 만료일 계산 (1년 후)
        LocalDate expiredDate = LocalDate.now().plusYears(1);
        
        // 프로필 저장
        InvestmentProfile profile = InvestmentProfile.builder()
                .userSeqNo(userSeqNo)
                .profileType(profileType)
                .score(totalScore)
                .description(getProfileDescription(profileType))
                .expiredDate(expiredDate)
                .isExpired(false)
                .build();
        
        InvestmentProfile savedProfile = investmentProfileRepository.save(profile);
        
        log.info("투자성향 검사 결과 제출 완료 - 프로필ID: {}, 타입: {}, 점수: {}", 
                savedProfile.getInvestmentProfileId(), profileType, totalScore);
        
        return InvestmentProfileResponse.from(savedProfile, commonCodeService);
    }
    
    /**
     * 사용자의 투자성향 프로필 조회
     */
    @Transactional(readOnly = true)
    public InvestmentProfileResponse getProfile(String userSeqNo) {
        log.info("투자성향 프로필 조회 시작 - 사용자: {}", userSeqNo);
        
        InvestmentProfile profile = investmentProfileRepository
                .findFirstByUserSeqNoAndIsExpiredFalseOrderByCreatedAtDesc(userSeqNo)
                .orElse(null);
        
        if (profile == null) {
            log.info("활성 투자성향 프로필이 없습니다 - 사용자: {}", userSeqNo);
            return null;
        }
        
        // 만료 여부 업데이트
        profile.updateExpiredStatus();
        if (profile.isExpired()) {
            log.info("투자성향 프로필이 만료되었습니다 - 사용자: {}", userSeqNo);
            return null;
        }
        
        log.info("투자성향 프로필 조회 완료 - 프로필ID: {}, 타입: {}", 
                profile.getInvestmentProfileId(), profile.getProfileType());
        
        return InvestmentProfileResponse.from(profile, commonCodeService);
    }
    
    /**
     * 사용자의 투자성향 프로필 엔티티 조회
     */
    @Transactional(readOnly = true)
    public InvestmentProfile getProfileEntity(String userSeqNo) {
        log.info("투자성향 프로필 엔티티 조회 시작 - 사용자: {}", userSeqNo);
        
        InvestmentProfile profile = investmentProfileRepository
                .findFirstByUserSeqNoAndIsExpiredFalseOrderByCreatedAtDesc(userSeqNo)
                .orElse(null);
        
        if (profile == null) {
            log.info("활성 투자성향 프로필이 없습니다 - 사용자: {}", userSeqNo);
            return null;
        }
        
        // 만료 여부 업데이트
        profile.updateExpiredStatus();
        if (profile.isExpired()) {
            log.info("투자성향 프로필이 만료되었습니다 - 사용자: {}", userSeqNo);
            return null;
        }
        
        log.info("투자성향 프로필 엔티티 조회 완료 - 프로필ID: {}, 타입: {}", 
                profile.getInvestmentProfileId(), profile.getProfileType());
        
        return profile;
    }
    
    /**
     * 기존 프로필 만료 처리
     */
    private void expireExistingProfiles(String userSeqNo) {
        List<InvestmentProfile> existingProfiles = investmentProfileRepository
                .findActiveByUserSeqNoOrderByCreatedAtDesc(userSeqNo);
        
        existingProfiles.forEach(profile -> {
            profile.updateExpiredStatus();
        });
        
        if (!existingProfiles.isEmpty()) {
            investmentProfileRepository.saveAll(existingProfiles);
            log.info("기존 투자성향 프로필 만료 처리 완료 - 사용자: {}, 만료된 프로필 수: {}", 
                    userSeqNo, existingProfiles.size());
        }
    }
    
    /**
     * 답변 점수 계산
     */
    private int calculateTotalScore(List<InvestmentProfileSubmitRequest.AnswerRequest> answers) {
        int totalScore = 0;
        
        for (InvestmentProfileSubmitRequest.AnswerRequest answer : answers) {
            InvestmentAnswerOption option = investmentAnswerOptionRepository
                    .findByQuestionIdAndOptionValue(answer.getQuestionId(), answer.getAnswer())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "유효하지 않은 답변입니다. 질문ID: " + answer.getQuestionId() + 
                            ", 답변: " + answer.getAnswer()));
            
            totalScore += option.getScore();
        }
        
        return totalScore;
    }
    
    /**
     * 총점에 따른 투자성향 타입 결정 (금융감독원 표준)
     * 
     * 금융감독원 투자성향 분류 기준:
     * - 보수형: 11-25점
     * - 중립형: 26-40점  
     * - 공격형: 41-55점
     */
    private String determineProfileType(int totalScore) {
        // 총점 범위에 따른 투자성향 분류 (금융감독원 표준)
        if (totalScore <= 25) {
            return "conservative";
        } else if (totalScore <= 40) {
            return "neutral";
        } else {
            return "aggressive";
        }
    }
    
    /**
     * 투자성향 타입에 따른 설명 반환
     */
    private String getProfileDescription(String profileType) {
        return commonCodeService.getCodeName("INVESTMENT_PROFILE_TYPE", profileType);
    }
}
