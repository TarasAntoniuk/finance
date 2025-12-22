package com.tarasantoniuk.finance.banking.bankaccounttransaction.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BankAccountTransactionService {

    private final BankAccountTransactionEventRepository transactionEventRepository;
    private final BankAccountBalanceSnapshotRepository balanceSnapshotRepository;
    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrencyRepository currencyRepository;

    public BankAccountTransactionService(
            BankAccountTransactionEventRepository transactionEventRepository,
            BankAccountBalanceSnapshotRepository balanceSnapshotRepository,
            BankAccountRepository bankAccountRepository,
            OrganizationRepository organizationRepository,
            CurrencyRepository currencyRepository) {
        this.transactionEventRepository = transactionEventRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.organizationRepository = organizationRepository;
        this.currencyRepository = currencyRepository;
    }

    /**
     * Create transaction event for bank receipt (money in)
     */
    public BankAccountTransactionEvent createReceiptEvent(
            Long bankAccountId,
            Long organizationId,
            Long currencyId,
            LocalDateTime transactionDateTime,
            BigDecimal amount,
            String documentType,
            Long documentId,
            String description) {

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + currencyId));

        BankAccountTransactionEvent event = new BankAccountTransactionEvent();
        event.setBankAccount(bankAccount);
        event.setOrganization(organization);
        event.setCurrency(currency);
        event.setTransactionDateTime(transactionDateTime);
        event.setTransactionType(TransactionType.DEBIT);
        event.setAmount(amount);
        event.setDocumentType(documentType);
        event.setDocumentId(documentId);
        event.setDescription(description);

        // Calculate balance after transaction
        BigDecimal currentBalance = calculateBalance(bankAccountId, transactionDateTime);
        BigDecimal balanceAfter = currentBalance.add(amount);
        event.setBalanceAfter(balanceAfter);

        return transactionEventRepository.save(event);
    }

    /**
     * Create transaction event for bank payment (money out)
     */
    public BankAccountTransactionEvent createPaymentEvent(
            Long bankAccountId,
            Long organizationId,
            Long currencyId,
            LocalDateTime transactionDateTime,
            BigDecimal amount,
            String documentType,
            Long documentId,
            String description) {

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + currencyId));

        BankAccountTransactionEvent event = new BankAccountTransactionEvent();
        event.setBankAccount(bankAccount);
        event.setOrganization(organization);
        event.setCurrency(currency);
        event.setTransactionDateTime(transactionDateTime);
        event.setTransactionType(TransactionType.CREDIT);
        event.setAmount(amount);
        event.setDocumentType(documentType);
        event.setDocumentId(documentId);
        event.setDescription(description);

        // Calculate balance after transaction
        BigDecimal currentBalance = calculateBalance(bankAccountId, transactionDateTime);
        BigDecimal balanceAfter = currentBalance.subtract(amount);
        event.setBalanceAfter(balanceAfter);

        return transactionEventRepository.save(event);
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
            startDateTime = snapshot.getSnapshotDateTime();
        } else {
            // No snapshot, start from zero
            balance = BigDecimal.ZERO;
            startDateTime = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        }

        // Get all events after snapshot up to (but not including) the specified datetime
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(bankAccountId, startDateTime, atDateTime.minusNanos(1));

        // Apply events to balance
        for (BankAccountTransactionEvent event : events) {
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
     * Get all events for bank account
     */
    @Transactional(readOnly = true)
    public List<BankAccountTransactionEvent> getAccountEvents(Long bankAccountId) {
        return transactionEventRepository.findByBankAccountIdWithRelations(bankAccountId);
    }

    /**
     * Get events for bank account within datetime range
     */
    @Transactional(readOnly = true)
    public List<BankAccountTransactionEvent> getAccountEventsInDateTimeRange(
            Long bankAccountId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                bankAccountId, startDateTime, endDateTime);
    }

    /**
     * Find event by document
     */
    @Transactional(readOnly = true)
    public BankAccountTransactionEvent findByDocument(String documentType, Long documentId) {
        return transactionEventRepository.findByDocumentTypeAndDocumentIdWithRelations(documentType, documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction event not found for document: " + documentType + " #" + documentId));
    }

    /**
     * Find active (non-reversed) transaction event for document
     * Returns only the most recent event that is not marked as reversed
     */
    @Transactional(readOnly = true)
    public BankAccountTransactionEvent findActiveByDocument(String documentType, Long documentId) {
        return transactionEventRepository.findActiveByDocumentTypeAndDocumentId(documentType, documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active transaction event not found for document: " + documentType + " #" + documentId));
    }

    /**
     * Check if event exists for document (only non-reversed)
     */
    @Transactional(readOnly = true)
    public boolean existsByDocument(String documentType, Long documentId) {
        return transactionEventRepository.existsByDocumentTypeAndDocumentIdAndIsReversedFalse(documentType, documentId);
    }

    /**
     * Reverse transaction and mark it as reversed.
     * Establishes bidirectional links between the original event and its reversal event
     * to maintain a complete audit trail of transaction reversals.
     */
    public void reverseTransaction(Long eventId, Long reversalEventId) {
        BankAccountTransactionEvent originalEvent = transactionEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction event not found with id: " + eventId));

        BankAccountTransactionEvent reversalEvent = transactionEventRepository.findById(reversalEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Reversal event not found with id: " + reversalEventId));

        // Mark original event as reversed
        originalEvent.setIsReversed(true);
        originalEvent.setReversedByEventId(reversalEventId);

        // Reversal event also knows about original event (for tracking)
        reversalEvent.setReversedByEventId(eventId);

        transactionEventRepository.saveAll(List.of(originalEvent, reversalEvent));
    }

    /**
     * Create balance snapshot for specific point in time.
     * This is used for optimization - to avoid recalculating from all events.
     */
    public BankAccountBalanceSnapshot createSnapshot(Long bankAccountId, LocalDateTime snapshotDateTime) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        // Check if snapshot already exists
        if (balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(bankAccountId, snapshotDateTime)) {
            throw new IllegalStateException("Snapshot already exists for datetime: " + snapshotDateTime);
        }

        // Get organization from BankAccount holder
        Organization organization = organizationRepository.findById(bankAccount.getHolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + bankAccount.getHolderId()));

        // Calculate opening balance (balance before first event of the period)
        BigDecimal openingBalance = calculateBalance(bankAccountId, snapshotDateTime.toLocalDate().atStartOfDay());

        // Get events for this period (from start of day to snapshot datetime)
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateTimeRangeWithRelations(
                        bankAccountId, 
                        snapshotDateTime.toLocalDate().atStartOfDay(), 
                        snapshotDateTime);

        // Calculate turnovers
        BigDecimal debitTurnover = BigDecimal.ZERO;
        BigDecimal creditTurnover = BigDecimal.ZERO;
        Long lastEventId = null;

        for (BankAccountTransactionEvent event : events) {
            if (event.getTransactionType() == TransactionType.DEBIT) {
                debitTurnover = debitTurnover.add(event.getAmount());
            } else {
                creditTurnover = creditTurnover.add(event.getAmount());
            }
            lastEventId = event.getId();
        }

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance.add(debitTurnover).subtract(creditTurnover);

        // Create snapshot
        BankAccountBalanceSnapshot snapshot = new BankAccountBalanceSnapshot();
        snapshot.setBankAccount(bankAccount);
        snapshot.setOrganization(organization);
        snapshot.setCurrency(bankAccount.getCurrency());
        snapshot.setSnapshotDateTime(snapshotDateTime);
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
