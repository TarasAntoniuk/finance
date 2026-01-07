package com.tarasantoniuk.finance.common.snapshot.service;

import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
import com.tarasantoniuk.finance.common.snapshot.repository.SnapshotValidityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for module-specific snapshot validity services.
 * <p>
 * This class provides common validity lifecycle operations for all modules that implement
 * snapshot functionality. Subclasses must implement {@link #getSnapshotType()} to define
 * their specific snapshot type.
 * <p>
 * Example subclass implementation:
 * <pre>
 * {@code
 * @Service
 * public class BankAccountSnapshotValidityService extends AbstractSnapshotValidityService {
 *
 *     public BankAccountSnapshotValidityService(SnapshotValidityRepository repository) {
 *         super(repository);
 *     }
 *
 *     @Override
 *     protected String getSnapshotType() {
 *         return "BANK_ACCOUNT_BALANCE";
 *     }
 * }
 * }
 * </pre>
 * <p>
 * Validity lifecycle:
 * <ol>
 *     <li>When backdated transaction occurs: {@link #invalidateSnapshots} creates INVALID record</li>
 *     <li>Before recalculation starts: {@link #markAsRecalculating} updates to RECALCULATING</li>
 *     <li>After successful recalculation: {@link #deleteValidity} removes the record</li>
 * </ol>
 * <p>
 * <strong>Important:</strong> After successful recalculation, validity records are deleted.
 * The absence of a record means all snapshots are valid.
 */
@Transactional
public abstract class AbstractSnapshotValidityService {

    private static final Logger log = LoggerFactory.getLogger(AbstractSnapshotValidityService.class);

    protected final SnapshotValidityRepository repository;

    protected AbstractSnapshotValidityService(SnapshotValidityRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the snapshot type handled by this service (e.g., "BANK_ACCOUNT_BALANCE").
     * <p>
     * This method must be implemented by subclasses to define their specific snapshot type.
     * The snapshot type is used as a discriminator in the snapshot_validity table to separate
     * records from different modules.
     *
     * @return the snapshot type identifier
     */
    protected abstract String getSnapshotType();

    /**
     * Invalidate snapshots starting from given datetime.
     * <p>
     * Called when a backdated transaction is created. If an earlier invalidation already exists,
     * updates to the earlier date. Otherwise, creates a new INVALID record.
     * <p>
     * This method is idempotent - calling it multiple times with the same or later dates
     * will not create duplicate records.
     *
     * @param entityId the ID of the entity (e.g., bank account ID)
     * @param invalidFromDateTime the datetime from which snapshots became invalid
     * @param causedByEventId the ID of the event that caused invalidation (optional)
     */
    public void invalidateSnapshots(Long entityId, LocalDateTime invalidFromDateTime, Long causedByEventId) {
        String snapshotType = getSnapshotType();
        LocalDate invalidFromDate = invalidFromDateTime.toLocalDate();

        log.info("Invalidating {} snapshots for entity {} from date {} caused by event {}",
                snapshotType, entityId, invalidFromDate, causedByEventId);

        Optional<SnapshotValidity> validityOpt = repository.findActiveBySnapshotTypeAndEntityId(
                snapshotType, entityId);

        if (validityOpt.isPresent()) {
            SnapshotValidity validity = validityOpt.get();

            // If new invalidation date is earlier - update it
            if (invalidFromDate.isBefore(validity.getInvalidFromDate())) {
                log.info("Updating existing validity record: moving invalid_from_date from {} to {}",
                        validity.getInvalidFromDate(), invalidFromDate);

                validity.setInvalidFromDate(invalidFromDate);
                validity.setCausedByEventId(causedByEventId);
                validity.setStatus(ValidityStatus.INVALID);
                repository.save(validity);
            } else {
                log.debug("Existing validity record already covers this invalidation date");
            }
        } else {
            // Create new validity record
            log.info("Creating new validity record for {} entity {} from date {}",
                    snapshotType, entityId, invalidFromDate);

            SnapshotValidity validity = new SnapshotValidity();
            validity.setSnapshotType(snapshotType);
            validity.setEntityId(entityId);
            validity.setInvalidFromDate(invalidFromDate);
            validity.setCausedByEventId(causedByEventId);
            validity.setInvalidationReason("BACKDATED_TRANSACTION");
            validity.setStatus(ValidityStatus.INVALID);
            repository.save(validity);
        }
    }

    /**
     * Check if snapshot is valid for given date.
     * <p>
     * Returns true if no INVALID records exist for this date. This is the primary method
     * for checking whether cached snapshots can be trusted.
     *
     * @param entityId the ID of the entity
     * @param date the date to check
     * @return true if snapshot is valid, false if invalid
     */
    @Transactional(readOnly = true)
    public boolean isSnapshotValidForDate(Long entityId, LocalDate date) {
        return repository.isSnapshotValidForDate(getSnapshotType(), entityId, date);
    }

    /**
     * Get earliest invalidation date for entity.
     * <p>
     * Returns empty if all snapshots are valid. This is useful to determine from which date
     * snapshots need to be recalculated.
     *
     * @param entityId the ID of the entity
     * @return optional invalidation date, empty if all snapshots are valid
     */
    @Transactional(readOnly = true)
    public Optional<LocalDate> getInvalidFromDate(Long entityId) {
        return repository.getInvalidFromDate(getSnapshotType(), entityId);
    }

    /**
     * Mark validity record as recalculating.
     * <p>
     * Prevents concurrent recalculation of same snapshots. Should be called before starting
     * the recalculation process.
     *
     * @param validity the validity record to mark
     */
    public void markAsRecalculating(SnapshotValidity validity) {
        validity.setStatus(ValidityStatus.RECALCULATING);
        repository.save(validity);

        log.info("Marked {} snapshots for entity {} as RECALCULATING",
                validity.getSnapshotType(), validity.getEntityId());
    }

    /**
     * Delete validity record after successful recalculation.
     * <p>
     * Absence of record means all snapshots are valid. This should be called after
     * snapshots have been successfully recalculated.
     *
     * @param validity the validity record to delete
     */
    public void deleteValidity(SnapshotValidity validity) {
        repository.delete(validity);

        log.info("Deleted validity record for {} entity {} - snapshots are now valid",
                validity.getSnapshotType(), validity.getEntityId());
    }

    /**
     * Get all INVALID records for this snapshot type.
     * <p>
     * Used by scheduled jobs for batch recalculation. Returns only records with INVALID status,
     * excluding RECALCULATING records to avoid processing snapshots that are already being
     * recalculated.
     *
     * @return list of INVALID validity records
     */
    @Transactional(readOnly = true)
    public List<SnapshotValidity> getAllInvalidRecords() {
        return repository.findBySnapshotTypeAndStatus(getSnapshotType(), ValidityStatus.INVALID);
    }
}
