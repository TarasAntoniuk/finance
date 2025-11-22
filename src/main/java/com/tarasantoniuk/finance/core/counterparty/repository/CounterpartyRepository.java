package com.tarasantoniuk.finance.core.counterparty.repository;

import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {

    boolean existsByCode(String code);

}