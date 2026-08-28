package com.tarasantoniuk.finance.security.config;

import com.tarasantoniuk.finance.common.ErrorResponse;
import com.tarasantoniuk.finance.security.auth.exception.AccountDisabledException;
import com.tarasantoniuk.finance.security.auth.exception.AccountLockedException;
import com.tarasantoniuk.finance.security.auth.exception.GoogleAuthenticationException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidCredentialsException;
import com.tarasantoniuk.finance.security.auth.exception.InvalidTokenException;
import com.tarasantoniuk.finance.security.user.exception.LastAdminException;
import com.tarasantoniuk.finance.security.user.exception.SelfModificationException;
import com.tarasantoniuk.finance.security.user.exception.UserAlreadyExistsException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(1)
public class SecurityExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabledException(
            AccountDisabledException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            AccountLockedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Handles denials raised inside the service layer (OrganizationSecurityContext).
     * Denials at the filter chain level are handled by CustomAccessDeniedHandler instead.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(SelfModificationException.class)
    public ResponseEntity<ErrorResponse> handleSelfModificationException(
            SelfModificationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(LastAdminException.class)
    public ResponseEntity<ErrorResponse> handleLastAdminException(
            LastAdminException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRequestNotPermitted(
            RequestNotPermitted ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
