package com.marry1q.marry1qbe.domain.giftMoney.service;

import com.marry1q.marry1qbe.domain.giftMoney.dto.request.CreateGiftMoneyRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.request.UpdateGiftMoneyRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.request.UpdateThanksStatusRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyListResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyStatisticsResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.SafeAccountTransactionListResponse;
import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoney;
import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import com.marry1q.marry1qbe.domain.giftMoney.exception.GiftMoneyNotFoundException;
import com.marry1q.marry1qbe.domain.giftMoney.exception.GiftMoneyStatsNotFoundException;
import com.marry1q.marry1qbe.domain.giftMoney.repository.GiftMoneyRepository;
import com.marry1q.marry1qbe.domain.giftMoney.repository.GiftMoneyStatsRepository;
import com.marry1q.marry1qbe.domain.account.service.AccountService;
import com.marry1q.marry1qbe.domain.account.repository.CoupleAccountTransactionRepository;
import com.marry1q.marry1qbe.domain.customer.service.CustomerService;
import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftMoneyService {
    
    private final GiftMoneyRepository giftMoneyRepository;
    private final GiftMoneyStatsRepository giftMoneyStatsRepository;
    private final AccountService accountService;
    private final CoupleAccountTransactionRepository coupleAccountTransactionRepository;
    private final CustomerService customerService;
    
    /**
     * 축의금 목록 조회 (필터링 + 페이징)
     */
    public GiftMoneyListResponse getGiftMoneyList(
            String name,
            String relationship,
            String source,
            LocalDate startDate,
            LocalDate endDate,
            Boolean thanksSent,
            Long coupleId,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<GiftMoney> giftMoneyPage = giftMoneyRepository.findGiftMoneyWithFilters(
                name, relationship, source, startDate, endDate, thanksSent, coupleId, pageable);
        
        Page<GiftMoneyResponse> responsePage = giftMoneyPage.map(GiftMoneyResponse::from);
        
        return GiftMoneyListResponse.from(responsePage);
    }
    
    /**
     * 축의금 단건 조회
     */
    public GiftMoneyResponse getGiftMoney(Long giftMoneyId, Long coupleId) {
        GiftMoney giftMoney = giftMoneyRepository.findById(giftMoneyId)
                .orElseThrow(() -> new GiftMoneyNotFoundException(giftMoneyId));
        
        // 해당 커플의 축의금인지 확인
        if (!giftMoney.getCoupleId().equals(coupleId)) {
            throw new GiftMoneyNotFoundException(giftMoneyId, coupleId);
        }
        
        return GiftMoneyResponse.from(giftMoney);
    }
    
    /**
     * 축의금 생성
     */
    @Transactional
    public GiftMoneyResponse createGiftMoney(CreateGiftMoneyRequest request, Long coupleId) {
        try {
            // 축의금 엔티티 생성
            GiftMoney giftMoney = GiftMoney.builder()
                    .name(request.getName())
                    .amount(request.getAmount())
                    .relationship(request.getRelationship())
                    .source(request.getSource())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .memo(request.getMemo())
                    .giftDate(request.getGiftDate())
                    .thanksSent(false) // 기본값: 감사 연락 미완료
                    .coupleId(coupleId)
                    .build();
            
            // 축의금 저장
            GiftMoney savedGiftMoney = giftMoneyRepository.save(giftMoney);
            
            // 통계 증분 업데이트
            giftMoneyRepository.updateStatisticsIncrementallyForCreate(coupleId, savedGiftMoney);
            
            log.info("축의금 생성 완료: giftMoneyId={}, coupleId={}", savedGiftMoney.getGiftMoneyId(), coupleId);
            
            return GiftMoneyResponse.from(savedGiftMoney);
            
                            } catch (Exception e) {
                        log.error("축의금 생성 중 오류 발생: coupleId={}, error={}", coupleId, e.getMessage(), e);
                        throw new CustomException(ErrorCode.GIFT_MONEY_CREATE_FAILED, "축의금 생성에 실패했습니다.");
                    }
    }
    
    /**
     * 축의금 수정
     */
    @Transactional
    public GiftMoneyResponse updateGiftMoney(Long giftMoneyId, UpdateGiftMoneyRequest request, Long coupleId) {
        try {
            // 기존 축의금 조회
            GiftMoney existingGiftMoney = giftMoneyRepository.findById(giftMoneyId)
                    .orElseThrow(() -> new GiftMoneyNotFoundException(giftMoneyId));
            
            // 해당 커플의 축의금인지 확인
            if (!existingGiftMoney.getCoupleId().equals(coupleId)) {
                throw new GiftMoneyNotFoundException(giftMoneyId, coupleId);
            }
            
            // 기존 데이터 백업 (통계 업데이트용)
            GiftMoney oldGiftMoney = GiftMoney.builder()
                    .giftMoneyId(existingGiftMoney.getGiftMoneyId())
                    .name(existingGiftMoney.getName())
                    .amount(existingGiftMoney.getAmount())
                    .relationship(existingGiftMoney.getRelationship())
                    .source(existingGiftMoney.getSource())
                    .phone(existingGiftMoney.getPhone())
                    .address(existingGiftMoney.getAddress())
                    .memo(existingGiftMoney.getMemo())
                    .giftDate(existingGiftMoney.getGiftDate())
                    .thanksSent(existingGiftMoney.getThanksSent())
                    .thanksDate(existingGiftMoney.getThanksDate())
                    .thanksSentBy(existingGiftMoney.getThanksSentBy())
                    .coupleId(existingGiftMoney.getCoupleId())
                    .createdAt(existingGiftMoney.getCreatedAt())
                    .updatedAt(existingGiftMoney.getUpdatedAt())
                    .build();
            
            // 필드 업데이트
            if (StringUtils.hasText(request.getName())) {
                existingGiftMoney.setName(request.getName());
            }
            if (request.getAmount() != null) {
                existingGiftMoney.setAmount(request.getAmount());
            }
            if (request.getRelationship() != null) {
                existingGiftMoney.setRelationship(request.getRelationship());
            }
            if (request.getSource() != null) {
                existingGiftMoney.setSource(request.getSource());
            }
            if (request.getPhone() != null) {
                existingGiftMoney.setPhone(request.getPhone());
            }
            if (request.getAddress() != null) {
                existingGiftMoney.setAddress(request.getAddress());
            }
            if (request.getMemo() != null) {
                existingGiftMoney.setMemo(request.getMemo());
            }
            if (request.getGiftDate() != null) {
                existingGiftMoney.setGiftDate(request.getGiftDate());
            }
            if (request.getThanksSent() != null) {
                existingGiftMoney.setThanksSent(request.getThanksSent());
            }
            if (request.getThanksDate() != null) {
                existingGiftMoney.setThanksDate(request.getThanksDate());
            }
            if (request.getThanksSentBy() != null) {
                existingGiftMoney.setThanksSentBy(request.getThanksSentBy());
            }
            
            // 축의금 저장
            GiftMoney updatedGiftMoney = giftMoneyRepository.save(existingGiftMoney);
            
            // 통계 증분 업데이트
            giftMoneyRepository.updateStatisticsIncrementallyForUpdate(coupleId, oldGiftMoney, updatedGiftMoney);
            
            log.info("축의금 수정 완료: giftMoneyId={}, coupleId={}", updatedGiftMoney.getGiftMoneyId(), coupleId);
            
            return GiftMoneyResponse.from(updatedGiftMoney);
            
                            } catch (Exception e) {
                        log.error("축의금 수정 중 오류 발생: giftMoneyId={}, coupleId={}, error={}", giftMoneyId, coupleId, e.getMessage(), e);
                        throw new CustomException(ErrorCode.GIFT_MONEY_UPDATE_FAILED, "축의금 수정에 실패했습니다.");
                    }
    }
    
    /**
     * 감사 연락 상태 변경
     */
    @Transactional
    public GiftMoneyResponse updateThanksStatus(Long giftMoneyId, UpdateThanksStatusRequest request, Long coupleId) {
        try {
            // 기존 축의금 조회
            GiftMoney existingGiftMoney = giftMoneyRepository.findById(giftMoneyId)
                    .orElseThrow(() -> new GiftMoneyNotFoundException(giftMoneyId));
            
            // 해당 커플의 축의금인지 확인
            if (!existingGiftMoney.getCoupleId().equals(coupleId)) {
                throw new GiftMoneyNotFoundException(giftMoneyId, coupleId);
            }
            
            // 기존 데이터 백업 (통계 업데이트용)
            GiftMoney oldGiftMoney = GiftMoney.builder()
                    .giftMoneyId(existingGiftMoney.getGiftMoneyId())
                    .name(existingGiftMoney.getName())
                    .amount(existingGiftMoney.getAmount())
                    .relationship(existingGiftMoney.getRelationship())
                    .source(existingGiftMoney.getSource())
                    .phone(existingGiftMoney.getPhone())
                    .address(existingGiftMoney.getAddress())
                    .memo(existingGiftMoney.getMemo())
                    .giftDate(existingGiftMoney.getGiftDate())
                    .thanksSent(existingGiftMoney.getThanksSent())
                    .thanksDate(existingGiftMoney.getThanksDate())
                    .thanksSentBy(existingGiftMoney.getThanksSentBy())
                    .coupleId(existingGiftMoney.getCoupleId())
                    .createdAt(existingGiftMoney.getCreatedAt())
                    .updatedAt(existingGiftMoney.getUpdatedAt())
                    .build();
            
            // 감사 연락 상태 업데이트
            existingGiftMoney.setThanksSent(request.getThanksSent());
            existingGiftMoney.setThanksDate(request.getThanksDate());
            existingGiftMoney.setThanksSentBy(request.getThanksSentBy());
            
            // 축의금 저장
            GiftMoney updatedGiftMoney = giftMoneyRepository.save(existingGiftMoney);
            
            // 통계 증분 업데이트 (감사 연락 상태만 변경)
            giftMoneyRepository.updateStatisticsIncrementallyForUpdate(coupleId, oldGiftMoney, updatedGiftMoney);
            
            log.info("감사 연락 상태 변경 완료: giftMoneyId={}, coupleId={}, thanksSent={}", 
                    updatedGiftMoney.getGiftMoneyId(), coupleId, request.getThanksSent());
            
            return GiftMoneyResponse.from(updatedGiftMoney);
            
                            } catch (Exception e) {
                        log.error("감사 연락 상태 변경 중 오류 발생: giftMoneyId={}, coupleId={}, error={}", 
                                giftMoneyId, coupleId, e.getMessage(), e);
                        throw new CustomException(ErrorCode.GIFT_MONEY_UPDATE_FAILED, "감사 연락 상태 변경에 실패했습니다.");
                    }
    }
    
    /**
     * 축의금 삭제
     */
    @Transactional
    public void deleteGiftMoney(Long giftMoneyId, Long coupleId) {
        try {
            // 기존 축의금 조회
            GiftMoney existingGiftMoney = giftMoneyRepository.findById(giftMoneyId)
                    .orElseThrow(() -> new GiftMoneyNotFoundException(giftMoneyId));
            
            // 해당 커플의 축의금인지 확인
            if (!existingGiftMoney.getCoupleId().equals(coupleId)) {
                throw new GiftMoneyNotFoundException(giftMoneyId, coupleId);
            }
            
            // 통계 증분 업데이트 (삭제 전에 실행)
            giftMoneyRepository.updateStatisticsIncrementallyForDelete(coupleId, existingGiftMoney);
            
            // 축의금 삭제
            giftMoneyRepository.delete(existingGiftMoney);
            
            log.info("축의금 삭제 완료: giftMoneyId={}, coupleId={}", giftMoneyId, coupleId);
            
                            } catch (Exception e) {
                        log.error("축의금 삭제 중 오류 발생: giftMoneyId={}, coupleId={}, error={}", 
                                giftMoneyId, coupleId, e.getMessage(), e);
                        throw new CustomException(ErrorCode.GIFT_MONEY_DELETE_FAILED, "축의금 삭제에 실패했습니다.");
                    }
    }
    
    /**
     * 안심계좌 입금 내역 조회 (거래내역 동기화 포함)
     */
    @Transactional
    public SafeAccountTransactionListResponse getSafeAccountTransactions(
            Long coupleId,
            int page,
            int size) {
        
        try {
            log.info("안심계좌 입금 내역 조회 시작: coupleId={}, page={}, size={}", 
                    coupleId, page, size);
            
            // 1. 거래내역 동기화 (동기 처리)
            accountService.syncTransactions();
            log.info("거래내역 동기화 완료");
            
            // 2. 현재 사용자의 모임통장 조회
            Account coupleAccount = customerService.getCurrentUserCoupleAccount();
            log.info("모임통장 조회 완료: accountId={}, accountNumber={}", 
                    coupleAccount.getAccountId(), coupleAccount.getAccountNumber());
            
            // 3. 안심계좌 입금 내역 조회 (페이징)
            Pageable pageable = PageRequest.of(page, size);
            Page<com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction> transactionPage = 
                coupleAccountTransactionRepository.findByAccountIdAndIsSafeAccountDepositTrueOrderByTransactionDateDescTransactionTimeDesc(
                    coupleAccount.getAccountId(), pageable);
            
            log.info("안심계좌 입금 내역 조회 완료: 총 {}건, 현재 페이지 {}건", 
                    transactionPage.getTotalElements(), transactionPage.getNumberOfElements());
            
            // 4. DTO 변환
            SafeAccountTransactionListResponse response = SafeAccountTransactionListResponse.from(transactionPage);
            
            log.info("안심계좌 입금 내역 조회 성공: coupleId={}", coupleId);
            return response;
            
        } catch (Exception e) {
            log.error("안심계좌 입금 내역 조회 중 오류 발생: coupleId={}, error={}", coupleId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "안심계좌 입금 내역 조회에 실패했습니다.");
        }
    }
    
    
}
