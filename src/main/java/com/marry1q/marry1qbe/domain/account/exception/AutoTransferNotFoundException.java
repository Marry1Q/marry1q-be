package com.marry1q.marry1qbe.domain.account.exception;

/**
 * 자동이체를 찾을 수 없을 때 발생하는 예외
 */
public class AutoTransferNotFoundException extends RuntimeException {
    
    public AutoTransferNotFoundException(String message) {
        super(message);
    }
    
    public AutoTransferNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
