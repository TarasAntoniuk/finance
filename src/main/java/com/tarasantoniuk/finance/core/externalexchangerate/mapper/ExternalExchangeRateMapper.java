package com.tarasantoniuk.finance.core.externalexchangerate.mapper;

import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDto;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateResponseDto;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CurrencyMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExternalExchangeRateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currencyFrom", source = "currencyFromId", qualifiedByName = "idToCurrencyFrom")
    @Mapping(target = "currencyTo", source = "currencyToId", qualifiedByName = "idToCurrencyTo")
    ExternalExchangeRate toEntity(ExternalExchangeRateRequestDto requestDTO);

    ExternalExchangeRateResponseDto toResponseDTO(ExternalExchangeRate exchangeRate);

    List<ExternalExchangeRateResponseDto> toResponseDTOList(List<ExternalExchangeRate> exchangeRates);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currencyFrom", source = "currencyFromId", qualifiedByName = "idToCurrencyFrom")
    @Mapping(target = "currencyTo", source = "currencyToId", qualifiedByName = "idToCurrencyTo")
    void updateEntityFromDTO(ExternalExchangeRateRequestDto requestDTO,
                             @MappingTarget ExternalExchangeRate exchangeRate);

    @Named("idToCurrencyFrom")
    default Currency idToCurrencyFrom(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setId(currencyId);
        return currency;
    }

    @Named("idToCurrencyTo")
    default Currency idToCurrencyTo(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setId(currencyId);
        return currency;
    }
}