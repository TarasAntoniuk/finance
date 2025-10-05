package com.tarasantoniuk.finance.organization.mapper;

import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.organization.dto.OrganizationRequestDTO;
import com.tarasantoniuk.finance.organization.dto.OrganizationResponseDTO;
import com.tarasantoniuk.finance.organization.entity.Organization;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CountryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    Organization toEntity(OrganizationRequestDTO requestDTO);

    OrganizationResponseDTO toResponseDTO(Organization organization);

    List<OrganizationResponseDTO> toResponseDTOList(List<Organization> organizations);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "country", source = "countryId", qualifiedByName = "idToCountry")
    void updateEntityFromDTO(OrganizationRequestDTO requestDTO, @MappingTarget Organization organization);

    @Named("idToCountry")
    default Country idToCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        Country country = new Country();
        country.setId(countryId);
        return country;
    }
}