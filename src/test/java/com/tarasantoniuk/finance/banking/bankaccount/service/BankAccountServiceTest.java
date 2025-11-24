package com.tarasantoniuk.finance.banking.bankaccount.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDTO;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDTO;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.exception.BankAccountNotFoundException;
import com.tarasantoniuk.finance.banking.bankaccount.exception.DuplicateBankAccountException;
import com.tarasantoniuk.finance.banking.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
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
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findAllWithRelations()).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDTO> result = bankAccountService.getAllBankAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findAllWithRelations();
    }

    @Test
    void getBankAccountById_WhenExists_ShouldReturnBankAccount() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDTO result = bankAccountService.getBankAccountById(1L);

        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findByIdWithRelations(1L);
    }

    @Test
    void getBankAccountById_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.getBankAccountById(1L));
    }

    @Test
    void getBankAccountByAccountNumber_WhenExists_ShouldReturnBankAccount() {
        when(bankAccountRepository.findByAccountNumberWithRelations("UA213223130000026007233566001"))
                .thenReturn(Optional.of(bankAccount));
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDTO result = bankAccountService.getBankAccountByAccountNumber("UA213223130000026007233566001");

        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findByAccountNumberWithRelations("UA213223130000026007233566001");
    }

    @Test
    void getBankAccountByAccountNumber_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByAccountNumberWithRelations("INVALID")).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.getBankAccountByAccountNumber("INVALID"));
    }

    @Test
    void getBankAccountsByHolder_ShouldReturnFilteredList() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByHolderWithRelations(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByHolderWithRelations(AccountHolderType.ORGANIZATION, 10L);
    }

    @Test
    void getBankAccountsByBank_ShouldReturnFilteredList() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByBankIdWithRelations(1L)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByBank(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByBankIdWithRelations(1L);
    }

    @Test
    void getBankAccountsByStatus_ShouldReturnFilteredList() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByStatusWithRelations(AccountStatus.ACTIVE)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDTO> result = bankAccountService.getBankAccountsByStatus(AccountStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByStatusWithRelations(AccountStatus.ACTIVE);
    }

    @Test
    void getDefaultBankAccountsByHolder_ShouldReturnDefaultAccounts() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDTO> result = bankAccountService.getDefaultBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1))
                .findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, 10L);
    }

    @Test
    void createBankAccount_WhenValid_ShouldReturnCreatedBankAccount() {
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.empty());
        when(bankAccountMapper.toEntity(requestDTO)).thenReturn(bankAccount);
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDTO result = bankAccountService.createBankAccount(requestDTO);

        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void createBankAccount_WhenDuplicateAccountNumber_ShouldThrowException() {
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.of(bankAccount));

        assertThrows(DuplicateBankAccountException.class,
                () -> bankAccountService.createBankAccount(requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void updateBankAccount_WhenValid_ShouldReturnUpdatedBankAccount() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001"))
                .thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDTO result = bankAccountService.updateBankAccount(1L, requestDTO);

        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(bankAccount);
        verify(bankAccountMapper, times(1)).updateEntity(requestDTO, bankAccount);
    }

    @Test
    void updateBankAccount_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.updateBankAccount(1L, requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void updateBankAccount_WhenDuplicateAccountNumber_ShouldThrowException() {
        BankAccount anotherAccount = new BankAccount();
        anotherAccount.setId(2L);
        anotherAccount.setAccountNumber("UA213223130000026007233566001");

        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001"))
                .thenReturn(Optional.of(anotherAccount));

        assertThrows(DuplicateBankAccountException.class,
                () -> bankAccountService.updateBankAccount(1L, requestDTO));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void changeStatus_WhenExists_ShouldChangeStatusSuccessfully() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        bankAccountService.changeStatus(1L, AccountStatus.INACTIVE);

        assertEquals(AccountStatus.INACTIVE, bankAccount.getStatus());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void setAsDefault_WhenExists_ShouldSetDefaultSuccessfully() {
        bankAccount.setIsDefault(false);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        bankAccountService.setAsDefault(1L);

        assertTrue(bankAccount.getIsDefault());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void unsetAsDefault_WhenExists_ShouldUnsetDefaultSuccessfully() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        bankAccountService.unsetAsDefault(1L);

        assertFalse(bankAccount.getIsDefault());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void deleteBankAccount_WhenExists_ShouldDeleteSuccessfully() {
        when(bankAccountRepository.existsById(1L)).thenReturn(true);

        bankAccountService.deleteBankAccount(1L);

        verify(bankAccountRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBankAccount_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.existsById(1L)).thenReturn(false);

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.deleteBankAccount(1L));
        verify(bankAccountRepository, never()).deleteById(anyLong());
    }
}