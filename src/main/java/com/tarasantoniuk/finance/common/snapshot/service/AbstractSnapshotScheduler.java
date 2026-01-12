package com.tarasantoniuk.finance.common.snapshot.service;

import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract base class providing scheduling infrastructure for snapshot operations.
 * <p>
 * This class provides template methods for daily snapshot creation and recalculation
 * of invalid snapshots. Subclasses implement module-specific operations via abstract
 * methods and add {@code @Scheduled} annotations on their methods that call the template methods.
 * <p>
 * The scheduler provides:
 * <ul>
 *     <li>Consistent error handling and logging across all modules</li>
 *     <li>Template methods for common snapshot operations</li>
 *     <li>Protection against concurrent recalculation</li>
 *     <li>Detailed statistics and progress tracking</li>
 * </ul>
 * <p>
 * Example subclass implementation:
 * <pre>
 * {@code
 * @Service
 * public class BankAccountSnapshotScheduler extends AbstractSnapshotScheduler {
 *
 *     private final BankAccountRepository bankAccountRepository;
 *     private final BankAccountBalanceService balanceService;
 *     private final BankAccountSnapshotValidityService validityService;
 *
 *     public BankAccountSnapshotScheduler(
 *             BankAccountRepository bankAccountRepository,
 *             BankAccountBalanceService balanceService,
 *             BankAccountSnapshotValidityService validityService) {
 *         this.bankAccountRepository = bankAccountRepository;
 *         this.balanceService = balanceService;
 *         this.validityService = validityService;
 *     }
 *
 *     @Override
 *     protected String getSnapshotType() {
 *         return "BANK_ACCOUNT_BALANCE";
 *     }
 *
 *     @Override
 *     protected List<Long> getActiveEntityIds() {
 *         return bankAccountRepository.findAllActiveIds();
 *     }
 *
 *     @Scheduled(cron = "0 30 0 * * *")
 *     @Transactional
 *     public void createDailySnapshots() {
 *         executeCreateDailySnapshots();
 *     }
 *
 *     @Scheduled(cron = "0 0 2 * * *")
 *     @Transactional
 *     public void recalculateInvalidSnapshots() {
 *         List<SnapshotValidity> invalidRecords = validityService.getAllInvalidRecords();
 *         executeRecalculateInvalidSnapshots(invalidRecords);
 *     }
 * }
 * }
 * </pre>
 */
