package com.scribe.currency.client.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class FixerRatesResponse extends AbstractFixerResponse {
    private Map<String, BigDecimal> rates;
}
