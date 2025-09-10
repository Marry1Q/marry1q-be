package com.marry1q.marry1qbe.domain.finance.exception;

public class CategoryBudgetNotSetException extends RuntimeException {
    
    public CategoryBudgetNotSetException(String message) {
        super(message);
    }
    
    public CategoryBudgetNotSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
