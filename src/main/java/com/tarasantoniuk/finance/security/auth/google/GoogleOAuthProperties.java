package com.tarasantoniuk.finance.security.auth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.oauth")
public record GoogleOAuthProperties(String clientId, String issuer, String jwkSetUri) {
}
