package com.marry1q.marry1qbe.domain.account.exception;

/**
 * 입금이체 실패 예외
 */
public class DepositTransferException extends RuntimeException {
    
    public DepositTransferException(String message) {
        super(message);
    }
    
    public DepositTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
