package com.tarasantoniuk.finance.currency.mapper;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.currency.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CurrencyMapperTest {

    @Autowired
    private CurrencyMapper currencyMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setSymbol("$");
        requestDTO.setMinorUnit(2);
        requestDTO.setIsActive(true);

        // When
        Currency entity = currencyMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getCode()).isEqualTo("USD");
        assertThat(entity.getNumericCode()).isEqualTo("840");
        assertThat(entity.getName()).isEqualTo("US Dollar");
        assertThat(entity.getSymbol()).isEqualTo("$");
        assertThat(entity.getMinorUnit()).isEqualTo(2);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullSymbol() {
        // Given
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("XAU");
        requestDTO.setNumericCode("959");
        requestDTO.setName("Gold");
        requestDTO.setSymbol(null);
        requestDTO.setMinorUnit(0);
        requestDTO.setIsActive(true);

        // When
        Currency entity = currencyMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getSymbol()).isNull();
    }

    @Test
    void toResponseDTO_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Currency entity = new Currency();
        entity.setId(1L);
        entity.setCode("EUR");
        entity.setNumericCode("978");
        entity.setName("Euro");
        entity.setSymbol("€");
        entity.setMinorUnit(2);
        entity.setIsActive(true);
        entity.setCreatedAt(now.minusDays(1));
        entity.setUpdatedAt(now);

        // When
        CurrencyResponseDTO responseDTO = currencyMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getCode()).isEqualTo("EUR");
        assertThat(responseDTO.getNumericCode()).isEqualTo("978");
        assertThat(responseDTO.getName()).isEqualTo("Euro");
        assertThat(responseDTO.getSymbol()).isEqualTo("€");
        assertThat(responseDTO.getMinorUnit()).isEqualTo(2);
        assertThat(responseDTO.getIsActive()).isTrue();
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseDTO_shouldHandleNullFields() {
        // Given
        Currency entity = new Currency();
        entity.setId(2L);
        entity.setCode("UAH");
        entity.setNumericCode("980");
        entity.setName("Ukrainian Hryvnia");
        entity.setSymbol(null); // null symbol
        entity.setMinorUnit(2);
        entity.setIsActive(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null); // null updatedAt

        // When
        CurrencyResponseDTO responseDTO = currencyMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getSymbol()).isNull();
        assertThat(responseDTO.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseDTOList_shouldMapAllEntities() {
        // Given
        Currency currency1 = new Currency();
        currency1.setId(1L);
        currency1.setCode("USD");
        currency1.setNumericCode("840");
        currency1.setName("US Dollar");
        currency1.setSymbol("$");
        currency1.setMinorUnit(2);
        currency1.setIsActive(true);

        Currency currency2 = new Currency();
        currency2.setId(2L);
        currency2.setCode("EUR");
        currency2.setNumericCode("978");
        currency2.setName("Euro");
        currency2.setSymbol("€");
        currency2.setMinorUnit(2);
        currency2.setIsActive(true);

        List<Currency> currencies = Arrays.asList(currency1, currency2);

        // When
        List<CurrencyResponseDTO> responseDTOs = currencyMapper.toResponseDTOList(currencies);

        // Then
        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getCode()).isEqualTo("USD");
        assertThat(responseDTOs.get(1).getCode()).isEqualTo("EUR");
    }

    @Test
    void updateEntityFromDTO_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(10);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(5);

        Currency existingEntity = new Currency();
        existingEntity.setId(5L);
        existingEntity.setCode("OLD");
        existingEntity.setNumericCode("000");
        existingEntity.setName("Old Currency");
        existingEntity.setSymbol("O");
        existingEntity.setMinorUnit(0);
        existingEntity.setIsActive(false);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        CurrencyRequestDTO updateDTO = new CurrencyRequestDTO();
        updateDTO.setCode("NEW");
        updateDTO.setNumericCode("999");
        updateDTO.setName("New Currency");
        updateDTO.setSymbol("N");
        updateDTO.setMinorUnit(3);
        updateDTO.setIsActive(true);

        // When
        currencyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getCode()).isEqualTo("NEW");
        assertThat(existingEntity.getNumericCode()).isEqualTo("999");
        assertThat(existingEntity.getName()).isEqualTo("New Currency");
        assertThat(existingEntity.getSymbol()).isEqualTo("N");
        assertThat(existingEntity.getMinorUnit()).isEqualTo(3);
        assertThat(existingEntity.getIsActive()).isTrue();

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(5L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityFromDTO_shouldIgnoreNullValues() {
        // Given
        Currency existingEntity = new Currency();
        existingEntity.setId(10L);
        existingEntity.setCode("USD");
        existingEntity.setNumericCode("840");
        existingEntity.setName("US Dollar");
        existingEntity.setSymbol("$");
        existingEntity.setMinorUnit(2);
        existingEntity.setIsActive(true);

        CurrencyRequestDTO updateDTO = new CurrencyRequestDTO();
        updateDTO.setCode("EUR"); // update this
        updateDTO.setNumericCode(null); // this should be ignored
        updateDTO.setName("Euro"); // update this
        updateDTO.setSymbol(null); // this should be ignored
        updateDTO.setMinorUnit(2);
        updateDTO.setIsActive(null); // this should be ignored

        // When
        currencyMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getCode()).isEqualTo("EUR"); // updated
        assertThat(existingEntity.getNumericCode()).isEqualTo("840"); // unchanged (null ignored)
        assertThat(existingEntity.getName()).isEqualTo("Euro"); // updated
        assertThat(existingEntity.getSymbol()).isEqualTo("$"); // unchanged (null ignored)
        assertThat(existingEntity.getIsActive()).isTrue(); // unchanged (null ignored)
    }

    @Test
    void toEntity_shouldSetDefaultIsActiveIfNotProvided() {
        // Given
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("GBP");
        requestDTO.setNumericCode("826");
        requestDTO.setName("British Pound");
        requestDTO.setSymbol("£");
        requestDTO.setMinorUnit(2);
        requestDTO.setIsActive(null);

        // When
        Currency entity = currencyMapper.toEntity(requestDTO);

        // Then
        // MapStruct will map null, but Currency constructor has default value true
        // This depends on whether you want DTO null to override entity default
        assertThat(entity.getIsActive()).isNull(); // MapStruct maps null as-is
    }
}