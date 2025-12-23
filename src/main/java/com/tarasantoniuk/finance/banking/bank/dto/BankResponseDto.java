package com.tarasantoniuk.finance.banking.bank.dto;

import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Bank response with full details and related entities")
public class BankResponseDto extends BaseBankDto {

    @Schema(description = "Unique identifier of the bank", example = "1")
    private Long id;

    @Schema(description = "Country information")
    private CountryResponseDto country;

    @Schema(description = "Associated counterparty information (if available)")
    private CounterpartyResponseDto counterparty;

    @Schema(description = "Timestamp when the record was created", example = "2025-12-02T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-12-02T10:35:00")
    private LocalDateTime updatedAt;

    public BankResponseDto() {
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

    public CounterpartyResponseDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyResponseDto counterparty) {
        this.counterparty = counterparty;
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