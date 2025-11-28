package com.tarasantoniuk.finance.common;

import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main orchestrator for cleaning all test data.
 * Delegates to domain-specific cleaners in correct order.
 */
@Component
public class TestDataCleaner {

    @Autowired
    private TestDataCleanerBanking bankingCleaner;

    @Autowired
    private TestDataCleanerCore coreCleaner;

    /**
     * Cleans all test data in correct order:
     * 1. Banking domain (depends on core)
     * 2. Core domain (base entities)
     */
    public void cleanAll() {
        bankingCleaner.cleanAll();
        coreCleaner.cleanAll();
    }
}