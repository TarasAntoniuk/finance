package com.tarasantoniuk.finance.externalexchangerate.mapper;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExternalExchangeRateMapperTest {

    @Autowired
    private ExternalExchangeRateMapper exchangeRateMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.of(2024, 11, 15));
        requestDTO.setCurrencyFromId(1L); // USD
        requestDTO.setCurrencyToId(2L);   // EUR
        requestDTO.setRate(new BigDecimal("0.852345"));
        requestDTO.setSource("ECB");
        requestDTO.setSourceUrl("https://ecb.europa.eu/rates");
        requestDTO.setIsActive(true);

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 15));
        assertThat(entity.getCurrencyFrom()).isNotNull();
        assertThat(entity.getCurrencyFrom().getId()).isEqualTo(1L);
        assertThat(entity.getCurrencyTo()).isNotNull();
        assertThat(entity.getCurrencyTo().getId()).isEqualTo(2L);
        assertThat(entity.getRate()).isEqualByComparingTo(new BigDecimal("0.852345"));
        assertThat(entity.getSource()).isEqualTo("ECB");
        assertThat(entity.getSourceUrl()).isEqualTo("https://ecb.europa.eu/rates");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.of(2024, 11, 15));
        requestDTO.setCurrencyFromId(3L);
        requestDTO.setCurrencyToId(4L);
        requestDTO.setRate(new BigDecimal("1.000000"));
        requestDTO.setSource("MANUAL");
        requestDTO.setSourceUrl(null); // optional
        requestDTO.setIsActive(null);  // optional

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getSourceUrl()).isNull();
        assertThat(entity.getIsActive()).isNull();
    }

    @Test
    void toEntity_shouldCreateBothCurrenciesWithOnlyIdSet() {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.of(2024, 10, 1));
        requestDTO.setCurrencyFromId(10L);
        requestDTO.setCurrencyToId(20L);
        requestDTO.setRate(new BigDecimal("25.5"));
        requestDTO.setSource("NBU");

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom()).isNotNull();
        assertThat(entity.getCurrencyFrom().getId()).isEqualTo(10L);
        assertThat(entity.getCurrencyFrom().getCode()).isNull();

        assertThat(entity.getCurrencyTo()).isNotNull();
        assertThat(entity.getCurrencyTo().getId()).isEqualTo(20L);
        assertThat(entity.getCurrencyTo().getCode()).isNull();
    }

    @Test
    void toEntity_shouldHandleDifferentRatePrecisions() {
        // Test with various decimal precisions
        ExternalExchangeRateRequestDTO requestDTO1 = new ExternalExchangeRateRequestDTO();
        requestDTO1.setExchangeDate(LocalDate.now());
        requestDTO1.setCurrencyFromId(1L);
        requestDTO1.setCurrencyToId(2L);
        requestDTO1.setRate(new BigDecimal("1.123456")); // 6 decimal places
        requestDTO1.setSource("TEST");

        ExternalExchangeRate entity1 = exchangeRateMapper.toEntity(requestDTO1);
        assertThat(entity1.getRate()).isEqualByComparingTo(new BigDecimal("1.123456"));

        // Test with integer rate
        ExternalExchangeRateRequestDTO requestDTO2 = new ExternalExchangeRateRequestDTO();
        requestDTO2.setExchangeDate(LocalDate.now());
        requestDTO2.setCurrencyFromId(1L);
        requestDTO2.setCurrencyToId(2L);
        requestDTO2.setRate(new BigDecimal("100"));
        requestDTO2.setSource("TEST");

        ExternalExchangeRate entity2 = exchangeRateMapper.toEntity(requestDTO2);
        assertThat(entity2.getRate()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    void toResponseDTO_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Currency usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setNumericCode("840");
        usd.setName("US Dollar");
        usd.setSymbol("$");
        usd.setMinorUnit(2);
        usd.setIsActive(true);
        usd.setCreatedAt(now.minusDays(100));
        usd.setUpdatedAt(now.minusDays(50));

        Currency eur = new Currency();
        eur.setId(2L);
        eur.setCode("EUR");
        eur.setNumericCode("978");
        eur.setName("Euro");
        eur.setSymbol("€");
        eur.setMinorUnit(2);
        eur.setIsActive(true);
        eur.setCreatedAt(now.minusDays(100));
        eur.setUpdatedAt(now.minusDays(50));

        ExternalExchangeRate entity = new ExternalExchangeRate();
        entity.setId(100L);
        entity.setExchangeDate(LocalDate.of(2024, 11, 15));
        entity.setCurrencyFrom(usd);
        entity.setCurrencyTo(eur);
        entity.setRate(new BigDecimal("0.925000"));
        entity.setSource("ECB");
        entity.setSourceUrl("https://ecb.europa.eu/stats");
        entity.setIsActive(true);
        entity.setCreatedAt(now.minusDays(10));
        entity.setUpdatedAt(now.minusDays(2));

        // When
        ExternalExchangeRateResponseDTO responseDTO = exchangeRateMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(100L);
        assertThat(responseDTO.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 15));
        assertThat(responseDTO.getRate()).isEqualByComparingTo(new BigDecimal("0.925000"));
        assertThat(responseDTO.getSource()).isEqualTo("ECB");
        assertThat(responseDTO.getSourceUrl()).isEqualTo("https://ecb.europa.eu/stats");
        assertThat(responseDTO.getIsActive()).isTrue();
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now.minusDays(2));

        // Check currencyFrom mapping
        assertThat(responseDTO.getCurrencyFrom()).isNotNull();
        assertThat(responseDTO.getCurrencyFrom().getId()).isEqualTo(1L);
        assertThat(responseDTO.getCurrencyFrom().getCode()).isEqualTo("USD");
        assertThat(responseDTO.getCurrencyFrom().getName()).isEqualTo("US Dollar");
        assertThat(responseDTO.getCurrencyFrom().getSymbol()).isEqualTo("$");
        assertThat(responseDTO.getCurrencyFrom().getMinorUnit()).isEqualTo(2);

        // Check currencyTo mapping
        assertThat(responseDTO.getCurrencyTo()).isNotNull();
        assertThat(responseDTO.getCurrencyTo().getId()).isEqualTo(2L);
        assertThat(responseDTO.getCurrencyTo().getCode()).isEqualTo("EUR");
        assertThat(responseDTO.getCurrencyTo().getName()).isEqualTo("Euro");
        assertThat(responseDTO.getCurrencyTo().getSymbol()).isEqualTo("€");
        assertThat(responseDTO.getCurrencyTo().getMinorUnit()).isEqualTo(2);
    }

    @Test
    void toResponseDTO_shouldHandleNullOptionalFields() {
        // Given
        Currency uah = new Currency();
        uah.setId(3L);
        uah.setCode("UAH");
        uah.setName("Ukrainian Hryvnia");

        Currency usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setName("US Dollar");

        ExternalExchangeRate entity = new ExternalExchangeRate();
        entity.setId(200L);
        entity.setExchangeDate(LocalDate.of(2024, 11, 1));
        entity.setCurrencyFrom(uah);
        entity.setCurrencyTo(usd);
        entity.setRate(new BigDecimal("0.025"));
        entity.setSource("NBU");
        entity.setSourceUrl(null); // optional
        entity.setIsActive(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null); // optional

        // When
        ExternalExchangeRateResponseDTO responseDTO = exchangeRateMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getSourceUrl()).isNull();
        assertThat(responseDTO.getIsActive()).isFalse();
        assertThat(responseDTO.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseDTOList_shouldMapAllEntities() {
        // Given
        Currency usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setName("US Dollar");

        Currency eur = new Currency();
        eur.setId(2L);
        eur.setCode("EUR");
        eur.setName("Euro");

        Currency uah = new Currency();
        uah.setId(3L);
        uah.setCode("UAH");
        uah.setName("Ukrainian Hryvnia");

        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setId(1L);
        rate1.setExchangeDate(LocalDate.of(2024, 11, 15));
        rate1.setCurrencyFrom(usd);
        rate1.setCurrencyTo(eur);
        rate1.setRate(new BigDecimal("0.92"));
        rate1.setSource("ECB");

        ExternalExchangeRate rate2 = new ExternalExchangeRate();
        rate2.setId(2L);
        rate2.setExchangeDate(LocalDate.of(2024, 11, 15));
        rate2.setCurrencyFrom(usd);
        rate2.setCurrencyTo(uah);
        rate2.setRate(new BigDecimal("41.25"));
        rate2.setSource("NBU");

        List<ExternalExchangeRate> rates = Arrays.asList(rate1, rate2);

        // When
        List<ExternalExchangeRateResponseDTO> responseDTOs = exchangeRateMapper.toResponseDTOList(rates);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(0).getCurrencyFrom().getCode()).isEqualTo("USD");
        assertThat(responseDTOs.get(0).getCurrencyTo().getCode()).isEqualTo("EUR");
        assertThat(responseDTOs.get(0).getRate()).isEqualByComparingTo(new BigDecimal("0.92"));
        assertThat(responseDTOs.get(0).getSource()).isEqualTo("ECB");

        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getCurrencyFrom().getCode()).isEqualTo("USD");
        assertThat(responseDTOs.get(1).getCurrencyTo().getCode()).isEqualTo("UAH");
        assertThat(responseDTOs.get(1).getRate()).isEqualByComparingTo(new BigDecimal("41.25"));
        assertThat(responseDTOs.get(1).getSource()).isEqualTo("NBU");
    }

    @Test
    void toResponseDTOList_shouldHandleEmptyList() {
        // Given
        List<ExternalExchangeRate> rates = Arrays.asList();

        // When
        List<ExternalExchangeRateResponseDTO> responseDTOs = exchangeRateMapper.toResponseDTOList(rates);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntityFromDTO_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(60);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(30);

        Currency oldUsd = new Currency();
        oldUsd.setId(1L);
        oldUsd.setCode("USD");

        Currency oldEur = new Currency();
        oldEur.setId(2L);
        oldEur.setCode("EUR");

        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(500L);
        existingEntity.setExchangeDate(LocalDate.of(2024, 10, 1));
        existingEntity.setCurrencyFrom(oldUsd);
        existingEntity.setCurrencyTo(oldEur);
        existingEntity.setRate(new BigDecimal("0.900000"));
        existingEntity.setSource("OLD_SOURCE");
        existingEntity.setSourceUrl("https://old-url.com");
        existingEntity.setIsActive(false);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        ExternalExchangeRateRequestDTO updateDTO = new ExternalExchangeRateRequestDTO();
        updateDTO.setExchangeDate(LocalDate.of(2024, 11, 15));
        updateDTO.setCurrencyFromId(3L); // UAH
        updateDTO.setCurrencyToId(4L);   // PLN
        updateDTO.setRate(new BigDecimal("0.150000"));
        updateDTO.setSource("NEW_SOURCE");
        updateDTO.setSourceUrl("https://new-url.com");
        updateDTO.setIsActive(true);

        // When
        exchangeRateMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 15));
        assertThat(existingEntity.getCurrencyFrom()).isNotNull();
        assertThat(existingEntity.getCurrencyFrom().getId()).isEqualTo(3L);
        assertThat(existingEntity.getCurrencyTo()).isNotNull();
        assertThat(existingEntity.getCurrencyTo().getId()).isEqualTo(4L);
        assertThat(existingEntity.getRate()).isEqualByComparingTo(new BigDecimal("0.150000"));
        assertThat(existingEntity.getSource()).isEqualTo("NEW_SOURCE");
        assertThat(existingEntity.getSourceUrl()).isEqualTo("https://new-url.com");
        assertThat(existingEntity.getIsActive()).isTrue();

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(500L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityFromDTO_shouldIgnoreNullValues() {
        // Given
        Currency existingUsd = new Currency();
        existingUsd.setId(1L);

        Currency existingEur = new Currency();
        existingEur.setId(2L);

        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(600L);
        existingEntity.setExchangeDate(LocalDate.of(2024, 11, 1));
        existingEntity.setCurrencyFrom(existingUsd);
        existingEntity.setCurrencyTo(existingEur);
        existingEntity.setRate(new BigDecimal("0.920000"));
        existingEntity.setSource("EXISTING_SOURCE");
        existingEntity.setSourceUrl("https://existing.com");
        existingEntity.setIsActive(true);

        ExternalExchangeRateRequestDTO updateDTO = new ExternalExchangeRateRequestDTO();
        updateDTO.setExchangeDate(LocalDate.of(2024, 11, 15)); // update
        updateDTO.setCurrencyFromId(null); // ignore
        updateDTO.setCurrencyToId(null);   // ignore
        updateDTO.setRate(new BigDecimal("0.950000")); // update
        updateDTO.setSource(null);     // ignore
        updateDTO.setSourceUrl(null);  // ignore
        updateDTO.setIsActive(null);   // ignore

        // When
        exchangeRateMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 15)); // updated
        assertThat(existingEntity.getCurrencyFrom().getId()).isEqualTo(1L); // unchanged
        assertThat(existingEntity.getCurrencyTo().getId()).isEqualTo(2L);   // unchanged
        assertThat(existingEntity.getRate()).isEqualByComparingTo(new BigDecimal("0.950000")); // updated
        assertThat(existingEntity.getSource()).isEqualTo("EXISTING_SOURCE"); // unchanged
        assertThat(existingEntity.getSourceUrl()).isEqualTo("https://existing.com"); // unchanged
        assertThat(existingEntity.getIsActive()).isTrue(); // unchanged
    }

    @Test
    void updateEntityFromDTO_shouldChangeBothCurrencies() {
        // Given
        Currency oldFrom = new Currency();
        oldFrom.setId(1L);

        Currency oldTo = new Currency();
        oldTo.setId(2L);

        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(700L);
        existingEntity.setExchangeDate(LocalDate.of(2024, 11, 1));
        existingEntity.setCurrencyFrom(oldFrom);
        existingEntity.setCurrencyTo(oldTo);
        existingEntity.setRate(new BigDecimal("1.0"));
        existingEntity.setSource("TEST");

        ExternalExchangeRateRequestDTO updateDTO = new ExternalExchangeRateRequestDTO();
        updateDTO.setExchangeDate(LocalDate.of(2024, 11, 1));
        updateDTO.setCurrencyFromId(10L); // change
        updateDTO.setCurrencyToId(20L);   // change
        updateDTO.setRate(new BigDecimal("1.0"));
        updateDTO.setSource("TEST");

        // When
        exchangeRateMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getCurrencyFrom().getId()).isEqualTo(10L);
        assertThat(existingEntity.getCurrencyTo().getId()).isEqualTo(20L);
    }

    @Test
    void updateEntityFromDTO_shouldChangeOnlyOneField() {
        // Given
        Currency usd = new Currency();
        usd.setId(1L);

        Currency eur = new Currency();
        eur.setId(2L);

        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(800L);
        existingEntity.setExchangeDate(LocalDate.of(2024, 11, 1));
        existingEntity.setCurrencyFrom(usd);
        existingEntity.setCurrencyTo(eur);
        existingEntity.setRate(new BigDecimal("0.900000"));
        existingEntity.setSource("OLD_SOURCE");
        existingEntity.setSourceUrl("https://old.com");
        existingEntity.setIsActive(false);

        ExternalExchangeRateRequestDTO updateDTO = new ExternalExchangeRateRequestDTO();
        updateDTO.setExchangeDate(LocalDate.of(2024, 11, 1));
        updateDTO.setCurrencyFromId(1L);
        updateDTO.setCurrencyToId(2L);
        updateDTO.setRate(new BigDecimal("0.925000")); // only this changed
        updateDTO.setSource("OLD_SOURCE");
        updateDTO.setSourceUrl("https://old.com");
        updateDTO.setIsActive(false);

        // When
        exchangeRateMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getRate()).isEqualByComparingTo(new BigDecimal("0.925000"));
        assertThat(existingEntity.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 1));
        assertThat(existingEntity.getSource()).isEqualTo("OLD_SOURCE");
    }

    @Test
    void updateEntityFromDTO_shouldPartiallyUpdateMultipleFields() {
        // Given
        Currency usd = new Currency();
        usd.setId(1L);

        Currency eur = new Currency();
        eur.setId(2L);

        ExternalExchangeRate existingEntity = new ExternalExchangeRate();
        existingEntity.setId(900L);
        existingEntity.setExchangeDate(LocalDate.of(2024, 10, 1));
        existingEntity.setCurrencyFrom(usd);
        existingEntity.setCurrencyTo(eur);
        existingEntity.setRate(new BigDecimal("0.900000"));
        existingEntity.setSource("ECB");
        existingEntity.setSourceUrl("https://ecb.eu");
        existingEntity.setIsActive(true);

        ExternalExchangeRateRequestDTO updateDTO = new ExternalExchangeRateRequestDTO();
        updateDTO.setExchangeDate(LocalDate.of(2024, 11, 15)); // update
        updateDTO.setCurrencyFromId(null); // ignore
        updateDTO.setCurrencyToId(2L);     // keep same
        updateDTO.setRate(new BigDecimal("0.920000")); // update
        updateDTO.setSource("ECB_NEW");    // update
        updateDTO.setSourceUrl(null);      // ignore
        updateDTO.setIsActive(false);      // update

        // When
        exchangeRateMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getExchangeDate()).isEqualTo(LocalDate.of(2024, 11, 15));
        assertThat(existingEntity.getCurrencyFrom().getId()).isEqualTo(1L); // unchanged
        assertThat(existingEntity.getCurrencyTo().getId()).isEqualTo(2L);
        assertThat(existingEntity.getRate()).isEqualByComparingTo(new BigDecimal("0.920000"));
        assertThat(existingEntity.getSource()).isEqualTo("ECB_NEW");
        assertThat(existingEntity.getSourceUrl()).isEqualTo("https://ecb.eu"); // unchanged
        assertThat(existingEntity.getIsActive()).isFalse();
    }

    @Test
    void toEntity_shouldHandleDifferentSources() {
        // Test ECB
        ExternalExchangeRateRequestDTO ecb = new ExternalExchangeRateRequestDTO();
        ecb.setExchangeDate(LocalDate.now());
        ecb.setCurrencyFromId(1L);
        ecb.setCurrencyToId(2L);
        ecb.setRate(new BigDecimal("0.92"));
        ecb.setSource("ECB");

        ExternalExchangeRate ecbEntity = exchangeRateMapper.toEntity(ecb);
        assertThat(ecbEntity.getSource()).isEqualTo("ECB");

        // Test NBU
        ExternalExchangeRateRequestDTO nbu = new ExternalExchangeRateRequestDTO();
        nbu.setExchangeDate(LocalDate.now());
        nbu.setCurrencyFromId(1L);
        nbu.setCurrencyToId(3L);
        nbu.setRate(new BigDecimal("41.25"));
        nbu.setSource("NBU");

        ExternalExchangeRate nbuEntity = exchangeRateMapper.toEntity(nbu);
        assertThat(nbuEntity.getSource()).isEqualTo("NBU");

        // Test MONOBANK
        ExternalExchangeRateRequestDTO mono = new ExternalExchangeRateRequestDTO();
        mono.setExchangeDate(LocalDate.now());
        mono.setCurrencyFromId(1L);
        mono.setCurrencyToId(3L);
        mono.setRate(new BigDecimal("41.30"));
        mono.setSource("MONOBANK");

        ExternalExchangeRate monoEntity = exchangeRateMapper.toEntity(mono);
        assertThat(monoEntity.getSource()).isEqualTo("MONOBANK");
    }

    @Test
    void toEntity_shouldHandleSameCurrencyPair() {
        // Given - same currency (rate should be 1.0)
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L); // USD
        requestDTO.setCurrencyToId(1L);   // USD
        requestDTO.setRate(BigDecimal.ONE);
        requestDTO.setSource("SYSTEM");

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom().getId()).isEqualTo(1L);
        assertThat(entity.getCurrencyTo().getId()).isEqualTo(1L);
        assertThat(entity.getRate()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void toEntity_shouldHandleNullCurrencyFromId() {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(null); // ← null!
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(new BigDecimal("1.0"));
        requestDTO.setSource("TEST");

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom()).isNull();
        assertThat(entity.getCurrencyTo()).isNotNull();
    }

    @Test
    void toEntity_shouldHandleNullCurrencyToId() {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(null); // ← null!
        requestDTO.setRate(new BigDecimal("1.0"));
        requestDTO.setSource("TEST");

        // When
        ExternalExchangeRate entity = exchangeRateMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrencyFrom()).isNotNull();
        assertThat(entity.getCurrencyTo()).isNull();
    }
}