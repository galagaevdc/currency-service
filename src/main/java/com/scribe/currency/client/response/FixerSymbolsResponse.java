package com.scribe.currency.client.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class FixerSymbolsResponse extends AbstractFixerResponse {
    private Map<String, String> symbols;
}
