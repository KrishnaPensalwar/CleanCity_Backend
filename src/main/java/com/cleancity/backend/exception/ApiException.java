package com.cleancity.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception that maps 1:1 to a catalog {@link ErrorCode}.
 * Prefer the single-arg constructor so clients always get the standard message.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Override the default catalog message with a more specific one
     * (still paired with the same errorCode/title/status).
     */
    public ApiException(ErrorCode errorCode, String message) {
        super(message != null && !message.isBlank() ? message : errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
