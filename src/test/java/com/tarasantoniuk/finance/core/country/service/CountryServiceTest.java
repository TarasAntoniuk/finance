package com.tarasantoniuk.finance.core.country.service;

import com.tarasantoniuk.finance.core.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryAlreadyExistsException;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.country.service.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountryMapper countryMapper;

    @InjectMocks
    private CountryService countryService;

    private Country country;
    private CountryRequestDTO requestDTO;
    private CountryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1L);
        country.setName("United States");
        country.setIsoCode("USA");
        country.setPhoneCode("+1");

        requestDTO = new CountryRequestDTO();
        requestDTO.setName("United States");
        requestDTO.setIsoCode("USA");
        requestDTO.setPhoneCode("+1");

        responseDTO = new CountryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("United States");
        responseDTO.setIsoCode("USA");
    }

    @Test
    void getAllCountries_ShouldReturnListOfCountries() {
        // Given
        List<Country> countries = Collections.singletonList(country);
        when(countryRepository.findAll()).thenReturn(countries);
        when(countryMapper.toResponseDTOList(countries)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<CountryResponseDTO> result = countryService.getAllCountries();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(countryRepository, times(1)).findAll();
    }

    @Test
    void getCountryById_WhenExists_ShouldReturnCountry() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.getCountryById(1L);

        // Then
        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).findById(1L);
    }

    @Test
    void getCountryById_WhenNotExists_ShouldThrowException() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> countryService.getCountryById(1L));
    }

    @Test
    void getCountryByIsoCode_WhenExists_ShouldReturnCountry() {
        // Given
        when(countryRepository.findByIsoCode("USA")).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.getCountryByIsoCode("USA");

        // Then
        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).findByIsoCode("USA");
    }

    @Test
    void getCountryByIsoCode_WhenNotExists_ShouldThrowException() {
        // Given
        when(countryRepository.findByIsoCode("XXX")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> countryService.getCountryByIsoCode("XXX"));
    }

    @Test
    void getCountryByIsoCode_WhenDifferentCase_ShouldStillFind() {
        // Given
        when(countryRepository.findByIsoCode("usa")).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.getCountryByIsoCode("usa");

        // Then
        assertNotNull(result);
        verify(countryRepository, times(1)).findByIsoCode("usa");
    }

    @Test
    void createCountry_WhenValid_ShouldReturnCreatedCountry() {
        // Given
        when(countryRepository.existsByIsoCode("USA")).thenReturn(false);
        when(countryMapper.toEntity(requestDTO)).thenReturn(country);
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.createCountry(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).save(any(Country.class));
    }

    @Test
    void createCountry_WhenIsoCodeExists_ShouldThrowException() {
        // Given
        when(countryRepository.existsByIsoCode("USA")).thenReturn(true);

        // When & Then
        assertThrows(CountryAlreadyExistsException.class,
                () -> countryService.createCountry(requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    // ========== UPDATE TESTS - COMPREHENSIVE BRANCH COVERAGE ==========

    @Test
    void updateCountry_WhenValid_ShouldReturnUpdatedCountry() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(countryRepository, times(1)).save(country);
    }

    @Test
    void updateCountry_WhenNotExists_ShouldThrowException() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> countryService.updateCountry(1L, requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenIsoCodeNotChanged_ShouldNotCheckDuplicate() {
        // Given - same ISO code
        country.setIsoCode("USA");
        requestDTO.setIsoCode("USA");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(countryRepository, never()).existsByIsoCode(anyString());
        verify(countryRepository, times(1)).save(country);
    }

    @Test
    void updateCountry_WhenChangingToExistingIsoCode_ShouldThrowException() {
        // Given - changing ISO code to existing one
        country.setIsoCode("USA");
        requestDTO.setIsoCode("CAN");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryRepository.existsByIsoCode("CAN")).thenReturn(true);

        // When & Then
        assertThrows(CountryAlreadyExistsException.class,
                () -> countryService.updateCountry(1L, requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenChangingToNonExistingIsoCode_ShouldSucceed() {
        // Given - changing ISO code to non-existing one
        country.setIsoCode("USA");
        requestDTO.setIsoCode("MEX");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryRepository.existsByIsoCode("MEX")).thenReturn(false);
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        // When
        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(countryRepository, times(1)).existsByIsoCode("MEX");
        verify(countryRepository, times(1)).save(country);
    }

    // ========== DELETE TESTS ==========

    @Test
    void deleteCountry_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(countryRepository.existsById(1L)).thenReturn(true);

        // When
        countryService.deleteCountry(1L);

        // Then
        verify(countryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCountry_WhenNotExists_ShouldThrowException() {
        // Given
        when(countryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> countryService.deleteCountry(1L));
        verify(countryRepository, never()).deleteById(anyLong());
    }
}