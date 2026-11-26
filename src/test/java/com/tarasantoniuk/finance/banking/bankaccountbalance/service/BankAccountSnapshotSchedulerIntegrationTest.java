package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.snapshot.repository.SnapshotValidityRepository;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for bank account snapshot validity feature.
 * Tests the complete workflow with real services and database.
 */
/**
 * Runs the nightly jobs against PostgreSQL without a test-managed transaction,
 * so commit and rollback behave as they do in production.
 */
@SpringBootTest
@DisplayName("BankAccountSnapshotScheduler transaction behaviour")
class BankAccountSnapshotSchedulerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BankAccountSnapshotScheduler scheduler;

    @Autowired
    private BankAccountBalanceService balanceService;

    @Autowired
    private BankReceiptService bankReceiptService;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;

    private Currency currency;
    private Organization organization;
    private Counterparty counterparty;
    private Bank bank;

    @BeforeEach
    void setUp() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createCustomerCounterparty();
        bank = factoryBanking.createPrivatBank(country);
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @Test
    @DisplayName("A counterparty-held account should neither get a snapshot nor break the run")
    void counterpartyAccountShouldNotBreakNightlyRun() {
        BankAccount organizationAccount = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
        BankAccount counterpartyAccount = factoryBanking.createCounterpartyBankAccount(bank, currency, counterparty);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        scheduler.createDailySnapshots();

        assertThat(balanceService.hasSnapshotForDay(organizationAccount.getId(), yesterday)).isTrue();
        assertThat(balanceService.hasSnapshotForDay(counterpartyAccount.getId(), yesterday)).isFalse();
    }

    @Test
    @DisplayName("An existing snapshot for one account should not roll back the others")
    void existingSnapshotShouldNotRollBackOtherAccounts() {
        BankAccount first = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
        BankAccount second = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        balanceService.createSnapshot(first.getId(), yesterday.atStartOfDay());

        scheduler.createDailySnapshots();

        assertThat(balanceService.hasSnapshotForDay(second.getId(), yesterday)).isTrue();
    }

    @Test
    @DisplayName("Recalculation should rebuild the snapshot of the backdated day itself")
    void recalculationShouldRebuildBackdatedDaySnapshot() {
        BankAccount account = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
        LocalDate backdatedDay = LocalDate.now().minusDays(3);
        for (int i = 5; i >= 1; i--) {
            balanceService.createSnapshot(account.getId(), LocalDate.now().minusDays(i).atStartOfDay());
        }
        BankReceipt receipt = factoryBanking.createBankReceipt(
                account, counterparty, currency, organization, new BigDecimal("250.00"));
        receipt.setTransactionDateTime(backdatedDay.atTime(10, 0));
        bankReceiptRepository.save(receipt);
        bankReceiptService.post(receipt.getId());

        scheduler.recalculateInvalidSnapshots();

        assertThat(balanceService.hasSnapshotForDay(account.getId(), backdatedDay)).isTrue();
        assertThat(balanceService.getSnapshot(account.getId(), backdatedDay.plusDays(1).atStartOfDay())
                .getClosingBalance()).isEqualByComparingTo("250.00");
    }
}
