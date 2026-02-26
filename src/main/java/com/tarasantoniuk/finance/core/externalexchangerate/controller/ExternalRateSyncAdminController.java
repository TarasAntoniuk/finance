package com.tarasantoniuk.finance.core.externalexchangerate.controller;

import com.tarasantoniuk.finance.core.externalexchangerate.source.ecb.ECBSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/external-rate-sync")
@Tag(name = "Admin - Exchange Rate Sync", description = "Admin operations for external exchange rate synchronization")
public class ExternalRateSyncAdminController {

    private final ECBSyncService ecbSyncService;

    public ExternalRateSyncAdminController(ECBSyncService ecbSyncService) {
        this.ecbSyncService = ecbSyncService;
    }

    @PostMapping("/ecb/daily")
    @Operation(summary = "Sync daily ECB exchange rates", description = "Synchronize latest exchange rates from European Central Bank (ECB). Fetches current day rates and saves them to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
            @ApiResponse(responseCode = "503", description = "ECB service unavailable or sync failed")
    })
    public ResponseEntity<Map<String, Object>> syncECBDaily() {
        int count = ecbSyncService.syncDaily();
        return ResponseEntity.ok(Map.of("success", true, "saved", count));
    }

    @PostMapping("/ecb/history")
    @Operation(summary = "Sync historical ECB exchange rates", description = "Synchronize historical exchange rates from European Central Bank (ECB). Fetches all available historical rates and saves them to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
            @ApiResponse(responseCode = "503", description = "ECB service unavailable or sync failed")
    })
    public ResponseEntity<Map<String, Object>> syncECBHistory() {
        int count = ecbSyncService.syncHistory();
        return ResponseEntity.ok(Map.of("success", true, "saved", count));
    }
}