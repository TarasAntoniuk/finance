package com.tarasantoniuk.finance.security.auth.service;

import com.tarasantoniuk.finance.security.auth.dto.AuthResponse;
import com.tarasantoniuk.finance.security.auth.dto.LoginRequest;
import com.tarasantoniuk.finance.security.auth.dto.RegisterRequest;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.token.entity.RefreshToken;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import com.tarasantoniuk.finance.security.token.service.TokenBlacklistService;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import com.tarasantoniuk.finance.security.auth.exception.AccountDisabledException;
import com.tarasantoniuk.finance.security.auth.exception.AccountLockedException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidCredentialsException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidTokenException;
import com.tarasantoniuk.finance.security.user.exception.UserAlreadyExistsException;
import com.tarasantoniuk.finance.security.audit.ClientIpResolver;
import com.tarasantoniuk.finance.security.audit.SecurityAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final ApplicationEventPublisher eventPublisher;
    private final ClientIpResolver clientIpResolver;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       TokenBlacklistService tokenBlacklistService,
                       ApplicationEventPublisher eventPublisher,
                       ClientIpResolver clientIpResolver) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.eventPublisher = eventPublisher;
        this.clientIpResolver = clientIpResolver;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.GUEST);
        userRepository.save(user);

        eventPublisher.publishEvent(SecurityAuditEvent.registration(user.getEmail(), clientIpResolver.resolve()));

        return generateTokenPair(user);
    }

    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class, AccountDisabledException.class})
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new AccountDisabledException("Account is disabled");
        }

        String clientIp = clientIpResolver.resolve();

        if (user.isLocked()) {
            eventPublisher.publishEvent(SecurityAuditEvent.loginFailed(request.getEmail(), clientIp, "account_locked"));
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Try again after " + user.getLockedUntil());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                userRepository.save(user);
                eventPublisher.publishEvent(SecurityAuditEvent.accountLocked(request.getEmail(), clientIp));
                throw new AccountLockedException(
                        "Account locked for " + LOCK_DURATION_MINUTES + " minutes due to too many failed attempts");
            }

            userRepository.save(user);
            eventPublisher.publishEvent(SecurityAuditEvent.loginFailed(request.getEmail(), clientIp, "bad_password"));
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        refreshTokenRepository.revokeAllByUserId(user.getId());

        eventPublisher.publishEvent(SecurityAuditEvent.loginSuccess(user.getEmail(), clientIp));

        return generateTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        if (!jwtService.isTokenValid(rawRefreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String tokenHash = jwtService.hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isUsed()) {
            refreshTokenRepository.revokeAllByUserId(refreshToken.getUser().getId());
            log.warn("SECURITY: Refresh token reuse detected for userId={}. All tokens revoked.",
                    refreshToken.getUser().getId());
            eventPublisher.publishEvent(SecurityAuditEvent.tokenReuseDetected(
                    refreshToken.getUser().getEmail(), clientIpResolver.resolve()));
            throw new InvalidTokenException("Token reuse detected");
        }

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        refreshToken.setUsed(true);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        eventPublisher.publishEvent(SecurityAuditEvent.tokenRefresh(
                refreshToken.getUser().getEmail(), clientIpResolver.resolve()));

        return generateTokenPair(refreshToken.getUser());
    }

    @Transactional
    public void logout(Long userId, String email, String accessToken) {
        String jti = jwtService.extractClaims(accessToken).getId();
        if (jti != null) {
            tokenBlacklistService.blacklist(jti);
        }
        refreshTokenRepository.revokeAllByUserId(userId);
        eventPublisher.publishEvent(SecurityAuditEvent.logout(email, clientIpResolver.resolve()));
    }

    private AuthResponse generateTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(jwtService.hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(
                jwtService.getRefreshTokenExpiration() / 1000
        ));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken);
    }

}