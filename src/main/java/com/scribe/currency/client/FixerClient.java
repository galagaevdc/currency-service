package com.scribe.currency.client;

import com.scribe.currency.client.response.AbstractFixerResponse;
import com.scribe.currency.client.response.ErrorResponse;
import com.scribe.currency.client.response.FixerRatesResponse;
import com.scribe.currency.client.response.FixerSymbolsResponse;
import com.scribe.currency.configuration.FixerApiConfig;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class FixerClient {
  private final FixerApiConfig fixerApiConfig;
  private final RestTemplate restTemplate;
  private String exchangeUrl;
  private String supportedCurrenciesUrl;

  @PostConstruct
  public void init() {
    this.exchangeUrl =
        "%s/latest?access_key=%s".formatted(fixerApiConfig.getUrl(), fixerApiConfig.getKey());
    this.supportedCurrenciesUrl =
        "%s/symbols?access_key=%s".formatted(fixerApiConfig.getUrl(), fixerApiConfig.getKey());
  }

  public Map<String, BigDecimal> getExchangeRates() {
    FixerRatesResponse response = restTemplate.getForObject(exchangeUrl, FixerRatesResponse.class);
    processErrorResponse(response, "fetch exchange rates");
    return response.getRates();
  }

  public List<String> getSupportedCurrencies() {
    FixerSymbolsResponse response =
        restTemplate.getForObject(supportedCurrenciesUrl, FixerSymbolsResponse.class);
    processErrorResponse(response, "fetch supported currencies");
    return new ArrayList<>(response.getSymbols().keySet());
  }

  private static void processErrorResponse(
      final AbstractFixerResponse response, final String endpoint) {
    if (response == null) {
      throw new RuntimeException("Failed to " + endpoint);
    }
    if (!response.isSuccess()) {
      ErrorResponse error = response.getError();
      throw new RuntimeException("Failed to " + endpoint + ": " + error.getInfo());
    }
  }
}
