package com.marry1q.marry1qbe.domain.giftMoney.enums;

public enum Relationship {
    FAMILY("가족"),
    RELATIVE("친척"),
    FRIEND("친구"),
    COLLEAGUE("회사동료"),
    ACQUAINTANCE("지인"),
    OTHER("기타");
    
    private final String displayName;
    
    Relationship(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
