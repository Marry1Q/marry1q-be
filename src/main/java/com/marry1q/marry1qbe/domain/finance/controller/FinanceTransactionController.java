package com.marry1q.marry1qbe.domain.finance.controller;

import com.marry1q.marry1qbe.domain.finance.dto.request.CreateTransactionRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.TransactionSearchRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateTransactionRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.TransactionListResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.TransactionResponse;
import com.marry1q.marry1qbe.domain.finance.service.FinanceTransactionService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.jwt.JwtTokenProvider;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/finance/transactions")
@RequiredArgsConstructor
@Tag(name = "Finance Transaction", description = "가계부 거래 내역 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class FinanceTransactionController {
    
    private final FinanceTransactionService financeTransactionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "거래 내역 목록 조회",
        description = "검색 조건과 페이징을 포함한 거래 내역 목록을 조회합니다. 사용자 이름 정보가 포함됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "거래 내역 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionListResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "content": [
                            {
                                "transactionId": 1,
                                "amount": 500000,
                                "transactionType": "EXPENSE",
                                "description": "웨딩홀 예약금",
                                "memo": "신라호텔 예약금",
                                "transactionDate": "2024-01-15",
                                "transactionTime": "14:30:00",
                                "categoryId": 1,
                                "categoryName": "웨딩홀",
                                "userSeqNo": "1234567890",
                                "userName": "김철수",
                                "createdAt": "2024-01-15T14:30:00",
                                "updatedAt": "2024-01-15T14:30:00"
                            }
                        ],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                                "sorted": true,
                                "unsorted": false
                            }
                        },
                        "totalElements": 1,
                        "totalPages": 1,
                        "last": true,
                        "first": true,
                        "numberOfElements": 1
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    public CustomApiResponse<TransactionListResponse> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "검색어 (설명, 메모에서 검색)") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "사용자 시퀀스 번호") @RequestParam(required = false) String userSeqNo,
            @Parameter(description = "거래 타입 (INCOME/EXPENSE)") @RequestParam(required = false) String transactionType,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        TransactionSearchRequest searchRequest = TransactionSearchRequest.builder()
                .searchTerm(searchTerm)
                .categoryId(categoryId)
                .userSeqNo(userSeqNo)
                .transactionType(transactionType != null ? 
                        com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction.TransactionType.valueOf(transactionType) : null)
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .page(page)
                .size(size)
                .build();
        
        Page<TransactionResponse> transactions = financeTransactionService.getTransactions(searchRequest, coupleId);
        
        return CustomApiResponse.success(TransactionListResponse.from(transactions), "거래 내역 목록 조회가 완료되었습니다.");
    }
    
    @Operation(
        summary = "거래 내역 단건 조회",
        description = "특정 거래 내역의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "거래 내역 단건 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "transactionId": 1,
                        "amount": 500000,
                        "transactionType": "EXPENSE",
                        "description": "웨딩홀 예약금",
                        "memo": "신라호텔 예약금",
                        "transactionDate": "2024-01-15",
                        "transactionTime": "14:30:00",
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "userSeqNo": "1234567890",
                        "userName": "김철수",
                        "createdAt": "2024-01-15T14:30:00",
                        "updatedAt": "2024-01-15T14:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{transactionId}")
    public CustomApiResponse<TransactionResponse> getTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "거래 내역 ID") @PathVariable Long transactionId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        TransactionResponse transaction = financeTransactionService.getTransaction(transactionId, coupleId);
        
        return CustomApiResponse.success(transaction, "거래 내역 조회가 완료되었습니다.");
    }
    
    @Operation(
        summary = "거래 내역 생성",
        description = "새로운 거래 내역을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "거래 내역 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "transactionId": 1,
                        "amount": 500000,
                        "transactionType": "EXPENSE",
                        "description": "웨딩홀 예약금",
                        "memo": "신라호텔 예약금",
                        "transactionDate": "2024-01-15",
                        "transactionTime": "14:30:00",
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "userSeqNo": "1234567890",
                        "userName": "김철수",
                        "createdAt": "2024-01-15T14:30:00",
                        "updatedAt": "2024-01-15T14:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public CustomApiResponse<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTransactionRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        TransactionResponse transaction = financeTransactionService.createTransaction(request, currentUserSeqNo, coupleId);
        
        return CustomApiResponse.success(transaction, "거래 내역이 성공적으로 생성되었습니다.");
    }
    
    @Operation(
        summary = "거래 내역 수정",
        description = "기존 거래 내역을 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "거래 내역 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransactionResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "transactionId": 1,
                        "amount": 600000,
                        "transactionType": "EXPENSE",
                        "description": "웨딩홀 예약금 수정",
                        "memo": "신라호텔 예약금 (수정됨)",
                        "transactionDate": "2024-01-15",
                        "transactionTime": "14:30:00",
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "userSeqNo": "1234567890",
                        "userName": "김철수",
                        "createdAt": "2024-01-15T14:30:00",
                        "updatedAt": "2024-01-15T15:00:00"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{transactionId}")
    public CustomApiResponse<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "거래 내역 ID") @PathVariable Long transactionId,
            @Valid @RequestBody UpdateTransactionRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        TransactionResponse transaction = financeTransactionService.updateTransaction(transactionId, request, coupleId);
        
        return CustomApiResponse.success(transaction, "거래 내역이 성공적으로 수정되었습니다.");
    }
    
    @Operation(
        summary = "거래 내역 삭제",
        description = "거래 내역을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "거래 내역 삭제 성공"
        )
    })
    @DeleteMapping("/{transactionId}")
    public CustomApiResponse<Void> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "거래 내역 ID") @PathVariable Long transactionId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        financeTransactionService.deleteTransaction(transactionId, coupleId);
        
        return CustomApiResponse.success(null, "거래 내역이 성공적으로 삭제되었습니다.");
    }
    

    
    /**
     * 사용자로부터 커플 ID 조회 (임시 구현)
     * TODO: 실제 사용자-커플 매핑 로직으로 교체 필요
     */
}
