package com.marry1q.marry1qbe.domain.account.exception;

/**
 * 출금이체 실패 예외
 */
public class WithdrawTransferException extends RuntimeException {
    
    public WithdrawTransferException(String message) {
        super(message);
    }
    
    public WithdrawTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
