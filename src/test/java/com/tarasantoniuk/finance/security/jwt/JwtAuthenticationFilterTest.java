package com.tarasantoniuk.finance.security.jwt;

import com.tarasantoniuk.finance.security.jwt.service.JwtService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

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

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WhenValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("sub", "1");
        claimMap.put("email", "test@example.com");
        claimMap.put("role", "USER");
        Claims claims = new DefaultClaims(claimMap);

        when(jwtService.extractClaims("valid-token")).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, principal.userId());
        assertEquals("test@example.com", principal.email());
        assertEquals(UserRole.USER, principal.role());
    }

    @Test
    void doFilterInternal_WhenValidTokenWithAdminRole_ShouldSetAdminAuthority() throws ServletException, IOException {
        request.setRequestURI("/api/v1/currencies");
        request.addHeader("Authorization", "Bearer admin-token");

        when(jwtService.isTokenValid("admin-token")).thenReturn(true);

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("sub", "2");
        claimMap.put("email", "admin@example.com");
        claimMap.put("role", "ADMIN");
        Claims claims = new DefaultClaims(claimMap);

        when(jwtService.extractClaims("admin-token")).thenReturn(claims);

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

        when(jwtService.isTokenValid("guest-token")).thenReturn(true);

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("sub", "3");
        claimMap.put("email", "guest@example.com");
        claimMap.put("role", "GUEST");
        Claims claims = new DefaultClaims(claimMap);

        when(jwtService.extractClaims("guest-token")).thenReturn(claims);

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

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);

        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                "existing-user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractClaims(anyString());
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
}
