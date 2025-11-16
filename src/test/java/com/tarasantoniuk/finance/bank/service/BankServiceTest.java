package com.tarasantoniuk.finance.bank.service;

import com.tarasantoniuk.finance.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bank.exception.BankNotFoundException;
import com.tarasantoniuk.finance.bank.exception.DuplicateBankException;
import com.tarasantoniuk.finance.bank.mapper.BankMapper;
import com.tarasantoniuk.finance.bank.repository.BankRepository;
import com.tarasantoniuk.finance.country.entity.Country;
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
class BankServiceTest {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private BankMapper bankMapper;

    @InjectMocks
    private BankService bankService;

    private Bank bank;
    private BankRequestDTO requestDTO;
    private BankResponseDTO responseDTO;
    private Country country;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1L);
        country.setName("Ukraine");

        bank = new Bank();
        bank.setId(1L);
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");
        bank.setCountry(country);
        bank.setAddress("1 Hrushevskoho St, Kyiv");
        bank.setPhoneNumber("+380443639999");
        bank.setWebsite("www.privatbank.ua");
        bank.setIsActive(true);

        requestDTO = new BankRequestDTO();
        requestDTO.setName("PrivatBank");
        requestDTO.setSwiftCode("PBANUA2X");
        requestDTO.setCountryId(1L);
        requestDTO.setAddress("1 Hrushevskoho St, Kyiv");
        requestDTO.setPhoneNumber("+380443639999");
        requestDTO.setWebsite("www.privatbank.ua");
        requestDTO.setIsActive(true);

        responseDTO = new BankResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("PrivatBank");
        responseDTO.setSwiftCode("PBANUA2X");
    }

    @Test
    void getAllBanks_ShouldReturnListOfBanks() {
        // Given
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findAll()).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankResponseDTO> result = bankService.getAllBanks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findAll();
    }

    @Test
    void getBankById_WhenExists_ShouldReturnBank() {
        // Given
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        BankResponseDTO result = bankService.getBankById(1L);

        // Then
        assertNotNull(result);
        assertEquals("PrivatBank", result.getName());
        verify(bankRepository, times(1)).findById(1L);
    }

    @Test
    void getBankById_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankNotFoundException.class,
                () -> bankService.getBankById(1L));
    }

    @Test
    void getBanksByCountry_ShouldReturnFilteredList() {
        // Given
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findByCountryId(1L)).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankResponseDTO> result = bankService.getBanksByCountry(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findByCountryId(1L);
    }

    @Test
    void getActiveBanks_ShouldReturnActiveOnly() {
        // Given
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findByIsActiveTrue()).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankResponseDTO> result = bankService.getActiveBanks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getBankBySwiftCode_WhenExists_ShouldReturnBank() {
        // Given
        when(bankRepository.findBySwiftCode("PBANUA2X")).thenReturn(Optional.of(bank));
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        BankResponseDTO result = bankService.getBankBySwiftCode("PBANUA2X");

        // Then
        assertNotNull(result);
        assertEquals("PBANUA2X", result.getSwiftCode());
        verify(bankRepository, times(1)).findBySwiftCode("PBANUA2X");
    }

    @Test
    void getBankBySwiftCode_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankRepository.findBySwiftCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankNotFoundException.class,
                () -> bankService.getBankBySwiftCode("INVALID"));
    }

    @Test
    void createBank_WhenValid_ShouldReturnCreatedBank() {
        // Given
        when(bankRepository.findBySwiftCode("PBANUA2X")).thenReturn(Optional.empty());
        when(bankMapper.toEntity(requestDTO)).thenReturn(bank);
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        BankResponseDTO result = bankService.createBank(requestDTO);

        // Then
        assertNotNull(result);
        verify(bankRepository, times(1)).save(any(Bank.class));
    }

    @Test
    void createBank_WhenDuplicateSwiftCode_ShouldThrowException() {
        // Given
        when(bankRepository.findBySwiftCode("PBANUA2X")).thenReturn(Optional.of(bank));

        // When & Then
        assertThrows(DuplicateBankException.class,
                () -> bankService.createBank(requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void updateBank_WhenValid_ShouldReturnUpdatedBank() {
        // Given
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.findBySwiftCode("PBANUA2X")).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        BankResponseDTO result = bankService.updateBank(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(bankRepository, times(1)).save(bank);
        verify(bankMapper, times(1)).updateEntity(requestDTO, bank);
    }

    @Test
    void updateBank_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankNotFoundException.class,
                () -> bankService.updateBank(1L, requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void updateBank_WhenDuplicateSwiftCode_ShouldThrowException() {
        // Given
        Bank anotherBank = new Bank();
        anotherBank.setId(2L);
        anotherBank.setSwiftCode("PBANUA2X");

        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.findBySwiftCode("PBANUA2X")).thenReturn(Optional.of(anotherBank));

        // When & Then
        assertThrows(DuplicateBankException.class,
                () -> bankService.updateBank(1L, requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void activateBank_WhenExists_ShouldActivateSuccessfully() {
        // Given
        bank.setIsActive(false);
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        bankService.activateBank(1L);

        // Then
        assertTrue(bank.getIsActive());
        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    void deactivateBank_WhenExists_ShouldDeactivateSuccessfully() {
        // Given
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        // When
        bankService.deactivateBank(1L);

        // Then
        assertFalse(bank.getIsActive());
        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    void deleteBank_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(bankRepository.existsById(1L)).thenReturn(true);

        // When
        bankService.deleteBank(1L);

        // Then
        verify(bankRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBank_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(BankNotFoundException.class,
                () -> bankService.deleteBank(1L));
        verify(bankRepository, never()).deleteById(anyLong());
    }
}