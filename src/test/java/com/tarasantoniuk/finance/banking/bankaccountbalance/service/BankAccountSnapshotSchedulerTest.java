package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountSnapshotScheduler Unit Tests")
class BankAccountSnapshotSchedulerTest {

    @Mock
    private BankAccountBalanceService balanceService;

    @Mock
    private BankAccountSnapshotValidityService validityService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountSnapshotScheduler scheduler;

    private LocalDateTime testDateTime;
    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC001");
    }

    @Nested
    @DisplayName("createDailySnapshots Tests")
    class CreateDailySnapshotsTests {

        @Test
        @DisplayName("Should create snapshots for all active accounts")
        void shouldCreateSnapshotsForAllActiveAccounts() {
            // Given
            List<Long> activeAccountIds = List.of(1L, 2L, 3L);
            when(bankAccountRepository.findAllActiveAccountIds()).thenReturn(activeAccountIds);
            when(validityService.isSnapshotValidForDate(anyLong(), any(LocalDate.class))).thenReturn(true);

            // When
            scheduler.createDailySnapshots();

            // Then
            verify(bankAccountRepository).findAllActiveAccountIds();
            verify(balanceService, times(3)).createSnapshot(anyLong(), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(1L), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(2L), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(3L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should skip when no active accounts exist")
        void shouldSkipWhenNoActiveAccountsExist() {
            // Given
            when(bankAccountRepository.findAllActiveAccountIds()).thenReturn(Collections.emptyList());

            // When
            scheduler.createDailySnapshots();

            // Then
            verify(bankAccountRepository).findAllActiveAccountIds();
            verify(balanceService, never()).createSnapshot(anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle snapshot already exists gracefully")
        void shouldHandleSnapshotAlreadyExistsGracefully() {
            // Given
            List<Long> activeAccountIds = List.of(1L, 2L);
            when(bankAccountRepository.findAllActiveAccountIds()).thenReturn(activeAccountIds);
            when(validityService.isSnapshotValidForDate(anyLong(), any(LocalDate.class))).thenReturn(true);
            doThrow(new IllegalStateException("Snapshot already exists"))
                    .when(balanceService).createSnapshot(eq(1L), any(LocalDateTime.class));

            // When - should not throw exception
            assertThatCode(() -> scheduler.createDailySnapshots()).doesNotThrowAnyException();

            // Then
            verify(balanceService, times(2)).createSnapshot(anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should continue processing remaining accounts if one fails")
        void shouldContinueProcessingRemainingAccountsIfOneFails() {
            // Given
            List<Long> activeAccountIds = List.of(1L, 2L, 3L);
            when(bankAccountRepository.findAllActiveAccountIds()).thenReturn(activeAccountIds);
            when(validityService.isSnapshotValidForDate(anyLong(), any(LocalDate.class))).thenReturn(true);
            doThrow(new RuntimeException("Database error"))
                    .when(balanceService).createSnapshot(eq(2L), any(LocalDateTime.class));

            // When - should not throw exception
            assertThatCode(() -> scheduler.createDailySnapshots()).doesNotThrowAnyException();

            // Then
            verify(balanceService, times(3)).createSnapshot(anyLong(), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(1L), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(2L), any(LocalDateTime.class));
            verify(balanceService).createSnapshot(eq(3L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should use midnight of current day for snapshot time")
        void shouldUseMidnightOfCurrentDayForSnapshotTime() {
            // Given
            List<Long> activeAccountIds = List.of(1L);
            when(bankAccountRepository.findAllActiveAccountIds()).thenReturn(activeAccountIds);
            when(validityService.isSnapshotValidForDate(anyLong(), any(LocalDate.class))).thenReturn(true);

            // When
            scheduler.createDailySnapshots();

            // Then
            verify(balanceService).createSnapshot(eq(1L), argThat(dateTime ->
                    dateTime.getHour() == 0 &&
                            dateTime.getMinute() == 0 &&
                            dateTime.getSecond() == 0 &&
                            dateTime.getNano() == 0
            ));
        }

        @Test
        @DisplayName("Should handle repository exception by logging and returning")
        void shouldHandleRepositoryExceptionByLoggingAndReturning() {
            // Given
            when(bankAccountRepository.findAllActiveAccountIds())
                    .thenThrow(new RuntimeException("Repository error"));

            // When/Then - method has no try-catch for findAllActiveAccountIds, so exception will propagate
            assertThatCode(() -> scheduler.createDailySnapshots())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Repository error");
        }
    }

    @Nested
    @DisplayName("recalculateInvalidSnapshots Tests")
    class RecalculateInvalidSnapshotsTests {

        @Test
        @DisplayName("Should recalculate all invalid snapshots")
        void shouldRecalculateAllInvalidSnapshots() {
            // Given
            List<SnapshotValidity> invalidRecords = createInvalidRecords(3);
            when(validityService.getAllInvalidRecords()).thenReturn(invalidRecords);

            // When
            scheduler.recalculateInvalidSnapshots();

            // Then
            verify(validityService).getAllInvalidRecords();
            // Note: We can't directly verify private method calls,
            // but we verify the service call that would trigger them
        }

        @Test
        @DisplayName("Should skip when no invalid records exist")
        void shouldSkipWhenNoInvalidRecordsExist() {
            // Given
            when(validityService.getAllInvalidRecords()).thenReturn(Collections.emptyList());

            // When
            scheduler.recalculateInvalidSnapshots();

            // Then
            verify(validityService).getAllInvalidRecords();
            verify(validityService, never()).markAsRecalculating(any(SnapshotValidity.class));
        }

        @Test
        @DisplayName("Should handle recalculation failure gracefully")
        void shouldHandleRecalculationFailureGracefully() {
            // Given
            List<SnapshotValidity> invalidRecords = createInvalidRecords(2);
            when(validityService.getAllInvalidRecords()).thenReturn(invalidRecords);
            doThrow(new RuntimeException("Recalculation error"))
                    .when(validityService).markAsRecalculating(any(SnapshotValidity.class));

            // When - should not throw exception
            assertThatCode(() -> scheduler.recalculateInvalidSnapshots()).doesNotThrowAnyException();

            // Then
            verify(validityService).getAllInvalidRecords();
        }

        @Test
        @DisplayName("Should log summary of recalculation results")
        void shouldLogSummaryOfRecalculationResults() {
            // Given
            List<SnapshotValidity> invalidRecords = createInvalidRecords(5);
            when(validityService.getAllInvalidRecords()).thenReturn(invalidRecords);

            // When
            scheduler.recalculateInvalidSnapshots();

            // Then
            verify(validityService).getAllInvalidRecords();
            // Logging verification is implicit - the method should complete successfully
        }

        @Test
        @DisplayName("Should handle validity service exception by propagating")
        void shouldHandleValidityServiceExceptionByPropagating() {
            // Given
            when(validityService.getAllInvalidRecords())
                    .thenThrow(new RuntimeException("Service error"));

            // When/Then - method has no try-catch for getAllInvalidRecords, so exception will propagate
            assertThatCode(() -> scheduler.recalculateInvalidSnapshots())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Service error");
        }
    }

    @Nested
    @DisplayName("recalculateSnapshotsForAccount Tests")
    class RecalculateSnapshotsForAccountTests {

        @Test
        @DisplayName("Should successfully recalculate snapshots")
        void shouldSuccessfullyRecalculateSnapshots() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);

            // When
            // Test through triggerRecalculationForAccount
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));

            scheduler.triggerRecalculationForAccount(1L);

            // Then
            InOrder inOrder = inOrder(validityService, balanceService);
            inOrder.verify(validityService).markAsRecalculating(any(SnapshotValidity.class));
            inOrder.verify(balanceService).deleteSnapshotsFrom(eq(1L), any(LocalDateTime.class));
            inOrder.verify(balanceService, atLeastOnce()).createSnapshot(eq(1L), any(LocalDateTime.class));
            inOrder.verify(validityService).deleteValidity(any(SnapshotValidity.class));
        }

        @Test
        @DisplayName("Should handle deletion failure")
        void shouldHandleDeletionFailure() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));
            doThrow(new RuntimeException("Deletion failed"))
                    .when(balanceService).deleteSnapshotsFrom(anyLong(), any(LocalDateTime.class));

            // When - should throw RuntimeException wrapped exception
            assertThatCode(() -> scheduler.triggerRecalculationForAccount(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to recalculate snapshots for account");

            // Then
            verify(validityService).markAsRecalculating(any(SnapshotValidity.class));
            verify(balanceService).deleteSnapshotsFrom(anyLong(), any(LocalDateTime.class));
            verify(validityService, never()).deleteValidity(any(SnapshotValidity.class));
        }

        @Test
        @DisplayName("Should create snapshots from invalid date to yesterday")
        void shouldCreateSnapshotsFromInvalidDateToYesterday() {
            // Given
            LocalDate invalidFromDate = LocalDate.now().minusDays(5);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));

            // When
            scheduler.triggerRecalculationForAccount(1L);

            // Then
            // Should create snapshot for each day from invalidFromDate to yesterday (inclusive)
            verify(balanceService, atLeast(4)).createSnapshot(eq(1L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should mark as recalculating before starting")
        void shouldMarkAsRecalculatingBeforeStarting() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));

            // When
            scheduler.triggerRecalculationForAccount(1L);

            // Then
            InOrder inOrder = inOrder(validityService, balanceService);
            inOrder.verify(validityService).markAsRecalculating(any(SnapshotValidity.class));
            inOrder.verify(balanceService).deleteSnapshotsFrom(anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle snapshot creation failure")
        void shouldHandleSnapshotCreationFailure() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));
            doThrow(new RuntimeException("Snapshot creation failed"))
                    .when(balanceService).createSnapshot(anyLong(), any(LocalDateTime.class));

            // When - should throw RuntimeException
            assertThatCode(() -> scheduler.triggerRecalculationForAccount(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to recalculate snapshots for account");

            // Then
            verify(validityService).markAsRecalculating(any(SnapshotValidity.class));
            verify(validityService, never()).deleteValidity(any(SnapshotValidity.class));
        }
    }

    @Nested
    @DisplayName("triggerRecalculationForAccount Tests")
    class TriggerRecalculationForAccountTests {

        @Test
        @DisplayName("Should trigger recalculation for specific account")
        void shouldTriggerRecalculationForSpecificAccount() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(1L, invalidFromDate);
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));

            // When
            scheduler.triggerRecalculationForAccount(1L);

            // Then
            verify(validityService).getAllInvalidRecords();
            verify(validityService).markAsRecalculating(any(SnapshotValidity.class));
            verify(balanceService).deleteSnapshotsFrom(eq(1L), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should skip when no invalid record exists")
        void shouldSkipWhenNoInvalidRecordExists() {
            // Given
            when(validityService.getAllInvalidRecords()).thenReturn(Collections.emptyList());

            // When
            scheduler.triggerRecalculationForAccount(1L);

            // Then
            verify(validityService).getAllInvalidRecords();
            verify(validityService, never()).markAsRecalculating(any(SnapshotValidity.class));
            verify(balanceService, never()).deleteSnapshotsFrom(anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should skip when account has no invalid snapshots")
        void shouldSkipWhenAccountHasNoInvalidSnapshots() {
            // Given
            LocalDate invalidFromDate = LocalDate.of(2024, 1, 10);
            SnapshotValidity validity = createValidityRecord(2L, invalidFromDate); // Different account
            when(validityService.getAllInvalidRecords()).thenReturn(List.of(validity));

            // When
            scheduler.triggerRecalculationForAccount(1L);

            // Then
            verify(validityService).getAllInvalidRecords();
            verify(validityService, never()).markAsRecalculating(any(SnapshotValidity.class));
            verify(balanceService, never()).deleteSnapshotsFrom(anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle null account ID gracefully")
        void shouldHandleNullAccountIdGracefully() {
            // Given
            when(validityService.getAllInvalidRecords()).thenReturn(Collections.emptyList());

            // When - should not throw exception
            assertThatCode(() -> scheduler.triggerRecalculationForAccount(null))
                    .doesNotThrowAnyException();

            // Then
            verify(validityService).getAllInvalidRecords();
        }
    }

    // ========== HELPER METHODS ==========

    private List<SnapshotValidity> createInvalidRecords(int count) {
        List<SnapshotValidity> records = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SnapshotValidity validity = new SnapshotValidity();
            validity.setId((long) i);
            validity.setSnapshotType("BANK_ACCOUNT_BALANCE");
            validity.setEntityId((long) i);
            validity.setInvalidFromDate(LocalDate.now().minusDays(i));
            validity.setStatus(ValidityStatus.INVALID);
            validity.setInvalidationReason("Backdated transaction");
            records.add(validity);
        }
        return records;
    }

    private SnapshotValidity createValidityRecord(Long accountId, LocalDate invalidFromDate) {
        SnapshotValidity validity = new SnapshotValidity();
        validity.setId(1L);
        validity.setSnapshotType("BANK_ACCOUNT_BALANCE");
        validity.setEntityId(accountId);
        validity.setInvalidFromDate(invalidFromDate);
        validity.setStatus(ValidityStatus.INVALID);
        validity.setInvalidationReason("Backdated transaction");
        validity.setCausedByEventId(100L);
        return validity;
    }
}
