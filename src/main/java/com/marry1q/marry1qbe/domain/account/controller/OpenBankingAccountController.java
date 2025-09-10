package com.marry1q.marry1qbe.domain.account.controller;

import com.marry1q.marry1qbe.domain.account.dto.request.AccountRegisterRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountRegisterResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.IntegratedAccountListResponse;
import com.marry1q.marry1qbe.domain.account.service.OpenBankingAccountService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "오픈뱅킹 계좌", description = "오픈뱅킹 계좌 관련 API")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class OpenBankingAccountController {
    
    private final OpenBankingAccountService openBankingAccountService;
    
    @Operation(summary = "계좌통합조회", description = "사용자의 모든 계좌 정보를 통합 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "계좌통합조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "resList": [
                          {
                            "listNum": "1",
                            "bankCodeStd": "097",
                            "activityType": "1",
                            "accountType": "1",
                            "accountNum": "1234567890123456",
                            "accountNumMasked": "1234************3456",
                            "accountSeq": "001",
                            "accountHolderName": "홍길동",
                            "accountIssueDate": "20230101",
                            "lastTranDate": "20250701",
                            "productName": "입출금통장",
                            "productSubName": "일반",
                            "dormancyYn": "N",
                            "balanceAmt": 5000000,
                            "depositAmt": 0,
                            "balanceCalcBasis1": "1",
                            "balanceCalcBasis2": "1",
                            "investmentLinkedYn": "N",
                            "bankLinkedYn": "Y",
                            "balanceAfterCancelYn": "N",
                            "savingsBankCode": "097"
                          }
                        ]
                      },
                      "message": "계좌통합조회 성공",
                      "timestamp": "2024-01-15T10:30:00"
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
                schema = @Schema(implementation = CustomApiResponse.class)
            )
        )
    })
    @PostMapping("/accountinfo-list")
    public CustomApiResponse<IntegratedAccountListResponse> getIntegratedAccountList() {
        log.info("계좌통합조회 API 호출");
        return openBankingAccountService.getIntegratedAccountList();
    }
    
    @Operation(summary = "계좌 등록", description = "사용자의 계좌 정보를 등록하고 marry1q DB에 저장합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "계좌 등록 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "data": {
                        "userSeqNo": "1234567890",
                        "accountNum": "1234567890123456",
                        "accountName": "김민수 급여통장",
                        "accountType": "입출금통장",
                        "isCoupleAccount": false
                      },
                      "message": "계좌 등록 성공",
                      "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "계좌 등록 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class)
            )
        )
    })
    @PostMapping("/register")
    public CustomApiResponse<AccountRegisterResponse> registerAccount(
            @RequestBody @Valid AccountRegisterRequest request) {
        log.info("계좌 등록 API 호출 - 계좌번호: {}", request.getRegisterAccountNum());
        return openBankingAccountService.registerAccount(request);
    }
}
