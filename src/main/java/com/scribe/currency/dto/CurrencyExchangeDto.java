package com.scribe.currency.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class CurrencyExchangeDto {
  private String base;
  private LocalDateTime actualizationDate;
  private Set<RateDto> rates = new HashSet<>();
}
