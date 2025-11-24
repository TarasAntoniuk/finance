package com.tarasantoniuk.finance.core.country.service;

import com.tarasantoniuk.finance.core.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryAlreadyExistsException;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
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
        List<Country> countries = Collections.singletonList(country);
        when(countryRepository.findAllWithCurrency()).thenReturn(countries);
        when(countryMapper.toResponseDTOList(countries)).thenReturn(Collections.singletonList(responseDTO));

        List<CountryResponseDTO> result = countryService.getAllCountries();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(countryRepository, times(1)).findAllWithCurrency();
    }

    @Test
    void getCountryById_WhenExists_ShouldReturnCountry() {
        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.getCountryById(1L);

        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).findByIdWithCurrency(1L);
    }

    @Test
    void getCountryById_WhenNotExists_ShouldThrowException() {
        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.empty());

        assertThrows(CountryNotFoundException.class,
                () -> countryService.getCountryById(1L));
    }

    @Test
    void getCountryByIsoCode_WhenExists_ShouldReturnCountry() {
        when(countryRepository.findByIsoCodeWithCurrency("USA")).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.getCountryByIsoCode("USA");

        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).findByIsoCodeWithCurrency("USA");
    }

    @Test
    void getCountryByIsoCode_WhenNotExists_ShouldThrowException() {
        when(countryRepository.findByIsoCodeWithCurrency("XXX")).thenReturn(Optional.empty());

        assertThrows(CountryNotFoundException.class,
                () -> countryService.getCountryByIsoCode("XXX"));
    }

    @Test
    void getCountryByIsoCode_WhenDifferentCase_ShouldStillFind() {
        when(countryRepository.findByIsoCodeWithCurrency("usa")).thenReturn(Optional.of(country));
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.getCountryByIsoCode("usa");

        assertNotNull(result);
        verify(countryRepository, times(1)).findByIsoCodeWithCurrency("usa");
    }

    @Test
    void createCountry_WhenValid_ShouldReturnCreatedCountry() {
        when(countryRepository.existsByIsoCode("USA")).thenReturn(false);
        when(countryMapper.toEntity(requestDTO)).thenReturn(country);
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.createCountry(requestDTO);

        assertNotNull(result);
        assertEquals("USA", result.getIsoCode());
        verify(countryRepository, times(1)).save(any(Country.class));
    }

    @Test
    void createCountry_WhenIsoCodeExists_ShouldThrowException() {
        when(countryRepository.existsByIsoCode("USA")).thenReturn(true);

        assertThrows(CountryAlreadyExistsException.class,
                () -> countryService.createCountry(requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenValid_ShouldReturnUpdatedCountry() {
        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.of(country));
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        assertNotNull(result);
        verify(countryRepository, times(1)).save(country);
    }

    @Test
    void updateCountry_WhenNotExists_ShouldThrowException() {
        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.empty());

        assertThrows(CountryNotFoundException.class,
                () -> countryService.updateCountry(1L, requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenIsoCodeNotChanged_ShouldNotCheckDuplicate() {
        country.setIsoCode("USA");
        requestDTO.setIsoCode("USA");

        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.of(country));
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        assertNotNull(result);
        verify(countryRepository, never()).existsByIsoCode(anyString());
        verify(countryRepository, times(1)).save(country);
    }

    @Test
    void updateCountry_WhenChangingToExistingIsoCode_ShouldThrowException() {
        country.setIsoCode("USA");
        requestDTO.setIsoCode("CAN");

        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.of(country));
        when(countryRepository.existsByIsoCode("CAN")).thenReturn(true);

        assertThrows(CountryAlreadyExistsException.class,
                () -> countryService.updateCountry(1L, requestDTO));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_WhenChangingToNonExistingIsoCode_ShouldSucceed() {
        country.setIsoCode("USA");
        requestDTO.setIsoCode("MEX");

        when(countryRepository.findByIdWithCurrency(1L)).thenReturn(Optional.of(country));
        when(countryRepository.existsByIsoCode("MEX")).thenReturn(false);
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponseDTO(country)).thenReturn(responseDTO);

        CountryResponseDTO result = countryService.updateCountry(1L, requestDTO);

        assertNotNull(result);
        verify(countryRepository, times(1)).existsByIsoCode("MEX");
        verify(countryRepository, times(1)).save(country);
    }

    @Test
    void deleteCountry_WhenExists_ShouldDeleteSuccessfully() {
        when(countryRepository.existsById(1L)).thenReturn(true);

        countryService.deleteCountry(1L);

        verify(countryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCountry_WhenNotExists_ShouldThrowException() {
        when(countryRepository.existsById(1L)).thenReturn(false);

        assertThrows(CountryNotFoundException.class,
                () -> countryService.deleteCountry(1L));
        verify(countryRepository, never()).deleteById(anyLong());
    }
}