public abstract class AbstractSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(AbstractSnapshotScheduler.class);

    protected AbstractSnapshotScheduler() {
    }

    /**
     * Returns the snapshot type (e.g., "BANK_ACCOUNT_BALANCE").
     * <p>
     * This identifier is used for logging and tracking purposes. It should match the
     * snapshot type used in the validity service.
     *
     * @return the snapshot type identifier
     */
    protected abstract String getSnapshotType();

    /**
     * Returns IDs of all active entities for snapshot creation.
     * <p>
     * Example implementations:
     * <ul>
     *     <li>Bank accounts: active bank account IDs</li>
     *     <li>Products: active product IDs</li>
     *     <li>Counterparties: active counterparty IDs</li>
     * </ul>
     *
     * @return list of active entity IDs
     */
    protected abstract List<Long> getActiveEntityIds();

    /**
     * Check if snapshot is valid for given entity and date.
     * <p>
     * Uses module-specific validity service to check if snapshots are marked as invalid.
     * Should delegate to the validity service's {@code isSnapshotValidForDate} method.
     *
     * @param entityId the ID of the entity
     * @param date the date to check
     * @return true if snapshot is valid, false if invalid
     */
    protected abstract boolean isSnapshotValidForDate(Long entityId, LocalDate date);

    /**
     * Create snapshot for specific entity.
     * <p>
     * Should delegate to the module-specific balance/snapshot service.
     * Throws {@link IllegalStateException} if snapshot already exists.
     *
     * @param entityId the ID of the entity
     * @param snapshotDateTime the datetime for the snapshot
     * @throws IllegalStateException if snapshot already exists
     */
    protected abstract void createSnapshotForEntity(Long entityId, LocalDateTime snapshotDateTime);

    /**
     * Delete invalid snapshots for entity from given datetime.
     * <p>
     * Should delegate to the module-specific snapshot repository to delete all
     * snapshots created on or after the specified datetime.
     *
     * @param entityId the ID of the entity
     * @param fromDateTime the datetime from which to delete snapshots
     */
    protected abstract void deleteSnapshotsFromForEntity(Long entityId, LocalDateTime fromDateTime);

    /**
     * Mark validity record as recalculating using module-specific service.
     * <p>
     * Should delegate to the validity service's {@code markAsRecalculating} method.
     * This prevents concurrent recalculation of the same snapshots.
     *
     * @param validity the validity record to mark
     */
    protected abstract void markAsRecalculating(SnapshotValidity validity);

    /**
     * Delete validity record using module-specific service.
     * <p>
     * Should delegate to the validity service's {@code deleteValidity} method.
     * Called after successful recalculation to indicate snapshots are now valid.
     *
     * @param validity the validity record to delete
     */
    protected abstract void deleteValidity(SnapshotValidity validity);

    /**
     * Template method for daily snapshot creation.
     * <p>
     * Subclasses call this from their {@code @Scheduled} method. This method:
     * <ol>
     *     <li>Gets all active entities</li>
     *     <li>Checks if snapshots are valid for yesterday</li>
     *     <li>Creates snapshots for valid entities</li>
     *     <li>Skips entities with invalid or existing snapshots</li>
     *     <li>Logs detailed statistics</li>
     * </ol>
     * <p>
     * Handles validation, creation, and error logging consistently across all modules.
     */
    protected void executeCreateDailySnapshots() {
        String snapshotType = getSnapshotType();
        log.info("Starting daily {} snapshot creation job", snapshotType);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime snapshotDateTime = yesterday.atStartOfDay();

        List<Long> activeEntityIds = getActiveEntityIds();
        log.info("Found {} active entities for {}", activeEntityIds.size(), snapshotType);

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        for (Long entityId : activeEntityIds) {
            try {
                // Check if snapshot for this date is valid (not invalidated)
                boolean isValid = isSnapshotValidForDate(entityId, yesterday);

                if (!isValid) {
                    log.debug("Skipping snapshot for {} entity {} on date {} - marked as invalid",
                            snapshotType, entityId, yesterday);
                    skipCount++;
                    continue;
                }

                // Create snapshot
                createSnapshotForEntity(entityId, snapshotDateTime);
                successCount++;

            } catch (IllegalStateException e) {
                // Snapshot already exists - this is OK
                log.debug("Snapshot already exists for {} entity {} on date {}",
                        snapshotType, entityId, yesterday);
                skipCount++;

            } catch (Exception e) {
                log.error("Failed to create daily snapshot for {} entity {} on date {}",
                        snapshotType, entityId, yesterday, e);
                errorCount++;
            }
        }

        log.info("Daily {} snapshot creation completed: success={}, skipped={}, errors={}",
                snapshotType, successCount, skipCount, errorCount);
    }

    /**
     * Template method for recalculating invalid snapshots.
     * <p>
     * Subclasses call this from their {@code @Scheduled} method. This method:
     * <ol>
     *     <li>Marks each invalid record as RECALCULATING</li>
     *     <li>Deletes invalid snapshots</li>
     *     <li>Recreates snapshots from invalid date to yesterday</li>
     *     <li>Deletes validity record on success</li>
     *     <li>Logs detailed statistics and errors</li>
     * </ol>
     * <p>
     * Handles marking as recalculating, recalculation, and error logging consistently
     * across all modules.
     *
     * @param invalidRecords list of validity records with INVALID status
     */
    protected void executeRecalculateInvalidSnapshots(List<SnapshotValidity> invalidRecords) {
        String snapshotType = getSnapshotType();
        log.info("Starting invalid {} snapshots recalculation job", snapshotType);
        log.info("Found {} entities with invalid snapshots", invalidRecords.size());

        int successCount = 0;
        int errorCount = 0;

        for (SnapshotValidity validity : invalidRecords) {
            try {
                // Mark as recalculating to prevent concurrent processing
                markAsRecalculating(validity);

                recalculateSnapshotsForEntity(validity);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to recalculate {} snapshots for entity {} from date {}",
                        snapshotType, validity.getEntityId(), validity.getInvalidFromDate(), e);
                errorCount++;
            }
        }

        log.info("Invalid {} snapshots recalculation completed: success={}, errors={}",
                snapshotType, successCount, errorCount);
    }

    /**
     * Recalculate snapshots for single entity.
     * <p>
     * This method:
     * <ol>
     *     <li>Deletes invalid snapshots from invalidFromDate to yesterday</li>
     *     <li>Recreates snapshots for each day in the range</li>
     *     <li>Deletes validity record on success</li>
     * </ol>
     * <p>
     * If any snapshot creation fails, the exception is re-thrown to mark the
     * recalculation as failed, preserving the validity record for retry.
     *
     * @param validity the validity record to process
     * @throws Exception if snapshot creation fails
     */
    private void recalculateSnapshotsForEntity(SnapshotValidity validity) {
        String snapshotType = getSnapshotType();
        Long entityId = validity.getEntityId();
        LocalDate invalidFromDate = validity.getInvalidFromDate();
        LocalDate today = LocalDate.now().minusDays(1); // Yesterday, as today's snapshot will be created tonight

        log.info("Recalculating {} snapshots for entity {} from {} to {}",
                snapshotType, entityId, invalidFromDate, today);

        // Delete invalid snapshots (from invalidFromDate to today)
        LocalDateTime invalidFromDateTime = invalidFromDate.plusDays(1).atStartOfDay();
        deleteSnapshotsFromForEntity(entityId, invalidFromDateTime);

        // Recalculate snapshots from invalidFromDate to today
        int recalculatedCount = 0;
        LocalDate currentDate = invalidFromDate;

        while (!currentDate.isAfter(today)) {
            try {
                createSnapshotForEntity(entityId, currentDate.atStartOfDay());
                recalculatedCount++;

            } catch (IllegalStateException e) {
                // Snapshot already exists - this should not happen after deletion, but log it
                log.warn("Snapshot already exists for {} entity {} on date {} during recalculation",
                        snapshotType, entityId, currentDate);

            } catch (Exception e) {
                log.error("Failed to create snapshot for {} entity {} on date {} during recalculation",
                        snapshotType, entityId, currentDate, e);
                throw e; // Re-throw to mark this recalculation as failed
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Recalculated {} snapshots for {} entity {}", recalculatedCount, snapshotType, entityId);

        // Delete validity record - all snapshots are now valid
        deleteValidity(validity);
    }
}
