package com.tarasantoniuk.finance.core.externalexchangerate.exception;

/**
 * Exception thrown when ECB exchange rate synchronization fails.
 * Extends RuntimeException directly — represents an infrastructure/external service failure,
 * not a client input error (InvalidOperationException).
 */
public class ECBSyncException extends RuntimeException {

    public ECBSyncException(String message) {
        super(message);
    }

    public ECBSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ECBSyncException fetchFailed(String url, Throwable cause) {
        return new ECBSyncException("Failed to fetch ECB data from: " + url, cause);
    }

    public static ECBSyncException parseFailed(Throwable cause) {
        return new ECBSyncException("Failed to parse ECB XML response", cause);
    }
}
