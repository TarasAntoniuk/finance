package com.tarasantoniuk.finance.common.period.service;

import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.period.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.common.period.enums.PeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportPeriodService Unit Tests")
class ReportPeriodServiceTest {

    private ReportPeriodService service;

    @BeforeEach
    void setUp() {
        service = new ReportPeriodService();
    }

    // ==================== DAY Period Tests ====================

    @Test
    @DisplayName("Should process DAY period with provided date")
    void processPeriod_ShouldProcessDayPeriod_WithProvidedDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 10, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.DAY, date, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.DAY, result.getPeriodType());
        assertEquals(date, result.getStartDate());
        assertEquals(date, result.getEndDate());
    }

    @Test
    @DisplayName("Should process DAY period with today when no date provided")
    void processPeriod_ShouldProcessDayPeriod_WithToday() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.DAY, null, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.DAY, result.getPeriodType());
        assertEquals(today, result.getStartDate());
        assertEquals(today, result.getEndDate());
    }

    // ==================== MONTH Period Tests ====================

    @Test
    @DisplayName("Should process MONTH period with provided date")
    void processPeriod_ShouldProcessMonthPeriod_WithProvidedDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 10, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.MONTH, date, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.MONTH, result.getPeriodType());
        assertEquals(LocalDate.of(2025, 10, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 10, 31), result.getEndDate());
    }

    @Test
    @DisplayName("Should process MONTH period with current month when no date provided")
    void processPeriod_ShouldProcessMonthPeriod_WithCurrentMonth() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate expectedStart = today.withDayOfMonth(1);
        LocalDate expectedEnd = today.withDayOfMonth(today.lengthOfMonth());

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.MONTH, null, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.MONTH, result.getPeriodType());
        assertEquals(expectedStart, result.getStartDate());
        assertEquals(expectedEnd, result.getEndDate());
    }

    @Test
    @DisplayName("Should process MONTH period for February in leap year")
    void processPeriod_ShouldProcessMonthPeriod_ForLeapYear() {
        // Given
        LocalDate dateInLeapYear = LocalDate.of(2024, 2, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.MONTH, dateInLeapYear, null);

        // Then
        assertEquals(LocalDate.of(2024, 2, 1), result.getStartDate());
        assertEquals(LocalDate.of(2024, 2, 29), result.getEndDate());
    }

    // ==================== QUARTER Period Tests ====================

    @Test
    @DisplayName("Should process QUARTER period for Q1")
    void processPeriod_ShouldProcessQuarterPeriod_ForQ1() {
        // Given
        LocalDate dateInQ1 = LocalDate.of(2025, 2, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.QUARTER, dateInQ1, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.QUARTER, result.getPeriodType());
        assertEquals(LocalDate.of(2025, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 31), result.getEndDate());
    }

    @Test
    @DisplayName("Should process QUARTER period for Q2")
    void processPeriod_ShouldProcessQuarterPeriod_ForQ2() {
        // Given
        LocalDate dateInQ2 = LocalDate.of(2025, 5, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.QUARTER, dateInQ2, null);

        // Then
        assertEquals(LocalDate.of(2025, 4, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 6, 30), result.getEndDate());
    }

    @Test
    @DisplayName("Should process QUARTER period for Q3")
    void processPeriod_ShouldProcessQuarterPeriod_ForQ3() {
        // Given
        LocalDate dateInQ3 = LocalDate.of(2025, 8, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.QUARTER, dateInQ3, null);

        // Then
        assertEquals(LocalDate.of(2025, 7, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 9, 30), result.getEndDate());
    }

    @Test
    @DisplayName("Should process QUARTER period for Q4")
    void processPeriod_ShouldProcessQuarterPeriod_ForQ4() {
        // Given
        LocalDate dateInQ4 = LocalDate.of(2025, 11, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.QUARTER, dateInQ4, null);

        // Then
        assertEquals(LocalDate.of(2025, 10, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getEndDate());
    }

    @Test
    @DisplayName("Should process QUARTER period with current quarter when no date provided")
    void processPeriod_ShouldProcessQuarterPeriod_WithCurrentQuarter() {
        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.QUARTER, null, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.QUARTER, result.getPeriodType());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
    }

    // ==================== YEAR Period Tests ====================

    @Test
    @DisplayName("Should process YEAR period with provided date")
    void processPeriod_ShouldProcessYearPeriod_WithProvidedDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 6, 15);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.YEAR, date, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.YEAR, result.getPeriodType());
        assertEquals(LocalDate.of(2025, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getEndDate());
    }

    @Test
    @DisplayName("Should process YEAR period with current year when no date provided")
    void processPeriod_ShouldProcessYearPeriod_WithCurrentYear() {
        // Given
        int currentYear = LocalDate.now().getYear();

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.YEAR, null, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.YEAR, result.getPeriodType());
        assertEquals(LocalDate.of(currentYear, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(currentYear, 12, 31), result.getEndDate());
    }

    // ==================== CUSTOM Period Tests ====================

    @Test
    @DisplayName("Should process CUSTOM period with both dates")
    void processPeriod_ShouldProcessCustomPeriod_WithBothDates() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);

        // When
        ReportPeriodDto result = service.processPeriod(PeriodType.CUSTOM, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.CUSTOM, result.getPeriodType());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
    }

    @Test
    @DisplayName("Should throw exception for CUSTOM period without start date")
    void processPeriod_ShouldThrowException_WhenCustomPeriodMissingStartDate() {
        // Given
        LocalDate endDate = LocalDate.of(2025, 11, 30);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.processPeriod(PeriodType.CUSTOM, null, endDate);
        });

        assertEquals("Start date and end date are required for CUSTOM period type", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for CUSTOM period without end date")
    void processPeriod_ShouldThrowException_WhenCustomPeriodMissingEndDate() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 10, 1);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.processPeriod(PeriodType.CUSTOM, startDate, null);
        });

        assertEquals("Start date and end date are required for CUSTOM period type", exception.getMessage());
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void processPeriod_ShouldThrowException_WhenStartDateAfterEndDate() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 1);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.processPeriod(PeriodType.CUSTOM, startDate, endDate);
        });

        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when period exceeds 365 days")
    void processPeriod_ShouldThrowException_WhenPeriodExceeds365Days() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 2);  // 367 days

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.processPeriod(PeriodType.CUSTOM, startDate, endDate);
        });

        assertTrue(exception.getMessage().contains("Period cannot exceed 365 days"));
    }

    @Test
    @DisplayName("Should accept period of exactly 365 days")
    void processPeriod_ShouldAcceptPeriod_Of365Days() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);  // Exactly 365 days

        // When & Then
        assertDoesNotThrow(() -> {
            service.processPeriod(PeriodType.CUSTOM, startDate, endDate);
        });
    }

    // ==================== Default Period Type Tests ====================

    @Test
    @DisplayName("Should default to QUARTER when period type is null")
    void processPeriod_ShouldDefaultToQuarter_WhenPeriodTypeIsNull() {
        // When
        ReportPeriodDto result = service.processPeriod(null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(PeriodType.QUARTER, result.getPeriodType());
    }
}
