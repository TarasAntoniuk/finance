package com.tarasantoniuk.finance.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank
        @Schema(description = "Google ID token (JWT) obtained from Google Identity Services",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idToken) {
}
