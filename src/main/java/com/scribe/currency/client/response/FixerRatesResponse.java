package com.scribe.currency.client.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FixerRatesResponse extends AbstractFixerResponse {
  private Map<String, BigDecimal> rates;
}
