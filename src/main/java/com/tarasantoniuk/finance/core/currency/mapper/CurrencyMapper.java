package com.tarasantoniuk.finance.core.currency.mapper;
//import com.example.demo.entity.Currency;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDTO;
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
    Currency toEntity(CurrencyRequestDTO requestDTO);

    CurrencyResponseDTO toResponseDTO(Currency currency);

    List<CurrencyResponseDTO> toResponseDTOList(List<Currency> currencies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CurrencyRequestDTO requestDTO, @MappingTarget Currency currency);
}