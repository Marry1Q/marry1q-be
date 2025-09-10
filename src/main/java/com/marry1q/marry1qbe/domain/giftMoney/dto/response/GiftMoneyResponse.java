package com.marry1q.marry1qbe.domain.giftMoney.dto.response;

import com.marry1q.marry1qbe.domain.giftMoney.entity.GiftMoney;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Relationship;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축의금 응답")
public class GiftMoneyResponse {
    
    @Schema(description = "축의금 ID", example = "1")
    private Long giftMoneyId;
    
    @Schema(description = "축의자 이름", example = "김철수")
    private String name;
    
    @Schema(description = "축의금 금액", example = "50000")
    private BigDecimal amount;
    
    @Schema(description = "관계", example = "FRIEND")
    private Relationship relationship;
    
    @Schema(description = "관계 표시명", example = "친구")
    private String relationshipDisplayName;
    
    @Schema(description = "받은방법", example = "CASH")
    private Source source;
    
    @Schema(description = "받은방법 표시명", example = "현금")
    private String sourceDisplayName;
    
    @Schema(description = "연락처", example = "010-1234-5678")
    private String phone;
    
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;
    
    @Schema(description = "메모", example = "정말 감사합니다!")
    private String memo;
    
    @Schema(description = "축의 날짜", example = "2024-01-15")
    private LocalDate giftDate;
    
    @Schema(description = "감사 연락 완료 여부", example = "false")
    private Boolean thanksSent;
    
    @Schema(description = "감사 연락 날짜", example = "2024-01-16")
    private LocalDate thanksDate;
    
    @Schema(description = "감사 연락한 사람", example = "신랑")
    private String thanksSentBy;
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private String createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private String updatedAt;
    
    public static GiftMoneyResponse from(GiftMoney giftMoney) {
        return GiftMoneyResponse.builder()
                .giftMoneyId(giftMoney.getGiftMoneyId())
                .name(giftMoney.getName())
                .amount(giftMoney.getAmount())
                .relationship(giftMoney.getRelationship())
                .relationshipDisplayName(giftMoney.getRelationship().getDisplayName())
                .source(giftMoney.getSource())
                .sourceDisplayName(giftMoney.getSource().getDisplayName())
                .phone(giftMoney.getPhone())
                .address(giftMoney.getAddress())
                .memo(giftMoney.getMemo())
                .giftDate(giftMoney.getGiftDate())
                .thanksSent(giftMoney.getThanksSent())
                .thanksDate(giftMoney.getThanksDate())
                .thanksSentBy(giftMoney.getThanksSentBy())
                .coupleId(giftMoney.getCoupleId())
                .createdAt(giftMoney.getCreatedAt() != null ? giftMoney.getCreatedAt().toString() : null)
                .updatedAt(giftMoney.getUpdatedAt() != null ? giftMoney.getUpdatedAt().toString() : null)
                .build();
    }
}
