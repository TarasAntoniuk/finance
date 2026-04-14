package com.tarasantoniuk.finance.core.currency.mapper;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CurrencyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Currency toEntity(CurrencyRequestDto requestDTO);

    CurrencyResponseDto toResponseDTO(Currency currency);

    List<CurrencyResponseDto> toResponseDTOList(List<Currency> currencies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CurrencyRequestDto requestDTO, @MappingTarget Currency currency);
}