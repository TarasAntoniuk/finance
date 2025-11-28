package com.tarasantoniuk.finance.core.organization.service;

import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationAlreadyExistsException;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.core.organization.mapper.OrganizationMapper;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization organization;
    private OrganizationRequestDto requestDTO;
    private OrganizationResponseDto responseDTO;
    private Country country;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1L);
        country.setName("Ukraine");
        country.setIsoCode("UKR");

        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Organization");
        organization.setRegistrationNumber("12345678");
        organization.setVatNumber("UA12345678");
        organization.setCountry(country);
        organization.setEmail("test@example.com");
        organization.setPhone("+380501234567");
        organization.setAddress("Kyiv, Ukraine");

        requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Organization");
        requestDTO.setRegistrationNumber("12345678");
        requestDTO.setVatNumber("UA12345678");
        requestDTO.setCountryId(1L);
        requestDTO.setEmail("test@example.com");
        requestDTO.setPhone("+380501234567");
        requestDTO.setAddress("Kyiv, Ukraine");

        responseDTO = new OrganizationResponseDto();
        responseDTO.setId(1L);
        responseDTO.setName("Test Organization");
        responseDTO.setRegistrationNumber("12345678");
    }

    @Test
    void getAllOrganizations_ShouldReturnListOfOrganizations() {
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findAllWithCountry()).thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        List<OrganizationResponseDto> result = organizationService.getAllOrganizations();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findAllWithCountry();
    }

    @Test
    void getOrganizationById_WhenExists_ShouldReturnOrganization() {
        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.getOrganizationById(1L);

        assertNotNull(result);
        assertEquals("12345678", result.getRegistrationNumber());
        verify(organizationRepository, times(1)).findByIdWithCountry(1L);
    }

    @Test
    void getOrganizationById_WhenNotExists_ShouldThrowException() {
        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.getOrganizationById(1L));
    }

    @Test
    void getOrganizationsByCountry_ShouldReturnFilteredList() {
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findByCountryIdWithCountry(1L)).thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        List<OrganizationResponseDto> result = organizationService.getOrganizationsByCountry(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findByCountryIdWithCountry(1L);
    }

    @Test
    void searchOrganizationsByName_ShouldReturnMatchingOrganizations() {
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findByNameContainingIgnoreCaseWithCountry("Test"))
                .thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        List<OrganizationResponseDto> result = organizationService.searchOrganizationsByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findByNameContainingIgnoreCaseWithCountry("Test");
    }

    @Test
    void createOrganization_WhenValid_ShouldReturnCreatedOrganization() {
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(false);
        when(organizationMapper.toEntity(requestDTO)).thenReturn(organization);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.createOrganization(requestDTO);

        assertNotNull(result);
        assertEquals("12345678", result.getRegistrationNumber());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void createOrganization_WhenCountryNotExists_ShouldThrowException() {
        when(countryRepository.existsById(1L)).thenReturn(false);

        assertThrows(CountryNotFoundException.class,
                () -> organizationService.createOrganization(requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void createOrganization_WhenRegistrationNumberExists_ShouldThrowException() {
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(true);

        assertThrows(OrganizationAlreadyExistsException.class,
                () -> organizationService.createOrganization(requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenValid_ShouldReturnUpdatedOrganization() {
        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.updateOrganization(1L, requestDTO);

        assertNotNull(result);
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenNotExists_ShouldThrowException() {
        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenCountryNotExists_ShouldThrowException() {
        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(false);

        assertThrows(CountryNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenRegistrationNumberIsNull_ShouldNotCheckDuplicate() {
        requestDTO.setRegistrationNumber(null);

        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.updateOrganization(1L, requestDTO);

        assertNotNull(result);
        verify(organizationRepository, never()).existsByRegistrationNumber(anyString());
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenRegistrationNumberNotChanged_ShouldNotCheckDuplicate() {
        organization.setRegistrationNumber("12345678");
        requestDTO.setRegistrationNumber("12345678");

        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.updateOrganization(1L, requestDTO);

        assertNotNull(result);
        verify(organizationRepository, never()).existsByRegistrationNumber(anyString());
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenChangingToExistingRegistrationNumber_ShouldThrowException() {
        organization.setRegistrationNumber("11111111");
        requestDTO.setRegistrationNumber("12345678");

        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(true);

        assertThrows(OrganizationAlreadyExistsException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenChangingToNonExistingRegistrationNumber_ShouldSucceed() {
        organization.setRegistrationNumber("11111111");
        requestDTO.setRegistrationNumber("99999999");

        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("99999999")).thenReturn(false);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        OrganizationResponseDto result = organizationService.updateOrganization(1L, requestDTO);

        assertNotNull(result);
        verify(organizationRepository, times(1)).existsByRegistrationNumber("99999999");
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void deleteOrganization_WhenExists_ShouldDeleteSuccessfully() {
        when(organizationRepository.existsById(1L)).thenReturn(true);

        organizationService.deleteOrganization(1L);

        verify(organizationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteOrganization_WhenNotExists_ShouldThrowException() {
        when(organizationRepository.existsById(1L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.deleteOrganization(1L));
        verify(organizationRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateOrganization_WhenDifferentCountryNotExists_ShouldThrowException() {
        requestDTO.setCountryId(999L);

        when(organizationRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(999L)).thenReturn(false);

        assertThrows(CountryNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }
}