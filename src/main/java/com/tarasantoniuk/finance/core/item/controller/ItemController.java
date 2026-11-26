package com.tarasantoniuk.finance.core.item.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.item.dto.ItemRequest;
import com.tarasantoniuk.finance.core.item.dto.ItemResponse;
import com.tarasantoniuk.finance.core.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@Validated
@Tag(name = "Core - Item", description = "Item catalog: services and goods used on document lines")
public class ItemController {

    private static final int MAX_PAGE_SIZE = 500;

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @Operation(summary = "Create item")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search items", description = "Case-insensitive search by code or name, sorted by name")
    public ResponseEntity<PageResponse<ItemResponse>> search(
            @Parameter(description = "Part of the code or name") @RequestParam(required = false) String q,
            @Parameter(description = "Only active items") @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(itemService.search(q, activeOnly, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item")
    public ResponseEntity<ItemResponse> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate item")
    public ResponseEntity<ItemResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.setActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate item")
    public ResponseEntity<ItemResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.setActive(id, false));
    }
}
