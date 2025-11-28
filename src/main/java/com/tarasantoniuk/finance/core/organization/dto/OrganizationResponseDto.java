package com.tarasantoniuk.finance.core.organization.dto;

import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;

import java.time.LocalDateTime;

/**
 * DTO for organization responses
 */
public class OrganizationResponseDto extends BaseOrganizationDto {

    private Long id;
    private CountryResponseDto country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationResponseDto() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CountryResponseDto getCountry() {
        return country;
    }

    public void setCountry(CountryResponseDto country) {
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