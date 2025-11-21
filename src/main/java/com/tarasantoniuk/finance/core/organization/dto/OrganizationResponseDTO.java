package com.tarasantoniuk.finance.core.organization.dto;

import com.tarasantoniuk.finance.core.country.dto.CountryResponseDTO;

import java.time.LocalDateTime;

/**
 * DTO for organization responses
 */
public class OrganizationResponseDTO extends BaseOrganizationDto {

    private Long id;
    private CountryResponseDTO country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationResponseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CountryResponseDTO getCountry() {
        return country;
    }

    public void setCountry(CountryResponseDTO country) {
        this.country = country;
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