package com.tarasantoniuk.finance.purchase.purchaseinvoice.controller;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceResponse;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceSummary;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.service.PurchaseInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-invoices")
@Validated
@Tag(name = "Purchase - Invoice", description = "Invoices received from suppliers (EN 16931 fields)")
public class PurchaseInvoiceController {

    private static final int MAX_PAGE_SIZE = 500;

    private final PurchaseInvoiceService invoiceService;

    public PurchaseInvoiceController(PurchaseInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @Operation(summary = "Create a draft purchase invoice", description = "Totals and VAT breakdown are computed from the lines")
    @ApiResponse(responseCode = "201", description = "Draft created")
    @ApiResponse(responseCode = "409", description = "The supplier invoice number is already registered")
    public ResponseEntity<PurchaseInvoiceResponse> create(@Valid @RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase invoice with lines and VAT breakdown")
    public ResponseEntity<PurchaseInvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search purchase invoices", description = "Newest issue date first")
    public ResponseEntity<PageResponse<PurchaseInvoiceSummary>> search(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(invoiceService.search(supplierId, status, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a draft purchase invoice", description = "Replaces header and lines")
    public ResponseEntity<PurchaseInvoiceResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a draft purchase invoice")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "Post: open the debt to the supplier", description = "Consumes the supplier's unused prepayments")
    public ResponseEntity<PurchaseInvoiceResponse> post(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.post(id));
    }

    @PostMapping("/{id}/unpost")
    @Operation(summary = "Unpost: reverse the debt", description = "409 while payments are allocated to the invoice")
    public ResponseEntity<PurchaseInvoiceResponse> unpost(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.unpost(id));
    }
}
