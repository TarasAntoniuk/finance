package com.tarasantoniuk.finance.security.audit;

public record SecurityAuditEvent(
        String action,
        String email,
        String ipAddress,
        boolean success,
        String details
) {

    public static SecurityAuditEvent loginSuccess(String email, String ip) {
        return new SecurityAuditEvent("LOGIN_SUCCESS", email, ip, true, null);
    }

    public static SecurityAuditEvent loginFailed(String email, String ip, String reason) {
        return new SecurityAuditEvent("LOGIN_FAILED", email, ip, false, reason);
    }

    public static SecurityAuditEvent logout(String email, String ip) {
        return new SecurityAuditEvent("LOGOUT", email, ip, true, null);
    }

    public static SecurityAuditEvent tokenRefresh(String email, String ip) {
        return new SecurityAuditEvent("TOKEN_REFRESH", email, ip, true, null);
    }

    public static SecurityAuditEvent accountLocked(String email, String ip) {
        return new SecurityAuditEvent("ACCOUNT_LOCKED", email, ip, false, "Too many failed attempts");
    }

    public static SecurityAuditEvent tokenReuseDetected(String email, String ip) {
        return new SecurityAuditEvent("TOKEN_REUSE_DETECTED", email, ip, false, "Potential token theft");
    }

    public static SecurityAuditEvent registration(String email, String ip) {
        return new SecurityAuditEvent("REGISTRATION", email, ip, true, null);
    }
}
