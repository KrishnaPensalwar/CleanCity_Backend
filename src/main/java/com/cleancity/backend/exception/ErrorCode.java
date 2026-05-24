package com.cleancity.backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Authentication required. Please provide a valid token."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "Invalid email or password."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "You don't have permission to perform this action."),
    REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_004", "Refresh token is not valid."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_005", "Refresh token has expired. Please sign in again."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_006", "Email is already in use."),
    DRIVER_EMAIL_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_007", "Driver email already exists."),
    NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "AUTH_008", "Not authorized to access this resource."),

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "Report not found."),
    REPORT_NOT_PENDING(HttpStatus.CONFLICT, "REPORT_002", "Only pending reports can be assigned to a driver."),
    REPORT_NOT_AWAITING_REVIEW(HttpStatus.CONFLICT, "REPORT_003", "Report is not awaiting review."),
    REPORT_ALREADY_APPROVED(HttpStatus.CONFLICT, "REPORT_004", "Approved report cannot be rejected."),
    COMPLETION_IMAGE_MISSING(HttpStatus.BAD_REQUEST, "REPORT_005", "Completion image is missing. Cannot approve."),
    INVALID_REPORT_ID(HttpStatus.BAD_REQUEST, "REPORT_006", "Invalid report ID."),
    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_007", "Complaint not found."),
    INVALID_COMPLAINT_ID(HttpStatus.BAD_REQUEST, "REPORT_008", "Invalid complaint ID."),

    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVER_001", "Driver not found."),
    DRIVER_INACTIVE(HttpStatus.BAD_REQUEST, "DRIVER_002", "Driver is not active."),
    DRIVER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "DRIVER_003", "Driver ID is required for admin assignment."),
    REPORT_NOT_ASSIGNED(HttpStatus.CONFLICT, "DRIVER_004", "Report is not in assigned state."),
    DRIVER_CANNOT_APPROVE(HttpStatus.FORBIDDEN, "DRIVER_005", "Drivers cannot approve reports. Request admin approval."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found."),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALID_001", "Validation failed."),
    CONFLICT(HttpStatus.CONFLICT, "VALID_002", "Operation cannot be completed due to current state."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "An internal server error occurred."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "SYS_002", "File exceeded the maximum allowed size of 5MB.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
