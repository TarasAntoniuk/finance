package com.tarasantoniuk.finance.banking.report.accountbalance.controller;

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

@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AccountBalanceReport Controller Integration Tests")
class AccountBalanceReportControllerIntegrationTest extends BaseIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Create core entities
        country = factoryCore.createUkraine();
        uah = factoryCore.createUAH();
        usd = factoryCore.createUSD();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createCustomerCounterparty();

        // Create banking entities
        bank = factoryBanking.createPrivatBank(country);
        uahAccount = factoryBanking.createOrganizationBankAccount(bank, uah, organization);
        usdAccount = factoryBanking.createOrganizationBankAccount(bank, usd, organization);

        // Create initial balances
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

    // ==================== GET Account Balances Tests ====================

    @Test
    @DisplayName("Should return all account balances")
    void getAccountBalances_ShouldReturnAllAccounts() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.reportDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.totalAccounts").value(2))
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                .andExpect(jsonPath("$.accounts[*].accountId", containsInAnyOrder(
                        uahAccount.getId().intValue(),
                        usdAccount.getId().intValue()
                )))
                .andExpect(jsonPath("$.accounts[*].currencyCode", containsInAnyOrder("UAH", "USD")))
                .andExpect(jsonPath("$.grandTotalByCurrency.UAH").value(50000.00))
                .andExpect(jsonPath("$.grandTotalByCurrency.USD").value(1000.00));
    }

    @Test
    @DisplayName("Should return balances as of specific date")
    void getAccountBalances_ShouldReturnBalancesAsOfDate() throws Exception {
        // Given
        LocalDate asOfDate = LocalDate.now().minusDays(1);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("asOfDate", asOfDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportDate").value(asOfDate.toString()))
                .andExpect(jsonPath("$.totalAccounts").value(2));
    }

    @Test
    @DisplayName("Should filter by organization ID")
    void getAccountBalances_ShouldFilterByOrganization() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("organizationId", organization.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(2))
                .andExpect(jsonPath("$.accounts[*].organizationId", everyItem(is(organization.getId().intValue()))))
                .andExpect(jsonPath("$.accounts[*].organizationName", everyItem(is(organization.getName()))));
    }

    @Test
    @DisplayName("Should filter by currency ID")
    void getAccountBalances_ShouldFilterByCurrency() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("currencyId", uah.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("UAH"))
                .andExpect(jsonPath("$.accounts[0].balance").value(50000.00))
                .andExpect(jsonPath("$.grandTotalByCurrency.UAH").value(50000.00))
                .andExpect(jsonPath("$.grandTotalByCurrency.USD").doesNotExist());
    }

    @Test
    @DisplayName("Should filter by both organization and currency")
    void getAccountBalances_ShouldFilterByBoth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("organizationId", organization.getId().toString())
                        .param("currencyId", usd.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(1))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$.accounts[0].balance").value(1000.00));
    }

    @Test
    @DisplayName("Should return empty report when no accounts match filters")
    void getAccountBalances_ShouldReturnEmpty_WhenNoMatches() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("organizationId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(0))
                .andExpect(jsonPath("$.accounts", hasSize(0)))
                .andExpect(jsonPath("$.grandTotalByCurrency").isEmpty());
    }

    @Test
    @DisplayName("Should include account details in response")
    void getAccountBalances_ShouldIncludeAccountDetails() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountId").exists())
                .andExpect(jsonPath("$.accounts[0].accountNumber").exists())
                .andExpect(jsonPath("$.accounts[0].bankName").value("PrivatBank"))
                .andExpect(jsonPath("$.accounts[0].bankSwiftCode").value("PBANUA2X"))
                .andExpect(jsonPath("$.accounts[0].currencySymbol").exists())
                .andExpect(jsonPath("$.accounts[0].organizationName").value(organization.getName()))
                .andExpect(jsonPath("$.accounts[0].balance").exists())
                .andExpect(jsonPath("$.accounts[0].accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should calculate totals by organization correctly")
    void getAccountBalances_ShouldCalculateTotalsByOrganization() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalsByOrganization").exists())
                .andExpect(jsonPath("$.totalsByOrganization.['" + organization.getName() + "'].UAH").value(50000.00))
                .andExpect(jsonPath("$.totalsByOrganization.['" + organization.getName() + "'].USD").value(1000.00));
    }


    @Test
    @DisplayName("Should show zero balance for account without transactions")
    void getAccountBalances_ShouldShowZeroBalance_WhenNoTransactions() throws Exception {
        // Given
        BankAccount emptyAccount = factoryBanking.createOrganizationBankAccount(bank, uah, organization);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(3))
                .andExpect(jsonPath("$.accounts[?(@.accountId == " + emptyAccount.getId() + ")].balance").value(0)); // Без .0
    }

    @Test
    @DisplayName("Should include last transaction date when transactions exist")
    void getAccountBalances_ShouldIncludeLastTransactionDate() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].lastTransactionDate").exists());
    }

    @Test
    @DisplayName("Should handle multiple organizations correctly")
    void getAccountBalances_ShouldHandleMultipleOrganizations() throws Exception {
        // Given - create second organization with account
        Organization org2 = factoryCore.createOrganization("Second Org", "12345678", country);
        BankAccount org2Account = factoryBanking.createOrganizationBankAccount(bank, uah, org2);
        factoryBanking.createInitialBalance(org2Account, counterparty, null, uah, org2, new BigDecimal("30000.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAccounts").value(3))
                .andExpect(jsonPath("$.totalsByOrganization").exists())
                .andExpect(jsonPath("$.totalsByOrganization.['" + organization.getName() + "']").exists())
                .andExpect(jsonPath("$.totalsByOrganization.['Second Org']").exists())
                .andExpect(jsonPath("$.grandTotalByCurrency.UAH").value(80000.00)); // 50000 + 30000
    }

    @Test
    @DisplayName("Should handle invalid date format")
    void getAccountBalances_ShouldReturnBadRequest_WhenInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("asOfDate", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle future date")
    void getAccountBalances_ShouldAcceptFutureDate() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(30);

        // When & Then
        mockMvc.perform(get("/api/v1/banking/reports/account-balances")
                        .param("asOfDate", futureDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportDate").value(futureDate.toString()));
    }
}