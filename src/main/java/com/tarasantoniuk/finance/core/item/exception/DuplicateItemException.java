package com.tarasantoniuk.finance.core.item.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

public class DuplicateItemException extends ResourceAlreadyExistsException {

    public DuplicateItemException(String code) {
        super("Item", "code", code);
    }
}
