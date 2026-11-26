package com.tarasantoniuk.finance.settlement.model;

import java.util.Objects;

/**
 * Balance key of counterparty settlements. A contract id will join it later.
 */
public record SettlementKey(Long organizationId, Long counterpartyId, Long currencyId) {

    public SettlementKey {
        Objects.requireNonNull(organizationId, "organizationId");
        Objects.requireNonNull(counterpartyId, "counterpartyId");
        Objects.requireNonNull(currencyId, "currencyId");
    }
}
