package com.tarasantoniuk.finance.counterparty.dto;

public class CounterpartyRequestDto extends BaseCounterpartyDto {

    private Long countryId;

    public CounterpartyRequestDto() {
        super();
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

}