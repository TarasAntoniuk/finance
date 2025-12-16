package com.tarasantoniuk.finance.common.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Report period information")
public class ReportPeriodDto {

    @Schema(description = "Period start date", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "Period end date", example = "2024-12-31")
    private LocalDate endDate;

    public ReportPeriodDto() {
    }

    public ReportPeriodDto(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Schema(description = "Number of days in period", example = "365")
    public int getDaysCount() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportPeriodDto that = (ReportPeriodDto) o;
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "ReportPeriodDto{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}