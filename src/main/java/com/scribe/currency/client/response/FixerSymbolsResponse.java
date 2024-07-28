package com.scribe.currency.client.response;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FixerSymbolsResponse extends AbstractFixerResponse {
  private Map<String, String> symbols;
}
