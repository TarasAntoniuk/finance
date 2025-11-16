package com.tarasantoniuk.finance.bankaccount.service;

import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bankaccount.dto.BankAccountRequestDTO;
import com.tarasantoniuk.finance.bankaccount.dto.BankAccountResponseDTO;
import com.tarasantoniuk.finance.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.bankaccount.exception.BankAccountNotFoundException;
import com.tarasantoniuk.finance.bankaccount.exception.DuplicateBankAccountException;
import com.tarasantoniuk.finance.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.currency.entity.Currency;
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
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @InjectMocks
    private BankAccountService bankAccountService;

    private BankAccount bankAccount;
    private BankAccountRequestDTO requestDTO;
    private BankAccountResponseDTO responseDTO;
    private Bank bank;
    private Currency currency;

    @BeforeEach
    void setUp() {
        bank = new Bank();
        bank.setId(1L);
        bank.setName("PrivatBank");

        currency = new Currency();
        currency.setId(1L);
        currency.setCode("UAH");

        bankAccount = new BankAccount();
        bankAccount.setId(1L);
        bankAccount.setAccountNumber("UA213223130000026007233566001");
        bankAccount.setHolderType(AccountHolderType.ORGANIZATION);
        bankAccount.setHolderId(10L);
        bankAccount.setBank(bank);
        bankAccount.setCurrency(currency);
        bankAccount.setAccountName("Main UAH Account");
        bankAccount.setStatus(AccountStatus.ACTIVE);
        bankAccount.setIsDefault(true);

        requestDTO = new BankAccountRequestDTO();
        requestDTO.setAccountNumber("UA213223130000026007233566001");
        requestDTO.setHolderType(AccountHolderType.ORGANIZATION);
        requestDTO.setHolderId(10L);
        requestDTO.setBankId(1L);
        requestDTO.setCurrencyId(1L);
        requestDTO.setAccountName("Main UAH Account");
        requestDTO.setStatus(AccountStatus.ACTIVE);
        requestDTO.setIsDefault(true);

        responseDTO = new BankAccountResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAccountNumber("UA213223130000026007233566001");
        responseDTO.setHolderType(AccountHolderType.ORGANIZATION);
        responseDTO.setHolderId(10L);
    }

    @Test
    void getAllBankAccounts_ShouldReturnListOfBankAccounts() {
        // Given
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findAll()).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankAccountResponseDTO> result = bankAccountService.getAllBankAccounts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void getBankAccountById_WhenExists_ShouldReturnBankAccount() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        BankAccountResponseDTO result = bankAccountService.getBankAccountById(1L);

        // Then
        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findById(1L);
    }

    @Test
    void getBankAccountById_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.getBankAccountById(1L));
    }

    @Test
    void getBankAccountByAccountNumber_WhenExists_ShouldReturnBankAccount() {
        // Given
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.of(bankAccount));
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        BankAccountResponseDTO result = bankAccountService.getBankAccountByAccountNumber("UA213223130000026007233566001");

        // Then
        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findByAccountNumber("UA213223130000026007233566001");
    }

    @Test
    void getBankAccountByAccountNumber_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.getBankAccountByAccountNumber("INVALID"));
    }

    @Test
    void getBankAccountsByHolder_ShouldReturnFilteredList() {
        // Given
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByHolderTypeAndHolderId(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByHolderTypeAndHolderId(AccountHolderType.ORGANIZATION, 10L);
    }

    @Test
    void getBankAccountsByBank_ShouldReturnFilteredList() {
        // Given
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByBankId(1L)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByBank(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByBankId(1L);
    }

    @Test
    void getBankAccountsByStatus_ShouldReturnFilteredList() {
        // Given
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByStatus(AccountStatus.ACTIVE)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByStatus(AccountStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByStatus(AccountStatus.ACTIVE);
    }

    @Test
    void getDefaultBankAccountsByHolder_ShouldReturnDefaultAccounts() {
        // Given
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByHolderTypeAndHolderIdAndIsDefaultTrue(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        // When
        List<BankAccountResponseDTO> result = bankAccountService.getDefaultBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1))
                .findByHolderTypeAndHolderIdAndIsDefaultTrue(AccountHolderType.ORGANIZATION, 10L);
    }

    @Test
    void createBankAccount_WhenValid_ShouldReturnCreatedBankAccount() {
        // Given
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.empty());
        when(bankAccountMapper.toEntity(requestDTO)).thenReturn(bankAccount);
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        BankAccountResponseDTO result = bankAccountService.createBankAccount(requestDTO);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void createBankAccount_WhenDuplicateAccountNumber_ShouldThrowException() {
        // Given
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.of(bankAccount));

        // When & Then
        assertThrows(DuplicateBankAccountException.class,
                () -> bankAccountService.createBankAccount(requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void updateBankAccount_WhenValid_ShouldReturnUpdatedBankAccount() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001"))
                .thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        BankAccountResponseDTO result = bankAccountService.updateBankAccount(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(bankAccount);
        verify(bankAccountMapper, times(1)).updateEntity(requestDTO, bankAccount);
    }

    @Test
    void updateBankAccount_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.updateBankAccount(1L, requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void updateBankAccount_WhenDuplicateAccountNumber_ShouldThrowException() {
        // Given
        BankAccount anotherAccount = new BankAccount();
        anotherAccount.setId(2L);
        anotherAccount.setAccountNumber("UA213223130000026007233566001");

        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001"))
                .thenReturn(Optional.of(anotherAccount));

        // When & Then
        assertThrows(DuplicateBankAccountException.class,
                () -> bankAccountService.updateBankAccount(1L, requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void changeStatus_WhenExists_ShouldChangeStatusSuccessfully() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        bankAccountService.changeStatus(1L, AccountStatus.INACTIVE);

        // Then
        assertEquals(AccountStatus.INACTIVE, bankAccount.getStatus());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void setAsDefault_WhenExists_ShouldSetDefaultSuccessfully() {
        // Given
        bankAccount.setIsDefault(false);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        bankAccountService.setAsDefault(1L);

        // Then
        assertTrue(bankAccount.getIsDefault());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void unsetAsDefault_WhenExists_ShouldUnsetDefaultSuccessfully() {
        // Given
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        // When
        bankAccountService.unsetAsDefault(1L);

        // Then
        assertFalse(bankAccount.getIsDefault());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void deleteBankAccount_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(bankAccountRepository.existsById(1L)).thenReturn(true);

        // When
        bankAccountService.deleteBankAccount(1L);

        // Then
        verify(bankAccountRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBankAccount_WhenNotExists_ShouldThrowException() {
        // Given
        when(bankAccountRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.deleteBankAccount(1L));
        verify(bankAccountRepository, never()).deleteById(anyLong());
    }
}