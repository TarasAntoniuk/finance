package com.tarasantoniuk.finance.core.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for Organization request and response DTOs
 */
public abstract class BaseOrganizationDto {

    @NotBlank(message = "Organization name is required")
    @Size(max = 200, message = "Organization name must not exceed 200 characters")
    private String name;

    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    private String registrationNumber;

    @Size(max = 50, message = "VAT number must not exceed 50 characters")
    private String vatNumber;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    public BaseOrganizationDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}