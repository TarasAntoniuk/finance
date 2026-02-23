package com.tarasantoniuk.finance.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditListener {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    @EventListener
    public void onSecurityEvent(SecurityAuditEvent event) {
        auditLog.info("action={} email={} ip={} success={} details={}",
                event.action(),
                event.email(),
                event.ipAddress(),
                event.success(),
                event.details() != null ? event.details() : "");
    }
}
