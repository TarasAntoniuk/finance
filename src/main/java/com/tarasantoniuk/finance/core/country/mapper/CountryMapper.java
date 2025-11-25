package com.tarasantoniuk.finance.core.country.mapper;

import com.tarasantoniuk.finance.core.country.dto.CountryRequestDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CurrencyMapper.class})  // Використовуємо CurrencyMapper для маппінгу Currency
public interface CountryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "currencyIdToCurrency")
    Country toEntity(CountryRequestDto requestDTO);

    // MapStruct автоматично використає CurrencyMapper.toResponseDTO() для currency
    CountryResponseDto toResponseDTO(Country country);

    List<CountryResponseDto> toResponseDTOList(List<Country> countries);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "currencyIdToCurrency")
    void updateEntityFromDTO(CountryRequestDto requestDTO, @MappingTarget Country country);

    // Helper method для конвертації currencyId в Currency
    @Named("currencyIdToCurrency")
    default Currency currencyIdToCurrency(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setId(currencyId);
        return currency;
    }
}