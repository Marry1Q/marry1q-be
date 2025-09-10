package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class S3UploadException extends CustomException {
    public S3UploadException() {
        super(ErrorCode.S3_UPLOAD_FAILED);
    }
    
    public S3UploadException(String message) {
        super(ErrorCode.S3_UPLOAD_FAILED, message);
    }
}
