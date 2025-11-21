package com.tarasantoniuk.finance.core.currency.entity;


import com.tarasantoniuk.finance.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code; // ISO 4217 alphabetic code (USD, EUR, UAH)

    @Column(name = "numeric_code", nullable = false, unique = true, length = 3)
    private String numericCode; // ISO 4217 numeric code (840, 978, 980)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Full name (US Dollar, Euro, Ukrainian Hryvnia)

    @Column(name = "symbol", length = 10)
    private String symbol; // Currency symbol ($, €, ₴)

    @Column(name = "minor_unit")
    private Integer minorUnit; // Number of decimal places (2 for most currencies)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Currency() {
    }

    public Currency(Long id, String code, String numericCode, String name,
                    String symbol, Integer minorUnit, Boolean isActive) {
        this.id = id;
        this.code = code;
        this.numericCode = numericCode;
        this.name = name;
        this.symbol = symbol;
        this.minorUnit = minorUnit;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public void setNumericCode(String numericCode) {
        this.numericCode = numericCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getMinorUnit() {
        return minorUnit;
    }

    public void setMinorUnit(Integer minorUnit) {
        this.minorUnit = minorUnit;
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
        Currency currency = (Currency) o;
        return Objects.equals(id, currency.id) && Objects.equals(code, currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", numericCode='" + numericCode + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", minorUnit=" + minorUnit +
                '}';
    }
}