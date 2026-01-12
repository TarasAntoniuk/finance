package com.tarasantoniuk.finance.banking.bankpayment.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountSnapshotValidityService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.mapper.BankPaymentMapper;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.common.exeption.InsufficientBalanceException;
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
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankPaymentService Unit Tests")
class BankPaymentServiceTest {

    @Mock
    private BankPaymentRepository bankPaymentRepository;

    @Mock
    private BankPaymentMapper bankPaymentMapper;

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

    @Mock
    private BankAccountBalanceService balanceService;

    @Mock
    private BankAccountTransactionEventRepository transactionEventRepository;

    @Mock
    private BankAccountSnapshotValidityService snapshotValidityService;

    @InjectMocks
    private BankPaymentService bankPaymentService;

    private BankPaymentRequestDto requestDto;
    private BankPayment payment;
    private BankPaymentResponseDto responseDto;
    private BankAccount account;
    private BankAccount counterpartyAccount;
    private Counterparty counterparty;
    private Currency currency;
    private Organization organization;

    @BeforeEach
    void setUp() {
        // Request DTO
        requestDto = new BankPaymentRequestDto();
        requestDto.setAccountId(1L);
        requestDto.setCounterpartyId(2L);
        requestDto.setCounterpartyBankAccountId(3L);
        requestDto.setCurrencyId(4L);
        requestDto.setOrganizationId(5L);
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        requestDto.setAmount(BigDecimal.valueOf(1000.00));
        requestDto.setDescription("Test payment");
        requestDto.setExternalTransactionId("EXT-12345");

        // Entities
        account = new BankAccount();
        account.setId(1L);

        counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(3L);

        counterparty = new Counterparty();
        counterparty.setId(2L);

        currency = new Currency();
        currency.setId(4L);

        organization = new Organization();
        organization.setId(5L);

        payment = new BankPayment();
        payment.setId(10L);
        payment.setAccount(account);
        payment.setCounterparty(counterparty);
        payment.setCounterpartyBankAccount(counterpartyAccount);
        payment.setCurrency(currency);
        payment.setOrganization(organization);
        payment.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        payment.setAmount(BigDecimal.valueOf(1000.00));
        payment.setDescription("Test payment");
        payment.setStatus(DocumentStatus.DRAFT);
        payment.setExternalTransactionId("EXT-12345");

        // Response DTO
        responseDto = new BankPaymentResponseDto();
        responseDto.setId(10L);
        responseDto.setAmount(BigDecimal.valueOf(1000.00));
        responseDto.setStatus(DocumentStatus.DRAFT);
    }

    @Nested
    @DisplayName("Create Payment Tests")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create payment")
        void shouldCreatePayment() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo(DocumentStatus.DRAFT);

