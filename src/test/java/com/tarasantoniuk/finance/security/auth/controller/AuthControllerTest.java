package com.tarasantoniuk.finance.security.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.DefaultOrganizationFixture;
import com.tarasantoniuk.finance.security.auth.dto.AccessTokenResponse;
import com.tarasantoniuk.finance.security.auth.dto.LoginRequest;
import com.tarasantoniuk.finance.security.auth.dto.RegisterRequest;
import com.tarasantoniuk.finance.security.auth.exception.GoogleAuthenticationException;
import com.tarasantoniuk.finance.security.auth.google.GoogleIdTokenVerifier;
import com.tarasantoniuk.finance.security.auth.google.GoogleUserInfo;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerTest extends BaseIntegrationTest {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final long DEFAULT_ORGANIZATION_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @BeforeEach
    void setUp() {
        // Clean data from other non-@Transactional test classes
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        DefaultOrganizationFixture.ensureExists(jdbcTemplate, DEFAULT_ORGANIZATION_ID);
    }

    // ========== REGISTER ==========

    @Test
    void register_WhenValidRequest_ShouldReturn201WithAccessTokenAndRefreshCookie() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("SecureP@ss1");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        assertRefreshTokenCookiePresent(result);
    }

    @Test
    void register_WhenDuplicateEmail_ShouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("SecureP@ss1");

        // Register first time
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register again with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email 'duplicate@example.com' already exists"));
    }

    @Test
    void register_WhenInvalidEmail_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("not-an-email");
        request.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenShortPassword_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenWeakPassword_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("alllowercase1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== LOGIN ==========

    @Test
    void login_WhenValidCredentials_ShouldReturn200WithAccessTokenAndRefreshCookie() throws Exception {
        registerUser("login@example.com", "SecureP@ss1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("SecureP@ss1");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        assertRefreshTokenCookiePresent(result);
    }

    @Test
    void login_WhenWrongPassword_ShouldReturn401() throws Exception {
        registerUser("wrongpass@example.com", "SecureP@ss1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_WhenNonExistentEmail_ShouldReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ========== LOGIN - ACCOUNT LOCKOUT ==========

    @Test
    void login_WhenAccountLocked_ShouldReturn403() throws Exception {
        registerUser("locked@example.com", "SecureP@ss1");

        // Lock the account
        User user = userRepository.findByEmail("locked@example.com").orElseThrow();
        user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(30));
        user.setFailedLoginAttempts(5);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("locked@example.com");
        loginRequest.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Account is locked")));
    }

    @Test
    void login_WhenMaxFailedAttempts_ShouldLockAccount() throws Exception {
        registerUser("lockme@example.com", "SecureP@ss1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("lockme@example.com");
        loginRequest.setPassword("wrongpassword");

        // 4 failed attempts - should still get 401
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // 5th failed attempt - account gets locked, should get 403
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Account locked for")));
    }

    // ========== GOOGLE LOGIN ==========

    @Test
    void google_WhenValidTokenForExistingUser_ShouldReturn200WithAccessTokenAndRefreshCookie() throws Exception {
        registerUser("google-existing@example.com", "SecureP@ss1");
        when(googleIdTokenVerifier.verify(anyString()))
                .thenReturn(new GoogleUserInfo("google-existing@example.com", true, "Existing", "google-sub"));

        MvcResult result = mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("idToken", "valid-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        assertRefreshTokenCookiePresent(result);
    }

    @Test
    void google_WhenInvalidToken_ShouldReturn401() throws Exception {
        when(googleIdTokenVerifier.verify(anyString()))
                .thenThrow(new GoogleAuthenticationException("Invalid Google ID token"));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("idToken", "bad-id-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Google ID token"));
    }

    @Test
    void google_WhenEmailNotVerified_ShouldReturn401() throws Exception {
        when(googleIdTokenVerifier.verify(anyString()))
                .thenReturn(new GoogleUserInfo("unverified@example.com", false, "Unverified", "google-sub"));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("idToken", "valid-id-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Google account email is not verified"));
    }

    @Test
    void google_WhenMissingIdToken_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== CHANGE PASSWORD ==========

    @Test
    void changePassword_WhenValidRequest_ShouldReturn204() throws Exception {
        TokenPair tokens = registerUser("changepass@example.com", "SecureP@ss1");

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "currentPassword", "SecureP@ss1",
                "newPassword", "NewSecureP@ss2"
        ));

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + tokens.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        // Login with new password should succeed
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("changepass@example.com");
        loginRequest.setPassword("NewSecureP@ss2");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_WhenWrongCurrentPassword_ShouldReturn401() throws Exception {
        TokenPair tokens = registerUser("changepass2@example.com", "SecureP@ss1");

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "currentPassword", "WrongP@ss1",
                "newPassword", "NewSecureP@ss2"
        ));

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + tokens.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "currentPassword", "SecureP@ss1",
                "newPassword", "NewSecureP@ss2"
        ));

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ========== REFRESH ==========

    @Test
    void refresh_WhenValidCookie_ShouldReturn200WithNewTokens() throws Exception {
        TokenPair tokens = registerUser("refresh@example.com", "SecureP@ss1");

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        assertRefreshTokenCookiePresent(result);
    }

    @Test
    void refresh_WhenInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WhenTokenUsedTwice_ShouldReturn401OnSecondUse() throws Exception {
        TokenPair tokens = registerUser("reuse@example.com", "SecureP@ss1");

        // First refresh succeeds
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken)))
                .andExpect(status().isOk());

        // Second refresh with same token fails (already revoked)
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    // ========== LOGOUT ==========

    @Test
    void logout_WhenAuthenticated_ShouldReturn204AndClearCookie() throws Exception {
        TokenPair tokens = registerUser("logout@example.com", "SecureP@ss1");

        MvcResult result = mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isNoContent())
                .andReturn();

        // Verify refresh token cookie is cleared
        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains(REFRESH_TOKEN_COOKIE));
        assertTrue(setCookieHeader.contains("Max-Age=0"));
    }

    @Test
    void logout_WhenNoToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // ========== LOGIN - DISABLED ACCOUNT ==========

    @Test
    void login_WhenAccountDisabled_ShouldReturn403() throws Exception {
        // Register a user then disable them
        registerUser("disabled@example.com", "SecureP@ss1");
        User user = userRepository.findByEmail("disabled@example.com").orElseThrow();
        user.setIsActive(false);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("disabled@example.com");
        loginRequest.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is disabled"));
    }

    // ========== REGISTER - VALIDATION ==========

    @Test
    void register_WhenEmptyEmail_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenEmptyPassword_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("valid@example.com");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenNullBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== LOGIN - VALIDATION ==========

    @Test
    void login_WhenEmptyEmail_ShouldReturn400() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WhenEmptyPassword_ShouldReturn400() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== REFRESH - EDGE CASES ==========

    @Test
    void refresh_WhenMissingCookie_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WhenBlankCookie_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "")))
                .andExpect(status().isUnauthorized());
    }

    // ========== ROLE-BASED ACCESS CONTROL ==========

    @Test
    void getEndpoint_WhenUnauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEndpoint_WhenGuestRole_ShouldNotReturn403() throws Exception {
        TokenPair tokens = registerUser("guest-get@example.com", "SecureP@ss1");
        // New registrations default to GUEST role - GUEST can read but may get 404/200 depending on data

        mockMvc.perform(get("/api/v1/currencies")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void postEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        TokenPair tokens = registerUser("guest-post@example.com", "SecureP@ss1");
        // GUEST role should not be able to POST to /api/v1/**

        mockMvc.perform(post("/api/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        TokenPair tokens = registerUser("guest-delete@example.com", "SecureP@ss1");

        mockMvc.perform(delete("/api/v1/currencies/1")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_WhenUserRole_ShouldReturn403() throws Exception {
        // Create a user with USER role
        TokenPair tokens = registerUserWithRole("user-delete@example.com", "SecureP@ss1", UserRole.USER);

        mockMvc.perform(delete("/api/v1/currencies/1")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void postEndpoint_WhenUserRole_ShouldNotReturn403() throws Exception {
        TokenPair tokens = registerUserWithRole("user-post@example.com", "SecureP@ss1", UserRole.USER);

        // USER should be allowed to POST - we don't check for 200 since body may be invalid,
        // but it should not be 403
        mockMvc.perform(post("/api/v1/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"swiftCode\":\"TESTCODE\",\"countryId\":1}")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void deleteEndpoint_WhenAdminRole_ShouldNotReturn403() throws Exception {
        TokenPair tokens = registerUserWithRole("admin-delete@example.com", "SecureP@ss1", UserRole.ADMIN);

        // ADMIN should be allowed to DELETE - we don't check for 200 since resource may not exist,
        // but it should not be 403
        mockMvc.perform(delete("/api/v1/currencies/99999")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void putEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        TokenPair tokens = registerUser("guest-put@example.com", "SecureP@ss1");

        mockMvc.perform(put("/api/v1/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_WhenAccessTokenUsedAfterLogout_ShouldReturn401() throws Exception {
        TokenPair tokens = registerUser("blacklist@example.com", "SecureP@ss1");

        // Logout - blacklists the access token
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isNoContent());

        // Try to use blacklisted access token - should fail with 401 (no authentication)
        mockMvc.perform(get("/api/v1/currencies")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_WhenTokenAfterLogout_RefreshShouldFail() throws Exception {
        TokenPair tokens = registerUser("logout-refresh@example.com", "SecureP@ss1");

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken))
                .andExpect(status().isNoContent());

        // Try to refresh with the old refresh token - should fail because all tokens are revoked
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    // ========== HELPER METHODS ==========

    private record TokenPair(String accessToken, String refreshToken) {}

    private TokenPair registerUserWithRole(String email, String password, UserRole role) throws Exception {
        registerUser(email, password);

        // Update role directly in DB
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(role);
        userRepository.save(user);

        // Re-login to get tokens with updated role
        return loginUser(email, password);
    }

    private TokenPair registerUser(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractTokenPair(result);
    }

    private TokenPair loginUser(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return extractTokenPair(result);
    }

    private TokenPair extractTokenPair(MvcResult result) throws Exception {
        AccessTokenResponse tokenResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccessTokenResponse.class);
        String refreshToken = extractRefreshTokenFromCookie(result);
        return new TokenPair(tokenResponse.getAccessToken(), refreshToken);
    }

    private String extractRefreshTokenFromCookie(MvcResult result) {
        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookieHeader, "Set-Cookie header should be present");
        assertTrue(setCookieHeader.contains(REFRESH_TOKEN_COOKIE + "="),
                "Cookie should contain refresh_token");

        // Extract token value from "refresh_token=<value>; ..."
        String cookiePart = setCookieHeader.split(";")[0];
        return cookiePart.substring(cookiePart.indexOf("=") + 1);
    }

    private void assertRefreshTokenCookiePresent(MvcResult result) {
        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookieHeader, "Set-Cookie header should be present");
        assertTrue(setCookieHeader.contains(REFRESH_TOKEN_COOKIE + "="));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        assertTrue(setCookieHeader.contains("Secure"));
        assertTrue(setCookieHeader.contains("SameSite=Strict"));
        assertTrue(setCookieHeader.contains("Path=/api/auth/refresh"));
    }
}
