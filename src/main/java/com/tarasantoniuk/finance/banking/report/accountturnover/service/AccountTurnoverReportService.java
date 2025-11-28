package com.tarasantoniuk.finance.banking.report.accountturnover.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverSummaryDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverTotalDto;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
//import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.report.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AccountTurnoverReportService {

    private static final int MAX_PERIOD_DAYS = 365;

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTransactionService transactionService;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public AccountTurnoverReportService(
            BankAccountRepository bankAccountRepository,
            BankAccountTransactionService transactionService,
            OrganizationRepository organizationRepository
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionService = transactionService;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Generate account turnover report for specified period
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param organizationId Filter by organization (null = all)
     * @param accountId Filter by specific account (null = all)
     * @param currencyId Filter by currency (null = all)
     * @return Account turnover report
     */
    public AccountTurnoverReportDto generateReport(
            LocalDate startDate,
            LocalDate endDate,
            Long organizationId,
            Long accountId,
            Long currencyId
    ) {
        // Validate period
        validatePeriod(startDate, endDate);

        // Build period DTO
        ReportPeriodDto period = new ReportPeriodDto(startDate, endDate);

        // Get filtered accounts
        List<BankAccount> accounts = getFilteredAccounts(organizationId, accountId, currencyId);

        // Build turnover items
        List<AccountTurnoverSummaryDto> items = accounts.stream()
                .map(account -> buildTurnoverItem(account, startDate, endDate))
                .collect(Collectors.toList());

        // Calculate summary by currency
        Map<String, AccountTurnoverTotalDto> summaryByCurrency = calculateSummaryByCurrency(items);

        // Build report
        AccountTurnoverReportDto report = new AccountTurnoverReportDto();
        report.setGeneratedAt(LocalDateTime.now());
        report.setPeriod(period);
        report.setAccounts(items);
        report.setTotalAccounts(items.size());
        report.setSummaryByCurrency(summaryByCurrency);

        return report;
    }

    /**
     * Validate report period (max 365 days)
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_PERIOD_DAYS) {
            throw new ValidationException(
                    String.format("Period cannot exceed %d days. Current period: %d days",
                            MAX_PERIOD_DAYS, daysBetween)
            );
        }
    }

    /**
     * Get filtered accounts based on criteria
     */
    private List<BankAccount> getFilteredAccounts(Long organizationId, Long accountId, Long currencyId) {
        // If specific account requested, return only that account
        if (accountId != null) {
            return bankAccountRepository.findByIdWithRelations(accountId)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        // Otherwise use organization/currency filters
        List<BankAccount> accounts;

        if (organizationId != null && currencyId != null) {
            accounts = bankAccountRepository.findByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId);
        } else if (organizationId != null) {
            accounts = bankAccountRepository.findByHolderIdWithRelations(organizationId);
        } else if (currencyId != null) {
            accounts = bankAccountRepository.findByCurrencyIdWithRelations(currencyId);
        } else {
            accounts = bankAccountRepository.findAllWithRelations();
        }

        return accounts;
    }

    /**
     * Build turnover item for single account
     */
    private AccountTurnoverSummaryDto buildTurnoverItem(BankAccount account, LocalDate startDate, LocalDate endDate) {
        // Calculate opening balance (day before start date)
        LocalDate dayBeforeStart = startDate.minusDays(1);
        BigDecimal openingBalance = transactionService.calculateBalance(account.getId(), dayBeforeStart);

        // Get events in period
        List<BankAccountTransactionEvent> eventsInPeriod = transactionService
                .getAccountEventsInDateRange(account.getId(), startDate, endDate);

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
     */
    private BigDecimal calculateTurnover(List<BankAccountTransactionEvent> events, TransactionType type) {
        return events.stream()
                .filter(event -> event.getTransactionType() == type)
                .filter(event -> !Boolean.TRUE.equals(event.getIsReversed()))
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

            // Add to totals
            summary.setTotalOpeningBalance(
                    summary.getTotalOpeningBalance().add(item.getOpeningBalance() != null ? item.getOpeningBalance() : BigDecimal.ZERO)
            );
            summary.setTotalDebitTurnover(
                    summary.getTotalDebitTurnover().add(item.getDebitTurnover() != null ? item.getDebitTurnover() : BigDecimal.ZERO)
            );
            summary.setTotalCreditTurnover(
                    summary.getTotalCreditTurnover().add(item.getCreditTurnover() != null ? item.getCreditTurnover() : BigDecimal.ZERO)
            );
            summary.setTotalClosingBalance(
                    summary.getTotalClosingBalance().add(item.getClosingBalance() != null ? item.getClosingBalance() : BigDecimal.ZERO)
            );
            summary.setTotalTransactionCount(summary.getTotalTransactionCount() + item.getTransactionCount());
        }

        return result;
    }
}