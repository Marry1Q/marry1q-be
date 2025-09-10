package com.marry1q.marry1qbe.domain.finance.dto.response;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class TransactionResponse {
    
    private Long transactionId;
    private String description;
    private BigDecimal amount;
    private FinanceTransaction.TransactionType transactionType;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private String memo;
    private Long categoryId;
    private String categoryName;
    private String iconName; // 카테고리 아이콘 이름
    private String colorName; // 카테고리 색상 이름
    private String userSeqNo;
    private String userName;
    private String createdAt;
    private String updatedAt;
    
    public static TransactionResponse from(FinanceTransaction transaction) {
        // 사용자 이름은 기본값으로 설정 (실제로는 JOIN 쿼리에서 가져옴)
        String userName = "알 수 없음";
        
        // 카테고리 정보 안전하게 가져오기
        String iconName = null;
        String colorName = null;
        String categoryName = "기타";
        Long categoryId = null;
        
        try {
            if (transaction.getFinanceCategory() != null) {
                categoryName = transaction.getFinanceCategory().getName();
                categoryId = transaction.getFinanceCategory().getFinanceCategoryId();
                iconName = transaction.getFinanceCategory().getIconName();
                colorName = transaction.getFinanceCategory().getColorName();
            }
        } catch (Exception e) {
            // 지연 로딩 실패 시 기본값 사용
            System.err.println("카테고리 정보 로딩 실패: " + e.getMessage());
        }
        
        return TransactionResponse.builder()
                .transactionId(transaction.getFinanceTransactionId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount().abs())
                .transactionType(transaction.getTransactionType())
                .transactionDate(transaction.getTransactionDate())
                .transactionTime(transaction.getTransactionTime())
                .memo(transaction.getMemo())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .iconName(iconName)
                .colorName(colorName)
                .userSeqNo(transaction.getUserSeqNo())
                .userName(userName)
                .createdAt(transaction.getCreatedAt().toString())
                .updatedAt(transaction.getUpdatedAt().toString())
                .build();
    }
}
