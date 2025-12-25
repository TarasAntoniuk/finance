package com.tarasantoniuk.finance.common.report.service;

import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.common.report.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.common.report.enums.PeriodType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 * Service for processing and validating report periods.
 * The client sends period type and date boundaries,
 * and this service validates and applies them.
 */
@Service
public class ReportPeriodService {

    private static final int MAX_PERIOD_DAYS = 365;

    /**
     * Process period parameters from client and create validated ReportPeriodDto.
     * This is the single source of truth for period calculation.
     *
     * @param periodType Period type sent by client (DAY, MONTH, QUARTER, YEAR, CUSTOM)
     * @param startDate  Start date sent by client (required for CUSTOM, optional for others)
     * @param endDate    End date sent by client (required for CUSTOM, optional for others)
     * @return Validated ReportPeriodDto
     * @throws ValidationException if period parameters are invalid
     */
    public ReportPeriodDto processPeriod(PeriodType periodType, LocalDate startDate, LocalDate endDate) {
        // Default to QUARTER if not specified
        PeriodType effectivePeriodType = periodType != null ? periodType : PeriodType.QUARTER;

        LocalDate effectiveStartDate;
        LocalDate effectiveEndDate;

        switch (effectivePeriodType) {
            case DAY:
                effectiveStartDate = resolveDayPeriod(startDate);
                effectiveEndDate = effectiveStartDate;
                break;

            case MONTH:
                effectiveStartDate = resolveMonthStart(startDate);
                effectiveEndDate = resolveMonthEnd(effectiveStartDate);
                break;

            case QUARTER:
                effectiveStartDate = resolveQuarterStart(startDate);
                effectiveEndDate = resolveQuarterEnd(effectiveStartDate);
                break;

            case YEAR:
                effectiveStartDate = resolveYearStart(startDate);
                effectiveEndDate = resolveYearEnd(effectiveStartDate);
                break;

            case CUSTOM:
                if (startDate == null || endDate == null) {
                    throw new ValidationException("Start date and end date are required for CUSTOM period type");
                }
                effectiveStartDate = startDate;
                effectiveEndDate = endDate;
                break;

            default:
                throw new ValidationException("Unsupported period type: " + effectivePeriodType);
        }

        // Validate the period
        validatePeriod(effectiveStartDate, effectiveEndDate);

        return new ReportPeriodDto(effectivePeriodType, effectiveStartDate, effectiveEndDate);
    }

    /**
     * Validate period dates and duration
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_PERIOD_DAYS) {
            throw new ValidationException(
                    String.format("Period cannot exceed %d days. Current period: %d days",
                            MAX_PERIOD_DAYS, daysBetween)
            );
        }
    }

    /**
     * Resolve day period - use provided date or default to today
     */
    private LocalDate resolveDayPeriod(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    /**
     * Resolve month start - first day of the month
     */
    private LocalDate resolveMonthStart(LocalDate date) {
        LocalDate baseDate = date != null ? date : LocalDate.now();
        return baseDate.withDayOfMonth(1);
    }

    /**
     * Resolve month end - last day of the month
     */
    private LocalDate resolveMonthEnd(LocalDate startDate) {
        return startDate.withDayOfMonth(startDate.lengthOfMonth());
    }

    /**
     * Resolve quarter start - first day of the quarter
     */
    private LocalDate resolveQuarterStart(LocalDate date) {
        LocalDate baseDate = date != null ? date : LocalDate.now();
        int currentMonth = baseDate.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
        return baseDate.withMonth(quarterStartMonth).withDayOfMonth(1);
    }

    /**
     * Resolve quarter end - last day of the quarter
     */
    private LocalDate resolveQuarterEnd(LocalDate startDate) {
        int quarterEndMonth = ((startDate.getMonthValue() - 1) / 3) * 3 + 3;
        return startDate.withMonth(quarterEndMonth).withDayOfMonth(
                Month.of(quarterEndMonth).length(startDate.isLeapYear())
        );
    }

    /**
     * Resolve year start - January 1st
     */
    private LocalDate resolveYearStart(LocalDate date) {
        LocalDate baseDate = date != null ? date : LocalDate.now();
        return baseDate.withMonth(1).withDayOfMonth(1);
    }

    /**
     * Resolve year end - December 31st
     */
    private LocalDate resolveYearEnd(LocalDate startDate) {
        return startDate.withMonth(12).withDayOfMonth(31);
    }
}
