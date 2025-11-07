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
        log.info("🔹 Starting syncHistory");
        long totalStart = System.currentTimeMillis();

        long step1 = System.currentTimeMillis();
        Map<LocalDate, Map<String, BigDecimal>> data = client.fetchHistory();
        log.info("⏱️  Step 1 (fetchHistory): {} ms, records: {}",
                System.currentTimeMillis() - step1,
                data != null ? data.size() : 0);

        long step2 = System.currentTimeMillis();
        int result = sync(data);
        log.info("⏱️  Step 2 (sync): {} ms", System.currentTimeMillis() - step2);

        log.info("🔹 Total syncHistory time: {} ms", System.currentTimeMillis() - totalStart);
        return result;
    }

    private int sync(Map<LocalDate, Map<String, BigDecimal>> data) {
        long syncStart = System.currentTimeMillis();

        if (data == null || data.isEmpty()) {
            log.warn("No data to sync");
            return 0;
        }

        long step1 = System.currentTimeMillis();
        Currency eur = currencyRepository.findByCode("EUR")
                .orElseThrow(() -> new RuntimeException("EUR currency not found"));
        log.info("⏱️  Loading EUR: {} ms", System.currentTimeMillis() - step1);

        long step2 = System.currentTimeMillis();
        Map<String, Currency> currencyMap = currencyRepository.findAll().stream()
                .collect(HashMap::new, (m, c) -> m.put(c.getCode(), c), HashMap::putAll);
        log.info("⏱️  Loading all currencies: {} ms, count: {}",
                System.currentTimeMillis() - step2, currencyMap.size());

        LocalDate minDate = data.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = data.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        long step3 = System.currentTimeMillis();
        Set<String> existingKeys = rateRepository
                .findByExchangeDateBetweenAndSource(minDate, maxDate, SOURCE)
                .stream()
                .map(r -> buildKey(r.getExchangeDate(), r.getCurrencyFrom().getId(), r.getCurrencyTo().getId()))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        log.info("⏱️  Loading existing rates: {} ms, count: {} (date range: {} - {})",
                System.currentTimeMillis() - step3, existingKeys.size(), minDate, maxDate);

        long step4 = System.currentTimeMillis();
        List<ExternalExchangeRate> newRates = new ArrayList<>();
        int skipped = 0;
        int batchCount = 0;

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

                ExternalExchangeRate rate = new ExternalExchangeRate();
                rate.setExchangeDate(date);
                rate.setCurrencyFrom(eur);
                rate.setCurrencyTo(targetCurrency);
                rate.setRate(rateEntry.getValue());
                rate.setSource(SOURCE);
                rate.setSourceUrl(SOURCE_URL);
                rate.setIsActive(true);

                newRates.add(rate);

                if (newRates.size() >= 1000) {
                    long batchStart = System.currentTimeMillis();
                    rateRepository.saveAll(newRates);
                    rateRepository.flush();
                    batchCount++;
                    log.info("⏱️  Saved batch #{}: {} records in {} ms",
                            batchCount, newRates.size(), System.currentTimeMillis() - batchStart);
                    newRates.clear();
                }
            }
        }

        if (!newRates.isEmpty()) {
            long batchStart = System.currentTimeMillis();
            rateRepository.saveAll(newRates);
            rateRepository.flush();
            log.info("⏱️  Saved final batch: {} records in {} ms",
                    newRates.size(), System.currentTimeMillis() - batchStart);
        }

        log.info("⏱️  Data processing and saving: {} ms", System.currentTimeMillis() - step4);

        int saved = data.values().stream().mapToInt(Map::size).sum() - skipped;
        log.info("🔹 sync() completed in {} ms: {} saved, {} skipped",
                System.currentTimeMillis() - syncStart, saved, skipped);
        return saved;
    }

    private String buildKey(LocalDate date, Long fromId, Long toId) {
        return date + "-" + fromId + "-" + toId;
    }
}