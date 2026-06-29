package com.tarasantoniuk.finance.banking.report.accountturnover.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverSummaryDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverTotalDto;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.period.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountTurnoverReportService Unit Tests")
class AccountTurnoverReportServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountTransactionService transactionService;

    @Mock
    private BankAccountBalanceService balanceService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationSecurityContext orgContext;

    @InjectMocks
    private AccountTurnoverReportService reportService;

    private BankAccount testAccount;
    private Organization testOrganization;
    private Currency testCurrency;
    private Bank testBank;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {
        lenient().when(orgContext.isAdmin()).thenReturn(true);
        lenient().when(orgContext.resolveOptionalOrganizationId(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(orgContext.resolveOrganizationId(any())).thenAnswer(inv -> inv.getArgument(0));

        startDateTime = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        endDateTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);

        // Create test country
        Country country = new Country();
        country.setId(1L);
        country.setIsoCode("UA");
        country.setName("Ukraine");

        // Create test currency
        testCurrency = new Currency();
        testCurrency.setId(1L);
        testCurrency.setCode("UAH");
        testCurrency.setSymbol("₴");
        testCurrency.setName("Ukrainian Hryvnia");

        // Create test organization
        testOrganization = new Organization();
        testOrganization.setId(1L);
        testOrganization.setName("Test Organization");
        testOrganization.setCountry(country);

        // Create test bank
        testBank = new Bank();
        testBank.setId(1L);
        testBank.setName("PrivatBank");
        testBank.setSwiftCode("PBANUA2X");
        testBank.setCountry(country);

        // Create test account
        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("UA123456789012345678901234567");
        testAccount.setBank(testBank);
        testAccount.setCurrency(testCurrency);
        testAccount.setHolderType(AccountHolderType.ORGANIZATION);
        testAccount.setHolderId(testOrganization.getId());
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    // ==================== Validation Tests ====================

    // Note: Period validation tests removed - validation now handled by ReportPeriodService

    @Test
    @DisplayName("Should accept period of exactly 365 days")
    void generateReport_ShouldAccept_When365Days() {
        // Given
        LocalDate yearStart = LocalDate.of(2024, 1, 1);
        LocalDate yearEnd = LocalDate.of(2024, 12, 31); // Exactly 365 days

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> {
            reportService.generateReport(new ReportPeriodDto(yearStart, yearEnd), null, null, null);
        });
    }

    // ==================== Generate Report Tests ====================

    @Test
    @DisplayName("Should generate report with all accounts when no filters")
    void generateReport_ShouldReturnAllAccounts_WhenNoFilters() {
        // Given
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(new BigDecimal("0.00"));
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        assertNotNull(report);
        assertNotNull(report.getPeriod());
        assertEquals(startDateTime.toLocalDate(), report.getPeriod().getStartDate());
        assertEquals(endDateTime.toLocalDate(), report.getPeriod().getEndDate());
        assertEquals(1, report.getTotalAccounts());
        assertEquals(1, report.getAccounts().size());

        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(testAccount.getId(), item.getAccountId());
        assertEquals(testAccount.getAccountNumber(), item.getAccountNumber());
        assertEquals("PrivatBank", item.getBankName());
        assertEquals("PBANUA2X", item.getBankSwiftCode());
        assertEquals("UAH", item.getCurrencyCode());
        assertEquals("₴", item.getCurrencySymbol());
        assertEquals("Test Organization", item.getOrganizationName());

        verify(bankAccountRepository).findOrganizationAccountsWithRelations(null);
    }

    @Test
    @DisplayName("Should filter by organization ID")
    void generateReport_ShouldFilterByOrganization_WhenOrganizationIdProvided() {
        // Given
        Long organizationId = 1L;
        when(bankAccountRepository.findOrganizationAccountsByHolderIdWithRelations(organizationId))
                .thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), organizationId, null, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findOrganizationAccountsByHolderIdWithRelations(organizationId);
        verify(bankAccountRepository, never()).findOrganizationAccountsWithRelations(null);
    }

    @Test
    @DisplayName("Should filter by currency ID")
    void generateReport_ShouldFilterByCurrency_WhenCurrencyIdProvided() {
        // Given
        Long currencyId = 1L;
        when(bankAccountRepository.findOrganizationAccountsByCurrencyIdWithRelations(currencyId, null))
                .thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, currencyId);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findOrganizationAccountsByCurrencyIdWithRelations(currencyId, null);
    }

    @Test
    @DisplayName("Should filter by specific account ID")
    void generateReport_ShouldFilterByAccount_WhenAccountIdProvided() {
        // Given
        Long accountId = 1L;
        when(bankAccountRepository.findByIdWithRelations(accountId))
                .thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, accountId, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByIdWithRelations(accountId);
        verify(bankAccountRepository, never()).findOrganizationAccountsWithRelations(null);
    }

    @Test
    @DisplayName("Should return empty report when account not found")
    void generateReport_ShouldReturnEmpty_WhenAccountNotFound() {
        // Given
        when(bankAccountRepository.findByIdWithRelations(anyLong()))
                .thenReturn(Optional.empty());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, 999L, null);

        // Then
        assertNotNull(report);
        assertEquals(0, report.getTotalAccounts());
        assertTrue(report.getAccounts().isEmpty());
    }

    @Test
    @DisplayName("Should exclude counterparty accounts from report")
    void generateReport_ShouldExcludeCounterpartyAccounts() {
        // Given - Create a counterparty account
        BankAccount counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(2L);
        counterpartyAccount.setAccountNumber("UA987654321098765432109876543");
        counterpartyAccount.setBank(testBank);
        counterpartyAccount.setCurrency(testCurrency);
        counterpartyAccount.setHolderType(AccountHolderType.COUNTERPARTY);
        counterpartyAccount.setHolderId(999L);  // Different counterparty
        counterpartyAccount.setStatus(AccountStatus.ACTIVE);

        // Only organization account should be returned by repository
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount)); // Only organization account
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        assertEquals(1, report.getAccounts().size());

        // Verify only organization account is included
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(testAccount.getId(), item.getAccountId());
        assertEquals(testAccount.getAccountNumber(), item.getAccountNumber());

        // Verify the correct repository method was called
        verify(bankAccountRepository).findOrganizationAccountsWithRelations(null);
    }

    @Test
    @DisplayName("Should exclude counterparty account when filtering by specific account ID")
    void generateReport_ShouldExcludeCounterpartyAccount_WhenFilteringByAccountId() {
        // Given - Create a counterparty account
        BankAccount counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(2L);
        counterpartyAccount.setAccountNumber("UA987654321098765432109876543");
        counterpartyAccount.setBank(testBank);
        counterpartyAccount.setCurrency(testCurrency);
        counterpartyAccount.setHolderType(AccountHolderType.COUNTERPARTY);
        counterpartyAccount.setHolderId(999L);
        counterpartyAccount.setStatus(AccountStatus.ACTIVE);

        // Repository returns the counterparty account
        when(bankAccountRepository.findByIdWithRelations(2L))
                .thenReturn(Optional.of(counterpartyAccount));

        // When - Request report for this counterparty account
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, 2L, null);

        // Then - Report should be empty because counterparty accounts are filtered out
        assertNotNull(report);
        assertEquals(0, report.getTotalAccounts());
        assertTrue(report.getAccounts().isEmpty());

        // Verify the account was queried
        verify(bankAccountRepository).findByIdWithRelations(2L);
    }

    // ==================== Turnover Calculation Tests ====================

    @Test
    @DisplayName("Should calculate opening and closing balances correctly")
    void generateReport_ShouldCalculateBalances_Correctly() {
        // Given
        BigDecimal openingBalance = new BigDecimal("10000.00");
        List<BankAccountTransactionEvent> events = createTestEvents();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));

        // Mock opening balance (start of day)
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(openingBalance);

        when(transactionService.getAccountEventsInDateTimeRange(
                eq(testAccount.getId()),
                eq(startDateTime.toLocalDate().atStartOfDay()),
                any(LocalDateTime.class)))
                .thenReturn(events);

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(openingBalance, item.getOpeningBalance());
        assertEquals(new BigDecimal("15000.00"), item.getDebitTurnover()); // 10000 + 5000
        assertEquals(new BigDecimal("8000.00"), item.getCreditTurnover());  // 5000 + 3000
        assertEquals(new BigDecimal("17000.00"), item.getClosingBalance()); // 10000 + 15000 - 8000
        assertEquals(4, item.getTransactionCount());
    }

    @Test
    @DisplayName("Should filter reversed transactions")
    void generateReport_ShouldFilterReversedTransactions() {
        // Given
        BankAccountTransactionEvent normalEvent = createEvent(TransactionType.DEBIT, new BigDecimal("1000.00"), false);
        BankAccountTransactionEvent reversedEvent = createEvent(TransactionType.DEBIT, new BigDecimal("2000.00"), true);

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(normalEvent, reversedEvent));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        // Should only count normal event, not reversed
        assertEquals(new BigDecimal("1000.00"), item.getDebitTurnover());
        assertEquals(BigDecimal.ZERO, item.getCreditTurnover());
    }

    // ==================== Summary Calculation Tests ====================

    @Test
    @DisplayName("Should calculate summary totals by currency")
    void generateReport_ShouldCalculateSummaryByCurrency() {
        // Given
        BankAccount account2 = createSecondAccount();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, account2));
        when(balanceService.calculateBalance(eq(testAccount.getId()), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(balanceService.calculateBalance(eq(account2.getId()), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateTimeRange(eq(testAccount.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(createTestEvents());
        when(transactionService.getAccountEventsInDateTimeRange(eq(account2.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        assertNotNull(summary);
        assertTrue(summary.containsKey("UAH"));

        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        assertEquals(new BigDecimal("15000.00"), uahTotal.getTotalOpeningBalance()); // 10000 + 5000
        assertEquals(new BigDecimal("30000.00"), uahTotal.getTotalDebitTurnover());  // 15000 * 2
        assertEquals(new BigDecimal("16000.00"), uahTotal.getTotalCreditTurnover()); // 8000 * 2
        assertEquals(new BigDecimal("29000.00"), uahTotal.getTotalClosingBalance());
        assertEquals(8, uahTotal.getTotalTransactionCount()); // 4 * 2
    }

    @Test
    @DisplayName("Should calculate summary for multiple currencies")
    void generateReport_ShouldCalculateSummaryForMultipleCurrencies() {
        // Given
        Currency usd = new Currency();
        usd.setId(2L);
        usd.setCode("USD");
        usd.setSymbol("$");

        BankAccount usdAccount = createSecondAccount();
        usdAccount.setCurrency(usd);

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, usdAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        assertEquals(2, summary.size());
        assertTrue(summary.containsKey("UAH"));
        assertTrue(summary.containsKey("USD"));
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle organization not found")
    void generateReport_ShouldHandleOrganizationNotFound() {
        // Given
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        assertNotNull(report);
        assertEquals("Unknown", report.getAccounts().get(0).getOrganizationName());
    }

    @Test
    @DisplayName("Should handle null values in account fields")
    void generateReport_ShouldHandleNullFields() {
        // Given
        BankAccount accountWithNulls = new BankAccount();
        accountWithNulls.setId(1L);
        accountWithNulls.setAccountNumber("UA123456789012345678901234567");
        accountWithNulls.setBank(null);
        accountWithNulls.setCurrency(null);
        accountWithNulls.setHolderId(null);
        accountWithNulls.setStatus(null);

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(accountWithNulls));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertNull(item.getBankName());
        assertNull(item.getBankSwiftCode());
        assertNull(item.getCurrencyCode());
        assertNull(item.getCurrencySymbol());
        assertNull(item.getOrganizationName());
        assertNull(item.getAccountStatus());
    }

    @Test
    @DisplayName("Should handle null currency code in summary calculation")
    void generateReport_ShouldHandleNullCurrencyCodeInSummary() {
        // Given
        testAccount.setCurrency(null);
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        assertTrue(summary.containsKey("UNKNOWN"));
        AccountTurnoverTotalDto unknownTotal = summary.get("UNKNOWN");
        assertNotNull(unknownTotal);
    }

    @Test
    @DisplayName("Should handle null opening balance")
    void generateReport_ShouldHandleNullOpeningBalance() {
        // Given
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));

        // Return null for opening balance (start of day)
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(null);

        when(transactionService.getAccountEventsInDateTimeRange(
                eq(testAccount.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        assertNotNull(report);
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        // Should treat null opening balance as BigDecimal.ZERO
        assertEquals(BigDecimal.ZERO, item.getOpeningBalance());
        assertEquals(BigDecimal.ZERO, item.getClosingBalance()); // 0 + 0 - 0 = 0
    }

    @Test
    @DisplayName("Should handle accounts with zero balances in summary calculation")
    void generateReport_ShouldHandleZeroBalancesInSummary() {
        // Given
        BankAccount account2 = createSecondAccount();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, account2));

        // Mock opening balances for both accounts (start of day)
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(new BigDecimal("1000.00"));
        when(balanceService.calculateBalance(eq(account2.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(BigDecimal.ZERO);

        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        // Should handle zero balances correctly
        assertEquals(new BigDecimal("1000.00"), uahTotal.getTotalOpeningBalance());
    }

    @Test
    @DisplayName("Should handle null debit turnover in summary calculation")
    void generateReport_ShouldHandleNullDebitTurnoverInSummary() {
        // Given
        BankAccount account2 = createSecondAccount();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, account2));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // First account has debit transactions
        when(transactionService.getAccountEventsInDateTimeRange(eq(testAccount.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(createEvent(TransactionType.DEBIT, new BigDecimal("500.00"), false)));

        // Second account has no transactions (will result in null/zero debit)
        when(transactionService.getAccountEventsInDateTimeRange(eq(account2.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        // Should handle both normal and zero/null values correctly
        assertEquals(new BigDecimal("500.00"), uahTotal.getTotalDebitTurnover());
    }

    @Test
    @DisplayName("Should handle null credit turnover in summary calculation")
    void generateReport_ShouldHandleNullCreditTurnoverInSummary() {
        // Given
        BankAccount account2 = createSecondAccount();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, account2));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // First account has credit transactions
        when(transactionService.getAccountEventsInDateTimeRange(eq(testAccount.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(createEvent(TransactionType.CREDIT, new BigDecimal("300.00"), false)));

        // Second account has no transactions (will result in null/zero credit)
        when(transactionService.getAccountEventsInDateTimeRange(eq(account2.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        // Should handle both normal and zero/null values correctly
        assertEquals(new BigDecimal("300.00"), uahTotal.getTotalCreditTurnover());
    }

    @Test
    @DisplayName("Should handle null closing balance in summary calculation")
    void generateReport_ShouldHandleNullClosingBalanceInSummary() {
        // Given
        BankAccount account2 = createSecondAccount();

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null))
                .thenReturn(Arrays.asList(testAccount, account2));

        // Mock opening balances for both accounts (start of day)
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(new BigDecimal("1000.00"));
        when(balanceService.calculateBalance(eq(account2.getId()), eq(startDateTime.toLocalDate().atStartOfDay())))
                .thenReturn(new BigDecimal("500.00"));

        // Mock for event retrieval - returns empty
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        // Should handle mixed balances correctly
        assertNotNull(uahTotal.getTotalClosingBalance());
        assertEquals(new BigDecimal("1500.00"), uahTotal.getTotalClosingBalance()); // 1000 + 500
    }

    // ==================== Helper Methods ====================

    private List<BankAccountTransactionEvent> createTestEvents() {
        return Arrays.asList(
                createEvent(TransactionType.DEBIT, new BigDecimal("10000.00"), false),
                createEvent(TransactionType.DEBIT, new BigDecimal("5000.00"), false),
                createEvent(TransactionType.CREDIT, new BigDecimal("5000.00"), false),
                createEvent(TransactionType.CREDIT, new BigDecimal("3000.00"), false)
        );
    }

    private BankAccountTransactionEvent createEvent(TransactionType type, BigDecimal amount, boolean isReversed) {
        BankAccountTransactionEvent event = new BankAccountTransactionEvent();
        event.setTransactionType(type);
        event.setAmount(amount);
        event.setIsReversed(isReversed);
        event.setTransactionDateTime(startDateTime.plusDays(5));
        return event;
    }

    private BankAccount createSecondAccount() {
        BankAccount account = new BankAccount();
        account.setId(2L);
        account.setAccountNumber("UA987654321098765432109876543");
        account.setBank(testBank);
        account.setCurrency(testCurrency);
        account.setHolderId(testOrganization.getId());
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }

    // ==================== Filter Combination Tests ====================

    @Test
    @DisplayName("Should filter by organization and currency together")
    void generateReport_ShouldFilterByOrganizationAndCurrency_WhenBothProvided() {
        // Given
        Long organizationId = 1L;
        Long currencyId = 1L;
        when(bankAccountRepository.findOrganizationAccountsByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId))
                .thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), organizationId, null, currencyId);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findOrganizationAccountsByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId);
        verify(bankAccountRepository, never()).findOrganizationAccountsWithRelations(null);
    }

    @Test
    @DisplayName("Should filter reversal events in turnover calculation")
    void generateReport_ShouldFilterReversalEvents() {
        // Given
        BankAccountTransactionEvent normalEvent = createEvent(TransactionType.DEBIT, new BigDecimal("1000.00"), false);
        normalEvent.setDocumentType("BankReceipt");

        BankAccountTransactionEvent reversalEvent = createEvent(TransactionType.CREDIT, new BigDecimal("1000.00"), false);
        reversalEvent.setDocumentType("BankReceiptReversal");

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(anyLong(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(normalEvent, reversalEvent));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(startDateTime.toLocalDate(), endDateTime.toLocalDate()), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        // Should only count normal event, reversal event should be filtered
        assertEquals(new BigDecimal("1000.00"), item.getDebitTurnover());
        assertEquals(BigDecimal.ZERO, item.getCreditTurnover());
        assertEquals(new BigDecimal("1000.00"), item.getClosingBalance());
    }

    // ==================== Account Turnover Details Tests ====================

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when accountId is null")
    void getAccountTurnoverDetails_ShouldThrowException_WhenAccountIdIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(null, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);
        });

        assertEquals("Account ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when startDate is null")
    void getAccountTurnoverDetails_ShouldThrowException_WhenStartDateIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(1L, null, endDateTime.toLocalDate(), 1L);
        });

        assertEquals("Start date and end date are required", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when endDate is null")
    void getAccountTurnoverDetails_ShouldThrowException_WhenEndDateIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), null, 1L);
        });

        assertEquals("Start date and end date are required", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when organizationId is null")
    void getAccountTurnoverDetails_ShouldThrowException_WhenOrganizationIdIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), null);
        });

        assertEquals("Organization ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when account not found")
    void getAccountTurnoverDetails_ShouldThrowException_WhenAccountNotFound() {
        // Given
        when(bankAccountRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(999L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);
        });

        assertEquals("Account not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception when account doesn't belong to organization")
    void getAccountTurnoverDetails_ShouldThrowException_WhenAccountDoesNotBelongToOrganization() {
        // Given
        testAccount.setHolderId(2L); // Different organization
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);
        });

        assertEquals("Account does not belong to the specified organization", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should throw exception for counterparty account")
    void getAccountTurnoverDetails_ShouldThrowException_WhenAccountIsCounterpartyAccount() {
        // Given
        testAccount.setHolderType(AccountHolderType.COUNTERPARTY);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);
        });

        assertEquals("Account is not an organization account", exception.getMessage());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should return details with no movements")
    void getAccountTurnoverDetails_ShouldReturnDetailsWithNoMovements() {
        // Given
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(1L, details.getAccountId());
        assertEquals("UA123456789012345678901234567", details.getAccountNumber());
        assertEquals("PrivatBank (PBANUA2X)", details.getAccountName());
        assertEquals("UAH", details.getCurrency());
        assertEquals(new BigDecimal("5000.00"), details.getOpeningBalance());
        assertEquals(new BigDecimal("5000.00"), details.getClosingBalance());
        assertTrue(details.getMovements().isEmpty());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should return details with movements and running balance")
    void getAccountTurnoverDetails_ShouldReturnDetailsWithMovements() {
        // Given
        BankAccountTransactionEvent event1 = createEvent(TransactionType.DEBIT, new BigDecimal("1000.00"), false);
        event1.setId(1L);
        event1.setDocumentId(100L);
        event1.setDocumentType("BankReceipt");
        event1.setDescription("Customer payment");
        event1.setTransactionDateTime(startDateTime.plusDays(1));

        BankAccountTransactionEvent event2 = createEvent(TransactionType.CREDIT, new BigDecimal("500.00"), false);
        event2.setId(2L);
        event2.setDocumentId(200L);
        event2.setDocumentType("BankPayment");
        event2.setDescription("Supplier payment");
        event2.setTransactionDateTime(startDateTime.plusDays(2));

        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(event1, event2));

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(new BigDecimal("5000.00"), details.getOpeningBalance());
        assertEquals(new BigDecimal("5500.00"), details.getClosingBalance()); // 5000 + 1000 - 500
        assertEquals(2, details.getMovements().size());

        // Verify first movement
        var movement1 = details.getMovements().get(0);
        assertEquals(1L, movement1.getEventId());
        assertEquals(100L, movement1.getDocumentId());
        assertEquals("BankReceipt", movement1.getDocumentType());
        assertEquals("Customer payment", movement1.getDescription());
        assertEquals(new BigDecimal("1000.00"), movement1.getDebit());
        assertEquals(BigDecimal.ZERO, movement1.getCredit());
        assertEquals(new BigDecimal("6000.00"), movement1.getRunningBalance()); // 5000 + 1000

        // Verify second movement
        var movement2 = details.getMovements().get(1);
        assertEquals(2L, movement2.getEventId());
        assertEquals(200L, movement2.getDocumentId());
        assertEquals("BankPayment", movement2.getDocumentType());
        assertEquals("Supplier payment", movement2.getDescription());
        assertEquals(BigDecimal.ZERO, movement2.getDebit());
        assertEquals(new BigDecimal("500.00"), movement2.getCredit());
        assertEquals(new BigDecimal("5500.00"), movement2.getRunningBalance()); // 6000 - 500
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should filter reversed and reversal events")
    void getAccountTurnoverDetails_ShouldFilterReversedAndReversalEvents() {
        // Given
        BankAccountTransactionEvent normalEvent = createEvent(TransactionType.DEBIT, new BigDecimal("1000.00"), false);
        normalEvent.setId(1L);
        normalEvent.setDocumentId(100L);
        normalEvent.setDocumentType("BankReceipt");
        normalEvent.setTransactionDateTime(startDateTime.plusDays(1));

        BankAccountTransactionEvent reversedEvent = createEvent(TransactionType.DEBIT, new BigDecimal("2000.00"), true);
        reversedEvent.setId(2L);
        reversedEvent.setDocumentId(200L);
        reversedEvent.setDocumentType("BankReceipt");
        reversedEvent.setTransactionDateTime(startDateTime.plusDays(2));

        BankAccountTransactionEvent reversalEvent = createEvent(TransactionType.CREDIT, new BigDecimal("2000.00"), false);
        reversalEvent.setId(3L);
        reversalEvent.setDocumentId(200L);
        reversalEvent.setDocumentType("BankReceiptReversal");
        reversalEvent.setTransactionDateTime(startDateTime.plusDays(3));

        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(normalEvent, reversedEvent, reversalEvent));

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(1, details.getMovements().size()); // Only normal event should be included
        assertEquals(1L, details.getMovements().get(0).getEventId());
        assertEquals(new BigDecimal("1000.00"), details.getClosingBalance());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should handle null opening balance")
    void getAccountTurnoverDetails_ShouldHandleNullOpeningBalance() {
        // Given
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(null); // Null opening balance
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(BigDecimal.ZERO, details.getOpeningBalance());
        assertEquals(BigDecimal.ZERO, details.getClosingBalance());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should handle null description")
    void getAccountTurnoverDetails_ShouldHandleNullDescription() {
        // Given
        BankAccountTransactionEvent event = createEvent(TransactionType.DEBIT, new BigDecimal("1000.00"), false);
        event.setId(1L);
        event.setDocumentId(100L);
        event.setDocumentType("BankReceipt");
        event.setDescription(null); // Null description
        event.setTransactionDateTime(startDateTime.plusDays(1));

        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(event));

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(1, details.getMovements().size());
        assertEquals("", details.getMovements().get(0).getDescription()); // Should be empty string, not null
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should handle bank with no SWIFT code")
    void getAccountTurnoverDetails_ShouldHandleBankWithNoSwiftCode() {
        // Given
        testBank.setSwiftCode(null);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals("PrivatBank", details.getAccountName()); // Should not include SWIFT code
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should handle null bank")
    void getAccountTurnoverDetails_ShouldHandleNullBank() {
        // Given
        testAccount.setBank(null);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals("Unknown Bank", details.getAccountName());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should handle null currency")
    void getAccountTurnoverDetails_ShouldHandleNullCurrency() {
        // Given
        testAccount.setCurrency(null);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals("UNKNOWN", details.getCurrency());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should sort movements chronologically")
    void getAccountTurnoverDetails_ShouldSortMovementsChronologically() {
        // Given
        BankAccountTransactionEvent event1 = createEvent(TransactionType.DEBIT, new BigDecimal("100.00"), false);
        event1.setId(3L);
        event1.setTransactionDateTime(startDateTime.plusDays(3));

        BankAccountTransactionEvent event2 = createEvent(TransactionType.DEBIT, new BigDecimal("200.00"), false);
        event2.setId(1L);
        event2.setTransactionDateTime(startDateTime.plusDays(1));

        BankAccountTransactionEvent event3 = createEvent(TransactionType.DEBIT, new BigDecimal("300.00"), false);
        event3.setId(2L);
        event3.setTransactionDateTime(startDateTime.plusDays(2));

        // Return events in random order
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(event1, event2, event3));

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(3, details.getMovements().size());
        // Should be sorted by date, then by ID
        assertEquals(1L, details.getMovements().get(0).getEventId());
        assertEquals(2L, details.getMovements().get(1).getEventId());
        assertEquals(3L, details.getMovements().get(2).getEventId());
    }

    @Test
    @DisplayName("getAccountTurnoverDetails - Should sort movements by ID when same timestamp")
    void getAccountTurnoverDetails_ShouldSortMovementsByIdWhenSameTimestamp() {
        // Given
        LocalDateTime sameTime = startDateTime.plusDays(1);

        BankAccountTransactionEvent event1 = createEvent(TransactionType.DEBIT, new BigDecimal("100.00"), false);
        event1.setId(3L);
        event1.setTransactionDateTime(sameTime);

        BankAccountTransactionEvent event2 = createEvent(TransactionType.DEBIT, new BigDecimal("200.00"), false);
        event2.setId(1L);
        event2.setTransactionDateTime(sameTime);

        BankAccountTransactionEvent event3 = createEvent(TransactionType.DEBIT, new BigDecimal("300.00"), false);
        event3.setId(2L);
        event3.setTransactionDateTime(sameTime);

        // Return events in random order
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testAccount));
        when(balanceService.calculateBalance(eq(1L), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(event1, event2, event3));

        // When
        var details = reportService.getAccountTurnoverDetails(1L, startDateTime.toLocalDate(), endDateTime.toLocalDate(), 1L);

        // Then
        assertNotNull(details);
        assertEquals(3, details.getMovements().size());
        // Should be sorted by ID when timestamps are equal
        assertEquals(1L, details.getMovements().get(0).getEventId());
        assertEquals(2L, details.getMovements().get(1).getEventId());
        assertEquals(3L, details.getMovements().get(2).getEventId());
    }

    // ==================== Boundary Condition Tests ====================

    @Test
    @DisplayName("Should not double-count transaction at exact start of period (00:00:00)")
    void generateReport_ShouldNotDoubleCountTransactionAtMidnight() {
        // Given: Transaction with exact timestamp at start of period
        LocalDate reportStartDate = LocalDate.of(2025, 1, 1);
        LocalDate reportEndDate = LocalDate.of(2025, 1, 31);
        LocalDateTime midnightTransaction = reportStartDate.atStartOfDay(); // 2025-01-01 00:00:00

        // Opening balance: 0 (no transactions before midnight)
        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(midnightTransaction)))
                .thenReturn(BigDecimal.ZERO); // No balance before midnight

        // Period has one transaction at exact midnight
        BankAccountTransactionEvent midnightEvent = createEvent(TransactionType.DEBIT, new BigDecimal("10000.00"), false);
        midnightEvent.setTransactionDateTime(midnightTransaction);
        midnightEvent.setDocumentType("BankReceipt");

        when(transactionService.getAccountEventsInDateTimeRange(
                eq(testAccount.getId()),
                eq(midnightTransaction),
                any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(midnightEvent));

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(reportStartDate, reportEndDate), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(BigDecimal.ZERO, item.getOpeningBalance(), "Opening balance should be 0");
        assertEquals(new BigDecimal("10000.00"), item.getDebitTurnover(), "Debit turnover should be 10000");
        assertEquals(BigDecimal.ZERO, item.getCreditTurnover(), "Credit turnover should be 0");
        assertEquals(new BigDecimal("10000.00"), item.getClosingBalance(),
                "Closing balance should be 10000 (not 20000 - transaction counted once, not twice)");
        assertEquals(1, item.getTransactionCount(), "Should count transaction only once");
    }

    @Test
    @DisplayName("Should correctly handle transaction at end of day (23:59:59)")
    void generateReport_ShouldHandleTransactionAtEndOfDay() {
        // Given: Transaction at end of period day
        LocalDate reportStartDate = LocalDate.of(2025, 1, 1);
        LocalDate reportEndDate = LocalDate.of(2025, 1, 1); // Single day report
        LocalDateTime endOfDay = reportStartDate.atTime(23, 59, 59);

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(reportStartDate.atStartOfDay())))
                .thenReturn(BigDecimal.ZERO);

        BankAccountTransactionEvent endOfDayEvent = createEvent(TransactionType.DEBIT, new BigDecimal("5000.00"), false);
        endOfDayEvent.setTransactionDateTime(endOfDay);
        endOfDayEvent.setDocumentType("BankReceipt");

        when(transactionService.getAccountEventsInDateTimeRange(
                eq(testAccount.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(endOfDayEvent));

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(reportStartDate, reportEndDate), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(BigDecimal.ZERO, item.getOpeningBalance());
        assertEquals(new BigDecimal("5000.00"), item.getDebitTurnover());
        assertEquals(new BigDecimal("5000.00"), item.getClosingBalance());
    }

    @Test
    @DisplayName("Should correctly separate transactions before and at period boundary")
    void generateReport_ShouldSeparateTransactionsAtBoundary() {
        // Given: One transaction before period, one at exact period start
        LocalDate reportStartDate = LocalDate.of(2025, 1, 2);
        LocalDate reportEndDate = LocalDate.of(2025, 1, 31);

        LocalDateTime beforePeriod = LocalDate.of(2025, 1, 1).atTime(23, 59, 59);
        LocalDateTime atPeriodStart = reportStartDate.atStartOfDay(); // 2025-01-02 00:00:00

        when(bankAccountRepository.findOrganizationAccountsWithRelations(null)).thenReturn(Arrays.asList(testAccount));

        // Opening balance includes transaction before period
        when(balanceService.calculateBalance(eq(testAccount.getId()), eq(atPeriodStart)))
                .thenReturn(new BigDecimal("1000.00")); // Result of transaction before period

        // Period includes only transaction at period start
        BankAccountTransactionEvent periodStartEvent = createEvent(TransactionType.DEBIT, new BigDecimal("2000.00"), false);
        periodStartEvent.setTransactionDateTime(atPeriodStart);
        periodStartEvent.setDocumentType("BankReceipt");

        when(transactionService.getAccountEventsInDateTimeRange(
                eq(testAccount.getId()),
                eq(atPeriodStart),
                any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(periodStartEvent));

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(
                new ReportPeriodDto(reportStartDate, reportEndDate), null, null, null);

        // Then
        AccountTurnoverSummaryDto item = report.getAccounts().get(0);
        assertEquals(new BigDecimal("1000.00"), item.getOpeningBalance(), "Opening should include transaction before period");
        assertEquals(new BigDecimal("2000.00"), item.getDebitTurnover(), "Period should include only transaction at start");
        assertEquals(new BigDecimal("3000.00"), item.getClosingBalance(), "Closing = 1000 + 2000");
        assertEquals(1, item.getTransactionCount(), "Should count only period transaction");
    }
}