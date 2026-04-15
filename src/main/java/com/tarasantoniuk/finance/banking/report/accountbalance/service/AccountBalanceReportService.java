package com.tarasantoniuk.finance.banking.report.accountbalance.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceItemDto;
import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceReportDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AccountBalanceReportService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTransactionService transactionService;
    private final BankAccountBalanceService balanceService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSecurityContext orgContext;

    @Autowired
    public AccountBalanceReportService(
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
     * Generate account balance report
     *
     * @param asOfDate       Date to calculate balances (null = today)
     * @param organizationId Filter by organization (null = all)
     * @param currencyId     Filter by currency (null = all)
     * @return Account balance report
     */
    public AccountBalanceReportDto generateReport(
            LocalDate asOfDate,
            Long organizationId,
            Long currencyId
    ) {
        // Default to today if not specified
        LocalDate reportDate = asOfDate != null ? asOfDate : LocalDate.now();

        // Scope organization filter to the caller (admin = pass-through, non-admin = forced to own org)
        Long scopedOrganizationId = orgContext.resolveOptionalOrganizationId(organizationId);

        // Get all organization bank accounts
        List<BankAccount> accounts = getFilteredAccounts(scopedOrganizationId, currencyId);

        // Build report items
        List<AccountBalanceItemDto> items = accounts.stream()
                .map(account -> buildBalanceItem(account, reportDate))
                .collect(Collectors.toList());

        // Calculate totals
        Map<String, Map<String, BigDecimal>> totalsByOrganization = calculateTotalsByOrganization(items);
        Map<String, BigDecimal> grandTotalByCurrency = calculateGrandTotalByCurrency(items);

        // Build report
        AccountBalanceReportDto report = new AccountBalanceReportDto();
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportDate(reportDate);
        report.setAccounts(items);
        report.setTotalAccounts(items.size());
        report.setTotalsByOrganization(totalsByOrganization);
        report.setGrandTotalByCurrency(grandTotalByCurrency);

        return report;
    }

    /**
     * Get filtered accounts based on criteria.
     * Only returns organization accounts (not counterparty accounts).
     * Balance is only tracked for organization accounts.
     */
    private List<BankAccount> getFilteredAccounts(Long organizationId, Long currencyId) {
        List<BankAccount> accounts;

        if (organizationId != null && currencyId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId);
        } else if (organizationId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByHolderIdWithRelations(organizationId);
        } else if (currencyId != null) {
            accounts = bankAccountRepository.findOrganizationAccountsByCurrencyIdWithRelations(currencyId);
        } else {
            accounts = bankAccountRepository.findOrganizationAccountsWithRelations();
        }

        return accounts;
    }

    /**
     * Build balance item for single account
     */
    private AccountBalanceItemDto buildBalanceItem(BankAccount account, LocalDate asOfDate) {
        // Calculate balance using transaction service (end of day)
        LocalDateTime endOfDay = asOfDate.plusDays(1).atStartOfDay();
        BigDecimal balance = balanceService.calculateBalance(account.getId(), endOfDay);

        // Get organization name
        String organizationName = getOrganizationName(account.getHolderId());

        // Get last transaction date
        LocalDate lastTransactionDate = getLastTransactionDate(account.getId(), asOfDate);

        // Build DTO
        AccountBalanceItemDto item = new AccountBalanceItemDto();
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
        item.setBalance(balance);
        item.setLastTransactionDate(lastTransactionDate);
        item.setAccountStatus(account.getStatus() != null ? account.getStatus().name() : null);

        return item;
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
     * Get last transaction date for account up to specified date
     */
    private LocalDate getLastTransactionDate(Long accountId, LocalDate upToDate) {
        LocalDateTime endOfDay = upToDate.plusDays(1).atStartOfDay();
        return transactionService.getAccountEvents(accountId).stream()
                .filter(event -> event.getTransactionDateTime().isBefore(endOfDay))
                .map(event -> event.getTransactionDateTime().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * Calculate totals grouped by organization and currency
     */
    private Map<String, Map<String, BigDecimal>> calculateTotalsByOrganization(
            List<AccountBalanceItemDto> items
    ) {
        Map<String, Map<String, BigDecimal>> result = new HashMap<>();

        for (AccountBalanceItemDto item : items) {
            String orgName = item.getOrganizationName() != null ? item.getOrganizationName() : "Unknown";
            String currencyCode = item.getCurrencyCode() != null ? item.getCurrencyCode() : "UNKNOWN";

            result.computeIfAbsent(orgName, k -> new HashMap<>());

            BigDecimal currentTotal = result.get(orgName).getOrDefault(currencyCode, BigDecimal.ZERO);
            BigDecimal newTotal = currentTotal.add(item.getBalance() != null ? item.getBalance() : BigDecimal.ZERO);

            result.get(orgName).put(currencyCode, newTotal);
        }

        return result;
    }

    /**
     * Calculate grand totals by currency
     */
    private Map<String, BigDecimal> calculateGrandTotalByCurrency(
            List<AccountBalanceItemDto> items
    ) {
        Map<String, BigDecimal> result = new HashMap<>();

        for (AccountBalanceItemDto item : items) {
            String currencyCode = item.getCurrencyCode() != null ? item.getCurrencyCode() : "UNKNOWN";
            BigDecimal currentTotal = result.getOrDefault(currencyCode, BigDecimal.ZERO);
            BigDecimal newTotal = currentTotal.add(item.getBalance() != null ? item.getBalance() : BigDecimal.ZERO);
            result.put(currencyCode, newTotal);
        }

        return result;
    }
}