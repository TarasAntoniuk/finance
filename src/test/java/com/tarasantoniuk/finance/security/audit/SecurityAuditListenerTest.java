package com.tarasantoniuk.finance.security.audit;

import com.tarasantoniuk.finance.security.audit.entity.SecurityAuditEntry;
import com.tarasantoniuk.finance.security.audit.repository.SecurityAuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SecurityAuditListenerTest {

    @Mock
    private SecurityAuditEntryRepository repository;

    @InjectMocks
    private SecurityAuditListener listener;

    @Test
    void onSecurityEvent_ShouldPersistEntry() {
        SecurityAuditEvent event = SecurityAuditEvent.loginSuccess("user@example.com", "127.0.0.1");

        listener.onSecurityEvent(event);

        ArgumentCaptor<SecurityAuditEntry> captor = ArgumentCaptor.forClass(SecurityAuditEntry.class);
        verify(repository).save(captor.capture());
        SecurityAuditEntry entry = captor.getValue();
        assertEquals("LOGIN_SUCCESS", entry.getAction());
        assertEquals("user@example.com", entry.getEmail());
        assertEquals("127.0.0.1", entry.getIpAddress());
        assertTrue(entry.isSuccess());
        assertNotNull(entry.getOccurredAt());
    }

    @Test
    void onSecurityEvent_ShouldPersistFailedLoginWithReason() {
        SecurityAuditEvent event = SecurityAuditEvent.loginFailed("user@example.com", "10.0.0.1", "bad_password");

        listener.onSecurityEvent(event);

        ArgumentCaptor<SecurityAuditEntry> captor = ArgumentCaptor.forClass(SecurityAuditEntry.class);
        verify(repository).save(captor.capture());
        SecurityAuditEntry entry = captor.getValue();
        assertEquals("LOGIN_FAILED", entry.getAction());
        assertFalse(entry.isSuccess());
        assertEquals("bad_password", entry.getDetails());
    }

    @Test
    void onSecurityEvent_WhenRepositoryFails_ShouldNotPropagate() {
        doThrow(new RuntimeException("DB down")).when(repository).save(org.mockito.ArgumentMatchers.any());

        SecurityAuditEvent event = SecurityAuditEvent.logout("user@example.com", "127.0.0.1");

        listener.onSecurityEvent(event);
    }

    @Test
    void onSecurityEvent_ShouldTruncateLongDetails() {
        String longDetails = "x".repeat(1000);
        SecurityAuditEvent event = new SecurityAuditEvent("CUSTOM", "user@example.com", "127.0.0.1", true, longDetails);
        when(repository.save(org.mockito.ArgumentMatchers.any(SecurityAuditEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.onSecurityEvent(event);

        ArgumentCaptor<SecurityAuditEntry> captor = ArgumentCaptor.forClass(SecurityAuditEntry.class);
        verify(repository).save(captor.capture());
        assertEquals(500, captor.getValue().getDetails().length());
    }
}
