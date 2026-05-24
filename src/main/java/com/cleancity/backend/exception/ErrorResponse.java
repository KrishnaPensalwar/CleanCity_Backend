package com.cleancity.backend.exception;

public class ErrorResponse {

    private String errorCode;
    private String message;
    private boolean isSuccess = false;

    public ErrorResponse() {
    }

    public ErrorResponse(ErrorCode errorCode) {
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.errorCode = errorCode.getCode();
        this.message = message;
    }

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
