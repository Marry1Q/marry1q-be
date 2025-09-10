package com.marry1q.marry1qbe.domain.finance.exception;

public class FinanceTransactionNotFoundException extends RuntimeException {
    
    public FinanceTransactionNotFoundException(String message) {
        super(message);
    }
    
    public FinanceTransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
