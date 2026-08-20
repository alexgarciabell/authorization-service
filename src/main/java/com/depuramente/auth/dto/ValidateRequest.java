package com.depuramente.auth.dto;

/**
 * Request containing an access token to validate.
 * @param accessToken access JWT
 */
public record ValidateRequest(
        String accessToken
) {
}
