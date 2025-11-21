package com.tarasantoniuk.finance.banking.bank.mapper;

import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDTO;
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
    Bank toEntity(BankRequestDTO request);

    BankResponseDTO toResponse(Bank entity);

    List<BankResponseDTO> toResponseList(List<Bank> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    @Mapping(target = "counterparty", source = "counterpartyId", qualifiedByName = "idToCounterparty")
    void updateEntity(BankRequestDTO request, @MappingTarget Bank entity);

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