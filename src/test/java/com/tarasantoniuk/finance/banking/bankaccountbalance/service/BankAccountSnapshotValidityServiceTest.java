package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.common.snapshot.entity.SnapshotValidity;
import com.tarasantoniuk.finance.common.snapshot.enums.ValidityStatus;
import com.tarasantoniuk.finance.common.snapshot.repository.SnapshotValidityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BankAccountSnapshotValidityService}.
 * <p>
 * Tests the snapshot validity management operations including invalidation,
 * validity checking, and recalculation lifecycle management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountSnapshotValidityService Unit Tests")
class BankAccountSnapshotValidityServiceTest {

    @Mock
    private SnapshotValidityRepository repository;

    @InjectMocks
    private BankAccountSnapshotValidityService validityService;

    private LocalDateTime testDateTime;
    private LocalDate testDate;
    private SnapshotValidity testValidity;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        testDate = testDateTime.toLocalDate();

        testValidity = new SnapshotValidity();
        testValidity.setId(1L);
        testValidity.setSnapshotType("BANK_ACCOUNT_BALANCE");
        testValidity.setEntityId(1L);
        testValidity.setInvalidFromDate(testDate);
        testValidity.setStatus(ValidityStatus.INVALID);
        testValidity.setCausedByEventId(100L);
        testValidity.setInvalidationReason("BACKDATED_TRANSACTION");
    }

    @Nested
    @DisplayName("getSnapshotType Tests")
    class GetSnapshotTypeTests {

        @Test
        @DisplayName("Should return BANK_ACCOUNT_BALANCE as snapshot type")
        void shouldReturnBankAccountBalanceAsSnapshotType() {
            // When - using reflection or indirectly through other methods
            when(repository.isSnapshotValidForDate(eq("BANK_ACCOUNT_BALANCE"), anyLong(), any(LocalDate.class)))
                    .thenReturn(true);

            validityService.isSnapshotValidForDate(1L, testDate);

            // Then
            verify(repository).isSnapshotValidForDate(eq("BANK_ACCOUNT_BALANCE"), eq(1L), eq(testDate));
        }
    }

    @Nested
    @DisplayName("invalidateSnapshots Tests")
    class InvalidateSnapshotsTests {

        @Test
        @DisplayName("Should create new validity record when none exists")
        void shouldCreateNewValidityRecordWhenNoneExists() {
            // Given
            when(repository.findActiveBySnapshotTypeAndEntityId("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.empty());

            // When
            validityService.invalidateSnapshots(1L, testDateTime, 100L);

            // Then
            ArgumentCaptor<SnapshotValidity> captor = ArgumentCaptor.forClass(SnapshotValidity.class);
            verify(repository).save(captor.capture());

            SnapshotValidity saved = captor.getValue();
            assertThat(saved.getSnapshotType()).isEqualTo("BANK_ACCOUNT_BALANCE");
            assertThat(saved.getEntityId()).isEqualTo(1L);
            assertThat(saved.getInvalidFromDate()).isEqualTo(testDate);
            assertThat(saved.getCausedByEventId()).isEqualTo(100L);
            assertThat(saved.getStatus()).isEqualTo(ValidityStatus.INVALID);
            assertThat(saved.getInvalidationReason()).isEqualTo("BACKDATED_TRANSACTION");
        }

        @Test
        @DisplayName("Should update existing record when new invalidation date is earlier")
        void shouldUpdateExistingRecordWhenNewInvalidationDateIsEarlier() {
            // Given
            SnapshotValidity existing = new SnapshotValidity();
            existing.setId(1L);
            existing.setSnapshotType("BANK_ACCOUNT_BALANCE");
            existing.setEntityId(1L);
            existing.setInvalidFromDate(testDate.plusDays(5)); // Later date
            existing.setStatus(ValidityStatus.INVALID);
            existing.setCausedByEventId(99L);

            when(repository.findActiveBySnapshotTypeAndEntityId("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.of(existing));

            LocalDateTime earlierDateTime = testDate.minusDays(2).atStartOfDay();

            // When
            validityService.invalidateSnapshots(1L, earlierDateTime, 100L);

            // Then
            verify(repository).save(existing);
            assertThat(existing.getInvalidFromDate()).isEqualTo(testDate.minusDays(2));
            assertThat(existing.getCausedByEventId()).isEqualTo(100L);
            assertThat(existing.getStatus()).isEqualTo(ValidityStatus.INVALID);
        }

        @Test
        @DisplayName("Should not update existing record when new invalidation date is same")
        void shouldNotUpdateExistingRecordWhenNewInvalidationDateIsSame() {
            // Given
            when(repository.findActiveBySnapshotTypeAndEntityId("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.of(testValidity));

            // When
            validityService.invalidateSnapshots(1L, testDateTime, 100L);

            // Then
            verify(repository, never()).save(any(SnapshotValidity.class));
        }

        @Test
        @DisplayName("Should not update existing record when new invalidation date is later")
        void shouldNotUpdateExistingRecordWhenNewInvalidationDateIsLater() {
            // Given
            when(repository.findActiveBySnapshotTypeAndEntityId("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.of(testValidity));

            LocalDateTime laterDateTime = testDate.plusDays(3).atStartOfDay();

            // When
            validityService.invalidateSnapshots(1L, laterDateTime, 101L);

            // Then
            verify(repository, never()).save(any(SnapshotValidity.class));
        }

        @Test
        @DisplayName("Should handle null causedByEventId")
        void shouldHandleNullCausedByEventId() {
            // Given
            when(repository.findActiveBySnapshotTypeAndEntityId("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.empty());

            // When
            validityService.invalidateSnapshots(1L, testDateTime, null);

            // Then
            ArgumentCaptor<SnapshotValidity> captor = ArgumentCaptor.forClass(SnapshotValidity.class);
            verify(repository).save(captor.capture());

            SnapshotValidity saved = captor.getValue();
            assertThat(saved.getCausedByEventId()).isNull();
        }
    }

    @Nested
    @DisplayName("isSnapshotValidForDate Tests")
    class IsSnapshotValidForDateTests {

        @Test
        @DisplayName("Should return true when no invalid records exist")
        void shouldReturnTrueWhenNoInvalidRecordsExist() {
            // Given
            when(repository.isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, testDate))
                    .thenReturn(true);

            // When
            boolean result = validityService.isSnapshotValidForDate(1L, testDate);

            // Then
            assertThat(result).isTrue();
            verify(repository).isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, testDate);
        }

        @Test
        @DisplayName("Should return false when invalid records exist for date")
        void shouldReturnFalseWhenInvalidRecordsExistForDate() {
            // Given
            when(repository.isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, testDate))
                    .thenReturn(false);

            // When
            boolean result = validityService.isSnapshotValidForDate(1L, testDate);

            // Then
            assertThat(result).isFalse();
            verify(repository).isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, testDate);
        }

        @Test
        @DisplayName("Should check validity for different dates")
        void shouldCheckValidityForDifferentDates() {
            // Given
            LocalDate futureDate = testDate.plusDays(10);
            when(repository.isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, futureDate))
                    .thenReturn(true);

            // When
            boolean result = validityService.isSnapshotValidForDate(1L, futureDate);

            // Then
            assertThat(result).isTrue();
            verify(repository).isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 1L, futureDate);
        }

        @Test
        @DisplayName("Should check validity for different accounts")
        void shouldCheckValidityForDifferentAccounts() {
            // Given
            when(repository.isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 2L, testDate))
                    .thenReturn(true);

            // When
            boolean result = validityService.isSnapshotValidForDate(2L, testDate);

            // Then
            assertThat(result).isTrue();
            verify(repository).isSnapshotValidForDate("BANK_ACCOUNT_BALANCE", 2L, testDate);
        }
    }

    @Nested
    @DisplayName("getInvalidFromDate Tests")
    class GetInvalidFromDateTests {

        @Test
        @DisplayName("Should return invalidFromDate when invalid records exist")
        void shouldReturnInvalidFromDateWhenInvalidRecordsExist() {
            // Given
            when(repository.getInvalidFromDate("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.of(testDate));

            // When
            Optional<LocalDate> result = validityService.getInvalidFromDate(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testDate);
            verify(repository).getInvalidFromDate("BANK_ACCOUNT_BALANCE", 1L);
        }

        @Test
        @DisplayName("Should return empty when no invalid records exist")
        void shouldReturnEmptyWhenNoInvalidRecordsExist() {
            // Given
            when(repository.getInvalidFromDate("BANK_ACCOUNT_BALANCE", 1L))
                    .thenReturn(Optional.empty());

            // When
            Optional<LocalDate> result = validityService.getInvalidFromDate(1L);

            // Then
            assertThat(result).isEmpty();
            verify(repository).getInvalidFromDate("BANK_ACCOUNT_BALANCE", 1L);
        }

        @Test
        @DisplayName("Should return correct date for different accounts")
        void shouldReturnCorrectDateForDifferentAccounts() {
            // Given
            LocalDate differentDate = testDate.minusDays(5);
            when(repository.getInvalidFromDate("BANK_ACCOUNT_BALANCE", 2L))
                    .thenReturn(Optional.of(differentDate));

            // When
            Optional<LocalDate> result = validityService.getInvalidFromDate(2L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(differentDate);
        }
    }

    @Nested
    @DisplayName("markAsRecalculating Tests")
    class MarkAsRecalculatingTests {

        @Test
        @DisplayName("Should update status to RECALCULATING")
        void shouldUpdateStatusToRecalculating() {
            // Given
            testValidity.setStatus(ValidityStatus.INVALID);

            // When
            validityService.markAsRecalculating(testValidity);

            // Then
            assertThat(testValidity.getStatus()).isEqualTo(ValidityStatus.RECALCULATING);
            verify(repository).save(testValidity);
        }

        @Test
        @DisplayName("Should save validity record")
        void shouldSaveValidityRecord() {
            // Given
            SnapshotValidity validity = new SnapshotValidity();
            validity.setId(1L);
            validity.setSnapshotType("BANK_ACCOUNT_BALANCE");
            validity.setEntityId(1L);
            validity.setStatus(ValidityStatus.INVALID);

            // When
            validityService.markAsRecalculating(validity);

            // Then
            verify(repository).save(validity);
        }

        @Test
        @DisplayName("Should handle already RECALCULATING status")
        void shouldHandleAlreadyRecalculatingStatus() {
            // Given
            testValidity.setStatus(ValidityStatus.RECALCULATING);

            // When
            validityService.markAsRecalculating(testValidity);

            // Then
            assertThat(testValidity.getStatus()).isEqualTo(ValidityStatus.RECALCULATING);
            verify(repository).save(testValidity);
        }
    }

    @Nested
    @DisplayName("deleteValidity Tests")
    class DeleteValidityTests {

        @Test
        @DisplayName("Should delete validity record")
        void shouldDeleteValidityRecord() {
            // When
            validityService.deleteValidity(testValidity);

            // Then
            verify(repository).delete(testValidity);
        }

        @Test
        @DisplayName("Should delete record regardless of status")
        void shouldDeleteRecordRegardlessOfStatus() {
            // Given
            testValidity.setStatus(ValidityStatus.RECALCULATING);

            // When
            validityService.deleteValidity(testValidity);

            // Then
            verify(repository).delete(testValidity);
        }

        @Test
        @DisplayName("Should delete record for any account")
        void shouldDeleteRecordForAnyAccount() {
            // Given
            testValidity.setEntityId(999L);

            // When
            validityService.deleteValidity(testValidity);

            // Then
            verify(repository).delete(testValidity);
        }
    }

    @Nested
    @DisplayName("getAllInvalidRecords Tests")
    class GetAllInvalidRecordsTests {

        @Test
        @DisplayName("Should return only INVALID status records")
        void shouldReturnOnlyInvalidStatusRecords() {
            // Given
            List<SnapshotValidity> invalidRecords = List.of(testValidity);
            when(repository.findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID))
                    .thenReturn(invalidRecords);

            // When
            List<SnapshotValidity> result = validityService.getAllInvalidRecords();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testValidity);
            assertThat(result.get(0).getStatus()).isEqualTo(ValidityStatus.INVALID);
            verify(repository).findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID);
        }

        @Test
        @DisplayName("Should return empty list when no invalid records exist")
        void shouldReturnEmptyListWhenNoInvalidRecordsExist() {
            // Given
            when(repository.findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID))
                    .thenReturn(Collections.emptyList());

            // When
            List<SnapshotValidity> result = validityService.getAllInvalidRecords();

            // Then
            assertThat(result).isEmpty();
            verify(repository).findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID);
        }

        @Test
        @DisplayName("Should return multiple invalid records")
        void shouldReturnMultipleInvalidRecords() {
            // Given
            SnapshotValidity validity1 = new SnapshotValidity();
            validity1.setId(1L);
            validity1.setEntityId(1L);
            validity1.setStatus(ValidityStatus.INVALID);

            SnapshotValidity validity2 = new SnapshotValidity();
            validity2.setId(2L);
            validity2.setEntityId(2L);
            validity2.setStatus(ValidityStatus.INVALID);

            List<SnapshotValidity> invalidRecords = List.of(validity1, validity2);
            when(repository.findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID))
                    .thenReturn(invalidRecords);

            // When
            List<SnapshotValidity> result = validityService.getAllInvalidRecords();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(validity1, validity2);
        }

        @Test
        @DisplayName("Should not return RECALCULATING status records")
        void shouldNotReturnRecalculatingStatusRecords() {
            // Given
            when(repository.findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID))
                    .thenReturn(Collections.emptyList());

            // When
            List<SnapshotValidity> result = validityService.getAllInvalidRecords();

            // Then
            assertThat(result).isEmpty();
            // Verify it specifically asks for INVALID, not RECALCULATING
            verify(repository).findBySnapshotTypeAndStatus("BANK_ACCOUNT_BALANCE", ValidityStatus.INVALID);
            verify(repository, never()).findBySnapshotTypeAndStatus(any(), eq(ValidityStatus.RECALCULATING));
        }
    }
}
