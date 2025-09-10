package com.marry1q.marry1qbe.domain.couple.service;

import com.marry1q.marry1qbe.domain.couple.dto.request.CreateCoupleRequest;
import com.marry1q.marry1qbe.domain.couple.dto.request.UpdateCoupleRequest;
import com.marry1q.marry1qbe.domain.couple.dto.response.CoupleResponse;
import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import com.marry1q.marry1qbe.domain.couple.exception.CoupleNotFoundException;
import com.marry1q.marry1qbe.domain.couple.exception.NoCoupleException;
import com.marry1q.marry1qbe.domain.couple.repository.CoupleRepository;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {
    
    private final CoupleRepository coupleRepository;
    private final SecurityUtil securityUtil;
    private final CustomerRepository customerRepository;
    
    /**
     * 현재 로그인한 사용자의 coupleId를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long getCurrentCoupleId() {
        String userSeqNo = securityUtil.getCurrentUserSeqNo();
        
        Customer customer = customerRepository.findById(userSeqNo)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userSeqNo));
        
        Long coupleId = customer.getCoupleId();
        if (coupleId == null) {
            log.info("사용자가 커플에 속해있지 않습니다. userSeqNo: {}", userSeqNo);
            throw new NoCoupleException("속해있는 커플이 없습니다.");
        }
        
        log.info("현재 사용자의 coupleId 조회 완료 - userSeqNo: {}, coupleId: {}", userSeqNo, coupleId);
        return coupleId;
    }
    
    /**
     * 커플 정보 조회
     */
    @Transactional(readOnly = true)
    public CoupleResponse getCoupleInfo(Long coupleId) {
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다: " + coupleId));
        
        // 커플 멤버들의 이름 조회
        List<String> memberNames = customerRepository.findByCoupleId(coupleId)
                .stream()
                .map(Customer::getCustomerName)
                .toList();
        
        return CoupleResponse.from(couple, memberNames);
    }
    
    /**
     * 현재 로그인한 사용자의 커플 정보 조회
     */
    @Transactional(readOnly = true)
    public CoupleResponse getCurrentCoupleInfo() {
        Long coupleId = getCurrentCoupleId();
        return getCoupleInfo(coupleId);
    }
    
    /**
     * 커플 정보 생성
     */
    public CoupleResponse createCouple(CreateCoupleRequest request) {
        Marry1qCouple couple = Marry1qCouple.builder()
                .weddingDate(request.getWeddingDate())
                .totalBudget(request.getTotalBudget())
                .coupleAccount(request.getCoupleAccount())
                .coupleCardNumber(request.getCoupleCardNumber())
                .currentSpent(BigDecimal.ZERO)
                .urlSlug(request.getUrlSlug())
                .build();
        
        Marry1qCouple savedCouple = coupleRepository.save(couple);
        return CoupleResponse.from(savedCouple, List.of()); // 새로 생성된 커플은 멤버가 없음
    }
    
    /**
     * 커플 정보 수정
     */
    public CoupleResponse updateCouple(Long coupleId, UpdateCoupleRequest request) {
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다: " + coupleId));
        
        couple.update(request.getWeddingDate(), request.getTotalBudget());
        Marry1qCouple updatedCouple = coupleRepository.save(couple);
        
        // 커플 멤버들의 이름 조회
        List<String> memberNames = customerRepository.findByCoupleId(coupleId)
                .stream()
                .map(Customer::getCustomerName)
                .toList();
        
        return CoupleResponse.from(updatedCouple, memberNames);
    }
    
    /**
     * 현재 로그인한 사용자의 커플 정보 수정
     */
    public CoupleResponse updateCurrentCoupleInfo(UpdateCoupleRequest request) {
        Long coupleId = getCurrentCoupleId();
        return updateCouple(coupleId, request);
    }
    

    
    /**
     * 커플 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByCoupleId(Long coupleId) {
        return coupleRepository.existsByCoupleId(coupleId);
    }
    
    /**
     * 커플의 현재 지출 금액 증가
     */
    @Transactional
    public void increaseCurrentSpent(Long coupleId, BigDecimal amount) {
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다: " + coupleId));
        
        BigDecimal currentSpent = couple.getCurrentSpent() != null ? couple.getCurrentSpent() : BigDecimal.ZERO;
        couple.updateCurrentSpent(currentSpent.add(amount));
        coupleRepository.save(couple);
    }
    
    /**
     * 커플의 현재 지출 금액 감소
     */
    @Transactional
    public void decreaseCurrentSpent(Long coupleId, BigDecimal amount) {
        Marry1qCouple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new CoupleNotFoundException("커플 정보를 찾을 수 없습니다: " + coupleId));
        
        BigDecimal currentSpent = couple.getCurrentSpent() != null ? couple.getCurrentSpent() : BigDecimal.ZERO;
        couple.updateCurrentSpent(currentSpent.subtract(amount));
        coupleRepository.save(couple);
    }
    

}
