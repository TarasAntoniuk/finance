package com.tarasantoniuk.finance.core.externalexchangerate.mapper;

import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDto;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalExchangeRateMapperUnitTest {

    private ExternalExchangeRateMapper mapper;

    @BeforeEach
    void setUp() {
        // Створюємо mapper БЕЗ Spring - тільки MapStruct
        mapper = Mappers.getMapper(ExternalExchangeRateMapper.class);
    }

    @Test
    void toEntity_shouldHandleNullCurrencyFromId() {
        // Given
        ExternalExchangeRateRequestDto requestDTO = new ExternalExchangeRateRequestDto();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(null); // null!
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(new BigDecimal("1.0"));
        requestDTO.setSource("TEST");

        // When
        ExternalExchangeRate entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom()).isNull();
        assertThat(entity.getCurrencyTo()).isNotNull();
        assertThat(entity.getCurrencyTo().getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_shouldHandleNullCurrencyToId() {
        // Given
        ExternalExchangeRateRequestDto requestDTO = new ExternalExchangeRateRequestDto();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(null); // null!
        requestDTO.setRate(new BigDecimal("1.0"));
        requestDTO.setSource("TEST");

        // When
        ExternalExchangeRate entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom()).isNotNull();
        assertThat(entity.getCurrencyFrom().getId()).isEqualTo(1L);
        assertThat(entity.getCurrencyTo()).isNull();
    }

    @Test
    void updateEntityFromDTO_shouldHandleNullCurrencyIds() {
        // Given
        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(1L);

        ExternalExchangeRateRequestDto updateDTO = new ExternalExchangeRateRequestDto();
        updateDTO.setExchangeDate(LocalDate.now());
        updateDTO.setCurrencyFromId(null);
        updateDTO.setCurrencyToId(null);
        updateDTO.setRate(new BigDecimal("1.0"));
        updateDTO.setSource("TEST");

        // When
        mapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then - mapper ignores null due to IGNORE strategy
        assertThat(existingEntity.getId()).isEqualTo(1L);
    }
}