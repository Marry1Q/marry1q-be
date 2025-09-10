package com.marry1q.marry1qbe.domain.plan1q.dto.response;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.grobal.commonCode.service.CommonCodeService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan1QProductResponse {
    private Long productId;
    private String productName;
    private String productType;
    private String productTypeName;
    private BigDecimal investmentRatio;
    private BigDecimal investmentAmount;

    private BigDecimal monthlyAmount;
    private Boolean subscribed;


    private LocalDate contractDate;
    private LocalDate maturityDate;
    private String terms;
    private String contract;
    private String accountNumber;               // 계좌번호 (하나은행 API와 동일)
    private String sourceAccountNumber;         // 출금계좌번호
    private String riskLevel;
    private String riskType;
    private String assetClass;
    private String strategy;

    private String period;
    private Long hanaBankProductId;
    private String hanaBankSubscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long plan1qGoalId;
    private String recommendationReason; // AI 추천 이유 (DB에 저장되지 않음, 프론트엔드 전달용)
    
    // AI 추천 시 받은 기대 수익률
    private BigDecimal expectedReturnRate;
    
    // 하나은행 API 필드명과 동일하게 유지
    private BigDecimal currentBalance;          // 하나은행: 현재 잔액
    private BigDecimal totalDeposit;            // 하나은행: 총 입금액
    private BigDecimal baseRate;                // 하나은행: 기준금리 (적금용)
    private BigDecimal profitRate;              // 하나은행: 수익률 (펀드용)
    private BigDecimal profit;                  // 하나은행: 수익/이자
    private LocalDateTime lastUpdated;          // 하나은행: 마지막 업데이트 시간
    
    // 백워드 호환성을 위한 필드 (프론트엔드 호환성 유지)
    private BigDecimal returnRate;              // 백워드 호환성용 - baseRate 또는 profitRate 값 사용

    
    public static Plan1QProductResponse from(Plan1QProduct product, CommonCodeService commonCodeService) {
        return Plan1QProductResponse.builder()
            .productId(product.getPlan1qProductId())
            .productName(product.getProductName())
            .productType(product.getProductType())
            .productTypeName(product.getProductTypeName(commonCodeService))
            .investmentRatio(product.getInvestmentRatio())
            .investmentAmount(product.getInvestmentAmount())

            .monthlyAmount(product.getMonthlyAmount())
            .subscribed(product.getSubscribed())


            .contractDate(product.getContractDate())
            .maturityDate(product.getMaturityDate())
            .terms(product.getTerms())
            .contract(product.getContract())
            .accountNumber(product.getAccountNumber())
            .sourceAccountNumber(product.getSourceAccountNumber())
            .riskLevel(product.getRiskLevel())
            .riskType(product.getRiskType())
            .assetClass(product.getAssetClass())
            .strategy(product.getStrategy())

            .period(product.getPeriod())
            .hanaBankProductId(product.getHanaBankProductId())
            .hanaBankSubscriptionId(product.getHanaBankSubscriptionId())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .plan1qGoalId(product.getPlan1qGoalId())
            .recommendationReason(null) // DB에서 가져온 상품에는 추천 이유가 없음
            .expectedReturnRate(product.getExpectedReturnRate())
            // 하나은행 API 필드들 기본값 설정 (실시간 조회 전까지)
            .currentBalance(BigDecimal.ZERO) // 초기 현재 잔액 0
            .totalDeposit(BigDecimal.ZERO) // 초기 총 입금액 0
            .baseRate(BigDecimal.ZERO) // 초기 기준금리 0
            .profitRate(BigDecimal.ZERO) // 초기 수익률 0
            .profit(BigDecimal.ZERO) // 초기 수익 0
            .lastUpdated(null) // 초기 업데이트 시간 null
            // 백워드 호환성 필드
            .returnRate(BigDecimal.ZERO) // 기본 수익률 3.2% (프론트엔드 호환성)
            .build();
    }
}
