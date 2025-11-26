package com.tarasantoniuk.finance.common;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
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

@Component
public class TestDataFactory {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // ==================== Countries ====================

    public Country createUkraine() {
        Country country = new Country();
        country.setName("Ukraine");
        country.setIsoCode("UKR");
        return countryRepository.save(country);
    }

    public Country createUSA() {
        Country country = new Country();
        country.setName("USA");
        country.setIsoCode("USA");
        return countryRepository.save(country);
    }

    public Country createCountry(String name, String isoCode) {
        Country country = new Country();
        country.setName(name);
        country.setIsoCode(isoCode);
        return countryRepository.save(country);
    }

    // ==================== Currencies ====================

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

    public Organization createOrganization(String name, String code, Country country) {
        Organization organization = new Organization();
        organization.setName(name);
        organization.setVatNumber(code);
        organization.setCountry(country);
        return organizationRepository.save(organization);
    }

    public Organization createDefaultOrganization(Country country) {
        return createOrganization("Test Organization", "TEST-ORG", country);
    }

    // ==================== Banks ====================

    public Bank createBank(String name, String swiftCode, Country country, Boolean isActive) {
        Bank bank = new Bank();
        bank.setName(name);
        bank.setSwiftCode(swiftCode);
        bank.setCountry(country);
        bank.setIsActive(isActive);
        return bankRepository.save(bank);
    }

    public Bank createPrivatBank(Country country) {
        Bank bank = new Bank();
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");
        bank.setCountry(country);
        bank.setAddress("1 Hrushevskoho St, Kyiv");
        bank.setPhoneNumber("+380443639999");
        bank.setWebsite("www.privatbank.ua");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    public Bank createMonobank(Country country) {
        Bank bank = new Bank();
        bank.setName("Monobank");
        bank.setSwiftCode("MBNKUA2X");
        bank.setCountry(country);
        bank.setWebsite("www.monobank.ua");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    // ==================== Counterparties ====================

    public Counterparty createCounterparty(String name, String code, CounterpartyType type) {
        Counterparty counterparty = new Counterparty();
        counterparty.setName(name);
        counterparty.setCode(code);
        counterparty.setType(type);
        counterparty.setIsActive(true);
        return counterpartyRepository.save(counterparty);
    }

    public Counterparty createDefaultCounterparty() {
        return createCounterparty("Test Counterparty", "CP-001", CounterpartyType.CUSTOMER);
    }

    // ==================== Bank Accounts ====================

    public BankAccount createBankAccountOrganization(Bank bank, Currency currency, Organization organization,
                                         String accountNumber) {
        BankAccount account = new BankAccount();
        account.setBank(bank);
        account.setCurrency(currency);
        account.setHolderId(organization.getId());
        account.setAccountNumber(accountNumber);
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setIsDefault(true);
        return bankAccountRepository.save(account);
    }

    public BankAccount createBankAccountCounterparty(Bank bank, Currency currency, Counterparty counterparty,
                                         String accountNumber) {
        BankAccount account = new BankAccount();
        account.setBank(bank);
        account.setCurrency(currency);
        account.setHolderId(counterparty.getId());
        account.setAccountNumber(accountNumber);
        account.setHolderType(AccountHolderType.COUNTERPARTY);
        account.setIsDefault(true);
        return bankAccountRepository.save(account);
    }

    public BankAccount createDefaultBankAccount(Bank bank, Currency currency, Organization organization) {
        return createBankAccountOrganization(bank, currency, organization, "UA123456789012345678901234567");
    }
}