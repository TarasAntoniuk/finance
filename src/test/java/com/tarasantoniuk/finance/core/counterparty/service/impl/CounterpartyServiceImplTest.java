package com.tarasantoniuk.finance.core.counterparty.service.impl;

import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.counterparty.service.impl.CounterpartyServiceImpl;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounterpartyServiceImplTest {

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CounterpartyMapper counterpartyMapper;

    @InjectMocks
    private CounterpartyServiceImpl counterpartyService;

    private Counterparty counterparty;
    private CounterpartyRequestDto requestDto;
    private CounterpartyResponseDto responseDto;
    private Country country;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1L);
        country.setIsoCode("UA");
        country.setName("Ukraine");

        counterparty = new Counterparty();
        counterparty.setId(1L);
        counterparty.setCode("CP001");
        counterparty.setName("Test Counterparty");
        counterparty.setType(Counterparty.CounterpartyType.CUSTOMER);
        counterparty.setIsActive(true);
        counterparty.setCountry(country);

        requestDto = new CounterpartyRequestDto();
        requestDto.setCode("CP001");
        requestDto.setName("Test Counterparty");
        requestDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        requestDto.setCountryId(1L);

        responseDto = new CounterpartyResponseDto();
        responseDto.setId(1L);
        responseDto.setCode("CP001");
        responseDto.setName("Test Counterparty");
        responseDto.setIsActive(true);
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_WhenValid_ShouldReturnCreatedCounterparty() {
        // Given
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(any(Counterparty.class))).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.create(requestDto);

        // Then
        assertNotNull(result);
        assertEquals("CP001", result.getCode());
        verify(counterpartyRepository).save(any(Counterparty.class));
        verify(countryRepository).findById(1L);
    }

    @Test
    void create_WhenCodeExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateCounterpartyException.class,
                () -> counterpartyService.create(requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void create_WhenCountryNotFound_ShouldThrowException() {
        // Given
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> counterpartyService.create(requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void create_WhenCountryIdIsNull_ShouldCreateWithoutCountry() {
        // Given
        requestDto.setCountryId(null);
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(counterpartyRepository.save(any(Counterparty.class))).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.create(requestDto);

        // Then
        assertNotNull(result);
        verify(countryRepository, never()).findById(anyLong());
        verify(counterpartyRepository).save(any(Counterparty.class));
    }

    // ========== GET BY ID TESTS ==========

    @Test
    void getById_WhenExists_ShouldReturnCounterparty() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CP001", result.getCode());
        verify(counterpartyRepository).findById(1L);
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.getById(1L));
    }

    // ========== GET ALL TESTS ==========

    @Test
    void getAll_ShouldReturnAllCounterparties() {
        // Given
        List<Counterparty> counterparties = List.of(counterparty);
        when(counterpartyRepository.findAll()).thenReturn(counterparties);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        List<CounterpartyResponseDto> result = counterpartyService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(counterpartyRepository).findAll();
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(counterpartyRepository.findAll()).thenReturn(List.of());

        // When
        List<CounterpartyResponseDto> result = counterpartyService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(counterpartyRepository).findAll();
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_WhenValid_ShouldReturnUpdatedCounterparty() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        // Then
        assertNotNull(result);
        verify(counterpartyMapper).updateEntity(requestDto, counterparty);
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void update_WhenNotExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void update_WhenChangingToExistingCode_ShouldThrowException() {
        // Given
        requestDto.setCode("CP002");
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.existsByCode("CP002")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateCounterpartyException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void update_WhenNotChangingCode_ShouldNotCheckExistence() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        // Then
        assertNotNull(result);
        verify(counterpartyRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_WhenCodeIsNull_ShouldNotCheckExistence() {
        // Given
        requestDto.setCode(null);
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        // Then
        assertNotNull(result);
        verify(counterpartyRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_WhenCountryIdIsNull_ShouldNotUpdateCountry() {
        // Given
        requestDto.setCountryId(null);
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        // When
        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        // Then
        assertNotNull(result);
        verify(countryRepository, never()).findById(anyLong());
    }

    @Test
    void update_WhenCountryNotFound_ShouldThrowException() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CountryNotFoundException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(counterpartyRepository.existsById(1L)).thenReturn(true);

        // When
        counterpartyService.delete(1L);

        // Then
        verify(counterpartyRepository).deleteById(1L);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.delete(1L));
        verify(counterpartyRepository, never()).deleteById(anyLong());
    }

    // ========== ACTIVATE TESTS ==========

    @Test
    void activate_WhenExists_ShouldActivateSuccessfully() {
        // Given
        counterparty.setIsActive(false);
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        // When
        counterpartyService.activate(1L);

        // Then
        assertTrue(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void activate_WhenNotExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.activate(1L));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void activate_WhenAlreadyActive_ShouldStillSave() {
        // Given
        counterparty.setIsActive(true);
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        // When
        counterpartyService.activate(1L);

        // Then
        assertTrue(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    // ========== DEACTIVATE TESTS ==========

    @Test
    void deactivate_WhenExists_ShouldDeactivateSuccessfully() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        // When
        counterpartyService.deactivate(1L);

        // Then
        assertFalse(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void deactivate_WhenNotExists_ShouldThrowException() {
        // Given
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.deactivate(1L));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void deactivate_WhenAlreadyInactive_ShouldStillSave() {
        // Given
        counterparty.setIsActive(false);
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        // When
        counterpartyService.deactivate(1L);

        // Then
        assertFalse(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }
}