package com.tarasantoniuk.finance.organization.service;

import com.tarasantoniuk.finance.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.country.repository.CountryRepository;
import com.tarasantoniuk.finance.organization.dto.OrganizationRequestDTO;
import com.tarasantoniuk.finance.organization.dto.OrganizationResponseDTO;
import com.tarasantoniuk.finance.organization.entity.Organization;
import com.tarasantoniuk.finance.organization.exception.OrganizationAlreadyExistsException;
import com.tarasantoniuk.finance.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.organization.mapper.OrganizationMapper;
import com.tarasantoniuk.finance.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final CountryRepository countryRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationRepository organizationRepository,
                               CountryRepository countryRepository,
                               OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.countryRepository = countryRepository;
        this.organizationMapper = organizationMapper;
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        return organizationMapper.toResponseDTOList(organizations);
    }

    @Transactional(readOnly = true)
    public OrganizationResponseDTO getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> OrganizationNotFoundException.byId(id));
        return organizationMapper.toResponseDTO(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getOrganizationsByCountry(Long countryId) {
        List<Organization> organizations = organizationRepository.findByCountryId(countryId);
        return organizationMapper.toResponseDTOList(organizations);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> searchOrganizationsByName(String name) {
        List<Organization> organizations = organizationRepository.findByNameContainingIgnoreCase(name);
        return organizationMapper.toResponseDTOList(organizations);
    }

    @Transactional
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO requestDTO) {
        if (!countryRepository.existsById(requestDTO.getCountryId())) {
            throw CountryNotFoundException.byId(requestDTO.getCountryId());
        }

        if (requestDTO.getRegistrationNumber() != null
                && organizationRepository.existsByRegistrationNumber(requestDTO.getRegistrationNumber())) {
            throw OrganizationAlreadyExistsException.byRegistrationNumber(requestDTO.getRegistrationNumber());
        }

        Organization organization = organizationMapper.toEntity(requestDTO);
        Organization savedOrganization = organizationRepository.save(organization);
        return organizationMapper.toResponseDTO(savedOrganization);
    }

    @Transactional
    public OrganizationResponseDTO updateOrganization(Long id, OrganizationRequestDTO requestDTO) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> OrganizationNotFoundException.byId(id));

        if (!countryRepository.existsById(requestDTO.getCountryId())) {
            throw CountryNotFoundException.byId(requestDTO.getCountryId());
        }

        if (requestDTO.getRegistrationNumber() != null
                && !requestDTO.getRegistrationNumber().equals(organization.getRegistrationNumber())
                && organizationRepository.existsByRegistrationNumber(requestDTO.getRegistrationNumber())) {
            throw OrganizationAlreadyExistsException.byRegistrationNumber(requestDTO.getRegistrationNumber());
        }

        organizationMapper.updateEntityFromDTO(requestDTO, organization);
        Organization updatedOrganization = organizationRepository.save(organization);
        return organizationMapper.toResponseDTO(updatedOrganization);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw OrganizationNotFoundException.byId(id);
        }
        organizationRepository.deleteById(id);
    }
}