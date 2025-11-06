package com.tarasantoniuk.finance.counterparty.mapper;

import com.tarasantoniuk.finance.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.country.mapper.CountryMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CountryMapper.class})
public interface CounterpartyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", defaultValue = "true")
    Counterparty toEntity(CounterpartyRequestDto request);

    @Mapping(source = "country", target = "country")
    CounterpartyResponseDto toResponse(Counterparty entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CounterpartyRequestDto request, @MappingTarget Counterparty entity);
}