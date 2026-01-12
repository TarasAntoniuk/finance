package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.common.snapshot.repository.SnapshotValidityRepository;
import com.tarasantoniuk.finance.common.snapshot.service.AbstractSnapshotValidityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing snapshot validity for bank account balances.
 * <p>
 * This service extends the common {@link AbstractSnapshotValidityService} with bank account
 * specific snapshot type. It provides all standard validity lifecycle operations:
 * <ul>
 *     <li>Invalidating snapshots when backdated transactions are created</li>
 *     <li>Checking if snapshots are valid for a given date</li>
 *     <li>Managing recalculation status</li>
 *     <li>Retrieving invalid records for batch processing</li>
 * </ul>
 * <p>
 * Used by:
 * <ul>
 *     <li>{@code BankAccountBalanceService} - for validity checks before using cached snapshots</li>
 *     <li>{@code BankAccountSnapshotScheduler} - for finding and recalculating invalid snapshots</li>
 *     <li>{@code BankAccountTransactionService} - for invalidating snapshots on backdated transactions</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * // Invalidate snapshots when backdated transaction is posted
 * validityService.invalidateSnapshots(bankAccountId, transactionDateTime, eventId);
 *
 * // Check if snapshot is valid before using it
 * boolean isValid = validityService.isSnapshotValidForDate(bankAccountId, date);
 * if (!isValid) {
 *     // Recalculate balance from events instead of using snapshot
 * }
 *
 * // Get all invalid records for batch recalculation
 * List<SnapshotValidity> invalidRecords = validityService.getAllInvalidRecords();
 * }
 * </pre>
 */
@Service
@Transactional
public class BankAccountSnapshotValidityService extends AbstractSnapshotValidityService {

    private static final String SNAPSHOT_TYPE = "BANK_ACCOUNT_BALANCE";

    public BankAccountSnapshotValidityService(SnapshotValidityRepository repository) {
        super(repository);
    }

    /**
     * Returns "BANK_ACCOUNT_BALANCE" as the snapshot type for this service.
     * <p>
     * This identifier is used to distinguish bank account balance snapshots from
     * other types of snapshots in the system (e.g., product balances, settlement balances).
     *
     * @return the snapshot type identifier
     */
    @Override
    protected String getSnapshotType() {
        return SNAPSHOT_TYPE;
    }
}
