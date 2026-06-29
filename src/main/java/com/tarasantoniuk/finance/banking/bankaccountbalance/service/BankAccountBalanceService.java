package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BankAccountBalanceService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountBalanceService.class);

    private final BankAccountBalanceSnapshotRepository balanceSnapshotRepository;
    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final BankAccountTransactionEventRepository transactionEventRepository;
    private final BankAccountSnapshotValidityService validityService;
    private final OrganizationSecurityContext orgContext;

    public BankAccountBalanceService(
            BankAccountBalanceSnapshotRepository balanceSnapshotRepository,
            BankAccountRepository bankAccountRepository,
            OrganizationRepository organizationRepository,
            BankAccountTransactionEventRepository transactionEventRepository,
            BankAccountSnapshotValidityService validityService,
            OrganizationSecurityContext orgContext) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.organizationRepository = organizationRepository;
        this.transactionEventRepository = transactionEventRepository;
        this.validityService = validityService;
        this.orgContext = orgContext;
    }

    /**
     * Defense-in-depth: if the call is running under an authenticated HTTP request
     * and the account belongs to an organization, verify the caller may access it.
     * Internal / scheduler callers (no principal) pass through.
     */
    private void ensureAccess(Long bankAccountId) {
        if (!orgContext.hasAuthenticatedPrincipal()) {
            return;
        }
        BankAccount account = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + bankAccountId));
        if (account.getHolderType() == AccountHolderType.ORGANIZATION) {
            orgContext.validateAccess(account.getHolderId());
        }
    }

    /**
     * Calculate balance for bank account at specific point in time.
     * Uses snapshot optimization: finds latest snapshot and adds events after it.
     *
     * @param bankAccountId bank account ID
     * @param atDateTime    point in time to calculate balance (exclusive - balance BEFORE this moment)
     * @return balance at the specified point in time
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(Long bankAccountId, LocalDateTime atDateTime) {
        ensureAccess(bankAccountId);
        // Check if there are invalid snapshots
        Optional<LocalDate> invalidFromDateOpt = validityService.getInvalidFromDate(bankAccountId);

        LocalDateTime snapshotSearchLimit = atDateTime;

        if (invalidFromDateOpt.isPresent()) {
            LocalDate invalidFromDate = invalidFromDateOpt.get();
            LocalDateTime invalidFromDateTime = invalidFromDate.atStartOfDay();

            // If invalidation date is before our target datetime,
            // we can only use snapshots before invalidation date
            if (invalidFromDateTime.isBefore(atDateTime)) {
                snapshotSearchLimit = invalidFromDateTime;
                log.debug("Snapshots are invalid from {}, limiting snapshot search to this date",
                        invalidFromDateTime);
            }
        }

        // Try to find latest valid snapshot before the datetime
        var snapshotOpt = balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(
                bankAccountId, snapshotSearchLimit);

        BigDecimal balance;
        LocalDateTime startDateTime;

        if (snapshotOpt.isPresent()) {
            // Start from snapshot
            BankAccountBalanceSnapshot snapshot = snapshotOpt.get();
            balance = snapshot.getClosingBalance();
            // FIX: Start AFTER snapshot datetime to avoid duplicating events already included in snapshot
            startDateTime = snapshot.getSnapshotDateTime().plusNanos(1);
            log.debug("Using snapshot from {} with balance {}, calculating from {}",
                    snapshot.getSnapshotDateTime(), balance, startDateTime);
        } else {
            // No snapshot, start from zero
            balance = BigDecimal.ZERO;
            startDateTime = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
            log.debug("No valid snapshot found, calculating from scratch starting at {}", startDateTime);
        }

        // Get all events after snapshot up to (but not including) the specified datetime
        // Query uses "< endDateTime" so we don't need to subtract anything - it's already exclusive
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(bankAccountId, startDateTime, atDateTime);

        log.debug("Found {} events between {} and {}", events.size(), startDateTime, atDateTime);

        // Apply events to balance
        // Exclude reversal events (BankReceiptReversal, BankPaymentReversal)
        // These are technical records that cancel other transactions, not real business transactions
        for (BankAccountTransactionEvent event : events) {
            // Skip reversal events
            if (event.getDocumentType() != null && event.getDocumentType().endsWith("Reversal")) {
                continue;
            }

            if (event.getTransactionType() == TransactionType.DEBIT) {
                balance = balance.add(event.getAmount());
            } else {
                balance = balance.subtract(event.getAmount());
            }
        }

        return balance;
    }

    /**
     * Get current balance for bank account (as of now)
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long bankAccountId) {
        return calculateBalance(bankAccountId, LocalDateTime.now());
    }

    /**
     * Create balance snapshot for end of day.
     * Snapshot is always created for the start of the next day (which represents end of current day).
     * This is used for optimization - to avoid recalculating from all events.
     *
     * @param bankAccountId bank account ID
     * @param snapshotDateTime datetime for snapshot (will be normalized to start of next day)
     * @return created snapshot
     */
    public BankAccountBalanceSnapshot createSnapshot(Long bankAccountId, LocalDateTime snapshotDateTime) {
        ensureAccess(bankAccountId);
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        // Normalize to start of next day (represents end of current day)
        LocalDate snapshotDate = snapshotDateTime.toLocalDate();
        LocalDateTime endOfDay = snapshotDate.plusDays(1).atStartOfDay();

        // Check if snapshot already exists for this day
        if (balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(bankAccountId, endOfDay)) {
            throw new IllegalStateException("Snapshot already exists for date: " + snapshotDate);
        }

        // Get organization from BankAccount holder
        Organization organization = organizationRepository.findById(bankAccount.getHolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + bankAccount.getHolderId()));

        // Calculate opening balance (balance at start of this day)
        LocalDateTime startOfDay = snapshotDate.atStartOfDay();
        BigDecimal openingBalance = calculateBalance(bankAccountId, startOfDay);

        // Get events for this day (from start to end of day - exclusive end)
        // Query uses < endDateTime so endOfDay (start of next day) is the exclusive boundary
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(
                        bankAccountId,
                        startOfDay,
                        endOfDay);

        // Calculate turnovers
        // Exclude reversal events (BankReceiptReversal, BankPaymentReversal)
        // These are technical records that cancel other transactions, not real business transactions
        BigDecimal debitTurnover = BigDecimal.ZERO;
        BigDecimal creditTurnover = BigDecimal.ZERO;
        Long lastEventId = null;

        for (BankAccountTransactionEvent event : events) {
            // Skip reversal events
            if (event.getDocumentType() != null && event.getDocumentType().endsWith("Reversal")) {
                continue;
            }

            if (event.getTransactionType() == TransactionType.DEBIT) {
                debitTurnover = debitTurnover.add(event.getAmount());
            } else {
                creditTurnover = creditTurnover.add(event.getAmount());
            }
            lastEventId = event.getId();
        }

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance.add(debitTurnover).subtract(creditTurnover);

        // Create snapshot with normalized datetime (start of next day)
        BankAccountBalanceSnapshot snapshot = new BankAccountBalanceSnapshot();
        snapshot.setBankAccount(bankAccount);
        snapshot.setOrganization(organization);
        snapshot.setCurrency(bankAccount.getCurrency());
        snapshot.setSnapshotDateTime(endOfDay);  // Start of next day
        snapshot.setOpeningBalance(openingBalance);
        snapshot.setDebitTurnover(debitTurnover);
        snapshot.setCreditTurnover(creditTurnover);
        snapshot.setClosingBalance(closingBalance);
        snapshot.setLastEventId(lastEventId);
        snapshot.setEventsCount(events.size());

        log.info("Created snapshot for account {} on date {}: opening={}, debit={}, credit={}, closing={}",
                bankAccountId, snapshotDate, openingBalance, debitTurnover, creditTurnover, closingBalance);

        return balanceSnapshotRepository.save(snapshot);
    }

    /**
     * Get balance snapshot for specific datetime
     */
    @Transactional(readOnly = true)
    public BankAccountBalanceSnapshot getSnapshot(Long bankAccountId, LocalDateTime snapshotDateTime) {
        ensureAccess(bankAccountId);
        return balanceSnapshotRepository.findByBankAccountIdAndSnapshotDateTimeWithRelations(bankAccountId, snapshotDateTime)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Snapshot not found for account " + bankAccountId + " at datetime " + snapshotDateTime));
    }

    /**
     * Get all snapshots for bank account within datetime range
     */
    @Transactional(readOnly = true)
    public List<BankAccountBalanceSnapshot> getSnapshotsInDateTimeRange(
            Long bankAccountId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ensureAccess(bankAccountId);
        return balanceSnapshotRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                bankAccountId, startDateTime, endDateTime);
    }

    /**
     * Delete snapshots starting from given datetime.
     * Used during recalculation of invalid snapshots.
     *
     * @param bankAccountId bank account ID
     * @param fromDateTime  datetime from which to delete snapshots (inclusive)
     */
    public void deleteSnapshotsFrom(Long bankAccountId, LocalDateTime fromDateTime) {
        ensureAccess(bankAccountId);
        int deletedCount = balanceSnapshotRepository
                .deleteByBankAccountIdAndSnapshotDateTimeGreaterThanEqual(bankAccountId, fromDateTime);

        log.info("Deleted {} invalid snapshots for account {} from date {}",
                deletedCount, bankAccountId, fromDateTime);
    }
}
