package com.tarasantoniuk.finance.core.counterparty.service.impl;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

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

    @Test
    void create_WhenValid_ShouldReturnCreatedCounterparty() {
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(any(Counterparty.class))).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.create(requestDto);

        assertNotNull(result);
        assertEquals("CP001", result.getCode());
        verify(counterpartyRepository).save(any(Counterparty.class));
        verify(countryRepository).findById(1L);
    }

    @Test
    void create_WhenCodeExists_ShouldThrowException() {
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(true);

        assertThrows(DuplicateCounterpartyException.class,
                () -> counterpartyService.create(requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void create_WhenCountryNotFound_ShouldThrowException() {
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CountryNotFoundException.class,
                () -> counterpartyService.create(requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void create_WhenCountryIdIsNull_ShouldCreateWithoutCountry() {
        requestDto.setCountryId(null);
        when(counterpartyRepository.existsByCode("CP001")).thenReturn(false);
        when(counterpartyMapper.toEntity(requestDto)).thenReturn(counterparty);
        when(counterpartyRepository.save(any(Counterparty.class))).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.create(requestDto);

        assertNotNull(result);
        verify(countryRepository, never()).findById(anyLong());
        verify(counterpartyRepository).save(any(Counterparty.class));
    }

    @Test
    void getById_WhenExists_ShouldReturnCounterparty() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CP001", result.getCode());
        verify(counterpartyRepository).findByIdWithCountry(1L);
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.getById(1L));
    }

    @Test
    void getAll_ShouldReturnPagedCounterparties() {
        int page = 0;
        int size = 100;

        List<Counterparty> counterparties = List.of(counterparty);
        Page<Counterparty> counterpartyPage = new PageImpl<>(counterparties,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name", "id")),
                1);

        when(counterpartyRepository.findAllWithCountry(any(Pageable.class))).thenReturn(counterpartyPage);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        PageResponse<CounterpartyResponseDto> result = counterpartyService.getAll(page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());

        PageMetadata metadata = result.getMetadata();
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(1, metadata.getTotalPages());
        assertEquals(100, metadata.getPageSize());
        assertEquals(1, metadata.getTotalElements());
        assertFalse(metadata.isHasNext());
        assertFalse(metadata.isHasPrevious());

        verify(counterpartyRepository).findAllWithCountry(any(Pageable.class));
    }

    @Test
    void getAll_WithMultiplePages_ShouldReturnCorrectMetadata() {
        int page = 1;
        int size = 100;

        List<Counterparty> counterparties = List.of(counterparty);
        Page<Counterparty> counterpartyPage = new PageImpl<>(counterparties,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name", "id")),
                250);

        when(counterpartyRepository.findAllWithCountry(any(Pageable.class))).thenReturn(counterpartyPage);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        PageResponse<CounterpartyResponseDto> result = counterpartyService.getAll(page, size);

        assertNotNull(result);

        PageMetadata metadata = result.getMetadata();
        assertEquals(1, metadata.getCurrentPage());
        assertEquals(3, metadata.getTotalPages());
        assertEquals(100, metadata.getPageSize());
        assertEquals(250, metadata.getTotalElements());
        assertTrue(metadata.isHasNext());
        assertTrue(metadata.isHasPrevious());
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyPage() {
        int page = 0;
        int size = 100;

        Page<Counterparty> counterpartyPage = new PageImpl<>(List.of(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name", "id")),
                0);

        when(counterpartyRepository.findAllWithCountry(any(Pageable.class))).thenReturn(counterpartyPage);

        PageResponse<CounterpartyResponseDto> result = counterpartyService.getAll(page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getMetadata().getTotalElements());
        verify(counterpartyRepository).findAllWithCountry(any(Pageable.class));
    }

    @Test
    void getAll_ShouldSortByNameAscAndIdAsc() {
        int page = 0;
        int size = 100;
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        Page<Counterparty> counterpartyPage = new PageImpl<>(List.of(),
                PageRequest.of(page, size), 0);

        when(counterpartyRepository.findAllWithCountry(any(Pageable.class))).thenReturn(counterpartyPage);

        counterpartyService.getAll(page, size);

        verify(counterpartyRepository).findAllWithCountry(pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());

        Sort sort = capturedPageable.getSort();
        assertTrue(sort.isSorted());
        assertEquals(2, sort.stream().count());

        Sort.Order nameOrder = sort.getOrderFor("name");
        assertNotNull(nameOrder);
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());

        Sort.Order idOrder = sort.getOrderFor("id");
        assertNotNull(idOrder);
        assertEquals(Sort.Direction.ASC, idOrder.getDirection());
    }

    @Test
    void getAll_WithCustomPageSize_ShouldUseProvidedSize() {
        int page = 0;
        int size = 50;

        Page<Counterparty> counterpartyPage = new PageImpl<>(List.of(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name", "id")),
                0);

        when(counterpartyRepository.findAllWithCountry(any(Pageable.class))).thenReturn(counterpartyPage);

        PageResponse<CounterpartyResponseDto> result = counterpartyService.getAll(page, size);

        assertEquals(50, result.getMetadata().getPageSize());
    }

    @Test
    void update_WhenValid_ShouldReturnUpdatedCounterparty() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        assertNotNull(result);
        verify(counterpartyMapper).updateEntity(requestDto, counterparty);
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void update_WhenNotExists_ShouldThrowException() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void update_WhenChangingToExistingCode_ShouldThrowException() {
        requestDto.setCode("CP002");
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.existsByCode("CP002")).thenReturn(true);

        assertThrows(DuplicateCounterpartyException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void update_WhenNotChangingCode_ShouldNotCheckExistence() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        assertNotNull(result);
        verify(counterpartyRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_WhenCodeIsNull_ShouldNotCheckExistence() {
        requestDto.setCode(null);
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        assertNotNull(result);
        verify(counterpartyRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_WhenCountryIdIsNull_ShouldNotUpdateCountry() {
        requestDto.setCountryId(null);
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);
        when(counterpartyMapper.toResponse(counterparty)).thenReturn(responseDto);

        CounterpartyResponseDto result = counterpartyService.update(1L, requestDto);

        assertNotNull(result);
        verify(countryRepository, never()).findById(anyLong());
    }

    @Test
    void update_WhenCountryNotFound_ShouldThrowException() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CountryNotFoundException.class,
                () -> counterpartyService.update(1L, requestDto));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void delete_WhenExists_ShouldDeleteSuccessfully() {
        when(counterpartyRepository.existsById(1L)).thenReturn(true);

        counterpartyService.delete(1L);

        verify(counterpartyRepository).deleteById(1L);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        when(counterpartyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.delete(1L));
        verify(counterpartyRepository, never()).deleteById(anyLong());
    }

    @Test
    void activate_WhenExists_ShouldActivateSuccessfully() {
        counterparty.setIsActive(false);
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        counterpartyService.activate(1L);

        assertTrue(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void activate_WhenNotExists_ShouldThrowException() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.activate(1L));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void activate_WhenAlreadyActive_ShouldStillSave() {
        counterparty.setIsActive(true);
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        counterpartyService.activate(1L);

        assertTrue(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void deactivate_WhenExists_ShouldDeactivateSuccessfully() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        counterpartyService.deactivate(1L);

        assertFalse(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }

    @Test
    void deactivate_WhenNotExists_ShouldThrowException() {
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.empty());

        assertThrows(CounterpartyNotFoundException.class,
                () -> counterpartyService.deactivate(1L));
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    void deactivate_WhenAlreadyInactive_ShouldStillSave() {
        counterparty.setIsActive(false);
        when(counterpartyRepository.findByIdWithCountry(1L)).thenReturn(Optional.of(counterparty));
        when(counterpartyRepository.save(counterparty)).thenReturn(counterparty);

        counterpartyService.deactivate(1L);

        assertFalse(counterparty.getIsActive());
        verify(counterpartyRepository).save(counterparty);
    }
}