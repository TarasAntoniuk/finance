package com.tarasantoniuk.finance.accountingpolicy.mapper;

import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.entity.AccountingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class AccountingPolicyMapperUnitTest {

    private AccountingPolicyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AccountingPolicyMapper.class);
    }

    @Test
    void toEntity_shouldHandleNullOrganizationId() {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(null); // null!
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);

        // When
        AccountingPolicy entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getOrganization()).isNull();
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(1L);
    }

    @Test
    void toEntity_shouldHandleNullCurrencyId() {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(null); // null!

        // When
        AccountingPolicy entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getOrganization()).isNotNull();
        assertThat(entity.getOrganization().getId()).isEqualTo(1L);
        assertThat(entity.getCurrency()).isNull();
    }

    @Test
    void updateEntityFromDTO_shouldHandleNullIds() {
        // Given
        AccountingPolicy existingEntity = new AccountingPolicy();
        existingEntity.setId(1L);

        AccountingPolicyRequestDTO updateDTO = new AccountingPolicyRequestDTO();
        updateDTO.setOrganizationId(null);
        updateDTO.setYear(2024);
        updateDTO.setCurrencyId(null);

        // When
        mapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getId()).isEqualTo(1L);
    }
}
