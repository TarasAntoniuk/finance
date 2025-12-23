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
import java.time.LocalDateTime;
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
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

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
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "TEST_DOCUMENT", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("300.00"), "TEST_DOCUMENT", 2L, "Payment 1");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(2));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should calculate balance starting from snapshot")
        void shouldCalculateBalanceFromSnapshot() {
            // Given: Create snapshot first
            LocalDateTime snapshotDateTime = testDateTime.minusDays(5);

            // Create some events before snapshot
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    snapshotDateTime, new BigDecimal("5000.00"), "TEST_DOCUMENT", 1L, "Initial receipt");

            // Create snapshot
            service.createSnapshot(bankAccount.getId(), snapshotDateTime);

            // Create events after snapshot
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("2000.00"), "TEST_DOCUMENT", 2L, "Receipt after snapshot");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), "TEST_DOCUMENT", 3L, "Payment after snapshot");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(2));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("6500.00")); // 5000 + 2000 - 500
        }

        @Test
        @DisplayName("Should return zero when no snapshot and no events")
        void shouldReturnZeroWhenNoData() {
            // Given: No events created

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime);

            // Then
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return snapshot balance when no events after snapshot")
        void shouldReturnSnapshotBalanceWhenNoEventsAfter() {
            // Given: Create snapshot with balance
            LocalDateTime snapshotDateTime = testDateTime.minusDays(1);

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    snapshotDateTime, new BigDecimal("3000.00"), "TEST_DOCUMENT", 1L, "Initial receipt");

            service.createSnapshot(bankAccount.getId(), snapshotDateTime);

            // No events after snapshot

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("Should handle multiple debit transactions")
        void shouldHandleMultipleDebits() {
            // Given
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("100.00"), "TEST", 1L, "Debit 1");

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("200.00"), "TEST", 2L, "Debit 2");

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(2), new BigDecimal("300.00"), "TEST", 3L, "Debit 3");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(3));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Should handle multiple credit transactions")
        void shouldHandleMultipleCredits() {
            // Given
            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("100.00"), "TEST", 1L, "Credit 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("200.00"), "TEST", 2L, "Credit 2");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(2), new BigDecimal("300.00"), "TEST", 3L, "Credit 3");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(3));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("-600.00"));
        }
    }

    @Nested
    @DisplayName("getCurrentBalance Tests")
    class GetCurrentBalanceTests {

        @Test
        @DisplayName("Should return current balance as of now")
        void shouldReturnCurrentBalance() {
            // Given: Create events for today
            LocalDateTime now = LocalDateTime.now();

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    now.minusHours(1), new BigDecimal("1000.00"), "TEST", 1L, "Receipt");

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
            LocalDateTime dayStart = testDateTime.toLocalDate().atStartOfDay();

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(8), new BigDecimal("1000.00"), "TEST", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(10), new BigDecimal("300.00"), "TEST", 2L, "Payment 1");

            LocalDateTime snapshotDateTime = dayStart.plusHours(23).plusMinutes(59);

            // Snapshot will be normalized to start of next day
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            // When
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), snapshotDateTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getSnapshotDateTime()).isEqualTo(expectedSnapshotDateTime);
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
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            // When
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDateTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSnapshotDateTime()).isEqualTo(expectedSnapshotDateTime);
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
            service.createSnapshot(bankAccount.getId(), testDateTime);

            // When/Then: Try to create again for same day
            assertThatThrownBy(() -> service.createSnapshot(bankAccount.getId(), testDateTime))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Snapshot already exists for date: " + testDateTime.toLocalDate());
        }

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            // Given
            Long nonExistentId = 999999L;

            // When/Then
            assertThatThrownBy(() -> service.createSnapshot(nonExistentId, testDateTime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank account not found with id: 999999");
        }

        @Test
        @DisplayName("Should create snapshot with opening balance from previous day")
        void shouldCreateSnapshotWithOpeningBalance() {
            // Given: Create events and snapshot for previous day
            LocalDateTime previousDay = testDateTime.minusDays(1);

            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    previousDay, new BigDecimal("5000.00"), "TEST", 1L, "Previous day receipt");

            // Create snapshot for previous day (will be normalized to end of day)
            service.createSnapshot(bankAccount.getId(), previousDay);

            // Create event for current day
            LocalDateTime currentDayStart = testDateTime.toLocalDate().atStartOfDay();
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    currentDayStart.plusHours(10), new BigDecimal("1000.00"), "TEST", 2L, "Current day receipt");

            // When: Create snapshot for current day
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDateTime);

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
            service.createSnapshot(bankAccount.getId(), testDateTime);

            // Snapshot is stored at start of next day
            LocalDateTime endOfDay = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            // When
            BankAccountBalanceSnapshot result = service.getSnapshot(bankAccount.getId(), endOfDay);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSnapshotDateTime()).isEqualTo(endOfDay);
        }

        @Test
        @DisplayName("Should throw exception when snapshot not found")
        void shouldThrowExceptionWhenSnapshotNotFound() {
            // Given: No snapshot created

            // When/Then
            assertThatThrownBy(() -> service.getSnapshot(bankAccount.getId(), testDateTime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Snapshot not found for account " + bankAccount.getId() + " at datetime " + testDateTime);
        }
    }

    @Nested
    @DisplayName("getAccountEvents Tests")
    class GetAccountEventsTests {

        @Test
        @DisplayName("Should return all events for account")
        void shouldReturnAllEvents() {
            // Given: Create multiple events
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "TEST", 1L, "Receipt 1");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), "TEST", 2L, "Payment 1");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEvents(bankAccount.getId());

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccountTransactionEvent::getTransactionType)
                    .containsExactlyInAnyOrder(TransactionType.DEBIT, TransactionType.CREDIT);
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
    @DisplayName("getAccountEventsInDateTimeRange Tests")
    class GetAccountEventsInDateTimeRangeTests {

        @Test
        @DisplayName("Should return events within datetime range")
        void shouldReturnEventsInDateTimeRange() {
            // Given
            LocalDateTime startDateTime = testDateTime;
            LocalDateTime endDateTime = testDateTime.plusDays(5);
            LocalDateTime middleDateTime = testDateTime.plusDays(2);

            // Events within range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    startDateTime, new BigDecimal("1000.00"), "TEST", 1L, "Receipt in range");

            service.createPaymentEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    middleDateTime, new BigDecimal("500.00"), "TEST", 2L, "Payment in range");

            // Event outside range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("2000.00"), "TEST", 3L, "Receipt outside range");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateTimeRange(
                    bankAccount.getId(), startDateTime, endDateTime);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(event ->
                    !event.getTransactionDateTime().isBefore(startDateTime) &&
                            !event.getTransactionDateTime().isAfter(endDateTime));
        }

        @Test
        @DisplayName("Should return empty list when no events in range")
        void shouldReturnEmptyListWhenNoEventsInRange() {
            // Given
            LocalDateTime startDateTime = testDateTime;
            LocalDateTime endDateTime = testDateTime.plusDays(5);

            // Create event outside range
            service.createReceiptEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("1000.00"), "TEST", 1L, "Receipt");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateTimeRange(
                    bankAccount.getId(), startDateTime, endDateTime);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSnapshotsInDateTimeRange Tests")
    class GetSnapshotsInDateTimeRangeTests {

        @Test
        @DisplayName("Should return snapshots within datetime range")
        void shouldReturnSnapshotsInDateTimeRange() {
            // Given
            LocalDateTime startDateTime = testDateTime; // 2024-01-15T10:00
            LocalDateTime endDateTime = testDateTime.plusDays(5); // 2024-01-20T10:00
            LocalDateTime middleDateTime = testDateTime.plusDays(3); // 2024-01-18T10:00

            // Create snapshots within range
            service.createSnapshot(bankAccount.getId(), startDateTime);
            service.createSnapshot(bankAccount.getId(), middleDateTime);

            // Create snapshot outside range
            service.createSnapshot(bankAccount.getId(), endDateTime.plusDays(10));

            // Expected normalized datetimes (start of next day)
            // 2024-01-15 -> 2024-01-16T00:00
            // 2024-01-18 -> 2024-01-19T00:00
            LocalDateTime expectedStart = startDateTime.toLocalDate().plusDays(1).atStartOfDay();
            LocalDateTime expectedMiddle = middleDateTime.toLocalDate().plusDays(1).atStartOfDay();

            // When: Search using inclusive range
            LocalDateTime searchEndDateTime = endDateTime.toLocalDate().plusDays(1).atStartOfDay();
            List<BankAccountBalanceSnapshot> result = service.getSnapshotsInDateTimeRange(
                    bankAccount.getId(), startDateTime.toLocalDate().atStartOfDay(), searchEndDateTime);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccountBalanceSnapshot::getSnapshotDateTime)
                    .containsExactlyInAnyOrder(expectedStart, expectedMiddle);
        }

        @Test
        @DisplayName("Should return empty list when no snapshots in range")
        void shouldReturnEmptyListWhenNoSnapshotsInRange() {
            // Given
            LocalDateTime startDateTime = testDateTime;
            LocalDateTime endDateTime = testDateTime.plusDays(5);

            // Create snapshot outside range
            service.createSnapshot(bankAccount.getId(), endDateTime.plusDays(10));

            // When
            LocalDateTime searchEndDateTime = endDateTime.toLocalDate().atTime(23, 59, 59, 999999999);
            List<BankAccountBalanceSnapshot> result = service.getSnapshotsInDateTimeRange(
                    bankAccount.getId(), startDateTime.toLocalDate().atStartOfDay(), searchEndDateTime);

            // Then
            assertThat(result).isEmpty();
        }
    }
}