            verify(bankPaymentRepository).existsByExternalTransactionId("EXT-12345");
            verify(bankPaymentMapper).toEntity(requestDto);
            verify(bankPaymentRepository).save(any(BankPayment.class));
            verify(bankPaymentMapper).toResponseDto(payment);
        }

        @Test
        @DisplayName("Should create payment without counterparty bank account")
        void shouldCreatePaymentWithoutCounterpartyBankAccount() {
            // Arrange
            requestDto.setCounterpartyBankAccountId(null);

            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankAccountRepository, times(1)).findById(anyLong()); // Only once for main account
        }

        @Test
        @DisplayName("Should create payment without external transaction ID")
        void shouldCreatePaymentWithoutExternalTransactionId() {
            // Arrange
            requestDto.setExternalTransactionId(null);
            payment.setExternalTransactionId(null);

            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentRepository, never()).existsByExternalTransactionId(anyString());
        }

        @Test
        @DisplayName("Should throw exception when duplicate external transaction ID")
        void shouldThrowExceptionWhenDuplicateExternalTransactionId() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId("EXT-12345")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("external transaction ID already exists");

            verify(bankPaymentMapper, never()).toEntity(any());
            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when account not found")
        void shouldThrowExceptionWhenAccountNotFound() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank account not found");

            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when counterparty not found")
        void shouldThrowExceptionWhenCounterpartyNotFound() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Counterparty not found");

            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when counterparty bank account not found")
        void shouldThrowExceptionWhenCounterpartyBankAccountNotFound() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Counterparty bank account not found");

            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when currency not found")
        void shouldThrowExceptionWhenCurrencyNotFound() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Currency not found");

            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when organization not found")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // Arrange
            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.create(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Organization not found");

            verify(bankPaymentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Payment Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should successfully update payment in DRAFT status")
        void shouldUpdatePaymentInDraftStatus() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.update(10L, requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentMapper).updateEntityFromDto(requestDto, payment);
            verify(bankPaymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.update(10L, requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank payment not found");

            verify(bankPaymentMapper, never()).updateEntityFromDto(any(), any());
            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating POSTED payment")
        void shouldThrowExceptionWhenUpdatingPostedPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.POSTED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.update(10L, requestDto))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot update payment in status");

            verify(bankPaymentMapper, never()).updateEntityFromDto(any(), any());
            verify(bankPaymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating CANCELLED payment")
        void shouldThrowExceptionWhenUpdatingCancelledPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.CANCELLED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.update(10L, requestDto))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot update payment in status");

            verify(bankPaymentMapper, never()).updateEntityFromDto(any(), any());
            verify(bankPaymentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Payment Tests")
    class FindTests {

        @Test
        @DisplayName("Should find payment by ID")
        void shouldFindPaymentById() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.findById(10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(bankPaymentRepository).findByIdWithDetails(10L);
        }

        @Test
        @DisplayName("Should throw exception when payment not found by ID")
        void shouldThrowExceptionWhenPaymentNotFoundById() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.findById(10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank payment not found");
        }

        @Test
        @DisplayName("Should find all payments with pagination")
        void shouldFindAllPaymentsWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findAllWithDetails(pageable)).thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getMetadata().getTotalElements()).isEqualTo(1);
            assertThat(result.getMetadata().getTotalPages()).isEqualTo(1);
            verify(bankPaymentRepository).findAllWithDetails(pageable);
        }

        @Test
        @DisplayName("Should find payments by account ID")
        void shouldFindPaymentsByAccountId() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findByAccountId(1L, pageable)).thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findByAccountId(1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankPaymentRepository).findByAccountId(1L, pageable);
        }

        @Test
        @DisplayName("Should find payments by counterparty ID")
        void shouldFindPaymentsByCounterpartyId() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findByCounterpartyId(2L, pageable)).thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findByCounterpartyId(2L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankPaymentRepository).findByCounterpartyId(2L, pageable);
        }

        @Test
        @DisplayName("Should find payments by status")
        void shouldFindPaymentsByStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findByStatus(DocumentStatus.DRAFT, pageable)).thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findByStatus(DocumentStatus.DRAFT, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankPaymentRepository).findByStatus(DocumentStatus.DRAFT, pageable);
        }

        @Test
        @DisplayName("Should find payments by date range")
        void shouldFindPaymentsByDateRange() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findByTransactionDateTimeBetween(startDateTime, endDateTime, pageable))
                    .thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findByDateTimeRange(
                    startDateTime, endDateTime, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankPaymentRepository).findByTransactionDateTimeBetween(startDateTime, endDateTime, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no payments found")
        void shouldReturnEmptyPageWhenNoPaymentsFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<BankPayment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(bankPaymentRepository.findAllWithDetails(pageable)).thenReturn(emptyPage);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getMetadata().getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Delete Payment Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should successfully delete payment in DRAFT status")
        void shouldDeletePaymentInDraftStatus() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act
            bankPaymentService.delete(10L);

            // Assert
            verify(bankPaymentRepository).delete(payment);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent payment")
        void shouldThrowExceptionWhenDeletingNonExistentPayment() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.delete(10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank payment not found");

            verify(bankPaymentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting POSTED payment")
        void shouldThrowExceptionWhenDeletingPostedPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.POSTED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.delete(10L))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot delete payment in status");

            verify(bankPaymentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting CANCELLED payment")
        void shouldThrowExceptionWhenDeletingCancelledPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.CANCELLED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.delete(10L))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot delete payment in status");

            verify(bankPaymentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Post Payment Tests")
    class PostTests {

        @Test
        @DisplayName("Should successfully post payment")
        void shouldPostPayment() {
            // Arrange
            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()
            )).thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.post(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).postDocument(
                    eq(1L), eq(5L), eq(4L), any(LocalDateTime.class),
                    eq(BigDecimal.valueOf(1000.00)), eq("Test payment"),
                    eq("BankPayment"), eq(10L)
            );
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }

        @Test
        @DisplayName("Should throw exception when posting non-existent payment")
        void shouldThrowExceptionWhenPostingNonExistentPayment() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank payment not found");

            verify(transactionService, never()).postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            );
        }

        @Test
        @DisplayName("Should throw exception when posting already POSTED payment")
        void shouldThrowExceptionWhenPostingAlreadyPostedPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.POSTED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot post payment in status");

            verify(transactionService, never()).postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            );
        }

        @Test
        @DisplayName("Should throw exception when transaction event already exists")
        void shouldThrowExceptionWhenTransactionEventAlreadyExists() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()))
                    .thenThrow(new ResourceAlreadyExistsException("Transaction event already exists"));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Transaction event already exists");
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(500.00));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance");

            verify(transactionService, never()).postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            );
        }

        @Test
        @DisplayName("Should allow posting when balance equals payment amount")
        void shouldAllowPostingWhenBalanceEqualsPaymentAmount() {
            // Arrange
            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(1000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            )).thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.post(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            );
        }

        @Test
        @DisplayName("Should throw exception when transaction event not found after posting payment")
        void shouldThrowExceptionWhenTransactionEventNotFoundAfterPosting() {
            // Arrange
            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            // Set payment as backdated (older than 1 hour) to trigger snapshot invalidation logic
            payment.setTransactionDateTime(LocalDateTime.now().minusHours(2));

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()
            )).thenReturn(mockEvent);
            // Transaction event not found - simulates data inconsistency
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transaction event not found after posting payment " + 10L);

            verify(transactionService).postDocument(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyString(), anyLong()
            );
            verify(transactionEventRepository).findActiveByDocumentTypeAndDocumentId("BankPayment", 10L);
        }
    }

    @Nested
    @DisplayName("Unpost Payment Tests")
    class UnpostTests {

        @Test
        @DisplayName("Should successfully unpost payment")
        void shouldUnpostPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.POSTED);

            BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
            reversalEvent.setId(101L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(transactionService.reverseDocument(
                    anyString(), anyLong(), anyString()
            )).thenReturn(reversalEvent);
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.unpost(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).reverseDocument(
                    eq("BankPayment"),
                    eq(10L),
                    eq("Test payment")
            );
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }


        @Test
        @DisplayName("Should throw exception when unposting non-existent payment")
        void shouldThrowExceptionWhenUnpostingNonExistentPayment() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.unpost(10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank payment not found");

            verify(transactionService, never()).reverseDocument(anyString(), anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when unposting DRAFT payment")
        void shouldThrowExceptionWhenUnpostingDraftPayment() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.unpost(10L))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot unpost payment in status");

            verify(transactionService, never()).reverseDocument(anyString(), anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when unposting CANCELLED payment")
        void shouldThrowExceptionWhenUnpostingCancelledPayment() {
            // Arrange
            payment.setStatus(DocumentStatus.CANCELLED);
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.unpost(10L))
                    .isInstanceOf(InvalidDocumentStatusException.class)
                    .hasMessageContaining("Cannot unpost payment in status");

            verify(transactionService, never()).reverseDocument(anyString(), anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle payment with zero amount")
        void shouldHandlePaymentWithZeroAmount() {
            // Arrange
            requestDto.setAmount(BigDecimal.ZERO);
            payment.setAmount(BigDecimal.ZERO);

            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }

        @Test
        @DisplayName("Should handle payment with very large amount")
        void shouldHandlePaymentWithVeryLargeAmount() {
            // Arrange
            BigDecimal largeAmount = new BigDecimal("999999999999.99");
            requestDto.setAmount(largeAmount);
            payment.setAmount(largeAmount);

            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }

        @Test
        @DisplayName("Should handle payment with future date")
        void shouldHandlePaymentWithFutureDateTime() {
            // Arrange
            LocalDateTime futureDateTime = LocalDateTime.now().plusDays(30);
            requestDto.setTransactionDateTime(futureDateTime);
            payment.setTransactionDateTime(futureDateTime);

            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }

        @Test
        @DisplayName("Should handle payment with empty description")
        void shouldHandlePaymentWithEmptyDescription() {
            // Arrange
            requestDto.setDescription("");
            payment.setDescription("");

            when(bankPaymentRepository.existsByExternalTransactionId(anyString())).thenReturn(false);
            when(bankPaymentMapper.toEntity(requestDto)).thenReturn(payment);
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(counterpartyRepository.findById(2L)).thenReturn(Optional.of(counterparty));
            when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(counterpartyAccount));
            when(currencyRepository.findById(4L)).thenReturn(Optional.of(currency));
            when(organizationRepository.findById(5L)).thenReturn(Optional.of(organization));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.create(requestDto);

            // Assert
            assertThat(result).isNotNull();
            verify(bankPaymentRepository).save(any(BankPayment.class));
        }

        @Test
        @DisplayName("Should handle multiple pages of results")
        void shouldHandleMultiplePagesOfResults() {
            // Arrange
            Pageable pageable = PageRequest.of(1, 10);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 25);

            when(bankPaymentRepository.findAllWithDetails(pageable)).thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMetadata().getCurrentPage()).isEqualTo(1);
            assertThat(result.getMetadata().getTotalPages()).isEqualTo(3);
            assertThat(result.getMetadata().getTotalElements()).isEqualTo(25);
            assertThat(result.getMetadata().isHasNext()).isTrue();
            assertThat(result.getMetadata().isHasPrevious()).isTrue();
        }
    }

    @Nested
    @DisplayName("Snapshot Invalidation Tests")
    class SnapshotInvalidationTests {

        @Test
        @DisplayName("Should invalidate snapshots when posting backdated payment (> 1 hour old)")
        void shouldInvalidateSnapshotsWhenPostingBackdatedPayment() {
            // Arrange - payment with transaction date older than 1 hour
            LocalDateTime backdatedDateTime = LocalDateTime.now().minusHours(2);
            payment.setTransactionDateTime(backdatedDateTime);
            payment.setStatus(DocumentStatus.DRAFT);

            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()))
                    .thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            bankPaymentService.post(10L);

            // Assert - verify invalidation was called
            verify(snapshotValidityService).invalidateSnapshots(
                    anyLong(), any(LocalDateTime.class), anyLong()
            );
        }

        @Test
        @DisplayName("Should not invalidate snapshots when posting current payment (< 1 hour old)")
        void shouldNotInvalidateSnapshotsWhenPostingCurrentPayment() {
            // Arrange - payment with transaction date less than 1 hour old
            LocalDateTime currentDateTime = LocalDateTime.now().minusMinutes(30);
            payment.setTransactionDateTime(currentDateTime);
            payment.setStatus(DocumentStatus.DRAFT);

            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()))
                    .thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            bankPaymentService.post(10L);

            // Assert - verify invalidation was NOT called
            verify(snapshotValidityService, never()).invalidateSnapshots(
                    anyLong(), any(LocalDateTime.class), anyLong()
            );
        }

        @Test
        @DisplayName("Should use correct parameters when invalidating snapshots")
        void shouldUseCorrectParametersForInvalidation() {
            // Arrange - backdated payment
            LocalDateTime backdatedDateTime = LocalDateTime.of(2024, 1, 10, 14, 30, 0);
            payment.setTransactionDateTime(backdatedDateTime);
            payment.setStatus(DocumentStatus.DRAFT);

            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()))
                    .thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            bankPaymentService.post(10L);

            // Assert - verify correct parameters
            ArgumentCaptor<Long> accountIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<LocalDateTime> dateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            ArgumentCaptor<Long> eventIdCaptor = ArgumentCaptor.forClass(Long.class);

            verify(snapshotValidityService).invalidateSnapshots(
                    accountIdCaptor.capture(),
                    dateTimeCaptor.capture(),
                    eventIdCaptor.capture()
            );

            // Verify account ID
            assertThat(accountIdCaptor.getValue())
                    .isEqualTo(payment.getAccount().getId())
                    .withFailMessage("Should use payment's account ID");

            // Verify invalidation date (start of day after transaction date)
            LocalDateTime expectedInvalidFromDateTime = backdatedDateTime.toLocalDate()
                    .plusDays(1)
                    .atStartOfDay();
            assertThat(dateTimeCaptor.getValue())
                    .isEqualTo(expectedInvalidFromDateTime)
                    .withFailMessage("Should invalidate from start of day after transaction date");

            // Verify event ID
            assertThat(eventIdCaptor.getValue())
                    .isEqualTo(mockEvent.getId())
                    .withFailMessage("Should use transaction event ID");
        }

        @Test
        @DisplayName("Should log invalidation for backdated payment")
        void shouldLogInvalidationForBackdatedPayment() {
            // Arrange - backdated payment
            LocalDateTime backdatedDateTime = LocalDateTime.now().minusHours(3);
            payment.setTransactionDateTime(backdatedDateTime);
            payment.setStatus(DocumentStatus.DRAFT);

            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(balanceService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.postDocument(
                    anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                    any(BigDecimal.class), anyString(), anyString(), anyLong()))
                    .thenReturn(mockEvent);
            when(transactionEventRepository.findActiveByDocumentTypeAndDocumentId("BankPayment", 10L))
                    .thenReturn(Optional.of(mockEvent));
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.post(10L);

            // Assert - verify method completed successfully (logging happens inside)
            assertThat(result).isNotNull();
            verify(snapshotValidityService).invalidateSnapshots(
                    anyLong(), any(LocalDateTime.class), anyLong()
            );
            // Note: Logging verification would require LogCaptor or similar, but we verify
            // the invalidation was called which triggers the log statement
        }
    }
}