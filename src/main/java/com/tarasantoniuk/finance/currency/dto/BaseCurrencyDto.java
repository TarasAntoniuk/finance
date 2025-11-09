package com.tarasantoniuk.finance.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for Currency request and response DTOs
 */
public abstract class BaseCurrencyDto {

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String code;

    @NotBlank(message = "Numeric code is required")
    @Size(min = 3, max = 3, message = "Numeric code must be exactly 3 characters")
    @Pattern(regexp = "^[0-9]{3}$", message = "Numeric code must be 3 digits")
    private String numericCode;

    @NotBlank(message = "Currency name is required")
    @Size(max = 100, message = "Currency name must not exceed 100 characters")
    private String name;

    @Size(max = 10, message = "Currency symbol must not exceed 10 characters")
    private String symbol;

    @NotNull(message = "Minor unit is required")
    private Integer minorUnit;

    private Boolean isActive;

    public BaseCurrencyDto() {
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
}