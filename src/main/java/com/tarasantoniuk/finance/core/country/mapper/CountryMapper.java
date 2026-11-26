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
        uses = {CurrencyMapper.class})
public interface CountryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "currencyIdToCurrency")
    Country toEntity(CountryRequestDto requestDTO);

    CountryResponseDto toResponseDTO(Country country);

    List<CountryResponseDto> toResponseDTOList(List<Country> countries);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "currencyIdToCurrency")
    void updateEntityFromDTO(CountryRequestDto requestDTO, @MappingTarget Country country);

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