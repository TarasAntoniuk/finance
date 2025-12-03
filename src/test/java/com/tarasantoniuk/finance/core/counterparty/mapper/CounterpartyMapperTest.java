package com.tarasantoniuk.finance.core.counterparty.mapper;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class CounterpartyMapperTest extends BaseIntegrationTest {

    @Autowired
    private CounterpartyMapper counterpartyMapper;

    @Test
    void toEntity_shouldMapAllFieldsFromRequestDto() {
        // Given
        CounterpartyRequestDto requestDto = new CounterpartyRequestDto();
        requestDto.setName("ABC Corporation");
        requestDto.setCode("ABC-001");
        requestDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        requestDto.setTaxNumber("1234567890");
        requestDto.setEmail("contact@abc.com");
        requestDto.setPhone("+380501234567");
        requestDto.setAddress("123 Main Street, Kyiv");
        requestDto.setCountryId(1L);
        requestDto.setIsActive(true);
        requestDto.setNotes("Important customer");

        // When
        Counterparty entity = counterpartyMapper.toEntity(requestDto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // should be ignored
        assertThat(entity.getName()).isEqualTo("ABC Corporation");
        assertThat(entity.getCode()).isEqualTo("ABC-001");
        assertThat(entity.getType()).isEqualTo(Counterparty.CounterpartyType.CUSTOMER);
        assertThat(entity.getTaxNumber()).isEqualTo("1234567890");
        assertThat(entity.getEmail()).isEqualTo("contact@abc.com");
        assertThat(entity.getPhone()).isEqualTo("+380501234567");
        assertThat(entity.getAddress()).isEqualTo("123 Main Street, Kyiv");
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(1L);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getNotes()).isEqualTo("Important customer");
        assertThat(entity.getCreatedAt()).isNull(); // should be ignored
        assertThat(entity.getUpdatedAt()).isNull(); // should be ignored
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        CounterpartyRequestDto requestDto = new CounterpartyRequestDto();
        requestDto.setName("Minimal Counterparty");
        requestDto.setCode("MIN-001");
        requestDto.setType(Counterparty.CounterpartyType.SUPPLIER);
        requestDto.setTaxNumber(null);
        requestDto.setEmail(null);
        requestDto.setPhone(null);
        requestDto.setAddress(null);
        requestDto.setCountryId(null);
        requestDto.setIsActive(null);
        requestDto.setNotes(null);

        // When
        Counterparty entity = counterpartyMapper.toEntity(requestDto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Minimal Counterparty");
        assertThat(entity.getCode()).isEqualTo("MIN-001");
        assertThat(entity.getType()).isEqualTo(Counterparty.CounterpartyType.SUPPLIER);
        assertThat(entity.getTaxNumber()).isNull();
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getPhone()).isNull();
        assertThat(entity.getAddress()).isNull();
        assertThat(entity.getCountry()).isNull();
        assertThat(entity.getIsActive()).isNull(); // Will be set by entity default or DB
        assertThat(entity.getNotes()).isNull();
    }

    @Test
    void toEntity_shouldHandleAllCounterpartyTypes() {
        // Test CUSTOMER
        CounterpartyRequestDto customer = new CounterpartyRequestDto();
        customer.setName("Customer Inc");
        customer.setCode("CUST-001");
        customer.setType(Counterparty.CounterpartyType.CUSTOMER);

        Counterparty customerEntity = counterpartyMapper.toEntity(customer);
        assertThat(customerEntity.getType()).isEqualTo(Counterparty.CounterpartyType.CUSTOMER);

        // Test SUPPLIER
        CounterpartyRequestDto supplier = new CounterpartyRequestDto();
        supplier.setName("Supplier LLC");
        supplier.setCode("SUPP-001");
        supplier.setType(Counterparty.CounterpartyType.SUPPLIER);

        Counterparty supplierEntity = counterpartyMapper.toEntity(supplier);
        assertThat(supplierEntity.getType()).isEqualTo(Counterparty.CounterpartyType.SUPPLIER);

        // Test BOTH
        CounterpartyRequestDto both = new CounterpartyRequestDto();
        both.setName("Partner Corp");
        both.setCode("BOTH-001");
        both.setType(Counterparty.CounterpartyType.BOTH);

        Counterparty bothEntity = counterpartyMapper.toEntity(both);
        assertThat(bothEntity.getType()).isEqualTo(Counterparty.CounterpartyType.BOTH);
    }

    @Test
    void toEntity_shouldCreateCountryWithOnlyIdSet() {
        // Given
        CounterpartyRequestDto requestDto = new CounterpartyRequestDto();
        requestDto.setName("Test Counterparty");
        requestDto.setCode("TEST-001");
        requestDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        requestDto.setCountryId(10L);

        // When
        Counterparty entity = counterpartyMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getCountry()).isNotNull();
        assertThat(entity.getCountry().getId()).isEqualTo(10L);
        assertThat(entity.getCountry().getName()).isNull();
        assertThat(entity.getCountry().getIsoCode()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Currency currency = new Currency();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setName("US Dollar");
        currency.setSymbol("$");

        Country country = new Country();
        country.setId(2L);
        country.setName("United States");
        country.setIsoCode("USA");
        country.setPhoneCode("+1");
        country.setCurrency(currency);
        country.setCreatedAt(now.minusDays(100));
        country.setUpdatedAt(now.minusDays(50));

        Counterparty entity = new Counterparty();
        entity.setId(10L);
        entity.setName("Global Trading Co");
        entity.setCode("GLB-001");
        entity.setType(Counterparty.CounterpartyType.BOTH);
        entity.setTaxNumber("US-TAX-123456");
        entity.setEmail("info@globaltrading.com");
        entity.setPhone("+12125551234");
        entity.setAddress("Wall Street 1, New York");
        entity.setCountry(country);
        entity.setIsActive(true);
        entity.setNotes("Premium partner");
        entity.setCreatedAt(now.minusDays(30));
        entity.setUpdatedAt(now.minusDays(5));

        // When
        CounterpartyResponseDto responseDto = counterpartyMapper.toResponse(entity);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(10L);
        assertThat(responseDto.getName()).isEqualTo("Global Trading Co");
        assertThat(responseDto.getCode()).isEqualTo("GLB-001");
        assertThat(responseDto.getType()).isEqualTo(Counterparty.CounterpartyType.BOTH);
        assertThat(responseDto.getTaxNumber()).isEqualTo("US-TAX-123456");
        assertThat(responseDto.getEmail()).isEqualTo("info@globaltrading.com");
        assertThat(responseDto.getPhone()).isEqualTo("+12125551234");
        assertThat(responseDto.getAddress()).isEqualTo("Wall Street 1, New York");
        assertThat(responseDto.getIsActive()).isTrue();
        assertThat(responseDto.getNotes()).isEqualTo("Premium partner");
        assertThat(responseDto.getCreatedAt()).isEqualTo(now.minusDays(30));
        assertThat(responseDto.getUpdatedAt()).isEqualTo(now.minusDays(5));

        // Check country mapping
        assertThat(responseDto.getCountry()).isNotNull();
        assertThat(responseDto.getCountry().getId()).isEqualTo(2L);
        assertThat(responseDto.getCountry().getName()).isEqualTo("United States");
        assertThat(responseDto.getCountry().getIsoCode()).isEqualTo("USA");
        assertThat(responseDto.getCountry().getPhoneCode()).isEqualTo("+1");

        // Check nested currency
        assertThat(responseDto.getCountry().getCurrency()).isNotNull();
        assertThat(responseDto.getCountry().getCurrency().getCode()).isEqualTo("USD");
        assertThat(responseDto.getCountry().getCurrency().getSymbol()).isEqualTo("$");
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        // Given
        Counterparty entity = new Counterparty();
        entity.setId(20L);
        entity.setName("Sparse Counterparty");
        entity.setCode("SPARSE-001");
        entity.setType(Counterparty.CounterpartyType.CUSTOMER);
        entity.setTaxNumber(null);
        entity.setEmail(null);
        entity.setPhone(null);
        entity.setAddress(null);
        entity.setCountry(null);
        entity.setIsActive(false);
        entity.setNotes(null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(null);

        // When
        CounterpartyResponseDto responseDto = counterpartyMapper.toResponse(entity);

        // Then
        assertThat(responseDto.getName()).isEqualTo("Sparse Counterparty");
        assertThat(responseDto.getTaxNumber()).isNull();
        assertThat(responseDto.getEmail()).isNull();
        assertThat(responseDto.getPhone()).isNull();
        assertThat(responseDto.getAddress()).isNull();
        assertThat(responseDto.getCountry()).isNull();
        assertThat(responseDto.getIsActive()).isFalse();
        assertThat(responseDto.getNotes()).isNull();
        assertThat(responseDto.getUpdatedAt()).isNull();
    }

    @Test
    void toResponseList_shouldMapAllEntities() {
        // Given
        Country ukraine = new Country();
        ukraine.setId(1L);
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");

        Country spain = new Country();
        spain.setId(2L);
        spain.setName("Spain");
        spain.setIsoCode("ESP");

        Counterparty cp1 = new Counterparty();
        cp1.setId(1L);
        cp1.setName("Ukrainian Supplier");
        cp1.setCode("UA-SUPP-001");
        cp1.setType(Counterparty.CounterpartyType.SUPPLIER);
        cp1.setCountry(ukraine);

        Counterparty cp2 = new Counterparty();
        cp2.setId(2L);
        cp2.setName("Spanish Customer");
        cp2.setCode("ES-CUST-001");
        cp2.setType(Counterparty.CounterpartyType.CUSTOMER);
        cp2.setCountry(spain);

        List<Counterparty> counterparties = Arrays.asList(cp1, cp2);

        // When
        List<CounterpartyResponseDto> responseDtos = counterpartyMapper.toResponseList(counterparties);

        // Then
        assertThat(responseDtos).hasSize(2);

        assertThat(responseDtos.get(0).getName()).isEqualTo("Ukrainian Supplier");
        assertThat(responseDtos.get(0).getCode()).isEqualTo("UA-SUPP-001");
        assertThat(responseDtos.get(0).getType()).isEqualTo(Counterparty.CounterpartyType.SUPPLIER);
        assertThat(responseDtos.get(0).getCountry().getName()).isEqualTo("Ukraine");

        assertThat(responseDtos.get(1).getName()).isEqualTo("Spanish Customer");
        assertThat(responseDtos.get(1).getCode()).isEqualTo("ES-CUST-001");
        assertThat(responseDtos.get(1).getType()).isEqualTo(Counterparty.CounterpartyType.CUSTOMER);
        assertThat(responseDtos.get(1).getCountry().getName()).isEqualTo("Spain");
    }

    @Test
    void toResponseList_shouldHandleEmptyList() {
        // Given
        List<Counterparty> counterparties = Arrays.asList();

        // When
        List<CounterpartyResponseDto> responseDtos = counterpartyMapper.toResponseList(counterparties);

        // Then
        assertThat(responseDtos).isEmpty();
    }

    @Test
    void updateEntity_shouldUpdateOnlyMappedFields() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(90);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(45);

        Country oldCountry = new Country();
        oldCountry.setId(1L);
        oldCountry.setName("Old Country");

        Counterparty existingEntity = new Counterparty();
        existingEntity.setId(100L);
        existingEntity.setName("Old Name");
        existingEntity.setCode("OLD-001");
        existingEntity.setType(Counterparty.CounterpartyType.CUSTOMER);
        existingEntity.setTaxNumber("OLD-TAX");
        existingEntity.setEmail("old@email.com");
        existingEntity.setPhone("+380111111111");
        existingEntity.setAddress("Old Address");
        existingEntity.setCountry(oldCountry);
        existingEntity.setIsActive(false);
        existingEntity.setNotes("Old notes");
        existingEntity.setCreatedAt(originalCreatedAt);
        existingEntity.setUpdatedAt(originalUpdatedAt);

        CounterpartyRequestDto updateDto = new CounterpartyRequestDto();
        updateDto.setName("New Name");
        updateDto.setCode("NEW-001");
        updateDto.setType(Counterparty.CounterpartyType.SUPPLIER);
        updateDto.setTaxNumber("NEW-TAX");
        updateDto.setEmail("new@email.com");
        updateDto.setPhone("+380222222222");
        updateDto.setAddress("New Address");
        updateDto.setCountryId(99L);
        updateDto.setIsActive(true);
        updateDto.setNotes("New notes");

        // When
        counterpartyMapper.updateEntity(updateDto, existingEntity);

        // Then
        // Updated fields
        assertThat(existingEntity.getName()).isEqualTo("New Name");
        assertThat(existingEntity.getCode()).isEqualTo("NEW-001");
        assertThat(existingEntity.getType()).isEqualTo(Counterparty.CounterpartyType.SUPPLIER);
        assertThat(existingEntity.getTaxNumber()).isEqualTo("NEW-TAX");
        assertThat(existingEntity.getEmail()).isEqualTo("new@email.com");
        assertThat(existingEntity.getPhone()).isEqualTo("+380222222222");
        assertThat(existingEntity.getAddress()).isEqualTo("New Address");
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(99L);
        assertThat(existingEntity.getIsActive()).isTrue();
        assertThat(existingEntity.getNotes()).isEqualTo("New notes");

        // Ignored fields - should remain unchanged
        assertThat(existingEntity.getId()).isEqualTo(100L);
        assertThat(existingEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @Test
    void updateEntity_shouldIgnoreNullValues() {
        // Given
        Country existingCountry = new Country();
        existingCountry.setId(5L);

        Counterparty existingEntity = new Counterparty();
        existingEntity.setId(50L);
        existingEntity.setName("Existing Name");
        existingEntity.setCode("EXIST-001");
        existingEntity.setType(Counterparty.CounterpartyType.BOTH);
        existingEntity.setTaxNumber("EXIST-TAX");
        existingEntity.setEmail("exist@email.com");
        existingEntity.setPhone("+380333333333");
        existingEntity.setAddress("Existing Address");
        existingEntity.setCountry(existingCountry);
        existingEntity.setIsActive(true);
        existingEntity.setNotes("Existing notes");

        CounterpartyRequestDto updateDto = new CounterpartyRequestDto();
        updateDto.setName("Updated Name"); // update
        updateDto.setCode(null); // ignore
        updateDto.setType(null); // ignore
        updateDto.setTaxNumber(null); // ignore
        updateDto.setEmail(null); // ignore
        updateDto.setPhone(null); // ignore
        updateDto.setAddress(null); // ignore
        updateDto.setCountryId(null); // ignore
        updateDto.setIsActive(null); // ignore
        updateDto.setNotes(null); // ignore

        // When
        counterpartyMapper.updateEntity(updateDto, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Updated Name"); // updated
        assertThat(existingEntity.getCode()).isEqualTo("EXIST-001"); // unchanged
        assertThat(existingEntity.getType()).isEqualTo(Counterparty.CounterpartyType.BOTH); // unchanged
        assertThat(existingEntity.getTaxNumber()).isEqualTo("EXIST-TAX"); // unchanged
        assertThat(existingEntity.getEmail()).isEqualTo("exist@email.com"); // unchanged
        assertThat(existingEntity.getPhone()).isEqualTo("+380333333333"); // unchanged
        assertThat(existingEntity.getAddress()).isEqualTo("Existing Address"); // unchanged
        assertThat(existingEntity.getCountry().getId()).isEqualTo(5L); // unchanged
        assertThat(existingEntity.getIsActive()).isTrue(); // unchanged
        assertThat(existingEntity.getNotes()).isEqualTo("Existing notes"); // unchanged
    }

    @Test
    void updateEntity_shouldChangeCountry() {
        // Given
        Country oldCountry = new Country();
        oldCountry.setId(1L);

        Counterparty existingEntity = new Counterparty();
        existingEntity.setId(75L);
        existingEntity.setName("Migrating Company");
        existingEntity.setCode("MIG-001");
        existingEntity.setType(Counterparty.CounterpartyType.CUSTOMER);
        existingEntity.setCountry(oldCountry);

        CounterpartyRequestDto updateDto = new CounterpartyRequestDto();
        updateDto.setName("Migrating Company");
        updateDto.setCode("MIG-001");
        updateDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        updateDto.setCountryId(2L); // change country

        // When
        counterpartyMapper.updateEntity(updateDto, existingEntity);

        // Then
        assertThat(existingEntity.getCountry()).isNotNull();
        assertThat(existingEntity.getCountry().getId()).isEqualTo(2L);
    }

    @Test
    void updateEntity_shouldChangeType() {
        // Given
        Counterparty existingEntity = new Counterparty();
        existingEntity.setId(80L);
        existingEntity.setName("Type Changer");
        existingEntity.setCode("TYPE-001");
        existingEntity.setType(Counterparty.CounterpartyType.CUSTOMER);

        CounterpartyRequestDto updateDto = new CounterpartyRequestDto();
        updateDto.setName("Type Changer");
        updateDto.setCode("TYPE-001");
        updateDto.setType(Counterparty.CounterpartyType.BOTH); // change type

        // When
        counterpartyMapper.updateEntity(updateDto, existingEntity);

        // Then
        assertThat(existingEntity.getType()).isEqualTo(Counterparty.CounterpartyType.BOTH);
    }

    @Test
    void updateEntity_shouldPartiallyUpdateMultipleFields() {
        // Given
        Country existingCountry = new Country();
        existingCountry.setId(10L);

        Counterparty existingEntity = new Counterparty();
        existingEntity.setId(90L);
        existingEntity.setName("Partial Update Co");
        existingEntity.setCode("PART-001");
        existingEntity.setType(Counterparty.CounterpartyType.SUPPLIER);
        existingEntity.setTaxNumber("PART-TAX");
        existingEntity.setEmail("partial@email.com");
        existingEntity.setPhone("+380444444444");
        existingEntity.setAddress("Partial Address");
        existingEntity.setCountry(existingCountry);
        existingEntity.setIsActive(false);
        existingEntity.setNotes("Old notes");

        CounterpartyRequestDto updateDto = new CounterpartyRequestDto();
        updateDto.setName("Partially Updated Co"); // update
        updateDto.setCode("PART-999"); // update
        updateDto.setType(null); // ignore
        updateDto.setTaxNumber("NEW-TAX"); // update
        updateDto.setEmail(null); // ignore
        updateDto.setPhone("+380555555555"); // update
        updateDto.setAddress(null); // ignore
        updateDto.setCountryId(null); // ignore
        updateDto.setIsActive(true); // update
        updateDto.setNotes(null); // ignore

        // When
        counterpartyMapper.updateEntity(updateDto, existingEntity);

        // Then
        assertThat(existingEntity.getName()).isEqualTo("Partially Updated Co");
        assertThat(existingEntity.getCode()).isEqualTo("PART-999");
        assertThat(existingEntity.getType()).isEqualTo(Counterparty.CounterpartyType.SUPPLIER); // unchanged
        assertThat(existingEntity.getTaxNumber()).isEqualTo("NEW-TAX");
        assertThat(existingEntity.getEmail()).isEqualTo("partial@email.com"); // unchanged
        assertThat(existingEntity.getPhone()).isEqualTo("+380555555555");
        assertThat(existingEntity.getAddress()).isEqualTo("Partial Address"); // unchanged
        assertThat(existingEntity.getCountry().getId()).isEqualTo(10L); // unchanged
        assertThat(existingEntity.getIsActive()).isTrue();
        assertThat(existingEntity.getNotes()).isEqualTo("Old notes"); // unchanged
    }

    @Test
    void toEntity_shouldHandleIsActiveFalse() {
        // Given
        CounterpartyRequestDto requestDto = new CounterpartyRequestDto();
        requestDto.setName("Inactive Counterparty");
        requestDto.setCode("INACT-001");
        requestDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        requestDto.setIsActive(false);

        // When
        Counterparty entity = counterpartyMapper.toEntity(requestDto);

        // Then
        assertThat(entity.getIsActive()).isFalse();
    }
}