package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class DatabaseException extends CustomException {
    public DatabaseException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public DatabaseException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
