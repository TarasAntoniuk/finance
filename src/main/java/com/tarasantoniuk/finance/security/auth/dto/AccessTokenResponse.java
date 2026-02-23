package com.tarasantoniuk.finance.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing only the access token; refresh token is set as HttpOnly cookie")
public class AccessTokenResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    public AccessTokenResponse() {
    }

    public AccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
