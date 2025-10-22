package com.tarasantoniuk.finance.externalexchangerate.repository;

import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalExchangeRateRepository extends JpaRepository<ExternalExchangeRate, Long> {

    Optional<ExternalExchangeRate> findByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
            LocalDate exchangeDate, Long currencyFromId, Long currencyToId, String source);

    List<ExternalExchangeRate> findByExchangeDate(LocalDate exchangeDate);

    List<ExternalExchangeRate> findByExchangeDateAndSource(LocalDate exchangeDate, String source);

    List<ExternalExchangeRate> findByCurrencyFromIdAndCurrencyToId(Long currencyFromId, Long currencyToId);

    List<ExternalExchangeRate> findBySource(String source);

    List<ExternalExchangeRate> findByExchangeDateBetween(LocalDate startDate, LocalDate endDate);

    List<ExternalExchangeRate> findByExchangeDateBetweenAndCurrencyFromIdAndCurrencyToId(
            LocalDate startDate, LocalDate endDate, Long currencyFromId, Long currencyToId);

    @Query("SELECT e FROM ExternalExchangeRate e WHERE e.exchangeDate = :date " +
            "AND e.currencyFrom.id = :currencyFromId AND e.currencyTo.id = :currencyToId " +
            "AND e.isActive = true ORDER BY e.createdAt DESC")
    List<ExternalExchangeRate> findActiveRatesByDateAndCurrencyPair(
            @Param("date") LocalDate date,
            @Param("currencyFromId") Long currencyFromId,
            @Param("currencyToId") Long currencyToId);

    @Query("SELECT e FROM ExternalExchangeRate e WHERE e.exchangeDate <= :date " +
            "AND e.currencyFrom.id = :currencyFromId AND e.currencyTo.id = :currencyToId " +
            "AND e.isActive = true ORDER BY e.exchangeDate DESC")
    List<ExternalExchangeRate> findLatestRateBeforeDate(
            @Param("date") LocalDate date,
            @Param("currencyFromId") Long currencyFromId,
            @Param("currencyToId") Long currencyToId);

    boolean existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
            LocalDate exchangeDate, Long currencyFromId, Long currencyToId, String source);

    List<ExternalExchangeRate> findByIsActive(Boolean isActive);

    List<ExternalExchangeRate> findByExchangeDateBetweenAndSource(
            LocalDate startDate, LocalDate endDate, String source);
}