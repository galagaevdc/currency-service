package com.scribe.currency.repository;

import com.scribe.currency.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long> {
  CurrencyEntity findByCode(String code);
}
