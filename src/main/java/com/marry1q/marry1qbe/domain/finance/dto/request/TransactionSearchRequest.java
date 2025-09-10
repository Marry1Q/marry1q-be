package com.marry1q.marry1qbe.domain.finance.dto.request;

import com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TransactionSearchRequest {
    
    private String searchTerm;
    private Long categoryId;
    private String userSeqNo;
    private FinanceTransaction.TransactionType transactionType;
    private LocalDate startDate;
    private LocalDate endDate;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}
