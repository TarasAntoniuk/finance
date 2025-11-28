package com.tarasantoniuk.finance.banking.bankpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.TestDataCleaner;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class BankPaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataCleaner testDataCleaner;

    @Autowired
    private TestDataFactoryCore testDataFactoryCore;

    @Autowired
    private TestDataFactoryBanking testDataFactoryBanking;

    @Autowired
    private BankPaymentRepository bankPaymentRepository;

    private Organization organization;
    private Currency currency;
    private Counterparty counterparty;
    private BankAccount bankAccount;
    private BankAccount counterpartyBankAccount;

    @BeforeEach
    void setUp() {
        testDataCleaner.cleanAll();

        Country country = testDataFactoryCore.createUkraine();
        organization = testDataFactoryCore.createDefaultOrganization(country);
        currency = testDataFactoryCore.createUAH();
        counterparty = testDataFactoryCore.createCustomerCounterparty();
        Bank bank = testDataFactoryBanking.createPrivatBank(country);
        bankAccount = testDataFactoryBanking.createOrganizationBankAccount(bank, currency, organization);
        counterpartyBankAccount = testDataFactoryBanking.createCounterpartyBankAccount(
                bank, currency, counterparty, "UA903052990000026001234567890");
    }

    @AfterEach
    void tearDown() {
        testDataCleaner.cleanAll();
    }

    // ==================== CREATE Tests ====================

    @Test
    void createBankPayment_ShouldReturnCreated_WhenValidRequest() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("10000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCounterpartyBankAccountId(counterpartyBankAccount.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setDescription("Test payment");
        requestDto.setExternalTransactionId("EXT-12345");

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.documentDate").value("2024-01-15"))
                .andExpect(jsonPath("$.amount").value(10000.00))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.description").value("Test payment"))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-12345"));
    }

    @Test
    void createBankPayment_ShouldReturnCreated_WhenWithoutCounterpartyBankAccount() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("5000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setDescription("Payment without counterparty account");

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createBankPayment_ShouldReturnBadRequest_WhenMissingRequiredFields() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankPayment_ShouldReturnBadRequest_WhenAmountIsNull() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankPayment_ShouldReturnBadRequest_WhenAmountIsZero() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(BigDecimal.ZERO);
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankPayment_ShouldReturnBadRequest_WhenAmountIsNegative() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("-100.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBankPayment_ShouldReturnConflict_WhenDuplicateExternalTransactionId() throws Exception {
        BankPayment existing = createBankPayment("EXT-DUPLICATE");

        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setExternalTransactionId("EXT-DUPLICATE");

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBankPayment_ShouldReturnNotFound_WhenAccountNotExists() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(99999L);
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBankPayment_ShouldReturnNotFound_WhenCounterpartyNotExists() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(99999L);
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBankPayment_ShouldReturnNotFound_WhenCurrencyNotExists() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(99999L);
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBankPayment_ShouldReturnNotFound_WhenOrganizationNotExists() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(99999L);

        mockMvc.perform(post("/api/v1/bank-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET BY ID Tests ====================

    @Test
    void getBankPaymentById_ShouldReturnPayment_WhenExists() throws Exception {
        BankPayment payment = createBankPayment("EXT-001");

        mockMvc.perform(get("/api/v1/bank-payments/{id}", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.externalTransactionId").value("EXT-001"));
    }

    @Test
    void getBankPaymentById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/bank-payments/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE Tests ====================

    @Test
    void updateBankPayment_ShouldReturnOk_WhenValidRequest() throws Exception {
        BankPayment payment = createBankPayment("EXT-002");

        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 20));
        requestDto.setAmount(new BigDecimal("15000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());
        requestDto.setDescription("Updated payment");
        requestDto.setExternalTransactionId("EXT-002");

        mockMvc.perform(put("/api/v1/bank-payments/{id}", payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.amount").value(15000.00))
                .andExpect(jsonPath("$.description").value("Updated payment"));
    }

    @Test
    void updateBankPayment_ShouldReturnNotFound_WhenPaymentNotExists() throws Exception {
        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(put("/api/v1/bank-payments/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBankPayment_ShouldReturnBadRequest_WhenPaymentIsPosted() throws Exception {
        BankPayment payment = createBankPayment("EXT-003");
        payment.setStatus(DocumentStatus.POSTED);
        bankPaymentRepository.save(payment);

        BankPaymentRequestDto requestDto = new BankPaymentRequestDto();
        requestDto.setDocumentDate(LocalDate.of(2024, 1, 15));
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setAccountId(bankAccount.getId());
        requestDto.setCounterpartyId(counterparty.getId());
        requestDto.setCurrencyId(currency.getId());
        requestDto.setOrganizationId(organization.getId());

        mockMvc.perform(put("/api/v1/bank-payments/{id}", payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET ALL Tests ====================

    @Test
    void getAllBankPayments_ShouldReturnPagedResults() throws Exception {
        createBankPayment("EXT-100");
        createBankPayment("EXT-101");
        createBankPayment("EXT-102");

        mockMvc.perform(get("/api/v1/bank-payments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.metadata.totalElements").value(3))
                .andExpect(jsonPath("$.metadata.totalPages").value(1));
    }

    @Test
    void getAllBankPayments_ShouldReturnEmptyPage_WhenNoPayments() throws Exception {
        mockMvc.perform(get("/api/v1/bank-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.metadata.totalElements").value(0));
    }

    @Test
    void getAllBankPayments_ShouldSupportPagination() throws Exception {
        for (int i = 0; i < 25; i++) {
            createBankPayment("EXT-" + i);
        }

        mockMvc.perform(get("/api/v1/bank-payments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.metadata.totalElements").value(25))
                .andExpect(jsonPath("$.metadata.totalPages").value(3))
                .andExpect(jsonPath("$.metadata.hasNext").value(true));
    }

    // ==================== GET BY ACCOUNT Tests ====================

    @Test
    void getBankPaymentsByAccountId_ShouldReturnPayments() throws Exception {
        createBankPayment("EXT-200");
        createBankPayment("EXT-201");

        mockMvc.perform(get("/api/v1/bank-payments/account/{accountId}", bankAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankPaymentsByAccountId_ShouldReturnEmptyPage_WhenNoPayments() throws Exception {
        mockMvc.perform(get("/api/v1/bank-payments/account/{accountId}", bankAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ==================== GET BY COUNTERPARTY Tests ====================

    @Test
    void getBankPaymentsByCounterpartyId_ShouldReturnPayments() throws Exception {
        createBankPayment("EXT-300");
        createBankPayment("EXT-301");

        mockMvc.perform(get("/api/v1/bank-payments/counterparty/{counterpartyId}", counterparty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankPaymentsByCounterpartyId_ShouldReturnEmptyPage_WhenNoPayments() throws Exception {
        mockMvc.perform(get("/api/v1/bank-payments/counterparty/{counterpartyId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ==================== GET BY STATUS Tests ====================

    @Test
    void getBankPaymentsByStatus_ShouldReturnPayments_WhenDraft() throws Exception {
        createBankPayment("EXT-400");
        createBankPayment("EXT-401");

        mockMvc.perform(get("/api/v1/bank-payments/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankPaymentsByStatus_ShouldReturnPayments_WhenPosted() throws Exception {
        BankPayment payment1 = createBankPayment("EXT-500");
        payment1.setStatus(DocumentStatus.POSTED);
        bankPaymentRepository.save(payment1);

        BankPayment payment2 = createBankPayment("EXT-501");
        payment2.setStatus(DocumentStatus.POSTED);
        bankPaymentRepository.save(payment2);

        mockMvc.perform(get("/api/v1/bank-payments/status/{status}", "POSTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getBankPaymentsByStatus_ShouldReturnEmptyPage_WhenNoPaymentsWithStatus() throws Exception {
        createBankPayment("EXT-600");

        mockMvc.perform(get("/api/v1/bank-payments/status/{status}", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ==================== GET BY DATE RANGE Tests ====================

    @Test
    void getBankPaymentsByDateRange_ShouldReturnPayments() throws Exception {
        BankPayment payment1 = createBankPayment("EXT-700");
        payment1.setDocumentDate(LocalDate.of(2024, 1, 10));
        bankPaymentRepository.save(payment1);

        BankPayment payment2 = createBankPayment("EXT-701");
        payment2.setDocumentDate(LocalDate.of(2024, 1, 20));
        bankPaymentRepository.save(payment2);

        BankPayment payment3 = createBankPayment("EXT-702");
        payment3.setDocumentDate(LocalDate.of(2024, 1, 30));
        bankPaymentRepository.save(payment3);

        mockMvc.perform(get("/api/v1/bank-payments/date-range")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getBankPaymentsByDateRange_ShouldReturnEmptyPage_WhenNoPaymentsInRange() throws Exception {
        BankPayment payment = createBankPayment("EXT-800");
        payment.setDocumentDate(LocalDate.of(2024, 1, 10));
        bankPaymentRepository.save(payment);

        mockMvc.perform(get("/api/v1/bank-payments/date-range")
                        .param("startDate", "2024-02-01")
                        .param("endDate", "2024-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ==================== DELETE Tests ====================

    @Test
    void deleteBankPayment_ShouldReturnNoContent_WhenPaymentIsDraft() throws Exception {
        BankPayment payment = createBankPayment("EXT-900");

        mockMvc.perform(delete("/api/v1/bank-payments/{id}", payment.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBankPayment_ShouldReturnNotFound_WhenPaymentNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/bank-payments/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBankPayment_ShouldReturnBadRequest_WhenPaymentIsPosted() throws Exception {
        BankPayment payment = createBankPayment("EXT-901");
        payment.setStatus(DocumentStatus.POSTED);
        bankPaymentRepository.save(payment);

        mockMvc.perform(delete("/api/v1/bank-payments/{id}", payment.getId()))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST Tests ====================

    @Test
    void postBankPayment_ShouldReturnOk_WhenPaymentIsDraft() throws Exception {
        BankPayment payment = createBankPayment("EXT-1000");

        mockMvc.perform(post("/api/v1/bank-payments/{id}/post", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

    @Test
    void postBankPayment_ShouldReturnNotFound_WhenPaymentNotExists() throws Exception {
        mockMvc.perform(post("/api/v1/bank-payments/{id}/post", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void postBankPayment_ShouldReturnBadRequest_WhenPaymentIsAlreadyPosted() throws Exception {
        BankPayment payment = createBankPayment("EXT-1001");
        payment.setStatus(DocumentStatus.POSTED);
        bankPaymentRepository.save(payment);

        mockMvc.perform(post("/api/v1/bank-payments/{id}/post", payment.getId()))
                .andExpect(status().isBadRequest());
    }

    // ==================== UNPOST Tests ====================

    @Test
    void unpostBankPayment_ShouldReturnOk_WhenPaymentIsPosted() throws Exception {
        // Create initial balance by posting a receipt first
        BankReceipt receipt = testDataFactoryBanking.createBankReceipt(bankAccount, counterparty, currency, organization);

        // Post receipt to create balance
        mockMvc.perform(post("/api/v1/bank-receipts/{id}/post", receipt.getId()))
                .andExpect(status().isOk());

        // Now create and post payment
        BankPayment payment = createBankPayment("EXT-1100");

        mockMvc.perform(post("/api/v1/bank-payments/{id}/post", payment.getId()))
                .andExpect(status().isOk());

        // Now unpost it
        mockMvc.perform(post("/api/v1/bank-payments/{id}/unpost", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void unpostBankPayment_ShouldReturnNotFound_WhenPaymentNotExists() throws Exception {
        mockMvc.perform(post("/api/v1/bank-payments/{id}/unpost", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void unpostBankPayment_ShouldReturnBadRequest_WhenPaymentIsDraft() throws Exception {
        BankPayment payment = createBankPayment("EXT-1101");

        mockMvc.perform(post("/api/v1/bank-payments/{id}/unpost", payment.getId()))
                .andExpect(status().isBadRequest());
    }

    // ==================== Helper Methods ====================

    private BankPayment createBankPayment(String externalTransactionId) {
        return testDataFactoryBanking.createBankPayment(
                bankAccount, counterparty, currency, organization);
    }
}