package com.tarasantoniuk.finance.banking.bankaccount.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDto;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.exception.BankAccountNotFoundException;
import com.tarasantoniuk.finance.banking.bankaccount.exception.DuplicateBankAccountException;
import com.tarasantoniuk.finance.banking.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
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

    private static final Long CALLER_ORG_ID = 10L;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private OrganizationSecurityContext orgContext;

    @InjectMocks
    private BankAccountService bankAccountService;

    private BankAccount bankAccount;
    private BankAccountRequestDto requestDTO;
    private BankAccountResponseDto responseDTO;
    private Bank bank;
    private Currency currency;

    @BeforeEach
    void setUp() {
        lenient().when(orgContext.isAdmin()).thenReturn(false);
        lenient().when(orgContext.getActiveOrganizationId()).thenReturn(CALLER_ORG_ID);
        lenient().when(orgContext.resolveOrganizationId(any())).thenReturn(CALLER_ORG_ID);

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
        bankAccount.setHolderId(CALLER_ORG_ID);
        bankAccount.setBank(bank);
        bankAccount.setCurrency(currency);
        bankAccount.setAccountName("Main UAH Account");
        bankAccount.setStatus(AccountStatus.ACTIVE);
        bankAccount.setIsDefault(true);

        requestDTO = new BankAccountRequestDto();
        requestDTO.setAccountNumber("UA213223130000026007233566001");
        requestDTO.setHolderType(AccountHolderType.ORGANIZATION);
        requestDTO.setHolderId(CALLER_ORG_ID);
        requestDTO.setBankId(1L);
        requestDTO.setCurrencyId(1L);
        requestDTO.setAccountName("Main UAH Account");
        requestDTO.setStatus(AccountStatus.ACTIVE);
        requestDTO.setIsDefault(true);

        responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setAccountNumber("UA213223130000026007233566001");
        responseDTO.setHolderType(AccountHolderType.ORGANIZATION);
        responseDTO.setHolderId(CALLER_ORG_ID);
    }

    @Test
    void getAllBankAccounts_ShouldReturnListOfBankAccounts() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findAllWithRelations(CALLER_ORG_ID)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getAllBankAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findAllWithRelations(CALLER_ORG_ID);
    }

    @Test
    void getBankAccountById_WhenExists_ShouldReturnBankAccount() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDto result = bankAccountService.getBankAccountById(1L);

        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findByIdWithRelations(1L);
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
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

        BankAccountResponseDto result = bankAccountService.getBankAccountByAccountNumber("UA213223130000026007233566001");

        assertNotNull(result);
        assertEquals("UA213223130000026007233566001", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findByAccountNumberWithRelations("UA213223130000026007233566001");
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
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
        when(bankAccountRepository.findByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolder(AccountHolderType.ORGANIZATION, CALLER_ORG_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID);
    }

    @Test
    void getBankAccountsByBank_ShouldReturnFilteredList() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByBankIdWithRelations(1L, CALLER_ORG_ID)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByBank(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByBankIdWithRelations(1L, CALLER_ORG_ID);
    }

    @Test
    void getBankAccountsByStatus_ShouldReturnFilteredList() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findByStatusWithRelations(AccountStatus.ACTIVE, CALLER_ORG_ID)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByStatus(AccountStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByStatusWithRelations(AccountStatus.ACTIVE, CALLER_ORG_ID);
    }

    @Test
    void getDefaultBankAccountsByHolder_ShouldReturnDefaultAccounts() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID))
                .thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getDefaultBankAccountsByHolder(AccountHolderType.ORGANIZATION, CALLER_ORG_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1))
                .findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID);
    }

    @Test
    void createBankAccount_WhenValid_ShouldReturnCreatedBankAccount() {
        when(bankAccountRepository.findByAccountNumber("UA213223130000026007233566001")).thenReturn(Optional.empty());
        when(bankAccountMapper.toEntity(requestDTO)).thenReturn(bankAccount);
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDto result = bankAccountService.createBankAccount(requestDTO);

        assertNotNull(result);
        verify(orgContext, times(1)).resolveOrganizationId(CALLER_ORG_ID);
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

        BankAccountResponseDto result = bankAccountService.updateBankAccount(1L, requestDTO);

        assertNotNull(result);
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
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
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
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
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void unsetAsDefault_WhenExists_ShouldUnsetDefaultSuccessfully() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        bankAccountService.unsetAsDefault(1L);

        assertFalse(bankAccount.getIsDefault());
        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void deleteBankAccount_WhenExists_ShouldDeleteSuccessfully() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));

        bankAccountService.deleteBankAccount(1L);

        verify(orgContext, times(1)).validateAccess(CALLER_ORG_ID);
        verify(bankAccountRepository, times(1)).delete(bankAccount);
    }

    @Test
    void deleteBankAccount_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.deleteBankAccount(1L));
        verify(bankAccountRepository, never()).delete(any(BankAccount.class));
    }

    // ==================== getBankAccountsByHolderType Tests ====================

    @Test
    void getBankAccountsByHolderType_WhenOrganization_ShouldReturnOrganizationAccounts() {
        List<BankAccount> bankAccounts = Collections.singletonList(bankAccount);
        when(bankAccountRepository.findOrganizationAccountsWithRelations(CALLER_ORG_ID)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolderType(AccountHolderType.ORGANIZATION);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findOrganizationAccountsWithRelations(CALLER_ORG_ID);
        verify(bankAccountRepository, never()).findCounterpartyAccountsWithRelations();
    }

    @Test
    void getBankAccountsByHolderType_WhenCounterparty_ShouldReturnCounterpartyAccounts() {
        BankAccount counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(2L);
        counterpartyAccount.setAccountNumber("UA987654321098765432109876543");
        counterpartyAccount.setHolderType(AccountHolderType.COUNTERPARTY);
        counterpartyAccount.setHolderId(20L);

        List<BankAccount> bankAccounts = Collections.singletonList(counterpartyAccount);
        when(bankAccountRepository.findCounterpartyAccountsWithRelations()).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(Collections.singletonList(responseDTO));

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolderType(AccountHolderType.COUNTERPARTY);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findCounterpartyAccountsWithRelations();
        verify(bankAccountRepository, never()).findOrganizationAccountsWithRelations(anyLong());
    }

    // ==================== Error Handling Tests ====================

    @Test
    void changeStatus_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.changeStatus(1L, AccountStatus.INACTIVE));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void setAsDefault_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.setAsDefault(1L));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void unsetAsDefault_WhenNotExists_ShouldThrowException() {
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> bankAccountService.unsetAsDefault(1L));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    // ==================== Empty List Tests ====================

    @Test
    void getAllBankAccounts_WhenEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findAllWithRelations(CALLER_ORG_ID)).thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getAllBankAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bankAccountRepository, times(1)).findAllWithRelations(CALLER_ORG_ID);
    }

    @Test
    void getBankAccountsByHolder_WhenEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID))
                .thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolder(AccountHolderType.ORGANIZATION, CALLER_ORG_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBankAccountsByBank_WhenEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findByBankIdWithRelations(1L, CALLER_ORG_ID)).thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByBank(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBankAccountsByStatus_WhenEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findByStatusWithRelations(AccountStatus.ACTIVE, CALLER_ORG_ID)).thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByStatus(AccountStatus.ACTIVE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDefaultBankAccountsByHolder_WhenEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, CALLER_ORG_ID, CALLER_ORG_ID))
                .thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getDefaultBankAccountsByHolder(AccountHolderType.ORGANIZATION, CALLER_ORG_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBankAccountsByHolderType_WhenOrganizationEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findOrganizationAccountsWithRelations(CALLER_ORG_ID)).thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolderType(AccountHolderType.ORGANIZATION);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBankAccountsByHolderType_WhenCounterpartyEmpty_ShouldReturnEmptyList() {
        when(bankAccountRepository.findCounterpartyAccountsWithRelations()).thenReturn(Collections.emptyList());
        when(bankAccountMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<BankAccountResponseDto> result = bankAccountService.getBankAccountsByHolderType(AccountHolderType.COUNTERPARTY);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Status Change Tests ====================

    @Test
    void changeStatus_ToInactive_ShouldChangeStatusCorrectly() {
        bankAccount.setStatus(AccountStatus.ACTIVE);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDto result = bankAccountService.changeStatus(1L, AccountStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(AccountStatus.INACTIVE, bankAccount.getStatus());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void changeStatus_ToActive_ShouldChangeStatusCorrectly() {
        bankAccount.setStatus(AccountStatus.INACTIVE);
        when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);
        when(bankAccountMapper.toResponse(bankAccount)).thenReturn(responseDTO);

        BankAccountResponseDto result = bankAccountService.changeStatus(1L, AccountStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(AccountStatus.ACTIVE, bankAccount.getStatus());
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }
}
