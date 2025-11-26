package com.tarasantoniuk.finance.banking.bankreceipt.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.banking.bankreceipt.mapper.BankReceiptMapper;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.common.TestDataFactory;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankReceiptServiceTest {

    @Mock
    private BankReceiptRepository bankReceiptRepository;

    @Mock
    private BankReceiptMapper bankReceiptMapper;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private BankReceiptService bankReceiptService;

    @Mock
    private BankAccountTransactionService transactionService;

    private BankReceiptRequestDto requestDto;
    private BankReceipt receipt;
    private BankReceiptResponseDto responseDto;
    private BankAccount account;
    private Counterparty counterparty;
    private Currency currency;
    private Organization organization;

    @BeforeEach
    void setUp() {
        // Setup test data
        requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("10000.00"));
        requestDto.setAccountId(1L);
        requestDto.setCounterpartyId(5L);
        requestDto.setCurrencyId(2L);
        requestDto.setOrganizationId(1L);
        requestDto.setExternalTransactionId("EXT-12345");

        account = new BankAccount();
        account.setId(1L);

        counterparty = new Counterparty();
        counterparty.setId(5L);

        currency = new Currency();
        currency.setId(2L);

        organization = new Organization();
        organization.setId(1L);

        receipt = new BankReceipt();
        receipt.setId(100L);
        receipt.setDocumentDate(requestDto.getDocumentDate());
        receipt.setReceiptType(requestDto.getReceiptType());
        receipt.setAmount(requestDto.getAmount());
        receipt.setAccount(account);
        receipt.setCounterparty(counterparty);
        receipt.setCurrency(currency);
        receipt.setOrganization(organization);
        receipt.setStatus(DocumentStatus.DRAFT);
        receipt.setExternalTransactionId(requestDto.getExternalTransactionId());

        responseDto = new BankReceiptResponseDto();
        responseDto.setId(100L);
        responseDto.setStatus(DocumentStatus.DRAFT);
    }

    @Test
    void create_ShouldCreateReceipt_WhenValidRequest() {
        // Given
        when(bankReceiptRepository.existsByExternalTransactionId(any())).thenReturn(false);
        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(5L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(2L)).thenReturn(Optional.of(currency));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(bankReceiptRepository.save(any(BankReceipt.class))).thenReturn(receipt);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.create(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(DocumentStatus.DRAFT, result.getStatus());
        verify(bankReceiptRepository).existsByExternalTransactionId(requestDto.getExternalTransactionId());
        verify(bankReceiptMapper).toEntity(requestDto);
        verify(bankReceiptRepository).save(any(BankReceipt.class));
        verify(bankReceiptMapper).toResponseDto(receipt);
    }

    @Test
    void create_ShouldThrowException_WhenDuplicateExternalTransactionId() {
        // Given
        when(bankReceiptRepository.existsByExternalTransactionId(any())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class,
                () -> bankReceiptService.create(requestDto));
        verify(bankReceiptRepository).existsByExternalTransactionId(requestDto.getExternalTransactionId());
        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenAccountNotFound() {
        // Given
        when(bankReceiptRepository.existsByExternalTransactionId(any())).thenReturn(false);
        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.create(requestDto));
        verify(bankAccountRepository).findById(1L);
        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenCounterpartyNotFound() {
        // Given
        when(bankReceiptRepository.existsByExternalTransactionId(any())).thenReturn(false);
        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(5L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.create(requestDto));
        verify(counterpartyRepository).findById(5L);
        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateReceipt_WhenStatusIsDraft() {
        // Given
        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setDocumentDate(LocalDate.of(2024, 2, 20));
        updateDto.setReceiptType(ReceiptType.REFUND);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setAccountId(1L);
        updateDto.setCounterpartyId(5L);
        updateDto.setCurrencyId(2L);
        updateDto.setOrganizationId(1L);

        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(receipt));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(5L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(2L)).thenReturn(Optional.of(currency));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(bankReceiptRepository.save(any(BankReceipt.class))).thenReturn(receipt);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.update(100L, updateDto);

        // Then
        assertNotNull(result);
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptMapper).updateEntityFromDto(updateDto, receipt);
        verify(bankReceiptRepository).save(receipt);
    }

    @Test
    void update_ShouldThrowException_WhenStatusIsPosted() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class,
                () -> bankReceiptService.update(100L, requestDto));
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenReceiptNotFound() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.update(100L, requestDto));
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnReceipt_WhenExists() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(receipt));
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.findById(100L);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptMapper).toResponseDto(receipt);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.findById(100L));
        verify(bankReceiptRepository).findByIdWithDetails(100L);
    }

    @Test
    void findAll_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<BankReceipt> page = new PageImpl<>(List.of(receipt), pageable, 1);

        when(bankReceiptRepository.findAllWithDetails(pageable)).thenReturn(page);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result = bankReceiptService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().getCurrentPage());
        assertEquals(1, result.getMetadata().getTotalElements());
        verify(bankReceiptRepository).findAllWithDetails(pageable);
    }

    @Test
    void findByAccountId_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<BankReceipt> page = new PageImpl<>(List.of(receipt), pageable, 1);

        when(bankReceiptRepository.findByAccountId(1L, pageable)).thenReturn(page);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result = bankReceiptService.findByAccountId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByAccountId(1L, pageable);
    }

    @Test
    void findByStatus_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<BankReceipt> page = new PageImpl<>(List.of(receipt), pageable, 1);

        when(bankReceiptRepository.findByStatus(DocumentStatus.DRAFT, pageable)).thenReturn(page);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result = bankReceiptService.findByStatus(DocumentStatus.DRAFT, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByStatus(DocumentStatus.DRAFT, pageable);
    }

    @Test
    void delete_ShouldDeleteReceipt_WhenStatusIsDraft() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(receipt));

        // When
        bankReceiptService.delete(100L);

        // Then
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptRepository).delete(receipt);
    }

    @Test
    void delete_ShouldThrowException_WhenStatusIsPosted() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class,
                () -> bankReceiptService.delete(100L));
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowException_WhenReceiptNotFound() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.delete(100L));
        verify(bankReceiptRepository).findByIdWithDetails(100L);
        verify(bankReceiptRepository, never()).delete(any());
    }

    // ========== POST TESTS ==========

    @Test
    @DisplayName("post() - should post DRAFT receipt and create transaction event")
    void post_ShouldPostReceiptAndCreateEvent() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.DRAFT);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));
        when(transactionService.existsByDocument("BankReceipt", receipt.getId()))
                .thenReturn(false);

        BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
        mockEvent.setId(1L);
        when(transactionService.createReceiptEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString()))
                .thenReturn(mockEvent);

        when(bankReceiptRepository.save(any(BankReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankReceiptResponseDto expectedDto = TestDataFactory.createBankReceiptResponseDto();
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(expectedDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.post(receipt.getId());

        // Then
        assertNotNull(result);
        verify(transactionService).existsByDocument("BankReceipt", receipt.getId());
        verify(transactionService).createReceiptEvent(
                receipt.getAccount().getId(),
                receipt.getOrganization().getId(),
                receipt.getCurrency().getId(),
                receipt.getDocumentDate(),
                receipt.getAmount(),
                "BankReceipt",
                receipt.getId(),
                receipt.getDescription()
        );
        assertEquals(DocumentStatus.POSTED, receipt.getStatus());
        verify(bankReceiptRepository).save(receipt);
    }

    @Test
    @DisplayName("post() - should throw exception when receipt not found")
    void post_ShouldThrowException_WhenReceiptNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(bankReceiptRepository.findByIdWithDetails(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.post(nonExistentId));

        verify(transactionService, never()).createReceiptEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("post() - should throw exception when receipt is not DRAFT")
    void post_ShouldThrowException_WhenReceiptNotDraft() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.POSTED);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class,
                () -> bankReceiptService.post(receipt.getId()));

        verify(transactionService, never()).existsByDocument(anyString(), anyLong());
        verify(transactionService, never()).createReceiptEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("post() - should throw exception when transaction event already exists")
    void post_ShouldThrowException_WhenEventAlreadyExists() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.DRAFT);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));
        when(transactionService.existsByDocument("BankReceipt", receipt.getId()))
                .thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class,
                () -> bankReceiptService.post(receipt.getId()));

        verify(transactionService, never()).createReceiptEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
        verify(bankReceiptRepository, never()).save(any(BankReceipt.class));
    }

