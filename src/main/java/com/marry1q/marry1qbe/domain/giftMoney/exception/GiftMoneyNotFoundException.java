package com.marry1q.marry1qbe.domain.giftMoney.exception;

public class GiftMoneyNotFoundException extends RuntimeException {
    
    public GiftMoneyNotFoundException(String message) {
        super(message);
    }
    
    public GiftMoneyNotFoundException(Long giftMoneyId) {
        super("축의금을 찾을 수 없습니다: " + giftMoneyId);
    }
    
    public GiftMoneyNotFoundException(Long giftMoneyId, Long coupleId) {
        super("해당 커플의 축의금을 찾을 수 없습니다: giftMoneyId=" + giftMoneyId + ", coupleId=" + coupleId);
    }
}
