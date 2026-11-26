package com.tarasantoniuk.finance.core.item.service;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.core.item.dto.ItemRequest;
import com.tarasantoniuk.finance.core.item.dto.ItemResponse;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import com.tarasantoniuk.finance.core.item.exception.DuplicateItemException;
import com.tarasantoniuk.finance.core.item.exception.ItemNotFoundException;
import com.tarasantoniuk.finance.core.item.mapper.ItemMapper;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Item catalog. Deliberately global, like counterparties: the same service or product is bought
 * by several organizations, and visibility comes from the scoped documents that reference it.
 */
@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        validateVat(request);
        ensureCodeIsFree(request.code());
        Item item = itemRepository.save(itemMapper.toEntity(request));
        return itemMapper.toResponse(item);
    }

    public ItemResponse getById(Long id) {
        return itemMapper.toResponse(findOrThrow(id));
    }

    public PageResponse<ItemResponse> search(String text, boolean activeOnly, int page, int size) {
        String searchText = text == null ? "" : text.trim();
        Page<Item> items = itemRepository.search(
                searchText, activeOnly, PageRequest.of(page, size, Sort.by("name", "id")));
        return PageResponse.of(items, items.map(itemMapper::toResponse).getContent());
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findOrThrow(id);
        validateVat(request);
        if (!item.getCode().equals(request.code())) {
            ensureCodeIsFree(request.code());
        }
        itemMapper.updateEntity(request, item);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.delete(findOrThrow(id));
    }

    @Transactional
    public ItemResponse setActive(Long id, boolean active) {
        Item item = findOrThrow(id);
        item.setIsActive(active);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    private Item findOrThrow(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    private void ensureCodeIsFree(String code) {
        if (itemRepository.existsByCode(code)) {
            throw new DuplicateItemException(code);
        }
    }

    private void validateVat(ItemRequest request) {
        if (!request.vatCategory().isConsistentWith(request.vatRate())) {
            String expected = request.vatCategory() == VatCategory.S ? "a positive" : "a zero";
            throw new ValidationException("VAT category " + request.vatCategory()
                    + " requires " + expected + " rate, got " + request.vatRate());
        }
    }
}
