package com.marry1q.marry1qbe.domain.plan1q.exception;

import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;

/**
 * Plan1Q 목표를 찾을 수 없을 때 발생하는 예외
 */
public class Plan1QGoalNotFoundException extends CustomException {
    
    public Plan1QGoalNotFoundException(String message) {
        super(ErrorCode.PLAN1Q_GOAL_NOT_FOUND, message);
    }
    
    public Plan1QGoalNotFoundException(String message, Throwable cause) {
        super(ErrorCode.PLAN1Q_GOAL_NOT_FOUND, message);
    }
}
