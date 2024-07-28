package com.scribe.currency.repository;

import com.scribe.currency.entity.CurrencyEntity;
import com.scribe.currency.entity.CurrencyExchangeRateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyExchangeRepository
    extends JpaRepository<CurrencyExchangeRateEntity, Long> {
  Optional<CurrencyExchangeRateEntity> findByFromAndTo(
      CurrencyEntity fromCurrency, CurrencyEntity toCurrency);
}
