package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class CoupleNotFoundException extends CustomException {
    public CoupleNotFoundException() {
        super(ErrorCode.COUPLE_NOT_FOUND);
    }
    
    public CoupleNotFoundException(String message) {
        super(ErrorCode.COUPLE_NOT_FOUND, message);
    }
}
