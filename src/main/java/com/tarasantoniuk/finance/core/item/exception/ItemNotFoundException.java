package com.tarasantoniuk.finance.core.item.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

public class ItemNotFoundException extends ResourceNotFoundException {

    public ItemNotFoundException(Long id) {
        super("Item", "id", id);
    }
}
