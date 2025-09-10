package com.marry1q.marry1qbe.domain.giftMoney.repository;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoney;
import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface GiftMoneyRepositoryCustom {
    
    /**
     * 필터링 조건을 포함한 축의금 목록 조회
     */
    Page<GiftMoney> findGiftMoneyWithFilters(
            String name,
            String relationship,
            String source,
            LocalDate startDate,
            LocalDate endDate,
            Boolean thanksSent,
            Long coupleId,
            Pageable pageable
    );
    
    /**
     * 축의금 통계 계산 및 업데이트
     */
    GiftMoneyStats calculateAndUpdateStatistics(Long coupleId);
    
    /**
     * 축의금 통계 증분 업데이트 (생성)
     */
    void updateStatisticsIncrementallyForCreate(Long coupleId, GiftMoney giftMoney);
    
    /**
     * 축의금 통계 증분 업데이트 (수정)
     */
    void updateStatisticsIncrementallyForUpdate(Long coupleId, GiftMoney oldGiftMoney, GiftMoney newGiftMoney);
    
    /**
     * 축의금 통계 증분 업데이트 (삭제)
     */
    void updateStatisticsIncrementallyForDelete(Long coupleId, GiftMoney giftMoney);
}
