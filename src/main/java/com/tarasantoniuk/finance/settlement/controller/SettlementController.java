package com.tarasantoniuk.finance.settlement.controller;

import com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable;
import com.tarasantoniuk.finance.settlement.dto.OpenItemResponse;
import com.tarasantoniuk.finance.settlement.service.SettlementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@Tag(name = "Settlements", description = "What is owed to counterparties: balances and open invoices")
public class SettlementController {

    private final SettlementQueryService settlementQueryService;

    public SettlementController(SettlementQueryService settlementQueryService) {
        this.settlementQueryService = settlementQueryService;
    }

    @GetMapping("/payables")
    @Operation(summary = "Balance per counterparty and currency",
            description = "Positive balance: we owe; negative: prepaid. Overdue counts unpaid invoices past their due date.")
    public ResponseEntity<List<CounterpartyPayable>> getPayables(
            @Parameter(description = "ADMIN only: limit to one organization")
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(settlementQueryService.getPayables(organizationId));
    }

    @GetMapping("/counterparties/{counterpartyId}/open-items")
    @Operation(summary = "Open invoices and prepayments of a counterparty", description = "Oldest due date first")
    public ResponseEntity<List<OpenItemResponse>> getOpenItems(
            @PathVariable Long counterpartyId,
            @Parameter(description = "ADMIN only: limit to one organization")
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(settlementQueryService.getOpenItems(counterpartyId, organizationId));
    }
}
