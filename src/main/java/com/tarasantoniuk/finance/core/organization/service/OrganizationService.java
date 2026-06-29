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
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final CountryRepository countryRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationSecurityContext orgContext;

    public OrganizationService(OrganizationRepository organizationRepository,
                               CountryRepository countryRepository,
                               OrganizationMapper organizationMapper,
                               OrganizationSecurityContext orgContext) {
        this.organizationRepository = organizationRepository;
        this.countryRepository = countryRepository;
        this.organizationMapper = organizationMapper;
        this.orgContext = orgContext;
    }

    public PageResponse<OrganizationResponseDto> getAllOrganizations(int page, int size) {
        if (!orgContext.isAdmin()) {
            return ownOrganizationAsPage();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> orgPage = organizationRepository.findAllWithCountry(pageable);
        return buildPageResponse(orgPage);
    }

    public OrganizationResponseDto getOrganizationById(Long id) {
        Organization organization = organizationRepository.findByIdWithCountry(id)
                .orElseThrow(() -> OrganizationNotFoundException.byId(id));
        orgContext.validateAccess(organization.getId());
        return organizationMapper.toResponseDTO(organization);
    }

    public List<OrganizationResponseDto> getOrganizationsByCountry(Long countryId, int limit) {
        if (!orgContext.isAdmin()) {
            return ownOrganization()
                    .filter(org -> org.getCountry() != null && countryId.equals(org.getCountry().getId()))
                    .map(org -> List.of(organizationMapper.toResponseDTO(org)))
                    .orElse(Collections.emptyList());
        }
        List<Organization> organizations = organizationRepository.findByCountryIdWithCountry(countryId, PageRequest.of(0, limit));
        return organizationMapper.toResponseDTOList(organizations);
    }

    public PageResponse<OrganizationResponseDto> searchOrganizationsByName(String name, int page, int size) {
        if (!orgContext.isAdmin()) {
            return ownOrganization()
                    .filter(org -> org.getName() != null && org.getName().toLowerCase().contains(name.toLowerCase()))
                    .map(this::singleOrganizationPage)
                    .orElseGet(this::emptyPage);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> orgPage = organizationRepository.findByNameContainingIgnoreCaseWithCountry(name, pageable);
        return buildPageResponse(orgPage);
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
        orgContext.validateAccess(organization.getId());

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

    private Optional<Organization> ownOrganization() {
        Long activeOrgId = orgContext.getActiveOrganizationId();
        return organizationRepository.findByIdWithCountry(activeOrgId);
    }

    private PageResponse<OrganizationResponseDto> ownOrganizationAsPage() {
        return ownOrganization()
                .map(this::singleOrganizationPage)
                .orElseGet(this::emptyPage);
    }

    private PageResponse<OrganizationResponseDto> singleOrganizationPage(Organization organization) {
        return buildPageResponse(new PageImpl<>(List.of(organization)));
    }

    private PageResponse<OrganizationResponseDto> emptyPage() {
        return buildPageResponse(new PageImpl<>(Collections.emptyList()));
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
}
