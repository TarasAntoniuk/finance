package com.tarasantoniuk.finance.banking.bankaccount.mapper;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDto;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankAccountMapperTest extends BaseIntegrationTest {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromrequestDto() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA213223130000026007233566001");
        requestDto.setHolderType(AccountHolderType.ORGANIZATION);
        requestDto.setHolderId(10L);
        requestDto.setBankId(1L);
        requestDto.setCurrencyId(2L);
        requestDto.setAccountName("Main UAH Account");
        requestDto.setStatus(AccountStatus.ACTIVE);
        requestDto.setIsDefault(true);

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getAccountNumber()).isEqualTo("UA213223130000026007233566001");
        assertThat(entity.getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
        assertThat(entity.getHolderId()).isEqualTo(10L);
        assertThat(entity.getBank()).isNotNull();
        assertThat(entity.getBank().getId()).isEqualTo(1L);
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(2L);
        assertThat(entity.getAccountName()).isEqualTo("Main UAH Account");
        assertThat(entity.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(entity.getIsDefault()).isTrue();
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA123456789012345678901234567");
        requestDto.setHolderType(AccountHolderType.COUNTERPARTY);
        requestDto.setHolderId(5L);
        requestDto.setBankId(3L);
        requestDto.setCurrencyId(1L);
        requestDto.setAccountName(null);
        requestDto.setStatus(null);
        requestDto.setIsDefault(null);

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getAccountNumber()).isEqualTo("UA123456789012345678901234567");
        assertThat(entity.getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY);
        assertThat(entity.getHolderId()).isEqualTo(5L);
        assertThat(entity.getBank().getId()).isEqualTo(3L);
        assertThat(entity.getCurrency().getId()).isEqualTo(1L);
        assertThat(entity.getAccountName()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getIsDefault()).isNull();
    }

    @Test
    void toEntity_shouldCreateBankAndCurrencyWithOnlyIdSet() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA111111111111111111111111111");
        requestDto.setHolderType(AccountHolderType.ORGANIZATION);
        requestDto.setHolderId(1L);
        requestDto.setBankId(10L);
        requestDto.setCurrencyId(20L);

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getBank()).isNotNull();
        assertThat(entity.getBank().getId()).isEqualTo(10L);
        assertThat(entity.getBank().getName()).isNull();

        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(20L);
        assertThat(entity.getCurrency().getCode()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");

        Currency currency = new Currency();
        currency.setId(2L);
        currency.setCode("UAH");
        currency.setName("Ukrainian Hryvnia");
        currency.setSymbol("₴");

        BankAccount entity = new BankAccount();
        entity.setId(100L);
        entity.setAccountNumber("UA213223130000026007233566001");
        entity.setHolderType(AccountHolderType.ORGANIZATION);
        entity.setHolderId(10L);
        entity.setBank(bank);
        entity.setCurrency(currency);
        entity.setAccountName("Main UAH Account");
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setIsDefault(true);
        entity.setCreatedAt(now.minusDays(30));
        entity.setUpdatedAt(now.minusDays(5));

        // When
        BankAccountResponseDto responseDTO = bankAccountMapper.toResponse(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(100L);
        assertThat(responseDTO.getAccountNumber()).isEqualTo("UA213223130000026007233566001");
        assertThat(responseDTO.getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
        assertThat(responseDTO.getHolderId()).isEqualTo(10L);
        assertThat(responseDTO.getAccountName()).isEqualTo("Main UAH Account");
        assertThat(responseDTO.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(responseDTO.getIsDefault()).isTrue();
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(30));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now.minusDays(5));

        // Check bank mapping
        assertThat(responseDTO.getBank()).isNotNull();
        assertThat(responseDTO.getBank().getId()).isEqualTo(1L);
        assertThat(responseDTO.getBank().getName()).isEqualTo("PrivatBank");
        assertThat(responseDTO.getBank().getSwiftCode()).isEqualTo("PBANUA2X");

        // Check currency mapping
        assertThat(responseDTO.getCurrency()).isNotNull();
        assertThat(responseDTO.getCurrency().getId()).isEqualTo(2L);
        assertThat(responseDTO.getCurrency().getCode()).isEqualTo("UAH");
        assertThat(responseDTO.getCurrency().getName()).isEqualTo("Ukrainian Hryvnia");
        assertThat(responseDTO.getCurrency().getSymbol()).isEqualTo("₴");
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        // Given
        Bank bank = new Bank();
        bank.setId(2L);
        bank.setName("Monobank");

        Currency currency = new Currency();
        currency.setId(1L);
        currency.setCode("USD");

        BankAccount entity = new BankAccount();
        entity.setId(200L);
        entity.setAccountNumber("UA987654321098765432109876543");
        entity.setHolderType(AccountHolderType.COUNTERPARTY);
        entity.setHolderId(5L);
        entity.setBank(bank);
        entity.setCurrency(currency);
        entity.setAccountName(null);
        entity.setStatus(AccountStatus.INACTIVE);
        entity.setIsDefault(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        BankAccountResponseDto responseDTO = bankAccountMapper.toResponse(entity);

        // Then
        assertThat(responseDTO.getAccountNumber()).isEqualTo("UA987654321098765432109876543");
        assertThat(responseDTO.getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY);
        assertThat(responseDTO.getAccountName()).isNull();
        assertThat(responseDTO.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(responseDTO.getIsDefault()).isFalse();
        assertThat(responseDTO.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseList_shouldMapAllEntities() {
        // Given
        Bank bank1 = new Bank();
        bank1.setId(1L);
        bank1.setName("PrivatBank");

        Bank bank2 = new Bank();
        bank2.setId(2L);
        bank2.setName("Monobank");

        Currency uah = new Currency();
        uah.setId(1L);
        uah.setCode("UAH");

        Currency usd = new Currency();
        usd.setId(2L);
        usd.setCode("USD");

        BankAccount account1 = new BankAccount();
        account1.setId(1L);
        account1.setAccountNumber("UA111111111111111111111111111");
        account1.setHolderType(AccountHolderType.ORGANIZATION);
        account1.setHolderId(1L);
        account1.setBank(bank1);
        account1.setCurrency(uah);
        account1.setStatus(AccountStatus.ACTIVE);

        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        account2.setAccountNumber("UA222222222222222222222222222");
        account2.setHolderType(AccountHolderType.COUNTERPARTY);
        account2.setHolderId(2L);
        account2.setBank(bank2);
        account2.setCurrency(usd);
        account2.setStatus(AccountStatus.ACTIVE);

        List<BankAccount> accounts = Arrays.asList(account1, account2);

        // When
        List<BankAccountResponseDto> responseDTOs = bankAccountMapper.toResponseList(accounts);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(0).getAccountNumber()).isEqualTo("UA111111111111111111111111111");
        assertThat(responseDTOs.get(0).getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
        assertThat(responseDTOs.get(0).getBank().getName()).isEqualTo("PrivatBank");
        assertThat(responseDTOs.get(0).getCurrency().getCode()).isEqualTo("UAH");

        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getAccountNumber()).isEqualTo("UA222222222222222222222222222");
        assertThat(responseDTOs.get(1).getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY);
        assertThat(responseDTOs.get(1).getBank().getName()).isEqualTo("Monobank");
        assertThat(responseDTOs.get(1).getCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    void toResponseList_shouldHandleEmptyList() {
        // Given
        List<BankAccount> accounts = List.of();

        // When
        List<BankAccountResponseDto> responseDTOs = bankAccountMapper.toResponseList(accounts);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntity_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(90);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(45);

        Bank oldBank = new Bank();
        oldBank.setId(1L);
        oldBank.setName("Old Bank");

        Currency oldCurrency = new Currency();
        oldCurrency.setId(1L);
        oldCurrency.setCode("UAH");

        BankAccount existingEntity = new BankAccount();
        existingEntity.setId(500L);
        existingEntity.setAccountNumber("UA111111111111111111111111111");
        existingEntity.setHolderType(AccountHolderType.ORGANIZATION);
        existingEntity.setHolderId(1L);
        existingEntity.setBank(oldBank);
        existingEntity.setCurrency(oldCurrency);
        existingEntity.setAccountName("Old Account");
        existingEntity.setStatus(AccountStatus.ACTIVE);
        existingEntity.setIsDefault(false);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        BankAccountRequestDto updateDTO = new BankAccountRequestDto();
        updateDTO.setAccountNumber("UA999999999999999999999999999");
        updateDTO.setHolderType(AccountHolderType.COUNTERPARTY);
        updateDTO.setHolderId(99L);
        updateDTO.setBankId(88L);
        updateDTO.setCurrencyId(77L);
        updateDTO.setAccountName("New Account");
        updateDTO.setStatus(AccountStatus.INACTIVE);
        updateDTO.setIsDefault(true);

        // When
        bankAccountMapper.updateEntity(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getAccountNumber()).isEqualTo("UA999999999999999999999999999");
        assertThat(existingEntity.getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY);
        assertThat(existingEntity.getHolderId()).isEqualTo(99L);
        assertThat(existingEntity.getBank()).isNotNull();
        assertThat(existingEntity.getBank().getId()).isEqualTo(88L);
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(77L);
        assertThat(existingEntity.getAccountName()).isEqualTo("New Account");
        assertThat(existingEntity.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(existingEntity.getIsDefault()).isTrue();

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(500L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntity_shouldIgnoreNullValues() {
        // Given
        Bank existingBank = new Bank();
        existingBank.setId(5L);

        Currency existingCurrency = new Currency();
        existingCurrency.setId(3L);

        BankAccount existingEntity = new BankAccount();
        existingEntity.setId(600L);
        existingEntity.setAccountNumber("UA123456789012345678901234567");
        existingEntity.setHolderType(AccountHolderType.ORGANIZATION);
        existingEntity.setHolderId(10L);
        existingEntity.setBank(existingBank);
        existingEntity.setCurrency(existingCurrency);
        existingEntity.setAccountName("Existing Account");
        existingEntity.setStatus(AccountStatus.ACTIVE);
        existingEntity.setIsDefault(true);

        BankAccountRequestDto updateDTO = new BankAccountRequestDto();
        updateDTO.setAccountNumber("UA999999999999999999999999999");
        updateDTO.setHolderType(AccountHolderType.COUNTERPARTY);
        updateDTO.setHolderId(20L);
        updateDTO.setBankId(null); // ignore
        updateDTO.setCurrencyId(null); // ignore
        updateDTO.setAccountName(null); // ignore
        updateDTO.setStatus(AccountStatus.CLOSED); // update
        updateDTO.setIsDefault(null); // ignore

        // When
        bankAccountMapper.updateEntity(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getAccountNumber()).isEqualTo("UA999999999999999999999999999"); // updated
        assertThat(existingEntity.getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY); // updated
        assertThat(existingEntity.getHolderId()).isEqualTo(20L); // updated
        assertThat(existingEntity.getBank().getId()).isEqualTo(5L); // unchanged
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(3L); // unchanged
        assertThat(existingEntity.getAccountName()).isEqualTo("Existing Account"); // unchanged
        assertThat(existingEntity.getStatus()).isEqualTo(AccountStatus.CLOSED); // updated
        assertThat(existingEntity.getIsDefault()).isTrue(); // unchanged
    }

    @Test
    void toEntity_shouldHandleDifferentHolderTypes() {
        // Test ORGANIZATION
        BankAccountRequestDto orgDTO = new BankAccountRequestDto();
        orgDTO.setAccountNumber("UA111111111111111111111111111");
        orgDTO.setHolderType(AccountHolderType.ORGANIZATION);
        orgDTO.setHolderId(1L);
        orgDTO.setBankId(1L);
        orgDTO.setCurrencyId(1L);

        BankAccount orgEntity = bankAccountMapper.toEntity(orgDTO);
        assertThat(orgEntity.getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);

        // Test COUNTERPARTY
        BankAccountRequestDto counterpartyDTO = new BankAccountRequestDto();
        counterpartyDTO.setAccountNumber("UA222222222222222222222222222");
        counterpartyDTO.setHolderType(AccountHolderType.COUNTERPARTY);
        counterpartyDTO.setHolderId(2L);
        counterpartyDTO.setBankId(1L);
        counterpartyDTO.setCurrencyId(1L);

        BankAccount counterpartyEntity = bankAccountMapper.toEntity(counterpartyDTO);
        assertThat(counterpartyEntity.getHolderType()).isEqualTo(AccountHolderType.COUNTERPARTY);
    }

    @Test
    void toEntity_shouldHandleDifferentStatuses() {
        // Test ACTIVE
        BankAccountRequestDto activeDTO = new BankAccountRequestDto();
        activeDTO.setAccountNumber("UA111111111111111111111111111");
        activeDTO.setHolderType(AccountHolderType.ORGANIZATION);
        activeDTO.setHolderId(1L);
        activeDTO.setBankId(1L);
        activeDTO.setCurrencyId(1L);
        activeDTO.setStatus(AccountStatus.ACTIVE);

        BankAccount activeEntity = bankAccountMapper.toEntity(activeDTO);
        assertThat(activeEntity.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        // Test INACTIVE
        BankAccountRequestDto inactiveDTO = new BankAccountRequestDto();
        inactiveDTO.setAccountNumber("UA222222222222222222222222222");
        inactiveDTO.setHolderType(AccountHolderType.ORGANIZATION);
        inactiveDTO.setHolderId(1L);
        inactiveDTO.setBankId(1L);
        inactiveDTO.setCurrencyId(1L);
        inactiveDTO.setStatus(AccountStatus.INACTIVE);

        BankAccount inactiveEntity = bankAccountMapper.toEntity(inactiveDTO);
        assertThat(inactiveEntity.getStatus()).isEqualTo(AccountStatus.INACTIVE);

        // Test CLOSED
        BankAccountRequestDto closedDTO = new BankAccountRequestDto();
        closedDTO.setAccountNumber("UA333333333333333333333333333");
        closedDTO.setHolderType(AccountHolderType.ORGANIZATION);
        closedDTO.setHolderId(1L);
        closedDTO.setBankId(1L);
        closedDTO.setCurrencyId(1L);
        closedDTO.setStatus(AccountStatus.CLOSED);

        BankAccount closedEntity = bankAccountMapper.toEntity(closedDTO);
        assertThat(closedEntity.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void toEntity_shouldHandleNullBankId() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA111111111111111111111111111");
        requestDto.setHolderType(AccountHolderType.ORGANIZATION);
        requestDto.setHolderId(1L);
        requestDto.setBankId(null); // null bankId
        requestDto.setCurrencyId(1L);

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getBank()).isNull();
        assertThat(entity.getCurrency()).isNotNull();
    }

    @Test
    void toEntity_shouldHandleNullCurrencyId() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA111111111111111111111111111");
        requestDto.setHolderType(AccountHolderType.ORGANIZATION);
        requestDto.setHolderId(1L);
        requestDto.setBankId(1L);
        requestDto.setCurrencyId(null); // null currencyId

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getBank()).isNotNull();
        assertThat(entity.getCurrency()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullBankIdAndCurrencyId() {
        // Given
        BankAccountRequestDto requestDto = new BankAccountRequestDto();
        requestDto.setAccountNumber("UA111111111111111111111111111");
        requestDto.setHolderType(AccountHolderType.ORGANIZATION);
        requestDto.setHolderId(1L);
        requestDto.setBankId(null); // null bankId
        requestDto.setCurrencyId(null); // null currencyId

        // When
        BankAccount entity = bankAccountMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getBank()).isNull();
        assertThat(entity.getCurrency()).isNull();
    }
}