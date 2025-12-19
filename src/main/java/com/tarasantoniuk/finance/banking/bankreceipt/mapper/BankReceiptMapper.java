package com.tarasantoniuk.finance.banking.bankreceipt.mapper;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.mapper.OrganizationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
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
    @Mapping(target = "account", source = "accountId", qualifiedByName = "mapAccount")
    @Mapping(target = "counterparty", source = "counterpartyId", qualifiedByName = "mapCounterparty")
    @Mapping(target = "counterpartyBankAccount", source = "counterpartyBankAccountId", qualifiedByName = "mapCounterpartyBankAccount")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "mapCurrency")
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "mapOrganization")
    BankReceipt toEntity(BankReceiptRequestDto dto);

    /**
     * Update existing entity from request DTO
     * Maps only IDs to references without loading from database
     * Actual entities should be loaded by service via loadRelatedEntities()
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "account", source = "accountId", qualifiedByName = "mapAccount")
    @Mapping(target = "counterparty", source = "counterpartyId", qualifiedByName = "mapCounterparty")
    @Mapping(target = "counterpartyBankAccount", source = "counterpartyBankAccountId", qualifiedByName = "mapCounterpartyBankAccount")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "mapCurrency")
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "mapOrganization")
    void updateEntityFromDto(BankReceiptRequestDto dto, @MappingTarget BankReceipt entity);

    /**
     * Map account ID to BankAccount entity stub (ID only)
     * Prevents Hibernate from detecting ID changes on existing relationships
     */
    @Named("mapAccount")
    default BankAccount mapAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        BankAccount account = new BankAccount();
        account.setId(accountId);
        return account;
    }

    /**
     * Map counterparty ID to Counterparty entity stub (ID only)
     * Prevents Hibernate from detecting ID changes on existing relationships
     */
    @Named("mapCounterparty")
    default Counterparty mapCounterparty(Long counterpartyId) {
        if (counterpartyId == null) {
            return null;
        }
        Counterparty counterparty = new Counterparty();
        counterparty.setId(counterpartyId);
        return counterparty;
    }

    /**
     * Map counterparty bank account ID to BankAccount entity stub (ID only)
     * Prevents Hibernate from detecting ID changes on existing relationships
     */
    @Named("mapCounterpartyBankAccount")
    default BankAccount mapCounterpartyBankAccount(Long bankAccountId) {
        if (bankAccountId == null) {
            return null;
        }
        BankAccount account = new BankAccount();
        account.setId(bankAccountId);
        return account;
    }

    /**
     * Map currency ID to Currency entity stub (ID only)
     * Prevents Hibernate from detecting ID changes on existing relationships
     */
    @Named("mapCurrency")
    default Currency mapCurrency(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setId(currencyId);
        return currency;
    }

    /**
     * Map organization ID to Organization entity stub (ID only)
     * Prevents Hibernate from detecting ID changes on existing relationships
     */
    @Named("mapOrganization")
    default Organization mapOrganization(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        Organization organization = new Organization();
        organization.setId(organizationId);
        return organization;
    }
}