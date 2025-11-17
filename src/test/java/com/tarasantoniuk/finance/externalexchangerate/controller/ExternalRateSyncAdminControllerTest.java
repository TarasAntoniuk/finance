package com.tarasantoniuk.finance.externalexchangerate.controller;

import com.tarasantoniuk.finance.externalexchangerate.source.ecb.ECBSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalRateSyncAdminController.class)
class ExternalRateSyncAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ECBSyncService ecbSyncService;

    @Test
    void syncECBDaily_WhenSuccessful_ShouldReturnOkWithCount() throws Exception {
        // Given
        when(ecbSyncService.syncDaily()).thenReturn(42);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(42));
    }

    @Test
    void syncECBDaily_WhenNoNewRates_ShouldReturnOkWithZero() throws Exception {
        // Given
        when(ecbSyncService.syncDaily()).thenReturn(0);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(0));
    }

    @Test
    void syncECBDaily_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(ecbSyncService.syncDaily()).thenThrow(new RuntimeException("ECB API is unavailable"));

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/daily"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("ECB API is unavailable"));
    }

    @Test
    void syncECBHistory_WhenSuccessful_ShouldReturnOkWithCount() throws Exception {
        // Given
        when(ecbSyncService.syncHistory()).thenReturn(1250);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(1250));
    }

    @Test
    void syncECBHistory_WhenNoNewRates_ShouldReturnOkWithZero() throws Exception {
        // Given
        when(ecbSyncService.syncHistory()).thenReturn(0);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(0));
    }

    @Test
    void syncECBHistory_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(ecbSyncService.syncHistory()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/history"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Database connection failed"));
    }

    @Test
    void syncECBDaily_WhenLargeDataset_ShouldReturnOkWithHighCount() throws Exception {
        // Given
        when(ecbSyncService.syncDaily()).thenReturn(999);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(999));
    }

    @Test
    void syncECBHistory_WhenLargeDataset_ShouldReturnOkWithHighCount() throws Exception {
        // Given
        when(ecbSyncService.syncHistory()).thenReturn(5000);

        // When & Then
        mockMvc.perform(post("/api/admin/external-rate-sync/ecb/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.saved").value(5000));
    }
}