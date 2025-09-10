package com.marry1q.marry1qbe.domain.giftMoney.exception;

public class GiftMoneyStatsNotFoundException extends RuntimeException {
    
    public GiftMoneyStatsNotFoundException(String message) {
        super(message);
    }
    
    public GiftMoneyStatsNotFoundException(Long coupleId) {
        super("축의금 통계를 찾을 수 없습니다: coupleId=" + coupleId);
    }
}
