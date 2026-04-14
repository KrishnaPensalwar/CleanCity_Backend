package com.cleancity.backend.dto;

public class MessageResponse {
    private String message;
    private boolean isSuccess = false;

    public MessageResponse(String message) {
        this.message = message;
    }

    public MessageResponse(String message, boolean isSuccess) {
        this.message = message;
        this.isSuccess = isSuccess;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean getIsSuccess() { return isSuccess; }
    public void setIsSuccess(boolean isSuccess) { this.isSuccess = isSuccess; }
}
