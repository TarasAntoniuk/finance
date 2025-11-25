package com.tarasantoniuk.finance.common.document.enums;

public enum DocumentStatus {
    /**
     * Document is created but not yet posted to accounting
     */
    DRAFT,

    /**
     * Document is posted and accounting events are created
     */
    POSTED,


    /**
     * Document is cancelled and reversal events are created
     */
    CANCELLED
}
