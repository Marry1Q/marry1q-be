package com.marry1q.marry1qbe.domain.account.controller;

import com.marry1q.marry1qbe.domain.account.dto.request.DepositRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.WithdrawRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferCreateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferUpdateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.TransactionReviewRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AccountHolderNameRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.DepositResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.WithdrawResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.AutoTransferResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.MyAccountsResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountInfoResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.TransactionReviewResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.TransactionResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountHolderNameResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.ProductPaymentInfoResponse;
import com.marry1q.marry1qbe.domain.account.service.DepositService;
import com.marry1q.marry1qbe.domain.account.service.WithdrawService;
import com.marry1q.marry1qbe.domain.account.service.AutoTransferService;
import com.marry1q.marry1qbe.domain.account.service.AccountService;
import com.marry1q.marry1qbe.domain.account.exception.InsufficientBalanceException;
import com.marry1q.marry1qbe.domain.account.exception.WithdrawTransferException;
import com.marry1q.marry1qbe.domain.account.exception.DepositTransferException;
import com.marry1q.marry1qbe.domain.account.exception.AutoTransferNotFoundException;
import com.marry1q.marry1qbe.grobal.exception.ExternalApiException;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "모임통장", description = "모임통장 관련 API")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {
    
    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final AutoTransferService autoTransferService;
    private final AccountService accountService;
    
    @PostMapping("/deposit")
    @Operation(
        summary = "모임통장 채우기", 
        description = "다른 계좌에서 모임통장으로 금액을 채우는 기능입니다. 출금 계좌번호와 출금 은행 코드를 입력하면 해당 계좌에서 모임통장으로 이체됩니다. 출금이체 → 입금이체 순차 처리로 원자성을 보장합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "모임통장 채우기 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepositResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "모임통장에 100,000원이 성공적으로 입금되었습니다.",
                        "data": {
                            "transactionId": "TXN123456789",
                            "accountNumber": "110-654321-098765",
                            "amount": 100000,
                            "balanceAfterTransaction": 500000,
                            "status": "SUCCESS"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "출금 계좌 번호는 필수입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "유효하지 않은 토큰입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "거래 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    }
                    """
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "모임통장 채우기 요청 정보",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DepositRequest.class),
            examples = @ExampleObject(
                name = "모임통장 채우기 요청 예시",
                value = """
                {
                    "withdrawAccountNumber": "110-123456-789012",
                    "withdrawBankCode": "081",
                    "amount": 100000,
                    "description": "모임통장 채우기",
                    "memo": "월급 입금",
                    "fromName": "김철수",
                    "toName": "이영희"
                }
                """
            )
        )
    )
    public ResponseEntity<CustomApiResponse<DepositResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {
        
        log.info("=====================================================");
        log.info("🏦 [MARRY1Q-BE] 모임통장 채우기 요청 수신");
        log.info("=====================================================");
        log.info("👤 사용자: {}", userDetails.getUsername());
        log.info("📤 요청 데이터:");
        log.info("  ├─ 출금계좌번호: {}", request.getWithdrawAccountNumber());
        log.info("  ├─ 출금은행코드: {}", request.getWithdrawBankCode());
        log.info("  ├─ 금액: {:,}원", request.getAmount().intValue());
        log.info("  ├─ 모임통장용 설명: {}", request.getDepositDescription());
        log.info("  ├─ 출금계좌용 설명: {}", request.getWithdrawDescription());
        log.info("  ├─ 메모: {}", request.getMemo());
        log.info("  ├─ 보낸사람: {}", request.getFromName());
        log.info("  └─ 받는사람: {}", request.getToName());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("=====================================================");
        
        try {
            DepositResponse response = depositService.processDeposit(request);
            
            // 거래 완료 후 즉시 동기화 실행 (거래내역 반영)
            try {
                accountService.syncTransactions();
                log.info("채우기 후 동기화 완료");
            } catch (Exception syncException) {
                log.warn("채우기 후 동기화 실패 (거래는 성공): {}", syncException.getMessage());
            }
            
            String message = String.format("모임통장에 %,d원이 성공적으로 입금되었습니다.", request.getAmount().intValue());
            
            log.info("=====================================================");
            log.info("✅ [MARRY1Q-BE] 모임통장 채우기 처리 성공");
            log.info("=====================================================");
            log.info("📥 응답 데이터:");
            log.info("  ├─ 거래ID: {}", response.getTransactionId());
            log.info("  ├─ 계좌번호: {}", response.getAccountNumber());
            log.info("  ├─ 거래금액: {:,}원", response.getAmount().intValue());
            log.info("  ├─ 거래후잔액: {:,}원", response.getBalanceAfterTransaction().intValue());
            log.info("  ├─ 상태: {}", response.getStatus());
            log.info("  └─ 완료시간: {}", response.getCompletedAt());
            log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
            log.info("=====================================================");
            
            return ResponseEntity.ok(CustomApiResponse.success(response, message));
            
        } catch (WithdrawTransferException e) {
            log.error("출금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("WITHDRAW_TRANSFER_ERROR", e.getMessage()));
            
        } catch (DepositTransferException e) {
            log.error("입금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("DEPOSIT_TRANSFER_ERROR", e.getMessage()));
            
        } catch (Exception e) {
            log.error("모임통장 채우기 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("DEPOSIT_PROCESS_ERROR", "거래 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }
    
    @PostMapping("/withdraw")
    @Operation(
        summary = "모임통장에서 보내기", 
        description = "모임통장에서 다른 개인 계좌로 금액을 보내는 기능입니다. 받는 사람의 은행 코드와 계좌번호를 입력하면 모임통장에서 해당 계좌로 이체됩니다. 출금이체 → 입금이체 순차 처리로 원자성을 보장합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "모임통장에서 보내기 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WithdrawResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "100,000원이 성공적으로 이체되었습니다.",
                        "data": {
                            "transactionId": "TXN987654321",
                            "accountNumber": "110-654321-098765",
                            "amount": 100000,
                            "balanceAfterTransaction": 400000,
                            "status": "SUCCESS"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 또는 잔액 부족",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "잔액 부족",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "모임통장 잔액이 부족합니다. 현재 잔액: 50,000원, 필요 금액: 100,000원"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "유효하지 않은 토큰입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "거래 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    }
                    """
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "모임통장에서 보내기 요청 정보",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = WithdrawRequest.class),
            examples = @ExampleObject(
                name = "모임통장에서 보내기 요청 예시",
                value = """
                {
                    "depositBankCode": "081",
                    "depositAccountNumber": "110-123456-789012",
                    "depositAccountHolderName": "김철수",
                    "amount": 100000,
                    "description": "용돈 보내기",
                    "memo": "12월 용돈",
                    "fromName": "이영희",
                    "toName": "김철수"
                }
                """
            )
        )
    )
    public ResponseEntity<CustomApiResponse<WithdrawResponse>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        
        log.info("=====================================================");
        log.info("💸 [MARRY1Q-BE] 모임통장에서 보내기 요청 수신");
        log.info("=====================================================");
        log.info("👤 사용자: {}", userDetails.getUsername());
        log.info("📤 요청 데이터:");
        log.info("  ├─ 받는계좌번호: {}", request.getDepositAccountNumber());
        log.info("  ├─ 받는은행코드: {}", request.getDepositBankCode());
        log.info("  ├─ 받는사람명: {}", request.getDepositAccountHolderName());
        log.info("  ├─ 금액: {:,}원", request.getAmount().intValue());
        log.info("  ├─ 개인계좌용 설명: {}", request.getDepositDescription());
        log.info("  ├─ 모임통장용 설명: {}", request.getWithdrawDescription());
        log.info("  ├─ 메모: {}", request.getMemo());
        log.info("  ├─ 보낸사람: {}", request.getFromName());
        log.info("  └─ 받는사람: {}", request.getToName());
        log.info("⏰ 요청 시간: {}", java.time.LocalDateTime.now());
        log.info("=====================================================");
        
        try {
            WithdrawResponse response = withdrawService.processWithdraw(request);
            
            // 거래 완료 후 즉시 동기화 실행 (거래내역 반영)
            try {
                accountService.syncTransactions();
                log.info("보내기 후 동기화 완료");
            } catch (Exception syncException) {
                log.warn("보내기 후 동기화 실패 (거래는 성공): {}", syncException.getMessage());
            }
            
            String message = String.format("%,d원이 성공적으로 이체되었습니다.", request.getAmount().intValue());
            
            log.info("=====================================================");
            log.info("✅ [MARRY1Q-BE] 모임통장에서 보내기 처리 성공");
            log.info("=====================================================");
            log.info("📥 응답 데이터:");
            log.info("  ├─ 거래ID: {}", response.getTransactionId());
            log.info("  ├─ 계좌번호: {}", response.getAccountNumber());
            log.info("  ├─ 거래금액: {:,}원", response.getAmount().intValue());
            log.info("  ├─ 거래후잔액: {:,}원", response.getBalanceAfterTransaction().intValue());
            log.info("  ├─ 상태: {}", response.getStatus());
            log.info("  └─ 완료시간: {}", response.getCompletedAt());
            log.info("⏰ 응답 시간: {}", java.time.LocalDateTime.now());
            log.info("=====================================================");
            
            return ResponseEntity.ok(CustomApiResponse.success(response, message));
            
        } catch (InsufficientBalanceException e) {
            log.error("잔액 부족: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(CustomApiResponse.error("INSUFFICIENT_BALANCE", e.getMessage()));
            
        } catch (WithdrawTransferException e) {
            log.error("출금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("WITHDRAW_TRANSFER_ERROR", e.getMessage()));
            
        } catch (DepositTransferException e) {
            log.error("입금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("DEPOSIT_TRANSFER_ERROR", e.getMessage()));
            
        } catch (Exception e) {
            log.error("모임통장에서 보내기 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("WITHDRAW_PROCESS_ERROR", "거래 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }
    
    @GetMapping("/info")
    @Operation(
        summary = "모임통장 정보 조회", 
        description = "현재 사용자의 모임통장 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "모임통장 정보 조회가 완료되었습니다.",
                        "data": {
                            "accountId": 1,
                            "bank": "081",
                            "accountNumber": "110-654321-098765",
                            "accountName": "모임통장",
                            "balance": 500000,
                            "createdAt": "2024-01-01T00:00:00",
                            "updatedAt": "2024-01-15T09:30:00",
                            "lastSyncedAt": "2024-01-15T09:30:00",
                            "isCoupleAccount": true,
                            "userSeqNo": "M1234567890"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<AccountInfoResponse>> getAccountInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("모임통장 정보 조회 요청 수신 - 사용자: {}", userDetails.getUsername());
        
        try {
            AccountInfoResponse account = accountService.getCoupleAccountInfo();
            
            log.info("모임통장 정보 조회 성공");
            
            return ResponseEntity.ok(CustomApiResponse.success(account, "모임통장 정보 조회가 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("모임통장 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("ACCOUNT_INFO_ERROR", "모임통장 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/sync-transactions")
    @Operation(
        summary = "거래내역 동기화", 
        description = "최근 동기화 시간 이후의 거래내역을 오픈뱅킹 서버에서 조회하여 DB에 업데이트합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "동기화 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "거래내역 동기화가 완료되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<Void>> syncTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("거래내역 동기화 요청 수신 - 사용자: {}", userDetails.getUsername());
        
        try {
            accountService.syncTransactions();
            
            log.info("거래내역 동기화 성공");
            
            return ResponseEntity.ok(CustomApiResponse.success(null, "거래내역 동기화가 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("거래내역 동기화 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("SYNC_TRANSACTIONS_ERROR", "거래내역 동기화 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/my-accounts")
    @Operation(
        summary = "개인 계좌 목록 조회", 
        description = "현재 사용자의 개인 계좌 목록을 실시간 잔액과 함께 조회합니다. (모임통장 제외)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "개인 계좌 목록 조회가 완료되었습니다.",
                        "data": {
                            "accounts": [
                                {
                                    "accountId": 2,
                                    "bank": "081",
                                    "accountNumber": "110-123456-789012",
                                    "accountName": "급여통장",
                                    "balance": 2000000,
                                    "createdAt": "2024-01-01T00:00:00",
                                    "updatedAt": "2024-01-15T09:30:00",
                                    "lastSyncedAt": "2024-01-15T09:30:00",
                                    "isCoupleAccount": false,
                                    "userSeqNo": "M1234567890"
                                }
                            ],
                            "totalCount": 1
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<MyAccountsResponse>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("개인 계좌 목록 조회 요청 - 사용자: {}", userDetails.getUsername());
        
        try {
            MyAccountsResponse response = accountService.getMyAccounts();
            
            log.info("개인 계좌 목록 조회 성공 - 건수: {}", response.getTotalCount());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "개인 계좌 목록 조회가 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("개인 계좌 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("GET_MY_ACCOUNTS_ERROR", "개인 계좌 목록 조회에 실패했습니다"));
        }
    }
    
    // ===== 자동이체 관련 API =====
    
    @PostMapping("/auto-transfers")
    @Operation(
        summary = "자동이체 등록", 
        description = "자동이체를 등록합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "자동이체 등록 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "자동이체가 등록되었습니다.",
                        "data": {
                            "id": 1,
                            "toAccountNumber": "110-123456-789012",
                            "toAccountName": "김철수",
                            "toBankCode": "081",
                            "amount": 500000,
                            "schedule": "매월 25일",
                            "nextTransferDate": "2024-02-25",
                            "memo": "용돈",
                            "fromAccountId": 1,
                            "active": true
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "계좌번호가 입력되지 않았습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<AutoTransferResponse>> createAutoTransfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AutoTransferCreateRequest request) {
        
        log.info("자동이체 등록 요청 수신 - 사용자: {}, 계좌주명: {}, 계좌번호: {}, 은행코드: {}, 금액: {}, 주기: {}", 
                 userDetails.getUsername(), request.getToAccountName(), 
                 request.getToAccountNumber(), request.getToBankCode(),
                 request.getAmount(), request.getFrequency());
        
        try {
            AutoTransferResponse response = autoTransferService.createAutoTransfer(request);
            
            log.info("자동이체 등록 성공 - ID: {}", response.getAutoTransferId());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "자동이체가 등록되었습니다."));
            
        } catch (Exception e) {
            log.error("자동이체 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("CREATE_AUTO_TRANSFER_ERROR", e.getMessage()));
        }
    }
    
    @GetMapping("/auto-transfers")
    @Operation(
        summary = "자동이체 목록 조회", 
        description = "자동이체 목록을 조회합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "자동이체 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "자동이체 목록을 조회했습니다.",
                        "data": [
                            {
                                "id": 1,
                                "toAccountNumber": "110-123456-789012",
                                "toAccountName": "김철수",
                                "toBankCode": "081",
                                "amount": 500000,
                                "schedule": "매월 25일",
                                "nextTransferDate": "2024-02-25",
                                "memo": "용돈",
                                "fromAccountId": 1,
                                "active": true
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<List<AutoTransferResponse>>> getAutoTransferList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fromAccountNumber) {
        
        log.info("자동이체 목록 조회 요청 - 사용자: {}, 출금 계좌번호: {}", userDetails.getUsername(), fromAccountNumber);
        
        try {
            List<AutoTransferResponse> response = autoTransferService.getAutoTransferList(fromAccountNumber);
            
            log.info("자동이체 목록 조회 성공 - 개수: {}", response.size());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "자동이체 목록을 조회했습니다."));
            
        } catch (Exception e) {
            log.error("자동이체 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("GET_AUTO_TRANSFER_LIST_ERROR", e.getMessage()));
        }
    }
    
    @PutMapping("/auto-transfers/{id}")
    @Operation(
        summary = "자동이체 수정", 
        description = "자동이체를 수정합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "자동이체 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "자동이체가 수정되었습니다.",
                        "data": {
                            "id": 1,
                            "toAccountNumber": "110-123456-789012",
                            "toAccountName": "김철수",
                            "toBankCode": "081",
                            "amount": 600000,
                            "schedule": "매월 25일",
                            "nextTransferDate": "2024-02-25",
                            "memo": "용돈",
                            "fromAccountId": 1,
                            "active": true
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "계좌번호가 입력되지 않았습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "자동이체를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<AutoTransferResponse>> updateAutoTransfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AutoTransferUpdateRequest request) {
        
        log.info("자동이체 수정 요청 수신 - 사용자: {}, ID: {}, 계좌주명: {}, 계좌번호: {}, 은행코드: {}, 금액: {}, 주기: {}", 
                 userDetails.getUsername(), id, request.getToAccountName(), 
                 request.getToAccountNumber(), request.getToBankCode(),
                 request.getAmount(), request.getFrequency());
        
        try {
            AutoTransferResponse response = autoTransferService.updateAutoTransfer(id, request);
            
            log.info("자동이체 수정 성공 - ID: {}", response.getAutoTransferId());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "자동이체가 수정되었습니다."));
            
        } catch (AutoTransferNotFoundException e) {
            log.error("자동이체를 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error("AUTO_TRANSFER_NOT_FOUND", e.getMessage()));
            
        } catch (IllegalArgumentException e) {
            log.error("권한 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CustomApiResponse.error("FORBIDDEN", e.getMessage()));
            
        } catch (Exception e) {
            log.error("자동이체 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("UPDATE_AUTO_TRANSFER_ERROR", e.getMessage()));
        }
    }
    
    @DeleteMapping("/auto-transfers/{id}")
    @Operation(
        summary = "자동이체 삭제", 
        description = "자동이체를 삭제합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "자동이체 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "자동이체가 삭제되었습니다.",
                        "data": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "자동이체를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<Void>> deleteAutoTransfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        log.info("자동이체 삭제 요청 수신 - 사용자: {}, ID: {}", userDetails.getUsername(), id);
        
        try {
            autoTransferService.deleteAutoTransfer(id);
            
            log.info("자동이체 삭제 성공 - ID: {}", id);
            
            return ResponseEntity.ok(CustomApiResponse.success(null, "자동이체가 삭제되었습니다."));
            
        } catch (AutoTransferNotFoundException e) {
            log.error("자동이체를 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error("AUTO_TRANSFER_NOT_FOUND", e.getMessage()));
            
        } catch (IllegalArgumentException e) {
            log.error("권한 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CustomApiResponse.error("FORBIDDEN", e.getMessage()));
            
        } catch (Exception e) {
            log.error("자동이체 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("DELETE_AUTO_TRANSFER_ERROR", e.getMessage()));
        }
    }
    

    
    @GetMapping("/transactions")
    @Operation(
        summary = "모임통장 거래내역 조회", 
        description = "모임통장의 거래내역을 조회합니다. (페이징 지원)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "거래내역 조회가 완료되었습니다.",
                        "data": [
                            {
                                "accountTransactionId": 1,
                                "tranId": "TXN123456789",
                                "type": "DEPOSIT",
                                "amount": 100000,
                                "description": "월급 입금",
                                "memo": null,
                                "transactionDate": "2024-01-15",
                                "transactionTime": "09:30:00",
                                "fromName": "김철수",
                                "toName": "모임통장",
                                "reviewStatus": "PENDING",
                                "accountNumber": "110-654321-098765",
                                "accountId": 1,
                                "financeCategoryId": null,
                                "balanceAfterTransaction": 500000,
                                "createdAt": "2024-01-15T09:30:00",
                                "updatedAt": "2024-01-15T09:30:00"
                            }
                        ],
                        "pagination": {
                            "total": 50,
                            "page": 0,
                            "size": 20,
                            "hasNext": true
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<Object>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("거래내역 조회 요청 수신 - 사용자: {}, 페이지: {}, 크기: {}", 
                userDetails.getUsername(), page, size);
        
        try {
            // 거래내역 동기화 먼저 수행
            accountService.syncTransactions();
            
            // 페이징된 거래내역 조회
            var pageable = org.springframework.data.domain.PageRequest.of(page, size);
            var transactions = accountService.getTransactions(pageable);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("transactions", transactions.getContent());
            responseData.put("pagination", Map.of(
                "total", transactions.getTotalElements(),
                "page", transactions.getNumber(),
                "size", transactions.getSize(),
                "hasNext", transactions.hasNext()
            ));
            
            log.info("거래내역 조회 성공 - 총 건수: {}, 현재 페이지: {}", 
                    transactions.getTotalElements(), transactions.getNumber());
            
            return ResponseEntity.ok(CustomApiResponse.success(responseData, "거래내역 조회가 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("거래내역 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("GET_TRANSACTIONS_ERROR", e.getMessage()));
        }
    }
    
    @GetMapping("/transactions/review")
    @Operation(
        summary = "리뷰 대기 거래내역 조회", 
        description = "리뷰가 필요한 거래내역들을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "리뷰 대기 거래내역 조회가 완료되었습니다.",
                        "data": [
                            {
                                "accountTransactionId": 1,
                                "tranId": "TXN123456789",
                                "type": "DEPOSIT",
                                "amount": 100000,
                                "description": "월급 입금",
                                "memo": null,
                                "transactionDate": "2024-01-15",
                                "transactionTime": "09:30:00",
                                "fromName": "김철수",
                                "toName": "모임통장",
                                "reviewStatus": "PENDING",
                                "accountNumber": "110-654321-098765",
                                "accountId": 1,
                                "financeCategoryId": null,
                                "balanceAfterTransaction": 500000,
                                "createdAt": "2024-01-15T09:30:00",
                                "updatedAt": "2024-01-15T09:30:00"
                            }
                        ],
                        "summary": {
                            "totalCount": 1,
                            "totalAmount": 100000
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<Object>> getReviewTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("리뷰 대기 거래내역 조회 요청 수신 - 사용자: {}", userDetails.getUsername());
        
        try {
            var transactions = accountService.getReviewTransactions();
            
            // 요약 정보 계산
            int totalCount = transactions.size();
            var totalAmount = transactions.stream()
                    .map(TransactionResponse::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("transactions", transactions);
            responseData.put("summary", Map.of(
                "totalCount", totalCount,
                "totalAmount", totalAmount
            ));
            
            log.info("리뷰 대기 거래내역 조회 성공 - 건수: {}, 총 금액: {}", totalCount, totalAmount);
            
            return ResponseEntity.ok(CustomApiResponse.success(responseData, "리뷰 대기 거래내역 조회가 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("리뷰 대기 거래내역 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("GET_REVIEW_TRANSACTIONS_ERROR", e.getMessage()));
        }
    }
    
    @PutMapping("/transactions/review/{id}")
    @Operation(
        summary = "거래내역 리뷰 상태 변경", 
        description = "거래내역의 리뷰 상태를 REVIEWED로 변경합니다. 요청 본문은 선택사항이며, 제공하지 않으면 기본값으로 리뷰 상태가 변경됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "변경 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "거래내역 리뷰 상태가 변경되었습니다.",
                        "data": {
                            "accountTransactionId": 1,
                            "tranId": "TXN123456789",
                            "type": "DEPOSIT",
                            "amount": 100000,
                            "description": "월급 입금",
                            "memo": "월급 입금 메모",
                            "transactionDate": "2024-01-15",
                            "transactionTime": "09:30:00",
                            "fromName": "김철수",
                            "toName": "모임통장",
                            "reviewStatus": "REVIEWED",
                            "accountNumber": "110-654321-098765",
                            "accountId": 1,
                            "financeCategoryId": 1,
                            "balanceAfterTransaction": 500000,
                            "createdAt": "2024-01-15T09:30:00",
                            "updatedAt": "2024-01-15T10:00:00"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "해당 거래내역에 대한 수정 권한이 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "거래내역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "거래내역 리뷰 상태 변경 요청 (선택사항)",
        required = false,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TransactionReviewRequest.class),
            examples = {
                @ExampleObject(
                    name = "요청 본문 없음",
                    summary = "요청 본문 없이 호출",
                    description = "요청 본문을 제공하지 않으면 기본값으로 리뷰 상태가 변경됩니다.",
                    value = ""
                ),
                @ExampleObject(
                    name = "기본 리뷰 상태 변경",
                    summary = "기본 리뷰 상태 변경",
                    description = "reviewStatus만 제공하여 기본 리뷰 상태로 변경",
                    value = """
                    {
                        "reviewStatus": "reviewed"
                    }
                    """
                ),
                @ExampleObject(
                    name = "카테고리와 메모 포함",
                    summary = "카테고리와 메모를 포함한 리뷰 상태 변경",
                    description = "카테고리 ID와 메모를 함께 제공하여 리뷰 상태 변경",
                    value = """
                    {
                        "reviewStatus": "reviewed",
                        "categoryId": 1,
                        "memo": "월급 입금 메모"
                    }
                    """
                )
            }
        )
    )
    public ResponseEntity<CustomApiResponse<TransactionReviewResponse>> updateTransactionReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        
        log.info("거래내역 리뷰 상태 변경 요청 수신 - 사용자: {}, 거래 ID: {}", 
                userDetails.getUsername(), id);
        
        try {
            // 요청 본문이 없거나 reviewStatus가 없으면 기본값으로 "reviewed" 설정
            String reviewStatus = "reviewed"; // 기본값
            Long categoryId = null;
            String memo = null;
            
            // 요청 본문이 있으면 파싱
            if (request != null) {
                if (request.get("reviewStatus") != null) {
                    reviewStatus = (String) request.get("reviewStatus");
                }
                if (request.get("categoryId") != null) {
                    categoryId = Long.valueOf(request.get("categoryId").toString());
                }
                if (request.get("memo") != null) {
                    memo = (String) request.get("memo");
                }
            }
            
            TransactionReviewResponse transaction = accountService.updateTransactionReview(id, reviewStatus, categoryId, memo);
            
            log.info("거래내역 리뷰 상태 변경 성공 - ID: {}, 상태: {}", id, reviewStatus);
            
            return ResponseEntity.ok(CustomApiResponse.success(transaction, "거래내역 리뷰 상태가 변경되었습니다."));
            
        } catch (IllegalArgumentException e) {
            log.error("거래내역을 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error("TRANSACTION_NOT_FOUND", e.getMessage()));
            
        } catch (Exception e) {
            log.error("거래내역 리뷰 상태 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("UPDATE_TRANSACTION_REVIEW_ERROR", e.getMessage()));
        }
    }
    
    @GetMapping("/auto-transfers/product-payment-info")
    @Operation(
        summary = "상품별 자동이체 납입 정보 조회", 
        description = "특정 상품 계좌번호로 해당 상품에 입금되는 자동이체의 납입 진행 상황을 조회합니다. " +
                "Plan1Q 상품 가입 시 해당 상품 계좌로 입금되는 모든 자동이체의 납입 현황을 확인할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "상품별 자동이체 납입 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductPaymentInfoResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "상품별 자동이체 납입 정보를 조회했습니다.",
                        "data": [
                            {
                                "autoTransferId": 1,
                                "fromAccountNumber": "110-987-654321",
                                "toAccountNumber": "110-123-456789",
                                "amount": 500000,
                                "nextPaymentDate": "2024-02-25",
                                "currentInstallment": 3,
                                "totalInstallments": 12,
                                "remainingInstallments": 9,
                                "paymentStatus": "SUCCESS",
                                "isFirstInstallment": false,
                                "lastExecutionDate": "2024-01-25"
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "계좌번호가 올바르지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CustomApiResponse<List<ProductPaymentInfoResponse>>> getProductPaymentInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String toAccountNumber) {
        
        log.info("상품별 자동이체 납입 정보 조회 요청 - 사용자: {}, 입금 계좌번호: {}", 
                 userDetails.getUsername(), toAccountNumber);
        
        try {
            List<ProductPaymentInfoResponse> response = autoTransferService.getProductPaymentInfo(toAccountNumber);
            
            log.info("상품별 자동이체 납입 정보 조회 성공 - 조회 건수: {}", response.size());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "상품별 자동이체 납입 정보를 조회했습니다."));
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error("INVALID_REQUEST", e.getMessage()));
            
        } catch (Exception e) {
            log.error("상품별 자동이체 납입 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("GET_PRODUCT_PAYMENT_INFO_ERROR", e.getMessage()));
        }
    }

    @PostMapping("/holder-name")
    @Operation(
        summary = "계좌주명 조회", 
        description = "계좌번호와 은행코드를 통해 해당 계좌의 예금주명을 조회합니다. auth-backend를 통해 실제 계좌 정보를 조회하여 계좌주명을 반환합니다. 인증된 사용자만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "계좌주명 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountHolderNameResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "계좌주명 조회가 완료되었습니다.",
                        "data": {
                            "accountHolderName": "김철수"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "계좌번호는 필수입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "유효하지 않은 토큰입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "계좌를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "계좌 없음",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "존재하지 않는 계좌입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "status": "ERROR",
                        "message": "계좌주명 조회 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "계좌주명 조회 요청 정보",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AccountHolderNameRequest.class),
            examples = @ExampleObject(
                name = "계좌주명 조회 요청 예시",
                value = """
                {
                    "bankCode": "081",
                    "accountNumber": "110-123456-789012"
                }
                """
            )
        )
    )
    public ResponseEntity<CustomApiResponse<AccountHolderNameResponse>> getAccountHolderName(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountHolderNameRequest request) {
        
        log.info("계좌주명 조회 요청 수신 - 사용자: {}, 은행코드: {}, 계좌번호: {}", 
                 userDetails.getUsername(), request.getBankCode(), request.getAccountNumber());
        
        try {
            AccountHolderNameResponse response = accountService.getAccountHolderName(request);
            
            log.info("계좌주명 조회 성공 - 계좌주명: {}", response.getAccountHolderName());
            
            return ResponseEntity.ok(CustomApiResponse.success(response, "계좌주명 조회가 완료되었습니다."));
            
        } catch (IllegalArgumentException e) {
            log.error("존재하지 않는 계좌: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error("ACCOUNT_NOT_FOUND", "존재하지 않는 계좌입니다."));
            
        } catch (ExternalApiException e) {
            log.error("외부 API 호출 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("EXTERNAL_API_ERROR", "계좌 정보 조회에 실패했습니다."));
            
        } catch (Exception e) {
            log.error("계좌주명 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("ACCOUNT_HOLDER_NAME_ERROR", "계좌주명 조회 중 오류가 발생했습니다."));
        }
    }
}
