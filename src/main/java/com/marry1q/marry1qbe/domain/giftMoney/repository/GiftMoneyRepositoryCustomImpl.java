package com.marry1q.marry1qbe.domain.giftMoney.repository;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoney;
import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoneyStats;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Relationship;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Source;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.marry1q.marry1qbe.domain.giftMoney.entity.QGiftMoney.giftMoney;
import static com.marry1q.marry1qbe.domain.giftMoney.entity.QGiftMoneyStats.giftMoneyStats;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GiftMoneyRepositoryCustomImpl implements GiftMoneyRepositoryCustom {
    
    // 금액 범위 상수들 (성능 최적화)
    private static final BigDecimal AMOUNT_30K = new BigDecimal("30000");
    private static final BigDecimal AMOUNT_50K = new BigDecimal("50000");
    private static final BigDecimal AMOUNT_100K = new BigDecimal("100000");
    private static final BigDecimal AMOUNT_200K = new BigDecimal("200000");
    private static final BigDecimal AMOUNT_500K = new BigDecimal("500000");
    
    private final JPAQueryFactory queryFactory;
    private final GiftMoneyStatsRepository giftMoneyStatsRepository;
    
    @Override
    public Page<GiftMoney> findGiftMoneyWithFilters(
            String name,
            String relationship,
            String source,
            LocalDate startDate,
            LocalDate endDate,
            Boolean thanksSent,
            Long coupleId,
            Pageable pageable) {
        
        BooleanBuilder whereClause = new BooleanBuilder();
        
        // 커플 ID 필터 (필수)
        whereClause.and(giftMoney.coupleId.eq(coupleId));
        
        // 이름 필터
        if (name != null && !name.trim().isEmpty()) {
            whereClause.and(giftMoney.name.containsIgnoreCase(name.trim()));
        }
        
        // 관계 필터
        if (relationship != null && !relationship.trim().isEmpty()) {
            try {
                Relationship relationshipEnum = Relationship.valueOf(relationship.toUpperCase());
                whereClause.and(giftMoney.relationship.eq(relationshipEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid relationship value: {}", relationship);
            }
        }
        
        // 받은방법 필터
        if (source != null && !source.trim().isEmpty()) {
            try {
                Source sourceEnum = Source.valueOf(source.toUpperCase());
                whereClause.and(giftMoney.source.eq(sourceEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid source value: {}", source);
            }
        }
        
        // 날짜 범위 필터
        if (startDate != null) {
            whereClause.and(giftMoney.giftDate.goe(startDate));
        }
        if (endDate != null) {
            whereClause.and(giftMoney.giftDate.loe(endDate));
        }
        
        // 감사 연락 상태 필터
        if (thanksSent != null) {
            whereClause.and(giftMoney.thanksSent.eq(thanksSent));
        }
        
        // 전체 개수 조회
        long total = queryFactory
                .selectFrom(giftMoney)
                .where(whereClause)
                .fetchCount();
        
        // 페이징된 데이터 조회
        List<GiftMoney> content = queryFactory
                .selectFrom(giftMoney)
                .where(whereClause)
                .orderBy(giftMoney.giftDate.desc(), giftMoney.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    @Transactional
    public GiftMoneyStats calculateAndUpdateStatistics(Long coupleId) {
        // 기존 통계 조회 또는 새로 생성
        GiftMoneyStats stats = giftMoneyStatsRepository.findByCoupleId(coupleId)
                .orElse(GiftMoneyStats.builder().coupleId(coupleId).build());
        
        // 전체 통계 계산
        BigDecimal totalAmount = queryFactory
                .select(giftMoney.amount.sum().coalesce(BigDecimal.ZERO))
                .from(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId))
                .fetchOne();
        
        Long totalCount = queryFactory
                .select(giftMoney.count())
                .from(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId))
                .fetchOne();
        
        Long thanksNotSentCount = queryFactory
                .select(giftMoney.count())
                .from(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId)
                        .and(giftMoney.thanksSent.eq(false)))
                .fetchOne();
        
                            // 관계별 통계 계산
                    List<Tuple> relationshipStats = queryFactory
                            .select(giftMoney.relationship, giftMoney.amount.sum().coalesce(BigDecimal.ZERO), giftMoney.count())
                            .from(giftMoney)
                            .where(giftMoney.coupleId.eq(coupleId))
                            .groupBy(giftMoney.relationship)
                            .fetch();
        
        // 통계 업데이트
        stats.setTotalAmount(totalAmount);
        stats.setTotalCount(totalCount.intValue());
        stats.setThanksNotSentCount(thanksNotSentCount.intValue());
        
        // 관계별 통계 업데이트
        updateRelationshipStats(stats, relationshipStats);
        
        // 최고 후원자 조회
        GiftMoney topDonor = queryFactory
                .selectFrom(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId))
                .orderBy(giftMoney.amount.desc())
                .fetchFirst();
        
        if (topDonor != null) {
            stats.setTopDonorName(topDonor.getName());
            stats.setTopDonorAmount(topDonor.getAmount());
            stats.setTopDonorGiftMoneyId(topDonor.getGiftMoneyId());
        }
        
        // 금액대별 통계 계산
        updateAmountRangeStats(stats, coupleId);
        
        return giftMoneyStatsRepository.save(stats);
    }
    
    @Override
    @Transactional
    public void updateStatisticsIncrementallyForCreate(Long coupleId, GiftMoney giftMoney) {
        GiftMoneyStats stats = giftMoneyStatsRepository.findByCoupleId(coupleId)
                .orElse(GiftMoneyStats.builder().coupleId(coupleId).build());
        
        // 기본 통계 업데이트
        stats.addAmount(giftMoney.getAmount());
        stats.incrementCount();
        
        if (!giftMoney.getThanksSent()) {
            stats.incrementThanksNotSentCount();
        }
        
        // 관계별 통계 업데이트
        updateRelationshipStatsIncrementally(stats, giftMoney.getRelationship(), giftMoney.getAmount(), 1);
        
        // 금액대별 통계 업데이트
        updateAmountRangeStatsIncrementally(stats, giftMoney.getAmount(), 1);
        
        // 최고 후원자 업데이트
        updateTopDonorIfNeeded(stats, giftMoney);
        
        // 날짜 통계 업데이트
        stats.updateDateStats(giftMoney.getGiftDate());
        stats.calculateDailyAverage();
        
        giftMoneyStatsRepository.save(stats);
    }
    
    @Override
    @Transactional
    public void updateStatisticsIncrementallyForUpdate(Long coupleId, GiftMoney oldGiftMoney, GiftMoney newGiftMoney) {
        GiftMoneyStats stats = giftMoneyStatsRepository.findByCoupleId(coupleId)
                .orElse(GiftMoneyStats.builder().coupleId(coupleId).build());
        
        // 금액 차이 계산
        BigDecimal amountDiff = newGiftMoney.getAmount().subtract(oldGiftMoney.getAmount());
        stats.addAmount(amountDiff);
        
        // 감사 연락 상태 변경 처리
        if (oldGiftMoney.getThanksSent() && !newGiftMoney.getThanksSent()) {
            stats.incrementThanksNotSentCount();
        } else if (!oldGiftMoney.getThanksSent() && newGiftMoney.getThanksSent()) {
            stats.decrementThanksNotSentCount();
        }
        
        // 관계 변경 처리
        if (!oldGiftMoney.getRelationship().equals(newGiftMoney.getRelationship())) {
            updateRelationshipStatsIncrementally(stats, oldGiftMoney.getRelationship(), oldGiftMoney.getAmount(), -1);
            updateRelationshipStatsIncrementally(stats, newGiftMoney.getRelationship(), newGiftMoney.getAmount(), 1);
        } else {
            // 같은 관계인 경우 금액 차이만 반영
            updateRelationshipStatsIncrementally(stats, newGiftMoney.getRelationship(), amountDiff, 0);
        }
        
        // 금액대별 통계 업데이트
        updateAmountRangeStatsIncrementally(stats, oldGiftMoney.getAmount(), -1);
        updateAmountRangeStatsIncrementally(stats, newGiftMoney.getAmount(), 1);
        
        // 최고 후원자 재계산
        recalculateTopDonor(stats, coupleId);
        
        giftMoneyStatsRepository.save(stats);
    }
    
    @Override
    @Transactional
    public void updateStatisticsIncrementallyForDelete(Long coupleId, GiftMoney giftMoney) {
        GiftMoneyStats stats = giftMoneyStatsRepository.findByCoupleId(coupleId)
                .orElse(GiftMoneyStats.builder().coupleId(coupleId).build());
        
        // 기본 통계 업데이트
        stats.subtractAmount(giftMoney.getAmount());
        stats.decrementCount();
        
        if (!giftMoney.getThanksSent()) {
            stats.decrementThanksNotSentCount();
        }
        
        // 관계별 통계 업데이트
        updateRelationshipStatsIncrementally(stats, giftMoney.getRelationship(), giftMoney.getAmount(), -1);
        
        // 금액대별 통계 업데이트
        updateAmountRangeStatsIncrementally(stats, giftMoney.getAmount(), -1);
        
        // 최고 후원자 재계산
        recalculateTopDonor(stats, coupleId);
        
        giftMoneyStatsRepository.save(stats);
    }
    
                    private void updateRelationshipStats(GiftMoneyStats stats, List<Tuple> relationshipStats) {
        // 모든 관계별 통계 초기화
        stats.setFamilyAmount(BigDecimal.ZERO);
        stats.setFamilyCount(0);
        stats.setRelativeAmount(BigDecimal.ZERO);
        stats.setRelativeCount(0);
        stats.setFriendAmount(BigDecimal.ZERO);
        stats.setFriendCount(0);
        stats.setColleagueAmount(BigDecimal.ZERO);
        stats.setColleagueCount(0);
        stats.setAcquaintanceAmount(BigDecimal.ZERO);
        stats.setAcquaintanceCount(0);
        stats.setOtherAmount(BigDecimal.ZERO);
        stats.setOtherCount(0);
        
                            // 관계별 통계 설정
                    for (Tuple result : relationshipStats) {
                        Relationship relationship = result.get(giftMoney.relationship);
                        BigDecimal amount = result.get(giftMoney.amount.sum().coalesce(BigDecimal.ZERO));
                        Long count = result.get(giftMoney.count());
            
            switch (relationship) {
                case FAMILY:
                    stats.setFamilyAmount(amount);
                    stats.setFamilyCount(count.intValue());
                    break;
                case RELATIVE:
                    stats.setRelativeAmount(amount);
                    stats.setRelativeCount(count.intValue());
                    break;
                case FRIEND:
                    stats.setFriendAmount(amount);
                    stats.setFriendCount(count.intValue());
                    break;
                case COLLEAGUE:
                    stats.setColleagueAmount(amount);
                    stats.setColleagueCount(count.intValue());
                    break;
                case ACQUAINTANCE:
                    stats.setAcquaintanceAmount(amount);
                    stats.setAcquaintanceCount(count.intValue());
                    break;
                case OTHER:
                    stats.setOtherAmount(amount);
                    stats.setOtherCount(count.intValue());
                    break;
            }
        }
    }
    
    private void updateRelationshipStatsIncrementally(GiftMoneyStats stats, Relationship relationship, BigDecimal amount, int countDiff) {
        switch (relationship) {
            case FAMILY:
                stats.setFamilyAmount(stats.getFamilyAmount().add(amount));
                stats.setFamilyCount(stats.getFamilyCount() + countDiff);
                break;
            case RELATIVE:
                stats.setRelativeAmount(stats.getRelativeAmount().add(amount));
                stats.setRelativeCount(stats.getRelativeCount() + countDiff);
                break;
            case FRIEND:
                stats.setFriendAmount(stats.getFriendAmount().add(amount));
                stats.setFriendCount(stats.getFriendCount() + countDiff);
                break;
            case COLLEAGUE:
                stats.setColleagueAmount(stats.getColleagueAmount().add(amount));
                stats.setColleagueCount(stats.getColleagueCount() + countDiff);
                break;
            case ACQUAINTANCE:
                stats.setAcquaintanceAmount(stats.getAcquaintanceAmount().add(amount));
                stats.setAcquaintanceCount(stats.getAcquaintanceCount() + countDiff);
                break;
            case OTHER:
                stats.setOtherAmount(stats.getOtherAmount().add(amount));
                stats.setOtherCount(stats.getOtherCount() + countDiff);
                break;
        }
    }
    
                    // 금액 범위 판별을 위한 공통 조건 함수들
    private boolean isAmountUnder30k(BigDecimal amount) {
        return amount != null && amount.compareTo(AMOUNT_30K) < 0;
    }
    
    private boolean isAmount30kTo50k(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(AMOUNT_30K) >= 0 && 
               amount.compareTo(AMOUNT_50K) < 0;
    }
    
    private boolean isAmount50kTo100k(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(AMOUNT_50K) >= 0 && 
               amount.compareTo(AMOUNT_100K) < 0;
    }
    
    private boolean isAmount100kTo200k(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(AMOUNT_100K) >= 0 && 
               amount.compareTo(AMOUNT_200K) < 0;
    }
    
    private boolean isAmount200kTo500k(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(AMOUNT_200K) >= 0 && 
               amount.compareTo(AMOUNT_500K) <= 0;
    }
    
    private boolean isAmountOver500k(BigDecimal amount) {
        return amount != null && amount.compareTo(AMOUNT_500K) > 0;
    }

                    private void updateAmountRangeStats(GiftMoneyStats stats, Long coupleId) {
        // 모든 축의금 데이터를 조회하여 공통 로직으로 계산
        List<GiftMoney> allGiftMoney = queryFactory
                .selectFrom(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId))
                .fetch();
        
        int under30k = 0, amount30kTo50k = 0, amount50kTo100k = 0;
        int amount100kTo200k = 0, amount200kTo500k = 0, amountOver500k = 0;
        
        for (GiftMoney gift : allGiftMoney) {
            BigDecimal amount = gift.getAmount();
            if (isAmountUnder30k(amount)) {
                under30k++;
            } else if (isAmount30kTo50k(amount)) {
                amount30kTo50k++;
            } else if (isAmount50kTo100k(amount)) {
                amount50kTo100k++;
            } else if (isAmount100kTo200k(amount)) {
                amount100kTo200k++;
            } else if (isAmount200kTo500k(amount)) {
                amount200kTo500k++;
            } else if (isAmountOver500k(amount)) {
                amountOver500k++;
            }
        }
        
        stats.setAmountUnder30kCount(under30k);
        stats.setAmount30kTo50kCount(amount30kTo50k);
        stats.setAmount50kTo100kCount(amount50kTo100k);
        stats.setAmount100kTo200kCount(amount100kTo200k);
        stats.setAmount200kTo500kCount(amount200kTo500k);
        stats.setAmountOver500kCount(amountOver500k);
    }
    
    private void updateAmountRangeStatsIncrementally(GiftMoneyStats stats, BigDecimal amount, int countDiff) {
        // 공통 조건 함수를 사용하여 완전히 동일한 로직 적용
        if (isAmountUnder30k(amount)) {
            stats.setAmountUnder30kCount(stats.getAmountUnder30kCount() + countDiff);
        } else if (isAmount30kTo50k(amount)) {
            stats.setAmount30kTo50kCount(stats.getAmount30kTo50kCount() + countDiff);
        } else if (isAmount50kTo100k(amount)) {
            stats.setAmount50kTo100kCount(stats.getAmount50kTo100kCount() + countDiff);
        } else if (isAmount100kTo200k(amount)) {
            stats.setAmount100kTo200kCount(stats.getAmount100kTo200kCount() + countDiff);
        } else if (isAmount200kTo500k(amount)) {
            stats.setAmount200kTo500kCount(stats.getAmount200kTo500kCount() + countDiff);
        } else if (isAmountOver500k(amount)) {
            stats.setAmountOver500kCount(stats.getAmountOver500kCount() + countDiff);
        }
        // amount가 null이거나 음수인 경우는 아무 처리하지 않음
    }
    
    private void updateTopDonorIfNeeded(GiftMoneyStats stats, GiftMoney newGiftMoney) {
        if (stats.getTopDonorAmount() == null || newGiftMoney.getAmount().compareTo(stats.getTopDonorAmount()) > 0) {
            stats.setTopDonorName(newGiftMoney.getName());
            stats.setTopDonorAmount(newGiftMoney.getAmount());
            stats.setTopDonorGiftMoneyId(newGiftMoney.getGiftMoneyId());
        }
    }
    
    private void recalculateTopDonor(GiftMoneyStats stats, Long coupleId) {
        GiftMoney topDonor = queryFactory
                .selectFrom(giftMoney)
                .where(giftMoney.coupleId.eq(coupleId))
                .orderBy(giftMoney.amount.desc())
                .fetchFirst();
        
        if (topDonor != null) {
            stats.setTopDonorName(topDonor.getName());
            stats.setTopDonorAmount(topDonor.getAmount());
            stats.setTopDonorGiftMoneyId(topDonor.getGiftMoneyId());
        } else {
            stats.setTopDonorName(null);
            stats.setTopDonorAmount(BigDecimal.ZERO);
            stats.setTopDonorGiftMoneyId(null);
        }
    }
}
