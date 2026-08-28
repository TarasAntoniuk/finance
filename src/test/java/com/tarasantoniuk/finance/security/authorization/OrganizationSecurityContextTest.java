package com.tarasantoniuk.finance.security.authorization;

import com.tarasantoniuk.finance.security.jwt.JwtPrincipal;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationSecurityContextTest {

    private static final Long ORG_ID = 1L;
    private static final Long OTHER_ORG_ID = 2L;

    private final OrganizationSecurityContext securityContext = new OrganizationSecurityContext();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UserRole role, Long organizationId) {
        JwtPrincipal principal = new JwtPrincipal(10L, "user@example.com", role, organizationId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private void authenticateWithForeignPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null, List.of()));
    }

    // getCurrentPrincipal

    @Test
    void getCurrentPrincipal_WhenAuthenticated_ShouldReturnPrincipal() {
        authenticateAs(UserRole.USER, ORG_ID);

        JwtPrincipal principal = securityContext.getCurrentPrincipal();

        assertEquals(10L, principal.userId());
        assertEquals(ORG_ID, principal.organizationId());
    }

    @Test
    void getCurrentPrincipal_WhenNoAuthentication_ShouldThrow() {
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                securityContext::getCurrentPrincipal);

        assertEquals("No authenticated principal", ex.getMessage());
    }

    @Test
    void getCurrentPrincipal_WhenPrincipalIsNotJwtPrincipal_ShouldThrow() {
        authenticateWithForeignPrincipal();

        assertThrows(AccessDeniedException.class, securityContext::getCurrentPrincipal);
    }

    // hasAuthenticatedPrincipal

    @Test
    void hasAuthenticatedPrincipal_WhenAuthenticated_ShouldReturnTrue() {
        authenticateAs(UserRole.GUEST, ORG_ID);

        assertTrue(securityContext.hasAuthenticatedPrincipal());
    }

    @Test
    void hasAuthenticatedPrincipal_WhenNoAuthentication_ShouldReturnFalse() {
        assertFalse(securityContext.hasAuthenticatedPrincipal());
    }

    @Test
    void hasAuthenticatedPrincipal_WhenPrincipalIsNotJwtPrincipal_ShouldReturnFalse() {
        authenticateWithForeignPrincipal();

        assertFalse(securityContext.hasAuthenticatedPrincipal());
    }

    // isAdmin

    @Test
    void isAdmin_WhenAdmin_ShouldReturnTrue() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        assertTrue(securityContext.isAdmin());
    }

    @Test
    void isAdmin_WhenNotAdmin_ShouldReturnFalse() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertFalse(securityContext.isAdmin());
    }

    // getActiveOrganizationId

    @Test
    void getActiveOrganizationId_WhenOrganizationPresent_ShouldReturnIt() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertEquals(ORG_ID, securityContext.getActiveOrganizationId());
    }

    @Test
    void getActiveOrganizationId_WhenNoOrganization_ShouldThrow() {
        authenticateAs(UserRole.USER, null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                securityContext::getActiveOrganizationId);

        assertEquals("No active organization for current user", ex.getMessage());
    }

    // validateAccess

    @Test
    void validateAccess_WhenOrganizationIdIsNull_ShouldThrow() {
        authenticateAs(UserRole.USER, ORG_ID);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> securityContext.validateAccess(null));

        assertEquals("Organization id is required", ex.getMessage());
    }

    @Test
    void validateAccess_WhenAdmin_ShouldAllowAnyOrganization() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        assertDoesNotThrow(() -> securityContext.validateAccess(OTHER_ORG_ID));
    }

    @Test
    void validateAccess_WhenOwnOrganization_ShouldPass() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertDoesNotThrow(() -> securityContext.validateAccess(ORG_ID));
    }

    @Test
    void validateAccess_WhenForeignOrganization_ShouldThrow() {
        authenticateAs(UserRole.USER, ORG_ID);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> securityContext.validateAccess(OTHER_ORG_ID));

        assertEquals("User does not have access to organization 2", ex.getMessage());
    }

    // resolveOrganizationId

    @Test
    void resolveOrganizationId_WhenAdminWithRequestedOrganization_ShouldReturnRequested() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        assertEquals(OTHER_ORG_ID, securityContext.resolveOrganizationId(OTHER_ORG_ID));
    }

    @Test
    void resolveOrganizationId_WhenAdminWithoutRequestedOrganization_ShouldThrow() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> securityContext.resolveOrganizationId(null));

        assertEquals("Organization id must be specified for admin", ex.getMessage());
    }

    @Test
    void resolveOrganizationId_WhenUserWithoutRequestedOrganization_ShouldReturnActive() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertEquals(ORG_ID, securityContext.resolveOrganizationId(null));
    }

    @Test
    void resolveOrganizationId_WhenUserRequestsOwnOrganization_ShouldReturnActive() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertEquals(ORG_ID, securityContext.resolveOrganizationId(ORG_ID));
    }

    @Test
    void resolveOrganizationId_WhenUserRequestsForeignOrganization_ShouldThrow() {
        authenticateAs(UserRole.USER, ORG_ID);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> securityContext.resolveOrganizationId(OTHER_ORG_ID));

        assertEquals("User does not have access to organization 2", ex.getMessage());
    }

    // resolveOptionalOrganizationId

    @Test
    void resolveOptionalOrganizationId_WhenAdminWithoutRequest_ShouldReturnNullForAllOrganizations() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        assertNull(securityContext.resolveOptionalOrganizationId(null));
    }

    @Test
    void resolveOptionalOrganizationId_WhenAdminWithRequest_ShouldReturnRequested() {
        authenticateAs(UserRole.ADMIN, ORG_ID);

        assertEquals(OTHER_ORG_ID, securityContext.resolveOptionalOrganizationId(OTHER_ORG_ID));
    }

    @Test
    void resolveOptionalOrganizationId_WhenUserWithoutRequest_ShouldReturnActive() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertEquals(ORG_ID, securityContext.resolveOptionalOrganizationId(null));
    }

    @Test
    void resolveOptionalOrganizationId_WhenUserRequestsOwnOrganization_ShouldReturnActive() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertEquals(ORG_ID, securityContext.resolveOptionalOrganizationId(ORG_ID));
    }

    @Test
    void resolveOptionalOrganizationId_WhenUserRequestsForeignOrganization_ShouldThrow() {
        authenticateAs(UserRole.USER, ORG_ID);

        assertThrows(AccessDeniedException.class,
                () -> securityContext.resolveOptionalOrganizationId(OTHER_ORG_ID));
    }

    @Test
    void resolveOptionalOrganizationId_WhenUserHasNoOrganization_ShouldThrow() {
        authenticateAs(UserRole.USER, null);

        assertThrows(AccessDeniedException.class,
                () -> securityContext.resolveOptionalOrganizationId(null));
    }
}
