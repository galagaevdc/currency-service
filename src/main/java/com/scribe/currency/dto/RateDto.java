package com.scribe.currency.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateDto {
    private String currency;
    private BigDecimal rate;
}
