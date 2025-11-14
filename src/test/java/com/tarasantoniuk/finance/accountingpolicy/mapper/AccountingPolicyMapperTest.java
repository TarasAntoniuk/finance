package com.tarasantoniuk.finance.accountingpolicy.mapper;

import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.country.mapper.CountryMapperImpl;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.organization.entity.Organization;
import com.tarasantoniuk.finance.organization.mapper.OrganizationMapper;
import com.tarasantoniuk.finance.organization.mapper.OrganizationMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class AccountingPolicyMapperTest extends BaseIntegrationTest {


    @Autowired
    AccountingPolicyMapper accountingPolicyMapper;


    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(2L);
        requestDTO.setFiscalYearStartMonth(1);
        requestDTO.setDepreciationMethod("STRAIGHT_LINE");
        requestDTO.setInventoryValuationMethod("FIFO");
        requestDTO.setRevenueRecognitionMethod("ACCRUAL");
        requestDTO.setVatAccountingMethod("INVOICE");
        requestDTO.setIsActive(true);
        requestDTO.setNotes("Standard accounting policy for 2024");

        // When
        AccountingPolicy entity = accountingPolicyMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getOrganization()).isNotNull();
        assertThat(entity.getOrganization().getId()).isEqualTo(1L);
        assertThat(entity.getYear()).isEqualTo(2024);
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(2L);
        assertThat(entity.getFiscalYearStartMonth()).isEqualTo(1);
        assertThat(entity.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE");
        assertThat(entity.getInventoryValuationMethod()).isEqualTo("FIFO");
        assertThat(entity.getRevenueRecognitionMethod()).isEqualTo("ACCRUAL");
        assertThat(entity.getVatAccountingMethod()).isEqualTo("INVOICE");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getNotes()).isEqualTo("Standard accounting policy for 2024");
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(5L);
        requestDTO.setYear(2025);
        requestDTO.setCurrencyId(3L);
        requestDTO.setFiscalYearStartMonth(null);
        requestDTO.setDepreciationMethod(null);
        requestDTO.setInventoryValuationMethod(null);
        requestDTO.setRevenueRecognitionMethod(null);
        requestDTO.setVatAccountingMethod(null);
        requestDTO.setIsActive(null);
        requestDTO.setNotes(null);

        // When
        AccountingPolicy entity = accountingPolicyMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getOrganization().getId()).isEqualTo(5L);
        assertThat(entity.getYear()).isEqualTo(2025);
        assertThat(entity.getCurrency().getId()).isEqualTo(3L);
        assertThat(entity.getFiscalYearStartMonth()).isNull();
        assertThat(entity.getDepreciationMethod()).isNull();
        assertThat(entity.getInventoryValuationMethod()).isNull();
        assertThat(entity.getRevenueRecognitionMethod()).isNull();
        assertThat(entity.getVatAccountingMethod()).isNull();
        assertThat(entity.getIsActive()).isNull();
        assertThat(entity.getNotes()).isNull();
    }

    @Test
    void toEntity_shouldCreateOrganizationAndCurrencyWithOnlyIdSet() {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(10L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(20L);

        // When
        AccountingPolicy entity = accountingPolicyMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getOrganization()).isNotNull();
        assertThat(entity.getOrganization().getId()).isEqualTo(10L);
        assertThat(entity.getOrganization().getName()).isNull();

        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(20L);
        assertThat(entity.getCurrency().getCode()).isNull();
    }

    @Test
    void toEntity_shouldHandleDifferentFiscalYearStartMonths() {
        // Test January (1)
        AccountingPolicyRequestDTO jan = new AccountingPolicyRequestDTO();
        jan.setOrganizationId(1L);
        jan.setYear(2024);
        jan.setCurrencyId(1L);
        jan.setFiscalYearStartMonth(1);

        AccountingPolicy janEntity = accountingPolicyMapper.toEntity(jan);
        assertThat(janEntity.getFiscalYearStartMonth()).isEqualTo(1);

        // Test July (7)
        AccountingPolicyRequestDTO jul = new AccountingPolicyRequestDTO();
        jul.setOrganizationId(1L);
        jul.setYear(2024);
        jul.setCurrencyId(1L);
        jul.setFiscalYearStartMonth(7);

        AccountingPolicy julEntity = accountingPolicyMapper.toEntity(jul);
        assertThat(julEntity.getFiscalYearStartMonth()).isEqualTo(7);

        // Test December (12)
        AccountingPolicyRequestDTO dec = new AccountingPolicyRequestDTO();
        dec.setOrganizationId(1L);
        dec.setYear(2024);
        dec.setCurrencyId(1L);
        dec.setFiscalYearStartMonth(12);

        AccountingPolicy decEntity = accountingPolicyMapper.toEntity(dec);
        assertThat(decEntity.getFiscalYearStartMonth()).isEqualTo(12);
    }

    @Test
    void toEntity_shouldHandleDifferentAccountingMethods() {
        // Test different depreciation methods
        AccountingPolicyRequestDTO dto1 = new AccountingPolicyRequestDTO();
        dto1.setOrganizationId(1L);
        dto1.setYear(2024);
        dto1.setCurrencyId(1L);
        dto1.setDepreciationMethod("DECLINING_BALANCE");

        AccountingPolicy entity1 = accountingPolicyMapper.toEntity(dto1);
        assertThat(entity1.getDepreciationMethod()).isEqualTo("DECLINING_BALANCE");

        // Test different inventory methods
        AccountingPolicyRequestDTO dto2 = new AccountingPolicyRequestDTO();
        dto2.setOrganizationId(1L);
        dto2.setYear(2024);
        dto2.setCurrencyId(1L);
        dto2.setInventoryValuationMethod("WEIGHTED_AVERAGE");

        AccountingPolicy entity2 = accountingPolicyMapper.toEntity(dto2);
        assertThat(entity2.getInventoryValuationMethod()).isEqualTo("WEIGHTED_AVERAGE");

        // Test cash-based revenue recognition
        AccountingPolicyRequestDTO dto3 = new AccountingPolicyRequestDTO();
        dto3.setOrganizationId(1L);
        dto3.setYear(2024);
        dto3.setCurrencyId(1L);
        dto3.setRevenueRecognitionMethod("CASH");

        AccountingPolicy entity3 = accountingPolicyMapper.toEntity(dto3);
        assertThat(entity3.getRevenueRecognitionMethod()).isEqualTo("CASH");

        // Test payment-based VAT
        AccountingPolicyRequestDTO dto4 = new AccountingPolicyRequestDTO();
        dto4.setOrganizationId(1L);
        dto4.setYear(2024);
        dto4.setCurrencyId(1L);
        dto4.setVatAccountingMethod("PAYMENT");

        AccountingPolicy entity4 = accountingPolicyMapper.toEntity(dto4);
        assertThat(entity4.getVatAccountingMethod()).isEqualTo("PAYMENT");
    }

    @Test
    void toResponseDTO_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Country country = new Country();
        country.setId(1L);
        country.setName("Ukraine");
        country.setIsoCode("UKR");

        Organization organization = new Organization();
        organization.setId(10L);
        organization.setName("Tech Corp Ltd");
        organization.setRegistrationNumber("12345678");
        organization.setCountry(country);
        organization.setCreatedAt(now.minusDays(365));
        organization.setUpdatedAt(now.minusDays(180));

        Currency currency = new Currency();
        currency.setId(2L);
        currency.setCode("UAH");
        currency.setNumericCode("980");
        currency.setName("Ukrainian Hryvnia");
        currency.setSymbol("₴");
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        currency.setCreatedAt(now.minusDays(365));
        currency.setUpdatedAt(now.minusDays(180));

        AccountingPolicy entity = new AccountingPolicy();
        entity.setId(100L);
        entity.setOrganization(organization);
        entity.setYear(2024);
        entity.setCurrency(currency);
        entity.setFiscalYearStartMonth(1);
        entity.setDepreciationMethod("STRAIGHT_LINE");
        entity.setInventoryValuationMethod("FIFO");
        entity.setRevenueRecognitionMethod("ACCRUAL");
        entity.setVatAccountingMethod("INVOICE");
        entity.setIsActive(true);
        entity.setNotes("Main accounting policy");
        entity.setCreatedAt(now.minusDays(30));
        entity.setUpdatedAt(now.minusDays(5));

        // When
        AccountingPolicyResponseDTO responseDTO = accountingPolicyMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(100L);
        assertThat(responseDTO.getYear()).isEqualTo(2024);
        assertThat(responseDTO.getFiscalYearStartMonth()).isEqualTo(1);
        assertThat(responseDTO.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE");
        assertThat(responseDTO.getInventoryValuationMethod()).isEqualTo("FIFO");
        assertThat(responseDTO.getRevenueRecognitionMethod()).isEqualTo("ACCRUAL");
        assertThat(responseDTO.getVatAccountingMethod()).isEqualTo("INVOICE");
        assertThat(responseDTO.getIsActive()).isTrue();
        assertThat(responseDTO.getNotes()).isEqualTo("Main accounting policy");
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(30));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now.minusDays(5));

        // Check organization mapping
        assertThat(responseDTO.getOrganization()).isNotNull();
        assertThat(responseDTO.getOrganization().getId()).isEqualTo(10L);
        assertThat(responseDTO.getOrganization().getName()).isEqualTo("Tech Corp Ltd");
        assertThat(responseDTO.getOrganization().getRegistrationNumber()).isEqualTo("12345678");

        // Check nested country in organization
        assertThat(responseDTO.getOrganization().getCountry()).isNotNull();
        assertThat(responseDTO.getOrganization().getCountry().getName()).isEqualTo("Ukraine");
        assertThat(responseDTO.getOrganization().getCountry().getIsoCode()).isEqualTo("UKR");

        // Check currency mapping
        assertThat(responseDTO.getCurrency()).isNotNull();
        assertThat(responseDTO.getCurrency().getId()).isEqualTo(2L);
        assertThat(responseDTO.getCurrency().getCode()).isEqualTo("UAH");
        assertThat(responseDTO.getCurrency().getName()).isEqualTo("Ukrainian Hryvnia");
        assertThat(responseDTO.getCurrency().getSymbol()).isEqualTo("₴");
    }

    @Test
    void toResponseDTO_shouldHandleNullOptionalFields() {
        // Given
        Organization organization = new Organization();
        organization.setId(5L);
        organization.setName("Minimal Org");

        Currency currency = new Currency();
        currency.setId(3L);
        currency.setCode("USD");

        AccountingPolicy entity = new AccountingPolicy();
        entity.setId(200L);
        entity.setOrganization(organization);
        entity.setYear(2025);
        entity.setCurrency(currency);
        entity.setFiscalYearStartMonth(null);
        entity.setDepreciationMethod(null);
        entity.setInventoryValuationMethod(null);
        entity.setRevenueRecognitionMethod(null);
        entity.setVatAccountingMethod(null);
        entity.setIsActive(false);
        entity.setNotes(null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        AccountingPolicyResponseDTO responseDTO = accountingPolicyMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getYear()).isEqualTo(2025);
        assertThat(responseDTO.getFiscalYearStartMonth()).isNull();
        assertThat(responseDTO.getDepreciationMethod()).isNull();
        assertThat(responseDTO.getInventoryValuationMethod()).isNull();
        assertThat(responseDTO.getRevenueRecognitionMethod()).isNull();
        assertThat(responseDTO.getVatAccountingMethod()).isNull();
        assertThat(responseDTO.getIsActive()).isFalse();
        assertThat(responseDTO.getNotes()).isNull();
        assertThat(responseDTO.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseDTOList_shouldMapAllEntities() {
        // Given
        Organization org1 = new Organization();
        org1.setId(1L);
        org1.setName("Company A");

        Organization org2 = new Organization();
        org2.setId(2L);
        org2.setName("Company B");

        Currency uah = new Currency();
        uah.setId(1L);
        uah.setCode("UAH");

        Currency usd = new Currency();
        usd.setId(2L);
        usd.setCode("USD");

        AccountingPolicy policy1 = new AccountingPolicy();
        policy1.setId(1L);
        policy1.setOrganization(org1);
        policy1.setYear(2024);
        policy1.setCurrency(uah);
        policy1.setDepreciationMethod("STRAIGHT_LINE");

        AccountingPolicy policy2 = new AccountingPolicy();
        policy2.setId(2L);
        policy2.setOrganization(org2);
        policy2.setYear(2024);
        policy2.setCurrency(usd);
        policy2.setDepreciationMethod("DECLINING_BALANCE");

        List<AccountingPolicy> policies = Arrays.asList(policy1, policy2);

        // When
        List<AccountingPolicyResponseDTO> responseDTOs = accountingPolicyMapper.toResponseDTOList(policies);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.getFirst().getId()).isEqualTo(1L);
        assertThat(responseDTOs.getFirst().getOrganization().getName()).isEqualTo("Company A");
        assertThat(responseDTOs.getFirst().getCurrency().getCode()).isEqualTo("UAH");
        assertThat(responseDTOs.getFirst().getDepreciationMethod()).isEqualTo("STRAIGHT_LINE");

        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getOrganization().getName()).isEqualTo("Company B");
        assertThat(responseDTOs.get(1).getCurrency().getCode()).isEqualTo("USD");
        assertThat(responseDTOs.get(1).getDepreciationMethod()).isEqualTo("DECLINING_BALANCE");
    }

    @Test
    void toResponseDTOList_shouldHandleEmptyList() {
        // Given
        List<AccountingPolicy> policies = List.of();

        // When
        List<AccountingPolicyResponseDTO> responseDTOs = accountingPolicyMapper.toResponseDTOList(policies);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntityFromDTO_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(90);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(45);

        Organization oldOrg = new Organization();
        oldOrg.setId(1L);
        oldOrg.setName("Old Organization");

        Currency oldCurrency = new Currency();
        oldCurrency.setId(1L);
        oldCurrency.setCode("UAH");

        AccountingPolicy existingEntity = new AccountingPolicy();
        existingEntity.setId(500L);
        existingEntity.setOrganization(oldOrg);
        existingEntity.setYear(2023);
        existingEntity.setCurrency(oldCurrency);
        existingEntity.setFiscalYearStartMonth(1);
        existingEntity.setDepreciationMethod("STRAIGHT_LINE");
        existingEntity.setInventoryValuationMethod("FIFO");
        existingEntity.setRevenueRecognitionMethod("ACCRUAL");
        existingEntity.setVatAccountingMethod("INVOICE");
        existingEntity.setIsActive(false);
        existingEntity.setNotes("Old notes");
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        AccountingPolicyRequestDTO updateDTO = new AccountingPolicyRequestDTO();
        updateDTO.setOrganizationId(99L);
        updateDTO.setYear(2024);
        updateDTO.setCurrencyId(88L);
        updateDTO.setFiscalYearStartMonth(7);
        updateDTO.setDepreciationMethod("DECLINING_BALANCE");
        updateDTO.setInventoryValuationMethod("WEIGHTED_AVERAGE");
        updateDTO.setRevenueRecognitionMethod("CASH");
        updateDTO.setVatAccountingMethod("PAYMENT");
        updateDTO.setIsActive(true);
        updateDTO.setNotes("New notes");

        // When
        accountingPolicyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getOrganization()).isNotNull();
        assertThat(existingEntity.getOrganization().getId()).isEqualTo(99L);
        assertThat(existingEntity.getYear()).isEqualTo(2024);
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(88L);
        assertThat(existingEntity.getFiscalYearStartMonth()).isEqualTo(7);
        assertThat(existingEntity.getDepreciationMethod()).isEqualTo("DECLINING_BALANCE");
        assertThat(existingEntity.getInventoryValuationMethod()).isEqualTo("WEIGHTED_AVERAGE");
        assertThat(existingEntity.getRevenueRecognitionMethod()).isEqualTo("CASH");
        assertThat(existingEntity.getVatAccountingMethod()).isEqualTo("PAYMENT");
        assertThat(existingEntity.getIsActive()).isTrue();
        assertThat(existingEntity.getNotes()).isEqualTo("New notes");

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(500L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityFromDTO_shouldIgnoreNullValues() {
        // Given
        Organization existingOrg = new Organization();
        existingOrg.setId(5L);

        Currency existingCurrency = new Currency();
        existingCurrency.setId(3L);

        AccountingPolicy existingEntity = new AccountingPolicy();
        existingEntity.setId(600L);
        existingEntity.setOrganization(existingOrg);
        existingEntity.setYear(2024);
        existingEntity.setCurrency(existingCurrency);
        existingEntity.setFiscalYearStartMonth(1);
        existingEntity.setDepreciationMethod("STRAIGHT_LINE");
        existingEntity.setInventoryValuationMethod("FIFO");
        existingEntity.setRevenueRecognitionMethod("ACCRUAL");
        existingEntity.setVatAccountingMethod("INVOICE");
        existingEntity.setIsActive(true);
        existingEntity.setNotes("Existing notes");

        AccountingPolicyRequestDTO updateDTO = new AccountingPolicyRequestDTO();
        updateDTO.setOrganizationId(null); // ignore
        updateDTO.setYear(2025); // update
        updateDTO.setCurrencyId(null); // ignore
        updateDTO.setFiscalYearStartMonth(null); // ignore
        updateDTO.setDepreciationMethod(null); // ignore
        updateDTO.setInventoryValuationMethod("LIFO"); // update
        updateDTO.setRevenueRecognitionMethod(null); // ignore
        updateDTO.setVatAccountingMethod(null); // ignore
        updateDTO.setIsActive(null); // ignore
        updateDTO.setNotes(null); // ignore

        // When
        accountingPolicyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getOrganization().getId()).isEqualTo(5L); // unchanged
        assertThat(existingEntity.getYear()).isEqualTo(2025); // updated
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(3L); // unchanged
        assertThat(existingEntity.getFiscalYearStartMonth()).isEqualTo(1); // unchanged
        assertThat(existingEntity.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE"); // unchanged
        assertThat(existingEntity.getInventoryValuationMethod()).isEqualTo("LIFO"); // updated
        assertThat(existingEntity.getRevenueRecognitionMethod()).isEqualTo("ACCRUAL"); // unchanged
        assertThat(existingEntity.getVatAccountingMethod()).isEqualTo("INVOICE"); // unchanged
        assertThat(existingEntity.getIsActive()).isTrue(); // unchanged
        assertThat(existingEntity.getNotes()).isEqualTo("Existing notes"); // unchanged
    }

    @Test
    void updateEntityFromDTO_shouldChangeOrganizationAndCurrency() {
        // Given
        Organization oldOrg = new Organization();
        oldOrg.setId(1L);

        Currency oldCurrency = new Currency();
        oldCurrency.setId(1L);

        AccountingPolicy existingEntity = new AccountingPolicy();
        existingEntity.setId(700L);
        existingEntity.setOrganization(oldOrg);
        existingEntity.setYear(2024);
        existingEntity.setCurrency(oldCurrency);

        AccountingPolicyRequestDTO updateDTO = new AccountingPolicyRequestDTO();
        updateDTO.setOrganizationId(10L); // change
        updateDTO.setYear(2024);
        updateDTO.setCurrencyId(20L); // change

        // When
        accountingPolicyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getOrganization().getId()).isEqualTo(10L);
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(20L);
    }

    @Test
    void updateEntityFromDTO_shouldPartiallyUpdateMultipleFields() {
        // Given
        Organization existingOrg = new Organization();
        existingOrg.setId(10L);

        Currency existingCurrency = new Currency();
        existingCurrency.setId(5L);

        AccountingPolicy existingEntity = new AccountingPolicy();
        existingEntity.setId(800L);
        existingEntity.setOrganization(existingOrg);
        existingEntity.setYear(2023);
        existingEntity.setCurrency(existingCurrency);
        existingEntity.setFiscalYearStartMonth(1);
        existingEntity.setDepreciationMethod("STRAIGHT_LINE");
        existingEntity.setInventoryValuationMethod("FIFO");
        existingEntity.setRevenueRecognitionMethod("ACCRUAL");
        existingEntity.setVatAccountingMethod("INVOICE");
        existingEntity.setIsActive(true);
        existingEntity.setNotes("Old notes");

        AccountingPolicyRequestDTO updateDTO = new AccountingPolicyRequestDTO();
        updateDTO.setOrganizationId(10L); // keep same
        updateDTO.setYear(2024); // update
        updateDTO.setCurrencyId(null); // ignore
        updateDTO.setFiscalYearStartMonth(7); // update
        updateDTO.setDepreciationMethod(null); // ignore
        updateDTO.setInventoryValuationMethod("WEIGHTED_AVERAGE"); // update
        updateDTO.setRevenueRecognitionMethod(null); // ignore
        updateDTO.setVatAccountingMethod("PAYMENT"); // update
        updateDTO.setIsActive(false); // update
        updateDTO.setNotes(null); // ignore

        // When
        accountingPolicyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getOrganization().getId()).isEqualTo(10L);
        assertThat(existingEntity.getYear()).isEqualTo(2024);
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(5L); // unchanged
        assertThat(existingEntity.getFiscalYearStartMonth()).isEqualTo(7);
        assertThat(existingEntity.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE"); // unchanged
        assertThat(existingEntity.getInventoryValuationMethod()).isEqualTo("WEIGHTED_AVERAGE");
        assertThat(existingEntity.getRevenueRecognitionMethod()).isEqualTo("ACCRUAL"); // unchanged
        assertThat(existingEntity.getVatAccountingMethod()).isEqualTo("PAYMENT");
        assertThat(existingEntity.getIsActive()).isFalse();
        assertThat(existingEntity.getNotes()).isEqualTo("Old notes"); // unchanged
    }

    @Test
    void toEntity_shouldHandleDifferentYears() {
        // Test past year
        AccountingPolicyRequestDTO past = new AccountingPolicyRequestDTO();
        past.setOrganizationId(1L);
        past.setYear(2020);
        past.setCurrencyId(1L);

        AccountingPolicy pastEntity = accountingPolicyMapper.toEntity(past);
        assertThat(pastEntity.getYear()).isEqualTo(2020);

        // Test current year
        AccountingPolicyRequestDTO current = new AccountingPolicyRequestDTO();
        current.setOrganizationId(1L);
        current.setYear(2024);
        current.setCurrencyId(1L);

        AccountingPolicy currentEntity = accountingPolicyMapper.toEntity(current);
        assertThat(currentEntity.getYear()).isEqualTo(2024);

        // Test future year
        AccountingPolicyRequestDTO future = new AccountingPolicyRequestDTO();
        future.setOrganizationId(1L);
        future.setYear(2030);
        future.setCurrencyId(1L);

        AccountingPolicy futureEntity = accountingPolicyMapper.toEntity(future);
        assertThat(futureEntity.getYear()).isEqualTo(2030);
    }
}