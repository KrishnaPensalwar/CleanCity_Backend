package com.cleancity.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Canonical API error catalog.
 * Clients should match on {@link #getCode()} and display {@link #getMessage()} (or a localized copy).
 */
public enum ErrorCode {

    // ── Auth / Account (AUTH_xxx) ───────────────────────────────────────────
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_001",
            "Unauthorized",
            "Authentication is required. Please sign in and send a valid Bearer token."),
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_002",
            "Invalid Credentials",
            "The email or password you entered is incorrect. Please try again."),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "AUTH_003",
            "Access Denied",
            "You do not have permission to perform this action."),
    REFRESH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "AUTH_004",
            "Invalid Refresh Token",
            "The refresh token is invalid or has already been used. Please sign in again."),
    REFRESH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_005",
            "Refresh Token Expired",
            "Your session has expired. Please sign in again."),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTH_006",
            "Email Already Registered",
            "An account with this email already exists. Try signing in or use a different email."),
    PHONE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTH_007",
            "Phone Already Registered",
            "An account with this phone number already exists. Use a different phone number."),
    ACCOUNT_INACTIVE(
            HttpStatus.FORBIDDEN,
            "AUTH_008",
            "Account Inactive",
            "This account is inactive, suspended, or pending activation. Contact support if you need help."),
    ALREADY_DRIVER(
            HttpStatus.CONFLICT,
            "AUTH_009",
            "Already a Driver",
            "This account already has the driver role."),
    TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "AUTH_010",
            "Invalid Token",
            "The access token is invalid or malformed. Please sign in again."),

    // ── Reports / Complaints (REPORT_xxx) ───────────────────────────────────
    REPORT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REPORT_001",
            "Report Not Found",
            "No report was found for the given ID."),
    REPORT_NOT_PENDING(
            HttpStatus.CONFLICT,
            "REPORT_002",
            "Report Not Pending",
            "Only reports in PENDING status can be assigned to a driver."),
    REPORT_NOT_AWAITING_REVIEW(
            HttpStatus.CONFLICT,
            "REPORT_003",
            "Report Not Awaiting Review",
            "This report is not awaiting admin review, so it cannot be approved or rejected."),
    REPORT_ALREADY_APPROVED(
            HttpStatus.CONFLICT,
            "REPORT_004",
            "Report Already Approved",
            "This report is already approved and cannot be rejected."),
    COMPLETION_IMAGE_MISSING(
            HttpStatus.BAD_REQUEST,
            "REPORT_005",
            "Completion Image Missing",
            "A completion photo is required before this report can be approved."),
    INVALID_REPORT_ID(
            HttpStatus.BAD_REQUEST,
            "REPORT_006",
            "Invalid Report ID",
            "The report ID format is invalid. Expected a UUID."),
    COMPLAINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REPORT_007",
            "Complaint Not Found",
            "No complaint was found for the given ID."),
    INVALID_COMPLAINT_ID(
            HttpStatus.BAD_REQUEST,
            "REPORT_008",
            "Invalid Complaint ID",
            "The complaint ID format is invalid. Expected a UUID."),
    REPORT_NOT_ASSIGNED(
            HttpStatus.CONFLICT,
            "REPORT_009",
            "Report Not Assigned",
            "This report must be in ASSIGNED status before it can be completed."),

    // ── Drivers (DRIVER_xxx) ────────────────────────────────────────────────
    DRIVER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "DRIVER_001",
            "Driver Not Found",
            "No driver profile was found for this account."),
    DRIVER_INACTIVE(
            HttpStatus.FORBIDDEN,
            "DRIVER_002",
            "Driver Inactive",
            "This driver account is inactive and cannot perform driver actions."),
    DRIVER_ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "DRIVER_003",
            "Driver ID Required",
            "Admin assignment requires a driverId in the request body."),
    DRIVER_CANNOT_APPROVE(
            HttpStatus.FORBIDDEN,
            "DRIVER_004",
            "Driver Cannot Approve",
            "Drivers cannot approve reports. Upload a completion photo and wait for admin review."),
    DRIVER_NOT_APPROVED(
            HttpStatus.FORBIDDEN,
            "DRIVER_005",
            "Driver Not Approved",
            "Your driver account is pending approval or was rejected. You cannot take driver actions yet."),

    // ── Users / Profile (USER_xxx) ───────────────────────────────────────────
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_001",
            "User Not Found",
            "No user profile was found for this account."),
    PROFILE_UPDATE_EMPTY(
            HttpStatus.BAD_REQUEST,
            "USER_002",
            "Nothing to Update",
            "Provide at least one profile field to update: name, address, phone, or profileImage."),
    INVALID_PROFILE_NAME(
            HttpStatus.BAD_REQUEST,
            "USER_003",
            "Invalid Name",
            "Name must be between 2 and 100 characters."),
    INVALID_PROFILE_IMAGE(
            HttpStatus.BAD_REQUEST,
            "USER_004",
            "Invalid Profile Image",
            "Profile image must be a valid https URL."),

    // ── Validation / Input (VALID_xxx) ──────────────────────────────────────
    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "VALID_001",
            "Validation Failed",
            "One or more request fields are invalid. Check the details for each field."),
    INVALID_COORDINATES(
            HttpStatus.BAD_REQUEST,
            "VALID_002",
            "Invalid Coordinates",
            "Latitude must be between -90 and 90, and longitude between -180 and 180."),
    INVALID_LATITUDE(
            HttpStatus.BAD_REQUEST,
            "VALID_003",
            "Invalid Latitude",
            "Latitude is required and must be a number between -90 and 90."),
    INVALID_LONGITUDE(
            HttpStatus.BAD_REQUEST,
            "VALID_004",
            "Invalid Longitude",
            "Longitude is required and must be a number between -180 and 180."),
    INVALID_TIMESTAMP(
            HttpStatus.BAD_REQUEST,
            "VALID_005",
            "Invalid Timestamp",
            "Timestamp is required, must not be in the future, and must be within the last year."),
    DESCRIPTION_TOO_LONG(
            HttpStatus.BAD_REQUEST,
            "VALID_006",
            "Description Too Long",
            "Description must be at most 2000 characters."),
    IMAGE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "VALID_007",
            "Image Required",
            "An image file is required and cannot be empty."),
    INVALID_IMAGE_TYPE(
            HttpStatus.BAD_REQUEST,
            "VALID_008",
            "Invalid Image Type",
            "Only JPEG and PNG images are allowed."),
    INVALID_IMAGE_CONTENT(
            HttpStatus.BAD_REQUEST,
            "VALID_009",
            "Invalid Image Content",
            "The uploaded file is not a valid JPEG or PNG image."),

    // ── System (SYS_xxx) ────────────────────────────────────────────────────
    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SYS_001",
            "Internal Server Error",
            "Something went wrong on our side. Please try again later."),
    FILE_TOO_LARGE(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "SYS_002",
            "File Too Large",
            "The uploaded file exceeds the maximum allowed size of 5MB."),
    RATE_LIMITED(
            HttpStatus.TOO_MANY_REQUESTS,
            "SYS_003",
            "Too Many Requests",
            "You have made too many requests. Please wait a minute and try again."),
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SYS_004",
            "Not Found",
            "The requested resource was not found."),
    CONFLICT(
            HttpStatus.CONFLICT,
            "SYS_005",
            "Conflict",
            "The request could not be completed because of a conflict with the current resource state."),
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "SYS_006",
            "Bad Request",
            "The request could not be understood or is missing required data.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String title;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String title, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.title = title;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /** Machine-readable stable code (e.g. AUTH_002). */
    public String getCode() {
        return code;
    }

    /** Short human-readable title for UI headers. */
    public String getTitle() {
        return title;
    }

    /** Clear, user-facing explanation of what went wrong. */
    public String getMessage() {
        return message;
    }

    /** @deprecated Use {@link #getTitle()} */
    @Deprecated
    public String getError() {
        return title;
    }
}
