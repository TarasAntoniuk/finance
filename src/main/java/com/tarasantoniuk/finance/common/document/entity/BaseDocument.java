package com.tarasantoniuk.finance.common.document.entity;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.entity.BaseEntity;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base class for all business documents in the system.
 * Provides common document lifecycle management fields and methods.
 * <p>
 * Document Lifecycle:
 * DRAFT → POSTED → (optionally) CANCELLED
 */
@MappedSuperclass
public abstract class BaseDocument extends BaseEntity {

    @Column(nullable = false)
    private LocalDate documentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column
    private LocalDateTime postedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String description;

    // Getters and Setters

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Status check methods

    /**
     * Checks if document can be modified.
     * Only DRAFT documents are mutable.
     */
    public boolean isMutable() {
        return status == DocumentStatus.DRAFT;
    }

    /**
     * Checks if document is posted and creates accounting events.
     */
    public boolean isPosted() {
        return status == DocumentStatus.POSTED;
    }

    /**
     * Checks if document is cancelled.
     */
    public boolean isCancelled() {
        return status == DocumentStatus.CANCELLED;
    }

    // Lifecycle management methods

    /**
     * Marks document as posted.
     * Sets status to POSTED and records posting timestamp.
     *
     * @throws IllegalStateException if document is not in DRAFT status
     */
    public void markAsPosted() {
        if (!isMutable()) {
            throw new IllegalStateException(
                    "Only DRAFT documents can be posted. Current status: " + status
            );
        }
        this.status = DocumentStatus.POSTED;
        this.postedAt = LocalDateTime.now();
    }

    /**
     * Marks document as cancelled.
     * Sets status to CANCELLED and records cancellation timestamp.
     *
     * @throws IllegalStateException if document is not in POSTED status
     */
    public void markAsCancelled() {
        if (!isPosted()) {
            throw new IllegalStateException(
                    "Only POSTED documents can be cancelled. Current status: " + status
            );
        }
        this.status = DocumentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}