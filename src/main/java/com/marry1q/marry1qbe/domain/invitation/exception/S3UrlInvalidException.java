package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class S3UrlInvalidException extends CustomException {
    public S3UrlInvalidException() {
        super(ErrorCode.S3_URL_INVALID);
    }
    
    public S3UrlInvalidException(String message) {
        super(ErrorCode.S3_URL_INVALID, message);
    }
}
