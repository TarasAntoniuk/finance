package com.tarasantoniuk.finance.core.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for Country request and response DTOs
 */
public abstract class BaseCountryDto {

    @NotBlank(message = "Country name is required")
    @Size(max = 100, message = "Country name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "ISO code is required")
    @Size(min = 2, max = 3, message = "ISO code must be 2-3 characters")
    private String isoCode;

    @Size(max = 10, message = "Phone code must not exceed 10 characters")
    private String phoneCode;

    public BaseCountryDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}