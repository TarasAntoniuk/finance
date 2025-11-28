package com.tarasantoniuk.finance.core.common;

import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty.CounterpartyType;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating core domain entities in integration tests.
 * All methods create valid, persisted entities ready to use.
 * For invalid test cases, create objects manually in the test itself.
 */
@Component
public class TestDataFactoryCore {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    // ==================== Countries ====================

    /**
     * Creates Ukraine country (UKR)
     */
    public Country createUkraine() {
        Country country = new Country();
        country.setName("Ukraine");
        country.setIsoCode("UKR");
        return countryRepository.save(country);
    }

    /**
     * Creates USA country (USA)
     */
    public Country createUSA() {
        Country country = new Country();
        country.setName("USA");
        country.setIsoCode("USA");
        return countryRepository.save(country);
    }

    /**
     * Creates Poland country (POL)
     */
    public Country createPoland() {
        Country country = new Country();
        country.setName("Poland");
        country.setIsoCode("POL");
        return countryRepository.save(country);
    }

    /**
     * Creates custom country with specified name and ISO code
     */
    public Country createCountry(String name, String isoCode) {
        Country country = new Country();
        country.setName(name);
        country.setIsoCode(isoCode);
        return countryRepository.save(country);
    }

    // ==================== Currencies ====================

    /**
     * Creates Ukrainian Hryvnia (UAH)
     */
    public Currency createUAH() {
        Currency currency = new Currency();
        currency.setCode("UAH");
        currency.setName("Ukrainian Hryvnia");
        currency.setSymbol("₴");
        currency.setNumericCode("980");
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        return currencyRepository.save(currency);
    }

    /**
     * Creates US Dollar (USD)
     */
    public Currency createUSD() {
        Currency currency = new Currency();
        currency.setCode("USD");
        currency.setName("US Dollar");
        currency.setSymbol("$");
        currency.setNumericCode("840");
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        return currencyRepository.save(currency);
    }

    /**
     * Creates Euro (EUR)
     */
    public Currency createEUR() {
        Currency currency = new Currency();
        currency.setCode("EUR");
        currency.setName("Euro");
        currency.setSymbol("€");
        currency.setNumericCode("978");
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        return currencyRepository.save(currency);
    }

    /**
     * Creates custom currency with specified parameters
     */
    public Currency createCurrency(String code, String name, String symbol, String numericCode) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(name);
        currency.setSymbol(symbol);
        currency.setNumericCode(numericCode);
        currency.setMinorUnit(2);
        currency.setIsActive(true);
        return currencyRepository.save(currency);
    }

    // ==================== Organizations ====================

    /**
     * Creates default test organization
     */
    public Organization createDefaultOrganization(Country country) {
        return createOrganization("Test Organization", "12345678", country);
    }

    /**
     * Creates organization with custom name and VAT number
     */
    public Organization createOrganization(String name, String vatNumber, Country country) {
        Organization organization = new Organization();
        organization.setName(name);
        organization.setVatNumber(vatNumber);
        organization.setCountry(country);
        return organizationRepository.save(organization);
    }

    // ==================== Counterparties ====================

    /**
     * Creates default CUSTOMER counterparty
     */
    public Counterparty createCustomerCounterparty() {
        return createCounterparty("Test Customer", "CUST-001", CounterpartyType.CUSTOMER);
    }

    /**
     * Creates default SUPPLIER counterparty
     */
    public Counterparty createSupplierCounterparty() {
        return createCounterparty("Test Supplier", "SUPP-001", CounterpartyType.SUPPLIER);
    }

    /**
     * Creates counterparty with custom parameters
     */
    public Counterparty createCounterparty(String name, String code, CounterpartyType type) {
        Counterparty counterparty = new Counterparty();
        counterparty.setName(name);
        counterparty.setCode(code);
        counterparty.setType(type);
        counterparty.setIsActive(true);
        return counterpartyRepository.save(counterparty);
    }
}