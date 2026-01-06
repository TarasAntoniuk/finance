package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BankAccountBalanceService {

    private final BankAccountBalanceSnapshotRepository balanceSnapshotRepository;
    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final BankAccountTransactionEventRepository transactionEventRepository;

    public BankAccountBalanceService(
            BankAccountBalanceSnapshotRepository balanceSnapshotRepository,
            BankAccountRepository bankAccountRepository,
            OrganizationRepository organizationRepository,
            BankAccountTransactionEventRepository transactionEventRepository) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.organizationRepository = organizationRepository;
        this.transactionEventRepository = transactionEventRepository;
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
        // Try to find latest snapshot before the datetime
        var snapshotOpt = balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(
                bankAccountId, atDateTime);

        BigDecimal balance;
        LocalDateTime startDateTime;

        if (snapshotOpt.isPresent()) {
            // Start from snapshot
            BankAccountBalanceSnapshot snapshot = snapshotOpt.get();
            balance = snapshot.getClosingBalance();
            // FIX: Start AFTER snapshot datetime to avoid duplicating events already included in snapshot
            startDateTime = snapshot.getSnapshotDateTime().plusNanos(1);
        } else {
            // No snapshot, start from zero
            balance = BigDecimal.ZERO;
            startDateTime = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        }

        // Get all events after snapshot up to (but not including) the specified datetime
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(bankAccountId, startDateTime, atDateTime.minusNanos(1));

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
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(
                        bankAccountId,
                        startOfDay,
                        endOfDay.minusNanos(1));

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

        return balanceSnapshotRepository.save(snapshot);
    }

    /**
     * Get balance snapshot for specific datetime
     */
    @Transactional(readOnly = true)
    public BankAccountBalanceSnapshot getSnapshot(Long bankAccountId, LocalDateTime snapshotDateTime) {
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
        return balanceSnapshotRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                bankAccountId, startDateTime, endDateTime);
    }
}
