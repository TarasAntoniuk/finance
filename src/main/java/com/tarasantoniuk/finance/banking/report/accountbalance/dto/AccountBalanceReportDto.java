package com.tarasantoniuk.finance.banking.report.accountbalance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Schema(description = "Account balance report")
public class AccountBalanceReportDto {

    @Schema(description = "Report generation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "Report date (as of date)", example = "2024-01-15")
    private LocalDate reportDate;

    @Schema(description = "List of account balances")
    private List<AccountBalanceItemDto> accounts;

    @Schema(description = "Total number of accounts", example = "5")
    private int totalAccounts;

    @Schema(description = "Totals grouped by organization and currency")
    private Map<String, Map<String, BigDecimal>> totalsByOrganization;

    @Schema(description = "Grand total by currency")
    private Map<String, BigDecimal> grandTotalByCurrency;

    public AccountBalanceReportDto() {
    }

    public AccountBalanceReportDto(LocalDateTime generatedAt, LocalDate reportDate,
                                   List<AccountBalanceItemDto> accounts, int totalAccounts,
                                   Map<String, Map<String, BigDecimal>> totalsByOrganization,
                                   Map<String, BigDecimal> grandTotalByCurrency) {
        this.generatedAt = generatedAt;
        this.reportDate = reportDate;
        this.accounts = accounts;
        this.totalAccounts = totalAccounts;
        this.totalsByOrganization = totalsByOrganization;
        this.grandTotalByCurrency = grandTotalByCurrency;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public List<AccountBalanceItemDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountBalanceItemDto> accounts) {
        this.accounts = accounts;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public Map<String, Map<String, BigDecimal>> getTotalsByOrganization() {
        return totalsByOrganization;
    }

    public void setTotalsByOrganization(Map<String, Map<String, BigDecimal>> totalsByOrganization) {
        this.totalsByOrganization = totalsByOrganization;
    }

    public Map<String, BigDecimal> getGrandTotalByCurrency() {
        return grandTotalByCurrency;
    }

    public void setGrandTotalByCurrency(Map<String, BigDecimal> grandTotalByCurrency) {
        this.grandTotalByCurrency = grandTotalByCurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountBalanceReportDto that = (AccountBalanceReportDto) o;
        return totalAccounts == that.totalAccounts &&
                Objects.equals(generatedAt, that.generatedAt) &&
                Objects.equals(reportDate, that.reportDate) &&
                Objects.equals(accounts, that.accounts) &&
                Objects.equals(totalsByOrganization, that.totalsByOrganization) &&
                Objects.equals(grandTotalByCurrency, that.grandTotalByCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generatedAt, reportDate, accounts, totalAccounts,
                totalsByOrganization, grandTotalByCurrency);
    }

    @Override
    public String toString() {
        return "AccountBalanceReportDto{" +
                "generatedAt=" + generatedAt +
                ", reportDate=" + reportDate +
                ", accounts=" + accounts +
                ", totalAccounts=" + totalAccounts +
                ", totalsByOrganization=" + totalsByOrganization +
                ", grandTotalByCurrency=" + grandTotalByCurrency +
                '}';
    }
}