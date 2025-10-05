package com.tarasantoniuk.finance.accountingpolicy.mapper;


import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.organization.entity.Organization;
import com.tarasantoniuk.finance.organization.mapper.OrganizationMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {OrganizationMapper.class, CurrencyMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountingPolicyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "idToOrganization")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "idToCurrency")
    AccountingPolicy toEntity(AccountingPolicyRequestDTO requestDTO);

    AccountingPolicyResponseDTO toResponseDTO(AccountingPolicy accountingPolicy);

    List<AccountingPolicyResponseDTO> toResponseDTOList(List<AccountingPolicy> accountingPolicies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "idToOrganization")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "idToCurrency")
    void updateEntityFromDTO(AccountingPolicyRequestDTO requestDTO, @MappingTarget AccountingPolicy accountingPolicy);

    @Named("idToOrganization")
    default Organization idToOrganization(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        Organization organization = new Organization();
        organization.setId(organizationId);
        return organization;
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