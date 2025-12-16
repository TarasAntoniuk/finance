package com.tarasantoniuk.finance.banking.bankaccounttransaction.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BankAccountTransactionService Integration Tests")
class BankAccountTransactionServiceTest extends BaseIntegrationTest {

    @Autowired
    private BankAccountTransactionService service;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;


    private Country country;
    private Currency currency;
    private Organization organization;
    private Bank bank;
    private BankAccount bankAccount;
    private LocalDate testDate;

//    @BeforeAll
//    static void cleanDatabase(@Autowired TestDataCleanerBanking cleanerBanking,
//                              @Autowired TestDataCleanerCore cleanerCore) {
//        cleanerBanking.cleanAll();
//        cleanerCore.cleanAll();
//    }

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);


        System.out.println("=== Currencies in DB BEFORE setUp: " + factoryCore.getCurrencyRepository().findAll().size());

        // Create core entities
        country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);

        // Create banking entities
        bank = factoryBanking.createPrivatBank(country);
        bankAccount = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        // Clean up in correct order (most dependent first)
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @Nested
    @DisplayName("calculateBalance Tests")
    class CalculateBalanceTests {

        @Test
        @DisplayName("Should calculate balance from zero when no snapshot exists")
        void shouldCalculateBalanceFromZero() {
            // Given: Create events directly
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("1000.00"), "TEST_DOCUMENT", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("300.00"), "TEST_DOCUMENT", 2L, "Payment 1");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should calculate balance starting from snapshot")
        void shouldCalculateBalanceFromSnapshot() {
            // Given: Create snapshot first
            LocalDate snapshotDate = testDate.minusDays(5);

            // Create some events before snapshot
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), snapshotDate, new BigDecimal("5000.00"), "TEST_DOCUMENT", 1L, "Initial receipt");

            // Create snapshot
            service.createSnapshot(bankAccount.getId(), snapshotDate);

            // Create events after snapshot
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("2000.00"), "TEST_DOCUMENT", 2L, "Receipt after snapshot");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("500.00"), "TEST_DOCUMENT", 3L, "Payment after snapshot");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("6500.00")); // 5000 + 2000 - 500
        }

        @Test
        @DisplayName("Should return zero when no snapshot and no events")
        void shouldReturnZeroWhenNoData() {
            // Given: No events created

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return snapshot balance when no events after snapshot")
        void shouldReturnSnapshotBalanceWhenNoEventsAfter() {
            // Given: Create snapshot with balance
            LocalDate snapshotDate = testDate.minusDays(1);

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), snapshotDate, new BigDecimal("3000.00"), "TEST_DOCUMENT", 1L, "Initial receipt");

            service.createSnapshot(bankAccount.getId(), snapshotDate);

            // No events after snapshot

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("Should handle multiple debit transactions")
        void shouldHandleMultipleDebits() {
            // Given
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("100.00"), "TEST", 1L, "Debit 1");

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("200.00"), "TEST", 2L, "Debit 2");

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("300.00"), "TEST", 3L, "Debit 3");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Should handle multiple credit transactions")
        void shouldHandleMultipleCredits() {
            // Given
            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("100.00"), "TEST", 1L, "Credit 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("200.00"), "TEST", 2L, "Credit 2");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("300.00"), "TEST", 3L, "Credit 3");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDate);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("-600.00"));
        }
    }

    @Nested
    @DisplayName("getCurrentBalance Tests")
    class GetCurrentBalanceTests {

        @Test
        @DisplayName("Should return current balance as of today")
        void shouldReturnCurrentBalance() {
            // Given: Create events for today
            LocalDate today = LocalDate.now();

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), today, new BigDecimal("1000.00"), "TEST", 1L, "Receipt");

            // When
            BigDecimal balance = service.getCurrentBalance(bankAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }

    @Nested
    @DisplayName("createSnapshot Tests")
    class CreateSnapshotTests {

        @Test
        @DisplayName("Should create snapshot successfully with events")
        void shouldCreateSnapshotWithEvents() {
            // Given: Create events for the day
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("1000.00"), "TEST", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("300.00"), "TEST", 2L, "Payment 1");

            // When
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getSnapshotDate()).isEqualTo(testDate);
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getDebitTurnover()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(result.getCreditTurnover()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(result.getClosingBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
            assertThat(result.getEventsCount()).isEqualTo(2);
            assertThat(result.getLastEventId()).isNotNull();
        }

        @Test
        @DisplayName("Should create snapshot with no events")
        void shouldCreateSnapshotWithNoEvents() {
            // Given: No events for the day

            // When
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getDebitTurnover()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getCreditTurnover()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getEventsCount()).isEqualTo(0);
            assertThat(result.getLastEventId()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when snapshot already exists")
        void shouldThrowExceptionWhenSnapshotExists() {
            // Given: Create snapshot first time
            service.createSnapshot(bankAccount.getId(), testDate);

            // When/Then: Try to create again
            assertThatThrownBy(() -> service.createSnapshot(bankAccount.getId(), testDate)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Snapshot already exists for date: " + testDate);
        }

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            // Given
            Long nonExistentId = 999999L;

            // When/Then
            assertThatThrownBy(() -> service.createSnapshot(nonExistentId, testDate)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Bank account not found with id: 999999");
        }

        @Test
        @DisplayName("Should create snapshot with opening balance from previous day")
        void shouldCreateSnapshotWithOpeningBalance() {
            // Given: Create events and snapshot for previous day
            LocalDate previousDay = testDate.minusDays(1);

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), previousDay, new BigDecimal("5000.00"), "TEST", 1L, "Previous day receipt");

            service.createSnapshot(bankAccount.getId(), previousDay);

            // Create event for current day
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("1000.00"), "TEST", 2L, "Current day receipt");

            // When
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDate);

            // Then
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(result.getClosingBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
        }
    }

    @Nested
    @DisplayName("getSnapshot Tests")
    class GetSnapshotTests {

        @Test
        @DisplayName("Should return snapshot when it exists")
        void shouldReturnSnapshot() {
            // Given: Create snapshot first
            service.createSnapshot(bankAccount.getId(), testDate);

            // When
            BankAccountBalanceSnapshot result = service.getSnapshot(bankAccount.getId(), testDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSnapshotDate()).isEqualTo(testDate);
        }

        @Test
        @DisplayName("Should throw exception when snapshot not found")
        void shouldThrowExceptionWhenSnapshotNotFound() {
            // Given: No snapshot created

            // When/Then
            assertThatThrownBy(() -> service.getSnapshot(bankAccount.getId(), testDate)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Snapshot not found for account " + bankAccount.getId() + " on date " + testDate);
        }
    }

    @Nested
    @DisplayName("getAccountEvents Tests")
    class GetAccountEventsTests {

        @Test
        @DisplayName("Should return all events for account")
        void shouldReturnAllEvents() {
            // Given: Create multiple events
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("1000.00"), "TEST", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), testDate, new BigDecimal("500.00"), "TEST", 2L, "Payment 1");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEvents(bankAccount.getId());

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccountTransactionEvent::getTransactionType).containsExactlyInAnyOrder(TransactionType.DEBIT, TransactionType.CREDIT);
        }

        @Test
        @DisplayName("Should return empty list when no events")
        void shouldReturnEmptyListWhenNoEvents() {
            // Given: No events created

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEvents(bankAccount.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAccountEventsInDateRange Tests")
    class GetAccountEventsInDateRangeTests {

        @Test
        @DisplayName("Should return events within date range")
        void shouldReturnEventsInDateRange() {
            // Given
            LocalDate startDate = testDate;
            LocalDate endDate = testDate.plusDays(5);
            LocalDate middleDate = testDate.plusDays(2);

            // Events within range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), startDate, new BigDecimal("1000.00"), "TEST", 1L, "Receipt in range");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(), middleDate, new BigDecimal("500.00"), "TEST", 2L, "Payment in range");

            // Event outside range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), endDate.plusDays(10), new BigDecimal("2000.00"), "TEST", 3L, "Receipt outside range");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateRange(bankAccount.getId(), startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(event -> !event.getTransactionDate().isBefore(startDate) && !event.getTransactionDate().isAfter(endDate));
        }

        @Test
        @DisplayName("Should return empty list when no events in range")
        void shouldReturnEmptyListWhenNoEventsInRange() {
            // Given
            LocalDate startDate = testDate;
            LocalDate endDate = testDate.plusDays(5);

            // Create event outside range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(), endDate.plusDays(10), new BigDecimal("1000.00"), "TEST", 1L, "Receipt");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateRange(bankAccount.getId(), startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSnapshotsInDateRange Tests")
    class GetSnapshotsInDateRangeTests {

        @Test
        @DisplayName("Should return snapshots within date range")
        void shouldReturnSnapshotsInDateRange() {
            // Given
            LocalDate startDate = testDate;
            LocalDate endDate = testDate.plusDays(5);
            LocalDate middleDate = testDate.plusDays(3);

            // Create snapshots within range
            service.createSnapshot(bankAccount.getId(), startDate);
            service.createSnapshot(bankAccount.getId(), middleDate);

            // Create snapshot outside range
            service.createSnapshot(bankAccount.getId(), endDate.plusDays(10));

            // When
            List<BankAccountBalanceSnapshot> result = service.getSnapshotsInDateRange(bankAccount.getId(), startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccountBalanceSnapshot::getSnapshotDate).containsExactlyInAnyOrder(startDate, middleDate);
        }

        @Test
        @DisplayName("Should return empty list when no snapshots in range")
        void shouldReturnEmptyListWhenNoSnapshotsInRange() {
            // Given
            LocalDate startDate = testDate;
            LocalDate endDate = testDate.plusDays(5);

            // Create snapshot outside range
            service.createSnapshot(bankAccount.getId(), endDate.plusDays(10));

            // When
            List<BankAccountBalanceSnapshot> result = service.getSnapshotsInDateRange(bankAccount.getId(), startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }
    }
}