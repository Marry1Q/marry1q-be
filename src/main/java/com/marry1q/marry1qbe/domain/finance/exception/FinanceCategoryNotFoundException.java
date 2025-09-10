package com.marry1q.marry1qbe.domain.finance.exception;

public class FinanceCategoryNotFoundException extends RuntimeException {
    
    public FinanceCategoryNotFoundException(String message) {
        super(message);
    }
    
    public FinanceCategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
