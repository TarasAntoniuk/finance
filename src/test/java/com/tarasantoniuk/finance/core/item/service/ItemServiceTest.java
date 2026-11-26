package com.tarasantoniuk.finance.core.item.service;

import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.core.item.dto.ItemRequest;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import com.tarasantoniuk.finance.core.item.exception.DuplicateItemException;
import com.tarasantoniuk.finance.core.item.exception.ItemNotFoundException;
import com.tarasantoniuk.finance.core.item.mapper.ItemMapper;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @InjectMocks
    private ItemService itemService;

    @Test
    void create_WhenCodeIsFree_ShouldSaveItem() {
        when(itemRepository.existsByCode("CONS-H")).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = itemService.create(request("CONS-H", VatCategory.S, "20.00"));

        assertThat(response.code()).isEqualTo("CONS-H");
        assertThat(response.vatRate()).isEqualByComparingTo("20.00");
    }

    @Test
    void create_WhenCodeExists_ShouldThrowDuplicate() {
        when(itemRepository.existsByCode("CONS-H")).thenReturn(true);

        assertThatThrownBy(() -> itemService.create(request("CONS-H", VatCategory.S, "20.00")))
                .isInstanceOf(DuplicateItemException.class);
        verify(itemRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({"S, 0.00", "Z, 20.00", "E, 7.00", "AE, 20.00"})
    void create_WhenRateContradictsCategory_ShouldThrowValidation(VatCategory category, String rate) {
        assertThatThrownBy(() -> itemService.create(request("X", category, rate)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("VAT category " + category);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_WhenCodeUnchanged_ShouldNotCheckUniqueness() {
        Item existing = item(1L, "CONS-H");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        var response = itemService.update(1L, request("CONS-H", VatCategory.Z, "0.00"));

        assertThat(response.vatCategory()).isEqualTo(VatCategory.Z);
        verify(itemRepository, never()).existsByCode(any());
    }

    @Test
    void update_WhenNewCodeTaken_ShouldThrowDuplicate() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item(1L, "CONS-H")));
        when(itemRepository.existsByCode("CONS-D")).thenReturn(true);

        assertThatThrownBy(() -> itemService.update(1L, request("CONS-D", VatCategory.S, "20.00")))
                .isInstanceOf(DuplicateItemException.class);
    }

    @Test
    void getById_WhenMissing_ShouldThrowNotFound() {
        when(itemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(9L)).isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void setActive_ShouldToggleFlag() {
        Item existing = item(1L, "CONS-H");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        assertThat(itemService.setActive(1L, false).isActive()).isFalse();
    }

    private static ItemRequest request(String code, VatCategory category, String rate) {
        return new ItemRequest(code, "Consulting", ItemType.SERVICE, "HUR", category, new BigDecimal(rate), null);
    }

    private static Item item(Long id, String code) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setName("Consulting");
        item.setType(ItemType.SERVICE);
        item.setUnitCode("HUR");
        item.setVatCategory(VatCategory.S);
        item.setVatRate(new BigDecimal("20.00"));
        return item;
    }
}
