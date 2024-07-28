package com.scribe.currency.repository;

import com.scribe.currency.entity.CurrencyEntity;
import com.scribe.currency.entity.CurrencyExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchangeRateEntity, Long> {
    Optional<CurrencyExchangeRateEntity> findByFromAndTo(CurrencyEntity fromCurrency, CurrencyEntity toCurrency);
}