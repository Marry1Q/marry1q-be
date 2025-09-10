package com.marry1q.marry1qbe.domain.account.service;

import com.marry1q.marry1qbe.domain.account.dto.request.AccountRegisterRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.IntegratedAccountListRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.AccountRegisterResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.IntegratedAccountListResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.UserRegisterResponse;
import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.domain.account.repository.AccountRepository;
import com.marry1q.marry1qbe.domain.account.service.external.AuthBackendApiService;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import com.marry1q.marry1qbe.grobal.dto.ExternalApiResponse;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.openBankingToken.OpenBankingTokenService;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpenBankingAccountService {
    
    private final AuthBackendApiService authBackendApiService;
    private final OpenBankingTokenService openBankingTokenService;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtil securityUtil;
    
    /**
     * 계좌통합조회
     */
    public CustomApiResponse<IntegratedAccountListResponse> getIntegratedAccountList() {
        try {
            log.info("계좌통합조회 요청 시작");
            
            // 1. 현재 로그인한 사용자 정보 조회
            String userSeqNo = securityUtil.getCurrentUserSeqNo();
            log.info("현재 사용자 SeqNo: {}", userSeqNo);
            
            Customer customer = customerRepository.findById(userSeqNo)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없습니다 - userSeqNo: {}", userSeqNo);
                        return new CustomException(ErrorCode.CUSTOMER_NOT_FOUND);
                    });
            
            log.info("사용자 정보 조회 성공 - 이름: {}, 이메일: {}", 
                    customer.getCustomerName(), customer.getCustomerEmail());
            
            // 2. 토큰 조회
            String token;
            Optional<String> tokenOptional = openBankingTokenService.getValidToken();
            if (tokenOptional.isEmpty()) {
                log.error("유효한 토큰이 없습니다. 토큰 발급을 시도합니다.");
                token = openBankingTokenService.issueAndSaveToken();
                log.info("토큰 발급 성공");
            } else {
                token = tokenOptional.get();
                log.info("토큰 조회 성공");
            }
            
            // 3. 요청 데이터 구성
            IntegratedAccountListRequest request = new IntegratedAccountListRequest(
                    customer.getCustomerName(),
                    customer.getCustomerNum(),
                    customer.getCustomerEmail(),
                    "Y",
                    "1",
                    String.valueOf(System.currentTimeMillis()),
                    "30"
            );
            
            log.info("계좌통합조회 요청 데이터 구성 완료 - userName: {}, userNum: {}", 
                    request.getUserName(), request.getUserNum());
            
            // 4. auth-backend API 호출
            ExternalApiResponse<IntegratedAccountListResponse> response = 
                    authBackendApiService.getIntegratedAccountList(request, token);
            
            if (!response.isSuccess()) {
                log.error("auth-backend API 호출 실패 - message: {}", response.getMessage());
                throw new CustomException(ErrorCode.INTEGRATED_ACCOUNT_LIST_FAILED);
            }
            
            log.info("계좌통합조회 성공 - 조회된 계좌 수: {}", 
                    response.getData().getResList().size());
            
            // 5. 이미 marry1q DB에 등록된 계좌 필터링
            List<IntegratedAccountListResponse.Account> allAccounts = response.getData().getResList();
            List<IntegratedAccountListResponse.Account> unregisteredAccounts = filterUnregisteredAccounts(allAccounts, userSeqNo);
            
            log.info("미등록 계좌 필터링 완료 - 전체: {}개, 미등록: {}개", 
                    allAccounts.size(), unregisteredAccounts.size());
            
            // 6. 필터링된 결과로 응답 구성
            IntegratedAccountListResponse filteredResponse = IntegratedAccountListResponse.builder()
                    .resList(unregisteredAccounts)
                    .build();
            
            return CustomApiResponse.success(filteredResponse, "계좌통합조회 성공");
            
        } catch (CustomException e) {
            log.error("계좌통합조회 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("계좌통합조회 실패 - 예상치 못한 오류", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 계좌 등록 (트랜잭션 처리)
     */
    @Transactional
    public CustomApiResponse<AccountRegisterResponse> registerAccount(AccountRegisterRequest request) {
        try {
            log.info("계좌 등록 요청 시작 - 계좌번호: {}, 은행코드: {}", 
                    request.getRegisterAccountNum(), request.getBankCodeStd());
            
            // 1. 현재 로그인한 사용자 정보 조회
            String userSeqNo = securityUtil.getCurrentUserSeqNo();
            log.info("현재 사용자 SeqNo: {}", userSeqNo);
            
            Customer customer = customerRepository.findById(userSeqNo)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없습니다 - userSeqNo: {}", userSeqNo);
                        return new CustomException(ErrorCode.CUSTOMER_NOT_FOUND);
                    });
            
            log.info("사용자 정보 조회 성공 - 이름: {}, 이메일: {}, CI: {}", 
                    customer.getCustomerName(), customer.getCustomerEmail(), customer.getUserCi());
            
            // 2. 요청 데이터에 사용자 정보 설정
            request.setUserName(customer.getCustomerName());
            request.setUserEmail(customer.getCustomerEmail());
            request.setUserCi(customer.getUserCi());
            request.setUserInfo(customer.getCustomerInfo());
            request.setAccountAlias(request.getAccountName());  // 계좌별칭을 계좌명으로 설정
            
            log.info("계좌 등록 요청 데이터 설정 완료 - userName: {}, userEmail: {}, userCi: {}", 
                    request.getUserName(), request.getUserEmail(), request.getUserCi());
            
            // 3. 중복 계좌 확인
            Optional<Account> existingAccount = accountRepository.findByAccountNumber(request.getRegisterAccountNum());
            if (existingAccount.isPresent()) {
                log.warn("이미 등록된 계좌입니다 - 계좌번호: {}", request.getRegisterAccountNum());
                throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
            }
            
            // 4. 토큰 조회
            String token;
            Optional<String> tokenOptional = openBankingTokenService.getValidToken();
            if (tokenOptional.isEmpty()) {
                log.error("유효한 토큰이 없습니다. 토큰 발급을 시도합니다.");
                token = openBankingTokenService.issueAndSaveToken();
                log.info("토큰 발급 성공");
            } else {
                token = tokenOptional.get();
                log.info("토큰 조회 성공");
            }
            
            // 5. auth-backend API 호출
            ExternalApiResponse<UserRegisterResponse> response = 
                    authBackendApiService.registerUserAccount(request, token);
            
            if (!response.isSuccess()) {
                log.error("auth-backend API 호출 실패 - message: {}", response.getMessage());
                throw new CustomException(ErrorCode.ACCOUNT_REGISTER_FAILED);
            }
            
            log.info("auth-backend 계좌 등록 성공 - userSeqNo: {}, accountNum: {}", 
                    response.getData().getUserSeqNo(), response.getData().getAccountNum());
            
            // 6. marry1q DB에 계좌 정보 저장 (트랜잭션 내에서)
            UserRegisterResponse registerResponse = response.getData();
            Account account = Account.builder()
                    .userSeqNo(registerResponse.getUserSeqNo())
                    .bank(request.getBankCodeStd())
                    .accountNumber(registerResponse.getAccountNum())
                    .accountName(request.getAccountName())
                    .isCoupleAccount(request.isCoupleAccount())
                    .accountType(request.getAccountType())
                    .build();
            
            accountRepository.save(account);
            log.info("marry1q DB 계좌 정보 저장 완료 - 계좌번호: {}", account.getAccountNumber());
            
            // 7. 응답 데이터 구성
            AccountRegisterResponse responseData = AccountRegisterResponse.builder()
                    .userSeqNo(registerResponse.getUserSeqNo())
                    .accountNum(registerResponse.getAccountNum())
                    .accountName(request.getAccountName())
                    .accountType(request.getAccountType())
                    .isCoupleAccount(request.isCoupleAccount())
                    .build();
            
            log.info("계좌 등록 완료 - 계좌번호: {}, 계좌명: {}", 
                    responseData.getAccountNum(), responseData.getAccountName());
            
            return CustomApiResponse.success(responseData, "계좌 등록 성공");
            
        } catch (CustomException e) {
            log.error("계좌 등록 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("계좌 등록 실패 - 예상치 못한 오류", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 이미 marry1q DB에 등록된 계좌를 필터링하여 미등록 계좌만 반환
     */
    private List<IntegratedAccountListResponse.Account> filterUnregisteredAccounts(
            List<IntegratedAccountListResponse.Account> allAccounts, String userSeqNo) {
        
        log.info("계좌 필터링 시작 - 전체 계좌 수: {}", allAccounts.size());
        
        // 현재 사용자의 등록된 계좌번호 목록 조회
        List<String> registeredAccountNumbers = accountRepository.findByUserSeqNo(userSeqNo)
                .stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toList());
        
        log.info("사용자 등록 계좌 수: {}", registeredAccountNumbers.size());
        
        // 미등록 계좌만 필터링
        List<IntegratedAccountListResponse.Account> unregisteredAccounts = allAccounts.stream()
                .filter(account -> !registeredAccountNumbers.contains(account.getAccountNum()))
                .collect(Collectors.toList());
        
        log.info("필터링 결과 - 미등록 계좌 수: {}", unregisteredAccounts.size());
        
        return unregisteredAccounts;
    }
}
