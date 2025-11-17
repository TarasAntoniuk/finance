package com.tarasantoniuk.finance.organization.service;

import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.country.repository.CountryRepository;
import com.tarasantoniuk.finance.organization.dto.OrganizationRequestDTO;
import com.tarasantoniuk.finance.organization.dto.OrganizationResponseDTO;
import com.tarasantoniuk.finance.organization.entity.Organization;
import com.tarasantoniuk.finance.organization.exception.OrganizationAlreadyExistsException;
import com.tarasantoniuk.finance.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.organization.mapper.OrganizationMapper;
import com.tarasantoniuk.finance.organization.repository.OrganizationRepository;
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
    private OrganizationRequestDTO requestDTO;
    private OrganizationResponseDTO responseDTO;
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

        requestDTO = new OrganizationRequestDTO();
        requestDTO.setName("Test Organization");
        requestDTO.setRegistrationNumber("12345678");
        requestDTO.setVatNumber("UA12345678");
        requestDTO.setCountryId(1L);
        requestDTO.setEmail("test@example.com");
        requestDTO.setPhone("+380501234567");
        requestDTO.setAddress("Kyiv, Ukraine");

        responseDTO = new OrganizationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Test Organization");
        responseDTO.setRegistrationNumber("12345678");
    }

    @Test
    void getAllOrganizations_ShouldReturnListOfOrganizations() {
        // Given
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findAll()).thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<OrganizationResponseDTO> result = organizationService.getAllOrganizations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findAll();
    }

    @Test
    void getOrganizationById_WhenExists_ShouldReturnOrganization() {
        // Given
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.getOrganizationById(1L);

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getRegistrationNumber());
        verify(organizationRepository, times(1)).findById(1L);
    }

    @Test
    void getOrganizationById_WhenNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.getOrganizationById(1L));
    }

    @Test
    void getOrganizationsByCountry_ShouldReturnFilteredList() {
        // Given
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findByCountryId(1L)).thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<OrganizationResponseDTO> result = organizationService.getOrganizationsByCountry(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findByCountryId(1L);
    }

    @Test
    void searchOrganizationsByName_ShouldReturnMatchingOrganizations() {
        // Given
        List<Organization> organizations = Arrays.asList(organization);
        when(organizationRepository.findByNameContainingIgnoreCase("Test"))
                .thenReturn(organizations);
        when(organizationMapper.toResponseDTOList(organizations)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<OrganizationResponseDTO> result = organizationService.searchOrganizationsByName("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void createOrganization_WhenValid_ShouldReturnCreatedOrganization() {
        // Given
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(false);
        when(organizationMapper.toEntity(requestDTO)).thenReturn(organization);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.createOrganization(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getRegistrationNumber());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void createOrganization_WhenCountryNotExists_ShouldThrowException() {
        // Given
        when(countryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> organizationService.createOrganization(requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void createOrganization_WhenRegistrationNumberExists_ShouldThrowException() {
        // Given
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(true);

        // When & Then
        assertThrows(OrganizationAlreadyExistsException.class,
                () -> organizationService.createOrganization(requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    // ========== UPDATE TESTS - COMPREHENSIVE BRANCH COVERAGE ==========

    @Test
    void updateOrganization_WhenValid_ShouldReturnUpdatedOrganization() {
        // Given
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.updateOrganization(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenCountryNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenRegistrationNumberIsNull_ShouldNotCheckDuplicate() {
        // Given
        requestDTO.setRegistrationNumber(null);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.updateOrganization(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(organizationRepository, never()).existsByRegistrationNumber(anyString());
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenRegistrationNumberNotChanged_ShouldNotCheckDuplicate() {
        // Given - same registration number
        organization.setRegistrationNumber("12345678");
        requestDTO.setRegistrationNumber("12345678");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.updateOrganization(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(organizationRepository, never()).existsByRegistrationNumber(anyString());
        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void updateOrganization_WhenChangingToExistingRegistrationNumber_ShouldThrowException() {
        // Given - changing registration number to existing one
        organization.setRegistrationNumber("11111111");
        requestDTO.setRegistrationNumber("12345678");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("12345678")).thenReturn(true);

        // When & Then
        assertThrows(OrganizationAlreadyExistsException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void updateOrganization_WhenChangingToNonExistingRegistrationNumber_ShouldSucceed() {
        // Given - changing registration number to non-existing one
        organization.setRegistrationNumber("11111111");
        requestDTO.setRegistrationNumber("99999999");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.existsByRegistrationNumber("99999999")).thenReturn(false);
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toResponseDTO(organization)).thenReturn(responseDTO);

        // When
        OrganizationResponseDTO result = organizationService.updateOrganization(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(organizationRepository, times(1)).existsByRegistrationNumber("99999999");
        verify(organizationRepository, times(1)).save(organization);
    }

    // ========== DELETE TESTS ==========

    @Test
    void deleteOrganization_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(true);

        // When
        organizationService.deleteOrganization(1L);

        // Then
        verify(organizationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteOrganization_WhenNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.deleteOrganization(1L));
        verify(organizationRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateOrganization_WhenDifferentCountryNotExists_ShouldThrowException() {
        // Given
        requestDTO.setCountryId(999L); // Different country

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(countryRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> organizationService.updateOrganization(1L, requestDTO));
        verify(organizationRepository, never()).save(any(Organization.class));
    }
}