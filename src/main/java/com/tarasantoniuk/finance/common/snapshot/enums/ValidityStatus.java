package com.tarasantoniuk.finance.common.snapshot.enums;

/**
 * Represents the validity status of balance snapshots across the application.
 * <p>
 * This enum is used across all modules that implement snapshot functionality to track
 * whether cached balance data is currently valid or needs recalculation.
 * <p>
 * Examples of modules using snapshots:
 * <ul>
 *     <li>Bank account balances - cached balances for performance</li>
 *     <li>Inventory balances - stock quantity snapshots</li>
 *     <li>Settlement balances - counterparty balance snapshots</li>
 * </ul>
 */
public enum ValidityStatus {
    /**
     * Snapshots are invalid and need recalculation
     */
    INVALID,

    /**
     * Snapshots are currently being recalculated
     */
    RECALCULATING,

    /**
     * Snapshots are valid and up-to-date
     */
    VALID
}
