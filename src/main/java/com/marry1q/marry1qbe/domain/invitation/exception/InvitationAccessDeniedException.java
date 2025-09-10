package com.marry1q.marry1qbe.domain.invitation.exception;

import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.exception.CustomException;

public class InvitationAccessDeniedException extends CustomException {
    public InvitationAccessDeniedException() {
        super(ErrorCode.INVITATION_ACCESS_DENIED);
    }
    
    public InvitationAccessDeniedException(String message) {
        super(ErrorCode.INVITATION_ACCESS_DENIED, message);
    }
}
