package com.tarasantoniuk.finance.core.externalexchangerate.repository;

import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalExchangeRateRepository extends JpaRepository<ExternalExchangeRate, Long> {

    /**
     * Find all exchange rates with currencies loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @param pageable pagination parameters
     * @return paginated exchange rates with currencies eagerly loaded
     */
    @Query(value = "SELECT DISTINCT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "ORDER BY e.exchangeDate DESC, e.id DESC",
            countQuery = "SELECT COUNT(e) FROM ExternalExchangeRate e")
    Page<ExternalExchangeRate> findAllWithCurrencies(Pageable pageable);

    /**
     * Find exchange rate by ID with currencies loaded.
     *
     * @param id exchange rate ID
     * @return optional exchange rate with currencies
     */
    @Query("SELECT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.id = :id")
    Optional<ExternalExchangeRate> findByIdWithCurrencies(@Param("id") Long id);

    /**
     * Find exchange rates by date with currencies loaded.
     *
     * @param date exchange date
     * @return list of exchange rates with currencies
     */
    @Query("SELECT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.exchangeDate = :date")
    List<ExternalExchangeRate> findByExchangeDateWithCurrencies(@Param("date") LocalDate date);

    /**
     * Find exchange rates by date and source with currencies loaded.
     *
     * @param date   exchange date
     * @param source data source
     * @return list of exchange rates with currencies
     */
    @Query("SELECT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.exchangeDate = :date AND e.source = :source")
    List<ExternalExchangeRate> findByExchangeDateAndSourceWithCurrencies(
            @Param("date") LocalDate date,
            @Param("source") String source);

    /**
     * Find exchange rates within date range with currencies loaded.
     *
     * @param startDate start of date range
     * @param endDate   end of date range
     * @param pageable  pagination parameters
     * @return paginated exchange rates with currencies
     */
    @Query(value = "SELECT DISTINCT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.exchangeDate BETWEEN :startDate AND :endDate " +
            "ORDER BY e.exchangeDate DESC, e.id DESC",
            countQuery = "SELECT COUNT(e) FROM ExternalExchangeRate e " +
                    "WHERE e.exchangeDate BETWEEN :startDate AND :endDate")
    Page<ExternalExchangeRate> findByExchangeDateBetweenWithCurrencies(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find exchange rates by currency pair with currencies loaded.
     *
     * @param currencyFromId source currency ID
     * @param currencyToId   target currency ID
     * @param pageable       pagination parameters
     * @return paginated exchange rates with currencies
     */
    @Query(value = "SELECT DISTINCT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.currencyFrom.id = :currencyFromId " +
            "AND e.currencyTo.id = :currencyToId " +
            "ORDER BY e.exchangeDate DESC, e.id DESC",
            countQuery = "SELECT COUNT(e) FROM ExternalExchangeRate e " +
                    "WHERE e.currencyFrom.id = :currencyFromId " +
                    "AND e.currencyTo.id = :currencyToId")
    Page<ExternalExchangeRate> findByCurrencyPairWithCurrencies(
            @Param("currencyFromId") Long currencyFromId,
            @Param("currencyToId") Long currencyToId,
            Pageable pageable);

    /**
     * Find latest exchange rates by currency from and date with currencies loaded.
     *
     * @param date           reference date
     * @param currencyFromId source currency ID
     * @return list of latest exchange rates with currencies
     */
    @Query("SELECT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.currencyFrom.id = :currencyFromId " +
            "AND e.exchangeDate = (" +
            "    SELECT MAX(e2.exchangeDate) " +
            "    FROM ExternalExchangeRate e2 " +
            "    WHERE e2.currencyFrom.id = :currencyFromId " +
            "    AND e2.exchangeDate <= :date" +
            ")")
    List<ExternalExchangeRate> findLatestRatesByCurrencyFromWithCurrencies(
            @Param("date") LocalDate date,
            @Param("currencyFromId") Long currencyFromId);

    /**
     * Find latest active exchange rate before or on specified date with currencies loaded.
     *
     * @param date           reference date
     * @param currencyFromId source currency ID
     * @param currencyToId   target currency ID
     * @return list of exchange rates with currencies
     */
    @Query("SELECT e FROM ExternalExchangeRate e " +
            "LEFT JOIN FETCH e.currencyFrom " +
            "LEFT JOIN FETCH e.currencyTo " +
            "WHERE e.exchangeDate <= :date " +
            "AND e.currencyFrom.id = :currencyFromId " +
            "AND e.currencyTo.id = :currencyToId " +
            "AND e.isActive = true " +
            "ORDER BY e.exchangeDate DESC")
    List<ExternalExchangeRate> findLatestRateBeforeDateWithCurrencies(
            @Param("date") LocalDate date,
            @Param("currencyFromId") Long currencyFromId,
            @Param("currencyToId") Long currencyToId);

    /**
     * Check if exchange rate exists for given parameters.
     *
     * @param exchangeDate   exchange date
     * @param currencyFromId source currency ID
     * @param currencyToId   target currency ID
     * @param source         data source
     * @return true if exists
     */
    boolean existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
            LocalDate exchangeDate, Long currencyFromId, Long currencyToId, String source);

    /**
     * Find exchange rates between dates and source (for bulk operations).
     *
     * @param startDate start date
     * @param endDate   end date
     * @param source    data source
     * @return list of exchange rates
     */
    List<ExternalExchangeRate> findByExchangeDateBetweenAndSource(
            LocalDate startDate, LocalDate endDate, String source);
}