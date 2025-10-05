package com.tarasantoniuk.finance.organization.dto;

import com.tarasantoniuk.finance.country.dto.CountryResponseDTO;

import java.time.LocalDateTime;

public class OrganizationResponseDTO {

    private Long id;
    private String name;
    private String registrationNumber;
    private String vatNumber;
    private String address;
    private String email;
    private String phone;
    private CountryResponseDTO country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationResponseDTO() {
    }

    public OrganizationResponseDTO(Long id, String name, String registrationNumber,
                                   String vatNumber, String address, String email,
                                   String phone, CountryResponseDTO country,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.vatNumber = vatNumber;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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