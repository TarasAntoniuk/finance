package com.tarasantoniuk.finance.banking.report.accountturnover.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Account turnover totals for a currency")
public class AccountTurnoverTotalDto {

    @Schema(description = "Total opening balance", example = "0.00")
    private BigDecimal totalOpeningBalance;

    @Schema(description = "Total debit turnover", example = "150000.00")
    private BigDecimal totalDebitTurnover;

    @Schema(description = "Total credit turnover", example = "100000.00")
    private BigDecimal totalCreditTurnover;

    @Schema(description = "Total closing balance", example = "50000.00")
    private BigDecimal totalClosingBalance;

    @Schema(description = "Total transaction count", example = "25")
    private int totalTransactionCount;

    public AccountTurnoverTotalDto() {
        this.totalOpeningBalance = BigDecimal.ZERO;
        this.totalDebitTurnover = BigDecimal.ZERO;
        this.totalCreditTurnover = BigDecimal.ZERO;
        this.totalClosingBalance = BigDecimal.ZERO;
        this.totalTransactionCount = 0;
    }

    public AccountTurnoverTotalDto(BigDecimal totalOpeningBalance, BigDecimal totalDebitTurnover,
                                   BigDecimal totalCreditTurnover, BigDecimal totalClosingBalance,
                                   int totalTransactionCount) {
        this.totalOpeningBalance = totalOpeningBalance;
        this.totalDebitTurnover = totalDebitTurnover;
        this.totalCreditTurnover = totalCreditTurnover;
        this.totalClosingBalance = totalClosingBalance;
        this.totalTransactionCount = totalTransactionCount;
    }

    public BigDecimal getTotalOpeningBalance() {
        return totalOpeningBalance;
    }

    public void setTotalOpeningBalance(BigDecimal totalOpeningBalance) {
        this.totalOpeningBalance = totalOpeningBalance;
    }

    public BigDecimal getTotalDebitTurnover() {
        return totalDebitTurnover;
    }

    public void setTotalDebitTurnover(BigDecimal totalDebitTurnover) {
        this.totalDebitTurnover = totalDebitTurnover;
    }

    public BigDecimal getTotalCreditTurnover() {
        return totalCreditTurnover;
    }

    public void setTotalCreditTurnover(BigDecimal totalCreditTurnover) {
        this.totalCreditTurnover = totalCreditTurnover;
    }

    public BigDecimal getTotalClosingBalance() {
        return totalClosingBalance;
    }

    public void setTotalClosingBalance(BigDecimal totalClosingBalance) {
        this.totalClosingBalance = totalClosingBalance;
    }

    public int getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public void setTotalTransactionCount(int totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTurnoverTotalDto that = (AccountTurnoverTotalDto) o;
        return totalTransactionCount == that.totalTransactionCount &&
                Objects.equals(totalOpeningBalance, that.totalOpeningBalance) &&
                Objects.equals(totalDebitTurnover, that.totalDebitTurnover) &&
                Objects.equals(totalCreditTurnover, that.totalCreditTurnover) &&
                Objects.equals(totalClosingBalance, that.totalClosingBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOpeningBalance, totalDebitTurnover, totalCreditTurnover,
                totalClosingBalance, totalTransactionCount);
    }

    @Override
    public String toString() {
        return "AccountTurnoverTotalDto{" +
                "totalOpeningBalance=" + totalOpeningBalance +
                ", totalDebitTurnover=" + totalDebitTurnover +
                ", totalCreditTurnover=" + totalCreditTurnover +
                ", totalClosingBalance=" + totalClosingBalance +
                ", totalTransactionCount=" + totalTransactionCount +
                '}';
    }
}
