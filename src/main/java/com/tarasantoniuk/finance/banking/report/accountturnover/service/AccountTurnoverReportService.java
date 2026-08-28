package com.tarasantoniuk.finance.banking.report.accountturnover.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverDetailsDTO;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverSummaryDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverTotalDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.MovementDTO;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.period.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class AccountTurnoverReportService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTransactionService transactionService;
    private final BankAccountBalanceService balanceService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSecurityContext orgContext;

    @Autowired
    public AccountTurnoverReportService(
            BankAccountRepository bankAccountRepository,
            BankAccountTransactionService transactionService,
            BankAccountBalanceService balanceService,
            OrganizationRepository organizationRepository,
            OrganizationSecurityContext orgContext
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionService = transactionService;
        this.balanceService = balanceService;
        this.organizationRepository = organizationRepository;
        this.orgContext = orgContext;
    }

    /**
     * Generate account turnover report for specified period
     *
     * @param period         Report period (validated by controller)
     * @param organizationId Filter by organization (null = all)
     * @param accountId      Filter by specific account (null = all)
     * @param currencyId     Filter by currency (null = all)
     * @return Account turnover report
     */
    public AccountTurnoverReportDto generateReport(
            ReportPeriodDto period,
            Long organizationId,
            Long accountId,
            Long currencyId
    ) {
        // Period is already validated by ReportPeriodService
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = period.getEndDate();

        // Scope organization filter to the caller (admin = pass-through, non-admin = forced to own org)
        Long scopedOrganizationId = orgContext.resolveOptionalOrganizationId(organizationId);

        // Get filtered accounts
        List<BankAccount> accounts = getFilteredAccounts(scopedOrganizationId, accountId, currencyId);

        // Build turnover items
        List<AccountTurnoverSummaryDto> items = accounts.stream()
                .map(account -> buildTurnoverItem(account, startDate, endDate))
                .collect(Collectors.toList());

        // Calculate summary by currency
        Map<String, AccountTurnoverTotalDto> summaryByCurrency = calculateSummaryByCurrency(items);

        // Build report
        AccountTurnoverReportDto report = new AccountTurnoverReportDto();
        report.setGeneratedAt(LocalDateTime.now());
        report.setPeriod(period);  // Use the period from controller (includes periodType)
        report.setAccounts(items);
        report.setTotalAccounts(items.size());
        report.setSummaryByCurrency(summaryByCurrency);

        return report;
    }

    /**
     * Get filtered accounts based on criteria.
     * Only returns organization accounts (not counterparty accounts).
     * Balance is only tracked for organization accounts.
     */
    private List<BankAccount> getFilteredAccounts(Long organizationId, Long accountId, Long currencyId) {
        // If specific account requested, return only that account if it's an organization account
        if (accountId != null) {
            return bankAccountRepository.findByIdWithRelations(accountId)
                    .filter(account -> account.getHolderType() == AccountHolderType.ORGANIZATION)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        // Otherwise use organization/currency filters - only organization accounts
        List<BankAccount> accounts;

        if (organizationId != null && currencyId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId);
        } else if (organizationId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByHolderIdWithRelations(organizationId);
        } else if (currencyId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByCurrencyIdWithRelations(currencyId, null);
        } else {
            accounts = bankAccountRepository.findOrganizationAccountsWithRelations(null);
        }

        return accounts;
    }

    /**
     * Build turnover item for single account
     */
    private AccountTurnoverSummaryDto buildTurnoverItem(BankAccount account, LocalDate startDate, LocalDate endDate) {
        // Calculate opening balance (start of startDate)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        BigDecimal openingBalance = balanceService.calculateBalance(account.getId(), startDateTime);

        // Handle null opening balance
        if (openingBalance == null) {
            openingBalance = BigDecimal.ZERO;
        }

        // Get events in period (from start of startDate to end of endDate)
        // Use start of next day as exclusive end boundary (query uses < endDateTime)
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        List<BankAccountTransactionEvent> eventsInPeriod = transactionService
                .getAccountEventsInDateTimeRange(account.getId(), startDateTime, endDateTime);

        // Calculate turnovers
        BigDecimal debitTurnover = calculateTurnover(eventsInPeriod, TransactionType.DEBIT);
        BigDecimal creditTurnover = calculateTurnover(eventsInPeriod, TransactionType.CREDIT);

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance.add(debitTurnover).subtract(creditTurnover);

        // Get organization name
        String organizationName = getOrganizationName(account.getHolderId());

        // Build DTO
        AccountTurnoverSummaryDto item = new AccountTurnoverSummaryDto();
        item.setAccountId(account.getId());
        item.setAccountNumber(account.getAccountNumber());

        Bank bank = account.getBank();
        item.setBankName(bank != null ? bank.getName() : null);
        item.setBankSwiftCode(bank != null ? bank.getSwiftCode() : null);

        Currency currency = account.getCurrency();
        item.setCurrencyCode(currency != null ? currency.getCode() : null);
        item.setCurrencySymbol(currency != null ? currency.getSymbol() : null);

        item.setOrganizationName(organizationName);
        item.setOrganizationId(account.getHolderId());
        item.setOpeningBalance(openingBalance);
        item.setDebitTurnover(debitTurnover);
        item.setCreditTurnover(creditTurnover);
        item.setClosingBalance(closingBalance);
        item.setTransactionCount(eventsInPeriod.size());
        item.setAccountStatus(account.getStatus() != null ? account.getStatus().name() : null);

        return item;
    }

    /**
     * Calculate turnover for specific transaction type
     * Excludes:
     * 1. Events that are reversed (isReversed = true)
     * 2. Reversal events themselves (documentType ends with "Reversal")
     */
    private BigDecimal calculateTurnover(List<BankAccountTransactionEvent> events, TransactionType type) {
        return events.stream()
                .filter(event -> event.getTransactionType() == type)
                .filter(event -> !Boolean.TRUE.equals(event.getIsReversed()))
                .filter(event -> event.getDocumentType() == null || !event.getDocumentType().endsWith("Reversal"))
                .map(BankAccountTransactionEvent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get organization name by ID
     */
    private String getOrganizationName(Long organizationId) {
        if (organizationId == null) {
            return null;
        }

        Optional<Organization> organization = organizationRepository.findById(organizationId);
        return organization.map(Organization::getName).orElse("Unknown");
    }

    /**
     * Calculate summary totals grouped by currency
     */
    private Map<String, AccountTurnoverTotalDto> calculateSummaryByCurrency(List<AccountTurnoverSummaryDto> items) {
        Map<String, AccountTurnoverTotalDto> result = new HashMap<>();

        for (AccountTurnoverSummaryDto item : items) {
            String currencyCode = item.getCurrencyCode() != null ? item.getCurrencyCode() : "UNKNOWN";

            AccountTurnoverTotalDto summary = result.computeIfAbsent(currencyCode, k -> new AccountTurnoverTotalDto());

            // Amounts are guaranteed non-null by buildTurnoverItem
            summary.setTotalOpeningBalance(summary.getTotalOpeningBalance().add(item.getOpeningBalance()));
            summary.setTotalDebitTurnover(summary.getTotalDebitTurnover().add(item.getDebitTurnover()));
            summary.setTotalCreditTurnover(summary.getTotalCreditTurnover().add(item.getCreditTurnover()));
            summary.setTotalClosingBalance(summary.getTotalClosingBalance().add(item.getClosingBalance()));
            summary.setTotalTransactionCount(summary.getTotalTransactionCount() + item.getTransactionCount());
        }

        return result;
    }

    /**
     * Get detailed account turnover with all movements for a specific account
     *
     * @param accountId      Account ID
     * @param startDate      Period start date
     * @param endDate        Period end date
     * @param organizationId Organization ID (for validation)
     * @return Detailed account turnover with movements
     */
    public AccountTurnoverDetailsDTO getAccountTurnoverDetails(
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            Long organizationId
    ) {
        // Validate input
        if (accountId == null) {
            throw new ValidationException("Account ID is required");
        }
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (organizationId == null) {
            throw new ValidationException("Organization ID is required");
        }

        // Scope organization filter to the caller
        Long scopedOrganizationId = orgContext.resolveOrganizationId(organizationId);

        // Get account with relations
        BankAccount account = bankAccountRepository.findByIdWithRelations(accountId)
                .orElseThrow(() -> new ValidationException("Account not found with ID: " + accountId));

        // Verify account belongs to the organization
        if (!scopedOrganizationId.equals(account.getHolderId())) {
            throw new ValidationException("Account does not belong to the specified organization");
        }

        // Verify it's an organization account
        if (account.getHolderType() != AccountHolderType.ORGANIZATION) {
            throw new ValidationException("Account is not an organization account");
        }

        // Calculate opening balance (before start date)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        BigDecimal openingBalance = balanceService.calculateBalance(accountId, startDateTime);
        if (openingBalance == null) {
            openingBalance = BigDecimal.ZERO;
        }

        // Get events in period (from start of startDate to end of endDate)
        // Use start of next day as exclusive end boundary (query uses < endDateTime)
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        List<BankAccountTransactionEvent> eventsInPeriod = transactionService
                .getAccountEventsInDateTimeRange(accountId, startDateTime, endDateTime);

        // Filter out reversed events and reversal events
        List<BankAccountTransactionEvent> filteredEvents = eventsInPeriod.stream()
                .filter(event -> !Boolean.TRUE.equals(event.getIsReversed()))
                .filter(event -> event.getDocumentType() == null || !event.getDocumentType().endsWith("Reversal"))
                .sorted(Comparator.comparing(BankAccountTransactionEvent::getTransactionDateTime)
                        .thenComparing(BankAccountTransactionEvent::getId))
                .collect(Collectors.toList());

        // Build movements with running balance
        List<MovementDTO> movements = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        for (BankAccountTransactionEvent event : filteredEvents) {
            MovementDTO movement = new MovementDTO();
            movement.setEventId(event.getId());
            movement.setDocumentId(event.getDocumentId());
            movement.setDocumentType(event.getDocumentType());
            movement.setDocumentDate(event.getTransactionDateTime());
            movement.setDescription(event.getDescription() != null ? event.getDescription() : "");

            // Set debit/credit based on transaction type
            if (event.getTransactionType() == TransactionType.DEBIT) {
                movement.setDebit(event.getAmount());
                movement.setCredit(BigDecimal.ZERO);
                runningBalance = runningBalance.add(event.getAmount());
            } else {
                movement.setDebit(BigDecimal.ZERO);
                movement.setCredit(event.getAmount());
                runningBalance = runningBalance.subtract(event.getAmount());
            }

            movement.setRunningBalance(runningBalance);
            movements.add(movement);
        }

        // Closing balance is the last running balance (or opening balance if no movements)
        BigDecimal closingBalance = movements.isEmpty() ? openingBalance : runningBalance;

        // Build account name from bank info
        Bank bank = account.getBank();
        String accountName = bank != null
                ? bank.getName() + (bank.getSwiftCode() != null ? " (" + bank.getSwiftCode() + ")" : "")
                : "Unknown Bank";

        // Build DTO
        AccountTurnoverDetailsDTO details = new AccountTurnoverDetailsDTO();
        details.setAccountId(accountId);
        details.setAccountNumber(account.getAccountNumber());
        details.setAccountName(accountName);

        Currency currency = account.getCurrency();
        details.setCurrency(currency != null ? currency.getCode() : "UNKNOWN");

        details.setOpeningBalance(openingBalance);
        details.setClosingBalance(closingBalance);
        details.setMovements(movements);

        return details;
    }
}