package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class InvitationNotFoundException extends CustomException {
    public InvitationNotFoundException() {
        super(ErrorCode.INVITATION_NOT_FOUND);
    }
    
    public InvitationNotFoundException(String message) {
        super(ErrorCode.INVITATION_NOT_FOUND, message);
    }
}
