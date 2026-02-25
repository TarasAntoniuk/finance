package com.tarasantoniuk.finance.core.externalexchangerate.exception;

import com.tarasantoniuk.finance.common.exception.InvalidOperationException;

/**
 * Exception thrown when ECB exchange rate synchronization fails
 */
public class ECBSyncException extends InvalidOperationException {

    public ECBSyncException(String message) {
        super(message);
    }

    public ECBSyncException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public static ECBSyncException fetchFailed(String url, Throwable cause) {
        return new ECBSyncException("Failed to fetch ECB data from: " + url, cause);
    }

    public static ECBSyncException parseFailed(Throwable cause) {
        return new ECBSyncException("Failed to parse ECB XML response", cause);
    }
}
