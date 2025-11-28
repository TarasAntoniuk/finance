package com.tarasantoniuk.finance.banking.report.accountturnover.controller;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;

import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("AccountTurnoverReport Controller Integration Tests")
class AccountTurnoverReportControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    private Country country;
    private Organization organization;
    private Currency uah;
    private Currency usd;
    private Bank bank;
    private Counterparty counterparty;
    private BankAccount uahAccount;
    private BankAccount usdAccount;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @BeforeEach
    void setUp() {
        periodStart = LocalDate.now().minusDays(30);
        periodEnd = LocalDate.now();

        // Create core entities
        country = factoryCore.createUkraine();
        uah = factoryCore.createUAH();
        usd = factoryCore.createUSD();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createSupplierCounterparty();

        // Create banking entities
        bank = factoryBanking.createPrivatBank(country);
        uahAccount = factoryBanking.createOrganizationBankAccount(bank, uah, organization);
        usdAccount = factoryBanking.createOrganizationBankAccount(bank, usd, organization);

        // Create initial balances (posted receipts)
        factoryBanking.createInitialBalance(
                uahAccount,
                counterparty,
                null,
                uah,
                organization,
                new BigDecimal("50000.00")
        );

        factoryBanking.createInitialBalance(
                usdAccount,
                counterparty,
                null,
                usd,
                organization,
                new BigDecimal("1000.00")
        );
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    // ==================== VALIDATION Tests ====================

    @Test
    @DisplayName("Should return bad request when startDate is missing")
    void getAccountTurnovers_ShouldReturnBadRequest_WhenStartDateMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when endDate is missing")
    void getAccountTurnovers_ShouldReturnBadRequest_WhenEndDateMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when startDate after endDate")
    void getAccountTurnovers_ShouldReturnBadRequest_WhenStartDateAfterEndDate() throws Exception {
        // Given
        LocalDate invalidStart = LocalDate.now();
        LocalDate invalidEnd = LocalDate.now().minusDays(10);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", invalidStart.toString())
                        .param("endDate", invalidEnd.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("Should return bad request when period exceeds 365 days")
    void getAccountTurnovers_ShouldReturnBadRequest_WhenPeriodExceeds365Days() throws Exception {
        // Given
        LocalDate longStart = LocalDate.of(2024, 1, 1);
        LocalDate longEnd = LocalDate.of(2025, 1, 2); // 367 days

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", longStart.toString())
                        .param("endDate", longEnd.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Period cannot exceed 365 days")));
    }

    @Test
    @DisplayName("Should accept period of exactly 365 days")
    void getAccountTurnovers_ShouldAccept_When365Days() throws Exception {
        // Given
        LocalDate yearStart = LocalDate.of(2024, 1, 1);
        LocalDate yearEnd = LocalDate.of(2024, 12, 31); // Exactly 365 days

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", yearStart.toString())
                        .param("endDate", yearEnd.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle invalid date format")
    void getAccountTurnovers_ShouldReturnBadRequest_WhenInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", "invalid-date")
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isBadRequest());
    }

    // ==================== GENERATE REPORT Tests ====================

    @Test
    @DisplayName("Should generate report with all accounts when no filters")
    void getAccountTurnovers_ShouldReturnAllAccounts_WhenNoFilters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.period.startDate").value(periodStart.toString()))
                .andExpect(jsonPath("$.period.endDate").value(periodEnd.toString()))
                .andExpect(jsonPath("$.period.daysCount").exists())
                .andExpect(jsonPath("$.totalAccounts").value(2))
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                .andExpect(jsonPath("$.accounts[*].accountId", containsInAnyOrder(
                        uahAccount.getId().intValue(),
                        usdAccount.getId().intValue()
                )))
                .andExpect(jsonPath("$.accounts[*].currencyCode", containsInAnyOrder("UAH", "USD")));
    }

    @Test
    @DisplayName("Should include account turnover details")
    void getAccountTurnovers_ShouldIncludeDetails() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountId").exists())
                .andExpect(jsonPath("$.accounts[0].accountNumber").exists())
                .andExpect(jsonPath("$.accounts[0].bankName").value("PrivatBank"))
                .andExpect(jsonPath("$.accounts[0].bankSwiftCode").value("PBANUA2X"))
                .andExpect(jsonPath("$.accounts[0].currencySymbol").exists())
                .andExpect(jsonPath("$.accounts[0].organizationName").value(organization.getName()))
                .andExpect(jsonPath("$.accounts[0].openingBalance").exists())
                .andExpect(jsonPath("$.accounts[0].debitTurnover").exists())
                .andExpect(jsonPath("$.accounts[0].creditTurnover").exists())
                .andExpect(jsonPath("$.accounts[0].closingBalance").exists())
                .andExpect(jsonPath("$.accounts[0].transactionCount").exists())
                .andExpect(jsonPath("$.accounts[0].accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should calculate opening and closing balances correctly")
    void getAccountTurnovers_ShouldCalculateBalances() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].openingBalance").value(0))
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].debitTurnover").value(50000.00))
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].creditTurnover").value(0))
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].closingBalance").value(50000.00))
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].transactionCount").value(1));
    }

    @Test
    @DisplayName("Should calculate summary by currency")
    void getAccountTurnovers_ShouldCalculateSummaryByCurrency() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaryByCurrency").exists())
                .andExpect(jsonPath("$.summaryByCurrency.UAH").exists())
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalOpeningBalance").value(0))
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalDebitTurnover").value(50000.00))
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalCreditTurnover").value(0))
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalClosingBalance").value(50000.00))
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalTransactionCount").value(1))
                .andExpect(jsonPath("$.summaryByCurrency.USD").exists())
                .andExpect(jsonPath("$.summaryByCurrency.USD.totalDebitTurnover").value(1000.00));
    }

    // ==================== FILTER Tests ====================

    @Test
    @DisplayName("Should filter by organization ID")
    void getAccountTurnovers_ShouldFilterByOrganization() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString())
                        .param("organizationId", organization.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(2))
                .andExpect(jsonPath("$.accounts[*].organizationId", everyItem(is(organization.getId().intValue()))));
    }

    @Test
    @DisplayName("Should filter by currency ID")
    void getAccountTurnovers_ShouldFilterByCurrency() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString())
                        .param("currencyId", uah.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("UAH"))
                .andExpect(jsonPath("$.summaryByCurrency.UAH").exists())
                .andExpect(jsonPath("$.summaryByCurrency.USD").doesNotExist());
    }

    @Test
    @DisplayName("Should filter by specific account ID")
    void getAccountTurnovers_ShouldFilterByAccount() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString())
                        .param("accountId", uahAccount.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.accounts[0].accountId").value(uahAccount.getId()))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("UAH"));
    }

    @Test
    @DisplayName("Should filter by both organization and currency")
    void getAccountTurnovers_ShouldFilterByBoth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString())
                        .param("organizationId", organization.getId().toString())
                        .param("currencyId", usd.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("USD"));
    }

    @Test
    @DisplayName("Should return empty report when no accounts match filters")
    void getAccountTurnovers_ShouldReturnEmpty_WhenNoMatches() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString())
                        .param("organizationId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(0))
                .andExpect(jsonPath("$.accounts", hasSize(0)))
                .andExpect(jsonPath("$.summaryByCurrency").isEmpty());
    }

    // ==================== EDGE CASES Tests ====================

    @Test
    @DisplayName("Should show zero turnovers for account without transactions in period")
    void getAccountTurnovers_ShouldShowZeroTurnovers_WhenNoTransactionsInPeriod() throws Exception {
        // Given - create account without transactions
        BankAccount emptyAccount = factoryBanking.createOrganizationBankAccount(bank, uah, organization);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(3))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].openingBalance").value(0))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].debitTurnover").value(0))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].creditTurnover").value(0))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].closingBalance").value(0))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].transactionCount").value(0));
    }

    @Test
    @DisplayName("Should handle multiple organizations correctly")
    void getAccountTurnovers_ShouldHandleMultipleOrganizations() throws Exception {
        // Given
        Organization org2 = factoryCore.createOrganization("Second Org", "12345678", country);
        BankAccount org2Account = factoryBanking.createOrganizationBankAccount(bank, uah, org2);
        factoryBanking.createInitialBalance(org2Account, counterparty, null, uah, org2, new BigDecimal("30000.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", periodStart.toString())
                        .param("endDate", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(3))
                .andExpect(jsonPath("$.summaryByCurrency.UAH.totalDebitTurnover").value(80000.00)); // 50000 + 30000
    }

    @Test
    @DisplayName("Should handle period with transactions outside range")
    void getAccountTurnovers_ShouldExcludeTransactionsOutsideRange() throws Exception {
        // Given - period that excludes today's transactions
        LocalDate oldStart = LocalDate.now().minusDays(60);
        LocalDate oldEnd = LocalDate.now().minusDays(31);

        // When & Then - should have zero turnovers since all transactions are today
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", oldStart.toString())
                        .param("endDate", oldEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].debitTurnover").value(0))
                .andExpect(jsonPath("$.accounts[?(@.currencyCode == 'UAH')].transactionCount").value(0));
    }

    @Test
    @DisplayName("Should handle single day period")
    void getAccountTurnovers_ShouldHandleSingleDay() throws Exception {
        // Given
        LocalDate today = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", today.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.daysCount").value(1))
                .andExpect(jsonPath("$.totalAccounts").value(2));
    }

    @Test
    @DisplayName("Should calculate period days count correctly")
    void getAccountTurnovers_ShouldCalculateDaysCount() throws Exception {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-turnovers")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.daysCount").value(31)); // January has 31 days
    }
}