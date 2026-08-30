package com.tarasantoniuk.finance.security.auth.service;

import com.tarasantoniuk.finance.common.config.ProvisioningProperties;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.auth.LockoutProperties;
import com.tarasantoniuk.finance.security.auth.dto.AuthResponse;
import com.tarasantoniuk.finance.security.auth.dto.LoginRequest;
import com.tarasantoniuk.finance.security.auth.dto.RegisterRequest;
import com.tarasantoniuk.finance.security.auth.exception.GoogleAuthenticationException;
import com.tarasantoniuk.finance.security.auth.google.GoogleIdTokenVerifier;
import com.tarasantoniuk.finance.security.auth.google.GoogleUserInfo;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.token.entity.RefreshToken;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import com.tarasantoniuk.finance.security.token.service.TokenBlacklistService;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.auth.exception.AccountDisabledException;
import com.tarasantoniuk.finance.security.auth.exception.AccountLockedException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidCredentialsException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidTokenException;
import com.tarasantoniuk.finance.security.user.exception.UserAlreadyExistsException;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import com.tarasantoniuk.finance.security.audit.ClientIpResolver;
import io.jsonwebtoken.Claims;
import org.springframework.context.ApplicationEventPublisher;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ClientIpResolver clientIpResolver;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private OrganizationRepository organizationRepository;

    private LockoutProperties lockoutProperties;
    private ProvisioningProperties provisioningProperties;

    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        lockoutProperties = new LockoutProperties();
        provisioningProperties = new ProvisioningProperties(1L);
        authService = new AuthService(userRepository, refreshTokenRepository, jwtService,
                passwordEncoder, tokenBlacklistService, eventPublisher, clientIpResolver, lockoutProperties,
                googleIdTokenVerifier, organizationRepository, provisioningProperties);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.GUEST);
        user.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("SecureP@ss1");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("SecureP@ss1");
    }

    private final Organization defaultOrganization = new Organization();

    // ========== REGISTER ==========

    @Test
    void register_WhenValidRequest_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss1")).thenReturn("encodedPassword");
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(defaultOrganization));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_ShouldSaveUserWithCorrectFields() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss1")).thenReturn("encodedPassword");
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(defaultOrganization));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserRole.GUEST, savedUser.getRole());
        assertSame(defaultOrganization, savedUser.getOrganization());
    }

    @Test
    void register_WhenDefaultOrganizationMissing_ShouldThrowIllegalState() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
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
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecureP@ss1", "encodedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenRepository).revokeAllByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_WhenEmailNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void login_WhenWrongPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecureP@ss1", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
        assertEquals(1, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void login_WhenUserInactive_ShouldThrowAccountDisabledException() {
        user.setIsActive(false);
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));

        AccountDisabledException exception = assertThrows(AccountDisabledException.class,
                () -> authService.login(loginRequest));

        assertEquals("Account is disabled", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    // ========== LOGIN - ACCOUNT LOCKOUT ==========

    @Test
    void login_WhenAccountLocked_ShouldThrowAccountLockedException() {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WhenMaxFailedAttemptsReached_ShouldLockAccount() {
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecureP@ss1", "encodedPassword")).thenReturn(false);

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest));
        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void login_WhenSuccessfulAfterFailedAttempts_ShouldResetCounter() {
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecureP@ss1", "encodedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        authService.login(loginRequest);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void login_WhenLockExpired_ShouldAllowLogin() {
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmailForUpdate("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecureP@ss1", "encodedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
    }

    // ========== GOOGLE LOGIN ==========

    @Test
    void loginWithGoogle_WhenNewUser_ShouldProvisionGuestInDefaultOrganization() {
        when(googleIdTokenVerifier.verify("id-token"))
                .thenReturn(new GoogleUserInfo("new@example.com", true, "New User", "google-sub"));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        Organization defaultOrg = new Organization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(defaultOrg));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedRandom");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.loginWithGoogle("id-token");

        assertEquals("access-token", response.accessToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals(UserRole.GUEST, savedUser.getRole());
        assertSame(defaultOrg, savedUser.getOrganization());
        assertTrue(savedUser.getIsActive());
    }

    @Test
    void loginWithGoogle_WhenExistingUser_ShouldLoginWithoutChangingRoleOrOrganization() {
        when(googleIdTokenVerifier.verify("id-token"))
                .thenReturn(new GoogleUserInfo("test@example.com", true, "Test", "google-sub"));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.loginWithGoogle("id-token");

        assertEquals("access-token", response.accessToken());
        assertEquals(UserRole.GUEST, user.getRole());
        verify(userRepository, never()).save(any(User.class));
        verify(organizationRepository, never()).findById(anyLong());
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    void loginWithGoogle_WhenEmailNotVerified_ShouldThrowGoogleAuthenticationException() {
        when(googleIdTokenVerifier.verify("id-token"))
                .thenReturn(new GoogleUserInfo("new@example.com", false, "New", "google-sub"));

        assertThrows(GoogleAuthenticationException.class,
                () -> authService.loginWithGoogle("id-token"));

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loginWithGoogle_WhenExistingUserDisabled_ShouldThrowAccountDisabledException() {
        user.setIsActive(false);
        when(googleIdTokenVerifier.verify("id-token"))
                .thenReturn(new GoogleUserInfo("test@example.com", true, "Test", "google-sub"));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountDisabledException.class,
                () -> authService.loginWithGoogle("id-token"));

        verify(refreshTokenRepository, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void loginWithGoogle_WhenDefaultOrganizationMissing_ShouldThrowIllegalState() {
        when(googleIdTokenVerifier.verify("id-token"))
                .thenReturn(new GoogleUserInfo("new@example.com", true, "New", "google-sub"));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> authService.loginWithGoogle("id-token"));

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== REFRESH ==========

    @Test
    void refresh_WhenValidToken_ShouldReturnNewAuthResponse() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("raw-refresh-token")).thenReturn(true);
        when(jwtService.hashToken("raw-refresh-token")).thenReturn("hashed-token");
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtService.hashToken("new-refresh-token")).thenReturn("new-hashed-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.refresh("raw-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
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
        when(jwtService.hashToken("revoked-token")).thenReturn("hashed-token");
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(storedToken));

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
        when(jwtService.hashToken("expired-token")).thenReturn("hashed-token");
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(storedToken));

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("expired-token"));

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refresh_WhenTokenHashNotFound_ShouldThrowInvalidTokenException() {
        when(jwtService.isTokenValid("unknown-token")).thenReturn(true);
        when(jwtService.hashToken("unknown-token")).thenReturn("unknown-hash");
        when(refreshTokenRepository.findByTokenHash("unknown-hash")).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("unknown-token"));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refresh_ShouldSaveNewRefreshTokenWithCorrectExpiration() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("raw-token")).thenReturn(true);
        when(jwtService.hashToken("raw-token")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");
        when(jwtService.hashToken("new-refresh")).thenReturn("new-hashed");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        authService.refresh("raw-token");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(tokenCaptor.capture());

        // The second saved token is the new one
        RefreshToken newToken = tokenCaptor.getAllValues().get(1);
        assertEquals("new-hashed", newToken.getTokenHash());
        assertEquals(user, newToken.getUser());
        assertFalse(newToken.isRevoked());
    }

    // ========== REFRESH - REUSE DETECTION ==========

    @Test
    void refresh_WhenTokenAlreadyUsed_ShouldRevokeAllAndThrow() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setUsed(true);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("reused-token")).thenReturn(true);
        when(jwtService.hashToken("reused-token")).thenReturn("hashed-token");
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(storedToken));

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> authService.refresh("reused-token"));

        assertEquals("Token reuse detected", exception.getMessage());
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    void refresh_WhenValidToken_ShouldMarkAsUsed() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setRevoked(false);
        storedToken.setUsed(false);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.isTokenValid("raw-token")).thenReturn(true);
        when(jwtService.hashToken("raw-token")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");
        when(jwtService.hashToken("new-refresh")).thenReturn("new-hashed");
        when(jwtService.extractExpiration(anyString())).thenReturn(LocalDateTime.now().plusDays(7));

        authService.refresh("raw-token");

        assertTrue(storedToken.isUsed());
        assertTrue(storedToken.isRevoked());
    }

    // ========== CHANGE PASSWORD ==========

    @Test
    void changePassword_WhenValidCurrentPassword_ShouldUpdateAndRevokeTokensAndBlacklistAccessToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldP@ss1", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewP@ss1")).thenReturn("newEncodedPassword");

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("jti", "access-jti");
        claimMap.put("exp", new java.util.Date(System.currentTimeMillis() + 900_000));
        Claims claims = new DefaultClaims(claimMap);
        when(jwtService.extractClaims("current-access-token")).thenReturn(claims);

        authService.changePassword(1L, "OldP@ss1", "NewP@ss1", "current-access-token");

        assertEquals("newEncodedPassword", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenBlacklistService).blacklist(eq("access-jti"), any(java.time.LocalDateTime.class));
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    void changePassword_WhenWrongCurrentPassword_ShouldThrowInvalidCredentials() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongP@ss1", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.changePassword(1L, "WrongP@ss1", "NewP@ss1", "access-token"));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WhenUserNotFound_ShouldThrowInvalidCredentials() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.changePassword(99L, "OldP@ss1", "NewP@ss1", "access-token"));
    }

    // ========== LOGOUT ==========

    @Test
    void logout_ShouldBlacklistAccessTokenAndRevokeAllRefreshTokens() {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("jti", "test-jti");
        claimMap.put("exp", new java.util.Date(System.currentTimeMillis() + 900_000));
        Claims claims = new DefaultClaims(claimMap);

        when(jwtService.extractClaims("access-token")).thenReturn(claims);

        authService.logout(1L, "test@example.com", "access-token");

        verify(tokenBlacklistService).blacklist(eq("test-jti"), any(java.time.LocalDateTime.class));
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    void logout_WhenJtiIsNull_ShouldStillRevokeRefreshTokens() {
        Map<String, Object> claimMap = new HashMap<>();
        Claims claims = new DefaultClaims(claimMap);

        when(jwtService.extractClaims("access-token")).thenReturn(claims);

        authService.logout(1L, "test@example.com", "access-token");

        verify(tokenBlacklistService, never()).blacklist(anyString(), any(java.time.LocalDateTime.class));
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }
}
