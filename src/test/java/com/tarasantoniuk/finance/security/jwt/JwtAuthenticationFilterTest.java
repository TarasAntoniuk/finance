package com.tarasantoniuk.finance.security.jwt;

import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.token.service.TokenBlacklistService;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== doFilterInternal ==========

    @Test
    void doFilterInternal_WhenNoAuthHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenAuthHeaderNotBearer_ShouldContinueFilterChain() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenInvalidToken_ShouldContinueWithoutAuth() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.validateAndExtractClaims("invalid-token")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer valid-token");

        Claims claims = createClaims("test-jti", "1", "test@example.com", "USER", null);
        when(jwtService.validateAndExtractClaims("valid-token")).thenReturn(Optional.of(claims));
        when(tokenBlacklistService.isBlacklisted("test-jti")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, principal.userId());
        assertEquals("test@example.com", principal.email());
        assertEquals(UserRole.USER, principal.role());
        assertNull(principal.organizationId());
    }

    @Test
    void doFilterInternal_WhenTokenBlacklisted_ShouldNotSetAuthentication() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer blacklisted-token");

        Claims claims = createClaims("blacklisted-jti", "1", "test@example.com", "USER", null);
        when(jwtService.validateAndExtractClaims("blacklisted-token")).thenReturn(Optional.of(claims));
        when(tokenBlacklistService.isBlacklisted("blacklisted-jti")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenValidTokenWithAdminRole_ShouldSetAdminAuthority() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer admin-token");

        Claims claims = createClaims(null, "2", "admin@example.com", "ADMIN", null);
        when(jwtService.validateAndExtractClaims("admin-token")).thenReturn(Optional.of(claims));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void doFilterInternal_WhenValidTokenWithGuestRole_ShouldSetGuestAuthority() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer guest-token");

        Claims claims = createClaims(null, "3", "guest@example.com", "GUEST", null);
        when(jwtService.validateAndExtractClaims("guest-token")).thenReturn(Optional.of(claims));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST")));
    }

    @Test
    void doFilterInternal_WhenAlreadyAuthenticated_ShouldNotOverrideAuthentication() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer valid-token");

        Claims claims = createClaims(null, "1", "test@example.com", "USER", null);
        when(jwtService.validateAndExtractClaims("valid-token")).thenReturn(Optional.of(claims));

        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                "existing-user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    // ========== shouldNotFilter ==========

    @Test
    void shouldNotFilter_WhenSwaggerUiPath_ShouldReturnTrue() {
        request.setRequestURI("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenSwaggerUiHtml_ShouldReturnTrue() {
        request.setRequestURI("/swagger-ui.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenApiDocsPath_ShouldReturnTrue() {
        request.setRequestURI("/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenApiDocsSubPath_ShouldReturnTrue() {
        request.setRequestURI("/api-docs/swagger-config");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenWebjarsPath_ShouldReturnTrue() {
        request.setRequestURI("/webjars/swagger-ui/index.css");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenApiEndpoint_ShouldReturnFalse() {
        request.setRequestURI("/api/v1/currencies");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_WhenAuthEndpoint_ShouldReturnFalse() {
        request.setRequestURI("/api/auth/login");
        assertFalse(filter.shouldNotFilter(request));
    }

    private Claims createClaims(String jti, String sub, String email, String role, Long orgId) {
        Map<String, Object> claimMap = new HashMap<>();
        if (jti != null) {
            claimMap.put("jti", jti);
        }
        claimMap.put("sub", sub);
        claimMap.put("email", email);
        claimMap.put("role", role);
        if (orgId != null) {
            claimMap.put("orgId", orgId);
        }
        return new DefaultClaims(claimMap);
    }
}
