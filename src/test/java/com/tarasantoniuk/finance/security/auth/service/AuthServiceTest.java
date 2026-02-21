package com.tarasantoniuk.finance.security.auth.service;

import com.tarasantoniuk.finance.security.auth.dto.AuthResponse;
import com.tarasantoniuk.finance.security.auth.dto.LoginRequest;
import com.tarasantoniuk.finance.security.auth.dto.RegisterRequest;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.token.entity.RefreshToken;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.auth.exception.AccountDisabledException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidCredentialsException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidTokenException;
import com.tarasantoniuk.finance.security.user.exception.UserAlreadyExistsException;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
        user.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    // ========== REGISTER ==========

    @Test
    void register_WhenValidRequest_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_ShouldSaveUserWithCorrectFields() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getRole());
    }

    @Test
    void register_WhenDuplicateEmail_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== LOGIN ==========

    @Test
    void login_WhenValidCredentials_ShouldReturnAuthResponse() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(refreshTokenRepository).revokeAllByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_WhenEmailNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void login_WhenWrongPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void login_WhenUserInactive_ShouldThrowAccountDisabledException() {
        user.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AccountDisabledException exception = assertThrows(AccountDisabledException.class,
                () -> authService.login(loginRequest));

        assertEquals("Account is disabled", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    // ========== REFRESH ==========

    @Test
    void refresh_WhenValidToken_ShouldReturnNewAuthResponse() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("raw-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthResponse response = authService.refresh("raw-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository).save(storedToken);
        verify(refreshTokenRepository).save(argThat(rt -> rt != storedToken));
    }

    @Test
    void refresh_WhenInvalidJwt_ShouldThrowInvalidTokenException() {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("invalid-token"));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void refresh_WhenTokenRevoked_ShouldThrowInvalidTokenException() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(true);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("revoked-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("revoked-token"));

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refresh_WhenTokenExpired_ShouldThrowInvalidTokenException() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(jwtService.isTokenValid("expired-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("expired-token"));

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    // ========== LOGOUT ==========

    @Test
    void logout_ShouldRevokeAllUserTokens() {
        authService.logout(1L);

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}
