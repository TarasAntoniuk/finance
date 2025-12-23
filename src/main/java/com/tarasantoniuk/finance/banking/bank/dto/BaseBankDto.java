package com.tarasantoniuk.finance.banking.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for Bank request and response DTOs
 */
@Schema(description = "Base bank information")
public abstract class BaseBankDto {

    @Schema(
            description = "Name of the bank",
            example = "Bank of America",
            required = true,
            maxLength = 100
    )
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String name;

    @Schema(
            description = "SWIFT/BIC code of the bank",
            example = "BOFAUS3N",
            required = true,
            maxLength = 20
    )
    @NotBlank(message = "SWIFT code is required")
    @Size(max = 20, message = "SWIFT code must not exceed 20 characters")
    private String swiftCode;

    @Schema(
            description = "Physical address of the bank",
            example = "100 North Tryon Street, Charlotte, NC 28255",
            maxLength = 200
    )
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @Schema(
            description = "Contact phone number",
            example = "+1-704-386-5681",
            maxLength = 20
    )
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Schema(
            description = "Bank's website URL",
            example = "https://www.bankofamerica.com",
            maxLength = 100
    )
    @Size(max = 100, message = "Website must not exceed 100 characters")
    private String website;

    @Schema(
            description = "Whether the bank is active",
            example = "true"
    )
    private Boolean isActive;

    public BaseBankDto() {
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