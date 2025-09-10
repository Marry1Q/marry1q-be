package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class S3DeleteException extends CustomException {
    public S3DeleteException() {
        super(ErrorCode.S3_DELETE_FAILED);
    }
    
    public S3DeleteException(String message) {
        super(ErrorCode.S3_DELETE_FAILED, message);
    }
}
