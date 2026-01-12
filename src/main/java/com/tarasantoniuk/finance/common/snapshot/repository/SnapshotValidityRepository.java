package com.tarasantoniuk.finance.common.snapshot.repository;

import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing snapshot validity records across all modules.
 * <p>
 * This repository provides centralized access to snapshot validity tracking data.
 * It supports any module that implements snapshot functionality by using a combination
 * of snapshot type and entity ID.
 * <p>
 * Examples of snapshot types:
 * <ul>
 *     <li>BANK_ACCOUNT_BALANCE - bank account balance snapshots</li>
 *     <li>PRODUCT_BALANCE - inventory/stock balance snapshots</li>
 *     <li>SETTLEMENT_BALANCE - counterparty settlement balance snapshots</li>
 * </ul>
 * <p>
 * <strong>Important:</strong> Records are deleted after successful recalculation.
 * There is no permanent history - only current INVALID and RECALCULATING records exist.
 */
@Repository
public interface SnapshotValidityRepository extends JpaRepository<SnapshotValidity, Long> {

    /**
     * Find active (INVALID or RECALCULATING) validity record for specific entity.
     * Returns earliest invalidation if multiple exist.
     *
     * @param snapshotType the type of snapshot (e.g., "BANK_ACCOUNT_BALANCE")
     * @param entityId the ID of the entity
     * @return optional validity record
     */
    @Query("""
            SELECT v FROM SnapshotValidity v
            WHERE v.snapshotType = :snapshotType
            AND v.entityId = :entityId
            AND v.status IN ('INVALID', 'RECALCULATING')
            ORDER BY v.invalidFromDate ASC
            """)
    Optional<SnapshotValidity> findActiveBySnapshotTypeAndEntityId(
            @Param("snapshotType") String snapshotType,
            @Param("entityId") Long entityId);

    /**
     * Find all validity records with given status.
     *
     * @param status the validity status
     * @return list of validity records
     */
    List<SnapshotValidity> findByStatus(ValidityStatus status);

    /**
     * Find all validity records for specific snapshot type and status.
     * Useful for module-specific batch processing.
     *
     * @param snapshotType the type of snapshot (e.g., "BANK_ACCOUNT_BALANCE")
     * @param status the validity status
     * @return list of validity records
     */
    List<SnapshotValidity> findBySnapshotTypeAndStatus(String snapshotType, ValidityStatus status);

    /**
     * Check if snapshot is valid for given date.
     * Returns false if any INVALID record exists with invalidFromDate <= date.
     *
     * @param snapshotType the type of snapshot (e.g., "BANK_ACCOUNT_BALANCE")
     * @param entityId the ID of the entity
     * @param date the date to check
     * @return true if snapshot is valid, false otherwise
     */
    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN false ELSE true END
            FROM SnapshotValidity v
            WHERE v.snapshotType = :snapshotType
            AND v.entityId = :entityId
            AND v.status = 'INVALID'
            AND v.invalidFromDate <= :date
            """)
    boolean isSnapshotValidForDate(
            @Param("snapshotType") String snapshotType,
            @Param("entityId") Long entityId,
            @Param("date") LocalDate date);

    /**
     * Get earliest invalidation date for entity.
     * Returns empty if no invalid snapshots exist.
     *
     * @param snapshotType the type of snapshot (e.g., "BANK_ACCOUNT_BALANCE")
     * @param entityId the ID of the entity
     * @return optional invalidation date
     */
    @Query("""
            SELECT v.invalidFromDate FROM SnapshotValidity v
            WHERE v.snapshotType = :snapshotType
            AND v.entityId = :entityId
            AND v.status IN ('INVALID', 'RECALCULATING')
            ORDER BY v.invalidFromDate ASC
            LIMIT 1
            """)
    Optional<LocalDate> getInvalidFromDate(
            @Param("snapshotType") String snapshotType,
            @Param("entityId") Long entityId);

    /**
     * Delete all validity records for specific entity.
     * Used when entity is deleted or after successful recalculation.
     *
     * @param snapshotType the type of snapshot (e.g., "BANK_ACCOUNT_BALANCE")
     * @param entityId the ID of the entity
     */
    void deleteBySnapshotTypeAndEntityId(String snapshotType, Long entityId);
}
