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

    /**
     * Maps client input and authentication argument failures to a safe response.
     *
     * @param exception exception raised by application code
     * @param request current HTTP request
     * @return response containing the public status and client-facing message
     */
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

    /**
     * Handles malformed, expired, or incorrectly signed JWTs.
     *
     * @param exception JWT parsing or validation failure
     * @param request current HTTP request
     * @return unauthorized error response
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            JwtException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "The access token is invalid or expired.", request);
    }

    /**
     * Handles refresh-token lifecycle failures and unexpected runtime failures.
     *
     * @param exception runtime failure raised by application code
     * @param request current HTTP request
     * @return mapped client-safe error response
     */
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

    /**
     * Handles malformed JSON and missing required request headers.
     *
     * @param exception request parsing or binding failure
     * @param request current HTTP request
     * @return bad-request error response
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "The request is invalid or missing required information.", request);
    }

    /**
     * Provides a generic response without exposing internal implementation details.
     *
     * @param exception unexpected application failure
     * @param request current HTTP request
     * @return generic internal-server-error response
     */
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
