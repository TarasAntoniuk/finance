package com.tarasantoniuk.finance.core.country.repository;

import com.tarasantoniuk.finance.core.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    /**
     * Find all countries with currency loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @return list of countries with currency eagerly loaded
     */
    @Query("SELECT c FROM Country c " +
            "LEFT JOIN FETCH c.currency")
    List<Country> findAllWithCurrency();

    /**
     * Find country by ID with currency loaded.
     *
     * @param id country ID
     * @return optional country with currency
     */
    @Query("SELECT c FROM Country c " +
            "LEFT JOIN FETCH c.currency " +
            "WHERE c.id = :id")
    Optional<Country> findByIdWithCurrency(@Param("id") Long id);

    /**
     * Find country by ISO code with currency loaded.
     *
     * @param isoCode ISO country code
     * @return optional country with currency
     */
    @Query("SELECT c FROM Country c " +
            "LEFT JOIN FETCH c.currency " +
            "WHERE c.isoCode = :isoCode")
    Optional<Country> findByIsoCodeWithCurrency(@Param("isoCode") String isoCode);

    // Keep original methods for duplicate checks (don't need currency)
    Optional<Country> findByIsoCode(String isoCode);
    boolean existsByIsoCode(String isoCode);
}