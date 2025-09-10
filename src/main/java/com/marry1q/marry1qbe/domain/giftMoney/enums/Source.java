package com.marry1q.marry1qbe.domain.giftMoney.enums;

public enum Source {
    CASH("현금"),
    TRANSFER("계좌이체");
    
    private final String displayName;
    
    Source(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
