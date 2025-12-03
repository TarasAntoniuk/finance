package com.tarasantoniuk.finance.banking.bankreceipt.mapper;

import com.tarasantoniuk.finance.banking.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.organization.mapper.OrganizationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {
                BankAccountMapper.class,
                CounterpartyMapper.class,
                CurrencyMapper.class,
                OrganizationMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BankReceiptMapper {

    /**
     * Convert entity to response DTO
     */
    BankReceiptResponseDto toResponseDto(BankReceipt entity);

    /**
     * Convert request DTO to entity
     * Maps only IDs to references (actual entities loaded by service)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "account.id", source = "accountId")
    @Mapping(target = "counterparty.id", source = "counterpartyId")
    @Mapping(target = "counterpartyBankAccount.id", source = "counterpartyBankAccountId")
    @Mapping(target = "currency.id", source = "currencyId")
    @Mapping(target = "organization.id", source = "organizationId")
    BankReceipt toEntity(BankReceiptRequestDto dto);

    /**
     * Update existing entity from request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "account.id", source = "accountId")
    @Mapping(target = "counterparty.id", source = "counterpartyId")
    @Mapping(target = "counterpartyBankAccount.id", source = "counterpartyBankAccountId")
    @Mapping(target = "currency.id", source = "currencyId")
    @Mapping(target = "organization.id", source = "organizationId")
    void updateEntityFromDto(BankReceiptRequestDto dto, @MappingTarget BankReceipt entity);
}