package com.tarasantoniuk.finance.security.jwt;

import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.token.service.TokenBlacklistService;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.service.UserRevocationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRevocationService userRevocationService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   TokenBlacklistService tokenBlacklistService,
                                   UserRevocationService userRevocationService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRevocationService = userRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        Optional<Claims> optionalClaims = jwtService.validateAndExtractClaims(token);
        if (optionalClaims.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = optionalClaims.get();

        String jti = claims.getId();
        if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<JwtPrincipal> optionalPrincipal = toPrincipal(claims);
        if (optionalPrincipal.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtPrincipal principal = optionalPrincipal.get();
        if (userRevocationService.isRevoked(principal.userId())) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * Maps a signature-verified token onto a principal. The claims are written by this
     * application, so a shape that no longer parses (a renamed role, a non-numeric subject)
     * is not expected — but it must leave the request anonymous rather than throw, so that
     * JwtAuthenticationEntryPoint answers with the same 401 as every other rejected token.
     */
    private Optional<JwtPrincipal> toPrincipal(Claims claims) {
        try {
            return Optional.of(new JwtPrincipal(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(claims.get("role", String.class)),
                    claims.get("orgId", Long.class)));
        } catch (IllegalArgumentException | NullPointerException | JwtException e) {
            logger.debug("JWT claims could not be mapped to a principal: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html");
    }
}
