package com.tarasantoniuk.finance.common.document.exception;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;

public class InvalidDocumentStatusException extends RuntimeException {

    public InvalidDocumentStatusException(String message) {
        super(message);
    }

    public InvalidDocumentStatusException(DocumentStatus current, DocumentStatus expected, String operation) {
        super(String.format(
                "Cannot %s document with status %s. Expected status: %s",
                operation, current, expected
        ));
    }
}