package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
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
 *     <li>Deletes invalid snapshots and recreates them</li>
 *     <li>Removes validity record on successful completion</li>
 * </ul>
 * <p>
 * Each account is processed in its own transaction, so a failure for one account is logged and
 * leaves the other accounts' snapshots committed. A failed recalculation keeps its validity record
 * INVALID and is retried on the next run.
 * <p>
 * Manual recalculation trigger is also available via {@link #triggerRecalculationForAccount(Long)}.
 */
@Service
public class BankAccountSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(BankAccountSnapshotScheduler.class);

    private final BankAccountBalanceService balanceService;
    private final BankAccountSnapshotValidityService validityService;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionTemplate perAccountTransaction;

    public BankAccountSnapshotScheduler(
            BankAccountBalanceService balanceService,
            BankAccountSnapshotValidityService validityService,
            BankAccountRepository bankAccountRepository,
            PlatformTransactionManager transactionManager) {
        this.balanceService = balanceService;
        this.validityService = validityService;
        this.bankAccountRepository = bankAccountRepository;
        this.perAccountTransaction = new TransactionTemplate(transactionManager);
    }

    /**
     * Create yesterday's snapshot for every active bank account. Runs at 00:30.
     * Accounts with invalid snapshots are left to the recalculation job at 01:00.
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void createDailySnapshots() {
        log.info("Starting daily bank account snapshot creation job");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Long> activeAccountIds = bankAccountRepository.findAllActiveOrganizationAccountIds();
        log.info("Found {} active bank accounts", activeAccountIds.size());

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        for (Long accountId : activeAccountIds) {
            if (!validityService.isSnapshotValidForDate(accountId, yesterday)
                    || balanceService.hasSnapshotForDay(accountId, yesterday)) {
                skipCount++;
                continue;
            }
            try {
                balanceService.createSnapshot(accountId, yesterday.atStartOfDay());
                successCount++;
            } catch (RuntimeException e) {
                log.error("Failed to create daily snapshot for account {} on date {}", accountId, yesterday, e);
                errorCount++;
            }
        }

        log.info("Daily snapshot creation completed: success={}, skipped={}, errors={}",
                successCount, skipCount, errorCount);
    }

    /**
     * Rebuild invalidated snapshots for every account that has them. Runs at 01:00.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void recalculateInvalidSnapshots() {
        log.info("Starting invalid bank account snapshots recalculation job");

        List<SnapshotValidity> invalidRecords = validityService.getAllInvalidRecords();
        log.info("Found {} accounts with invalid snapshots", invalidRecords.size());

        int successCount = 0;
        int errorCount = 0;

        for (SnapshotValidity validity : invalidRecords) {
            try {
                recalculateInOwnTransaction(validity);
                successCount++;
            } catch (RuntimeException e) {
                log.error("Failed to recalculate snapshots for account {} from date {}",
                        validity.getEntityId(), validity.getInvalidFromDate(), e);
                errorCount++;
            }
        }

        log.info("Invalid snapshots recalculation completed: success={}, errors={}",
                successCount, errorCount);
    }

    /**
     * Manually trigger recalculation of invalid snapshots for one bank account.
     */
    public void triggerRecalculationForAccount(Long bankAccountId) {
        log.info("Manual recalculation triggered for account {}", bankAccountId);

        validityService.getAllInvalidRecords().stream()
                .filter(validity -> validity.getEntityId().equals(bankAccountId))
                .findFirst()
                .ifPresentOrElse(
                        this::recalculateInOwnTransaction,
                        () -> log.info("No invalid snapshots found for account {}", bankAccountId));
    }

    private void recalculateInOwnTransaction(SnapshotValidity validity) {
        perAccountTransaction.executeWithoutResult(status -> {
            validityService.markAsRecalculating(validity);
            recalculateSnapshotsForAccount(validity);
        });
    }

    /**
     * The validity record marks snapshots stored at {@code invalidFromDate} 00:00 and later as stale.
     * The first of them is the snapshot of the day before, so rebuilding starts there.
     */
    private void recalculateSnapshotsForAccount(SnapshotValidity validity) {
        Long bankAccountId = validity.getEntityId();
        LocalDate firstStaleDay = validity.getInvalidFromDate().minusDays(1);
        LocalDate lastClosedDay = LocalDate.now().minusDays(1);

        log.info("Recalculating snapshots for account {} from {} to {}", bankAccountId, firstStaleDay, lastClosedDay);

        balanceService.deleteSnapshotsFrom(bankAccountId, validity.getInvalidFromDate().atStartOfDay());

        int recalculatedCount = 0;
        for (LocalDate day = firstStaleDay; !day.isAfter(lastClosedDay); day = day.plusDays(1)) {
            balanceService.createSnapshot(bankAccountId, day.atStartOfDay());
            recalculatedCount++;
        }

        log.info("Recalculated {} snapshots for account {}", recalculatedCount, bankAccountId);
        validityService.deleteValidity(validity);
    }
}
