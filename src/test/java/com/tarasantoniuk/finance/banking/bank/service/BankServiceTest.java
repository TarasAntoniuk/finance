package com.tarasantoniuk.finance.banking.bank.service;

import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bank.exception.BankNotFoundException;
import com.tarasantoniuk.finance.banking.bank.exception.DuplicateBankException;
import com.tarasantoniuk.finance.banking.bank.mapper.BankMapper;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.core.country.entity.Country;
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
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findAllWithRelations()).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        List<BankResponseDTO> result = bankService.getAllBanks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findAllWithRelations();
    }

    @Test
    void getBankById_WhenExists_ShouldReturnBank() {
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bank));
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        BankResponseDTO result = bankService.getBankById(1L);

        assertNotNull(result);
        assertEquals("PrivatBank", result.getName());
        verify(bankRepository, times(1)).findByIdWithRelations(1L);
    }

    @Test
    void getBankById_WhenNotExists_ShouldThrowException() {
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankNotFoundException.class,
                () -> bankService.getBankById(1L));
    }

    @Test
    void getBanksByCountry_ShouldReturnFilteredList() {
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findByCountryIdWithRelations(1L)).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        List<BankResponseDTO> result = bankService.getBanksByCountry(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findByCountryIdWithRelations(1L);
    }

    @Test
    void getActiveBanks_ShouldReturnActiveOnly() {
        List<Bank> banks = Collections.singletonList(bank);
        when(bankRepository.findActiveWithRelations()).thenReturn(banks);
        when(bankMapper.toResponseList(banks)).thenReturn(Collections.singletonList(responseDTO));

        List<BankResponseDTO> result = bankService.getActiveBanks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankRepository, times(1)).findActiveWithRelations();
    }

    @Test
    void getBankBySwiftCode_WhenExists_ShouldReturnBank() {
        when(bankRepository.findBySwiftCodeWithRelations("PBANUA2X")).thenReturn(Optional.of(bank));
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        BankResponseDTO result = bankService.getBankBySwiftCode("PBANUA2X");

        assertNotNull(result);
        assertEquals("PBANUA2X", result.getSwiftCode());
        verify(bankRepository, times(1)).findBySwiftCodeWithRelations("PBANUA2X");
    }

    @Test
    void getBankBySwiftCode_WhenNotExists_ShouldThrowException() {
        when(bankRepository.findBySwiftCodeWithRelations("INVALID")).thenReturn(Optional.empty());

        assertThrows(BankNotFoundException.class,
                () -> bankService.getBankBySwiftCode("INVALID"));
    }

    @Test
    void createBank_WhenValid_ShouldReturnCreatedBank() {
        when(bankRepository.findBySwiftCodeWithRelations("PBANUA2X")).thenReturn(Optional.empty());
        when(bankMapper.toEntity(requestDTO)).thenReturn(bank);
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        BankResponseDTO result = bankService.createBank(requestDTO);

        assertNotNull(result);
        verify(bankRepository, times(1)).save(any(Bank.class));
    }

    @Test
    void createBank_WhenDuplicateSwiftCode_ShouldThrowException() {
        when(bankRepository.findBySwiftCodeWithRelations("PBANUA2X")).thenReturn(Optional.of(bank));

        assertThrows(DuplicateBankException.class,
                () -> bankService.createBank(requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void updateBank_WhenValid_ShouldReturnUpdatedBank() {
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.findBySwiftCodeWithRelations("PBANUA2X")).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        BankResponseDTO result = bankService.updateBank(1L, requestDTO);

        assertNotNull(result);
        verify(bankRepository, times(1)).save(bank);
        verify(bankMapper, times(1)).updateEntity(requestDTO, bank);
    }

    @Test
    void updateBank_WhenNotExists_ShouldThrowException() {
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankNotFoundException.class,
                () -> bankService.updateBank(1L, requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void updateBank_WhenDuplicateSwiftCode_ShouldThrowException() {
        Bank anotherBank = new Bank();
        anotherBank.setId(2L);
        anotherBank.setSwiftCode("PBANUA2X");

        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.findBySwiftCodeWithRelations("PBANUA2X")).thenReturn(Optional.of(anotherBank));

        assertThrows(DuplicateBankException.class,
                () -> bankService.updateBank(1L, requestDTO));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void activateBank_WhenExists_ShouldActivateSuccessfully() {
        bank.setIsActive(false);
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        bankService.activateBank(1L);

        assertTrue(bank.getIsActive());
        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    void deactivateBank_WhenExists_ShouldDeactivateSuccessfully() {
        when(bankRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(bankMapper.toResponse(bank)).thenReturn(responseDTO);

        bankService.deactivateBank(1L);

        assertFalse(bank.getIsActive());
        verify(bankRepository, times(1)).save(bank);
    }

    @Test
    void deleteBank_WhenExists_ShouldDeleteSuccessfully() {
        when(bankRepository.existsById(1L)).thenReturn(true);

        bankService.deleteBank(1L);

        verify(bankRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBank_WhenNotExists_ShouldThrowException() {
        when(bankRepository.existsById(1L)).thenReturn(false);

        assertThrows(BankNotFoundException.class,
                () -> bankService.deleteBank(1L));
        verify(bankRepository, never()).deleteById(anyLong());
    }
}