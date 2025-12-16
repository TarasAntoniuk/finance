package com.tarasantoniuk.finance.common.document.enums;

/**
 * Represents the type of balance snapshot creation.
 */
public enum SnapshotType {
    /**
     * Automatic snapshot created at the end of business day
     */
    END_OF_DAY,

    /**
     * Snapshot created in real-time after transaction posting
     */
    REAL_TIME,

    /**
     * Manually triggered snapshot (for reconciliation or admin purposes)
     */
    MANUAL
}