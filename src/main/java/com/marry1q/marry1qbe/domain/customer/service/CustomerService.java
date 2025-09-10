package com.marry1q.marry1qbe.domain.customer.service;

import com.marry1q.marry1qbe.domain.customer.dto.CustomerInfoResponse;
import com.marry1q.marry1qbe.domain.customer.dto.LoginRequest;
import com.marry1q.marry1qbe.domain.customer.dto.LoginResponse;
import com.marry1q.marry1qbe.domain.customer.dto.SignUpRequest;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.domain.account.entity.Account;
import com.marry1q.marry1qbe.domain.account.repository.AccountRepository;
import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import com.marry1q.marry1qbe.domain.couple.repository.Marry1qCoupleRepository;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.jwt.JwtTokenProvider;
import com.marry1q.marry1qbe.grobal.jwt.TokenBlacklistService;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AccountRepository accountRepository;
    private final Marry1qCoupleRepository coupleRepository;
    private final SecurityUtil securityUtil;
    
    @Transactional
    public void signUp(SignUpRequest request) {
        // 이메일 중복 확인
        if (customerRepository.existsByCustomerEmail(request.getCustomerEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getCustomerPw());
        
        // 핀 번호 암호화
        String encodedPin = passwordEncoder.encode(request.getCustomerPin());
        
        // 고객 정보 저장
        Customer customer = Customer.builder()
                .userSeqNo(request.getUserSeqNo())
                .userCi(request.getUserCi())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerInfo(request.getCustomerInfo())
                .customerPin(encodedPin)
                .customerEmail(request.getCustomerEmail())
                .customerPw(encodedPassword)
                .coupleId(request.getCoupleId())
                .build();
        
        customerRepository.save(customer);
    }
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        Customer customer = customerRepository.findByCustomerEmail(request.getCustomerEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getCustomerPw(), customer.getCustomerPw())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getUserSeqNo());
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getUserSeqNo());
        
        // 토큰 만료 시간 계산
        LocalDateTime accessTokenExpiredAt = LocalDateTime.now().plusSeconds(2592000); // 30일
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(5184000); // 60일
        
        // 토큰 저장
        tokenBlacklistService.saveToken(customer.getUserSeqNo(), accessToken, refreshToken, 
                                      accessTokenExpiredAt, refreshTokenExpiredAt);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(2592000L)
                .build();
    }
    
    @Transactional
    public void logout(String userSeqNo) {
        tokenBlacklistService.blacklistToken(userSeqNo);
    }
    
    public CustomerInfoResponse getCustomerInfo(String userSeqNo) {
        Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // coupleSlug 조회
        String coupleSlug = null;
        if (customer.getCoupleId() != null) {
            try {
                Marry1qCouple couple = coupleRepository.findById(customer.getCoupleId())
                        .orElse(null);
                if (couple != null) {
                    coupleSlug = couple.getUrlSlug();
                }
            } catch (Exception e) {
                log.warn("커플 정보 조회 실패 - coupleId: {}, error: {}", customer.getCoupleId(), e.getMessage());
            }
        }
        
        return CustomerInfoResponse.builder()
                .userSeqNo(customer.getUserSeqNo())
                .customerName(customer.getCustomerName())
                .customerPhone(customer.getCustomerPhone())
                .customerEmail(customer.getCustomerEmail())
                .coupleId(customer.getCoupleId())
                .coupleSlug(coupleSlug)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // DB에서 리프레시 토큰 확인
        var tokenEntity = tokenBlacklistService.getValidRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        
        // 새 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(tokenEntity.getUserSeqNo());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(tokenEntity.getUserSeqNo());
        
        // 토큰 만료 시간 계산
        LocalDateTime accessTokenExpiredAt = LocalDateTime.now().plusSeconds(2592000); // 30일
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(5184000); // 60일
        
        // 새 토큰 저장
        tokenBlacklistService.saveToken(tokenEntity.getUserSeqNo(), newAccessToken, newRefreshToken, 
                                      accessTokenExpiredAt, refreshTokenExpiredAt);
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(2592000L)
                .build();
    }
    
    /**
     * 현재 사용자의 모임통장 계좌번호 조회
     */
    public String getCurrentUserCoupleAccountNumber() {
        String userSeqNo = securityUtil.getCurrentUserSeqNo();
        
        // 1. 현재 사용자의 couple_id 조회
        Customer customer = customerRepository.findById(userSeqNo)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userSeqNo: " + userSeqNo));
        
        Long coupleId = customer.getCoupleId();
        if (coupleId == null) {
            throw new IllegalArgumentException("사용자가 커플에 속해있지 않습니다. userSeqNo: " + userSeqNo);
        }
        
        // 2. 커플의 모임통장 계좌번호 조회
        Marry1qCouple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new IllegalArgumentException("커플 정보를 찾을 수 없습니다. coupleId: " + coupleId));
        
        return couple.getCoupleAccount();
    }
    
    /**
     * 사용자의 모임통장 정보 조회
     */
    public Account getCurrentUserCoupleAccount() {
        String userSeqNo = securityUtil.getCurrentUserSeqNo();
        
        // 1. 현재 사용자의 couple_id 조회
        Customer customer = customerRepository.findById(userSeqNo)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userSeqNo: " + userSeqNo));
        
        Long coupleId = customer.getCoupleId();
        if (coupleId == null) {
            throw new IllegalArgumentException("사용자가 커플에 속해있지 않습니다. userSeqNo: " + userSeqNo);
        }
        
        // 2. 커플의 모임통장 계좌번호 조회
        Marry1qCouple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new IllegalArgumentException("커플 정보를 찾을 수 없습니다. coupleId: " + coupleId));
        
        String coupleAccountNumber = couple.getCoupleAccount();
        
        // 3. 모임통장 계좌 정보 조회
        Account coupleAccount = accountRepository.findByAccountNumber(coupleAccountNumber)
            .orElseThrow(() -> new IllegalArgumentException("모임통장을 찾을 수 없습니다. accountNumber: " + coupleAccountNumber));
        
        // 4. 권한 검증 및 로깅
        String accountOwnerUserSeqNo = coupleAccount.getUserSeqNo();
        log.info("모임통장 조회 - 현재 사용자: {}, 커플ID: {}, 계좌번호: {}, 계좌 소유주: {}", 
                userSeqNo, coupleId, coupleAccountNumber, accountOwnerUserSeqNo);
        
        return coupleAccount;
    }
    
    /**
     * 모임통장 소유주 user_seq_no 조회
     */
    public String getCoupleAccountOwnerUserSeqNo() {
        Account coupleAccount = getCurrentUserCoupleAccount();
        return coupleAccount.getUserSeqNo(); // 소유주의 user_seq_no 반환
    }
}
