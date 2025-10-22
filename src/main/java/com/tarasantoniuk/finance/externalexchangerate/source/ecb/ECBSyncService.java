package com.tarasantoniuk.finance.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ECBSyncService {

    private static final Logger log = LoggerFactory.getLogger(ECBSyncService.class);
    private static final String SOURCE = "ECB";
    private static final String SOURCE_URL = "https://www.ecb.europa.eu/stats/eurofxref";

    private final ECBClient client;
    private final ExternalExchangeRateRepository rateRepository;
    private final CurrencyRepository currencyRepository;

    public ECBSyncService(ECBClient client,
                          ExternalExchangeRateRepository rateRepository,
                          CurrencyRepository currencyRepository) {
        this.client = client;
        this.rateRepository = rateRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public int syncDaily() {
        log.info("Syncing daily ECB rates");
        return sync(client.fetchDaily());
    }

    @Transactional
    public int syncHistory() {
        log.info("Syncing ECB history - this may take several minutes");
        return sync(client.fetchHistory());
    }

    private int sync(Map<LocalDate, Map<String, BigDecimal>> data) {
        if (data == null || data.isEmpty()) {
            log.warn("No data to sync");
            return 0;
        }

        // Завантажити EUR один раз
        Currency eur = currencyRepository.findByCode("EUR")
                .orElseThrow(() -> new RuntimeException("EUR currency not found"));

        // Завантажити всі валюти один раз
        Map<String, Currency> currencyMap = currencyRepository.findAll().stream()
                .collect(HashMap::new, (m, c) -> m.put(c.getCode(), c), HashMap::putAll);

        log.info("Found {} currencies in database", currencyMap.size());

        // Завантажити існуючі курси один раз
        LocalDate minDate = data.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = data.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        Set<String> existingKeys = rateRepository
                .findByExchangeDateBetweenAndSource(minDate, maxDate, SOURCE)
                .stream()
                .map(r -> buildKey(r.getExchangeDate(), r.getCurrencyFrom().getId(), r.getCurrencyTo().getId()))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        log.info("Found {} existing rates for date range {} to {}", existingKeys.size(), minDate, maxDate);

        // Batch insert нових записів
        List<ExternalExchangeRate> newRates = new ArrayList<>();
        int skipped = 0;

        for (var dateEntry : data.entrySet()) {
            LocalDate date = dateEntry.getKey();

            for (var rateEntry : dateEntry.getValue().entrySet()) {
                String currencyCode = rateEntry.getKey();
                Currency targetCurrency = currencyMap.get(currencyCode);

                if (targetCurrency == null) {
                    skipped++;
                    continue;
                }

                String key = buildKey(date, eur.getId(), targetCurrency.getId());
                if (existingKeys.contains(key)) {
                    skipped++;
                    continue;
                }

                // Створити новий запис
                ExternalExchangeRate rate = new ExternalExchangeRate();
                rate.setExchangeDate(date);
                rate.setCurrencyFrom(eur);
                rate.setCurrencyTo(targetCurrency);
                rate.setRate(rateEntry.getValue());
                rate.setSource(SOURCE);
                rate.setSourceUrl(SOURCE_URL);
                rate.setIsActive(true);

                newRates.add(rate);

                // Batch save кожні 1000 записів
                if (newRates.size() >= 1000) {
                    rateRepository.saveAll(newRates);
                    rateRepository.flush();
                    log.info("Saved batch of {} rates", newRates.size());
                    newRates.clear();
                }
            }
        }

        // Зберегти залишок
        if (!newRates.isEmpty()) {
            rateRepository.saveAll(newRates);
            rateRepository.flush();
            log.info("Saved final batch of {} rates", newRates.size());
        }

        int saved = data.values().stream().mapToInt(Map::size).sum() - skipped;
        log.info("ECB sync completed: {} saved, {} skipped", saved, skipped);
        return saved;
    }

    private String buildKey(LocalDate date, Long fromId, Long toId) {
        return date + "-" + fromId + "-" + toId;
    }
}