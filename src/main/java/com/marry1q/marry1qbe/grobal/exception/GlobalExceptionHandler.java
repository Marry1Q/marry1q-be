package com.marry1q.marry1qbe.grobal.exception;

import com.marry1q.marry1qbe.domain.giftMoney.exception.GiftMoneyNotFoundException;
import com.marry1q.marry1qbe.domain.giftMoney.exception.GiftMoneyStatsNotFoundException;
import com.marry1q.marry1qbe.domain.invitation.exception.*;
import com.marry1q.marry1qbe.domain.couple.exception.NoCoupleException;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("ValidationException: {}", errors);
        return ResponseEntity.badRequest()
                .body(CustomApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), "입력값 검증에 실패했습니다."));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CustomApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CustomApiResponse.error(ErrorCode.INVALID_PASSWORD.getCode(), ErrorCode.INVALID_PASSWORD.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CustomApiResponse.error(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage()));
    }
    
    // 축의금 관련 예외 처리
    @ExceptionHandler(GiftMoneyNotFoundException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleGiftMoneyNotFoundException(GiftMoneyNotFoundException e) {
        log.error("GiftMoneyNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error(ErrorCode.GIFT_MONEY_NOT_FOUND.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(GiftMoneyStatsNotFoundException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleGiftMoneyStatsNotFoundException(GiftMoneyStatsNotFoundException e) {
        log.error("GiftMoneyStatsNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error(ErrorCode.GIFT_MONEY_STATS_NOT_FOUND.getCode(), e.getMessage()));
    }
    
    // 청첩장 관련 예외 처리
    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleInvitationNotFoundException(InvitationNotFoundException e) {
        log.error("InvitationNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(InvitationAccessDeniedException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleInvitationAccessDeniedException(InvitationAccessDeniedException e) {
        log.error("InvitationAccessDeniedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(NoCoupleException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleNoCoupleException(NoCoupleException e) {
        log.error("NoCoupleException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleS3UploadException(S3UploadException e) {
        log.error("S3UploadException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(S3DeleteException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleS3DeleteException(S3DeleteException e) {
        log.error("S3DeleteException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(S3UrlInvalidException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleS3UrlInvalidException(S3UrlInvalidException e) {
        log.error("S3UrlInvalidException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleDatabaseException(DatabaseException e) {
        log.error("DatabaseException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
