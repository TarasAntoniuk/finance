package com.tarasantoniuk.finance.banking.report.accountbalance.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceItemDto;
import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceReportDto;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
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
@DisplayName("AccountBalanceReportService Unit Tests")
class AccountBalanceReportServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountTransactionService transactionService;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private AccountBalanceReportService reportService;

    private BankAccount testAccount;
    private Organization testOrganization;
    private Currency testCurrency;
    private Bank testBank;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);

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

    // ==================== Generate Report Tests ====================

    @Test
    @DisplayName("Should generate report with all accounts when no filters")
    void generateReport_ShouldReturnAllAccounts_WhenNoFilters() {
        // Given
        List<BankAccount> accounts = Arrays.asList(testAccount);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(accounts);
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("50000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(testDate, report.getReportDate());
        assertEquals(1, report.getTotalAccounts());
        assertEquals(1, report.getAccounts().size());

        AccountBalanceItemDto item = report.getAccounts().get(0);
        assertEquals(testAccount.getId(), item.getAccountId());
        assertEquals(testAccount.getAccountNumber(), item.getAccountNumber());
        assertEquals("PrivatBank", item.getBankName());
        assertEquals("PBANUA2X", item.getBankSwiftCode());
        assertEquals("UAH", item.getCurrencyCode());
        assertEquals("₴", item.getCurrencySymbol());
        assertEquals("Test Organization", item.getOrganizationName());
        assertEquals(new BigDecimal("50000.00"), item.getBalance());

        verify(bankAccountRepository).findAllWithRelations();
        verify(transactionService).calculateBalance(testAccount.getId(), testDate);
    }

    @Test
    @DisplayName("Should filter by organization ID")
    void generateReport_ShouldFilterByOrganization_WhenOrganizationIdProvided() {
        // Given
        Long organizationId = 1L;
        when(bankAccountRepository.findByHolderIdWithRelations(organizationId))
                .thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, organizationId, null);

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
                .thenReturn(new BigDecimal("5000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, currencyId);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByCurrencyIdWithRelations(currencyId);
        verify(bankAccountRepository, never()).findAllWithRelations();
    }

    @Test
    @DisplayName("Should filter by both organization and currency")
    void generateReport_ShouldFilterByBoth_WhenBothIdsProvided() {
        // Given
        Long organizationId = 1L;
        Long currencyId = 1L;
        when(bankAccountRepository.findByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId))
                .thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("25000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, organizationId, currencyId);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        verify(bankAccountRepository).findByHolderIdAndCurrencyIdWithRelations(organizationId, currencyId);
        verify(bankAccountRepository, never()).findAllWithRelations();
    }

    @Test
    @DisplayName("Should use today's date when asOfDate is null")
    void generateReport_ShouldUseTodayDate_WhenAsOfDateIsNull() {
        // Given
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(null, null, null);

        // Then
        assertNotNull(report);
        assertEquals(LocalDate.now(), report.getReportDate());
    }

    @Test
    @DisplayName("Should return empty report when no accounts found")
    void generateReport_ShouldReturnEmptyReport_WhenNoAccounts() {
        // Given
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(0, report.getTotalAccounts());
        assertTrue(report.getAccounts().isEmpty());
        assertTrue(report.getGrandTotalByCurrency().isEmpty());
    }

    // ==================== Totals Calculation Tests ====================

    @Test
    @DisplayName("Should calculate totals by organization and currency")
    void generateReport_ShouldCalculateTotalsByOrganization() {
        // Given
        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        account2.setAccountNumber("UA987654321098765432109876543");
        account2.setBank(testBank);
        account2.setCurrency(testCurrency);
        account2.setHolderId(testOrganization.getId());
        account2.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));
        when(transactionService.calculateBalance(testAccount.getId(), testDate))
                .thenReturn(new BigDecimal("30000.00"));
        when(transactionService.calculateBalance(account2.getId(), testDate))
                .thenReturn(new BigDecimal("20000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(2, report.getTotalAccounts());

        Map<String, Map<String, BigDecimal>> totalsByOrg = report.getTotalsByOrganization();
        assertNotNull(totalsByOrg);
        assertTrue(totalsByOrg.containsKey("Test Organization"));
        assertEquals(new BigDecimal("50000.00"), totalsByOrg.get("Test Organization").get("UAH"));

        Map<String, BigDecimal> grandTotal = report.getGrandTotalByCurrency();
        assertEquals(new BigDecimal("50000.00"), grandTotal.get("UAH"));
    }

    @Test
    @DisplayName("Should calculate totals for multiple currencies")
    void generateReport_ShouldCalculateTotalsForMultipleCurrencies() {
        // Given
        Currency usd = new Currency();
        usd.setId(2L);
        usd.setCode("USD");
        usd.setSymbol("$");

        BankAccount usdAccount = new BankAccount();
        usdAccount.setId(2L);
        usdAccount.setAccountNumber("UA987654321098765432109876543");
        usdAccount.setBank(testBank);
        usdAccount.setCurrency(usd);
        usdAccount.setHolderId(testOrganization.getId());
        usdAccount.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, usdAccount));
        when(transactionService.calculateBalance(testAccount.getId(), testDate))
                .thenReturn(new BigDecimal("50000.00"));
        when(transactionService.calculateBalance(usdAccount.getId(), testDate))
                .thenReturn(new BigDecimal("1000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        Map<String, BigDecimal> grandTotal = report.getGrandTotalByCurrency();
        assertEquals(new BigDecimal("50000.00"), grandTotal.get("UAH"));
        assertEquals(new BigDecimal("1000.00"), grandTotal.get("USD"));
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle null balance from transaction service")
    void generateReport_ShouldHandleNullBalance() {
        // Given
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class))).thenReturn(null);
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getTotalAccounts());
        assertNull(report.getAccounts().get(0).getBalance());
    }

    @Test
    @DisplayName("Should handle organization not found")
    void generateReport_ShouldHandleOrganizationNotFound() {
        // Given
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals("Unknown", report.getAccounts().get(0).getOrganizationName());
    }

    @Test
    @DisplayName("Should find last transaction date")
    void generateReport_ShouldFindLastTransactionDate() {
        // Given
        BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
        event1.setTransactionDate(LocalDate.of(2024, 1, 10));

        BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
        event2.setTransactionDate(LocalDate.of(2024, 1, 5));

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong()))
                .thenReturn(Arrays.asList(event1, event2));

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(LocalDate.of(2024, 1, 10), report.getAccounts().get(0).getLastTransactionDate());
    }

    @Test
    @DisplayName("Should filter transactions by report date")
    void generateReport_ShouldFilterTransactionsByDate() {
        // Given
        BankAccountTransactionEvent beforeEvent = new BankAccountTransactionEvent();
        beforeEvent.setTransactionDate(LocalDate.of(2024, 1, 10));

        BankAccountTransactionEvent afterEvent = new BankAccountTransactionEvent();
        afterEvent.setTransactionDate(LocalDate.of(2024, 1, 20));

        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong()))
                .thenReturn(Arrays.asList(beforeEvent, afterEvent));

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        // Should only include transaction before or on testDate (2024-01-15)
        assertEquals(LocalDate.of(2024, 1, 10), report.getAccounts().get(0).getLastTransactionDate());
    }

    @Test
    @DisplayName("Should handle null Bank")
    void generateReport_ShouldHandleNullBank() {
        // Given
        testAccount.setBank(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        AccountBalanceItemDto item = report.getAccounts().get(0);
        assertNull(item.getBankName());
        assertNull(item.getBankSwiftCode());
    }

    @Test
    @DisplayName("Should handle null Currency")
    void generateReport_ShouldHandleNullCurrency() {
        // Given
        testAccount.setCurrency(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        AccountBalanceItemDto item = report.getAccounts().get(0);
        assertNull(item.getCurrencyCode());
        assertNull(item.getCurrencySymbol());
    }

    @Test
    @DisplayName("Should handle null Status")
    void generateReport_ShouldHandleNullStatus() {
        // Given
        testAccount.setStatus(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        AccountBalanceItemDto item = report.getAccounts().get(0);
        assertNull(item.getAccountStatus());
    }

    @Test
    @DisplayName("Should handle null organization ID")
    void generateReport_ShouldHandleNullOrganizationId() {
        // Given
        testAccount.setHolderId(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        AccountBalanceItemDto item = report.getAccounts().get(0);
        assertNull(item.getOrganizationName());
        assertNull(item.getOrganizationId());
    }

    @Test
    @DisplayName("Should handle null organization name in totals calculation")
    void generateReport_ShouldHandleNullOrganizationNameInTotals() {
        // Given
        testAccount.setHolderId(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        Map<String, Map<String, BigDecimal>> totalsByOrg = report.getTotalsByOrganization();
        assertTrue(totalsByOrg.containsKey("Unknown"));
        assertEquals(new BigDecimal("10000.00"), totalsByOrg.get("Unknown").get("UAH"));
    }

    @Test
    @DisplayName("Should handle null currency code in totals calculation")
    void generateReport_ShouldHandleNullCurrencyCodeInTotals() {
        // Given
        testAccount.setCurrency(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        Map<String, Map<String, BigDecimal>> totalsByOrg = report.getTotalsByOrganization();
        assertTrue(totalsByOrg.get("Test Organization").containsKey("UNKNOWN"));
        assertEquals(new BigDecimal("10000.00"), totalsByOrg.get("Test Organization").get("UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle null currency code in grand totals calculation")
    void generateReport_ShouldHandleNullCurrencyCodeInGrandTotals() {
        // Given
        testAccount.setCurrency(null);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(testAccount));
        when(transactionService.calculateBalance(anyLong(), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        Map<String, BigDecimal> grandTotal = report.getGrandTotalByCurrency();
        assertTrue(grandTotal.containsKey("UNKNOWN"));
        assertEquals(new BigDecimal("10000.00"), grandTotal.get("UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle null balance in totals calculation")
    void generateReport_ShouldHandleNullBalanceInTotalsCalculation() {
        // Given
        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        account2.setAccountNumber("UA987654321098765432109876543");
        account2.setBank(testBank);
        account2.setCurrency(testCurrency);
        account2.setHolderId(testOrganization.getId());
        account2.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findAllWithRelations())
                .thenReturn(Arrays.asList(testAccount, account2));
        when(transactionService.calculateBalance(testAccount.getId(), testDate))
                .thenReturn(new BigDecimal("30000.00"));
        when(transactionService.calculateBalance(account2.getId(), testDate))
                .thenReturn(null); // null balance
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(testOrganization));
        when(transactionService.getAccountEvents(anyLong())).thenReturn(Collections.emptyList());

        // When
        AccountBalanceReportDto report = reportService.generateReport(testDate, null, null);

        // Then
        assertNotNull(report);
        assertEquals(2, report.getTotalAccounts());

        Map<String, Map<String, BigDecimal>> totalsByOrg = report.getTotalsByOrganization();
        // Should sum correctly even with null balance (treated as 0)
        assertEquals(new BigDecimal("30000.00"), totalsByOrg.get("Test Organization").get("UAH"));

        Map<String, BigDecimal> grandTotal = report.getGrandTotalByCurrency();
        assertEquals(new BigDecimal("30000.00"), grandTotal.get("UAH"));
    }
}