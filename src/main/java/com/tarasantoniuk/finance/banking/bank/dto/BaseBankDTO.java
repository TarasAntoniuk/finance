package com.tarasantoniuk.finance.banking.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for Bank request and response DTOs
 */
public abstract class BaseBankDTO {

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "SWIFT code is required")
    @Size(max = 20, message = "SWIFT code must not exceed 20 characters")
    private String swiftCode;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Website must not exceed 100 characters")
    private String website;

    private Boolean isActive;

    public BaseBankDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}