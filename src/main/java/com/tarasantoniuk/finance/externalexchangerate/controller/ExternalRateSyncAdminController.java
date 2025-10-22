package com.tarasantoniuk.finance.externalexchangerate.controller;

import com.tarasantoniuk.finance.externalexchangerate.source.ecb.ECBSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/external-rate-sync")
public class ExternalRateSyncAdminController {

    private final ECBSyncService ecbSyncService;

    public ExternalRateSyncAdminController(ECBSyncService ecbSyncService) {
        this.ecbSyncService = ecbSyncService;
    }

    @PostMapping("/ecb/daily")
    public ResponseEntity<Map<String, Object>> syncECBDaily() {
        try {
            int count = ecbSyncService.syncDaily();
            return ResponseEntity.ok(Map.of("success", true, "saved", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/ecb/history")
    public ResponseEntity<Map<String, Object>> syncECBHistory() {
        try {
            int count = ecbSyncService.syncHistory();
            return ResponseEntity.ok(Map.of("success", true, "saved", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}