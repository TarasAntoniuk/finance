package com.tarasantoniuk.finance.banking.bankaccounttransaction.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            LocalDate transactionDate,
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
        event.setTransactionDate(transactionDate);
        event.setTransactionType(TransactionType.DEBIT);
        event.setAmount(amount);
        event.setDocumentType(documentType);
        event.setDocumentId(documentId);
        event.setDescription(description);

//        event.setDocumentDate(transactionDate);      // Заповнюємо NOT NULL поле з BaseDocument
//        event.setStatus(DocumentStatus.POSTED);

        // Calculate balance after transaction
        BigDecimal currentBalance = calculateBalance(bankAccountId, transactionDate);
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
            LocalDate transactionDate,
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
        event.setTransactionDate(transactionDate);
        event.setTransactionType(TransactionType.CREDIT);
        event.setAmount(amount);
        event.setDocumentType(documentType);
        event.setDocumentId(documentId);
        event.setDescription(description);

//        event.setDocumentDate(transactionDate);      // Заповнюємо NOT NULL поле з BaseDocument
//        event.setStatus(DocumentStatus.POSTED);

        // Calculate balance after transaction
        BigDecimal currentBalance = calculateBalance(bankAccountId, transactionDate);
        BigDecimal balanceAfter = currentBalance.subtract(amount);
        event.setBalanceAfter(balanceAfter);

        return transactionEventRepository.save(event);
    }

    /**
     * Calculate current balance for bank account on specific date
     * Uses snapshot optimization: finds latest snapshot and adds events after it
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(Long bankAccountId, LocalDate onDate) {
        // Try to find latest snapshot before or on the date
        var snapshotOpt = balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateWithRelations(
                bankAccountId, onDate);

        BigDecimal balance;
        LocalDate startDate;

        if (snapshotOpt.isPresent()) {
            // Start from snapshot
            BankAccountBalanceSnapshot snapshot = snapshotOpt.get();
            balance = snapshot.getClosingBalance();
            startDate = snapshot.getSnapshotDate().plusDays(1);
        } else {
            // No snapshot, start from zero
            balance = BigDecimal.ZERO;
            startDate = LocalDate.of(1900, 1, 1); // Far past
        }

        // Get all events after snapshot up to the date
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateRangeWithRelations(bankAccountId, startDate, onDate);

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
     * Get current balance for bank account (as of today)
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long bankAccountId) {
        return calculateBalance(bankAccountId, LocalDate.now());
    }

    /**
     * Get all events for bank account
     */
    @Transactional(readOnly = true)
    public List<BankAccountTransactionEvent> getAccountEvents(Long bankAccountId) {
        return transactionEventRepository.findByBankAccountIdWithRelations(bankAccountId);
    }

    /**
     * Get events for bank account within date range
     */
    @Transactional(readOnly = true)
    public List<BankAccountTransactionEvent> getAccountEventsInDateRange(
            Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        return transactionEventRepository.findByBankAccountIdAndDateRangeWithRelations(
                bankAccountId, startDate, endDate);
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
            * Check if event exists for document (only non-reversed)
    */
    @Transactional(readOnly = true)
    public boolean existsByDocument(String documentType, Long documentId) {
        return transactionEventRepository.existsByDocumentTypeAndDocumentIdAndIsReversedFalse(documentType, documentId);
    }

    /**
     * Reverse transaction (mark as reversed)
     */
    public void reverseTransaction(Long eventId, Long reversedByEventId) {
        BankAccountTransactionEvent event = transactionEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction event not found with id: " + eventId));

        event.setIsReversed(true);
        event.setReversedByEventId(reversedByEventId);
        transactionEventRepository.save(event);
    }

    /**
     * Create balance snapshot for specific date
     * This is used for optimization - to avoid recalculating from all events
     */
    public BankAccountBalanceSnapshot createSnapshot(Long bankAccountId, LocalDate snapshotDate) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        // Check if snapshot already exists
        if (balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDate(bankAccountId, snapshotDate)) {
            throw new IllegalStateException("Snapshot already exists for date: " + snapshotDate);
        }

        // Get organization from BankAccount holder
        Organization organization = organizationRepository.findById(bankAccount.getHolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + bankAccount.getHolderId()));

        // Calculate opening balance (closing balance of previous day)
        LocalDate previousDay = snapshotDate.minusDays(1);
        BigDecimal openingBalance = calculateBalance(bankAccountId, previousDay);

        // Get events for this day
        List<BankAccountTransactionEvent> events = transactionEventRepository
                .findByBankAccountIdAndDateRangeWithRelations(bankAccountId, snapshotDate, snapshotDate);

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
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setOpeningBalance(openingBalance);
        snapshot.setDebitTurnover(debitTurnover);
        snapshot.setCreditTurnover(creditTurnover);
        snapshot.setClosingBalance(closingBalance);
        snapshot.setLastEventId(lastEventId);
        snapshot.setEventsCount(events.size());

        return balanceSnapshotRepository.save(snapshot);
    }

    /**
     * Get balance snapshot for specific date
     */
    @Transactional(readOnly = true)
    public BankAccountBalanceSnapshot getSnapshot(Long bankAccountId, LocalDate snapshotDate) {
        return balanceSnapshotRepository.findByBankAccountIdAndSnapshotDateWithRelations(bankAccountId, snapshotDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Snapshot not found for account " + bankAccountId + " on date " + snapshotDate));
    }

    /**
     * Get all snapshots for bank account within date range
     */
    @Transactional(readOnly = true)
    public List<BankAccountBalanceSnapshot> getSnapshotsInDateRange(
            Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        return balanceSnapshotRepository.findByBankAccountIdAndDateRangeWithRelations(
                bankAccountId, startDate, endDate);
    }
}