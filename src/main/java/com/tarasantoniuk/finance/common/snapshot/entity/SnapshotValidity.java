package com.tarasantoniuk.finance.common.snapshot.entity;

import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Universal entity for tracking snapshot validity across all modules.
 * <p>
 * This entity provides a centralized mechanism to track when balance snapshots
 * become invalid and need recalculation. It supports any module that implements
 * snapshot functionality by using a combination of snapshot type and entity ID.
 * <p>
 * Examples of snapshot types and use cases:
 * <ul>
 *     <li>BANK_ACCOUNT_BALANCE - tracks validity of bank account balance snapshots</li>
 *     <li>PRODUCT_BALANCE - tracks validity of inventory/stock balance snapshots</li>
 *     <li>SETTLEMENT_BALANCE - tracks validity of counterparty settlement snapshots</li>
 * </ul>
 * <p>
 * Lifecycle and logic:
 * <ol>
 *     <li>When an event invalidates snapshots, create record with status=INVALID</li>
 *     <li>When recalculation starts, update status to RECALCULATING</li>
 *     <li>When recalculation completes successfully, DELETE the entire record</li>
 * </ol>
 * <p>
 * <strong>Important:</strong> The absence of a validity record means all snapshots are valid.
 * Only INVALID and RECALCULATING records exist in the table. After successful recalculation,
 * the record is deleted, which simplifies the logic and reduces table size.
 */
@Entity
@Table(
        name = "snapshot_validity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_snapshot_validity",
                        columnNames = {"snapshot_type", "entity_id", "invalid_from_date"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_snapshot_validity_status",
                        columnList = "snapshot_type, entity_id, status"
                ),
                @Index(
                        name = "idx_snapshot_validity_date",
                        columnList = "invalid_from_date"
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class SnapshotValidity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of snapshot being tracked.
     * Examples: "BANK_ACCOUNT_BALANCE", "PRODUCT_BALANCE", "SETTLEMENT_BALANCE"
     */
    @Column(name = "snapshot_type", nullable = false, length = 50)
    private String snapshotType;

    /**
     * ID of the specific entity (e.g., bank account ID, product ID, counterparty ID)
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Date from which snapshots became invalid and need recalculation
     */
    @Column(name = "invalid_from_date", nullable = false)
    private LocalDate invalidFromDate;

    /**
     * Current validity status
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ValidityStatus status;

    /**
     * Reason why snapshots became invalid.
     * Examples: "Document unposted", "Transaction corrected", "Manual invalidation"
     */
    @Column(name = "invalidation_reason", length = 500)
    private String invalidationReason;

    /**
     * ID of the event that caused the invalidation (optional).
     * For audit trail purposes.
     */
    @Column(name = "caused_by_event_id")
    private Long causedByEventId;

    /**
     * Record creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     */
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors

    public SnapshotValidity() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(String snapshotType) {
        this.snapshotType = snapshotType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public LocalDate getInvalidFromDate() {
        return invalidFromDate;
    }

    public void setInvalidFromDate(LocalDate invalidFromDate) {
        this.invalidFromDate = invalidFromDate;
    }

    public ValidityStatus getStatus() {
        return status;
    }

    public void setStatus(ValidityStatus status) {
        this.status = status;
    }

    public String getInvalidationReason() {
        return invalidationReason;
    }

    public void setInvalidationReason(String invalidationReason) {
        this.invalidationReason = invalidationReason;
    }

    public Long getCausedByEventId() {
        return causedByEventId;
    }

    public void setCausedByEventId(Long causedByEventId) {
        this.causedByEventId = causedByEventId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotValidity that = (SnapshotValidity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SnapshotValidity{" +
                "id=" + id +
                ", snapshotType='" + snapshotType + '\'' +
                ", entityId=" + entityId +
                ", invalidFromDate=" + invalidFromDate +
                ", status=" + status +
                ", invalidationReason='" + invalidationReason + '\'' +
                ", causedByEventId=" + causedByEventId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
