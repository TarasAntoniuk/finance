package com.tarasantoniuk.finance.banking.bankreceipt.controller;

import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bank-receipts")
@Tag(name = "Banking - Bank Receipts", description = "Operations for managing bank receipt documents")
public class BankReceiptController {

    private final BankReceiptService bankReceiptService;

    public BankReceiptController(BankReceiptService bankReceiptService) {
        this.bankReceiptService = bankReceiptService;
    }

    @PostMapping
    @Operation(
            summary = "Create new bank receipt",
            description = "Creates a new bank receipt document in DRAFT status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank receipt created successfully",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Related entity not found"),
            @ApiResponse(responseCode = "409", description = "Receipt with external transaction ID already exists")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Bank receipt data",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = BankReceiptRequestDto.class),
                    examples = @ExampleObject(
                            name = "Customer Payment",
                            value = """
                                    {
                                      "documentDate": "2025-12-02",
                                      "receiptType": "CUSTOMER_PAYMENT",
                                      "amount": 10000.00,
                                      "bankCommission": 50.00,
                                      "accountId": 1,
                                      "counterpartyId": 7,
                                      "counterpartyBankAccountId": 6,
                                      "currencyId": 2,
                                      "organizationId": 1,
                                      "description": "Payment for software development services",
                                      "paymentPurpose": "Invoice #INV-2025-125, December services",
                                      "paymentReference": "INV-2025-125",
                                      "incomingDocumentNumber": "PAY-DEC-12345",
                                      "incomingDocumentDate": "2025-12-01",
                                      "transactionDate": "2025-12-02",
                                      "valueDate": "2025-12-02",
                                      "externalTransactionId": "BANK-TXN-2025-DEC-001"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<BankReceiptResponseDto> create(
            @Valid @RequestBody BankReceiptRequestDto requestDto) {
        BankReceiptResponseDto response = bankReceiptService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update bank receipt",
            description = "Updates an existing bank receipt. Only DRAFT receipts can be modified."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt updated successfully",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Cannot update receipt in current status")
    })
    public ResponseEntity<BankReceiptResponseDto> update(
            @Parameter(description = "Bank receipt ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BankReceiptRequestDto requestDto) {
        BankReceiptResponseDto response = bankReceiptService.update(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get bank receipt by ID",
            description = "Retrieves a bank receipt by its ID with all related entities"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt found",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found")
    })
    public ResponseEntity<BankReceiptResponseDto> getById(
            @Parameter(description = "Bank receipt ID", required = true)
            @PathVariable Long id) {
        BankReceiptResponseDto response = bankReceiptService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all bank receipts",
            description = """
                    Retrieves all bank receipts with pagination and sorting.
                    
                    Sort examples:
                    - sort=documentDate,desc
                    - sort=amount,asc
                    - sort=id,desc
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts retrieved successfully"
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getAll(
            //@Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Get bank receipts by account",
            description = "Retrieves all bank receipts for a specific bank account"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts for account retrieved successfully"
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByAccountId(
            @Parameter(description = "Bank account ID", required = true)
            @PathVariable Long accountId,
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByAccountId(accountId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/counterparty/{counterpartyId}")
    @Operation(
            summary = "Get bank receipts by counterparty",
            description = "Retrieves all bank receipts from a specific counterparty"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts for counterparty retrieved successfully"
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByCounterpartyId(
            @Parameter(description = "Counterparty ID", required = true)
            @PathVariable Long counterpartyId,
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByCounterpartyId(counterpartyId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get bank receipts by status",
            description = "Retrieves all bank receipts with a specific status (DRAFT, POSTED, CANCELLED)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts with specified status retrieved successfully"
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByStatus(
            @Parameter(
                    description = "Document status",
                    required = true,
                    example = "DRAFT"
            )
            @PathVariable DocumentStatus status,
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(
            summary = "Get bank receipts by date range",
            description = "Retrieves all bank receipts within a specified date range"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts in date range retrieved successfully"
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByDateRange(
            @Parameter(
                    description = "Start date (inclusive)",
                    required = true,
                    example = "2025-10-01"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(
                    description = "End date (inclusive)",
                    required = true,
                    example = "2025-12-31"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        // Convert dates to datetime range (start of startDate to end of endDate)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByDateTimeRange(startDateTime, endDateTime, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete bank receipt",
            description = "Deletes a bank receipt. Only DRAFT receipts can be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank receipt deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete receipt in current status")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Bank receipt ID", required = true)
            @PathVariable Long id) {
        bankReceiptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/post")
    @Operation(
            summary = "Post bank receipt",
            description = "Posts the bank receipt (changes status from DRAFT to POSTED) and creates corresponding accounting entries"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt successfully posted",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Document already posted or cannot be posted"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found")
    })
    public ResponseEntity<BankReceiptResponseDto> post(
            @Parameter(description = "Bank receipt ID") @PathVariable Long id) {
        BankReceiptResponseDto responseDto = bankReceiptService.post(id);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{id}/unpost")
    @Operation(
            summary = "Unpost bank receipt",
            description = "Unposts the bank receipt (changes status from POSTED to DRAFT) and removes corresponding accounting entries"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt successfully unposted",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Document not posted or cannot be unposted"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found")
    })
    public ResponseEntity<BankReceiptResponseDto> unpost(
            @Parameter(description = "Bank receipt ID") @PathVariable Long id) {
        BankReceiptResponseDto responseDto = bankReceiptService.unpost(id);
        return ResponseEntity.ok(responseDto);
    }
}