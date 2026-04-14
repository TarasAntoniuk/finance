package com.tarasantoniuk.finance.security.audit;

import com.tarasantoniuk.finance.security.audit.entity.SecurityAuditEntry;
import com.tarasantoniuk.finance.security.audit.repository.SecurityAuditEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SecurityAuditListener {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final Logger log = LoggerFactory.getLogger(SecurityAuditListener.class);
    private static final int MAX_DETAILS_LENGTH = 500;

    private final SecurityAuditEntryRepository repository;

    public SecurityAuditListener(SecurityAuditEntryRepository repository) {
        this.repository = repository;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSecurityEvent(SecurityAuditEvent event) {
        auditLog.info("action={} email={} ip={} success={} details={}",
                event.action(),
                event.email(),
                event.ipAddress(),
                event.success(),
                event.details() != null ? event.details() : "");

        try {
            repository.save(new SecurityAuditEntry(
                    event.action(),
                    event.email(),
                    event.ipAddress(),
                    event.success(),
                    truncate(event.details()),
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Failed to persist security audit entry for action={}", event.action(), e);
        }
    }

    private String truncate(String details) {
        if (details == null || details.length() <= MAX_DETAILS_LENGTH) {
            return details;
        }
        return details.substring(0, MAX_DETAILS_LENGTH);
    }
}
