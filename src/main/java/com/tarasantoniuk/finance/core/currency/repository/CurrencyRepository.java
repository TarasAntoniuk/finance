package com.tarasantoniuk.finance.core.currency.repository;

import com.tarasantoniuk.finance.core.currency.entity.Currency;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    Optional<Currency> findByCode(String code);

    Optional<Currency> findByNumericCode(String numericCode);

    List<Currency> findByIsActive(Boolean isActive);

    boolean existsByCode(String code);

    boolean existsByNumericCode(String numericCode);

    List<Currency> findByNameContainingIgnoreCase(String name, Pageable pageable);
}