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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST_DOCUMENT", 1L, "Receipt 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("300.00"), TransactionType.CREDIT, "TEST_DOCUMENT", 2L, "Payment 1");

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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    snapshotDateTime, new BigDecimal("5000.00"), TransactionType.DEBIT, "TEST_DOCUMENT", 1L, "Initial receipt");

            // Create snapshot
            service.createSnapshot(bankAccount.getId(), snapshotDateTime);

            // Create events after snapshot
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("2000.00"), TransactionType.DEBIT, "TEST_DOCUMENT", 2L, "Receipt after snapshot");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), TransactionType.CREDIT, "TEST_DOCUMENT", 3L, "Payment after snapshot");

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

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    snapshotDateTime, new BigDecimal("3000.00"), TransactionType.DEBIT, "TEST_DOCUMENT", 1L, "Initial receipt");

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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("100.00"), TransactionType.DEBIT, "TEST", 1L, "Debit 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("200.00"), TransactionType.DEBIT, "TEST", 2L, "Debit 2");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(2), new BigDecimal("300.00"), TransactionType.DEBIT, "TEST", 3L, "Debit 3");

            // When
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(3));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Should handle multiple credit transactions")
        void shouldHandleMultipleCredits() {
            // Given
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("100.00"), TransactionType.CREDIT, "TEST", 1L, "Credit 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("200.00"), TransactionType.CREDIT, "TEST", 2L, "Credit 2");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(2), new BigDecimal("300.00"), TransactionType.CREDIT, "TEST", 3L, "Credit 3");

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

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    now.minusHours(1), new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt");

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

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(8), new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(10), new BigDecimal("300.00"), TransactionType.CREDIT, "TEST", 2L, "Payment 1");

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

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    previousDay, new BigDecimal("5000.00"), TransactionType.DEBIT, "TEST", 1L, "Previous day receipt");

            // Create snapshot for previous day (will be normalized to end of day)
            service.createSnapshot(bankAccount.getId(), previousDay);

            // Create event for current day
            LocalDateTime currentDayStart = testDateTime.toLocalDate().atStartOfDay();
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    currentDayStart.plusHours(10), new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 2L, "Current day receipt");

            // When: Create snapshot for current day
            BankAccountBalanceSnapshot result = service.createSnapshot(bankAccount.getId(), testDateTime);

            // Then
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(result.getClosingBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
        }

        @Test
        @DisplayName("Should skip reversal events when calculating turnovers in snapshot")
        void shouldSkipReversalEventsInSnapshot() {
            // Given: Create normal events and a reversal event for the same day
            LocalDateTime dayStart = testDateTime.toLocalDate().atStartOfDay();

            // 1. Create a receipt event at T+1: +1000
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(1), new BigDecimal("1000.00"), "Receipt 1", "BankReceipt", 100L);

            // 2. Create a payment event at T+2: -500
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(2), new BigDecimal("500.00"), "Payment 1", "BankPayment", 200L);

            // 3. Reverse the receipt at T+3 (creates BankReceiptReversal event)
            service.reverseDocument("BankReceipt", 100L, "Receipt 1");

            // 4. Create another receipt event at T+4: +300
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    dayStart.plusHours(4), new BigDecimal("300.00"), "Receipt 2", "BankReceipt", 101L);

            // When: Create snapshot for the day
            BankAccountBalanceSnapshot snapshot = service.createSnapshot(bankAccount.getId(), testDateTime);

            // Then: Reversal events should be excluded from turnovers
            // The query filters events where isReversed=false, so:
            // - BankReceipt #100: EXCLUDED (marked as isReversed=true when reversed)
            // - BankPayment #200: -500 credit turnover (isReversed=false)
            // - BankReceiptReversal: FETCHED by query (isReversed=false) but SKIPPED by code (ends with "Reversal")
            // - BankReceipt #101: +300 debit turnover (isReversed=false)
            assertThat(snapshot.getDebitTurnover()).isEqualByComparingTo(new BigDecimal("300.00")); // Only receipt #101
            assertThat(snapshot.getCreditTurnover()).isEqualByComparingTo(new BigDecimal("500.00")); // Only payment #200

            // Closing balance: 0 + 300 - 500 = -200
            assertThat(snapshot.getClosingBalance()).isEqualByComparingTo(new BigDecimal("-200.00"));

            // Events count is set to events.size() which includes ALL events fetched by query
            // Receipt #100 is filtered out by isReversed check in query (not counted)
            // Payment #200, BankReceiptReversal, Receipt #101 are all counted (even though reversal is skipped in turnover)
            assertThat(snapshot.getEventsCount()).isEqualTo(3);
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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), TransactionType.CREDIT, "TEST", 2L, "Payment 1");

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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    startDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt in range");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    middleDateTime, new BigDecimal("500.00"), TransactionType.CREDIT, "TEST", 2L, "Payment in range");

            // Event outside range
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("2000.00"), TransactionType.DEBIT, "TEST", 3L, "Receipt outside range");

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
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt");

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

    @Nested
    @DisplayName("postDocument Tests")
    class PostDocumentTests {

        @Test
        @DisplayName("Should post BankReceipt document successfully")
        void shouldPostBankReceiptDocument() {
            // Given
            Long documentId = 100L;
            BigDecimal amount = new BigDecimal("1000.00");

            // When
            BankAccountTransactionEvent result = service.postDocument(
                    bankAccount.getId(),
                    organization.getId(),
                    currency.getId(),
                    testDateTime,
                    amount,
                    "Test receipt",
                    "BankReceipt",
                    documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentType()).isEqualTo("BankReceipt");
            assertThat(result.getDocumentId()).isEqualTo(documentId);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEBIT);
            assertThat(result.getAmount()).isEqualByComparingTo(amount);
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should post BankPayment document successfully")
        void shouldPostBankPaymentDocument() {
            // Given
            Long documentId = 200L;
            BigDecimal amount = new BigDecimal("500.00");

            // When
            BankAccountTransactionEvent result = service.postDocument(
                    bankAccount.getId(),
                    organization.getId(),
                    currency.getId(),
                    testDateTime,
                    amount,
                    "Test payment",
                    "BankPayment",
                    documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentType()).isEqualTo("BankPayment");
            assertThat(result.getDocumentId()).isEqualTo(documentId);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.CREDIT);
            assertThat(result.getAmount()).isEqualByComparingTo(amount);
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(amount.negate());
        }

        @Test
        @DisplayName("Should throw exception when posting duplicate document")
        void shouldThrowExceptionWhenPostingDuplicate() {
            // Given
            Long documentId = 300L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "First post", "BankReceipt", documentId);

            // When/Then
            assertThatThrownBy(() -> service.postDocument(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Second post", "BankReceipt", documentId))
                    .isInstanceOf(com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Transaction event already exists for BankReceipt id: 300");
        }

        @Test
        @DisplayName("Should handle repost after unpost by marking reversal as reversed")
        void shouldHandleRepostAfterUnpost() {
            // Given: Post → Unpost → Repost cycle
            Long documentId = 400L;

            // 1. Initial post
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Initial post", "BankReceipt", documentId);

            // 2. Unpost (creates reversal event)
            service.reverseDocument("BankReceipt", documentId, "Initial post");

            // 3. Repost (should mark reversal as reversed)
            BankAccountTransactionEvent repostedEvent = service.postDocument(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Repost", "BankReceipt", documentId);

            // Then
            assertThat(repostedEvent).isNotNull();
            assertThat(repostedEvent.getIsReversed()).isFalse();

            // Verify reversal event is marked as reversed
            assertThat(service.existsByDocument("BankReceiptReversal", documentId)).isFalse();
        }
    }

    @Nested
    @DisplayName("reverseDocument Tests")
    class ReverseDocumentTests {

        @Test
        @DisplayName("Should reverse BankReceipt document successfully")
        void shouldReverseBankReceiptDocument() {
            // Given: Post a BankReceipt
            Long documentId = 500L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Original receipt", "BankReceipt", documentId);

            // When: Reverse it
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, "Original receipt");

            // Then
            assertThat(reversalEvent).isNotNull();
            assertThat(reversalEvent.getDocumentType()).isEqualTo("BankReceiptReversal");
            assertThat(reversalEvent.getDocumentId()).isEqualTo(documentId);
            assertThat(reversalEvent.getTransactionType()).isEqualTo(TransactionType.CREDIT); // Inverted
            assertThat(reversalEvent.getDescription()).contains("Reversal of receipt #500");
            assertThat(reversalEvent.getDescription()).contains("Original receipt");

            // Verify original event is marked as reversed
            BankAccountTransactionEvent originalEvent = service.findByDocument("BankReceipt", documentId);
            assertThat(originalEvent.getIsReversed()).isTrue();
            assertThat(originalEvent.getReversedByEventId()).isEqualTo(reversalEvent.getId());
        }

        @Test
        @DisplayName("Should reverse BankPayment document successfully")
        void shouldReverseBankPaymentDocument() {
            // Given: Post a BankPayment
            Long documentId = 600L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("500.00"), "Original payment", "BankPayment", documentId);

            // When: Reverse it
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankPayment", documentId, "Original payment");

            // Then
            assertThat(reversalEvent).isNotNull();
            assertThat(reversalEvent.getDocumentType()).isEqualTo("BankPaymentReversal");
            assertThat(reversalEvent.getDocumentId()).isEqualTo(documentId);
            assertThat(reversalEvent.getTransactionType()).isEqualTo(TransactionType.DEBIT); // Inverted
            assertThat(reversalEvent.getDescription()).contains("Reversal of payment #600");
        }

        @Test
        @DisplayName("Should handle null description in reversal")
        void shouldHandleNullDescriptionInReversal() {
            // Given
            Long documentId = 700L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), null, "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, null);

            // Then
            assertThat(reversalEvent.getDescription()).isEqualTo("Reversal of receipt #700");
            assertThat(reversalEvent.getDescription()).doesNotContain(":");
        }

        @Test
        @DisplayName("Should handle blank description in reversal")
        void shouldHandleBlankDescriptionInReversal() {
            // Given
            Long documentId = 800L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "   ", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, "   ");

            // Then
            assertThat(reversalEvent.getDescription()).isEqualTo("Reversal of receipt #800");
        }
    }

    @Nested
    @DisplayName("findByDocument Tests")
    class FindByDocumentTests {

        @Test
        @DisplayName("Should find document when it exists")
        void shouldFindDocumentWhenExists() {
            // Given
            Long documentId = 900L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent result = service.findByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentId()).isEqualTo(documentId);
        }

        @Test
        @DisplayName("Should throw exception when document not found")
        void shouldThrowExceptionWhenDocumentNotFound() {
            // Given: No document posted

            // When/Then
            assertThatThrownBy(() -> service.findByDocument("BankReceipt", 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction event not found for document: BankReceipt #999");
        }
    }

    @Nested
    @DisplayName("findActiveByDocument Tests")
    class FindActiveByDocumentTests {

        @Test
        @DisplayName("Should find active document when not reversed")
        void shouldFindActiveDocumentWhenNotReversed() {
            // Given
            Long documentId = 1000L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent result = service.findActiveByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsReversed()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when active document not found after reversal")
        void shouldThrowExceptionWhenActiveDocumentNotFoundAfterReversal() {
            // Given: Post and reverse
            Long documentId = 1100L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);
            service.reverseDocument("BankReceipt", documentId, "Test");

            // When/Then
            assertThatThrownBy(() -> service.findActiveByDocument("BankReceipt", documentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Active transaction event not found for document: BankReceipt #1100");
        }
    }

    @Nested
    @DisplayName("existsByDocument Tests")
    class ExistsByDocumentTests {

        @Test
        @DisplayName("Should return true when document exists and not reversed")
        void shouldReturnTrueWhenDocumentExistsAndNotReversed() {
            // Given
            Long documentId = 1200L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            boolean result = service.existsByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when document does not exist")
        void shouldReturnFalseWhenDocumentDoesNotExist() {
            // Given: No document posted

            // When
            boolean result = service.existsByDocument("BankReceipt", 999L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when document is reversed")
        void shouldReturnFalseWhenDocumentIsReversed() {
            // Given: Post and reverse
            Long documentId = 1300L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);
            service.reverseDocument("BankReceipt", documentId, "Test");

            // When
            boolean result = service.existsByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isFalse(); // Reversed document should not exist as active
        }
    }

    @Nested
    @DisplayName("reverseTransaction Tests")
    class ReverseTransactionTests {

        @Test
        @DisplayName("Should establish bidirectional links between events")
        void shouldEstablishBidirectionalLinks() {
            // Given
            BankAccountTransactionEvent originalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Original");

            BankAccountTransactionEvent reversalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.CREDIT,
                    "TESTReversal", 1L, "Reversal");

            // When
            service.reverseTransaction(originalEvent.getId(), reversalEvent.getId());

            // Then
            BankAccountTransactionEvent updatedOriginal = service.findByDocument("TEST", 1L);
            BankAccountTransactionEvent updatedReversal = service.findByDocument("TESTReversal", 1L);

            assertThat(updatedOriginal.getIsReversed()).isTrue();
            assertThat(updatedOriginal.getReversedByEventId()).isEqualTo(reversalEvent.getId());
            assertThat(updatedReversal.getReversedByEventId()).isEqualTo(originalEvent.getId());
        }

        @Test
        @DisplayName("Should throw exception when original event not found")
        void shouldThrowExceptionWhenOriginalEventNotFound() {
            // Given
            BankAccountTransactionEvent reversalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.CREDIT,
                    "TESTReversal", 1L, "Reversal");

            // When/Then
            assertThatThrownBy(() -> service.reverseTransaction(999999L, reversalEvent.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction event not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when reversal event not found")
        void shouldThrowExceptionWhenReversalEventNotFound() {
            // Given
            BankAccountTransactionEvent originalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Original");

            // When/Then
            assertThatThrownBy(() -> service.reverseTransaction(originalEvent.getId(), 999999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reversal event not found with id: 999999");
        }
    }

    @Nested
    @DisplayName("createTransactionEvent Tests")
    class CreateTransactionEventTests {

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    999999L, organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank account not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when organization not found")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    bankAccount.getId(), 999999L, currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Organization not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when currency not found")
        void shouldThrowExceptionWhenCurrencyNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), 999999L,
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Currency not found with id: 999999");
        }

        @Test
        @DisplayName("Should calculate balance after for DEBIT transaction")
        void shouldCalculateBalanceAfterForDebit() {
            // Given: Existing balance
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.minusHours(1), new BigDecimal("500.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Previous");

            // When: Create new DEBIT
            BankAccountTransactionEvent result = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("300.00"), TransactionType.DEBIT,
                    "TEST", 2L, "New debit");

            // Then
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("800.00")); // 500 + 300
        }

        @Test
        @DisplayName("Should calculate balance after for CREDIT transaction")
        void shouldCalculateBalanceAfterForCredit() {
            // Given: Existing balance
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.minusHours(1), new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Previous");

            // When: Create new CREDIT
            BankAccountTransactionEvent result = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("400.00"), TransactionType.CREDIT,
                    "TEST", 2L, "New credit");

            // Then
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("600.00")); // 1000 - 400
        }
    }

    @Nested
    @DisplayName("Balance Calculation with Reversal Events Tests")
    class BalanceCalculationWithReversalTests {

        @Test
        @DisplayName("Should exclude reversal events from balance calculation")
        void shouldExcludeReversalEventsFromBalanceCalculation() {
            // Given
            // 1. Post receipt: +1000
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Receipt", "BankReceipt", 1L);

            // 2. Reverse it: creates BankReceiptReversal event
            service.reverseDocument("BankReceipt", 1L, "Receipt");

            // When: Calculate balance
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusDays(1));

            // Then: Balance should be zero (original is reversed, reversal event is excluded)
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle post-unpost-repost cycle correctly in balance")
        void shouldHandlePostUnpostRepostCycleInBalance() {
            // Given
            // 1. Post: +1000
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Receipt", "BankReceipt", 1L);

            BigDecimal balanceAfterPost = service.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterPost).isEqualByComparingTo(new BigDecimal("1000.00"));

            // 2. Unpost: creates reversal
            service.reverseDocument("BankReceipt", 1L, "Receipt");

            BigDecimal balanceAfterUnpost = service.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterUnpost).isEqualByComparingTo(BigDecimal.ZERO);

            // 3. Repost: creates new event, marks reversal as reversed
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Receipt reposted", "BankReceipt", 1L);

            BigDecimal balanceAfterRepost = service.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterRepost).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("calculateBalance should skip reversal events with endsWith Reversal check")
        void calculateBalanceShouldSkipReversalEventsWithEndsWithCheck() {
            // Given: Create events that will be in the query result
            // 1. Post a receipt at T+1: +500
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), "First receipt", "BankReceipt", 100L);

            // 2. Post a payment at T+2: -200
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(2), new BigDecimal("200.00"), "First payment", "BankPayment", 200L);

            // 3. Reverse the receipt at T+3: creates BankReceiptReversal event
            service.reverseDocument("BankReceipt", 100L, "First receipt");

            // 4. Post another receipt at T+4: +300
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(4), new BigDecimal("300.00"), "Second receipt", "BankReceipt", 101L);

            // When: Calculate balance at T+5 (after all events)
            // This should include:
            // - Original receipt (+500) marked as reversed (excluded by isReversed=false in query)
            // - Payment (-200)
            // - BankReceiptReversal event (should be SKIPPED by endsWith("Reversal") check)
            // - Second receipt (+300)
            BigDecimal balance = service.calculateBalance(bankAccount.getId(), testDateTime.plusHours(5));

            // Then: Balance should be 100 (0 - 200 + 300)
            // The reversal event itself should be skipped due to endsWith("Reversal")
            assertThat(balance).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }
}