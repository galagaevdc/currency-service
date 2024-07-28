package com.scribe.currency.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class RateDto {
  private String currency;
  private BigDecimal rate;
}
