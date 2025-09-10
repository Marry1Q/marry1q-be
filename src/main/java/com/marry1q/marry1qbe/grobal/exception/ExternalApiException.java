package com.marry1q.marry1qbe.grobal.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {
    
    private final String externalApiName;
    private final String externalErrorMessage;
    private final int httpStatusCode;
    
    public ExternalApiException(String externalApiName, String message, String externalErrorMessage, int httpStatusCode) {
        super(message);
        this.externalApiName = externalApiName;
        this.externalErrorMessage = externalErrorMessage;
        this.httpStatusCode = httpStatusCode;
    }
    
    public ExternalApiException(String externalApiName, String message, String externalErrorMessage) {
        this(externalApiName, message, externalErrorMessage, 500);
    }
    
    public ExternalApiException(String externalApiName, String message) {
        this(externalApiName, message, null, 500);
    }
}
