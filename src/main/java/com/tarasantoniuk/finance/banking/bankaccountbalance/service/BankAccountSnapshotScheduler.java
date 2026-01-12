package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for managing automated snapshot operations for bank accounts.
 * <p>
 * This scheduler provides two main operations:
 * <ul>
 *     <li>Daily snapshot creation at 00:30 for all active bank accounts</li>
 *     <li>Invalid snapshot recalculation at 01:00</li>
 * </ul>
 * <p>
 * The scheduler integrates with:
 * <ul>
 *     <li>{@link BankAccountBalanceService} - for snapshot creation and deletion</li>
 *     <li>{@link BankAccountSnapshotValidityService} - for validity tracking and management</li>
 *     <li>{@link BankAccountRepository} - for retrieving active bank accounts</li>
 * </ul>
 * <p>
 * Daily snapshot creation:
 * <ul>
 *     <li>Runs at 00:30 every day</li>
 *     <li>Creates snapshots for previous day (yesterday)</li>
 *     <li>Skips accounts with invalid snapshots (will be recalculated later)</li>
 *     <li>Skips accounts where snapshot already exists</li>
 *     <li>Logs detailed statistics on completion</li>
 * </ul>
 * <p>
 * Invalid snapshot recalculation:
 * <ul>
 *     <li>Runs at 01:00 every day (30 minutes after snapshot creation)</li>
 *     <li>Finds all accounts with invalid snapshots</li>
 *     <li>Marks them as RECALCULATING to prevent concurrent processing</li>
 *     <li>Deletes invalid snapshots and recreates them</li>
 *     <li>Removes validity record on successful completion</li>
 * </ul>
 * <p>
 * Manual recalculation trigger is also available via {@link #triggerRecalculationForAccount(Long)}.
 */
@Service
public class BankAccountSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(BankAccountSnapshotScheduler.class);

    private final BankAccountBalanceService balanceService;
    private final BankAccountSnapshotValidityService validityService;
    private final BankAccountRepository bankAccountRepository;

    public BankAccountSnapshotScheduler(
            BankAccountBalanceService balanceService,
            BankAccountSnapshotValidityService validityService,
            BankAccountRepository bankAccountRepository) {
        this.balanceService = balanceService;
        this.validityService = validityService;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Create daily snapshots for all active bank accounts.
     * <p>
     * Runs every day at 00:30 (30 minutes after midnight). Creates snapshots for the previous
     * day (yesterday) for all active bank accounts. Skips accounts with invalid snapshots,
     * as they will be recalculated by the recalculation job at 01:00.
     * <p>
     * Statistics are logged on completion showing the number of successful creations,
     * skipped accounts, and errors.
     */
    @Scheduled(cron = "0 30 0 * * *") // 00:30 daily
    @Transactional
    public void createDailySnapshots() {
        log.info("Starting daily bank account snapshot creation job");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime snapshotDateTime = yesterday.atStartOfDay();

        List<Long> activeAccountIds = bankAccountRepository.findAllActiveAccountIds();
        log.info("Found {} active bank accounts", activeAccountIds.size());

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        for (Long accountId : activeAccountIds) {
            try {
                // Check if snapshot for this date is valid (not invalidated)
                boolean isValid = validityService.isSnapshotValidForDate(accountId, yesterday);

                if (!isValid) {
                    log.debug("Skipping snapshot for account {} on date {} - marked as invalid",
                            accountId, yesterday);
                    skipCount++;
                    continue;
                }

                // Create snapshot
                balanceService.createSnapshot(accountId, snapshotDateTime);
                successCount++;

            } catch (IllegalStateException e) {
                // Snapshot already exists - this is OK
                log.debug("Snapshot already exists for account {} on date {}", accountId, yesterday);
                skipCount++;

            } catch (Exception e) {
                log.error("Failed to create daily snapshot for account {} on date {}",
                        accountId, yesterday, e);
                errorCount++;
            }
        }

        log.info("Daily snapshot creation completed: success={}, skipped={}, errors={}",
                successCount, skipCount, errorCount);
    }

    /**
     * Recalculate invalid snapshots for all accounts.
     * <p>
     * Runs every day at 01:00 (1 hour after midnight, 30 minutes after daily snapshot creation).
     * Retrieves all accounts with invalid snapshots, marks them as RECALCULATING to prevent
     * concurrent processing, then recalculates all invalid snapshots from the invalidation
     * date to yesterday.
     * <p>
     * On successful recalculation, the validity record is deleted, indicating that all
     * snapshots are now valid.
     */
    @Scheduled(cron = "0 0 1 * * *") // 01:00 daily
    @Transactional
    public void recalculateInvalidSnapshots() {
        log.info("Starting invalid bank account snapshots recalculation job");

        List<SnapshotValidity> invalidRecords = validityService.getAllInvalidRecords();
        log.info("Found {} accounts with invalid snapshots", invalidRecords.size());

        int successCount = 0;
        int errorCount = 0;

        for (SnapshotValidity validity : invalidRecords) {
            try {
                // Mark as recalculating to prevent concurrent processing
                validityService.markAsRecalculating(validity);

                recalculateSnapshotsForAccount(validity);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to recalculate snapshots for account {} from date {}",
                        validity.getEntityId(), validity.getInvalidFromDate(), e);
                errorCount++;
            }
        }

        log.info("Invalid snapshots recalculation completed: success={}, errors={}",
                successCount, errorCount);
    }

    /**
     * Recalculate snapshots for single account.
     * <p>
     * Deletes all invalid snapshots from the invalidation date to yesterday, then recreates
     * them day by day. On successful completion, the validity record is deleted to indicate
     * that all snapshots are now valid.
     * <p>
     * If any snapshot creation fails during recalculation, the exception is re-thrown to
     * mark the recalculation as failed, preserving the validity record for retry.
     *
     * @param validity the validity record containing account ID and invalidation date
     * @throws Exception if snapshot creation fails during recalculation
     */
    private void recalculateSnapshotsForAccount(SnapshotValidity validity) throws Exception {
        Long bankAccountId = validity.getEntityId();
        LocalDate invalidFromDate = validity.getInvalidFromDate();
        LocalDate today = LocalDate.now().minusDays(1); // Yesterday, as today's snapshot will be created tonight

        log.info("Recalculating snapshots for account {} from {} to {}",
                bankAccountId, invalidFromDate, today);

        // Delete invalid snapshots (from invalidFromDate to today, inclusive)
        LocalDateTime invalidFromDateTime = invalidFromDate.atStartOfDay();
        balanceService.deleteSnapshotsFrom(bankAccountId, invalidFromDateTime);

        // Recalculate snapshots from invalidFromDate to today
        int recalculatedCount = 0;
        LocalDate currentDate = invalidFromDate;

        while (!currentDate.isAfter(today)) {
            try {
                balanceService.createSnapshot(bankAccountId, currentDate.atStartOfDay());
                recalculatedCount++;

            } catch (IllegalStateException e) {
                // Snapshot already exists - this should not happen after deletion, but log it
                log.warn("Snapshot already exists for account {} on date {} during recalculation",
                        bankAccountId, currentDate);

            } catch (Exception e) {
                log.error("Failed to create snapshot for account {} on date {} during recalculation",
                        bankAccountId, currentDate, e);
                throw e; // Re-throw to mark this recalculation as failed
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Recalculated {} snapshots for account {}", recalculatedCount, bankAccountId);

        // Delete validity record - all snapshots are now valid
        validityService.deleteValidity(validity);
    }

    /**
     * Manually trigger recalculation for specific account.
     * <p>
     * Can be called via API or admin panel to manually trigger recalculation for a specific
     * bank account. If no invalid snapshots exist for the account, the method logs this
     * and returns without performing any action.
     * <p>
     * This is useful for:
     * <ul>
     *     <li>Immediate recalculation without waiting for scheduled job</li>
     *     <li>Retry after failed automatic recalculation</li>
     *     <li>Manual intervention when needed</li>
     * </ul>
     *
     * @param bankAccountId the ID of the bank account to recalculate
     */
    @Transactional
    public void triggerRecalculationForAccount(Long bankAccountId) {
        log.info("Manual recalculation triggered for account {}", bankAccountId);

        List<SnapshotValidity> invalidRecords = validityService.getAllInvalidRecords();

        SnapshotValidity validity = invalidRecords.stream()
                .filter(v -> v.getEntityId().equals(bankAccountId))
                .findFirst()
                .orElse(null);

        if (validity == null) {
            log.info("No invalid snapshots found for account {}", bankAccountId);
            return;
        }

        try {
            validityService.markAsRecalculating(validity);
            recalculateSnapshotsForAccount(validity);
            log.info("Manual recalculation completed for account {}", bankAccountId);
        } catch (Exception e) {
            log.error("Failed to manually recalculate snapshots for account {}", bankAccountId, e);
            throw new RuntimeException("Failed to recalculate snapshots for account " + bankAccountId, e);
        }
    }
}
