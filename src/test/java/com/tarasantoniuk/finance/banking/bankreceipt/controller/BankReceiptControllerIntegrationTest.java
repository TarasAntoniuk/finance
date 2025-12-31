package com.tarasantoniuk.finance.banking.bankreceipt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("BankReceipt Controller Integration Tests")
class BankReceiptControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    private Country country;
    private Organization organization;
    private Currency currency;
    private Counterparty counterparty;
    private Bank bank;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        // Create core entities using idempotent factories
        country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createCustomerCounterparty();

        // Create banking entities
        bank = factoryBanking.createPrivatBank(country);
        bankAccount = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should return created when valid request")
    void createBankReceipt_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("10000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setBankCommission(new BigDecimal("50.00"));
        requestDto.setDescription("Test receipt");
        requestDto.setPaymentPurpose("Payment for services");
        requestDto.setExternalTransactionId("EXT-12345");

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.transactionDateTime").value("2024-01-15T09:00:00"))
                .andExpect(jsonPath("$.receiptType").value("CUSTOMER_PAYMENT"))
                .andExpect(jsonPath("$.amount").value(10000.00))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.bankCommission").value(50.00))
                .andExpect(jsonPath("$.description").value("Test receipt"))
                .andExpect(jsonPath("$.paymentPurpose").value("Payment for services"))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-12345"));
    }

    @Test
    @DisplayName("Should return bad request when missing required fields")
    void createBankReceipt_ShouldReturnBadRequest_WhenMissingRequiredFields() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is null")
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsNull() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is zero")
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsZero() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(BigDecimal.ZERO);
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is negative")
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsNegative() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("-100.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return conflict when duplicate external transaction ID")
    void createBankReceipt_ShouldReturnConflict_WhenDuplicateExternalTransactionId() throws Exception {
        // Given
        BankReceipt existing = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        existing.setExternalTransactionId("EXT-DUPLICATE");
        bankReceiptRepository.save(existing);

        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setExternalTransactionId("EXT-DUPLICATE");

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return not found when account not exists")
    void createBankReceipt_ShouldReturnNotFound_WhenAccountNotExists() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(99999L);
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when counterparty not exists")
    void createBankReceipt_ShouldReturnNotFound_WhenCounterpartyNotExists() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(99999L);
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return created when counterparty bank account provided")
    void createBankReceipt_ShouldReturnCreated_WhenCounterpartyBankAccountProvided() throws Exception {
        // Given - create a counterparty bank account
        BankAccount counterpartyBankAccount = factoryBanking.createCounterpartyBankAccount(bank, currency, counterparty);

        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCounterpartyBankAccountId(counterpartyBankAccount.getId()); // Set counterparty bank account
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.counterpartyBankAccount").exists())
                .andExpect(jsonPath("$.counterpartyBankAccount.id").value(counterpartyBankAccount.getId()));
    }

    @Test
    @DisplayName("Should return not found when counterparty bank account not exists")
    void createBankReceipt_ShouldReturnNotFound_WhenCounterpartyBankAccountNotExists() throws Exception {
        // Given
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9,0,0));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCounterpartyBankAccountId(99999L); // Non-existent counterparty bank account
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET BY ID Tests ====================

    @Test
    @DisplayName("Should return receipt when exists")
    void getBankReceiptById_ShouldReturnReceipt_WhenExists() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setExternalTransactionId("EXT-001");
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-001"));
    }

    @Test
    @DisplayName("Should return not found when not exists")
    void getBankReceiptById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== GET ALL Tests ====================

    @Test
    @DisplayName("Should return paged results")
    void getAllBankReceipts_ShouldReturnPagedResults() throws Exception {
        // Given
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.metadata.totalElements").value(3))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.pageSize").value(10))
                .andExpect(jsonPath("$.metadata.totalPages").value(1))
                .andExpect(jsonPath("$.metadata.hasNext").value(false))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(false));
    }

    @Test
    @DisplayName("Should return empty page when no receipts")
    void getAllBankReceipts_ShouldReturnEmptyPage_WhenNoReceipts() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.metadata.totalElements").value(0));
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should return updated when valid request")
    void updateBankReceipt_ShouldReturnUpdated_WhenValidRequest() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setExternalTransactionId("EXT-UPDATE-001");
        bankReceiptRepository.save(receipt);

        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setTransactionDateTime(LocalDateTime.now());
        updateDto.setReceiptType(ReceiptType.REFUND);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setDescription("Updated description");
        updateDto.setExternalTransactionId("EXT-UPDATE-001");
        updateDto.setAccountId(bankAccount.getId());
        updateDto.setCounterpartyId(counterparty.getId());
        updateDto.setCurrencyId(currency.getId());
        updateDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(put("/api/v1/bank-receipts/{id}", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.receiptType").value("REFUND"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("Should return conflict when status is posted")
    void updateBankReceipt_ShouldReturnConflict_WhenStatusIsPosted() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setStatus(DocumentStatus.POSTED);
        receipt.setExternalTransactionId("EXT-CONFLICT-001");
        bankReceiptRepository.save(receipt);

        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setTransactionDateTime(LocalDateTime.now());
        updateDto.setReceiptType(ReceiptType.REFUND);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setExternalTransactionId("EXT-CONFLICT-001");
        updateDto.setAccountId(bankAccount.getId());
        updateDto.setCounterpartyId(counterparty.getId());
        updateDto.setCurrencyId(currency.getId());
        updateDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(put("/api/v1/bank-receipts/{id}", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return not found when receipt not exists")
    void updateBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        // Given
        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setTransactionDateTime(LocalDateTime.now());
        updateDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setAccountId(bankAccount.getId());
        updateDto.setCounterpartyId(counterparty.getId());
        updateDto.setCurrencyId(currency.getId());
        updateDto.setOrganizationId(organization.getId());

        // When & Then
        mockMvc.perform(put("/api/v1/bank-receipts/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should return no content when status is draft")
    void deleteBankReceipt_ShouldReturnNoContent_WhenStatusIsDraft() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return conflict when status is posted")
    void deleteBankReceipt_ShouldReturnConflict_WhenStatusIsPosted() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return not found when receipt not exists")
    void deleteBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== FILTER Tests ====================

    @Test
    @DisplayName("Should return filtered by account ID")
    void getBankReceiptsByAccountId_ShouldReturnFiltered() throws Exception {
        // Given
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/account/{accountId}", bankAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should return empty when no receipts for account")
    void getBankReceiptsByAccountId_ShouldReturnEmpty_WhenNoReceiptsForAccount() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/account/{accountId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should return filtered by counterparty ID")
    void getBankReceiptsByCounterpartyId_ShouldReturnFiltered() throws Exception {
        // Given
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/counterparty/{counterpartyId}", counterparty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Should return empty when no receipts for counterparty")
    void getBankReceiptsByCounterpartyId_ShouldReturnEmpty_WhenNoReceiptsForCounterparty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bank-receipts/counterparty/{counterpartyId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should return filtered by status DRAFT")
    void getBankReceiptsByStatus_ShouldReturnFiltered_WhenStatusIsDraft() throws Exception {
        // Given - create receipts with different statuses
        BankReceipt draft1 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        BankReceipt draft2 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        BankReceipt posted = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        posted.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(posted);

        // When & Then - filter by DRAFT status
        mockMvc.perform(get("/api/v1/bank-receipts/status/{status}", DocumentStatus.DRAFT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should return filtered by status POSTED")
    void getBankReceiptsByStatus_ShouldReturnFiltered_WhenStatusIsPosted() throws Exception {
        // Given - create receipts with different statuses
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        BankReceipt posted1 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        posted1.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(posted1);
        BankReceipt posted2 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        posted2.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(posted2);

        // When & Then - filter by POSTED status
        mockMvc.perform(get("/api/v1/bank-receipts/status/{status}", DocumentStatus.POSTED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should return empty when no receipts with status")
    void getBankReceiptsByStatus_ShouldReturnEmpty_WhenNoReceiptsWithStatus() throws Exception {
        // Given - create only DRAFT receipts
        factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then - try to find CANCELLED receipts
        mockMvc.perform(get("/api/v1/bank-receipts/status/{status}", DocumentStatus.CANCELLED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should return receipts within date range")
    void getBankReceiptsByDateRange_ShouldReturnFiltered_WhenReceiptsInRange() throws Exception {
        // Given - create receipts with different transaction dates
        BankReceipt receipt1 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt1.setTransactionDateTime(LocalDateTime.of(2024, 10, 15, 10, 0));
        bankReceiptRepository.save(receipt1);

        BankReceipt receipt2 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt2.setTransactionDateTime(LocalDateTime.of(2024, 11, 10, 14, 30));
        bankReceiptRepository.save(receipt2);

        BankReceipt receipt3 = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt3.setTransactionDateTime(LocalDateTime.of(2024, 12, 5, 9, 15));
        bankReceiptRepository.save(receipt3);

        BankReceipt outsideRange = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        outsideRange.setTransactionDateTime(LocalDateTime.of(2025, 1, 10, 12, 0));
        bankReceiptRepository.save(outsideRange);

        // When & Then - filter by date range that includes only first 3 receipts
        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-10-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Should return receipts on exact boundary dates")
    void getBankReceiptsByDateRange_ShouldIncludeBoundaryDates() throws Exception {
        // Given - create receipt exactly on start date and end date
        BankReceipt onStartDate = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        onStartDate.setTransactionDateTime(LocalDateTime.of(2024, 10, 1, 0, 0, 0));
        bankReceiptRepository.save(onStartDate);

        BankReceipt onEndDate = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        onEndDate.setTransactionDateTime(LocalDateTime.of(2024, 10, 31, 23, 59, 59));
        bankReceiptRepository.save(onEndDate);

        BankReceipt beforeRange = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        beforeRange.setTransactionDateTime(LocalDateTime.of(2024, 9, 30, 23, 59, 59));
        bankReceiptRepository.save(beforeRange);

        BankReceipt afterRange = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        afterRange.setTransactionDateTime(LocalDateTime.of(2024, 11, 1, 0, 0, 1));
        bankReceiptRepository.save(afterRange);

        // When & Then - filter should include boundary dates
        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-10-01")
                        .param("endDate", "2024-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should return empty when no receipts in date range")
    void getBankReceiptsByDateRange_ShouldReturnEmpty_WhenNoReceiptsInRange() throws Exception {
        // Given - create receipt outside the search range
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        bankReceiptRepository.save(receipt);

        // When & Then - search in a different date range
        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-06-01")
                        .param("endDate", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ==================== POST Tests ====================

    @Test
    @DisplayName("Should return posted when valid draft receipt")
    void postBankReceipt_ShouldReturnPosted_WhenValidDraftReceipt() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.status").value("POSTED"));

        // Verify status changed
        BankReceipt updatedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.POSTED, updatedReceipt.getStatus());
    }

    @Test
    @DisplayName("Should return not found when receipt not exists for post")
    void postBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return conflict when receipt already posted")
    void postBankReceipt_ShouldReturnConflict_WhenReceiptAlreadyPosted() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);
        receipt.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    // ==================== UNPOST Tests ====================

    @Test
    @DisplayName("Should return draft when valid posted receipt")
    void unpostBankReceipt_ShouldReturnDraft_WhenValidPostedReceipt() throws Exception {
        // Given - create and post receipt
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // When & Then - unpost
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        // Verify status changed back to DRAFT
        BankReceipt updatedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.DRAFT, updatedReceipt.getStatus());
    }

    @Test
    @DisplayName("Should return not found when receipt not exists for unpost")
    void unpostBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return conflict when receipt is not posted")
    void unpostBankReceipt_ShouldReturnConflict_WhenReceiptIsNotPosted() throws Exception {
        // Given
        BankReceipt receipt = factoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}