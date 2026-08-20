package com.depuramente.auth.error;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Converts application and request errors into a consistent API response.
 * Internal exception details are deliberately not exposed to clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        String message = exception.getMessage();
        HttpStatus status;
        if ("User not found".equals(message)) {
            status = HttpStatus.NOT_FOUND;
        } else if ("Invalid credentials".equals(message)
                || "Invalid refresh token".equals(message)
                || "Invalid access token".equals(message)) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return error(status, message, request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            JwtException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "The access token is invalid or expired.", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException exception, HttpServletRequest request) {
        String message = null;
        if ("Refresh token revoked".equals(exception.getMessage())) {
            message = "The refresh token has been revoked.";
        } else if ("Refresh token expired".equals(exception.getMessage())) {
            message = "The refresh token has expired.";
        }
        if (message != null) {
            return error(HttpStatus.UNAUTHORIZED, message, request);
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "The request is invalid or missing required information.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(),
                message == null || message.isBlank()
                        ? "The request could not be processed."
                        : message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }
}
