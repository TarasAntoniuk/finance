package com.tarasantoniuk.finance.core.organization.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for organization creation and update requests
 */
public class OrganizationRequestDTO extends BaseOrganizationDto {

    @NotNull(message = "Country ID is required")
    private Long countryId;

    public OrganizationRequestDTO() {
        super();
    }

    public OrganizationRequestDTO(String name, String registrationNumber, String vatNumber,
                                  String address, String email, String phone, Long countryId) {
        super();
        setName(name);
        setRegistrationNumber(registrationNumber);
        setVatNumber(vatNumber);
        setAddress(address);
        setEmail(email);
        setPhone(phone);
        this.countryId = countryId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }
}