package com.tarasantoniuk.finance.banking.bankreceipt.service;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.service.BankAccountTransactionService;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.mapper.BankReceiptMapper;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BankReceiptService {

    private final BankReceiptRepository bankReceiptRepository;
    private final BankReceiptMapper bankReceiptMapper;
    private final BankAccountRepository bankAccountRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final CurrencyRepository currencyRepository;
    private final OrganizationRepository organizationRepository;
    private final BankAccountTransactionService transactionService;

    public BankReceiptService(
            BankReceiptRepository bankReceiptRepository,
            BankReceiptMapper bankReceiptMapper,
            BankAccountRepository bankAccountRepository,
            CounterpartyRepository counterpartyRepository,
            CurrencyRepository currencyRepository,
            OrganizationRepository organizationRepository,
            BankAccountTransactionService transactionService) {
        this.bankReceiptRepository = bankReceiptRepository;
        this.bankReceiptMapper = bankReceiptMapper;
        this.bankAccountRepository = bankAccountRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.currencyRepository = currencyRepository;
        this.organizationRepository = organizationRepository;
        this.transactionService = transactionService;
    }

    /**
     * Create new bank receipt in DRAFT status
     */
    public BankReceiptResponseDto create(BankReceiptRequestDto requestDto) {
        // Check for duplicate external transaction ID
        if (requestDto.getExternalTransactionId() != null &&
                bankReceiptRepository.existsByExternalTransactionId(requestDto.getExternalTransactionId())) {
            throw new ResourceAlreadyExistsException(
                    "Bank receipt with external transaction ID already exists: " +
                            requestDto.getExternalTransactionId()
            );
        }

        // Map DTO to entity
        BankReceipt receipt = bankReceiptMapper.toEntity(requestDto);

        // Load related entities
        loadRelatedEntities(receipt, requestDto);

        // Set default status
        receipt.setStatus(DocumentStatus.DRAFT);

        // Save
        BankReceipt savedReceipt = bankReceiptRepository.save(receipt);

        // Map to response DTO
        return bankReceiptMapper.toResponseDto(savedReceipt);
    }

    /**
     * Update existing bank receipt (only if in DRAFT status)
     */
    public BankReceiptResponseDto update(Long id, BankReceiptRequestDto requestDto) {
        BankReceipt existing = findEntityByIdOrThrow(id);

        // Only DRAFT documents can be modified
        if (!existing.isMutable()) {
            throw new InvalidDocumentStatusException(
                    "Cannot update receipt in status " + existing.getStatus()
            );
        }

        // Update entity from DTO
        bankReceiptMapper.updateEntityFromDto(requestDto, existing);

        // Reload related entities
        loadRelatedEntities(existing, requestDto);

        // Save
        BankReceipt updatedReceipt = bankReceiptRepository.save(existing);

        // Map to response DTO
        return bankReceiptMapper.toResponseDto(updatedReceipt);
    }

    /**
     * Find receipt by ID
     */
    @Transactional(readOnly = true)
    public BankReceiptResponseDto findById(Long id) {
        BankReceipt receipt = findEntityByIdOrThrow(id);
        return bankReceiptMapper.toResponseDto(receipt);
    }

    /**
     * Find all receipts with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<BankReceiptResponseDto> findAll(Pageable pageable) {
        Page<BankReceipt> receiptsPage = bankReceiptRepository.findAllWithDetails(pageable);
        List<BankReceiptResponseDto> dtoList = receiptsPage.map(bankReceiptMapper::toResponseDto).getContent();
        return buildPageResponse(receiptsPage, dtoList);
    }

    /**
     * Find receipts by account
     */
    @Transactional(readOnly = true)
    public PageResponse<BankReceiptResponseDto> findByAccountId(Long accountId, Pageable pageable) {
        Page<BankReceipt> receiptsPage = bankReceiptRepository.findByAccountId(accountId, pageable);
        List<BankReceiptResponseDto> dtoList = receiptsPage.map(bankReceiptMapper::toResponseDto).getContent();
        return buildPageResponse(receiptsPage, dtoList);
    }

    /**
     * Find receipts by counterparty
     */
    @Transactional(readOnly = true)
    public PageResponse<BankReceiptResponseDto> findByCounterpartyId(Long counterpartyId, Pageable pageable) {
        Page<BankReceipt> receiptsPage = bankReceiptRepository.findByCounterpartyId(counterpartyId, pageable);
        List<BankReceiptResponseDto> dtoList = receiptsPage.map(bankReceiptMapper::toResponseDto).getContent();
        return buildPageResponse(receiptsPage, dtoList);
    }

    /**
     * Find receipts by status
     */
    @Transactional(readOnly = true)
    public PageResponse<BankReceiptResponseDto> findByStatus(DocumentStatus status, Pageable pageable) {
        Page<BankReceipt> receiptsPage = bankReceiptRepository.findByStatus(status, pageable);
        List<BankReceiptResponseDto> dtoList = receiptsPage.map(bankReceiptMapper::toResponseDto).getContent();
        return buildPageResponse(receiptsPage, dtoList);
    }

    /**
     * Find receipts by datetime range
     */
    @Transactional(readOnly = true)
    public PageResponse<BankReceiptResponseDto> findByDateTimeRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable) {
        Page<BankReceipt> receiptsPage = bankReceiptRepository.findByTransactionDateTimeBetween(
                startDateTime, endDateTime, pageable);
        List<BankReceiptResponseDto> dtoList = receiptsPage.map(bankReceiptMapper::toResponseDto).getContent();
        return buildPageResponse(receiptsPage, dtoList);
    }

    /**
     * Delete receipt (only if in DRAFT status)
     */
    public void delete(Long id) {
        BankReceipt receipt = findEntityByIdOrThrow(id);

        // Only DRAFT documents can be deleted
        if (!receipt.isMutable()) {
            throw new InvalidDocumentStatusException(
                    "Cannot delete receipt in status " + receipt.getStatus()
            );
        }

        bankReceiptRepository.delete(receipt);
    }

    /**
     * Post bank receipt - create transaction event in Event Sourcing
     */
    public BankReceiptResponseDto post(Long id) {
        BankReceipt receipt = findEntityByIdOrThrow(id);

        // Only DRAFT documents can be posted
        if (receipt.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidDocumentStatusException(
                    "Cannot post receipt in status " + receipt.getStatus()
            );
        }

        // Check if transaction event already exists
        if (transactionService.existsByDocument("BankReceipt", id)) {
            throw new ResourceAlreadyExistsException(
                    "Transaction event already exists for BankReceipt id: " + id
            );
        }

        // Create transaction event (DEBIT - money in)
        transactionService.createReceiptEvent(
                receipt.getAccount().getId(),
                receipt.getOrganization().getId(),
                receipt.getCurrency().getId(),
                receipt.getTransactionDateTime(),
                receipt.getAmount(),
                "BankReceipt",
                receipt.getId(),
                receipt.getDescription()
        );

        // Update document status
        receipt.setStatus(DocumentStatus.POSTED);
        BankReceipt postedReceipt = bankReceiptRepository.save(receipt);

        return bankReceiptMapper.toResponseDto(postedReceipt);
    }

    /**
     * Unpost bank receipt - reverse transaction event
     */
    public BankReceiptResponseDto unpost(Long id) {
        BankReceipt receipt = findEntityByIdOrThrow(id);

        // Only POSTED documents can be unposted
        if (receipt.getStatus() != DocumentStatus.POSTED) {
            throw new InvalidDocumentStatusException(
                    "Cannot unpost receipt in status " + receipt.getStatus()
            );
        }

        // Find the active (non-reversed) transaction event
        var originalEvent = transactionService.findActiveByDocument("BankReceipt", id);

        // Create reversal event (CREDIT - reverse the DEBIT)
        // Use safe description: if receipt description is null, use a generic message
        String reversalDescription = "Reversal of receipt #" + receipt.getId();
        if (receipt.getDescription() != null && !receipt.getDescription().isBlank()) {
            reversalDescription = "Reversal of: " + receipt.getDescription();
        }

        var reversalEvent = transactionService.createPaymentEvent(
                receipt.getAccount().getId(),
                receipt.getOrganization().getId(),
                receipt.getCurrency().getId(),
                receipt.getTransactionDateTime(),
                receipt.getAmount(),
                "BankReceiptReversal",
                receipt.getId(),
                reversalDescription
        );

        // Mark original event as reversed and create bidirectional link
        transactionService.reverseTransaction(originalEvent.getId(), reversalEvent.getId());

        // Update document status
        receipt.setStatus(DocumentStatus.DRAFT);
        BankReceipt unpostedReceipt = bankReceiptRepository.save(receipt);

        return bankReceiptMapper.toResponseDto(unpostedReceipt);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private BankReceipt findEntityByIdOrThrow(Long id) {
        return bankReceiptRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank receipt not found with id: " + id
                ));
    }

    private void loadRelatedEntities(BankReceipt receipt, BankReceiptRequestDto requestDto) {
        // Load account
        BankAccount account = bankAccountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + requestDto.getAccountId()
                ));
        receipt.setAccount(account);

        // Load counterparty
        Counterparty counterparty = counterpartyRepository.findById(requestDto.getCounterpartyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Counterparty not found with id: " + requestDto.getCounterpartyId()
                ));
        receipt.setCounterparty(counterparty);

        // Load counterparty bank account (optional)
        if (requestDto.getCounterpartyBankAccountId() != null) {
            BankAccount counterpartyAccount = bankAccountRepository
                    .findById(requestDto.getCounterpartyBankAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Counterparty bank account not found with id: " +
                                    requestDto.getCounterpartyBankAccountId()
                    ));
            receipt.setCounterpartyBankAccount(counterpartyAccount);
        } else {
            receipt.setCounterpartyBankAccount(null);
        }

        // Load currency
        Currency currency = currencyRepository.findById(requestDto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + requestDto.getCurrencyId()
                ));
        receipt.setCurrency(currency);

        // Load organization
        Organization organization = organizationRepository.findById(requestDto.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with id: " + requestDto.getOrganizationId()
                ));
        receipt.setOrganization(organization);
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