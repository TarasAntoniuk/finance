package com.tarasantoniuk.finance.country.dto;

/**
 * DTO for country creation and update requests
 */
public class CountryRequestDTO extends BaseCountryDto {

    private Long currencyId;

    public CountryRequestDTO() {
        super();
    }

    public CountryRequestDTO(String name, String isoCode, String phoneCode, Long currencyId) {
        super();
        setName(name);
        setIsoCode(isoCode);
        setPhoneCode(phoneCode);
        this.currencyId = currencyId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }
}