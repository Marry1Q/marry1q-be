package com.marry1q.marry1qbe.domain.account.service;

import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.domain.account.entity.CoupleAccountTransaction;
import com.marry1q.marry1qbe.domain.account.repository.AccountRepository;
import com.marry1q.marry1qbe.domain.account.repository.CoupleAccountTransactionRepository;
import com.marry1q.marry1qbe.domain.account.service.external.AuthBackendApiService;
import com.marry1q.marry1qbe.domain.customer.service.CustomerService;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.plan1q.dto.hanabank.AccountSubscriptionResponse;
import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QProduct;
import com.marry1q.marry1qbe.grobal.openBankingToken.OpenBankingTokenService;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marry1q.marry1qbe.domain.account.dto.response.MyAccountsResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountInfoResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.TransactionResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.TransactionReviewResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountHolderNameResponse;
import com.marry1q.marry1qbe.domain.account.dto.request.AccountHolderNameRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final CoupleAccountTransactionRepository coupleAccountTransactionRepository;
    private final CustomerService customerService;
    private final OpenBankingTokenService openBankingTokenService;
    private final AuthBackendApiService authBackendApiService;
    private final SecurityUtil securityUtil;
    
    /**
     * 모임통장 정보 조회 (실시간 잔액 포함)
     */
    @Transactional(readOnly = true)
    public AccountInfoResponse getCoupleAccountInfo() {
        Account coupleAccount = customerService.getCurrentUserCoupleAccount();
        
        // 실시간 잔액 조회
        BigDecimal realTimeBalance;
        try {
            realTimeBalance = getRealTimeBalance(coupleAccount);
        } catch (Exception e) {
            log.error("실시간 잔액 조회 실패: {}", e.getMessage(), e);
            // 실시간 조회 실패 시 기본값 설정
            realTimeBalance = BigDecimal.ZERO;
        }
        
        // Entity를 DTO로 변환
        return convertToAccountInfoResponse(coupleAccount, realTimeBalance);
    }
    
    /**
     * 실시간 잔액 조회
     */
    private BigDecimal getRealTimeBalance(Account account) {
        // 오픈뱅킹 토큰 획득
        String token = openBankingTokenService.getValidToken()
                .orElseGet(() -> openBankingTokenService.issueAndSaveToken());
        
        // 소유주 user_seq_no 조회
        String ownerUserSeqNo = customerService.getCoupleAccountOwnerUserSeqNo();
        
        // 잔액 조회 요청 생성 (소유주 user_seq_no 사용)
        AuthBackendApiService.BalanceRequest request = new AuthBackendApiService.BalanceRequest(
                ownerUserSeqNo, // 소유주 user_seq_no 사용
                "081", // 하나은행
                account.getAccountNumber()
        );
        
        // 잔액 조회 API 호출
        var response = authBackendApiService.getAccountBalance(request, token);
        
        if (response != null && response.isSuccess() && response.getData() != null) {
            return response.getData().getBalanceAsBigDecimal();
        } else {
            throw new RuntimeException("잔액 조회에 실패했습니다.");
        }
    }
    
    /**
     * 개인 계좌 목록 조회 (모임통장 제외) + 실시간 잔액 조회
     * plan1q_product_id가 null이 아닌 경우는 제외
     */
    @Transactional(readOnly = true)
    public MyAccountsResponse getMyAccounts() {
        String userSeqNo = securityUtil.getCurrentUserSeqNo();
        List<Account> allAccounts = accountRepository.findByUserSeqNo(userSeqNo);
        
        // 필터링: plan1q_product_id가 null이 아닌 경우 제외
        List<Account> filteredAccounts = allAccounts.stream()
                .filter(account -> {
                    // plan1q_product_id가 null이 아닌 경우 제외
                    if (account.getPlan1qProductId() != null) {
                        return false;
                    }
                    // 모임통장도 제외
                    return !Boolean.TRUE.equals(account.getIsCoupleAccount());
                })
                .collect(Collectors.toList());
        
        List<MyAccountsResponse.AccountInfo> accountInfos = new ArrayList<>();
        
        for (Account account : filteredAccounts) {
            MyAccountsResponse.AccountInfo accountInfo = createAccountInfo(account);
            accountInfos.add(accountInfo);
        }
        
        return MyAccountsResponse.builder()
                .accounts(accountInfos)
                .totalCount(accountInfos.size())
                .build();
    }
    
    /**
     * 개인 계좌 정보 생성 (실시간 잔액 조회 포함)
     */
    private MyAccountsResponse.AccountInfo createAccountInfo(Account account) {
        MyAccountsResponse.AccountInfo.AccountInfoBuilder builder = MyAccountsResponse.AccountInfo.builder()
                .accountId(account.getAccountId())
                .bank(account.getBank())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .isCoupleAccount(account.getIsCoupleAccount())
                .userSeqNo(account.getUserSeqNo())
                .lastSyncedAt(account.getLastSyncedAt());
        
        // 실시간 잔액 조회
        try {
            BigDecimal balance = getRealTimeBalanceForPersonalAccount(account);
            builder.balance(balance)
                   .balanceStatus("SUCCESS");
        } catch (Exception e) {
            log.warn("개인 계좌 잔액 조회 실패 - 계좌ID: {}, 계좌번호: {}, 오류: {}", 
                    account.getAccountId(), account.getAccountNumber(), e.getMessage());
            builder.balance(null)
                   .balanceStatus("ERROR")
                   .errorMessage(e.getMessage());
        }
        
        return builder.build();
    }
    
    /**
     * 개인 계좌 실시간 잔액 조회
     */
    private BigDecimal getRealTimeBalanceForPersonalAccount(Account account) {
        // 오픈뱅킹 토큰 획득
        String token = openBankingTokenService.getValidToken()
                .orElseGet(() -> openBankingTokenService.issueAndSaveToken());
        
        // 잔액 조회 요청 생성 (개인 계좌 소유자의 user_seq_no 사용)
        AuthBackendApiService.BalanceRequest request = new AuthBackendApiService.BalanceRequest(
                account.getUserSeqNo(), // 개인 계좌 소유자의 user_seq_no
                account.getBank(), // 계좌의 은행 코드
                account.getAccountNumber() // 계좌번호
        );
        
        // 잔액 조회 API 호출
        var response = authBackendApiService.getAccountBalance(request, token);
        
        if (response != null && response.isSuccess() && response.getData() != null) {
            return response.getData().getBalanceAsBigDecimal();
        } else {
            throw new RuntimeException("잔액 조회에 실패했습니다.");
        }
    }
    
    /**
     * 모임통장 거래내역 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Pageable pageable) {
        // 현재 사용자의 모임통장 조회 및 권한 검증
        Account coupleAccount = customerService.getCurrentUserCoupleAccount();
        
        // 모임통장 소유주의 user_seq_no 로깅 (디버깅용)
        String ownerUserSeqNo = customerService.getCoupleAccountOwnerUserSeqNo();
        String currentUserSeqNo = securityUtil.getCurrentUserSeqNo();
        log.info("거래내역 조회 - 현재 사용자: {}, 모임통장 소유주: {}, 계좌번호: {}", 
                currentUserSeqNo, ownerUserSeqNo, coupleAccount.getAccountNumber());
        
        Page<CoupleAccountTransaction> transactionPage = coupleAccountTransactionRepository.findByAccountIdOrderByTransactionDateDescTransactionTimeDesc(
                coupleAccount.getAccountId(), pageable);
        
        // Entity Page를 DTO Page로 변환
        return transactionPage.map(this::convertToTransactionResponse);
    }
    
    /**
     * 리뷰 대기 거래내역 조회
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getReviewTransactions() {
        Account coupleAccount = customerService.getCurrentUserCoupleAccount();
        return coupleAccountTransactionRepository.findByAccountIdOrderByTransactionDateDescTransactionTimeDesc(
                coupleAccount.getAccountId())
                .stream()
                .filter(transaction -> transaction.getReviewStatus() == CoupleAccountTransaction.ReviewStatus.PENDING)
                .map(this::convertToTransactionResponse)
                .toList();
    }
    
    /**
     * 거래내역 리뷰 상태 변경
     */
    @Transactional
    public TransactionReviewResponse updateTransactionReview(Long transactionId, String reviewStatus, Long categoryId, String memo) {
        CoupleAccountTransaction transaction = coupleAccountTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래내역을 찾을 수 없습니다. ID: " + transactionId));
        
        // 현재 사용자의 모임통장 거래내역인지 확인
        Account coupleAccount = customerService.getCurrentUserCoupleAccount();
        if (!transaction.getAccountId().equals(coupleAccount.getAccountId())) {
            throw new IllegalArgumentException("해당 거래내역에 대한 수정 권한이 없습니다.");
        }
        
        log.info("거래내역 리뷰 상태 변경 - ID: {}, 현재 상태: {}, 요청 상태: {}", 
                transactionId, transaction.getReviewStatus(), reviewStatus);
        
        // 리뷰 상태 업데이트
        if ("reviewed".equals(reviewStatus)) {
            // 메모가 제공된 경우 업데이트
            if (memo != null && !memo.trim().isEmpty()) {
                transaction = CoupleAccountTransaction.builder()
                        .accountTransactionId(transaction.getAccountTransactionId())
                        .tranId(transaction.getTranId())
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .description(transaction.getDescription())
                        .memo(memo)
                        .transactionDate(transaction.getTransactionDate())
                        .transactionTime(transaction.getTransactionTime())
                        .fromName(transaction.getFromName())
                        .toName(transaction.getToName())
                        .reviewStatus(CoupleAccountTransaction.ReviewStatus.REVIEWED)
                        .accountNumber(transaction.getAccountNumber())
                        .accountId(transaction.getAccountId())
                        .financeCategoryId(categoryId != null ? categoryId : transaction.getFinanceCategoryId())
                        .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                        .createdAt(transaction.getCreatedAt())
                        .updatedAt(LocalDateTime.now())
                        .build();
            } else {
                // 메모가 없는 경우 기존 엔티티의 상태만 변경
                transaction = CoupleAccountTransaction.builder()
                        .accountTransactionId(transaction.getAccountTransactionId())
                        .tranId(transaction.getTranId())
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .description(transaction.getDescription())
                        .memo(transaction.getMemo())
                        .transactionDate(transaction.getTransactionDate())
                        .transactionTime(transaction.getTransactionTime())
                        .fromName(transaction.getFromName())
                        .toName(transaction.getToName())
                        .reviewStatus(CoupleAccountTransaction.ReviewStatus.REVIEWED)
                        .accountNumber(transaction.getAccountNumber())
                        .accountId(transaction.getAccountId())
                        .financeCategoryId(categoryId != null ? categoryId : transaction.getFinanceCategoryId())
                        .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                        .createdAt(transaction.getCreatedAt())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }
            
            log.info("거래내역 리뷰 상태를 REVIEWED로 변경 - ID: {}", transactionId);
        }
        
        CoupleAccountTransaction updatedTransaction = coupleAccountTransactionRepository.save(transaction);
        
        // Entity를 DTO로 변환
        return convertToTransactionReviewResponse(updatedTransaction, categoryId);
    }
    
    /**
     * 거래내역 동기화 (최근 동기화 시간 이후)
     */
    @Transactional
    public void syncTransactions() {
        log.info("거래내역 동기화 시작");
        
        // 1. 현재 사용자의 모임통장 조회
        Account coupleAccount = customerService.getCurrentUserCoupleAccount();
        LocalDateTime lastSyncedAt = coupleAccount.getLastSyncedAt();
        
        // 2. 모임통장 소유주의 user_seq_no 조회 및 로깅
        String ownerUserSeqNo = customerService.getCoupleAccountOwnerUserSeqNo();
        String currentUserSeqNo = securityUtil.getCurrentUserSeqNo();
        log.info("거래내역 동기화 - 현재 사용자: {}, 모임통장 소유주: {}, 계좌번호: {}", 
                currentUserSeqNo, ownerUserSeqNo, coupleAccount.getAccountNumber());
        
        // 3. 오픈뱅킹 토큰 획득
        String token = openBankingTokenService.getValidToken()
                .orElseGet(() -> openBankingTokenService.issueAndSaveToken());
        
        // 4. 거래내역조회 API 호출
        try {
            AuthBackendApiService.TransactionListRequest request = createTransactionListRequest(coupleAccount, lastSyncedAt);
            var response = authBackendApiService.getTransactionHistory(request, token);
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                // 5. 새로운 거래내역 저장
                saveNewTransactions(response.getData(), coupleAccount);
                
                // 6. last_synced_at 업데이트
                coupleAccount.updateLastSyncedAt();
                accountRepository.save(coupleAccount);
                
                log.info("거래내역 동기화 완료");
            }
            
        } catch (Exception e) {
            log.error("거래내역 동기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("거래내역 동기화에 실패했습니다.", e);
        }
    }
    
    /**
     * 거래내역조회 요청 생성
     */
    private AuthBackendApiService.TransactionListRequest createTransactionListRequest(Account coupleAccount, LocalDateTime lastSyncedAt) {
        // 모임통장 소유주의 user_seq_no 사용 (현재 사용자가 아닌)
        String ownerUserSeqNo = customerService.getCoupleAccountOwnerUserSeqNo();
        String fromDate = lastSyncedAt != null ? 
                lastSyncedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : 
                LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        log.info("거래내역조회 요청 생성 - 소유주 user_seq_no: {}, 계좌번호: {}, 조회기간: {} ~ {}", 
                ownerUserSeqNo, coupleAccount.getAccountNumber(), fromDate, toDate);
        
        return new AuthBackendApiService.TransactionListRequest(
                ownerUserSeqNo, // 모임통장 소유주의 user_seq_no 사용
                "081", // 하나은행
                coupleAccount.getAccountNumber(),
                fromDate,
                toDate
        );
    }
    
    /**
     * 새로운 거래내역 저장
     */
    private void saveNewTransactions(AuthBackendApiService.TransactionListResponse responseData, Account coupleAccount) {
        if (responseData.getResList() == null || responseData.getResList().isEmpty()) {
            log.info("새로운 거래내역이 없습니다.");
            return;
        }
        
        int savedCount = 0;
        for (AuthBackendApiService.TransactionItem item : responseData.getResList()) {
            try {
                // 중복 체크
                if (isDuplicateTransaction(item, coupleAccount.getAccountId())) {
                    log.debug("중복 거래내역 건너뛰기: {}", item.getBankTranId());
                    continue;
                }
                
                // CoupleAccountTransaction 엔티티 생성
                CoupleAccountTransaction transaction = createTransactionFromItem(item, coupleAccount);
                
                // 저장
                coupleAccountTransactionRepository.save(transaction);
                savedCount++;
                
            } catch (Exception e) {
                log.error("거래내역 저장 실패: {}", e.getMessage(), e);
            }
        }
        
        log.info("새로운 거래내역 {}건 저장 완료", savedCount);
    }
    
    /**
     * 중복 거래내역 체크
     */
    private boolean isDuplicateTransaction(AuthBackendApiService.TransactionItem item, Long accountId) {
        // tranId로 중복 체크
        if (item.getBankTranId() != null && !item.getBankTranId().isEmpty()) {
            return coupleAccountTransactionRepository.existsByTranId(item.getBankTranId());
        }
        
        // 거래일시+금액으로 중복 체크
        LocalDate tranDate = LocalDate.parse(item.getTranDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalTime tranTime = LocalTime.parse(item.getTranTime(), DateTimeFormatter.ofPattern("HHmmss"));
        BigDecimal amount = new BigDecimal(item.getTranAmt());
        
        return coupleAccountTransactionRepository.existsByTransactionDateAndTransactionTimeAndAmountAndAccountId(
                tranDate, tranTime, amount, accountId);
    }
    
    /**
     * TransactionItem을 CoupleAccountTransaction으로 변환
     */
    private CoupleAccountTransaction createTransactionFromItem(AuthBackendApiService.TransactionItem item, Account coupleAccount) {
        LocalDate tranDate = LocalDate.parse(item.getTranDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalTime tranTime = LocalTime.parse(item.getTranTime(), DateTimeFormatter.ofPattern("HHmmss"));
        BigDecimal amount = new BigDecimal(item.getTranAmt());
        BigDecimal afterBalance = new BigDecimal(item.getAfterBalanceAmt());
        
        // 거래 타입 결정
        CoupleAccountTransaction.TransactionType type = "입금".equals(item.getInoutType()) ? 
                CoupleAccountTransaction.TransactionType.DEPOSIT : 
                CoupleAccountTransaction.TransactionType.WITHDRAW;
        
        return CoupleAccountTransaction.builder()
                .tranId(item.getBankTranId())
                .type(type)
                .amount(amount)
                .description(item.getPrintedContent())
                .transactionDate(tranDate)
                .transactionTime(tranTime)
                .fromName(null) // 미분류내역은 사용자 정보가 null이어야 함
                .toName(null) // 미분류내역은 사용자 정보가 null이어야 함
                .reviewStatus(CoupleAccountTransaction.ReviewStatus.PENDING)
                .accountNumber(coupleAccount.getAccountNumber())
                .accountId(coupleAccount.getAccountId())
                .balanceAfterTransaction(afterBalance)
                .build();
    }
    
    /**
     * Account Entity를 AccountInfoResponse DTO로 변환
     */
    private AccountInfoResponse convertToAccountInfoResponse(Account account, BigDecimal balance) {
        return AccountInfoResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .bankName(account.getBank().equals("081") ? "하나은행" : account.getBank())
                .balance(balance)
                .lastSyncedAt(account.getLastSyncedAt())
                .isActive(true) // 기본값으로 설정
                .safeAccountNumber(account.getSafeAccountNumber()) // 안심계좌번호 추가
                .build();
    }
    
    /**
     * CoupleAccountTransaction Entity를 TransactionReviewResponse DTO로 변환
     */
    private TransactionReviewResponse convertToTransactionReviewResponse(CoupleAccountTransaction transaction, Long categoryId) {
        TransactionReviewResponse.CategoryInfo categoryInfo = null;
        if (categoryId != null) {
            categoryInfo = TransactionReviewResponse.CategoryInfo.builder()
                    .id(categoryId)
                    .name("카테고리명") // TODO: 실제 카테고리명 조회 필요
                    .build();
        }
        
        return TransactionReviewResponse.builder()
                .id(transaction.getAccountTransactionId())
                .reviewStatus(transaction.getReviewStatus().name().toLowerCase())
                .category(categoryInfo)
                .memo(transaction.getMemo())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
    
    /**
     * CoupleAccountTransaction Entity를 TransactionResponse DTO로 변환
     */
    private TransactionResponse convertToTransactionResponse(CoupleAccountTransaction transaction) {
        TransactionResponse.CategoryInfo categoryInfo = null;
        if (transaction.getFinanceCategoryId() != null) {
            categoryInfo = TransactionResponse.CategoryInfo.builder()
                    .id(transaction.getFinanceCategoryId())
                    .name("카테고리명") // TODO: 실제 카테고리명 조회 필요
                    .build();
        }
        
        return TransactionResponse.builder()
                .id(transaction.getAccountTransactionId())
                .type(transaction.getType().name().toLowerCase())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .date(transaction.getTransactionDate())
                .time(transaction.getTransactionTime())
                .fromName(transaction.getFromName())
                .toName(transaction.getToName())
                .reviewStatus(transaction.getReviewStatus().name().toLowerCase())
                .category(categoryInfo)
                .memo(transaction.getMemo())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .build();
    }
    
    /**
     * 계좌주명 조회
     * 
     * auth-backend를 통해 계좌번호와 은행코드로 실제 계좌의 예금주명을 조회합니다.
     * 
     * @param request 계좌주명 조회 요청 (은행코드, 계좌번호)
     * @return 계좌주명 응답
     * @throws IllegalArgumentException 존재하지 않는 계좌인 경우
     */
    @Transactional(readOnly = true)
    public AccountHolderNameResponse getAccountHolderName(AccountHolderNameRequest request) {
        log.info("계좌주명 조회 시작 - 은행코드: {}, 계좌번호: {}", request.getBankCode(), request.getAccountNumber());
        
        try {
            // 현재 사용자의 오픈뱅킹 토큰 조회
            String accessToken = openBankingTokenService.getValidToken()
                    .orElseThrow(() -> new IllegalStateException("유효한 오픈뱅킹 토큰을 찾을 수 없습니다."));
            log.debug("오픈뱅킹 토큰 조회 완료");
            
            // auth-backend를 통해 계좌 상세정보 조회
            String accountHolderName = authBackendApiService.getAccountHolderName(
                accessToken, 
                request.getBankCode(), 
                request.getAccountNumber()
            );
            
            if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
                log.warn("계좌주명이 비어있음 - 은행코드: {}, 계좌번호: {}", request.getBankCode(), request.getAccountNumber());
                throw new IllegalArgumentException("계좌 정보를 조회할 수 없습니다.");
            }
            
            log.info("계좌주명 조회 성공 - 계좌주명: {}", accountHolderName);
            
            return AccountHolderNameResponse.builder()
                    .accountHolderName(accountHolderName)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.error("존재하지 않는 계좌 - 은행코드: {}, 계좌번호: {}, 오류: {}", 
                     request.getBankCode(), request.getAccountNumber(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("계좌주명 조회 실패 - 은행코드: {}, 계좌번호: {}, 오류: {}", 
                     request.getBankCode(), request.getAccountNumber(), e.getMessage(), e);
            throw new RuntimeException("계좌주명 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * Plan1Q 상품 계좌 저장
     */
    @Transactional
    public Account savePlan1qAccount(AccountSubscriptionResponse hanaResponse, 
                                    Customer customer, 
                                    Plan1QProduct plan1QProduct) {
        try {
            log.info("Plan1Q 계좌 저장 시작 - 상품ID: {}, 계좌번호: {}", 
                    plan1QProduct.getPlan1qProductId(), hanaResponse.getAccountNumber());
            
            // 중복 검증
            Optional<Account> existingAccount = accountRepository.findByPlan1qProductId(plan1QProduct.getPlan1qProductId());
            if (existingAccount.isPresent()) {
                log.warn("이미 가입된 Plan1Q 상품입니다 - 상품ID: {}", plan1QProduct.getPlan1qProductId());
                throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS, "이미 가입된 Plan1Q 상품입니다.");
            }
            
            // 계좌 생성
            Account account = Account.builder()
                .userSeqNo(customer.getUserSeqNo())
                .bank("081") // 하나은행 코드
                .accountNumber(hanaResponse.getAccountNumber())
                .accountName(plan1QProduct.getProductName()) // Plan1Q 상품명 사용
                .isCoupleAccount(false)
                .accountType("P1Q") // 요청대로 "P1Q"
                .plan1qProductId(plan1QProduct.getPlan1qProductId()) // 핵심: Plan1Q 상품 ID 연결
                .build();
            
            Account savedAccount = accountRepository.save(account);
            log.info("Plan1Q 계좌 저장 완료 - 계좌ID: {}, 계좌번호: {}, 상품ID: {}", 
                    savedAccount.getAccountId(), savedAccount.getAccountNumber(), savedAccount.getPlan1qProductId());
            
            return savedAccount;
            
        } catch (CustomException e) {
            log.error("Plan1Q 계좌 저장 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Plan1Q 계좌 저장 실패 - 예상치 못한 오류", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Plan1Q 계좌 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
