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
import java.time.LocalDateTime;
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

    @Mock
    private BankAccountTransactionService transactionService;

    @InjectMocks
    private BankReceiptService bankReceiptService;

    private BankReceiptRequestDto requestDto;
    private BankReceipt receipt;
    private BankReceiptResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Create request DTO
        requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 0, 0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("10000.00"));
        requestDto.setAccountId(1L);
        requestDto.setCounterpartyId(1L);
        requestDto.setCurrencyId(1L);
        requestDto.setOrganizationId(1L);

        // Create test receipt entity
        receipt = createTestBankReceipt();
        receipt.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        receipt.setStatus(DocumentStatus.DRAFT);

        // Create response DTO
        responseDto = createBankReceiptResponseDto();
    }

    // ==================== Helper Methods ====================

    /**
     * Creates test BankReceipt entity for unit tests (not persisted)
     */
    private BankReceipt createTestBankReceipt() {
        BankReceipt receipt = new BankReceipt();
        receipt.setId(1L);
        receipt.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 0, 0));
        receipt.setAmount(new BigDecimal("1000.00"));
        receipt.setDescription("Test receipt");

        BankAccount account = new BankAccount();
        account.setId(1L);
        receipt.setAccount(account);

        Organization organization = new Organization();
        organization.setId(1L);
        receipt.setOrganization(organization);

        Currency currency = new Currency();
        currency.setId(1L);
        receipt.setCurrency(currency);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(1L);
        receipt.setCounterparty(counterparty);

        return receipt;
    }

    /**
     * Creates test BankReceiptResponseDto for unit tests
     */
    private BankReceiptResponseDto createBankReceiptResponseDto() {
        BankReceiptResponseDto dto = new BankReceiptResponseDto();
        dto.setId(1L);
        dto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 0, 0));
        dto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setDescription("Test bank receipt");
        dto.setStatus(DocumentStatus.DRAFT);
        return dto;
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should successfully create bank receipt")
    void shouldCreateBankReceipt() {
        // Given
        BankAccount account = new BankAccount();
        account.setId(1L);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(1L);

        Currency currency = new Currency();
        currency.setId(1L);

        Organization organization = new Organization();
        organization.setId(1L);

        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(bankReceiptRepository.save(any(BankReceipt.class))).thenReturn(receipt);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.create(requestDto);

        // Then
        assertNotNull(result);
        verify(bankReceiptRepository).save(any(BankReceipt.class));
        verify(bankReceiptMapper).toResponseDto(receipt);
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.create(requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when duplicate external transaction ID")
    void shouldThrowExceptionWhenDuplicateExternalTransactionId() {
        // Given
        requestDto.setExternalTransactionId("EXT-123");
        when(bankReceiptRepository.existsByExternalTransactionId("EXT-123")).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            bankReceiptService.create(requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should successfully update bank receipt in DRAFT status")
    void shouldUpdateBankReceiptInDraftStatus() {
        // Given
        BankAccount account = new BankAccount();
        account.setId(1L);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(1L);

        Currency currency = new Currency();
        currency.setId(1L);

        Organization organization = new Organization();
        organization.setId(1L);

        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(receipt));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(bankReceiptRepository.save(any(BankReceipt.class))).thenReturn(receipt);
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.update(1L, requestDto);

        // Then
        assertNotNull(result);
        verify(bankReceiptMapper).updateEntityFromDto(requestDto, receipt);
        verify(bankReceiptRepository).save(receipt);
    }

    @Test
    @DisplayName("Should throw exception when updating POSTED receipt")
    void shouldThrowExceptionWhenUpdatingPostedReceipt() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class, () -> {
            bankReceiptService.update(1L, requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }

    // ==================== FIND Tests ====================

    @Test
    @DisplayName("Should find bank receipt by ID")
    void shouldFindBankReceiptById() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(receipt));
        when(bankReceiptMapper.toResponseDto(receipt)).thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.findById(1L);

        // Then
        assertNotNull(result);
        verify(bankReceiptRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Should throw exception when receipt not found")
    void shouldThrowExceptionWhenReceiptNotFound() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.findById(1L);
        });
    }

    @Test
    @DisplayName("Should find all bank receipts with pagination")
    void shouldFindAllBankReceiptsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<BankReceipt> receipts = List.of(receipt);
        Page<BankReceipt> page = new PageImpl<>(receipts, pageable, 1);

        when(bankReceiptRepository.findAllWithDetails(pageable)).thenReturn(page);
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class))).thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result = bankReceiptService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getMetadata().getTotalElements());
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should successfully delete bank receipt in DRAFT status")
    void shouldDeleteBankReceiptInDraftStatus() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(receipt));

        // When
        bankReceiptService.delete(1L);

        // Then
        verify(bankReceiptRepository).delete(receipt);
    }

    @Test
    @DisplayName("Should throw exception when deleting POSTED receipt")
    void shouldThrowExceptionWhenDeletingPostedReceipt() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class, () -> bankReceiptService.delete(1L));

        verify(bankReceiptRepository, never()).delete(any());
    }

    // ==================== POST Tests ====================

    @Test
    @DisplayName("Should successfully post bank receipt")
    void shouldPostBankReceipt() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));

        BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
        mockEvent.setId(1L);
        when(transactionService.postDocument(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                any(BigDecimal.class), anyString(), anyString(), anyLong()))
                .thenReturn(mockEvent);

        when(bankReceiptRepository.save(any(BankReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.post(receipt.getId());

        // Then
        assertNotNull(result);
        verify(transactionService).postDocument(
                receipt.getAccount().getId(),
                receipt.getOrganization().getId(),
                receipt.getCurrency().getId(),
                receipt.getTransactionDateTime(),
                receipt.getAmount(),
                receipt.getDescription(),
                "BankReceipt",
                receipt.getId()
        );
        verify(bankReceiptRepository).save(any(BankReceipt.class));
    }

    @Test
    @DisplayName("Should throw exception when posting already POSTED receipt")
    void shouldThrowExceptionWhenPostingAlreadyPostedReceipt() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class, () -> {
            bankReceiptService.post(receipt.getId());
        });

        verify(transactionService, never()).postDocument(
                anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
        );
    }

    @Test
    @DisplayName("Should throw exception when transaction event already exists")
    void shouldThrowExceptionWhenTransactionEventAlreadyExists() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));
        when(transactionService.postDocument(
                anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()))
                .thenThrow(new ResourceAlreadyExistsException("Transaction event already exists"));

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            bankReceiptService.post(receipt.getId());
        });
    }

    // ==================== UNPOST Tests ====================

    @Test
    @DisplayName("Should successfully unpost bank receipt")
    void shouldUnpostBankReceipt() {
        // Given
        receipt.setStatus(DocumentStatus.POSTED);
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));

        BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
        reversalEvent.setId(2L);
        when(transactionService.reverseDocument(
                anyString(), anyLong(), anyString()))
                .thenReturn(reversalEvent);

        when(bankReceiptRepository.save(any(BankReceipt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        BankReceiptResponseDto result = bankReceiptService.unpost(receipt.getId());

        // Then
        assertNotNull(result);
        verify(transactionService).reverseDocument(
                "BankReceipt",
                receipt.getId(),
                receipt.getDescription()
        );
        verify(bankReceiptRepository).save(any(BankReceipt.class));
    }

    @Test
    @DisplayName("Should throw exception when unposting non-POSTED receipt")
    void shouldThrowExceptionWhenUnpostingNonPostedReceipt() {
        // Given
        receipt.setStatus(DocumentStatus.DRAFT);
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class, () -> {
            bankReceiptService.unpost(receipt.getId());
        });

        verify(transactionService, never()).reverseDocument(
                anyString(), anyLong(), anyString()
        );
    }

    @Test
    @DisplayName("Should throw exception when unposting DRAFT receipt")
    void shouldThrowExceptionWhenUnpostingDraftReceipt() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId())).thenReturn(Optional.of(receipt));

        // When & Then
        assertThrows(InvalidDocumentStatusException.class, () -> {
            bankReceiptService.unpost(receipt.getId());
        });

        verify(transactionService, never()).findByDocument(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when receipt not found for unpost")
    void shouldThrowExceptionWhenReceiptNotFoundForUnpost() {
        // Given
        when(bankReceiptRepository.findByIdWithDetails(receipt.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.unpost(receipt.getId());
        });
    }

    // ==================== FIND BY FILTERS Tests ====================

    @Test
    @DisplayName("Should find receipts by date range")
    void shouldFindReceiptsByDateRange() {
        // Given
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        Pageable pageable = PageRequest.of(0, 10);

        List<BankReceipt> receipts = List.of(receipt);
        Page<BankReceipt> page = new PageImpl<>(receipts, pageable, 1);

        when(bankReceiptRepository.findByTransactionDateTimeBetween(startDateTime, endDateTime, pageable))
                .thenReturn(page);
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result =
                bankReceiptService.findByDateTimeRange(startDateTime, endDateTime, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByTransactionDateTimeBetween(startDateTime, endDateTime, pageable);
    }

    @Test
    @DisplayName("Should find receipts by account ID")
    void shouldFindReceiptsByAccountId() {
        // Given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<BankReceipt> receipts = List.of(receipt);
        Page<BankReceipt> page = new PageImpl<>(receipts, pageable, 1);

        when(bankReceiptRepository.findByAccountId(accountId, pageable))
                .thenReturn(page);
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result =
                bankReceiptService.findByAccountId(accountId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByAccountId(accountId, pageable);
    }

    @Test
    @DisplayName("Should find receipts by counterparty ID")
    void shouldFindReceiptsByCounterpartyId() {
        // Given
        Long counterpartyId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<BankReceipt> receipts = List.of(receipt);
        Page<BankReceipt> page = new PageImpl<>(receipts, pageable, 1);

        when(bankReceiptRepository.findByCounterpartyId(counterpartyId, pageable))
                .thenReturn(page);
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result =
                bankReceiptService.findByCounterpartyId(counterpartyId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByCounterpartyId(counterpartyId, pageable);
    }

    @Test
    @DisplayName("Should find receipts by status")
    void shouldFindReceiptsByStatus() {
        // Given
        DocumentStatus status = DocumentStatus.DRAFT;
        Pageable pageable = PageRequest.of(0, 10);

        List<BankReceipt> receipts = List.of(receipt);
        Page<BankReceipt> page = new PageImpl<>(receipts, pageable, 1);

        when(bankReceiptRepository.findByStatus(status, pageable))
                .thenReturn(page);
        when(bankReceiptMapper.toResponseDto(any(BankReceipt.class)))
                .thenReturn(responseDto);

        // When
        PageResponse<BankReceiptResponseDto> result =
                bankReceiptService.findByStatus(status, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bankReceiptRepository).findByStatus(status, pageable);
    }

// ==================== LOAD RELATED ENTITIES Tests ====================

    @Test
    @DisplayName("Should throw exception when counterparty not found during create")
    void shouldThrowExceptionWhenCounterpartyNotFoundDuringCreate() {
        // Given
        BankAccount account = new BankAccount();
        account.setId(1L);

        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.create(requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when currency not found during create")
    void shouldThrowExceptionWhenCurrencyNotFoundDuringCreate() {
        // Given
        BankAccount account = new BankAccount();
        account.setId(1L);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(1L);

        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.create(requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when organization not found during create")
    void shouldThrowExceptionWhenOrganizationNotFoundDuringCreate() {
        // Given
        BankAccount account = new BankAccount();
        account.setId(1L);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(1L);

        Currency currency = new Currency();
        currency.setId(1L);

        when(bankReceiptMapper.toEntity(requestDto)).thenReturn(receipt);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(counterparty));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankReceiptService.create(requestDto);
        });

        verify(bankReceiptRepository, never()).save(any());
    }
}