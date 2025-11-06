package com.tarasantoniuk.finance.counterparty.repository;

import com.tarasantoniuk.finance.counterparty.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {

    Optional<Counterparty> findByCode(String code);

    boolean existsByCode(String code);

    List<Counterparty> findByIsActive(Boolean isActive);

    List<Counterparty> findByType(Counterparty.CounterpartyType type);

    @Query("SELECT c FROM Counterparty c WHERE c.isActive = true AND c.type IN :types")
    List<Counterparty> findActiveByTypes(@Param("types") List<Counterparty.CounterpartyType> types);

    @Query("SELECT c FROM Counterparty c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Counterparty> searchByNameOrCode(@Param("searchTerm") String searchTerm);
}