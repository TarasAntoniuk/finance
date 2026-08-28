package com.tarasantoniuk.finance.security.auth.google;

import com.tarasantoniuk.finance.security.auth.exception.GoogleAuthenticationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdTokenVerifier {

    private static final String EMAIL_CLAIM = "email";
    private static final String EMAIL_VERIFIED_CLAIM = "email_verified";
    private static final String NAME_CLAIM = "name";

    private final JwtDecoder googleIdTokenDecoder;

    public GoogleIdTokenVerifier(@Qualifier("googleIdTokenDecoder") JwtDecoder googleIdTokenDecoder) {
        this.googleIdTokenDecoder = googleIdTokenDecoder;
    }

    public GoogleUserInfo verify(String idToken) {
        Jwt jwt = decode(idToken);
        return new GoogleUserInfo(
                jwt.getClaimAsString(EMAIL_CLAIM),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean(EMAIL_VERIFIED_CLAIM)),
                jwt.getClaimAsString(NAME_CLAIM),
                jwt.getSubject());
    }

    private Jwt decode(String idToken) {
        try {
            return googleIdTokenDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new GoogleAuthenticationException("Invalid Google ID token");
        }
    }
}
