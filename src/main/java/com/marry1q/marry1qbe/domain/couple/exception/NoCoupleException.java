package com.marry1q.marry1qbe.domain.couple.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class NoCoupleException extends CustomException {
    public NoCoupleException() {
        super(ErrorCode.NO_COUPLE);
    }
    
    public NoCoupleException(String message) {
        super(ErrorCode.NO_COUPLE, message);
    }
}
