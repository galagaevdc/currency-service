package com.scribe.currency.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.scribe.currency.client.response.ErrorResponse;
import com.scribe.currency.client.response.FixerRatesResponse;
import com.scribe.currency.client.response.FixerSymbolsResponse;
import com.scribe.currency.configuration.FixerApiConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class FixerClientTest {

  @Mock private FixerApiConfig fixerApiConfig;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private FixerClient fixerClient;

  @BeforeEach
  public void setUp() {
    when(fixerApiConfig.getUrl()).thenReturn("http://api.fixer.io");
    when(fixerApiConfig.getKey()).thenReturn("testkey");
    fixerClient.init();
  }

  @Test
  public void testGetExchangeRatesWhenResponseNull() {
    when(restTemplate.getForObject(anyString(), eq(FixerRatesResponse.class))).thenReturn(null);

    final RuntimeException runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> {
              fixerClient.getExchangeRates();
            });
    assertEquals("Failed to fetch exchange rates", runtimeException.getMessage());
  }

  @Test
  public void testGetExchangeRatesWhenResponseSuccessFalse() {
    FixerRatesResponse response = new FixerRatesResponse();
    response.setSuccess(false);
    response.setError(createError());
    when(restTemplate.getForObject(anyString(), eq(FixerRatesResponse.class))).thenReturn(response);

    final RuntimeException runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> {
              fixerClient.getExchangeRates();
            });
    assertEquals("Failed to fetch exchange rates: Downtime", runtimeException.getMessage());
  }

  @Test
  public void testGetSupportedCurrenciesWhenResponseNull() {
    when(restTemplate.getForObject(anyString(), eq(FixerSymbolsResponse.class))).thenReturn(null);

    final RuntimeException runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> {
              fixerClient.getSupportedCurrencies();
            });
    assertEquals("Failed to fetch supported currencies", runtimeException.getMessage());
  }

  @Test
  public void testGetSupportedCurrenciesWhenResponseSuccessFalse() {
    FixerSymbolsResponse response = new FixerSymbolsResponse();
    response.setSuccess(false);
    response.setError(createError());
    when(restTemplate.getForObject(anyString(), eq(FixerSymbolsResponse.class)))
        .thenReturn(response);

    final RuntimeException runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> {
              fixerClient.getSupportedCurrencies();
            });
    assertEquals("Failed to fetch supported currencies: Downtime", runtimeException.getMessage());
  }

  private static @NotNull ErrorResponse createError() {
    final ErrorResponse error = new ErrorResponse();
    error.setInfo("Downtime");
    return error;
  }
}
