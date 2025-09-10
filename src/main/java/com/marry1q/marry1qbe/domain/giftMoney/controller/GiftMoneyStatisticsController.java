package com.marry1q.marry1qbe.domain.giftMoney.controller;

import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyStatisticsResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneySummaryResponse;
import com.marry1q.marry1qbe.domain.giftMoney.service.GiftMoneyStatisticsService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/gift-money/statistics")
@RequiredArgsConstructor
@Tag(name = "Gift Money Statistics", description = "축의금 통계 API")
@SecurityRequirement(name = "Bearer Authentication")
public class GiftMoneyStatisticsController {
    
    private final GiftMoneyStatisticsService statisticsService;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "요약 통계 조회",
        description = "대시보드용 축의금 요약 통계를 조회합니다."
    )
    @GetMapping("/summary")
    public ResponseEntity<CustomApiResponse<GiftMoneySummaryResponse>> getSummaryStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneySummaryResponse response = statisticsService.getSummaryStatistics(coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(response));
    }
    
    @Operation(
        summary = "전체 통계 조회",
        description = "통계 페이지용 축의금 전체 통계를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<CustomApiResponse<GiftMoneyStatisticsResponse>> getFullStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneyStatisticsResponse response = statisticsService.getFullStatistics(coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(response));
    }
}
