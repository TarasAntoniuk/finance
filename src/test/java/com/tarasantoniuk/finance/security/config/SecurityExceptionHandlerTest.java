package com.tarasantoniuk.finance.security.config;

import com.tarasantoniuk.finance.common.ErrorResponse;
import com.tarasantoniuk.finance.security.auth.exception.AccountDisabledException;
import com.tarasantoniuk.finance.security.auth.exception.AccountLockedException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidCredentialsException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidTokenException;
import com.tarasantoniuk.finance.security.user.exception.UserAlreadyExistsException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityExceptionHandlerTest {

    private SecurityExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new SecurityExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
    }

    @Test
    void handleUserAlreadyExistsException_ShouldReturn409() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("test@example.com");

        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExistsException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("/api/auth/login", response.getBody().getPath());
    }

    @Test
    void handleInvalidCredentialsException_ShouldReturn401() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialsException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void handleAccountDisabledException_ShouldReturn403() {
        AccountDisabledException ex = new AccountDisabledException("Account is disabled");

        ResponseEntity<ErrorResponse> response = handler.handleAccountDisabledException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleAccountLockedException_ShouldReturn403() {
        AccountLockedException ex = new AccountLockedException("Account is locked");

        ResponseEntity<ErrorResponse> response = handler.handleAccountLockedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleInvalidTokenException_ShouldReturn401() {
        InvalidTokenException ex = new InvalidTokenException("Invalid refresh token");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidTokenException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleRequestNotPermitted_ShouldReturn429() {
        RequestNotPermitted ex = mock(RequestNotPermitted.class);

        ResponseEntity<ErrorResponse> response = handler.handleRequestNotPermitted(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(429, response.getBody().getStatus());
        assertEquals("Too many requests. Please try again later.", response.getBody().getMessage());
    }
}
