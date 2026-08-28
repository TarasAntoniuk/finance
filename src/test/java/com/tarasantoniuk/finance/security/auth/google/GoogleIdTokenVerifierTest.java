package com.tarasantoniuk.finance.security.auth.google;

import com.tarasantoniuk.finance.security.auth.exception.GoogleAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenVerifierTest {

    @Mock
    private JwtDecoder googleIdTokenDecoder;

    private GoogleIdTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new GoogleIdTokenVerifier(googleIdTokenDecoder);
    }

    @Test
    void verify_WhenValidToken_ShouldMapClaims() {
        Jwt jwt = Jwt.withTokenValue("id-token")
                .header("alg", "RS256")
                .subject("google-sub-123")
                .claim("email", "user@example.com")
                .claim("email_verified", true)
                .claim("name", "User Example")
                .build();
        when(googleIdTokenDecoder.decode("id-token")).thenReturn(jwt);

        GoogleUserInfo info = verifier.verify("id-token");

        assertEquals("user@example.com", info.email());
        assertTrue(info.emailVerified());
        assertEquals("User Example", info.name());
        assertEquals("google-sub-123", info.subject());
    }

    @Test
    void verify_WhenDecoderRejectsToken_ShouldThrowGoogleAuthenticationException() {
        // A wrong audience/issuer surfaces as a JwtException from the configured decoder
        when(googleIdTokenDecoder.decode("bad-token"))
                .thenThrow(new JwtException("Invalid audience or issuer"));

        assertThrows(GoogleAuthenticationException.class, () -> verifier.verify("bad-token"));
    }

    @Test
    void verify_WhenEmailVerifiedClaimMissing_ShouldDefaultToFalse() {
        Jwt jwt = Jwt.withTokenValue("id-token")
                .header("alg", "RS256")
                .subject("google-sub-123")
                .claim("email", "user@example.com")
                .build();
        when(googleIdTokenDecoder.decode("id-token")).thenReturn(jwt);

        assertFalse(verifier.verify("id-token").emailVerified());
    }
}
