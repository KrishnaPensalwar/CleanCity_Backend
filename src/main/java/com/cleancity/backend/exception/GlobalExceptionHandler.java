package com.cleancity.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        log.warn("API error [{}] {}: {}", code.getCode(), code.getTitle(), ex.getMessage());
        return build(code, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Invalid credentials on {}", request.getRequestURI());
        return build(ErrorCode.INVALID_CREDENTIALS, request.getRequestURI());
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponse> handleDisabledAccount(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Inactive/locked account on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.ACCOUNT_INACTIVE, request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.UNAUTHORIZED, request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(ErrorCode.FILE_TOO_LARGE, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        String message = details.isEmpty()
                ? ErrorCode.VALIDATION_ERROR.getMessage()
                : details.get(0).getMessage();
        log.warn("Validation error on {}: {}", request.getRequestURI(), message);
        return build(ErrorCode.VALIDATION_ERROR, message, request.getRequestURI(), details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        String message = details.isEmpty()
                ? ErrorCode.VALIDATION_ERROR.getMessage()
                : details.get(0).getMessage();
        return build(ErrorCode.VALIDATION_ERROR, message, request.getRequestURI(), details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(
                ErrorCode.BAD_REQUEST,
                "Request body is missing or contains invalid JSON.",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Required query parameter '" + ex.getParameterName() + "' is missing.";
        return build(
                ErrorCode.VALIDATION_ERROR,
                message,
                request.getRequestURI(),
                List.of(new FieldErrorDetail(ex.getParameterName(), message)));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        String message = "Required multipart part '" + ex.getRequestPartName() + "' is missing.";
        return build(
                ErrorCode.IMAGE_REQUIRED,
                message,
                request.getRequestURI(),
                List.of(new FieldErrorDetail(ex.getRequestPartName(), message)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' has an invalid value.";
        return build(
                ErrorCode.VALIDATION_ERROR,
                message,
                request.getRequestURI(),
                List.of(new FieldErrorDetail(ex.getName(), message, ex.getValue())));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(
                ErrorCode.BAD_REQUEST,
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return build(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
        }
        return build(
                ErrorCode.BAD_REQUEST,
                ex.getReason() != null ? ex.getReason() : ErrorCode.BAD_REQUEST.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(
                ErrorCode.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : ErrorCode.BAD_REQUEST.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex, HttpServletRequest request) {
        log.warn("Forbidden on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.ACCESS_DENIED, request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(
                ErrorCode.CONFLICT,
                ex.getMessage() != null ? ex.getMessage() : ErrorCode.CONFLICT.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Internal error on {}", request.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, request.getRequestURI());
    }

    private FieldErrorDetail toDetail(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "Invalid value";
        return new FieldErrorDetail(fieldError.getField(), message, fieldError.getRejectedValue());
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String path) {
        return build(code, code.getMessage(), path, null);
    }

    private ResponseEntity<ErrorResponse> build(
            ErrorCode code,
            String message,
            String path,
            List<FieldErrorDetail> details) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ErrorResponse.of(code, message, path, details));
    }
}
