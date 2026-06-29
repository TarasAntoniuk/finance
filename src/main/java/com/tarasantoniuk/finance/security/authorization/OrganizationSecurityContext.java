package com.tarasantoniuk.finance.security.authorization;

import com.tarasantoniuk.finance.security.jwt.JwtPrincipal;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OrganizationSecurityContext {

    public JwtPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new AccessDeniedException("No authenticated principal");
        }
        return principal;
    }

    /**
     * Tells whether the current thread is running under an authenticated HTTP request.
     * Used by defense-in-depth helpers to skip access checks for scheduler / internal
     * callers that have no SecurityContext.
     */
    public boolean hasAuthenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal;
    }

    public boolean isAdmin() {
        return getCurrentPrincipal().role() == UserRole.ADMIN;
    }

    public Long getActiveOrganizationId() {
        JwtPrincipal principal = getCurrentPrincipal();
        Long organizationId = principal.organizationId();
        if (organizationId == null) {
            throw new AccessDeniedException("No active organization for current user");
        }
        return organizationId;
    }

    public void validateAccess(Long organizationId) {
        if (organizationId == null) {
            throw new AccessDeniedException("Organization id is required");
        }
        JwtPrincipal principal = getCurrentPrincipal();
        if (principal.role() == UserRole.ADMIN) {
            return;
        }
        if (!organizationId.equals(principal.organizationId())) {
            throw new AccessDeniedException(
                    "User does not have access to organization " + organizationId);
        }
    }

    public Long resolveOrganizationId(Long requested) {
        if (isAdmin()) {
            if (requested == null) {
                throw new AccessDeniedException("Organization id must be specified for admin");
            }
            return requested;
        }
        Long active = getActiveOrganizationId();
        if (requested != null && !requested.equals(active)) {
            throw new AccessDeniedException(
                    "User does not have access to organization " + requested);
        }
        return active;
    }

    /**
     * Resolve an optional organization filter for cross-organization queries (e.g. reports).
     * Admin: pass-through (null = all organizations).
     * Non-admin: forced to active org; throws if a different org was requested.
     */
    public Long resolveOptionalOrganizationId(Long requested) {
        if (isAdmin()) {
            return requested;
        }
        Long active = getActiveOrganizationId();
        if (requested != null && !requested.equals(active)) {
            throw new AccessDeniedException(
                    "User does not have access to organization " + requested);
        }
        return active;
    }
}
