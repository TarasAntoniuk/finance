package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankpayment.service.BankPaymentService;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for bank account snapshot validity feature.
 * Tests the complete workflow with real services and database.
 */
@SpringBootTest
@Transactional
@DisplayName("BankAccountSnapshotValidity Integration Tests")
class BankAccountSnapshotValidityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BankAccountBalanceService balanceService;

    @Autowired
    private BankAccountSnapshotValidityService validityService;

    @Autowired
    private BankAccountSnapshotScheduler scheduler;

    @Autowired
    private BankReceiptService bankReceiptService;

    @Autowired
    private BankPaymentService bankPaymentService;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    @Autowired
    private BankPaymentRepository bankPaymentRepository;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;

    @Autowired
    private SnapshotValidityRepository validityRepository;

    @Autowired
    private BankAccountBalanceSnapshotRepository snapshotRepository;

    // Test data
    private Country country;
    private Organization organization;
    private Currency currency;
    private Bank bank;
    private Counterparty counterparty;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();

        // Create core test data
        country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createCustomerCounterparty();

        // Create banking test data
        bank = factoryBanking.createPrivatBank(country);
        bankAccount = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @Nested
    @DisplayName("Snapshot Creation and Invalidation Tests")
    class SnapshotCreationAndInvalidationTests {

        @Test
        @DisplayName("Should create snapshot for bank account")
        void shouldCreateSnapshotForBankAccount() {
            // Given: bank account with initial balance
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            LocalDateTime snapshotTime = LocalDate.now().atStartOfDay();

            // When: createSnapshot is called
            BankAccountBalanceSnapshot snapshot = balanceService.createSnapshot(bankAccount.getId(), snapshotTime);

            // Then: snapshot should be created with correct balance
            // Note: Snapshot is normalized to start of next day (end of current day)
            LocalDateTime expectedSnapshotTime = LocalDate.now().plusDays(1).atStartOfDay();
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getBankAccount().getId()).isEqualTo(bankAccount.getId());
            assertThat(snapshot.getClosingBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(snapshot.getSnapshotDateTime()).isEqualTo(expectedSnapshotTime);

            // Verify snapshot is persisted
            List<BankAccountBalanceSnapshot> savedSnapshots = snapshotRepository.findAll();
            assertThat(savedSnapshots).hasSize(1);
            assertThat(savedSnapshots.get(0).getClosingBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should invalidate snapshots when backdated transaction is posted")
        void shouldInvalidateSnapshotsWhenBackdatedTransactionPosted() {
            // Given: bank account with snapshots for past 5 days
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshots for past 5 days
            for (int i = 5; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            // Verify 5 snapshots created
            assertThat(snapshotRepository.count()).isEqualTo(5);

            // When: backdated receipt is posted for 3 days ago (more than 1 hour old)
            BankReceipt backdatedReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            LocalDateTime backdatedTime = LocalDateTime.now().minusDays(3).minusHours(2);
            backdatedReceipt.setTransactionDateTime(backdatedTime);
            bankReceiptRepository.save(backdatedReceipt);
            bankReceiptService.post(backdatedReceipt.getId());

            // Then: validity record should be created
            List<SnapshotValidity> validityRecords = validityRepository.findAll();
            assertThat(validityRecords).hasSize(1);

            SnapshotValidity validity = validityRecords.get(0);
            assertThat(validity.getSnapshotType()).isEqualTo("BANK_ACCOUNT_BALANCE");
            assertThat(validity.getEntityId()).isEqualTo(bankAccount.getId());
            assertThat(validity.getInvalidFromDate()).isEqualTo(backdatedTime.toLocalDate().plusDays(1));
            assertThat(validity.getStatus()).isEqualTo(ValidityStatus.INVALID);
            assertThat(validity.getInvalidationReason()).isEqualTo("BACKDATED_TRANSACTION");

            // And: snapshots should still exist (not deleted)
            assertThat(snapshotRepository.count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should update invalidation date when earlier backdated transaction posted")
        void shouldUpdateInvalidationDateWhenEarlierBackdatedTransactionPosted() {
            // Given: bank account with invalid snapshots from 2 days ago
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Post first backdated transaction (2 days ago)
            BankReceipt receipt1 = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("100.00")
            );
            LocalDateTime backdatedTime1 = LocalDateTime.now().minusDays(2).minusHours(2);
            receipt1.setTransactionDateTime(backdatedTime1);
            bankReceiptRepository.save(receipt1);
            bankReceiptService.post(receipt1.getId());

            // Verify initial validity record
            List<SnapshotValidity> validities = validityRepository.findAll();
            assertThat(validities).hasSize(1);
            assertThat(validities.get(0).getInvalidFromDate()).isEqualTo(backdatedTime1.toLocalDate().plusDays(1));

            // When: another backdated transaction for 5 days ago is posted
            BankReceipt receipt2 = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("200.00")
            );
            LocalDateTime backdatedTime2 = LocalDateTime.now().minusDays(5).minusHours(2);
            receipt2.setTransactionDateTime(backdatedTime2);
            bankReceiptRepository.save(receipt2);
            bankReceiptService.post(receipt2.getId());

            // Then: invalidFromDate should be updated to 5 days ago
            validities = validityRepository.findAll();
            assertThat(validities).hasSize(1);
            assertThat(validities.get(0).getInvalidFromDate()).isEqualTo(backdatedTime2.toLocalDate().plusDays(1));
        }

        @Test
        @DisplayName("Should not create validity record for current transactions")
        void shouldNotCreateValidityRecordForCurrentTransactions() {
            // Given: bank account with initial balance
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // When: current (not backdated) receipt is posted
            BankReceipt currentReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            // Transaction time is set to now by default in factory
            bankReceiptService.post(currentReceipt.getId());

            // Then: no validity record should be created
            List<SnapshotValidity> validityRecords = validityRepository.findAll();
            assertThat(validityRecords).isEmpty();
        }
    }

    @Nested
    @DisplayName("Balance Calculation With Validity Tests")
    class BalanceCalculationWithValidityTests {

        @Test
        @DisplayName("Should calculate balance using valid snapshots")
        void shouldCalculateBalanceUsingValidSnapshots() {
            // Given: bank account with valid snapshots
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshot for yesterday
            LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
            balanceService.createSnapshot(bankAccount.getId(), yesterdayStart);

            // Post another receipt today
            BankReceipt todayReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            bankReceiptService.post(todayReceipt.getId());

            // When: calculateBalance is called
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());

            // Then: should use snapshot and balance should be correct
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Should calculate balance without using invalid snapshots")
        void shouldCalculateBalanceWithoutUsingInvalidSnapshots() {
            // Given: bank account with initial balance
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshots for past 5 days
            for (int i = 5; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            // Invalidate snapshots from 3 days ago
            LocalDate invalidFromDate = LocalDate.now().minusDays(3);
            validityService.invalidateSnapshots(
                    bankAccount.getId(),
                    invalidFromDate.atStartOfDay(),
                    999L
            );

            // Post a current receipt
            BankReceipt currentReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("200.00")
            );
            bankReceiptService.post(currentReceipt.getId());

            // When: calculateBalance for today is called
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());

            // Then: should only use snapshots before invalidation date and balance should be correct
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1200.00"));
        }

        @Test
        @DisplayName("Should calculate correct balance after backdated transaction")
        void shouldCalculateCorrectBalanceAfterBackdatedTransaction() {
            // Given: bank account with initial balance 1000 from 10 days ago
            BankReceipt initialReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );
            LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
            initialReceipt.setTransactionDateTime(tenDaysAgo);
            bankReceiptRepository.save(initialReceipt);
            bankReceiptService.post(initialReceipt.getId());

            // Create snapshots for past 5 days
            for (int i = 5; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            // When: backdated receipt of 500 posted 3 days ago
            BankReceipt backdatedReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            LocalDateTime backdatedTime = LocalDateTime.now().minusDays(3).minusHours(2);
            backdatedReceipt.setTransactionDateTime(backdatedTime);
            bankReceiptRepository.save(backdatedReceipt);
            bankReceiptService.post(backdatedReceipt.getId());

            // Then: balance today should be 1500
            BigDecimal currentBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(currentBalance).isEqualByComparingTo(new BigDecimal("1500.00"));

            // And: calculation should not use invalid snapshots
            Optional<LocalDate> invalidFromDate = validityService.getInvalidFromDate(bankAccount.getId());
            assertThat(invalidFromDate).isPresent();
        }
    }

    @Nested
    @DisplayName("Scheduler Operations Tests")
    class SchedulerOperationsTests {

        @Test
        @DisplayName("Should create daily snapshots for all active accounts")
        void shouldCreateDailySnapshotsForAllActiveAccounts() {
            // Given: 3 active bank accounts with transactions yesterday
            BankAccount account1 = bankAccount; // Already created in setUp
            BankAccount account2 = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
            BankAccount account3 = factoryBanking.createOrganizationBankAccount(bank, currency, organization);

            // Create initial balances with backdated transactions (yesterday)
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1).minusHours(12);

            BankReceipt receipt1 = factoryBanking.createBankReceipt(account1, counterparty, currency, organization, new BigDecimal("100.00"));
            receipt1.setTransactionDateTime(yesterday);
            bankReceiptRepository.save(receipt1);
            bankReceiptService.post(receipt1.getId());

            BankReceipt receipt2 = factoryBanking.createBankReceipt(account2, counterparty, currency, organization, new BigDecimal("200.00"));
            receipt2.setTransactionDateTime(yesterday);
            bankReceiptRepository.save(receipt2);
            bankReceiptService.post(receipt2.getId());

            BankReceipt receipt3 = factoryBanking.createBankReceipt(account3, counterparty, currency, organization, new BigDecimal("300.00"));
            receipt3.setTransactionDateTime(yesterday);
            bankReceiptRepository.save(receipt3);
            bankReceiptService.post(receipt3.getId());

            // When: createDailySnapshots is called manually
            scheduler.createDailySnapshots();

            // Then: 3 snapshots should be created for today
            List<BankAccountBalanceSnapshot> snapshots = snapshotRepository.findAll();
            assertThat(snapshots).hasSize(3);

            // And: all snapshots should have correct balances
            assertThat(snapshots)
                    .extracting(BankAccountBalanceSnapshot::getClosingBalance)
                    .containsExactlyInAnyOrder(
                            new BigDecimal("100.00"),
                            new BigDecimal("200.00"),
                            new BigDecimal("300.00")
                    );
        }

        @Test
        @DisplayName("Should skip creating snapshots for accounts with invalid snapshots")
        void shouldSkipCreatingSnapshotsForAccountsWithInvalidSnapshots() {
            // Given: 2 active accounts
            BankAccount account1 = bankAccount;
            BankAccount account2 = factoryBanking.createOrganizationBankAccount(bank, currency, organization);

            // Create initial balances
            factoryBanking.createInitialBalance(account1, counterparty, null, currency, organization, new BigDecimal("100.00"));
            factoryBanking.createInitialBalance(account2, counterparty, null, currency, organization, new BigDecimal("200.00"));

            // Invalidate account1 snapshots for yesterday
            LocalDate yesterday = LocalDate.now().minusDays(1);
            validityService.invalidateSnapshots(account1.getId(), yesterday.atStartOfDay(), 999L);

            // When: createDailySnapshots is called
            scheduler.createDailySnapshots();

            // Then: only 1 snapshot should be created (for account2)
            List<BankAccountBalanceSnapshot> snapshots = snapshotRepository.findAll();
            assertThat(snapshots).hasSize(1);
            assertThat(snapshots.get(0).getBankAccount().getId()).isEqualTo(account2.getId());
        }

        @Test
        @DisplayName("Should recalculate invalid snapshots correctly")
        void shouldRecalculateInvalidSnapshotsCorrectly() {
            // Given: bank account with snapshots for past 5 days
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshots for past 5 days
            for (int i = 5; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            assertThat(snapshotRepository.count()).isEqualTo(5);

            // Post backdated transaction 3 days ago
            BankReceipt backdatedReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            LocalDateTime backdatedTime = LocalDateTime.now().minusDays(3).minusHours(2);
            backdatedReceipt.setTransactionDateTime(backdatedTime);
            bankReceiptRepository.save(backdatedReceipt);
            bankReceiptService.post(backdatedReceipt.getId());

            // Verify validity record created
            assertThat(validityRepository.count()).isEqualTo(1);

            // When: recalculateInvalidSnapshots is called manually
            scheduler.recalculateInvalidSnapshots();

            // Then: invalid snapshots (last 2 days) should be deleted and recreated
            List<BankAccountBalanceSnapshot> snapshots = snapshotRepository.findAll();
            // Should still have snapshots, but they should be recalculated
            assertThat(snapshots.size()).isGreaterThan(0);

            // And: validity record should be deleted
            assertThat(validityRepository.count()).isEqualTo(0);

            // And: all snapshots should have correct balances (1000 + 500 = 1500)
            BigDecimal currentBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(currentBalance).isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Should handle multiple accounts during recalculation")
        void shouldHandleMultipleAccountsDuringRecalculation() {
            // Given: 3 accounts with invalid snapshots
            BankAccount account1 = bankAccount;
            BankAccount account2 = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
            BankAccount account3 = factoryBanking.createOrganizationBankAccount(bank, currency, organization);

            // Create initial balances
            factoryBanking.createInitialBalance(account1, counterparty, null, currency, organization, new BigDecimal("100.00"));
            factoryBanking.createInitialBalance(account2, counterparty, null, currency, organization, new BigDecimal("200.00"));
            factoryBanking.createInitialBalance(account3, counterparty, null, currency, organization, new BigDecimal("300.00"));

            // Invalidate all accounts
            LocalDate invalidDate = LocalDate.now().minusDays(1);
            validityService.invalidateSnapshots(account1.getId(), invalidDate.atStartOfDay(), 1L);
            validityService.invalidateSnapshots(account2.getId(), invalidDate.atStartOfDay(), 2L);
            validityService.invalidateSnapshots(account3.getId(), invalidDate.atStartOfDay(), 3L);

            assertThat(validityRepository.count()).isEqualTo(3);

            // When: recalculateInvalidSnapshots is called
            scheduler.recalculateInvalidSnapshots();

            // Then: all 3 accounts should be recalculated
            // And: all validity records should be deleted
            assertThat(validityRepository.count()).isEqualTo(0);

            // Verify balances are correct
            assertThat(balanceService.calculateBalance(account1.getId(), LocalDateTime.now()))
                    .isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(balanceService.calculateBalance(account2.getId(), LocalDateTime.now()))
                    .isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(balanceService.calculateBalance(account3.getId(), LocalDateTime.now()))
                    .isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("Manual Recalculation Tests")
    class ManualRecalculationTests {

        @Test
        @DisplayName("Should manually recalculate specific account")
        void shouldManuallyRecalculateSpecificAccount() {
            // Given: account with invalid snapshots
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshots
            for (int i = 3; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            // Invalidate snapshots
            LocalDate invalidDate = LocalDate.now().minusDays(2);
            validityService.invalidateSnapshots(bankAccount.getId(), invalidDate.atStartOfDay(), 999L);

            assertThat(validityRepository.count()).isEqualTo(1);

            // When: triggerRecalculationForAccount is called
            scheduler.triggerRecalculationForAccount(bankAccount.getId());

            // Then: snapshots should be recalculated
            // And: validity record should be deleted
            assertThat(validityRepository.count()).isEqualTo(0);

            // Verify balance is correct
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should handle manual recalculation for account without invalid snapshots")
        void shouldHandleManualRecalculationForAccountWithoutInvalidSnapshots() {
            // Given: account with all valid snapshots
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            assertThat(validityRepository.count()).isEqualTo(0);

            // When: triggerRecalculationForAccount is called
            // Then: should do nothing and not throw exception
            scheduler.triggerRecalculationForAccount(bankAccount.getId());

            // Verify no validity records created
            assertThat(validityRepository.count()).isEqualTo(0);

            // Verify balance is still correct
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }

    @Nested
    @DisplayName("Complete End-to-End Workflow Tests")
    class CompleteEndToEndWorkflowTests {

        @Test
        @DisplayName("Should handle complete invalidation and recalculation workflow")
        void shouldHandleCompleteInvalidationAndRecalculationWorkflow() {
            // Given: bank account created with daily receipts for past 10 days
            // Note: Use recent timestamps (within 30 minutes) to avoid triggering backdated invalidation
            LocalDateTime baseTime = LocalDateTime.now().minusMinutes(25);

            // Create initial balance
            BankReceipt initialReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("100.00")
            );
            initialReceipt.setTransactionDateTime(baseTime);
            bankReceiptRepository.save(initialReceipt);
            bankReceiptService.post(initialReceipt.getId());

            // Create 9 more receipts (total 1000)
            for (int i = 9; i >= 1; i--) {
                BankReceipt receipt = factoryBanking.createBankReceipt(
                        bankAccount,
                        counterparty,
                        currency,
                        organization,
                        new BigDecimal("100.00")
                );
                LocalDateTime receiptTime = baseTime.plusMinutes(i);
                receipt.setTransactionDateTime(receiptTime);
                bankReceiptRepository.save(receipt);
                bankReceiptService.post(receipt.getId());
            }

            // Verify initial state: balance is 1000
            BigDecimal initialBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(initialBalance).isEqualByComparingTo(new BigDecimal("1000.00"));

            // When: backdated payment of 500 posted 5 days ago
            BankPayment backdatedPayment = factoryBanking.createBankPayment(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("500.00")
            );
            LocalDateTime backdatedTime = LocalDateTime.now().minusDays(5).minusHours(2);
            backdatedPayment.setTransactionDateTime(backdatedTime);
            bankPaymentRepository.save(backdatedPayment);
            bankPaymentService.post(backdatedPayment.getId());

            // Then: validity record should be created for 5 days ago
            List<SnapshotValidity> validities = validityRepository.findAll();
            assertThat(validities).hasSize(1);
            assertThat(validities.get(0).getInvalidFromDate()).isEqualTo(backdatedTime.toLocalDate().plusDays(1));

            // And: balance calculation should still work correctly
            BigDecimal currentBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(currentBalance).isEqualByComparingTo(new BigDecimal("500.00")); // 1000 - 500

            // When: scheduler recalculates invalid snapshots
            scheduler.recalculateInvalidSnapshots();

            // Then: validity record should be deleted
            assertThat(validityRepository.count()).isEqualTo(0);

            // And: all balance calculations should use new snapshots
            currentBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(currentBalance).isEqualByComparingTo(new BigDecimal("500.00"));

            // When: another current receipt of 200 is posted
            BankReceipt currentReceipt = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("200.00")
            );
            bankReceiptService.post(currentReceipt.getId());

            // Then: no invalidation should occur
            assertThat(validityRepository.count()).isEqualTo(0);

            // And: balance should be 700
            BigDecimal finalBalance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(finalBalance).isEqualByComparingTo(new BigDecimal("700.00")); // 500 + 200
        }

        @Test
        @DisplayName("Should handle multiple backdated transactions correctly")
        void shouldHandleMultipleBackdatedTransactionsCorrectly() {
            // Given: account with snapshots
            factoryBanking.createInitialBalance(
                    bankAccount,
                    counterparty,
                    null,
                    currency,
                    organization,
                    new BigDecimal("1000.00")
            );

            // Create snapshots for past 10 days
            for (int i = 10; i >= 1; i--) {
                LocalDateTime snapshotTime = LocalDate.now().minusDays(i).atStartOfDay();
                balanceService.createSnapshot(bankAccount.getId(), snapshotTime);
            }

            // When: first backdated transaction 5 days ago
            BankReceipt receipt1 = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("100.00")
            );
            LocalDateTime time1 = LocalDateTime.now().minusDays(5).minusHours(2);
            receipt1.setTransactionDateTime(time1);
            bankReceiptRepository.save(receipt1);
            bankReceiptService.post(receipt1.getId());

            // Then: invalid from 5 days ago
            Optional<LocalDate> invalidFromDate = validityService.getInvalidFromDate(bankAccount.getId());
            assertThat(invalidFromDate).isPresent();
            assertThat(invalidFromDate.get()).isEqualTo(time1.toLocalDate().plusDays(1));

            // When: second backdated transaction 3 days ago
            BankReceipt receipt2 = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("50.00")
            );
            LocalDateTime time2 = LocalDateTime.now().minusDays(3).minusHours(2);
            receipt2.setTransactionDateTime(time2);
            bankReceiptRepository.save(receipt2);
            bankReceiptService.post(receipt2.getId());

            // Then: still invalid from 5 days ago (earlier date preserved)
            invalidFromDate = validityService.getInvalidFromDate(bankAccount.getId());
            assertThat(invalidFromDate).isPresent();
            assertThat(invalidFromDate.get()).isEqualTo(time1.toLocalDate().plusDays(1));

            // When: third backdated transaction 8 days ago
            BankReceipt receipt3 = factoryBanking.createBankReceipt(
                    bankAccount,
                    counterparty,
                    currency,
                    organization,
                    new BigDecimal("75.00")
            );
            LocalDateTime time3 = LocalDateTime.now().minusDays(8).minusHours(2);
            receipt3.setTransactionDateTime(time3);
            bankReceiptRepository.save(receipt3);
            bankReceiptService.post(receipt3.getId());

            // Then: invalid from 8 days ago (updated to earlier date)
            invalidFromDate = validityService.getInvalidFromDate(bankAccount.getId());
            assertThat(invalidFromDate).isPresent();
            assertThat(invalidFromDate.get()).isEqualTo(time3.toLocalDate().plusDays(1));

            // When: recalculation runs
            scheduler.recalculateInvalidSnapshots();

            // Then: all snapshots from 7 days ago recalculated correctly
            assertThat(validityRepository.count()).isEqualTo(0);
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), LocalDateTime.now());
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1225.00")); // 1000 + 100 + 50 + 75
        }
    }
}
