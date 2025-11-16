package com.tarasantoniuk.finance.bankaccount.mapper;

import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bank.mapper.BankMapper;
import com.tarasantoniuk.finance.bankaccount.dto.BankAccountRequestDTO;
import com.tarasantoniuk.finance.bankaccount.dto.BankAccountResponseDTO;
import com.tarasantoniuk.finance.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.mapper.CurrencyMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {BankMapper.class, CurrencyMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankAccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bank", source = "bankId", qualifiedByName = "idToBank")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "idToCurrency")
    BankAccount toEntity(BankAccountRequestDTO request);

    BankAccountResponseDTO toResponse(BankAccount entity);

    List<BankAccountResponseDTO> toResponseList(List<BankAccount> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bank", source = "bankId", qualifiedByName = "idToBank")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "idToCurrency")
    void updateEntity(BankAccountRequestDTO request, @MappingTarget BankAccount entity);

    @Named("idToBank")
    default Bank idToBank(Long bankId) {
        if (bankId == null) {
            return null;
        }
        Bank bank = new Bank();
        bank.setId(bankId);
        return bank;
    }

    @Named("idToCurrency")
    default Currency idToCurrency(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setId(currencyId);
        return currency;
    }
}