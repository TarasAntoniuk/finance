package com.tarasantoniuk.finance.bank.mapper;

import com.tarasantoniuk.finance.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.country.entity.Country;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankMapperTest extends BaseIntegrationTest {

    @Autowired
    BankMapper bankMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("PrivatBank");
        requestDTO.setSwiftCode("PBANUA2X");
        requestDTO.setCountryId(1L);
        requestDTO.setCounterpartyId(5L);
        requestDTO.setAddress("1 Hrushevskoho St, Kyiv");
        requestDTO.setPhoneNumber("+380443639999");
        requestDTO.setWebsite("www.privatbank.ua");
        requestDTO.setIsActive(true);

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getName()).isEqualTo("PrivatBank");
        assertThat(entity.getSwiftCode()).isEqualTo("PBANUA2X");
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(1L);
        assertThat(entity.getCounterparty()).isNotNull();
        assertThat(entity.getCounterparty().getId()).isEqualTo(5L);
        assertThat(entity.getAddress()).isEqualTo("1 Hrushevskoho St, Kyiv");
        assertThat(entity.getPhoneNumber()).isEqualTo("+380443639999");
        assertThat(entity.getWebsite()).isEqualTo("www.privatbank.ua");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("Monobank");
        requestDTO.setSwiftCode("MBNKUA2X");
        requestDTO.setCountryId(2L);
        requestDTO.setCounterpartyId(null);
        requestDTO.setAddress(null);
        requestDTO.setPhoneNumber(null);
        requestDTO.setWebsite(null);
        requestDTO.setIsActive(null);

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Monobank");
        assertThat(entity.getSwiftCode()).isEqualTo("MBNKUA2X");
        assertThat(entity.getCountry().getId()).isEqualTo(2L);
        assertThat(entity.getCounterparty()).isNull();
        assertThat(entity.getAddress()).isNull();
        assertThat(entity.getPhoneNumber()).isNull();
        assertThat(entity.getWebsite()).isNull();
        assertThat(entity.getIsActive()).isNull();
    }

    @Test
    void toEntity_shouldCreateCountryAndCounterpartyWithOnlyIdSet() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("Test Bank");
        requestDTO.setSwiftCode("TESTUA2X");
        requestDTO.setCountryId(10L);
        requestDTO.setCounterpartyId(20L);

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(10L);
        assertThat(entity.getCountry().getName()).isNull();

        assertThat(entity.getCounterparty()).isNotNull();
        assertThat(entity.getCounterparty().getId()).isEqualTo(20L);
        assertThat(entity.getCounterparty().getName()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Country country = new Country();
        country.setId(1L);
        country.setName("Ukraine");
        country.setIsoCode("UKR");

        Counterparty counterparty = new Counterparty();
        counterparty.setId(5L);
        counterparty.setName("PrivatBank Counterparty");
        counterparty.setTaxNumber("REG123");

        Bank entity = new Bank();
        entity.setId(100L);
        entity.setName("PrivatBank");
        entity.setSwiftCode("PBANUA2X");
        entity.setCountry(country);
        entity.setCounterparty(counterparty);
        entity.setAddress("1 Hrushevskoho St, Kyiv");
        entity.setPhoneNumber("+380443639999");
        entity.setWebsite("www.privatbank.ua");
        entity.setIsActive(true);
        entity.setCreatedAt(now.minusDays(30));
        entity.setUpdatedAt(now.minusDays(5));

        // When
        BankResponseDTO responseDTO = bankMapper.toResponse(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(100L);
        assertThat(responseDTO.getName()).isEqualTo("PrivatBank");
        assertThat(responseDTO.getSwiftCode()).isEqualTo("PBANUA2X");
        assertThat(responseDTO.getAddress()).isEqualTo("1 Hrushevskoho St, Kyiv");
        assertThat(responseDTO.getPhoneNumber()).isEqualTo("+380443639999");
        assertThat(responseDTO.getWebsite()).isEqualTo("www.privatbank.ua");
        assertThat(responseDTO.getIsActive()).isTrue();
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(30));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now.minusDays(5));

        // Check country mapping
        assertThat(responseDTO.getCountry()).isNotNull();
        assertThat(responseDTO.getCountry().getId()).isEqualTo(1L);
        assertThat(responseDTO.getCountry().getName()).isEqualTo("Ukraine");
        assertThat(responseDTO.getCountry().getIsoCode()).isEqualTo("UKR");

        // Check counterparty mapping
        assertThat(responseDTO.getCounterparty()).isNotNull();
        assertThat(responseDTO.getCounterparty().getId()).isEqualTo(5L);
        assertThat(responseDTO.getCounterparty().getName()).isEqualTo("PrivatBank Counterparty");
        assertThat(responseDTO.getCounterparty().getTaxNumber()).isEqualTo("REG123");
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        // Given
        Country country = new Country();
        country.setId(2L);
        country.setName("USA");

        Bank entity = new Bank();
        entity.setId(200L);
        entity.setName("Test Bank");
        entity.setSwiftCode("TESTUS33");
        entity.setCountry(country);
        entity.setCounterparty(null);
        entity.setAddress(null);
        entity.setPhoneNumber(null);
        entity.setWebsite(null);
        entity.setIsActive(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        BankResponseDTO responseDTO = bankMapper.toResponse(entity);

        // Then
        assertThat(responseDTO.getName()).isEqualTo("Test Bank");
        assertThat(responseDTO.getSwiftCode()).isEqualTo("TESTUS33");
        assertThat(responseDTO.getCounterparty()).isNull();
        assertThat(responseDTO.getAddress()).isNull();
        assertThat(responseDTO.getPhoneNumber()).isNull();
        assertThat(responseDTO.getWebsite()).isNull();
        assertThat(responseDTO.getIsActive()).isFalse();
        assertThat(responseDTO.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseList_shouldMapAllEntities() {
        // Given
        Country ukraine = new Country();
        ukraine.setId(1L);
        ukraine.setName("Ukraine");

        Country usa = new Country();
        usa.setId(2L);
        usa.setName("USA");

        Bank bank1 = new Bank();
        bank1.setId(1L);
        bank1.setName("PrivatBank");
        bank1.setSwiftCode("PBANUA2X");
        bank1.setCountry(ukraine);
        bank1.setIsActive(true);

        Bank bank2 = new Bank();
        bank2.setId(2L);
        bank2.setName("JPMorgan Chase");
        bank2.setSwiftCode("CHASUS33");
        bank2.setCountry(usa);
        bank2.setIsActive(true);

        List<Bank> banks = Arrays.asList(bank1, bank2);

        // When
        List<BankResponseDTO> responseDTOs = bankMapper.toResponseList(banks);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(0).getName()).isEqualTo("PrivatBank");
        assertThat(responseDTOs.get(0).getSwiftCode()).isEqualTo("PBANUA2X");
        assertThat(responseDTOs.get(0).getCountry().getName()).isEqualTo("Ukraine");

        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getName()).isEqualTo("JPMorgan Chase");
        assertThat(responseDTOs.get(1).getSwiftCode()).isEqualTo("CHASUS33");
        assertThat(responseDTOs.get(1).getCountry().getName()).isEqualTo("USA");
    }

    @Test
    void toResponseList_shouldHandleEmptyList() {
        // Given
        List<Bank> banks = List.of();

        // When
        List<BankResponseDTO> responseDTOs = bankMapper.toResponseList(banks);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntity_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(90);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(45);

        Country oldCountry = new Country();
        oldCountry.setId(1L);
        oldCountry.setName("Ukraine");

        Counterparty oldCounterparty = new Counterparty();
        oldCounterparty.setId(5L);

        Bank existingEntity = new Bank();
        existingEntity.setId(500L);
        existingEntity.setName("Old Bank");
        existingEntity.setSwiftCode("OLDBANK2X");
        existingEntity.setCountry(oldCountry);
        existingEntity.setCounterparty(oldCounterparty);
        existingEntity.setAddress("Old Address");
        existingEntity.setPhoneNumber("+380441111111");
        existingEntity.setWebsite("www.oldbank.com");
        existingEntity.setIsActive(false);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        BankRequestDTO updateDTO = new BankRequestDTO();
        updateDTO.setName("New Bank");
        updateDTO.setSwiftCode("NEWBANK2X");
        updateDTO.setCountryId(99L);
        updateDTO.setCounterpartyId(88L);
        updateDTO.setAddress("New Address");
        updateDTO.setPhoneNumber("+380442222222");
        updateDTO.setWebsite("www.newbank.com");
        updateDTO.setIsActive(true);

        // When
        bankMapper.updateEntity(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getName()).isEqualTo("New Bank");
        assertThat(existingEntity.getSwiftCode()).isEqualTo("NEWBANK2X");
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(99L);
        assertThat(existingEntity.getCounterparty()).isNotNull();
        assertThat(existingEntity.getCounterparty().getId()).isEqualTo(88L);
        assertThat(existingEntity.getAddress()).isEqualTo("New Address");
        assertThat(existingEntity.getPhoneNumber()).isEqualTo("+380442222222");
        assertThat(existingEntity.getWebsite()).isEqualTo("www.newbank.com");
        assertThat(existingEntity.getIsActive()).isTrue();

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(500L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntity_shouldIgnoreNullValues() {
        // Given
        Country existingCountry = new Country();
        existingCountry.setId(5L);

        Counterparty existingCounterparty = new Counterparty();
        existingCounterparty.setId(10L);

        Bank existingEntity = new Bank();
        existingEntity.setId(600L);
        existingEntity.setName("Existing Bank");
        existingEntity.setSwiftCode("EXISTUA2X");
        existingEntity.setCountry(existingCountry);
        existingEntity.setCounterparty(existingCounterparty);
        existingEntity.setAddress("Existing Address");
        existingEntity.setPhoneNumber("+380443333333");
        existingEntity.setWebsite("www.existing.com");
        existingEntity.setIsActive(true);

        BankRequestDTO updateDTO = new BankRequestDTO();
        updateDTO.setName("Updated Bank");
        updateDTO.setSwiftCode(null); // ignore
        updateDTO.setCountryId(null); // ignore
        updateDTO.setCounterpartyId(null); // ignore
        updateDTO.setAddress(null); // ignore
        updateDTO.setPhoneNumber("+380444444444"); // update
        updateDTO.setWebsite(null); // ignore
        updateDTO.setIsActive(null); // ignore

        // When
        bankMapper.updateEntity(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Updated Bank"); // updated
        assertThat(existingEntity.getSwiftCode()).isEqualTo("EXISTUA2X"); // unchanged
        assertThat(existingEntity.getCountry().getId()).isEqualTo(5L); // unchanged
        assertThat(existingEntity.getCounterparty().getId()).isEqualTo(10L); // unchanged
        assertThat(existingEntity.getAddress()).isEqualTo("Existing Address"); // unchanged
        assertThat(existingEntity.getPhoneNumber()).isEqualTo("+380444444444"); // updated
        assertThat(existingEntity.getWebsite()).isEqualTo("www.existing.com"); // unchanged
        assertThat(existingEntity.getIsActive()).isTrue(); // unchanged
    }

    @Test
    void updateEntity_shouldChangeCountryAndCounterparty() {
        // Given
        Country oldCountry = new Country();
        oldCountry.setId(1L);

        Counterparty oldCounterparty = new Counterparty();
        oldCounterparty.setId(1L);

        Bank existingEntity = new Bank();
        existingEntity.setId(700L);
        existingEntity.setName("Bank");
        existingEntity.setSwiftCode("BANKUA2X");
        existingEntity.setCountry(oldCountry);
        existingEntity.setCounterparty(oldCounterparty);

        BankRequestDTO updateDTO = new BankRequestDTO();
        updateDTO.setName("Bank");
        updateDTO.setSwiftCode("BANKUA2X");
        updateDTO.setCountryId(10L); // change
        updateDTO.setCounterpartyId(20L); // change

        // When
        bankMapper.updateEntity(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getCountry().getId()).isEqualTo(10L);
        assertThat(existingEntity.getCounterparty().getId()).isEqualTo(20L);
    }

    @Test
    void toEntity_shouldHandleDifferentSwiftCodes() {
        // Test Ukrainian bank
        BankRequestDTO ukrainian = new BankRequestDTO();
        ukrainian.setName("Ukrainian Bank");
        ukrainian.setSwiftCode("PBANUA2X");
        ukrainian.setCountryId(1L);

        Bank ukrainianEntity = bankMapper.toEntity(ukrainian);
        assertThat(ukrainianEntity.getSwiftCode()).isEqualTo("PBANUA2X");

        // Test US bank
        BankRequestDTO us = new BankRequestDTO();
        us.setName("US Bank");
        us.setSwiftCode("CHASUS33");
        us.setCountryId(2L);

        Bank usEntity = bankMapper.toEntity(us);
        assertThat(usEntity.getSwiftCode()).isEqualTo("CHASUS33");

        // Test German bank
        BankRequestDTO german = new BankRequestDTO();
        german.setName("German Bank");
        german.setSwiftCode("DEUTDEFF");
        german.setCountryId(3L);

        Bank germanEntity = bankMapper.toEntity(german);
        assertThat(germanEntity.getSwiftCode()).isEqualTo("DEUTDEFF");
    }

    @Test
    void toEntity_shouldHandleNullCountryId() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("Test Bank");
        requestDTO.setSwiftCode("TESTUA2X");
        requestDTO.setCountryId(null); // null countryId
        requestDTO.setCounterpartyId(5L);

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCountry()).isNull();
        assertThat(entity.getCounterparty()).isNotNull();
        assertThat(entity.getCounterparty().getId()).isEqualTo(5L);
    }

    @Test
    void toEntity_shouldHandleNullCounterpartyId() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("Test Bank");
        requestDTO.setSwiftCode("TESTUA2X");
        requestDTO.setCountryId(1L);
        requestDTO.setCounterpartyId(null); // null counterpartyId

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(1L);
        assertThat(entity.getCounterparty()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullCountryIdAndCounterpartyId() {
        // Given
        BankRequestDTO requestDTO = new BankRequestDTO();
        requestDTO.setName("Test Bank");
        requestDTO.setSwiftCode("TESTUA2X");
        requestDTO.setCountryId(null); // null countryId
        requestDTO.setCounterpartyId(null); // null counterpartyId

        // When
        Bank entity = bankMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCountry()).isNull();
        assertThat(entity.getCounterparty()).isNull();
    }
}