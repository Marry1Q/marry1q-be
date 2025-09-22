package com.marry1q.marry1qbe.domain.giftMoney.controller;

import com.marry1q.marry1qbe.domain.giftMoney.dto.request.CreateGiftMoneyRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.request.UpdateGiftMoneyRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.request.UpdateThanksStatusRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.request.UpdateSafeAccountTransactionReviewStatusRequest;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyListResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.GiftMoneyResponse;
import com.marry1q.marry1qbe.domain.giftMoney.dto.response.SafeAccountTransactionListResponse;
import com.marry1q.marry1qbe.domain.giftMoney.service.GiftMoneyService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/gift-money")
@RequiredArgsConstructor
@Tag(name = "Gift Money", description = "축의금 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class GiftMoneyController {
    
    private final GiftMoneyService giftMoneyService;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "축의금 목록 조회",
        description = "필터링 조건과 페이징을 포함한 축의금 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<CustomApiResponse<GiftMoneyListResponse>> getGiftMoneyList(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "축의자 이름") @RequestParam(required = false) String name,
            @Parameter(description = "관계") @RequestParam(required = false) String relationship,
            @Parameter(description = "받은방법") @RequestParam(required = false) String source,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @Parameter(description = "감사 연락 완료 여부") @RequestParam(required = false) Boolean thanksSent,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate) : null;
        
        GiftMoneyListResponse response = giftMoneyService.getGiftMoneyList(
                name, relationship, source, parsedStartDate, parsedEndDate, thanksSent, coupleId, page, size);
        
        return ResponseEntity.ok(CustomApiResponse.success(response));
    }
    
    @Operation(
        summary = "축의금 단건 조회",
        description = "특정 축의금의 상세 정보를 조회합니다."
    )
    @GetMapping("/{giftMoneyId}")
    public ResponseEntity<CustomApiResponse<GiftMoneyResponse>> getGiftMoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "축의금 ID") @PathVariable Long giftMoneyId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneyResponse response = giftMoneyService.getGiftMoney(giftMoneyId, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(response));
    }
    
    @Operation(
        summary = "축의금 생성",
        description = "새로운 축의금을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<CustomApiResponse<GiftMoneyResponse>> createGiftMoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGiftMoneyRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneyResponse response = giftMoneyService.createGiftMoney(request, coupleId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomApiResponse.success(response, "축의금이 성공적으로 생성되었습니다."));
    }
    
    @Operation(
        summary = "축의금 수정",
        description = "기존 축의금을 수정합니다."
    )
    @PutMapping("/{giftMoneyId}")
    public ResponseEntity<CustomApiResponse<GiftMoneyResponse>> updateGiftMoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "축의금 ID") @PathVariable Long giftMoneyId,
            @Valid @RequestBody UpdateGiftMoneyRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneyResponse response = giftMoneyService.updateGiftMoney(giftMoneyId, request, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(response, "축의금이 성공적으로 수정되었습니다."));
    }
    
    @Operation(
        summary = "감사 연락 상태 변경",
        description = "축의금의 감사 연락 상태를 변경합니다."
    )
    @PutMapping("/{giftMoneyId}/thanks-status")
    public ResponseEntity<CustomApiResponse<GiftMoneyResponse>> updateThanksStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "축의금 ID") @PathVariable Long giftMoneyId,
            @Valid @RequestBody UpdateThanksStatusRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        GiftMoneyResponse response = giftMoneyService.updateThanksStatus(giftMoneyId, request, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(response, "감사 연락 상태가 성공적으로 변경되었습니다."));
    }
    
    @Operation(
        summary = "축의금 삭제",
        description = "축의금을 삭제합니다."
    )
    @DeleteMapping("/{giftMoneyId}")
    public ResponseEntity<CustomApiResponse<Void>> deleteGiftMoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "축의금 ID") @PathVariable Long giftMoneyId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        giftMoneyService.deleteGiftMoney(giftMoneyId, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(null, "축의금이 성공적으로 삭제되었습니다."));
    }
    
    @Operation(
        summary = "안심계좌 입금 내역 조회",
        description = "모임통장 거래내역을 동기화하고 안심계좌로 입금된 거래내역을 조회합니다."
    )
    @GetMapping("/safe-account-transactions")
    public ResponseEntity<CustomApiResponse<SafeAccountTransactionListResponse>> getSafeAccountTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        SafeAccountTransactionListResponse response = giftMoneyService.getSafeAccountTransactions(
                coupleId, page, size);
        
        return ResponseEntity.ok(CustomApiResponse.success(response, "안심계좌 입금 내역 조회가 완료되었습니다."));
    }
    
    @Operation(
        summary = "안심계좌 거래내역 리뷰 상태 변경",
        description = "안심계좌 거래내역의 리뷰 상태를 변경합니다."
    )
    @PutMapping("/safe-account-transactions/{transactionId}/review-status")
    public ResponseEntity<CustomApiResponse<Void>> updateSafeAccountTransactionReviewStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "거래내역 ID") @PathVariable Long transactionId,
            @Valid @RequestBody UpdateSafeAccountTransactionReviewStatusRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        giftMoneyService.updateSafeAccountTransactionReviewStatus(transactionId, request, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(null, "안심계좌 거래내역 리뷰 상태가 성공적으로 변경되었습니다."));
    }
}
