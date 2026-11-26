package com.tarasantoniuk.finance.banking.bankpayment.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.mapper.BankPaymentMapper;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.common.exception.InsufficientBalanceException;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BankPaymentService {


    private final BankPaymentRepository bankPaymentRepository;
    private final BankPaymentMapper bankPaymentMapper;
    private final BankAccountRepository bankAccountRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final CurrencyRepository currencyRepository;
    private final OrganizationRepository organizationRepository;
    private final BankAccountTransactionService transactionService;
    private final BankAccountBalanceService balanceService;
    private final OrganizationSecurityContext orgContext;

    public BankPaymentService(
            BankPaymentRepository bankPaymentRepository,
            BankPaymentMapper bankPaymentMapper,
            BankAccountRepository bankAccountRepository,
            CounterpartyRepository counterpartyRepository,
            CurrencyRepository currencyRepository,
            OrganizationRepository organizationRepository,
            BankAccountTransactionService transactionService,
            BankAccountBalanceService balanceService,
            OrganizationSecurityContext orgContext) {
        this.bankPaymentRepository = bankPaymentRepository;
        this.bankPaymentMapper = bankPaymentMapper;
        this.bankAccountRepository = bankAccountRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.currencyRepository = currencyRepository;
        this.organizationRepository = organizationRepository;
        this.transactionService = transactionService;
        this.balanceService = balanceService;
        this.orgContext = orgContext;
    }

    private Long queryOrgScope() {
        return orgContext.isAdmin() ? null : orgContext.getActiveOrganizationId();
    }

    /**
     * Create new bank payment in DRAFT status
     */
    public BankPaymentResponseDto create(BankPaymentRequestDto requestDto) {
        requestDto.setOrganizationId(orgContext.resolveOrganizationId(requestDto.getOrganizationId()));

        // Check for duplicate external transaction ID
        if (requestDto.getExternalTransactionId() != null &&
                bankPaymentRepository.existsByExternalTransactionId(requestDto.getExternalTransactionId())) {
            throw new ResourceAlreadyExistsException(
                    "Bank payment with external transaction ID already exists: " +
                            requestDto.getExternalTransactionId()
            );
        }

        // Map DTO to entity
        BankPayment payment = bankPaymentMapper.toEntity(requestDto);

        // Load related entities
        loadRelatedEntities(payment, requestDto);

        // Set default status
        payment.setStatus(DocumentStatus.DRAFT);

        // Save
        BankPayment savedPayment = bankPaymentRepository.save(payment);

        // Map to response DTO
        return bankPaymentMapper.toResponseDto(savedPayment);
    }

    /**
     * Update existing bank payment (only if in DRAFT status)
     */
    public BankPaymentResponseDto update(Long id, BankPaymentRequestDto requestDto) {
        BankPayment existing = findEntityByIdOrThrow(id);
        requestDto.setOrganizationId(orgContext.resolveOrganizationId(requestDto.getOrganizationId()));

        // Only DRAFT documents can be modified
        if (!existing.isMutable()) {
            throw new InvalidDocumentStatusException(
                    "Cannot update payment in status " + existing.getStatus()
            );
        }

        // Update entity from DTO
        bankPaymentMapper.updateEntityFromDto(requestDto, existing);

        // Reload related entities
        loadRelatedEntities(existing, requestDto);

        // Save
        BankPayment updatedPayment = bankPaymentRepository.save(existing);

        // Map to response DTO
        return bankPaymentMapper.toResponseDto(updatedPayment);
    }

    /**
     * Find payment by ID
     */
    @Transactional(readOnly = true)
    public BankPaymentResponseDto findById(Long id) {
        BankPayment payment = findEntityByIdOrThrow(id);
        return bankPaymentMapper.toResponseDto(payment);
    }

    /**
     * Find all payments with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<BankPaymentResponseDto> findAll(Pageable pageable) {
        Page<BankPayment> paymentsPage = bankPaymentRepository.findAllWithDetails(queryOrgScope(), pageable);
        List<BankPaymentResponseDto> dtoList = paymentsPage.map(bankPaymentMapper::toResponseDto).getContent();
        return buildPageResponse(paymentsPage, dtoList);
    }

    /**
     * Find payments by account
     */
    @Transactional(readOnly = true)
    public PageResponse<BankPaymentResponseDto> findByAccountId(Long accountId, Pageable pageable) {
        Page<BankPayment> paymentsPage = bankPaymentRepository.findByAccountId(accountId, queryOrgScope(), pageable);
        List<BankPaymentResponseDto> dtoList = paymentsPage.map(bankPaymentMapper::toResponseDto).getContent();
        return buildPageResponse(paymentsPage, dtoList);
    }

    /**
     * Find payments by counterparty
     */
    @Transactional(readOnly = true)
    public PageResponse<BankPaymentResponseDto> findByCounterpartyId(Long counterpartyId, Pageable pageable) {
        Page<BankPayment> paymentsPage = bankPaymentRepository.findByCounterpartyId(counterpartyId, queryOrgScope(), pageable);
        List<BankPaymentResponseDto> dtoList = paymentsPage.map(bankPaymentMapper::toResponseDto).getContent();
        return buildPageResponse(paymentsPage, dtoList);
    }

    /**
     * Find payments by status
     */
    @Transactional(readOnly = true)
    public PageResponse<BankPaymentResponseDto> findByStatus(DocumentStatus status, Pageable pageable) {
        Page<BankPayment> paymentsPage = bankPaymentRepository.findByStatus(status, queryOrgScope(), pageable);
        List<BankPaymentResponseDto> dtoList = paymentsPage.map(bankPaymentMapper::toResponseDto).getContent();
        return buildPageResponse(paymentsPage, dtoList);
    }

    /**
     * Find payments by datetime range
     */
    @Transactional(readOnly = true)
    public PageResponse<BankPaymentResponseDto> findByDateTimeRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable) {
        Page<BankPayment> paymentsPage = bankPaymentRepository.findByTransactionDateTimeBetween(
                startDateTime, endDateTime, queryOrgScope(), pageable);
        List<BankPaymentResponseDto> dtoList = paymentsPage.map(bankPaymentMapper::toResponseDto).getContent();
        return buildPageResponse(paymentsPage, dtoList);
    }

    /**
     * Delete payment (only if in DRAFT status)
     */
    public void delete(Long id) {
        BankPayment payment = findEntityByIdOrThrow(id);

        // Only DRAFT documents can be deleted
        if (!payment.isMutable()) {
            throw new InvalidDocumentStatusException(
                    "Cannot delete payment in status " + payment.getStatus()
            );
        }

        bankPaymentRepository.delete(payment);
    }

    /**
     * Post bank payment - create transaction event in Event Sourcing
     */
    public BankPaymentResponseDto post(Long id) {
        BankPayment payment = findEntityForStatusChange(id);

        // Only DRAFT documents can be posted
        if (payment.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidDocumentStatusException(
                    "Cannot post payment in status " + payment.getStatus()
            );
        }

        // Lock the account so concurrent payments cannot both pass the balance check
        bankAccountRepository.findByIdForUpdate(payment.getAccount().getId());
        BigDecimal currentBalance = balanceService.getCurrentBalance(payment.getAccount().getId());
        if (currentBalance.compareTo(payment.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Current balance: " + currentBalance +
                            ", required: " + payment.getAmount()
            );
        }

        // Use unified method to post document
        transactionService.postDocument(
                payment.getAccount().getId(),
                payment.getOrganization().getId(),
                payment.getCurrency().getId(),
                payment.getTransactionDateTime(),
                payment.getAmount(),
                payment.getDescription(),
                "BankPayment",
                payment.getId()
        );

        // Update document status
        payment.setStatus(DocumentStatus.POSTED);
        BankPayment postedPayment = bankPaymentRepository.save(payment);

        return bankPaymentMapper.toResponseDto(postedPayment);
    }

    /**
     * Unpost bank payment - reverse transaction event
     */
    public BankPaymentResponseDto unpost(Long id) {
        BankPayment payment = findEntityForStatusChange(id);

        // Only POSTED documents can be unposted
        if (payment.getStatus() != DocumentStatus.POSTED) {
            throw new InvalidDocumentStatusException(
                    "Cannot unpost payment in status " + payment.getStatus()
            );
        }

        // Use unified method to reverse document (this fixes the "Reversal of: null" bug)
        transactionService.reverseDocument(
                "BankPayment",
                payment.getId(),
                payment.getDescription()
        );

        // Update document status
        payment.setStatus(DocumentStatus.DRAFT);
        BankPayment unpostedPayment = bankPaymentRepository.save(payment);

        return bankPaymentMapper.toResponseDto(unpostedPayment);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Locks the payment row before reading it, so concurrent post/unpost requests for the same
     * document run one after another and the second one sees the status set by the first.
     */
    private BankPayment findEntityForStatusChange(Long id) {
        bankPaymentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank payment not found with id: " + id
                ));
        return findEntityByIdOrThrow(id);
    }

    private BankPayment findEntityByIdOrThrow(Long id) {
        BankPayment payment = bankPaymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank payment not found with id: " + id
                ));
        orgContext.validateAccess(payment.getOrganization().getId());
        return payment;
    }

    private void loadRelatedEntities(BankPayment payment, BankPaymentRequestDto requestDto) {
        // Load account
        BankAccount account = bankAccountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + requestDto.getAccountId()
                ));
        payment.setAccount(account);

        // Load counterparty
        Counterparty counterparty = counterpartyRepository.findById(requestDto.getCounterpartyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Counterparty not found with id: " + requestDto.getCounterpartyId()
                ));
        payment.setCounterparty(counterparty);

        // Load counterparty bank account (optional)
        if (requestDto.getCounterpartyBankAccountId() != null) {
            BankAccount counterpartyAccount = bankAccountRepository
                    .findById(requestDto.getCounterpartyBankAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Counterparty bank account not found with id: " +
                                    requestDto.getCounterpartyBankAccountId()
                    ));
            payment.setCounterpartyBankAccount(counterpartyAccount);
        } else {
            payment.setCounterpartyBankAccount(null);
        }

        // Load currency
        Currency currency = currencyRepository.findById(requestDto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + requestDto.getCurrencyId()
                ));
        payment.setCurrency(currency);

        // Load organization
        Organization organization = organizationRepository.findById(requestDto.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with id: " + requestDto.getOrganizationId()
                ));
        payment.setOrganization(organization);
    }

    private <T> PageResponse<T> buildPageResponse(Page<?> page, List<T> content) {
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        return PageResponse.<T>builder()
                .content(content)
                .metadata(metadata)
                .build();
    }
}