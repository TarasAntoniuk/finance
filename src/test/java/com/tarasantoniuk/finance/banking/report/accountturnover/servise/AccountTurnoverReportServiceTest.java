package com.tarasantoniuk.finance.banking.report.accountturnover.servise;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverSummaryDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverTotalDto;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.report.accountturnover.service.AccountTurnoverReportService;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountTurnoverReportService Unit Tests")
class AccountTurnoverReportServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountTransactionService transactionService;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private AccountTurnoverReportService reportService;

    private BankAccount testAccount;
    private Organization testOrganization;
    private Currency testCurrency;
    private Bank testBank;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);

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
        testAccount.setHolderId(testOrganization.getId());
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should throw exception when start date is null")
    void generateReport_ShouldThrowException_WhenStartDateIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.generateReport(null, endDate, null, null, null);
        });

        assertEquals("Start date and end date are required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end date is null")
    void generateReport_ShouldThrowException_WhenEndDateIsNull() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.generateReport(startDate, null, null, null, null);
        });

        assertEquals("Start date and end date are required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void generateReport_ShouldThrowException_WhenStartDateAfterEndDate() {
        // Given
        LocalDate invalidStartDate = LocalDate.of(2024, 2, 1);
        LocalDate invalidEndDate = LocalDate.of(2024, 1, 1);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.generateReport(invalidStartDate, invalidEndDate, null, null, null);
        });

        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when period exceeds 365 days")
    void generateReport_ShouldThrowException_WhenPeriodExceeds365Days() {
        // Given
        LocalDate longStartDate = LocalDate.of(2024, 1, 1);
        LocalDate longEndDate = LocalDate.of(2025, 1, 2); // 367 days

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reportService.generateReport(longStartDate, longEndDate, null, null, null);
        });

        assertTrue(exception.getMessage().contains("Period cannot exceed 365 days"));
    }

    @Test
    @DisplayName("Should accept period of exactly 365 days")
    void generateReport_ShouldAccept_When365Days() {
        // Given
        LocalDate yearStart = LocalDate.of(2024, 1, 1);
        LocalDate yearEnd = LocalDate.of(2024, 12, 31); // Exactly 365 days

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Collections.emptyList());

        // When & Then
        assertDoesNotThrow(() -> {
            reportService.generateReport(yearStart, yearEnd, null, null, null);
        });
    }

    // ==================== Generate Report Tests ====================

    @Test
    @DisplayName("Should generate report with all accounts when no filters")
    void generateReport_ShouldReturnAllAccounts_WhenNoFilters() {
        // Given
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("0.00"));
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

        // Then
        assertNotNull(report);
        assertNotNull(report.getPeriod());
        assertEquals(startDate, report.getPeriod().getStartDate());
        assertEquals(endDate, report.getPeriod().getEndDate());
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

        verify(bankAccountRepository).findAllWithRelations();
    }

    @Test
    @DisplayName("Should filter by organization ID")
    void generateReport_ShouldFilterByOrganization_WhenOrganizationIdProvided() {
        // Given
        Long organizationId = 1L;
        when(bankAccountRepository.findByHolderIdWithRelations(organizationId))
                .thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, organizationId, null, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByHolderIdWithRelations(organizationId);
        verify(bankAccountRepository, never()).findAllWithRelations();
    }

    @Test
    @DisplayName("Should filter by currency ID")
    void generateReport_ShouldFilterByCurrency_WhenCurrencyIdProvided() {
        // Given
        Long currencyId = 1L;
        when(bankAccountRepository.findByCurrencyIdWithRelations(currencyId))
                .thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, currencyId);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByCurrencyIdWithRelations(currencyId);
    }

    @Test
    @DisplayName("Should filter by specific account ID")
    void generateReport_ShouldFilterByAccount_WhenAccountIdProvided() {
        // Given
        Long accountId = 1L;
        when(bankAccountRepository.findByIdWithRelations(accountId))
                .thenReturn(Optional.of(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, accountId, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByIdWithRelations(accountId);
        verify(bankAccountRepository, never()).findAllWithRelations();
    }

    @Test
    @DisplayName("Should return empty report when account not found")
    void generateReport_ShouldReturnEmpty_WhenAccountNotFound() {
        // Given
        when(bankAccountRepository.findByIdWithRelations(anyLong()))
                .thenReturn(Optional.empty());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, 999L, null);

        // Then
        assertNotNull(report);
        assertEquals(0, report.getTotalAccounts());
        assertTrue(report.getAccounts().isEmpty());
    }

    // ==================== Turnover Calculation Tests ====================

    @Test
    @DisplayName("Should calculate opening and closing balances correctly")
    void generateReport_ShouldCalculateBalances_Correctly() {
        // Given
        BigDecimal openingBalance = new BigDecimal("10000.00");
        List<BankAccountTransactionEvent> events = createTestEvents();

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(testAccount.getId(), startDate.minusDays(1)))
                .thenReturn(openingBalance);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(events);
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(normalEvent, reversedEvent));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));
        when(transactionService.calculateBalance(eq(testAccount.getId()), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(transactionService.calculateBalance(eq(account2.getId()), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateRange(eq(testAccount.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createTestEvents());
        when(transactionService.getAccountEventsInDateRange(eq(account2.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, usdAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(accountWithNulls));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createTestEvents());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));

        // Return null for opening balance (day before start)
        when(transactionService.calculateBalance(eq(testAccount.getId()), eq(startDate.minusDays(1))))
                .thenReturn(null);

        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));

        // Mock only opening balances (day before start)
        when(transactionService.calculateBalance(eq(testAccount.getId()), eq(startDate.minusDays(1))))
                .thenReturn(new BigDecimal("1000.00"));
        when(transactionService.calculateBalance(eq(account2.getId()), eq(startDate.minusDays(1))))
                .thenReturn(BigDecimal.ZERO);

        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // First account has debit transactions
        when(transactionService.getAccountEventsInDateRange(eq(testAccount.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(createEvent(TransactionType.DEBIT, new BigDecimal("500.00"), false)));

        // Second account has no transactions (will result in null/zero debit)
        when(transactionService.getAccountEventsInDateRange(eq(account2.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // First account has credit transactions
        when(transactionService.getAccountEventsInDateRange(eq(testAccount.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(createEvent(TransactionType.CREDIT, new BigDecimal("300.00"), false)));

        // Second account has no transactions (will result in null/zero credit)
        when(transactionService.getAccountEventsInDateRange(eq(account2.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

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

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));

        // First account has normal balance
        when(transactionService.calculateBalance(eq(testAccount.getId()), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // Second account opening balance is normal, but closing will be calculated as null
        when(transactionService.calculateBalance(eq(account2.getId()), eq(startDate.minusDays(1))))
                .thenReturn(new BigDecimal("500.00"));

        // Mock for closing balance calculation - we need to ensure getAccountEventsInDateRange returns empty
        when(transactionService.getAccountEventsInDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));

        // When
        AccountTurnoverReportDto report = reportService.generateReport(startDate, endDate, null, null, null);

        // Then
        Map<String, AccountTurnoverTotalDto> summary = report.getSummaryByCurrency();
        AccountTurnoverTotalDto uahTotal = summary.get("UAH");
        // Should handle mixed balances correctly
        assertNotNull(uahTotal.getTotalClosingBalance());
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
        event.setTransactionDate(startDate.plusDays(5));
        return event;
    }

    private BankAccount createSecondAccount() {
        BankAccount account = new BankAccount();
        account.setId(2L);
        account.setAccountNumber("UA987654321098765432109876543");
        account.setBank(testBank);
        account.setCurrency(testCurrency);
        account.setHolderId(testOrganization.getId());
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }
}