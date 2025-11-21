package com.tarasantoniuk.finance.core.counterparty.mapper;

import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.mapper.CountryMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CountryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CounterpartyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    Counterparty toEntity(CounterpartyRequestDto request);

    CounterpartyResponseDto toResponse(Counterparty entity);

    List<CounterpartyResponseDto> toResponseList(List<Counterparty> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    void updateEntity(CounterpartyRequestDto request, @MappingTarget Counterparty entity);

    @Named("idToCountry")
    default Country idToCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        Country country = new Country();
        country.setId(countryId);
        return country;
    }
}