package com.tarasantoniuk.finance.bank.repository;

import com.tarasantoniuk.finance.bank.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    List<Bank> findByCountryId(Long countryId);

    List<Bank> findByIsActiveTrue();

    Optional<Bank> findBySwiftCode(String swiftCode);
}