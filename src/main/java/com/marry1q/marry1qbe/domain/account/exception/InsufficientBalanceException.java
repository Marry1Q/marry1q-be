package com.marry1q.marry1qbe.domain.account.exception;

/**
 * 모임통장 잔액 부족 예외
 */
public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
