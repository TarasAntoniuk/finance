package com.tarasantoniuk.finance.banking.report.accountturnover.dto;

import com.tarasantoniuk.finance.common.report.dto.ReportPeriodDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Schema(description = "Account turnover report for a period")
public class AccountTurnoverReportDto {

    @Schema(description = "Report generation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "Report period")
    private ReportPeriodDto period;

    @Schema(description = "List of account turnovers")
    private List<AccountTurnoverSummaryDto> accounts;

    @Schema(description = "Total number of accounts", example = "5")
    private int totalAccounts;

    @Schema(description = "Summary totals by currency")
    private Map<String, AccountTurnoverTotalDto> summaryByCurrency;

    public AccountTurnoverReportDto() {
    }

    public AccountTurnoverReportDto(LocalDateTime generatedAt, ReportPeriodDto period,
                                    List<AccountTurnoverSummaryDto> accounts, int totalAccounts,
                                    Map<String, AccountTurnoverTotalDto> summaryByCurrency) {
        this.generatedAt = generatedAt;
        this.period = period;
        this.accounts = accounts;
        this.totalAccounts = totalAccounts;
        this.summaryByCurrency = summaryByCurrency;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public ReportPeriodDto getPeriod() {
        return period;
    }

    public void setPeriod(ReportPeriodDto period) {
        this.period = period;
    }

    public List<AccountTurnoverSummaryDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountTurnoverSummaryDto> accounts) {
        this.accounts = accounts;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public Map<String, AccountTurnoverTotalDto> getSummaryByCurrency() {
        return summaryByCurrency;
    }

    public void setSummaryByCurrency(Map<String, AccountTurnoverTotalDto> summaryByCurrency) {
        this.summaryByCurrency = summaryByCurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTurnoverReportDto that = (AccountTurnoverReportDto) o;
        return totalAccounts == that.totalAccounts &&
                Objects.equals(generatedAt, that.generatedAt) &&
                Objects.equals(period, that.period) &&
                Objects.equals(accounts, that.accounts) &&
                Objects.equals(summaryByCurrency, that.summaryByCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generatedAt, period, accounts, totalAccounts, summaryByCurrency);
    }

    @Override
    public String toString() {
        return "AccountTurnoverReportDto{" +
                "generatedAt=" + generatedAt +
                ", period=" + period +
                ", accounts=" + accounts +
                ", totalAccounts=" + totalAccounts +
                ", summaryByCurrency=" + summaryByCurrency +
                '}';
    }
}