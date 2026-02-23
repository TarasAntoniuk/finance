package com.tarasantoniuk.finance.security.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.security.auth.dto.AccessTokenResponse;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AdminUserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user and get token
        RegisterRequest adminRegister = new RegisterRequest();
        adminRegister.setEmail("admin@example.com");
        adminRegister.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isCreated());

        // Promote to ADMIN
        User admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        // Re-login to get token with ADMIN role
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"SecureP@ss1\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AccessTokenResponse tokenResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AccessTokenResponse.class);
        adminAccessToken = tokenResponse.getAccessToken();

        // Create a regular user
        RegisterRequest userRegister = new RegisterRequest();
        userRegister.setEmail("user@example.com");
        userRegister.setPassword("SecureP@ss1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isCreated());
    }

    // ========== LIST USERS ==========

    @Test
    void listUsers_WhenAdmin_ShouldReturn200WithUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].email").exists())
                .andExpect(jsonPath("$.content[0].role").exists())
                .andExpect(jsonPath("$.content[0].isActive").exists())
                .andExpect(jsonPath("$.metadata.totalElements").value(2));
    }

    @Test
    void listUsers_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsers_WhenNotAdmin_ShouldReturn403() throws Exception {
        // Login as regular user
        String userToken = loginAsUser("user@example.com");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ========== GET USER ==========

    @Test
    void getUser_WhenAdmin_ShouldReturn200WithDetails() throws Exception {
        User user = userRepository.findByEmail("user@example.com").orElseThrow();

        mockMvc.perform(get("/api/admin/users/" + user.getId())
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("GUEST"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.failedLoginAttempts").value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getUser_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNotFound());
    }

    // ========== CHANGE ROLE ==========

    @Test
    void changeRole_WhenAdmin_ShouldReturn204() throws Exception {
        User user = userRepository.findByEmail("user@example.com").orElseThrow();

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/role")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(UserRole.USER, updated.getRole());
    }

    @Test
    void changeRole_WhenNotAdmin_ShouldReturn403() throws Exception {
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        String userToken = loginAsUser("user@example.com");

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/role")
                        .param("role", "ADMIN")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ========== CHANGE STATUS ==========

    @Test
    void changeStatus_WhenAdmin_ShouldReturn204() throws Exception {
        User user = userRepository.findByEmail("user@example.com").orElseThrow();

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/status")
                        .param("active", "false")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(updated.getIsActive());
    }

    @Test
    void changeStatus_WhenNotAdmin_ShouldReturn403() throws Exception {
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        String userToken = loginAsUser("user@example.com");

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/status")
                        .param("active", "false")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private String loginAsUser(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"SecureP@ss1\"}"))
                .andExpect(status().isOk())
                .andReturn();

        AccessTokenResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccessTokenResponse.class);
        return response.getAccessToken();
    }

    private void assertEquals(UserRole expected, UserRole actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    private void assertFalse(Boolean value) {
        org.junit.jupiter.api.Assertions.assertFalse(value);
    }
}
