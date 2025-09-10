package com.marry1q.marry1qbe.domain.finance.exception;

public class CategoryBudgetNotFoundException extends RuntimeException {
    
    public CategoryBudgetNotFoundException(String message) {
        super(message);
    }
    
    public CategoryBudgetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
