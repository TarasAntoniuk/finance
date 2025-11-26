package com.tarasantoniuk.finance.common;

import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataCleaner {

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    /**
     * Очищає всі дані у правильному порядку (від залежних до головних)
     */
    public void cleanAll() {
        bankReceiptRepository.deleteAll();
        bankAccountRepository.deleteAll();
        counterpartyRepository.deleteAll();
        bankRepository.deleteAll();
        organizationRepository.deleteAll();
        currencyRepository.deleteAll();
        countryRepository.deleteAll();
    }

    /**
     * Очищає тільки дані банківської системи
     */
    public void cleanBankingData() {
        bankReceiptRepository.deleteAll();
        bankAccountRepository.deleteAll();
        bankRepository.deleteAll();
    }

    /**
     * Очищає тільки основні довідники
     */
    public void cleanCoreData() {
        organizationRepository.deleteAll();
        currencyRepository.deleteAll();
        countryRepository.deleteAll();
    }
}