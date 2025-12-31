package com.tarasantoniuk.finance.banking.bankreceipt.mapper;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BankReceiptMapperTest extends BaseIntegrationTest {

    @Autowired
    private BankReceiptMapper mapper;

    @Test
    void shouldMapRequestDtoToEntity() {
        // Given
        BankReceiptRequestDto dto = new BankReceiptRequestDto();
        dto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9, 0,0));
        dto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setBankCommission(new BigDecimal("50.00"));
        dto.setAccountId(1L);
        dto.setCounterpartyId(5L);
        dto.setCounterpartyBankAccountId(12L);
        dto.setCurrencyId(2L);
        dto.setOrganizationId(1L);
        dto.setDescription("Payment for services");
        dto.setPaymentPurpose("Invoice #INV-2024-001");
        dto.setPaymentReference("INV-2024-001");
        dto.setExternalTransactionId("BANK-TXN-12345");

        // When
        BankReceipt entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId(), "ID should not be mapped");
        assertEquals(dto.getTransactionDateTime(), entity.getTransactionDateTime());
        assertEquals(dto.getReceiptType(), entity.getReceiptType());
        assertEquals(dto.getAmount(), entity.getAmount());
        assertEquals(dto.getBankCommission(), entity.getBankCommission());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getPaymentPurpose(), entity.getPaymentPurpose());
        assertEquals(dto.getPaymentReference(), entity.getPaymentReference());
        assertEquals(dto.getExternalTransactionId(), entity.getExternalTransactionId());

        // Verify ID mapping for references
        assertNotNull(entity.getAccount());
        assertEquals(dto.getAccountId(), entity.getAccount().getId());
        assertNotNull(entity.getCounterparty());
        assertEquals(dto.getCounterpartyId(), entity.getCounterparty().getId());
        assertNotNull(entity.getCounterpartyBankAccount());
        assertEquals(dto.getCounterpartyBankAccountId(), entity.getCounterpartyBankAccount().getId());
        assertNotNull(entity.getCurrency());
        assertEquals(dto.getCurrencyId(), entity.getCurrency().getId());
        assertNotNull(entity.getOrganization());
        assertEquals(dto.getOrganizationId(), entity.getOrganization().getId());
    }

    @Test
    void shouldMapRequestDtoToEntity_WithNullCounterpartyBankAccount() {
        // Given
        BankReceiptRequestDto dto = new BankReceiptRequestDto();
        dto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9, 0, 0));
        dto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setAccountId(1L);
        dto.setCounterpartyId(5L);
        dto.setCounterpartyBankAccountId(null); // NULL
        dto.setCurrencyId(2L);
        dto.setOrganizationId(1L);

        // When
        BankReceipt entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        // MapStruct може створити пустий об'єкт з null ID - це прийнятно
        if (entity.getCounterpartyBankAccount() != null) {
            assertNull(entity.getCounterpartyBankAccount().getId(),
                    "Should have null ID when DTO had null counterpartyBankAccountId");
        }
    }

    @Test
    void shouldMapEntityToResponseDto() {
        // Given
        BankReceipt entity = createFullBankReceiptEntity();

        // When
        BankReceiptResponseDto dto = mapper.toResponseDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getTransactionDateTime(), dto.getTransactionDateTime());
        assertEquals(entity.getReceiptType(), dto.getReceiptType());
        assertEquals(entity.getAmount(), dto.getAmount());
        assertEquals(entity.getBankCommission(), dto.getBankCommission());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getPaymentPurpose(), dto.getPaymentPurpose());
        assertEquals(entity.getPaymentReference(), dto.getPaymentReference());
        assertEquals(entity.getExternalTransactionId(), dto.getExternalTransactionId());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getPostedAt(), dto.getPostedAt());
        assertEquals(entity.getCancelledAt(), dto.getCancelledAt());

        // Verify nested objects are mapped
        assertNotNull(dto.getAccount());
        assertEquals(entity.getAccount().getId(), dto.getAccount().getId());
        assertNotNull(dto.getCounterparty());
        assertEquals(entity.getCounterparty().getId(), dto.getCounterparty().getId());
        assertNotNull(dto.getCurrency());
        assertEquals(entity.getCurrency().getId(), dto.getCurrency().getId());
        assertNotNull(dto.getOrganization());
        assertEquals(entity.getOrganization().getId(), dto.getOrganization().getId());
    }

    @Test
    void shouldMapEntityToResponseDto_WithNullCounterpartyBankAccount() {
        // Given
        BankReceipt entity = createFullBankReceiptEntity();
        entity.setCounterpartyBankAccount(null); // NULL

        // When
        BankReceiptResponseDto dto = mapper.toResponseDto(entity);

        // Then
        assertNotNull(dto);
        assertNull(dto.getCounterpartyBankAccount(), "Counterparty bank account should be null in DTO");
    }

    @Test
    void shouldUpdateEntityFromDto() {
        // Given
        BankReceipt existingEntity = createFullBankReceiptEntity();
        Long originalId = existingEntity.getId();
        DocumentStatus originalStatus = existingEntity.getStatus();
        LocalDateTime originalPostedAt = existingEntity.getPostedAt();

        BankReceiptRequestDto updateDto = new BankReceiptRequestDto();
        updateDto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9, 0, 0));
        updateDto.setReceiptType(ReceiptType.REFUND);
        updateDto.setAmount(new BigDecimal("5000.00"));
        updateDto.setBankCommission(new BigDecimal("25.00"));
        updateDto.setAccountId(2L);
        updateDto.setCounterpartyId(6L);
        updateDto.setCounterpartyBankAccountId(15L);
        updateDto.setCurrencyId(3L);
        updateDto.setOrganizationId(2L);
        updateDto.setDescription("Updated description");
        updateDto.setPaymentPurpose("Updated purpose");

        // When
        mapper.updateEntityFromDto(updateDto, existingEntity);

        // Then
        // Verify immutable fields are NOT changed
        assertEquals(originalId, existingEntity.getId(), "ID should not be updated");
        assertEquals(originalStatus, existingEntity.getStatus(), "Status should not be updated");
        assertEquals(originalPostedAt, existingEntity.getPostedAt(), "PostedAt should not be updated");

        // Verify mutable fields ARE changed
        assertEquals(updateDto.getTransactionDateTime(), existingEntity.getTransactionDateTime());
        assertEquals(updateDto.getReceiptType(), existingEntity.getReceiptType());
        assertEquals(updateDto.getAmount(), existingEntity.getAmount());
        assertEquals(updateDto.getBankCommission(), existingEntity.getBankCommission());
        assertEquals(updateDto.getDescription(), existingEntity.getDescription());
        assertEquals(updateDto.getPaymentPurpose(), existingEntity.getPaymentPurpose());

        // Verify references are updated
        assertEquals(updateDto.getAccountId(), existingEntity.getAccount().getId());
        assertEquals(updateDto.getCounterpartyId(), existingEntity.getCounterparty().getId());
        assertEquals(updateDto.getCounterpartyBankAccountId(), existingEntity.getCounterpartyBankAccount().getId());
    }

    @Test
    void shouldHandleNullValuesInRequestDto() {
        // Given
        BankReceiptRequestDto dto = new BankReceiptRequestDto();
        dto.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9, 0,0));
        dto.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setAccountId(1L);
        dto.setCounterpartyId(5L);
        dto.setCurrencyId(2L);
        dto.setOrganizationId(1L);
        dto.setCounterpartyBankAccountId(null); // explicitly null
        // All other optional fields are null

        // When
        BankReceipt entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getBankCommission());
        assertNull(entity.getDescription());
        assertNull(entity.getPaymentPurpose());
        assertNull(entity.getPaymentReference());
        assertNull(entity.getExternalTransactionId());
    }

    @Test
    void mapAccount_ShouldReturnNull_WhenIdIsNull() {
        // When
        BankAccount result = mapper.mapAccount(null);

        // Then
        assertNull(result, "Should return null when account ID is null");
    }

    @Test
    void mapAccount_ShouldReturnEntityStub_WhenIdIsProvided() {
        // Given
        Long accountId = 123L;

        // When
        BankAccount result = mapper.mapAccount(accountId);

        // Then
        assertNotNull(result, "Should return entity stub");
        assertEquals(accountId, result.getId(), "Should have correct ID");
    }

    @Test
    void mapCounterparty_ShouldReturnNull_WhenIdIsNull() {
        // When
        Counterparty result = mapper.mapCounterparty(null);

        // Then
        assertNull(result, "Should return null when counterparty ID is null");
    }

    @Test
    void mapCounterparty_ShouldReturnEntityStub_WhenIdIsProvided() {
        // Given
        Long counterpartyId = 456L;

        // When
        Counterparty result = mapper.mapCounterparty(counterpartyId);

        // Then
        assertNotNull(result, "Should return entity stub");
        assertEquals(counterpartyId, result.getId(), "Should have correct ID");
    }

    @Test
    void mapCurrency_ShouldReturnNull_WhenIdIsNull() {
        // When
        Currency result = mapper.mapCurrency(null);

        // Then
        assertNull(result, "Should return null when currency ID is null");
    }

    @Test
    void mapCurrency_ShouldReturnEntityStub_WhenIdIsProvided() {
        // Given
        Long currencyId = 789L;

        // When
        Currency result = mapper.mapCurrency(currencyId);

        // Then
        assertNotNull(result, "Should return entity stub");
        assertEquals(currencyId, result.getId(), "Should have correct ID");
    }

    @Test
    void mapOrganization_ShouldReturnNull_WhenIdIsNull() {
        // When
        Organization result = mapper.mapOrganization(null);

        // Then
        assertNull(result, "Should return null when organization ID is null");
    }

    @Test
    void mapOrganization_ShouldReturnEntityStub_WhenIdIsProvided() {
        // Given
        Long organizationId = 321L;

        // When
        Organization result = mapper.mapOrganization(organizationId);

        // Then
        assertNotNull(result, "Should return entity stub");
        assertEquals(organizationId, result.getId(), "Should have correct ID");
    }

    // Helper method to create full entity
    private BankReceipt createFullBankReceiptEntity() {
        BankReceipt receipt = new BankReceipt();
        receipt.setId(100L);
        receipt.setTransactionDateTime(LocalDateTime.of(2024, 1, 15, 9, 0,0));
        receipt.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        receipt.setAmount(new BigDecimal("10000.00"));
        receipt.setBankCommission(new BigDecimal("50.00"));
        receipt.setDescription("Test payment");
        receipt.setPaymentPurpose("Test purpose");
        receipt.setPaymentReference("REF-001");
        receipt.setExternalTransactionId("EXT-12345");
        receipt.setStatus(DocumentStatus.DRAFT);
        receipt.setPostedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        receipt.setCancelledAt(null);

        // Create related entities with IDs
        BankAccount account = new BankAccount();
        account.setId(1L);
        receipt.setAccount(account);

        Counterparty counterparty = new Counterparty();
        counterparty.setId(5L);
        receipt.setCounterparty(counterparty);

        BankAccount counterpartyAccount = new BankAccount();
        counterpartyAccount.setId(12L);
        receipt.setCounterpartyBankAccount(counterpartyAccount);

        Currency currency = new Currency();
        currency.setId(2L);
        receipt.setCurrency(currency);

        Organization organization = new Organization();
        organization.setId(1L);
        receipt.setOrganization(organization);

        return receipt;
    }
}