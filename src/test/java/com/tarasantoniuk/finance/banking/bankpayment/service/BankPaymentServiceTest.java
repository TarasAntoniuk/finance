package com.tarasantoniuk.finance.banking.bankpayment.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
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
        payment.setDocumentDate(LocalDate.of(2024, 1, 15));
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
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<BankPayment> payments = List.of(payment);
            Page<BankPayment> paymentsPage = new PageImpl<>(payments, pageable, 1);

            when(bankPaymentRepository.findByDocumentDateBetween(startDate, endDate, pageable))
                    .thenReturn(paymentsPage);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            PageResponse<BankPaymentResponseDto> result = bankPaymentService.findByDateRange(
                    startDate, endDate, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bankPaymentRepository).findByDocumentDateBetween(startDate, endDate, pageable);
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
            when(transactionService.existsByDocument("BankPayment", 10L)).thenReturn(false);
            when(transactionService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(5000.00));
            when(transactionService.createPaymentEvent(
                    eq(1L), eq(5L), eq(4L), any(LocalDate.class),
                    eq(BigDecimal.valueOf(1000.00)), eq("BankPayment"),
                    eq(10L), anyString()
            )).thenReturn(mockEvent);
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.post(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).createPaymentEvent(
                    eq(1L), eq(5L), eq(4L), any(LocalDate.class),
                    eq(BigDecimal.valueOf(1000.00)), eq("BankPayment"),
                    eq(10L), anyString()
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

            verify(transactionService, never()).createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
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

            verify(transactionService, never()).createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
            );
        }

        @Test
        @DisplayName("Should throw exception when transaction event already exists")
        void shouldThrowExceptionWhenTransactionEventAlreadyExists() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(transactionService.existsByDocument("BankPayment", 10L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Transaction event already exists");

            verify(transactionService, never()).createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
            );
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            // Arrange
            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(transactionService.existsByDocument("BankPayment", 10L)).thenReturn(false);
            when(transactionService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(500.00));

            // Act & Assert
            assertThatThrownBy(() -> bankPaymentService.post(10L))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance");

            verify(transactionService, never()).createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
            );
        }

        @Test
        @DisplayName("Should allow posting when balance equals payment amount")
        void shouldAllowPostingWhenBalanceEqualsPaymentAmount() {
            // Arrange
            BankAccountTransactionEvent mockEvent = new BankAccountTransactionEvent();
            mockEvent.setId(100L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(transactionService.existsByDocument("BankPayment", 10L)).thenReturn(false);
            when(transactionService.getCurrentBalance(1L)).thenReturn(BigDecimal.valueOf(1000.00));
            when(transactionService.createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
            )).thenReturn(mockEvent);
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.post(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).createPaymentEvent(
                    anyLong(), anyLong(), anyLong(), any(), any(), anyString(), anyLong(), anyString()
            );
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

            BankAccountTransactionEvent originalEvent = new BankAccountTransactionEvent();
            originalEvent.setId(100L);

            BankAccountTransactionEvent reversalEvent = new BankAccountTransactionEvent();
            reversalEvent.setId(101L);

            when(bankPaymentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(payment));
            when(transactionService.findByDocument("BankPayment", 10L)).thenReturn(originalEvent);
            when(transactionService.createReceiptEvent(
                    eq(1L), eq(5L), eq(4L), any(LocalDate.class),
                    eq(BigDecimal.valueOf(1000.00)), eq("BankPaymentReversal"),
                    eq(10L), anyString()
            )).thenReturn(reversalEvent);
            when(bankPaymentRepository.save(any(BankPayment.class))).thenReturn(payment);
            when(bankPaymentMapper.toResponseDto(payment)).thenReturn(responseDto);

            // Act
            BankPaymentResponseDto result = bankPaymentService.unpost(10L);

            // Assert
            assertThat(result).isNotNull();
            verify(transactionService).findByDocument("BankPayment", 10L);
            verify(transactionService).createReceiptEvent(
                    eq(1L), eq(5L), eq(4L), any(LocalDate.class),
                    eq(BigDecimal.valueOf(1000.00)), eq("BankPaymentReversal"),
                    eq(10L), anyString()
            );
            verify(transactionService).reverseTransaction(100L, 101L);
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

            verify(transactionService, never()).findByDocument(anyString(), anyLong());
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

            verify(transactionService, never()).findByDocument(anyString(), anyLong());
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

            verify(transactionService, never()).findByDocument(anyString(), anyLong());
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
        void shouldHandlePaymentWithFutureDate() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(30);
            requestDto.setDocumentDate(futureDate);
            payment.setDocumentDate(futureDate);

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
}