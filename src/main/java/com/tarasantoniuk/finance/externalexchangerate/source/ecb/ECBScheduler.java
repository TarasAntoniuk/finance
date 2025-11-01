package com.tarasantoniuk.finance.externalexchangerate.source.ecb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ECBScheduler {

    private static final Logger log = LoggerFactory.getLogger(ECBScheduler.class);

    private final ECBSyncService syncService;

    @Value("${ecb.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${ecb.sync.initial-load:true}")
    private boolean initialLoadEnabled;

    public ECBScheduler(ECBSyncService syncService) {
        this.syncService = syncService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (!syncEnabled || !initialLoadEnabled) {
            log.info("ECB initial sync disabled");
            return;
        }

        log.info("Checking ECB initial sync");
        try {
            syncService.syncHistory();
            log.info("ECB history loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load ECB history on startup", e);
        }
    }

    @Scheduled(cron = "0 5 16 * * *", zone = "Europe/Paris")
    public void dailySync() {
        if (!syncEnabled) {
            log.info("ECB daily sync disabled");
            return;
        }

        log.info("Running daily ECB sync");
        try {
            syncService.syncDaily();
            log.info("Daily ECB sync completed successfully");
        } catch (Exception e) {
            log.error("Daily ECB sync failed", e);
        }
    }
}