package com.tarasantoniuk.finance.core.externalexchangerate.entity;


import com.tarasantoniuk.finance.common.entity.BaseEntity;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "external_exchange_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exchange_date", "currency_from_id", "currency_to_id", "source"}
        ),
        indexes = {
                @Index(name = "idx_exchange_rate_date_id", columnList = "exchange_date, id")
        }
)
public class ExternalExchangeRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "external_exchange_rate_seq")
    @SequenceGenerator(
            name = "external_exchange_rate_seq",
            sequenceName = "external_exchange_rate_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "exchange_date", nullable = false)
    private LocalDate exchangeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_from_id", nullable = false)
    private Currency currencyFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_to_id", nullable = false)
    private Currency currencyTo;

    @Column(name = "rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(name = "source", nullable = false, length = 100)
    private String source; // ECB, NBU, MONOBANK, PRIVATBANK, etc.

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public ExternalExchangeRate() {
    }

    public ExternalExchangeRate(Long id, LocalDate exchangeDate, Currency currencyFrom,
                                Currency currencyTo, BigDecimal rate, String source,
                                String sourceUrl, Boolean isActive) {
        this.id = id;
        this.exchangeDate = exchangeDate;
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.rate = rate;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.isActive = isActive;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(LocalDate exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    public Currency getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(Currency currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public Currency getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(Currency currencyTo) {
        this.currencyTo = currencyTo;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalExchangeRate that = (ExternalExchangeRate) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(exchangeDate, that.exchangeDate) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, exchangeDate, source);
    }

    @Override
    public String toString() {
        return "ExternalExchangeRate{" +
                "id=" + id +
                ", exchangeDate=" + exchangeDate +
                ", rate=" + rate +
                ", source='" + source + '\'' +
                '}';
    }
}