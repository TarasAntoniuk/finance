package com.tarasantoniuk.finance.core.common;

import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cleaner for core domain entities in integration tests.
 * Deletes data in correct order to avoid foreign key violations.
 */
@Component
public class TestDataCleanerCore {

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    /**
     * Deletes all core domain test data in correct order
     * Order: Counterparties → Organizations → Currencies → Countries
     */
    public void cleanAll() {
        counterpartyRepository.deleteAll();
        organizationRepository.deleteAll();
        currencyRepository.deleteAll();
        countryRepository.deleteAll();
    }
}