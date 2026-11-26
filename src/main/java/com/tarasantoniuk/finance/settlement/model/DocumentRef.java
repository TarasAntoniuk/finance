package com.tarasantoniuk.finance.settlement.model;

import java.util.Objects;

/**
 * Reference to a document by type name and id. The settlement module does not depend on the
 * modules that own the documents.
 */
public record DocumentRef(String type, Long id) {

    public DocumentRef {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }
}