// ========== UNPOST TESTS ==========

    @Test
    @DisplayName("unpost() - should unpost POSTED receipt and reverse transaction")
    void unpost_ShouldUnpostReceiptAndReverseTransaction() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.POSTED);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        BankAccountTransactionEvent originalEvent = new BankAccountTransactionEvent();
        originalEvent.setId(1L);
        when(transactionService.findByDocument("BankReceipt", receipt.getId()))
                .thenReturn(originalEvent);

        BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
        reversalEvent.setId(2L);
        when(transactionService.createPaymentEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString()))
                .thenReturn(reversalEvent);

        when(bankReceiptRepository.save(any(BankReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankReceiptResponseDto expectedDto = TestDataFactory.createBankReceiptResponseDto();
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(expectedDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.unpost(receipt.getId());

        // Then
        assertNotNull(result);
        verify(transactionService).findByDocument("BankReceipt", receipt.getId());
        verify(transactionService).createPaymentEvent(
                receipt.getAccount().getId(),
                receipt.getOrganization().getId(),
                receipt.getCurrency().getId(),
                receipt.getDocumentDate(),
                receipt.getAmount(),
                "BankReceiptReversal",
                receipt.getId(),
                "Reversal of: " + receipt.getDescription()
        );
        verify(transactionService).reverseTransaction(originalEvent.getId(), reversalEvent.getId());
        assertEquals(DocumentStatus.DRAFT, receipt.getStatus());
        verify(bankReceiptRepository).save(receipt);
    }

    @Test
    @DisplayName("unpost() - should throw exception when receipt not found")
    void unpost_ShouldThrowException_WhenReceiptNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(bankReceiptRepository.findByIdWithDetails(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.unpost(nonExistentId));

        verify(transactionService, never()).findByDocument(anyString(), anyLong());
        verify(transactionService, never()).createPaymentEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("unpost() - should throw exception when receipt is not POSTED")
    void unpost_ShouldThrowException_WhenReceiptNotPosted() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.DRAFT);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class,
                () -> bankReceiptService.unpost(receipt.getId()));

        verify(transactionService, never()).findByDocument(anyString(), anyLong());
        verify(transactionService, never()).createPaymentEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("unpost() - should throw exception when original event not found")
    void unpost_ShouldThrowException_WhenOriginalEventNotFound() {
        // Given
        BankReceipt receipt = TestDataFactory.createTestBankReceipt();
        receipt.setStatus(DocumentStatus.POSTED);

        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.of(receipt));
        when(transactionService.findByDocument("BankReceipt", receipt.getId()))
                .thenThrow(new ResourceNotFoundException("Transaction event not found"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> bankReceiptService.unpost(receipt.getId()));

        verify(transactionService, never()).createPaymentEvent(
                anyLong(), anyLong(), anyLong(), any(LocalDate.class),
                any(BigDecimal.class), anyString(), anyLong(), anyString());
        verify(bankReceiptRepository, never()).save(any(BankReceipt.class));
    }


}