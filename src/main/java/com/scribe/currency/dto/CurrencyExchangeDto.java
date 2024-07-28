package com.scribe.currency.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class CurrencyExchangeDto {
    private String base;
    private LocalDateTime actualizationDate;
    private Set<RateDto> rates = new HashSet<>();
}
