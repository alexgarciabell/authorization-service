package com.depuramente.auth.error;

import java.time.Instant;

/**
 * Stable error payload returned by the HTTP API.
 *
 * @param timestamp time at which the error was handled
 * @param status HTTP status code
 * @param error short HTTP status description
 * @param message safe, user-facing error message
 * @param path request path that produced the error
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
