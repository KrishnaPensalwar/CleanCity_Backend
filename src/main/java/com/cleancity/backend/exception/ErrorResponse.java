package com.cleancity.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error body returned for every failure response.
 *
 * <pre>
 * {
 *   "isSuccess": false,
 *   "status": 401,
 *   "errorCode": "AUTH_002",
 *   "error": "Invalid Credentials",
 *   "message": "The email or password you entered is incorrect. Please try again.",
 *   "timestamp": "2026-08-11T12:10:00.123Z",
 *   "path": "/auth/login",
 *   "details": [ { "field": "email", "message": "..." } ]
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean isSuccess = false;
    private int status;
    private String errorCode;
    private String error;
    private String message;
    private String timestamp;
    private String path;
    private List<FieldErrorDetail> details;

    public ErrorResponse() {
        this.timestamp = Instant.now().toString();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage(), null, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return of(errorCode, message, null, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return of(errorCode, message, path, null);
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            List<FieldErrorDetail> details) {
        ErrorResponse response = new ErrorResponse();
        response.isSuccess = false;
        response.status = errorCode.getHttpStatus().value();
        response.errorCode = errorCode.getCode();
        response.error = errorCode.getTitle();
        response.message = (message == null || message.isBlank()) ? errorCode.getMessage() : message;
        response.path = path;
        response.details = (details == null || details.isEmpty()) ? null : details;
        return response;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean success) {
        isSuccess = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /** Short error title (same as ErrorCode title). */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** Clear explanation of what went wrong. */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(List<FieldErrorDetail> details) {
        this.details = details;
    }
}
