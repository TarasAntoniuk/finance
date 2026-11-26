package com.tarasantoniuk.finance.core.counterparty.repository;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {

    /**
     * Find all counterparties with country loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @param pageable pagination information
     * @return page of counterparties with country eagerly loaded
     */
    @Query(value = "SELECT c FROM Counterparty c " +
            "LEFT JOIN FETCH c.country",
            countQuery = "SELECT COUNT(c) FROM Counterparty c")
    Page<Counterparty> findAllWithCountry(Pageable pageable);

    /**
     * Find counterparty by ID with country loaded.
     *
     * @param id counterparty ID
     * @return optional counterparty with country
     */
    @Query("SELECT c FROM Counterparty c " +
            "LEFT JOIN FETCH c.country " +
            "WHERE c.id = :id")
    Optional<Counterparty> findByIdWithCountry(@Param("id") Long id);

    // Keep original method for duplicate check (doesn't need country)
    boolean existsByCode(String code);

    /**
     * Row lock on the counterparty. Settlement writes take it first, so all postings that change
     * what is owed to one counterparty run one after another.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Counterparty c WHERE c.id = :id")
    Optional<Counterparty> findByIdForUpdate(@Param("id") Long id);
}
