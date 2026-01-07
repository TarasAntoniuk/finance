package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountBalanceService Unit Tests")
class BankAccountBalanceServiceTest {

    @Mock
    private BankAccountBalanceSnapshotRepository balanceSnapshotRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private BankAccountTransactionEventRepository transactionEventRepository;

    @Mock
    private BankAccountSnapshotValidityService validityService;

    @InjectMocks
    private BankAccountBalanceService balanceService;

    private BankAccount bankAccount;
    private Organization organization;
    private Currency currency;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

        // Create mock entities
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Organization");

        currency = new Currency();
        currency.setId(1L);
        currency.setCode("UAH");

        bankAccount = new BankAccount();
        bankAccount.setId(1L);
        bankAccount.setCurrency(currency);
        bankAccount.setHolderId(organization.getId());

        // Default stub: all snapshots are valid (no invalidation)
        lenient().when(validityService.getInvalidFromDate(anyLong())).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("calculateBalance Tests")
    class CalculateBalanceTests {

        @Test
        @DisplayName("Should calculate balance from zero when no snapshot exists")
        void shouldCalculateBalanceFromZero() {
            // Given: Create events directly
            List<BankAccountTransactionEvent> events = new ArrayList<>();

            BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
            event1.setId(1L);
            event1.setAmount(new BigDecimal("1000.00"));
            event1.setTransactionType(TransactionType.DEBIT);
            event1.setTransactionDateTime(testDateTime);
            event1.setDocumentType("TEST_DOCUMENT");
            event1.setIsReversed(false);
            events.add(event1);

            BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
            event2.setId(2L);
            event2.setAmount(new BigDecimal("300.00"));
            event2.setTransactionType(TransactionType.CREDIT);
            event2.setTransactionDateTime(testDateTime.plusHours(1));
            event2.setDocumentType("TEST_DOCUMENT");
            event2.setIsReversed(false);
            events.add(event2);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), testDateTime.plusHours(2)))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusHours(2));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should calculate balance starting from snapshot")
        void shouldCalculateBalanceFromSnapshot() {
            // Given: Create snapshot first
            LocalDateTime snapshotDateTime = testDateTime.minusDays(5);

            BankAccountBalanceSnapshot snapshot = new BankAccountBalanceSnapshot();
            snapshot.setId(1L);
            snapshot.setSnapshotDateTime(snapshotDateTime);
            snapshot.setClosingBalance(new BigDecimal("5000.00"));
            snapshot.setBankAccount(bankAccount);

            List<BankAccountTransactionEvent> events = new ArrayList<>();

            BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
            event1.setId(2L);
            event1.setAmount(new BigDecimal("2000.00"));
            event1.setTransactionType(TransactionType.DEBIT);
            event1.setTransactionDateTime(testDateTime);
            event1.setDocumentType("TEST_DOCUMENT");
            event1.setIsReversed(false);
            events.add(event1);

            BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
            event2.setId(3L);
            event2.setAmount(new BigDecimal("500.00"));
            event2.setTransactionType(TransactionType.CREDIT);
            event2.setTransactionDateTime(testDateTime.plusHours(1));
            event2.setDocumentType("TEST_DOCUMENT");
            event2.setIsReversed(false);
            events.add(event2);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), testDateTime.plusHours(2)))
                    .thenReturn(Optional.of(snapshot));
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusHours(2));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("6500.00")); // 5000 + 2000 - 500
        }

        @Test
        @DisplayName("Should return zero when no snapshot and no events")
        void shouldReturnZeroWhenNoData() {
            // Given: No events created
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), testDateTime))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new ArrayList<>());

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime);

            // Then
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return snapshot balance when no events after snapshot")
        void shouldReturnSnapshotBalanceWhenNoEventsAfter() {
            // Given: Create snapshot with balance
            LocalDateTime snapshotDateTime = testDateTime.minusDays(1);

            BankAccountBalanceSnapshot snapshot = new BankAccountBalanceSnapshot();
            snapshot.setId(1L);
            snapshot.setSnapshotDateTime(snapshotDateTime);
            snapshot.setClosingBalance(new BigDecimal("3000.00"));
            snapshot.setBankAccount(bankAccount);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), testDateTime))
                    .thenReturn(Optional.of(snapshot));
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new ArrayList<>());

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime);

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("Should handle multiple debit transactions")
        void shouldHandleMultipleDebits() {
            // Given
            List<BankAccountTransactionEvent> events = new ArrayList<>();

            BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
            event1.setId(1L);
            event1.setAmount(new BigDecimal("100.00"));
            event1.setTransactionType(TransactionType.DEBIT);
            event1.setDocumentType("TEST");
            event1.setIsReversed(false);
            events.add(event1);

            BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
            event2.setId(2L);
            event2.setAmount(new BigDecimal("200.00"));
            event2.setTransactionType(TransactionType.DEBIT);
            event2.setDocumentType("TEST");
            event2.setIsReversed(false);
            events.add(event2);

            BankAccountTransactionEvent event3 = new BankAccountTransactionEvent();
            event3.setId(3L);
            event3.setAmount(new BigDecimal("300.00"));
            event3.setTransactionType(TransactionType.DEBIT);
            event3.setDocumentType("TEST");
            event3.setIsReversed(false);
            events.add(event3);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(eq(bankAccount.getId()), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusHours(3));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Should handle multiple credit transactions")
        void shouldHandleMultipleCredits() {
            // Given
            List<BankAccountTransactionEvent> events = new ArrayList<>();

            BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
            event1.setId(1L);
            event1.setAmount(new BigDecimal("100.00"));
            event1.setTransactionType(TransactionType.CREDIT);
            event1.setDocumentType("TEST");
            event1.setIsReversed(false);
            events.add(event1);

            BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
            event2.setId(2L);
            event2.setAmount(new BigDecimal("200.00"));
            event2.setTransactionType(TransactionType.CREDIT);
            event2.setDocumentType("TEST");
            event2.setIsReversed(false);
            events.add(event2);

            BankAccountTransactionEvent event3 = new BankAccountTransactionEvent();
            event3.setId(3L);
            event3.setAmount(new BigDecimal("300.00"));
            event3.setTransactionType(TransactionType.CREDIT);
            event3.setDocumentType("TEST");
            event3.setIsReversed(false);
            events.add(event3);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(eq(bankAccount.getId()), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusHours(3));

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("-600.00"));
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
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            List<BankAccountTransactionEvent> events = new ArrayList<>();

            BankAccountTransactionEvent event1 = new BankAccountTransactionEvent();
            event1.setId(1L);
            event1.setAmount(new BigDecimal("1000.00"));
            event1.setTransactionType(TransactionType.DEBIT);
            event1.setDocumentType("TEST");
            event1.setIsReversed(false);
            events.add(event1);

            BankAccountTransactionEvent event2 = new BankAccountTransactionEvent();
            event2.setId(2L);
            event2.setAmount(new BigDecimal("300.00"));
            event2.setTransactionType(TransactionType.CREDIT);
            event2.setDocumentType("TEST");
            event2.setIsReversed(false);
            events.add(event2);

            when(bankAccountRepository.findByIdWithRelations(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(
                    bankAccount.getId(), expectedSnapshotDateTime)).thenReturn(false);
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), dayStart))
                    .thenReturn(Optional.empty());
            // Mock repository to return different results based on datetime range
            // For calculateBalance call (before start of day): return empty
            // For createSnapshot call (during the day): return test events
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> {
                        LocalDateTime start = invocation.getArgument(1);
                        LocalDateTime dayStart2 = testDateTime.toLocalDate().atStartOfDay();
                        // If querying before start of day, return empty (for calculateBalance)
                        if (start.isBefore(dayStart2)) {
                            return new ArrayList<>();
                        }
                        // Otherwise return events for the day (for createSnapshot)
                        return events;
                    });
            when(balanceSnapshotRepository.save(any(BankAccountBalanceSnapshot.class)))
                    .thenAnswer(invocation -> {
                        BankAccountBalanceSnapshot snapshot = invocation.getArgument(0);
                        snapshot.setId(1L);
                        return snapshot;
                    });

            // When
            BankAccountBalanceSnapshot result = balanceService.createSnapshot(bankAccount.getId(), testDateTime);

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
            LocalDateTime dayStart = testDateTime.toLocalDate().atStartOfDay();

            when(bankAccountRepository.findByIdWithRelations(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(
                    bankAccount.getId(), expectedSnapshotDateTime)).thenReturn(false);
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), dayStart))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new ArrayList<>());
            when(balanceSnapshotRepository.save(any(BankAccountBalanceSnapshot.class)))
                    .thenAnswer(invocation -> {
                        BankAccountBalanceSnapshot snapshot = invocation.getArgument(0);
                        snapshot.setId(1L);
                        return snapshot;
                    });

            // When
            BankAccountBalanceSnapshot result = balanceService.createSnapshot(bankAccount.getId(), testDateTime);

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
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            when(bankAccountRepository.findByIdWithRelations(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
            when(balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(
                    bankAccount.getId(), expectedSnapshotDateTime)).thenReturn(true);

            // When/Then: Try to create again for same day
            assertThatThrownBy(() -> balanceService.createSnapshot(bankAccount.getId(), testDateTime))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Snapshot already exists for date: " + testDateTime.toLocalDate());
        }

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            // Given
            Long nonExistentId = 999999L;
            when(bankAccountRepository.findByIdWithRelations(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> balanceService.createSnapshot(nonExistentId, testDateTime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank account not found with id: 999999");
        }

        @Test
        @DisplayName("Should create snapshot with opening balance from previous day")
        void shouldCreateSnapshotWithOpeningBalance() {
            // Given: Create events and snapshot for previous day
            LocalDateTime previousDay = testDateTime.minusDays(1);
            LocalDateTime currentDayStart = testDateTime.toLocalDate().atStartOfDay();
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            BankAccountBalanceSnapshot previousSnapshot = new BankAccountBalanceSnapshot();
            previousSnapshot.setId(1L);
            previousSnapshot.setClosingBalance(new BigDecimal("5000.00"));
            previousSnapshot.setSnapshotDateTime(previousDay.toLocalDate().plusDays(1).atStartOfDay());

            List<BankAccountTransactionEvent> currentDayEvents = new ArrayList<>();
            BankAccountTransactionEvent event = new BankAccountTransactionEvent();
            event.setId(2L);
            event.setAmount(new BigDecimal("1000.00"));
            event.setTransactionType(TransactionType.DEBIT);
            event.setDocumentType("TEST");
            event.setIsReversed(false);
            currentDayEvents.add(event);

            when(bankAccountRepository.findByIdWithRelations(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(
                    bankAccount.getId(), expectedSnapshotDateTime)).thenReturn(false);
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), currentDayStart))
                    .thenReturn(Optional.of(previousSnapshot));
            // Mock repository to return different results based on datetime range
            // For calculateBalance call (after snapshot): return empty (no events between snapshot and current day start)
            // For createSnapshot call (during the day): return current day events
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> {
                        LocalDateTime start = invocation.getArgument(1);
                        LocalDateTime currentDayStart2 = testDateTime.toLocalDate().atStartOfDay();
                        // If querying before current day start (for calculateBalance after snapshot), return empty
                        if (start.isBefore(currentDayStart2) || start.equals(previousSnapshot.getSnapshotDateTime().plusNanos(1))) {
                            return new ArrayList<>();
                        }
                        // Otherwise return events for current day (for createSnapshot)
                        return currentDayEvents;
                    });
            when(balanceSnapshotRepository.save(any(BankAccountBalanceSnapshot.class)))
                    .thenAnswer(invocation -> {
                        BankAccountBalanceSnapshot snapshot = invocation.getArgument(0);
                        snapshot.setId(2L);
                        return snapshot;
                    });

            // When: Create snapshot for current day
            BankAccountBalanceSnapshot result = balanceService.createSnapshot(bankAccount.getId(), testDateTime);

            // Then
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(result.getClosingBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
        }

        @Test
        @DisplayName("Should skip reversal events when calculating turnovers in snapshot")
        void shouldSkipReversalEventsInSnapshot() {
            // Given: Create normal events and a reversal event for the same day
            LocalDateTime dayStart = testDateTime.toLocalDate().atStartOfDay();
            LocalDateTime expectedSnapshotDateTime = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            List<BankAccountTransactionEvent> events = new ArrayList<>();

            // Payment event: -500
            BankAccountTransactionEvent paymentEvent = new BankAccountTransactionEvent();
            paymentEvent.setId(2L);
            paymentEvent.setAmount(new BigDecimal("500.00"));
            paymentEvent.setTransactionType(TransactionType.CREDIT);
            paymentEvent.setDocumentType("BankPayment");
            paymentEvent.setIsReversed(false);
            events.add(paymentEvent);

            // Reversal event (should be skipped)
            BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
            reversalEvent.setId(3L);
            reversalEvent.setAmount(new BigDecimal("1000.00"));
            reversalEvent.setTransactionType(TransactionType.CREDIT);
            reversalEvent.setDocumentType("BankReceiptReversal");
            reversalEvent.setIsReversed(false);
            events.add(reversalEvent);

            // Second receipt: +300
            BankAccountTransactionEvent receiptEvent = new BankAccountTransactionEvent();
            receiptEvent.setId(4L);
            receiptEvent.setAmount(new BigDecimal("300.00"));
            receiptEvent.setTransactionType(TransactionType.DEBIT);
            receiptEvent.setDocumentType("BankReceipt");
            receiptEvent.setIsReversed(false);
            events.add(receiptEvent);

            when(bankAccountRepository.findByIdWithRelations(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(balanceSnapshotRepository.existsByBankAccountIdAndSnapshotDateTime(
                    bankAccount.getId(), expectedSnapshotDateTime)).thenReturn(false);
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(bankAccount.getId(), dayStart))
                    .thenReturn(Optional.empty());
            // Mock repository to return different results based on datetime range
            // For calculateBalance call (before start of day): return empty
            // For createSnapshot call (during the day): return test events
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> {
                        LocalDateTime start = invocation.getArgument(1);
                        LocalDateTime dayStart2 = testDateTime.toLocalDate().atStartOfDay();
                        // If querying before start of day, return empty (for calculateBalance)
                        if (start.isBefore(dayStart2)) {
                            return new ArrayList<>();
                        }
                        // Otherwise return events for the day (for createSnapshot)
                        return events;
                    });
            when(balanceSnapshotRepository.save(any(BankAccountBalanceSnapshot.class)))
                    .thenAnswer(invocation -> {
                        BankAccountBalanceSnapshot snapshot = invocation.getArgument(0);
                        snapshot.setId(1L);
                        return snapshot;
                    });

            // When: Create snapshot for the day
            BankAccountBalanceSnapshot snapshot = balanceService.createSnapshot(bankAccount.getId(), testDateTime);

            // Then: Reversal events should be excluded from turnovers
            assertThat(snapshot.getDebitTurnover()).isEqualByComparingTo(new BigDecimal("300.00")); // Only receipt #101
            assertThat(snapshot.getCreditTurnover()).isEqualByComparingTo(new BigDecimal("500.00")); // Only payment #200

            // Closing balance: 0 + 300 - 500 = -200
            assertThat(snapshot.getClosingBalance()).isEqualByComparingTo(new BigDecimal("-200.00"));

            // Events count is set to events.size() which includes ALL events fetched by query
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
            LocalDateTime endOfDay = testDateTime.toLocalDate().plusDays(1).atStartOfDay();

            BankAccountBalanceSnapshot snapshot = new BankAccountBalanceSnapshot();
            snapshot.setId(1L);
            snapshot.setSnapshotDateTime(endOfDay);
            snapshot.setBankAccount(bankAccount);

            when(balanceSnapshotRepository.findByBankAccountIdAndSnapshotDateTimeWithRelations(bankAccount.getId(), endOfDay))
                    .thenReturn(Optional.of(snapshot));

            // When
            BankAccountBalanceSnapshot result = balanceService.getSnapshot(bankAccount.getId(), endOfDay);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSnapshotDateTime()).isEqualTo(endOfDay);
        }

        @Test
        @DisplayName("Should throw exception when snapshot not found")
        void shouldThrowExceptionWhenSnapshotNotFound() {
            // Given: No snapshot created
            when(balanceSnapshotRepository.findByBankAccountIdAndSnapshotDateTimeWithRelations(bankAccount.getId(), testDateTime))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> balanceService.getSnapshot(bankAccount.getId(), testDateTime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Snapshot not found for account " + bankAccount.getId() + " at datetime " + testDateTime);
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
            LocalDateTime searchStartDateTime = startDateTime.toLocalDate().atStartOfDay();
            LocalDateTime searchEndDateTime = endDateTime.toLocalDate().plusDays(1).atStartOfDay();

            LocalDateTime expectedStart = startDateTime.toLocalDate().plusDays(1).atStartOfDay();
            LocalDateTime expectedMiddle = testDateTime.plusDays(3).toLocalDate().plusDays(1).atStartOfDay();

            BankAccountBalanceSnapshot snapshot1 = new BankAccountBalanceSnapshot();
            snapshot1.setId(1L);
            snapshot1.setSnapshotDateTime(expectedStart);
            snapshot1.setBankAccount(bankAccount);

            BankAccountBalanceSnapshot snapshot2 = new BankAccountBalanceSnapshot();
            snapshot2.setId(2L);
            snapshot2.setSnapshotDateTime(expectedMiddle);
            snapshot2.setBankAccount(bankAccount);

            List<BankAccountBalanceSnapshot> snapshots = List.of(snapshot1, snapshot2);

            when(balanceSnapshotRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    bankAccount.getId(), searchStartDateTime, searchEndDateTime))
                    .thenReturn(snapshots);

            // When
            List<BankAccountBalanceSnapshot> result = balanceService.getSnapshotsInDateTimeRange(
                    bankAccount.getId(), searchStartDateTime, searchEndDateTime);

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
            LocalDateTime searchStartDateTime = startDateTime.toLocalDate().atStartOfDay();
            LocalDateTime searchEndDateTime = endDateTime.toLocalDate().atTime(23, 59, 59, 999999999);

            when(balanceSnapshotRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    bankAccount.getId(), searchStartDateTime, searchEndDateTime))
                    .thenReturn(new ArrayList<>());

            // When
            List<BankAccountBalanceSnapshot> result = balanceService.getSnapshotsInDateTimeRange(
                    bankAccount.getId(), searchStartDateTime, searchEndDateTime);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Balance Calculation with Reversal Events Tests")
    class BalanceCalculationWithReversalTests {

        @Test
        @DisplayName("Should exclude reversal events from balance calculation")
        void shouldExcludeReversalEventsFromBalanceCalculation() {
            // Given
            List<BankAccountTransactionEvent> events = new ArrayList<>();

            // No events returned because original is reversed
            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(eq(bankAccount.getId()), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When: Calculate balance
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusDays(1));

            // Then: Balance should be zero (original is reversed, reversal event is excluded)
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("calculateBalance should skip reversal events with endsWith Reversal check")
        void calculateBalanceShouldSkipReversalEventsWithEndsWithCheck() {
            // Given: Create events that will be in the query result
            List<BankAccountTransactionEvent> events = new ArrayList<>();

            // Payment event: -200
            BankAccountTransactionEvent paymentEvent = new BankAccountTransactionEvent();
            paymentEvent.setId(2L);
            paymentEvent.setAmount(new BigDecimal("200.00"));
            paymentEvent.setTransactionType(TransactionType.CREDIT);
            paymentEvent.setDocumentType("BankPayment");
            paymentEvent.setIsReversed(false);
            events.add(paymentEvent);

            // Reversal event (should be skipped by endsWith("Reversal") check)
            BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
            reversalEvent.setId(3L);
            reversalEvent.setAmount(new BigDecimal("500.00"));
            reversalEvent.setTransactionType(TransactionType.CREDIT);
            reversalEvent.setDocumentType("BankReceiptReversal");
            reversalEvent.setIsReversed(false);
            events.add(reversalEvent);

            // Second receipt: +300
            BankAccountTransactionEvent receiptEvent = new BankAccountTransactionEvent();
            receiptEvent.setId(4L);
            receiptEvent.setAmount(new BigDecimal("300.00"));
            receiptEvent.setTransactionType(TransactionType.DEBIT);
            receiptEvent.setDocumentType("BankReceipt");
            receiptEvent.setIsReversed(false);
            events.add(receiptEvent);

            when(balanceSnapshotRepository.findLatestByBankAccountIdBeforeDateTimeWithRelations(eq(bankAccount.getId()), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());
            when(transactionEventRepository.findByBankAccountIdAndDateTimeRangeWithRelations(
                    any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(events);

            // When: Calculate balance at T+5 (after all events)
            BigDecimal balance = balanceService.calculateBalance(bankAccount.getId(), testDateTime.plusHours(5));

            // Then: Balance should be 100 (0 - 200 + 300)
            // The reversal event itself should be skipped due to endsWith("Reversal")
            assertThat(balance).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }
}
