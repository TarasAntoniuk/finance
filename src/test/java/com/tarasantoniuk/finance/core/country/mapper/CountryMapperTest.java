package com.tarasantoniuk.finance.core.country.mapper;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.country.dto.CountryRequestDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class CountryMapperTest extends BaseIntegrationTest {

    @Autowired
    private CountryMapper countryMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("Ukraine");
        requestDTO.setIsoCode("UKR");
        requestDTO.setPhoneCode("+380");
        requestDTO.setCurrencyId(1L);

        // When
        Country entity = countryMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getName()).isEqualTo("Ukraine");
        assertThat(entity.getIsoCode()).isEqualTo("UKR");
        assertThat(entity.getPhoneCode()).isEqualTo("+380");
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(1L);
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullCurrencyId() {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("Antarctica");
        requestDTO.setIsoCode("ATA");
        requestDTO.setPhoneCode(null);
        requestDTO.setCurrencyId(null);

        // When
        Country entity = countryMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Antarctica");
        assertThat(entity.getIsoCode()).isEqualTo("ATA");
        assertThat(entity.getPhoneCode()).isNull();
        assertThat(entity.getCurrency()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullPhoneCode() {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("Vatican City");
        requestDTO.setIsoCode("VAT");
        requestDTO.setPhoneCode(null);
        requestDTO.setCurrencyId(2L);

        // When
        Country entity = countryMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getPhoneCode()).isNull();
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(2L);
    }

    @Test
    void toResponseDTO_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Currency currency = new Currency();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setNumericCode("840");
        currency.setName("US Dollar");
        currency.setSymbol("$");
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        currency.setCreatedAt(now.minusDays(10));
        currency.setUpdatedAt(now.minusDays(5));

        Country entity = new Country();
        entity.setId(5L);
        entity.setName("United States");
        entity.setIsoCode("USA");
        entity.setPhoneCode("+1");
        entity.setCurrency(currency);
        entity.setCreatedAt(now.minusDays(1));
        entity.setUpdatedAt(now);

        // When
        CountryResponseDto responseDTO = countryMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(5L);
        assertThat(responseDTO.getName()).isEqualTo("United States");
        assertThat(responseDTO.getIsoCode()).isEqualTo("USA");
        assertThat(responseDTO.getPhoneCode()).isEqualTo("+1");
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now);

        // Check currency mapping
        assertThat(responseDTO.getCurrency()).isNotNull();
        assertThat(responseDTO.getCurrency().getId()).isEqualTo(1L);
        assertThat(responseDTO.getCurrency().getCode()).isEqualTo("USD");
        assertThat(responseDTO.getCurrency().getNumericCode()).isEqualTo("840");
        assertThat(responseDTO.getCurrency().getName()).isEqualTo("US Dollar");
        assertThat(responseDTO.getCurrency().getSymbol()).isEqualTo("$");
        assertThat(responseDTO.getCurrency().getMinorUnit()).isEqualTo(2);
        assertThat(responseDTO.getCurrency().getIsActive()).isTrue();
        assertThat(responseDTO.getCurrency().getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(responseDTO.getCurrency().getUpdatedAt()).isEqualTo(now.minusDays(5));
    }

    @Test
    void toResponseDTO_shouldHandleNullCurrency() {
        // Given
        Country entity = new Country();
        entity.setId(10L);
        entity.setName("Nowhere Land");
        entity.setIsoCode("NWL");
        entity.setPhoneCode("+999");
        entity.setCurrency(null);
        entity.setCreatedAt(LocalDateTime.now());

        // When
        CountryResponseDto responseDTO = countryMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getCurrency()).isNull();
    }

    @Test
    void toResponseDTO_shouldHandleNullPhoneCodeAndUpdatedAt() {
        // Given
        Country entity = new Country();
        entity.setId(15L);
        entity.setName("Test Country");
        entity.setIsoCode("TST");
        entity.setPhoneCode(null);
        entity.setCurrency(null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        CountryResponseDto responseDTO = countryMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getPhoneCode()).isNull();
        assertThat(responseDTO.getUpdatedAt()).isNull();
        assertThat(responseDTO.getCurrency()).isNull();
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

        Country usa = new Country();
        usa.setId(1L);
        usa.setName("United States");
        usa.setIsoCode("USA");
        usa.setPhoneCode("+1");
        usa.setCurrency(usd);

        Country germany = new Country();
        germany.setId(2L);
        germany.setName("Germany");
        germany.setIsoCode("DEU");
        germany.setPhoneCode("+49");
        germany.setCurrency(eur);

        List<Country> countries = Arrays.asList(usa, germany);

        // When
        List<CountryResponseDto> responseDTOs = countryMapper.toResponseDTOList(countries);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.get(0).getName()).isEqualTo("United States");
        assertThat(responseDTOs.get(0).getIsoCode()).isEqualTo("USA");
        assertThat(responseDTOs.get(0).getCurrency().getCode()).isEqualTo("USD");

        assertThat(responseDTOs.get(1).getName()).isEqualTo("Germany");
        assertThat(responseDTOs.get(1).getIsoCode()).isEqualTo("DEU");
        assertThat(responseDTOs.get(1).getCurrency().getCode()).isEqualTo("EUR");
    }

    @Test
    void toResponseDTOList_shouldHandleEmptyList() {
        // Given
        List<Country> countries = List.of();

        // When
        List<CountryResponseDto> responseDTOs = countryMapper.toResponseDTOList(countries);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntityFromDTO_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(30);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(15);

        Currency oldCurrency = new Currency();
        oldCurrency.setId(1L);
        oldCurrency.setCode("OLD");

        Country existingEntity = new Country();
        existingEntity.setId(100L);
        existingEntity.setName("Old Country Name");
        existingEntity.setIsoCode("OLD");
        existingEntity.setPhoneCode("+111");
        existingEntity.setCurrency(oldCurrency);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        CountryRequestDto updateDTO = new CountryRequestDto();
        updateDTO.setName("New Country Name");
        updateDTO.setIsoCode("NEW");
        updateDTO.setPhoneCode("+222");
        updateDTO.setCurrencyId(99L);

        // When
        countryMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getName()).isEqualTo("New Country Name");
        assertThat(existingEntity.getIsoCode()).isEqualTo("NEW");
        assertThat(existingEntity.getPhoneCode()).isEqualTo("+222");
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(99L);

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(100L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityFromDTO_shouldIgnoreNullValues() {
        // Given
        Currency existingCurrency = new Currency();
        existingCurrency.setId(5L);

        Country existingEntity = new Country();
        existingEntity.setId(50L);
        existingEntity.setName("Existing Country");
        existingEntity.setIsoCode("EXI");
        existingEntity.setPhoneCode("+123");
        existingEntity.setCurrency(existingCurrency);

        CountryRequestDto updateDTO = new CountryRequestDto();
        updateDTO.setName("Updated Name"); // update this
        updateDTO.setIsoCode(null); // should be ignored
        updateDTO.setPhoneCode(null); // should be ignored
        updateDTO.setCurrencyId(null); // should be ignored

        // When
        countryMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Updated Name"); // updated
        assertThat(existingEntity.getIsoCode()).isEqualTo("EXI"); // unchanged (null ignored)
        assertThat(existingEntity.getPhoneCode()).isEqualTo("+123"); // unchanged (null ignored)
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(5L); // unchanged (null ignored)
    }

    @Test
    void updateEntityFromDTO_shouldUpdateCurrencyToNull() {
        // Given
        Currency existingCurrency = new Currency();
        existingCurrency.setId(10L);

        Country existingEntity = new Country();
        existingEntity.setId(75L);
        existingEntity.setName("Country With Currency");
        existingEntity.setIsoCode("CWC");
        existingEntity.setPhoneCode("+456");
        existingEntity.setCurrency(existingCurrency);

        CountryRequestDto updateDTO = new CountryRequestDto();
        updateDTO.setName("Country Without Currency");
        updateDTO.setIsoCode("CWC");
        updateDTO.setPhoneCode("+456");
        updateDTO.setCurrencyId(null); // Due to IGNORE strategy, this will be ignored

        // When
        countryMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // With NullValuePropertyMappingStrategy.IGNORE, null currency should NOT update
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(10L);
    }

    @Test
    void updateEntityFromDTO_shouldChangeCurrency() {
        // Given
        Currency oldCurrency = new Currency();
        oldCurrency.setId(1L);

        Country existingEntity = new Country();
        existingEntity.setId(80L);
        existingEntity.setName("Spain");
        existingEntity.setIsoCode("ESP");
        existingEntity.setPhoneCode("+34");
        existingEntity.setCurrency(oldCurrency);

        CountryRequestDto updateDTO = new CountryRequestDto();
        updateDTO.setName("Spain");
        updateDTO.setIsoCode("ESP");
        updateDTO.setPhoneCode("+34");
        updateDTO.setCurrencyId(2L); // change currency

        // When
        countryMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getCurrency()).isNotNull();
        assertThat(existingEntity.getCurrency().getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_shouldCreateCountryWithOnlyCurrencyIdSet() {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("Poland");
        requestDTO.setIsoCode("POL");
        requestDTO.setPhoneCode("+48");
        requestDTO.setCurrencyId(3L);

        // When
        Country entity = countryMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCurrency()).isNotNull();
        assertThat(entity.getCurrency().getId()).isEqualTo(3L);
        // Currency should only have ID set, other fields should be null
        assertThat(entity.getCurrency().getCode()).isNull();
        assertThat(entity.getCurrency().getName()).isNull();
    }
}