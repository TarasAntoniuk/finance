package com.tarasantoniuk.finance.security.jwt.service;

import com.tarasantoniuk.finance.security.jwt.JwtProperties;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String VALID_SECRET = "test-secret-key-for-jwt-must-be-at-least-32-bytes-long";

    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(VALID_SECRET);
        jwtProperties.setAccessTokenExpiration(900000L);
        jwtProperties.setRefreshTokenExpiration(604800000L);

        jwtService = new JwtService(jwtProperties);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
    }

    // ========== CONSTRUCTOR ==========

    @Test
    void constructor_WhenNullSecret_ShouldThrowIllegalStateException() {
        JwtProperties props = new JwtProperties();
        props.setSecret(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new JwtService(props));

        assertTrue(exception.getMessage().contains("JWT secret must be at least 32 characters"));
    }

    @Test
    void constructor_WhenShortSecret_ShouldThrowIllegalStateException() {
        JwtProperties props = new JwtProperties();
        props.setSecret("too-short");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new JwtService(props));

        assertTrue(exception.getMessage().contains("JWT secret must be at least 32 characters"));
    }

    @Test
    void constructor_WhenValidSecret_ShouldCreateService() {
        assertNotNull(jwtService);
    }

    // ========== GENERATE ACCESS TOKEN ==========

    @Test
    void generateAccessToken_ShouldReturnValidJwt() {
        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void generateAccessToken_ShouldContainUserClaims() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);

        assertEquals("1", claims.getSubject());
        assertEquals("test@example.com", claims.get("email", String.class));
        assertEquals("USER", claims.get("role", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    // ========== GENERATE REFRESH TOKEN ==========

    @Test
    void generateRefreshToken_ShouldReturnValidJwt() {
        String token = jwtService.generateRefreshToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void generateRefreshToken_ShouldContainSubjectOnly() {
        String token = jwtService.generateRefreshToken(user);
        Claims claims = jwtService.extractClaims(token);

        assertEquals("1", claims.getSubject());
        assertNull(claims.get("email", String.class));
        assertNull(claims.get("role", String.class));
    }

    // ========== EXTRACT CLAIMS ==========

    @Test
    void extractClaims_WhenExpiredToken_ShouldThrowException() {
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();

        assertThrows(Exception.class, () -> jwtService.extractClaims(expiredToken));
    }

    @Test
    void extractClaims_WhenInvalidSignature_ShouldThrowException() {
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-secret-key-must-be-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(differentKey)
                .compact();

        assertThrows(Exception.class, () -> jwtService.extractClaims(tokenWithWrongSignature));
    }

    @Test
    void extractClaims_WhenMalformedToken_ShouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.extractClaims("not.a.valid.jwt"));
    }

    // ========== EXTRACT USER ID ==========

    @Test
    void extractUserId_ShouldReturnUserId() {
        String token = jwtService.generateAccessToken(user);

        Long userId = jwtService.extractUserId(token);

        assertEquals(1L, userId);
    }

    // ========== IS TOKEN VALID ==========

    @Test
    void isTokenValid_WhenValidToken_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WhenExpiredToken_ShouldReturnFalse() {
        JwtProperties shortLivedProps = new JwtProperties();
        shortLivedProps.setSecret(VALID_SECRET);
        shortLivedProps.setAccessTokenExpiration(0L);
        shortLivedProps.setRefreshTokenExpiration(0L);

        JwtService shortLivedService = new JwtService(shortLivedProps);

        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();

        assertFalse(shortLivedService.isTokenValid(expiredToken));
    }

    @Test
    void isTokenValid_WhenMalformedToken_ShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid("malformed-token"));
    }

    @Test
    void isTokenValid_WhenInvalidSignature_ShouldReturnFalse() {
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-secret-key-must-be-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(differentKey)
                .compact();

        assertFalse(jwtService.isTokenValid(tokenWithWrongSignature));
    }

    @Test
    void isTokenValid_WhenNullToken_ShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void isTokenValid_WhenEmptyToken_ShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid(""));
    }

    // ========== HASH TOKEN ==========

    @Test
    void hashToken_ShouldReturnConsistentHash() {
        String token = "some-token-value";

        String hash1 = jwtService.hashToken(token);
        String hash2 = jwtService.hashToken(token);

        assertEquals(hash1, hash2);
    }

    @Test
    void hashToken_ShouldReturnDifferentHashesForDifferentTokens() {
        String hash1 = jwtService.hashToken("token-1");
        String hash2 = jwtService.hashToken("token-2");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void hashToken_ShouldReturnBase64EncodedString() {
        String hash = jwtService.hashToken("test-token");

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(hash));
    }

    // ========== GET REFRESH TOKEN EXPIRATION ==========

    @Test
    void getRefreshTokenExpiration_ShouldReturnConfiguredValue() {
        assertEquals(604800000L, jwtService.getRefreshTokenExpiration());
    }
}
