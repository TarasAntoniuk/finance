package com.tarasantoniuk.finance.core.externalexchangerate.source.ecb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ECBSchedulerTest {

    @Mock
    private ECBSyncService syncService;

    @InjectMocks
    private ECBScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Set default values for properties
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", true);
    }

    // ========== STARTUP TESTS ==========

    @Test
    void onStartup_WhenSyncEnabledAndInitialLoadEnabled_ShouldSyncHistory() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", true);
        when(syncService.syncHistory()).thenReturn(100);

        // When
        scheduler.onStartup();

        // Then
        verify(syncService, times(1)).syncHistory();
    }

    @Test
    void onStartup_WhenSyncDisabled_ShouldNotSyncHistory() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", false);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", true);

        // When
        scheduler.onStartup();

        // Then
        verify(syncService, never()).syncHistory();
    }

    @Test
    void onStartup_WhenInitialLoadDisabled_ShouldNotSyncHistory() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", false);

        // When
        scheduler.onStartup();

        // Then
        verify(syncService, never()).syncHistory();
    }

    @Test
    void onStartup_WhenBothDisabled_ShouldNotSyncHistory() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", false);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", false);

        // When
        scheduler.onStartup();

        // Then
        verify(syncService, never()).syncHistory();
    }

    @Test
    void onStartup_WhenSyncThrowsException_ShouldHandleGracefully() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", true);
        when(syncService.syncHistory()).thenThrow(new RuntimeException("ECB API unavailable"));

        // When - should not throw exception
        scheduler.onStartup();

        // Then
        verify(syncService, times(1)).syncHistory();
    }

    @Test
    void onStartup_WhenSyncReturnsZero_ShouldCompleteSuccessfully() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "initialLoadEnabled", true);
        when(syncService.syncHistory()).thenReturn(0);

        // When
        scheduler.onStartup();

        // Then
        verify(syncService, times(1)).syncHistory();
    }

    // ========== DAILY SYNC TESTS ==========

    @Test
    void dailySync_WhenSyncEnabled_ShouldSyncDaily() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        when(syncService.syncDaily()).thenReturn(10);

        // When
        scheduler.dailySync();

        // Then
        verify(syncService, times(1)).syncDaily();
    }

    @Test
    void dailySync_WhenSyncDisabled_ShouldNotSyncDaily() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", false);

        // When
        scheduler.dailySync();

        // Then
        verify(syncService, never()).syncDaily();
    }

    @Test
    void dailySync_WhenSyncThrowsException_ShouldHandleGracefully() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        when(syncService.syncDaily()).thenThrow(new RuntimeException("Network error"));

        // When - should not throw exception
        scheduler.dailySync();

        // Then
        verify(syncService, times(1)).syncDaily();
    }

    @Test
    void dailySync_WhenSyncReturnsZero_ShouldCompleteSuccessfully() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        when(syncService.syncDaily()).thenReturn(0);

        // When
        scheduler.dailySync();

        // Then
        verify(syncService, times(1)).syncDaily();
    }

    @Test
    void dailySync_WhenSyncReturnsLargeNumber_ShouldCompleteSuccessfully() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        when(syncService.syncDaily()).thenReturn(500);

        // When
        scheduler.dailySync();

        // Then
        verify(syncService, times(1)).syncDaily();
    }
}