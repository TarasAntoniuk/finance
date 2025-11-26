package com.tarasantoniuk.finance.banking.bankreceipt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.common.TestDataCleaner;
import com.tarasantoniuk.finance.common.TestDataFactory;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class BankReceiptControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataCleaner testDataCleaner;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    private Organization organization;
    private Currency currency;
    private Counterparty counterparty;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        testDataCleaner.cleanAll();

        Country country = testDataFactory.createUkraine();
        organization = testDataFactory.createDefaultOrganization(country);
        currency = testDataFactory.createUAH();
        counterparty = testDataFactory.createDefaultCounterparty();
        Bank bank = testDataFactory.createPrivatBank(country);
        bankAccount = testDataFactory.createDefaultBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        testDataCleaner.cleanAll();
    }

    // ==================== CREATE Tests ====================

    @Test
    void createBankReceipt_ShouldReturnCreated_WhenValidRequest() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
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

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.documentDate").value("2024-01-15"))
                .andExpect(jsonPath("$.receiptType").value("CUSTOMER_PAYMENT"))
                .andExpect(jsonPath("$.amount").value(10000.00))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.bankCommission").value(50.00))
                .andExpect(jsonPath("$.description").value("Test receipt"))
                .andExpect(jsonPath("$.paymentPurpose").value("Payment for services"))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-12345"));
    }

    @Test
    void createBankReceipt_ShouldReturnBadRequest_WhenMissingRequiredFields() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsNull() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsZero() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(BigDecimal.ZERO);
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankReceipt_ShouldReturnBadRequest_WhenAmountIsNegative() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("-100.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankReceipt_ShouldReturnBadRequest_WhenBankCommissionIsNegative() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setBankCommission(new BigDecimal("-10.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankReceipt_ShouldReturnConflict_WhenDuplicateExternalTransactionId() throws Exception {
        BankReceipt existing = createBankReceipt("EXT-DUPLICATE");

        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setExternalTransactionId("EXT-DUPLICATE");

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBankReceipt_ShouldReturnNotFound_WhenAccountNotExists() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(99999L);
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBankReceipt_ShouldReturnNotFound_WhenCounterpartyNotExists() throws Exception {
        BankReceiptRequestDto requestDto = new BankReceiptRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(99999L);
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET BY ID Tests ====================

    @Test
    void getBankReceiptById_ShouldReturnReceipt_WhenExists() throws Exception {
        BankReceipt receipt = createBankReceipt("EXT-001");

        mockMvc.perform(get("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-001"));
    }

    @Test
    void getBankReceiptById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/bank-receipts/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== GET ALL Tests ====================

    @Test
    void getAllBankReceipts_ShouldReturnPagedResults() throws Exception {
        createBankReceipt("EXT-1");
        createBankReceipt("EXT-2");
        createBankReceipt("EXT-3");

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
    void getAllBankReceipts_ShouldReturnEmptyPage_WhenNoReceipts() throws Exception {
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.metadata.totalElements").value(0));
    }

    @Test
    void getAllBankReceipts_ShouldHandlePagination_WhenMultiplePages() throws Exception {
        for (int i = 1; i <= 25; i++) {
            createBankReceipt("EXT-" + i);
        }

        // First page
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.metadata.totalElements").value(25))
                .andExpect(jsonPath("$.metadata.totalPages").value(3))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(false));

        // Second page
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));

        // Last page
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.metadata.hasNext").value(false))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));
    }

    @Test
    void getAllBankReceipts_ShouldRespectSorting() throws Exception {
        BankReceipt receipt1 = createBankReceipt("EXT-1");
        receipt1.setDocumentDate(LocalDate.of(2024, 1, 10));
        bankReceiptRepository.save(receipt1);

        BankReceipt receipt2 = createBankReceipt("EXT-2");
        receipt2.setDocumentDate(LocalDate.of(2024, 1, 20));
        bankReceiptRepository.save(receipt2);

        // Sort ascending
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("sort", "documentDate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalTransactionId").value("EXT-1"))
                .andExpect(jsonPath("$.content[1].externalTransactionId").value("EXT-2"));

        // Sort descending
        mockMvc.perform(get("/api/v1/bank-receipts")
                        .param("sort", "documentDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalTransactionId").value("EXT-2"))
                .andExpect(jsonPath("$.content[1].externalTransactionId").value("EXT-1"));
    }

    // ==================== UPDATE Tests ====================

    @Test
    void updateBankReceipt_ShouldReturnUpdated_WhenValidRequest() throws Exception {
        // Given
        BankReceipt receipt = createBankReceipt("EXT-UPDATE-001");

        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setDocumentDate(LocalDate.now());
        updateDto.setReceiptType(ReceiptType.REFUND);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setBankCommission(null);
        updateDto.setDescription("Updated description");
        updateDto.setPaymentPurpose(null);
        updateDto.setPaymentReference(null);
        updateDto.setIncomingDocumentNumber(null);
        updateDto.setIncomingDocumentDate(null);
        updateDto.setTransactionDate(null);
        updateDto.setValueDate(null);
        updateDto.setBankProcessedAt(null);
        updateDto.setExternalTransactionId("EXT-UPDATE-001"); // Той самий ID
        updateDto.setBankReference(null);
        updateDto.setAccountId(bankAccount.getId());
        updateDto.setCounterpartyId(counterparty.getId());
        updateDto.setCounterpartyBankAccountId(null);
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
    void updateBankReceipt_ShouldReturnConflict_WhenStatusIsPosted() throws Exception {
        // Given - створюємо receipt зі статусом POSTED
        BankReceipt receipt = createBankReceipt("EXT-CONFLICT-001");
        receipt.setStatus(DocumentStatus.POSTED);
        receipt = bankReceiptRepository.save(receipt);

        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setDocumentDate(LocalDate.now());
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
    void updateBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        // Given
        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setDocumentDate(LocalDate.now());
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
    void deleteBankReceipt_ShouldReturnNoContent_WhenStatusIsDraft() throws Exception {
        BankReceipt receipt = createBankReceipt("EXT-DELETE");

        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBankReceipt_ShouldReturnConflict_WhenStatusIsPosted() throws Exception {
        BankReceipt receipt = createBankReceipt("EXT-DELETE-POSTED");
        receipt.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(receipt);

        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", receipt.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/bank-receipts/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== FILTER Tests ====================

    @Test
    void getBankReceiptsByAccountId_ShouldReturnFiltered() throws Exception {
        createBankReceipt("EXT-ACCOUNT-1");
        createBankReceipt("EXT-ACCOUNT-2");

        mockMvc.perform(get("/api/v1/bank-receipts/account/{accountId}", bankAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankReceiptsByAccountId_ShouldReturnEmpty_WhenNoReceiptsForAccount() throws Exception {
        mockMvc.perform(get("/api/v1/bank-receipts/account/{accountId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getBankReceiptsByCounterpartyId_ShouldReturnFiltered() throws Exception {
        createBankReceipt("EXT-CP-1");
        createBankReceipt("EXT-CP-2");

        mockMvc.perform(get("/api/v1/bank-receipts/counterparty/{counterpartyId}", counterparty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankReceiptsByCounterpartyId_ShouldReturnEmpty_WhenNoReceiptsForCounterparty() throws Exception {
        mockMvc.perform(get("/api/v1/bank-receipts/counterparty/{counterpartyId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getBankReceiptsByStatus_ShouldReturnFiltered() throws Exception {
        BankReceipt draft = createBankReceipt("EXT-DRAFT");

        BankReceipt posted = createBankReceipt("EXT-POSTED");
        posted.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(posted);

        mockMvc.perform(get("/api/v1/bank-receipts/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalTransactionId").value("EXT-DRAFT"));

        mockMvc.perform(get("/api/v1/bank-receipts/status/{status}", "POSTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalTransactionId").value("EXT-POSTED"));
    }

    @Test
    void getBankReceiptsByDateRange_ShouldReturnFiltered() throws Exception {
        BankReceipt jan = createBankReceipt("EXT-JAN");
        jan.setDocumentDate(LocalDate.of(2024, 1, 15));
        bankReceiptRepository.save(jan);

        BankReceipt feb = createBankReceipt("EXT-FEB");
        feb.setDocumentDate(LocalDate.of(2024, 2, 15));
        bankReceiptRepository.save(feb);

        BankReceipt mar = createBankReceipt("EXT-MAR");
        mar.setDocumentDate(LocalDate.of(2024, 3, 15));
        bankReceiptRepository.save(mar);

        // Filter January only
        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].externalTransactionId").value("EXT-JAN"));

        // Filter entire Q1
        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void getBankReceiptsByDateRange_ShouldReturnEmpty_WhenNoReceiptsInRange() throws Exception {
        BankReceipt jan = createBankReceipt("EXT-JAN");
        jan.setDocumentDate(LocalDate.of(2024, 1, 15));
        bankReceiptRepository.save(jan);

        mockMvc.perform(get("/api/v1/bank-receipts/date-range")
                        .param("startDate", "2024-02-01")
                        .param("endDate", "2024-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ========== POST ENDPOINT TESTS ==========

    @Test
    void postBankReceipt_ShouldReturnPosted_WhenValidDraftReceipt() throws Exception {
        // Given - create DRAFT receipt
        BankReceipt receipt = createBankReceipt("EXT-POST-001");
        receipt.setStatus(DocumentStatus.DRAFT);
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.status").value("POSTED"));

        // Verify receipt status changed in database
        BankReceipt updatedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.POSTED, updatedReceipt.getStatus());
    }

    @Test
    void postBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void postBankReceipt_ShouldReturnConflict_WhenReceiptAlreadyPosted() throws Exception {
        // Given - create POSTED receipt
        BankReceipt receipt = createBankReceipt("EXT-ALREADY-POSTED");
        receipt.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void postBankReceipt_ShouldReturnConflict_WhenEventAlreadyExists() throws Exception {
        // Given - create receipt and post it first time
        BankReceipt receipt = createBankReceipt("EXT-DOUBLE-POST");

        // Post first time
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Set status back to DRAFT manually (edge case simulation)
        receipt.setStatus(DocumentStatus.DRAFT);
        bankReceiptRepository.save(receipt);

        // When & Then - try to post again (event already exists)
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

// ========== UNPOST ENDPOINT TESTS ==========

    @Test
    void unpostBankReceipt_ShouldReturnDraft_WhenValidPostedReceipt() throws Exception {
        // Given - create and post receipt
        BankReceipt receipt = createBankReceipt("EXT-UNPOST-001");

        // Post it first
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // When & Then - unpost
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receipt.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        // Verify receipt status changed back to DRAFT in database
        BankReceipt updatedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.DRAFT, updatedReceipt.getStatus());
    }

    @Test
    void unpostBankReceipt_ShouldReturnNotFound_WhenReceiptNotExists() throws Exception {
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void unpostBankReceipt_ShouldReturnConflict_WhenReceiptIsNotPosted() throws Exception {
        // Given - create DRAFT receipt
        BankReceipt receipt = createBankReceipt("EXT-NOT-POSTED");
        receipt.setStatus(DocumentStatus.DRAFT);
        bankReceiptRepository.save(receipt);

        // When & Then - try to unpost DRAFT receipt
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void unpostBankReceipt_ShouldReturnNotFound_WhenOriginalEventNotFound() throws Exception {
        // Given - create receipt with POSTED status but WITHOUT event (edge case)
        BankReceipt receipt = createBankReceipt("EXT-NO-EVENT");
        receipt.setStatus(DocumentStatus.POSTED);
        bankReceiptRepository.save(receipt);

        // When & Then
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== POST/UNPOST FLOW TEST ==========

    @Test
    void postAndUnpostBankReceipt_ShouldWorkCorrectly_WhenFullFlow() throws Exception {
        // Given - create DRAFT receipt
        BankReceipt receipt = createBankReceipt("EXT-FLOW-001");

        // Step 1: Post receipt
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));

        // Verify status in DB
        BankReceipt postedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.POSTED, postedReceipt.getStatus());

        // Step 2: Unpost receipt
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/unpost", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        // Verify status back to DRAFT in DB
        BankReceipt unpostedReceipt = bankReceiptRepository.findById(receipt.getId()).orElseThrow();
        assertEquals(DocumentStatus.DRAFT, unpostedReceipt.getStatus());

        // Step 3: Post again (should work)
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

//    // ==================== Helper Methods ====================
//
//    private Country createCountry() {
//        Country country = new Country();
//        country.setIsoCode("UA");
//        country.setName("Ukraine");
//        return countryRepository.save(country);
//    }
//
//    private Organization createOrganization() {
//
//
//        Organization org = new Organization();
//        org.setName("Test Organization");
//        org.setVatNumber("TEST-ORG");
//        org.setCountry(country);
//        return organizationRepository.save(org);
//    }
//
//    private Currency createCurrency() {
//        Currency currency = new Currency();
//        currency.setCode("UAH");
//        currency.setName("Ukrainian Hryvnia");
//        currency.setSymbol("₴");
//        currency.setNumericCode("980");
//        currency.setMinorUnit(2);
//        currency.setIsActive(true);
//        return currencyRepository.save(currency);
//    }
//
//    private Counterparty createCounterparty() {
//        Counterparty cp = new Counterparty();
//        cp.setName("Test Counterparty");
//        cp.setCode("CP-001");
//        cp.setType(Counterparty.CounterpartyType.CUSTOMER);
//        return counterpartyRepository.save(cp);
//    }
//
//    private Bank createBank() {
//        Bank bank = new Bank();
//        bank.setName("Test Bank");
//        bank.setSwiftCode("TESTUA2X");
//        bank.setCountry(country);
//        return bankRepository.save(bank);
//    }
//
//    private BankAccount createBankAccount(Bank bank, Currency currency, Organization organization) {
//        BankAccount account = new BankAccount();
//        account.setAccountNumber("UA123456789012345678901234567");
//        account.setBank(bank);
//        account.setCurrency(currency);
//        account.setHolderType(AccountHolderType.ORGANIZATION);
//        account.setHolderId(organization.getId());
//        account.setAccountName("Test Account");
//        account.setStatus(AccountStatus.ACTIVE);
//        account.setIsDefault(true);
//        return bankAccountRepository.save(account);
//    }

    private BankReceipt createBankReceipt(String externalTransactionId) {
        BankReceipt receipt = new BankReceipt();
        receipt.setDocumentDate(LocalDate.of(2024, 1, 15));
        receipt.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        receipt.setAmount(new BigDecimal("10000.00"));
        receipt.setAccount(bankAccount);
        receipt.setCounterparty(counterparty);
        receipt.setCurrency(currency);
        receipt.setOrganization(organization);
        receipt.setStatus(DocumentStatus.DRAFT);
        receipt.setExternalTransactionId(externalTransactionId);
        return bankReceiptRepository.save(receipt);
    }
}