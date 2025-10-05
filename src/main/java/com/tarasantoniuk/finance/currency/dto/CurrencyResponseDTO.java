package com.tarasantoniuk.finance.currency.dto;

import java.time.LocalDateTime;

public class CurrencyResponseDTO {

    private Long id;
    private String code;
    private String numericCode;
    private String name;
    private String symbol;
    private Integer minorUnit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CurrencyResponseDTO() {
    }

    public CurrencyResponseDTO(Long id, String code, String numericCode, String name,
                               String symbol, Integer minorUnit, Boolean isActive,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.numericCode = numericCode;
        this.name = name;
        this.symbol = symbol;
        this.minorUnit = minorUnit;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}