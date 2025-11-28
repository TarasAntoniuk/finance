package com.tarasantoniuk.finance.core.accountingpolicy.mapper;


import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDto;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDto;
import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.mapper.OrganizationMapper;
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
    AccountingPolicy toEntity(AccountingPolicyRequestDto requestDTO);

    AccountingPolicyResponseDto toResponseDTO(AccountingPolicy accountingPolicy);

    List<AccountingPolicyResponseDto> toResponseDTOList(List<AccountingPolicy> accountingPolicies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "idToOrganization")
    @Mapping(target = "currency", source = "currencyId", qualifiedByName = "idToCurrency")
    void updateEntityFromDTO(AccountingPolicyRequestDto requestDTO, @MappingTarget AccountingPolicy accountingPolicy);

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