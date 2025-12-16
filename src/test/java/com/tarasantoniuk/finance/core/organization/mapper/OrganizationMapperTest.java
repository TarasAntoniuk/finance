package com.tarasantoniuk.finance.core.organization.mapper;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class OrganizationMapperTest extends BaseIntegrationTest {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDTO() {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Tech Corp Ltd");
        requestDTO.setRegistrationNumber("12345678");
        requestDTO.setVatNumber("UA123456789");
        requestDTO.setAddress("123 Main Street, Kyiv");
        requestDTO.setEmail("info@techcorp.com");
        requestDTO.setPhone("+380441234567");
        requestDTO.setCountryId(1L);

        // When
        Organization entity = organizationMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getName()).isEqualTo("Tech Corp Ltd");
        assertThat(entity.getRegistrationNumber()).isEqualTo("12345678");
        assertThat(entity.getVatNumber()).isEqualTo("UA123456789");
        assertThat(entity.getAddress()).isEqualTo("123 Main Street, Kyiv");
        assertThat(entity.getEmail()).isEqualTo("info@techcorp.com");
        assertThat(entity.getPhone()).isEqualTo("+380441234567");
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(1L);
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Minimal Organization");
        requestDTO.setRegistrationNumber(null);
        requestDTO.setVatNumber(null);
        requestDTO.setAddress(null);
        requestDTO.setEmail(null);
        requestDTO.setPhone(null);
        requestDTO.setCountryId(5L);

        // When
        Organization entity = organizationMapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Minimal Organization");
        assertThat(entity.getRegistrationNumber()).isNull();
        assertThat(entity.getVatNumber()).isNull();
        assertThat(entity.getAddress()).isNull();
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getPhone()).isNull();
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(5L);
    }

    @Test
    void toEntity_shouldCreateCountryWithOnlyIdSet() {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Org");
        requestDTO.setCountryId(10L);

        // When
        Organization entity = organizationMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(10L);
        // Country should only have ID set, other fields should be null
        assertThat(entity.getCountry().getName()).isNull();
        assertThat(entity.getCountry().getIsoCode()).isNull();
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

        Country country = new Country();
        country.setId(2L);
        country.setName("United States");
        country.setIsoCode("USA");
        country.setPhoneCode("+1");
        country.setCurrency(currency);
        country.setCreatedAt(now.minusDays(100));
        country.setUpdatedAt(now.minusDays(50));

        Organization entity = new Organization();
        entity.setId(10L);
        entity.setName("American Tech Inc");
        entity.setRegistrationNumber("US-987654321");
        entity.setVatNumber("US-VAT-123");
        entity.setAddress("456 Silicon Valley, CA");
        entity.setEmail("contact@americantech.com");
        entity.setPhone("+14155551234");
        entity.setCountry(country);
        entity.setCreatedAt(now.minusDays(10));
        entity.setUpdatedAt(now.minusDays(2));

        // When
        OrganizationResponseDto responseDTO = organizationMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(10L);
        assertThat(responseDTO.getName()).isEqualTo("American Tech Inc");
        assertThat(responseDTO.getRegistrationNumber()).isEqualTo("US-987654321");
        assertThat(responseDTO.getVatNumber()).isEqualTo("US-VAT-123");
        assertThat(responseDTO.getAddress()).isEqualTo("456 Silicon Valley, CA");
        assertThat(responseDTO.getEmail()).isEqualTo("contact@americantech.com");
        assertThat(responseDTO.getPhone()).isEqualTo("+14155551234");
        assertThat(responseDTO.getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(now.minusDays(2));

        // Check country mapping (using CountryMapper)
        assertThat(responseDTO.getCountry()).isNotNull();
        assertThat(responseDTO.getCountry().getId()).isEqualTo(2L);
        assertThat(responseDTO.getCountry().getName()).isEqualTo("United States");
        assertThat(responseDTO.getCountry().getIsoCode()).isEqualTo("USA");
        assertThat(responseDTO.getCountry().getPhoneCode()).isEqualTo("+1");
        assertThat(responseDTO.getCountry().getCreatedAt()).isEqualTo(now.minusDays(100));
        assertThat(responseDTO.getCountry().getUpdatedAt()).isEqualTo(now.minusDays(50));

        // Check nested currency mapping
        assertThat(responseDTO.getCountry().getCurrency()).isNotNull();
        assertThat(responseDTO.getCountry().getCurrency().getId()).isEqualTo(1L);
        assertThat(responseDTO.getCountry().getCurrency().getCode()).isEqualTo("USD");
        assertThat(responseDTO.getCountry().getCurrency().getName()).isEqualTo("US Dollar");
        assertThat(responseDTO.getCountry().getCurrency().getSymbol()).isEqualTo("$");
    }

    @Test
    void toResponseDTO_shouldHandleNullOptionalFields() {
        // Given
        Country country = new Country();
        country.setId(3L);
        country.setName("Test Country");
        country.setIsoCode("TST");

        Organization entity = new Organization();
        entity.setId(20L);
        entity.setName("Sparse Organization");
        entity.setRegistrationNumber(null);
        entity.setVatNumber(null);
        entity.setAddress(null);
        entity.setEmail(null);
        entity.setPhone(null);
        entity.setCountry(country);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        OrganizationResponseDto responseDTO = organizationMapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO.getName()).isEqualTo("Sparse Organization");
        assertThat(responseDTO.getRegistrationNumber()).isNull();
        assertThat(responseDTO.getVatNumber()).isNull();
        assertThat(responseDTO.getAddress()).isNull();
        assertThat(responseDTO.getEmail()).isNull();
        assertThat(responseDTO.getPhone()).isNull();
        assertThat(responseDTO.getUpdatedAt()).isNull();
        assertThat(responseDTO.getCountry()).isNotNull();
        assertThat(responseDTO.getCountry().getId()).isEqualTo(3L);
    }

    @Test
    void toResponseDTOList_shouldMapAllEntities() {
        // Given
        Country ukraine = new Country();
        ukraine.setId(1L);
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");

        Country spain = new Country();
        spain.setId(2L);
        spain.setName("Spain");
        spain.setIsoCode("ESP");

        Organization org1 = new Organization();
        org1.setId(1L);
        org1.setName("Ukrainian Startup");
        org1.setRegistrationNumber("UA-111");
        org1.setEmail("info@ukrstartup.com");
        org1.setCountry(ukraine);

        Organization org2 = new Organization();
        org2.setId(2L);
        org2.setName("Spanish Consultancy");
        org2.setRegistrationNumber("ES-222");
        org2.setEmail("hola@spanishconsult.es");
        org2.setCountry(spain);

        List<Organization> organizations = Arrays.asList(org1, org2);

        // When
        List<OrganizationResponseDto> responseDTOs = organizationMapper.toResponseDTOList(organizations);

        // Then
        assertThat(responseDTOs).hasSize(2);

        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(0).getName()).isEqualTo("Ukrainian Startup");
        assertThat(responseDTOs.get(0).getRegistrationNumber()).isEqualTo("UA-111");
        assertThat(responseDTOs.get(0).getCountry().getName()).isEqualTo("Ukraine");
        assertThat(responseDTOs.get(0).getCountry().getIsoCode()).isEqualTo("UKR");

        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(responseDTOs.get(1).getName()).isEqualTo("Spanish Consultancy");
        assertThat(responseDTOs.get(1).getRegistrationNumber()).isEqualTo("ES-222");
        assertThat(responseDTOs.get(1).getCountry().getName()).isEqualTo("Spain");
        assertThat(responseDTOs.get(1).getCountry().getIsoCode()).isEqualTo("ESP");
    }

    @Test
    void toResponseDTOList_shouldHandleEmptyList() {
        // Given
        List<Organization> organizations = Arrays.asList();

        // When
        List<OrganizationResponseDto> responseDTOs = organizationMapper.toResponseDTOList(organizations);

        // Then
        assertThat(responseDTOs).isEmpty();
    }

    @Test
    void updateEntityFromDTO_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(60);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(30);

        Country oldCountry = new Country();
        oldCountry.setId(1L);
        oldCountry.setName("Old Country");

        Organization existingEntity = new Organization();
        existingEntity.setId(100L);
        existingEntity.setName("Old Organization Name");
        existingEntity.setRegistrationNumber("OLD-REG-123");
        existingEntity.setVatNumber("OLD-VAT-456");
        existingEntity.setAddress("Old Address 123");
        existingEntity.setEmail("old@email.com");
        existingEntity.setPhone("+380001111111");
        existingEntity.setCountry(oldCountry);
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        OrganizationRequestDto updateDTO = new OrganizationRequestDto();
        updateDTO.setName("New Organization Name");
        updateDTO.setRegistrationNumber("NEW-REG-999");
        updateDTO.setVatNumber("NEW-VAT-888");
        updateDTO.setAddress("New Address 456");
        updateDTO.setEmail("new@email.com");
        updateDTO.setPhone("+380002222222");
        updateDTO.setCountryId(99L);

        // When
        organizationMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getName()).isEqualTo("New Organization Name");
        assertThat(existingEntity.getRegistrationNumber()).isEqualTo("NEW-REG-999");
        assertThat(existingEntity.getVatNumber()).isEqualTo("NEW-VAT-888");
        assertThat(existingEntity.getAddress()).isEqualTo("New Address 456");
        assertThat(existingEntity.getEmail()).isEqualTo("new@email.com");
        assertThat(existingEntity.getPhone()).isEqualTo("+380002222222");
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(99L);

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(100L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntityFromDTO_shouldIgnoreNullValues() {
        // Given
        Country existingCountry = new Country();
        existingCountry.setId(5L);

        Organization existingEntity = new Organization();
        existingEntity.setId(50L);
        existingEntity.setName("Existing Org");
        existingEntity.setRegistrationNumber("EXIST-123");
        existingEntity.setVatNumber("EXIST-VAT");
        existingEntity.setAddress("Existing Address");
        existingEntity.setEmail("existing@email.com");
        existingEntity.setPhone("+380111111111");
        existingEntity.setCountry(existingCountry);

        OrganizationRequestDto updateDTO = new OrganizationRequestDto();
        updateDTO.setName("Updated Name Only"); // update this
        updateDTO.setRegistrationNumber(null); // should be ignored
        updateDTO.setVatNumber(null); // should be ignored
        updateDTO.setAddress(null); // should be ignored
        updateDTO.setEmail(null); // should be ignored
        updateDTO.setPhone(null); // should be ignored
        updateDTO.setCountryId(null); // should be ignored

        // When
        organizationMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Updated Name Only"); // updated
        assertThat(existingEntity.getRegistrationNumber()).isEqualTo("EXIST-123"); // unchanged
        assertThat(existingEntity.getVatNumber()).isEqualTo("EXIST-VAT"); // unchanged
        assertThat(existingEntity.getAddress()).isEqualTo("Existing Address"); // unchanged
        assertThat(existingEntity.getEmail()).isEqualTo("existing@email.com"); // unchanged
        assertThat(existingEntity.getPhone()).isEqualTo("+380111111111"); // unchanged
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(5L); // unchanged
    }

    @Test
    void updateEntityFromDTO_shouldChangeCountry() {
        // Given
        Country oldCountry = new Country();
        oldCountry.setId(1L);
        oldCountry.setName("Ukraine");

        Organization existingEntity = new Organization();
        existingEntity.setId(75L);
        existingEntity.setName("Migration Corp");
        existingEntity.setRegistrationNumber("MIG-123");
        existingEntity.setCountry(oldCountry);

        OrganizationRequestDto updateDTO = new OrganizationRequestDto();
        updateDTO.setName("Migration Corp");
        updateDTO.setRegistrationNumber("MIG-123");
        updateDTO.setCountryId(2L); // change country

        // When
        organizationMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(2L);
    }

    @Test
    void updateEntityFromDTO_shouldUpdateMultipleFieldsWhileIgnoringNulls() {
        // Given
        Country existingCountry = new Country();
        existingCountry.setId(10L);

        Organization existingEntity = new Organization();
        existingEntity.setId(80L);
        existingEntity.setName("Partial Update Org");
        existingEntity.setRegistrationNumber("PART-123");
        existingEntity.setVatNumber("PART-VAT-123");
        existingEntity.setAddress("Original Address");
        existingEntity.setEmail("original@email.com");
        existingEntity.setPhone("+380999999999");
        existingEntity.setCountry(existingCountry);

        OrganizationRequestDto updateDTO = new OrganizationRequestDto();
        updateDTO.setName("Partially Updated Org"); // update
        updateDTO.setRegistrationNumber("PART-999"); // update
        updateDTO.setVatNumber(null); // ignore
        updateDTO.setAddress("New Address"); // update
        updateDTO.setEmail(null); // ignore
        updateDTO.setPhone("+380888888888"); // update
        updateDTO.setCountryId(null); // ignore

        // When
        organizationMapper.updateEntityFromDTO(updateDTO, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Partially Updated Org");
        assertThat(existingEntity.getRegistrationNumber()).isEqualTo("PART-999");
        assertThat(existingEntity.getVatNumber()).isEqualTo("PART-VAT-123"); // unchanged
        assertThat(existingEntity.getAddress()).isEqualTo("New Address");
        assertThat(existingEntity.getEmail()).isEqualTo("original@email.com"); // unchanged
        assertThat(existingEntity.getPhone()).isEqualTo("+380888888888");
        assertThat(existingEntity.getCountry().getId()).isEqualTo(10L); // unchanged
    }

    @Test
    void toEntity_shouldHandleMinimalRequiredFields() {
        // Given - only required fields
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Minimal Org");
        requestDTO.setCountryId(7L);

        // When
        Organization entity = organizationMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getName()).isEqualTo("Minimal Org");
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(7L);
        assertThat(entity.getRegistrationNumber()).isNull();
        assertThat(entity.getVatNumber()).isNull();
        assertThat(entity.getAddress()).isNull();
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getPhone()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullCountryId() {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Org");
        requestDTO.setCountryId(null); // ← null!

        // When
        Organization entity = organizationMapper.toEntity(requestDTO);

        // Then
        assertThat(entity.getName()).isEqualTo("Test Org");
        assertThat(entity.getCountry()).isNull();
    }
}