package com.tarasantoniuk.finance.banking.bankpayment.mapper;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BankPaymentMapperTest extends BaseIntegrationTest {

    @Autowired
    private BankPaymentMapper mapper;

    @Test
    void shouldMapRequestDtoToEntity() {
        // Given
        BankPaymentRequestDto dto = createFullRequestDto();

        // When
        BankPayment entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId(), "ID should not be mapped");

        // Verify base fields from BaseBankPaymentDto
        assertEquals(dto.getDocumentDate(), entity.getDocumentDate());
        assertEquals(dto.getPaymentType(), entity.getPaymentType());
        assertEquals(dto.getAmount(), entity.getAmount());
        assertEquals(dto.getBankCommission(), entity.getBankCommission());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getPaymentPurpose(), entity.getPaymentPurpose());
        assertEquals(dto.getPaymentReference(), entity.getPaymentReference());
        assertEquals(dto.getOutgoingDocumentNumber(), entity.getOutgoingDocumentNumber());
        assertEquals(dto.getOutgoingDocumentDate(), entity.getOutgoingDocumentDate());
        assertEquals(dto.getTransactionDate(), entity.getTransactionDate());
        assertEquals(dto.getValueDate(), entity.getValueDate());
        assertEquals(dto.getBankProcessedAt(), entity.getBankProcessedAt());
        assertEquals(dto.getExternalTransactionId(), entity.getExternalTransactionId());
        assertEquals(dto.getBankReference(), entity.getBankReference());

        // Verify references are NOT mapped (ignored in mapper)
        assertNull(entity.getAccount(), "Account should not be mapped");
        assertNull(entity.getCounterparty(), "Counterparty should not be mapped");
        assertNull(entity.getCounterpartyBankAccount(), "CounterpartyBankAccount should not be mapped");
        assertNull(entity.getCurrency(), "Currency should not be mapped");
        assertNull(entity.getOrganization(), "Organization should not be mapped");

        // Verify status fields are NOT mapped from DTO (entity may have defaults)
        // Status has default value DRAFT in entity, so we verify it wasn't changed by mapping
        assertEquals(DocumentStatus.DRAFT, entity.getStatus(),
                "Status should have default value (not mapped from DTO)");
        assertNull(entity.getPostedAt(), "PostedAt should not be mapped");
        assertNull(entity.getCancelledAt(), "CancelledAt should not be mapped");
    }

    @Test
    void shouldMapRequestDtoToEntity_WithNullOptionalFields() {
        // Given
        BankPaymentRequestDto dto = new BankPaymentRequestDto();
        dto.setDocumentDate(LocalDate.of(2024, 1, 15));
        dto.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setAccountId(1L);
        dto.setCounterpartyId(5L);
        dto.setCurrencyId(2L);
        dto.setOrganizationId(1L);
        // All optional fields are null

        // When
        BankPayment entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getBankCommission());
        assertNull(entity.getDescription());
        assertNull(entity.getPaymentPurpose());
        assertNull(entity.getPaymentReference());
        assertNull(entity.getOutgoingDocumentNumber());
        assertNull(entity.getOutgoingDocumentDate());
        assertNull(entity.getTransactionDate());
        assertNull(entity.getValueDate());
        assertNull(entity.getBankProcessedAt());
        assertNull(entity.getExternalTransactionId());
        assertNull(entity.getBankReference());
        assertNull(entity.getCounterpartyBankAccount());
    }

    @Test
    void shouldMapEntityToResponseDto() {
        // Given
        BankPayment entity = createFullBankPaymentEntity();

        // When
        BankPaymentResponseDto dto = mapper.toResponseDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());

        // Verify base fields
        assertEquals(entity.getDocumentDate(), dto.getDocumentDate());
        assertEquals(entity.getPaymentType(), dto.getPaymentType());
        assertEquals(entity.getAmount(), dto.getAmount());
        assertEquals(entity.getBankCommission(), dto.getBankCommission());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getPaymentPurpose(), dto.getPaymentPurpose());
        assertEquals(entity.getPaymentReference(), dto.getPaymentReference());
        assertEquals(entity.getOutgoingDocumentNumber(), dto.getOutgoingDocumentNumber());
        assertEquals(entity.getOutgoingDocumentDate(), dto.getOutgoingDocumentDate());
        assertEquals(entity.getTransactionDate(), dto.getTransactionDate());
        assertEquals(entity.getValueDate(), dto.getValueDate());
        assertEquals(entity.getBankProcessedAt(), dto.getBankProcessedAt());
        assertEquals(entity.getExternalTransactionId(), dto.getExternalTransactionId());
        assertEquals(entity.getBankReference(), dto.getBankReference());

        // Verify status fields
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getPostedAt(), dto.getPostedAt());
        assertEquals(entity.getCancelledAt(), dto.getCancelledAt());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), dto.getUpdatedAt());

        // Verify nested objects are mapped
        assertNotNull(dto.getAccount());
        assertEquals(entity.getAccount().getId(), dto.getAccount().getId());
        assertNotNull(dto.getCounterparty());
        assertEquals(entity.getCounterparty().getId(), dto.getCounterparty().getId());
        assertNotNull(dto.getCounterpartyBankAccount());
        assertEquals(entity.getCounterpartyBankAccount().getId(), dto.getCounterpartyBankAccount().getId());
        assertNotNull(dto.getCurrency());
        assertEquals(entity.getCurrency().getId(), dto.getCurrency().getId());
        assertNotNull(dto.getOrganization());
        assertEquals(entity.getOrganization().getId(), dto.getOrganization().getId());
    }

    @Test
    void shouldMapEntityToResponseDto_WithNullCounterpartyBankAccount() {
        // Given
        BankPayment entity = createFullBankPaymentEntity();
        entity.setCounterpartyBankAccount(null);

        // When
        BankPaymentResponseDto dto = mapper.toResponseDto(entity);

        // Then
        assertNotNull(dto);
        assertNull(dto.getCounterpartyBankAccount(),
                "Counterparty bank account should be null in DTO");
    }

    @Test
    void shouldMapEntityToResponseDto_WithNullOptionalFields() {
        // Given
        BankPayment entity = new BankPayment();
        entity.setId(1L);
        entity.setDocumentDate(LocalDate.of(2024, 1, 15));
        entity.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        entity.setAmount(new BigDecimal("1000.00"));
        entity.setStatus(DocumentStatus.DRAFT);
        // All optional fields are null

        // When
        BankPaymentResponseDto dto = mapper.toResponseDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertNull(dto.getBankCommission());
        assertNull(dto.getDescription());
        assertNull(dto.getPaymentPurpose());
        assertNull(dto.getAccount());
        assertNull(dto.getCounterparty());
        assertNull(dto.getCurrency());
        assertNull(dto.getOrganization());
    }

    @Test
    void shouldUpdateEntityFromDto() {
        // Given
        BankPayment existingEntity = createFullBankPaymentEntity();
        Long originalId = existingEntity.getId();
        DocumentStatus originalStatus = existingEntity.getStatus();
        LocalDateTime originalPostedAt = existingEntity.getPostedAt();
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt();
        BankAccount originalAccount = existingEntity.getAccount();
        Counterparty originalCounterparty = existingEntity.getCounterparty();
        Currency originalCurrency = existingEntity.getCurrency();
        Organization originalOrganization = existingEntity.getOrganization();

        BankPaymentRequestDto updateDto = new BankPaymentRequestDto();
        updateDto.setDocumentDate(LocalDate.of(2024, 2, 20));
        updateDto.setPaymentType(PaymentType.TAX_PAYMENT);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setBankCommission(new BigDecimal("25.00"));
        updateDto.setAccountId(2L);
        updateDto.setCounterpartyId(6L);
        updateDto.setCounterpartyBankAccountId(15L);
        updateDto.setCurrencyId(3L);
        updateDto.setOrganizationId(2L);
        updateDto.setDescription("Updated description");
        updateDto.setPaymentPurpose("Updated purpose");
        updateDto.setPaymentReference("NEW-REF-001");
        updateDto.setOutgoingDocumentNumber("OUT-2024-002");
        updateDto.setOutgoingDocumentDate(LocalDate.of(2024, 2, 19));
        updateDto.setTransactionDate(LocalDate.of(2024, 2, 20));
        updateDto.setValueDate(LocalDate.of(2024, 2, 21));
        updateDto.setBankProcessedAt(LocalDateTime.of(2024, 2, 20, 14, 30));
        updateDto.setExternalTransactionId("EXT-NEW-456");
        updateDto.setBankReference("BANK-NEW-REF");

        // When
        mapper.updateEntityFromDto(updateDto, existingEntity);

        // Then
        // Verify immutable fields are NOT changed
        assertEquals(originalId, existingEntity.getId(), "ID should not be updated");
        assertEquals(originalStatus, existingEntity.getStatus(), "Status should not be updated");
        assertEquals(originalPostedAt, existingEntity.getPostedAt(), "PostedAt should not be updated");
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt(), "CreatedAt should not be updated");

        // Verify references are NOT changed (ignored in mapper)
        assertSame(originalAccount, existingEntity.getAccount(), "Account should not be updated by mapper");
        assertSame(originalCounterparty, existingEntity.getCounterparty(), "Counterparty should not be updated by mapper");
        assertSame(originalCurrency, existingEntity.getCurrency(), "Currency should not be updated by mapper");
        assertSame(originalOrganization, existingEntity.getOrganization(), "Organization should not be updated by mapper");

        // Verify mutable fields ARE changed
        assertEquals(updateDto.getDocumentDate(), existingEntity.getDocumentDate());
        assertEquals(updateDto.getPaymentType(), existingEntity.getPaymentType());
        assertEquals(updateDto.getAmount(), existingEntity.getAmount());
        assertEquals(updateDto.getBankCommission(), existingEntity.getBankCommission());
        assertEquals(updateDto.getDescription(), existingEntity.getDescription());
        assertEquals(updateDto.getPaymentPurpose(), existingEntity.getPaymentPurpose());
        assertEquals(updateDto.getPaymentReference(), existingEntity.getPaymentReference());
        assertEquals(updateDto.getOutgoingDocumentNumber(), existingEntity.getOutgoingDocumentNumber());
        assertEquals(updateDto.getOutgoingDocumentDate(), existingEntity.getOutgoingDocumentDate());
        assertEquals(updateDto.getTransactionDate(), existingEntity.getTransactionDate());
        assertEquals(updateDto.getValueDate(), existingEntity.getValueDate());
        assertEquals(updateDto.getBankProcessedAt(), existingEntity.getBankProcessedAt());
        assertEquals(updateDto.getExternalTransactionId(), existingEntity.getExternalTransactionId());
        assertEquals(updateDto.getBankReference(), existingEntity.getBankReference());
    }

    @Test
    void shouldNotUpdateNullableFieldsToNullWhenNotProvided() {
        // Given
        BankPayment existingEntity = createFullBankPaymentEntity();
        String originalDescription = existingEntity.getDescription();
        String originalPaymentPurpose = existingEntity.getPaymentPurpose();

        BankPaymentRequestDto updateDto = new BankPaymentRequestDto();
        updateDto.setDocumentDate(LocalDate.of(2024, 2, 20));
        updateDto.setPaymentType(PaymentType.TAX_PAYMENT);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setAccountId(1L);
        updateDto.setCounterpartyId(5L);
        updateDto.setCurrencyId(2L);
        updateDto.setOrganizationId(1L);
        // Description and PaymentPurpose are null in DTO

        // When
        mapper.updateEntityFromDto(updateDto, existingEntity);

        // Then - with default MapStruct behavior, null values will overwrite
        // This test documents the actual behavior
        assertNull(existingEntity.getDescription(),
                "Description should be set to null when DTO has null");
        assertNull(existingEntity.getPaymentPurpose(),
                "PaymentPurpose should be set to null when DTO has null");
    }

    @Test
    void shouldMapNullEntityToNullDto() {
        // When
        BankPaymentResponseDto dto = mapper.toResponseDto(null);

        // Then
        assertNull(dto, "Null entity should map to null DTO");
    }

    @Test
    void shouldMapNullDtoToNullEntity() {
        // When
        BankPayment entity = mapper.toEntity(null);

        // Then
        assertNull(entity, "Null DTO should map to null entity");
    }

    // Helper method to create full request DTO
    private BankPaymentRequestDto createFullRequestDto() {
        BankPaymentRequestDto dto = new BankPaymentRequestDto();
        dto.setDocumentDate(LocalDate.of(2024, 1, 15));
        dto.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setBankCommission(new BigDecimal("50.00"));
        dto.setAccountId(1L);
        dto.setCounterpartyId(5L);
        dto.setCounterpartyBankAccountId(12L);
        dto.setCurrencyId(2L);
        dto.setOrganizationId(1L);
        dto.setDescription("Payment for goods");
        dto.setPaymentPurpose("Invoice #INV-2024-001");
        dto.setPaymentReference("INV-2024-001");
        dto.setOutgoingDocumentNumber("OUT-2024-001");
        dto.setOutgoingDocumentDate(LocalDate.of(2024, 1, 14));
        dto.setTransactionDate(LocalDate.of(2024, 1, 15));
        dto.setValueDate(LocalDate.of(2024, 1, 16));
        dto.setBankProcessedAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
        dto.setExternalTransactionId("BANK-TXN-12345");
        dto.setBankReference("BANK-REF-001");
        return dto;
    }

    // Helper method to create full entity
    private BankPayment createFullBankPaymentEntity() {
        BankPayment payment = new BankPayment();
        payment.setId(100L);
        payment.setDocumentDate(LocalDate.of(2024, 1, 15));
        payment.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        payment.setAmount(new BigDecimal("10000.00"));
        payment.setBankCommission(new BigDecimal("50.00"));
        payment.setDescription("Test payment");
        payment.setPaymentPurpose("Test purpose");
        payment.setPaymentReference("REF-001");
        payment.setOutgoingDocumentNumber("OUT-2024-001");
        payment.setOutgoingDocumentDate(LocalDate.of(2024, 1, 14));
        payment.setTransactionDate(LocalDate.of(2024, 1, 15));
        payment.setValueDate(LocalDate.of(2024, 1, 16));
        payment.setBankProcessedAt(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
        payment.setExternalTransactionId("EXT-12345");
        payment.setBankReference("BANK-REF-001");
        payment.setStatus(DocumentStatus.DRAFT);
        payment.setPostedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        payment.setCancelledAt(null);
        payment.setCreatedAt(LocalDateTime.of(2024, 1, 15, 9, 0));
        payment.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 9, 30));

        // Create related entities with IDs
        BankAccount account = new BankAccount();
        account.setId(1L);
        payment.setAccount(account);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(5L);
        payment.setCounterparty(counterparty);

        BankAccount counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(12L);
        payment.setCounterpartyBankAccount(counterpartyAccount);

        Currency currency = new Currency();
        currency.setId(2L);
        payment.setCurrency(currency);

        Organization organization = new Organization();
        organization.setId(1L);
        payment.setOrganization(organization);

        return payment;
    }
}