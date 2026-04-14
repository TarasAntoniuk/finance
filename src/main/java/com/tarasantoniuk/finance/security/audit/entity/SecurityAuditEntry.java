package com.tarasantoniuk.finance.security.audit.entity;

import com.tarasantoniuk.finance.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_entries", indexes = {
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_email", columnList = "email"),
        @Index(name = "idx_audit_occurred_at", columnList = "occurredAt")
})
public class SecurityAuditEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_audit_seq")
    @SequenceGenerator(
            name = "security_audit_seq",
            sequenceName = "security_audit_entry_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 200)
    private String email;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 500)
    private String details;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    public SecurityAuditEntry() {
    }

    public SecurityAuditEntry(String action, String email, String ipAddress,
                              boolean success, String details, LocalDateTime occurredAt) {
        this.action = action;
        this.email = email;
        this.ipAddress = ipAddress;
        this.success = success;
        this.details = details;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
