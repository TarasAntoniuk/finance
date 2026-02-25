package com.tarasantoniuk.finance.core.organization.service;

import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationAlreadyExistsException;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.core.organization.mapper.OrganizationMapper;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
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

    /**
     * Get all organizations with pagination and optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load country in a single query.
     */
    public PageResponse<OrganizationResponseDto> getAllOrganizations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> orgPage = organizationRepository.findAllWithCountry(pageable);
        return buildPageResponse(orgPage);
    }

    /**
     * Get organization by ID with optimized query.
     */
    public OrganizationResponseDto getOrganizationById(Long id) {
        Organization organization = organizationRepository.findByIdWithCountry(id)
                .orElseThrow(() -> OrganizationNotFoundException.byId(id));
        return organizationMapper.toResponseDTO(organization);
    }

    /**
     * Get organizations by country with optimized query.
     */
    public List<OrganizationResponseDto> getOrganizationsByCountry(Long countryId) {
        List<Organization> organizations = organizationRepository.findByCountryIdWithCountry(countryId);
        return organizationMapper.toResponseDTOList(organizations);
    }

    /**
     * Search organizations by name with pagination and optimized query.
     */
    public PageResponse<OrganizationResponseDto> searchOrganizationsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> orgPage = organizationRepository.findByNameContainingIgnoreCaseWithCountry(name, pageable);
        return buildPageResponse(orgPage);
    }

    private PageResponse<OrganizationResponseDto> buildPageResponse(Page<Organization> orgPage) {
        List<OrganizationResponseDto> dtos = organizationMapper.toResponseDTOList(orgPage.getContent());

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(orgPage.getNumber())
                .totalPages(orgPage.getTotalPages())
                .pageSize(orgPage.getSize())
                .totalElements(orgPage.getTotalElements())
                .hasNext(orgPage.hasNext())
                .hasPrevious(orgPage.hasPrevious())
                .build();

        return PageResponse.<OrganizationResponseDto>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    @Transactional
    public OrganizationResponseDto createOrganization(OrganizationRequestDto requestDTO) {
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
    public OrganizationResponseDto updateOrganization(Long id, OrganizationRequestDto requestDTO) {
        Organization organization = organizationRepository.findByIdWithCountry(id)
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