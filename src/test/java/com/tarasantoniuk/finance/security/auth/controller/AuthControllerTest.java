package com.tarasantoniuk.finance.security.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.security.auth.dto.AuthResponse;
import com.tarasantoniuk.finance.security.auth.dto.LoginRequest;
import com.tarasantoniuk.finance.security.auth.dto.RegisterRequest;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== REGISTER ==========

    @Test
    void register_WhenValidRequest_ShouldReturn201WithTokens() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void register_WhenDuplicateEmail_ShouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");

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
        request.setPassword("password123");

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

    // ========== LOGIN ==========

    @Test
    void login_WhenValidCredentials_ShouldReturn200WithTokens() throws Exception {
        registerUser("login@example.com", "password123");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_WhenWrongPassword_ShouldReturn401() throws Exception {
        registerUser("wrongpass@example.com", "password123");

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
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ========== REFRESH ==========

    @Test
    void refresh_WhenValidToken_ShouldReturn200WithNewTokens() throws Exception {
        AuthResponse tokens = registerUser("refresh@example.com", "password123");

        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Token", tokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refresh_WhenInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Token", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WhenTokenUsedTwice_ShouldReturn401OnSecondUse() throws Exception {
        AuthResponse tokens = registerUser("reuse@example.com", "password123");

        // First refresh succeeds
        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Token", tokens.getRefreshToken()))
                .andExpect(status().isOk());

        // Second refresh with same token fails (already revoked)
        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Token", tokens.getRefreshToken()))
                .andExpect(status().isUnauthorized());
    }

    // ========== LOGOUT ==========

    @Test
    void logout_WhenAuthenticated_ShouldReturn204() throws Exception {
        AuthResponse tokens = registerUser("logout@example.com", "password123");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_WhenNoToken_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    // ========== LOGIN - DISABLED ACCOUNT ==========

    @Test
    void login_WhenAccountDisabled_ShouldReturn403() throws Exception {
        // Register a user then disable them
        registerUser("disabled@example.com", "password123");
        User user = userRepository.findByEmail("disabled@example.com").orElseThrow();
        user.setIsActive(false);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("disabled@example.com");
        loginRequest.setPassword("password123");

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
        request.setPassword("password123");

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
        loginRequest.setPassword("password123");

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
    void refresh_WhenMissingHeader_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getStatus() >= 400,
                        "Expected error status but got " + result.getResponse().getStatus()));
    }

    // ========== ROLE-BASED ACCESS CONTROL ==========

    @Test
    void getEndpoint_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEndpoint_WhenGuestRole_ShouldNotReturn403() throws Exception {
        AuthResponse tokens = registerUser("guest-get@example.com", "password123");
        // New registrations default to GUEST role - GUEST can read but may get 404/200 depending on data

        mockMvc.perform(get("/api/v1/currencies")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void postEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        AuthResponse tokens = registerUser("guest-post@example.com", "password123");
        // GUEST role should not be able to POST to /api/v1/**

        mockMvc.perform(post("/api/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        AuthResponse tokens = registerUser("guest-delete@example.com", "password123");

        mockMvc.perform(delete("/api/v1/currencies/1")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_WhenUserRole_ShouldReturn403() throws Exception {
        // Create a user with USER role
        AuthResponse tokens = registerUserWithRole("user-delete@example.com", "password123", UserRole.USER);

        mockMvc.perform(delete("/api/v1/currencies/1")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void postEndpoint_WhenUserRole_ShouldNotReturn403() throws Exception {
        AuthResponse tokens = registerUserWithRole("user-post@example.com", "password123", UserRole.USER);

        // USER should be allowed to POST - we don't check for 200 since body may be invalid,
        // but it should not be 403
        mockMvc.perform(post("/api/v1/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"swiftCode\":\"TESTCODE\",\"countryId\":1}")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void deleteEndpoint_WhenAdminRole_ShouldNotReturn403() throws Exception {
        AuthResponse tokens = registerUserWithRole("admin-delete@example.com", "password123", UserRole.ADMIN);

        // ADMIN should be allowed to DELETE - we don't check for 200 since resource may not exist,
        // but it should not be 403
        mockMvc.perform(delete("/api/v1/currencies/99999")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void putEndpoint_WhenGuestRole_ShouldReturn403() throws Exception {
        AuthResponse tokens = registerUser("guest-put@example.com", "password123");

        mockMvc.perform(put("/api/v1/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_WhenAccessTokenUsedAfterLogout_ShouldReturn403() throws Exception {
        AuthResponse tokens = registerUser("blacklist@example.com", "password123");

        // Logout - blacklists the access token
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isNoContent());

        // Try to use blacklisted access token - should fail
        mockMvc.perform(get("/api/v1/currencies")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_WhenTokenAfterLogout_RefreshShouldFail() throws Exception {
        AuthResponse tokens = registerUser("logout-refresh@example.com", "password123");

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isNoContent());

        // Try to refresh with the old refresh token - should fail because all tokens are revoked
        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Token", tokens.getRefreshToken()))
                .andExpect(status().isUnauthorized());
    }

    // ========== HELPER METHODS ==========

    private AuthResponse registerUserWithRole(String email, String password, UserRole role) throws Exception {
        AuthResponse tokens = registerUser(email, password);

        // Update role directly in DB
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(role);
        userRepository.save(user);

        // Re-login to get tokens with updated role
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
    }

    private AuthResponse registerUser(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
    }
}
