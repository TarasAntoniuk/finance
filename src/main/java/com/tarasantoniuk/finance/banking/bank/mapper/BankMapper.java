package com.tarasantoniuk.finance.banking.bank.mapper;

import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDto;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDto;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.mapper.CountryMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CountryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    @Mapping(target = "counterparty", source = "counterpartyId", qualifiedByName = "idToCounterparty")
    Bank toEntity(BankRequestDto request);

    BankResponseDto toResponse(Bank entity);

    List<BankResponseDto> toResponseList(List<Bank> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    @Mapping(target = "counterparty", source = "counterpartyId", qualifiedByName = "idToCounterparty")
    void updateEntity(BankRequestDto request, @MappingTarget Bank entity);

    @Named("idToCountry")
    default Country idToCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        Country country = new Country();
        country.setId(countryId);
        return country;
    }

    @Named("idToCounterparty")
    default Counterparty idToCounterparty(Long counterpartyId) {
        if (counterpartyId == null) {
            return null;
        }
        Counterparty counterparty = new Counterparty();
        counterparty.setId(counterpartyId);
        return counterparty;
    }
}