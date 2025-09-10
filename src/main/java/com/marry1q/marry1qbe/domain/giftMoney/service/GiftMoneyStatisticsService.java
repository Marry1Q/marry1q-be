package com.marry1q.marry1qbe.domain.giftMoney.service;

import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyStatisticsResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneySummaryResponse;
import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import com.marry1q.marry1qbe.domain.giftMoney.repository.GiftMoneyRepository;
import com.marry1q.marry1qbe.domain.giftMoney.repository.GiftMoneyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftMoneyStatisticsService {
    
    private final GiftMoneyStatsRepository giftMoneyStatsRepository;
    private final GiftMoneyRepository giftMoneyRepository;
    
    /**
     * 요약 통계 조회 (대시보드용)
     */
    public GiftMoneySummaryResponse getSummaryStatistics(Long coupleId) {
        GiftMoneyStats stats = getStats(coupleId);
        return GiftMoneySummaryResponse.from(stats);
    }
    
    /**
     * 전체 통계 조회 (통계 페이지용)
     */
    public GiftMoneyStatisticsResponse getFullStatistics(Long coupleId) {
        GiftMoneyStats stats = getStats(coupleId);
        return GiftMoneyStatisticsResponse.from(stats);
    }
    
    /**
     * 통계 데이터 조회 (없으면 빈 객체 생성)
     */
    private GiftMoneyStats getStats(Long coupleId) {
        return giftMoneyStatsRepository.findByCoupleId(coupleId)
                .orElse(GiftMoneyStats.builder().coupleId(coupleId).build());
    }
}
