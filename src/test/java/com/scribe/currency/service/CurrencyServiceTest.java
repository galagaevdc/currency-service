package com.scribe.currency.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.scribe.currency.client.FixerClient;
import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.dto.CurrencyExchangeDto;
import com.scribe.currency.dto.RateDto;
import com.scribe.currency.entity.CurrencyEntity;
import com.scribe.currency.entity.CurrencyExchangeRateEntity;
import com.scribe.currency.repository.CurrencyExchangeRepository;
import com.scribe.currency.repository.CurrencyRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

  private static final BigDecimal CONVERSION_RATE = BigDecimal.TEN;
  @Mock private CurrencyRepository currencyRepository;
  @Mock private CurrencyExchangeRepository currencyExchangeRateRepository;

  @Mock private FixerClient fixerClient;

  @InjectMocks private CurrencyService currencyService;

  @Test
  public void testGetExchangeRateWhenCurrencyNotFound() {
    when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

    final IllegalArgumentException illegalArgumentException =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              currencyService.getExchangeRate("USD");
            });

    assertEquals("Currency USD is not supported", illegalArgumentException.getMessage());
  }

  @Test
  public void testAddCurrencyWhenNotSupported() {
    final CurrencyDto currencyDto = new CurrencyDto();
    currencyDto.setCode("USD");
    currencyDto.setName("United States Dollar");
    when(fixerClient.getSupportedCurrencies()).thenReturn(Collections.singletonList("EUR"));

    final IllegalArgumentException illegalArgumentException =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              currencyService.addCurrency(currencyDto);
            });

    assertEquals("Currency USD is not supported", illegalArgumentException.getMessage());
  }

  @Test
  public void testUpdateExchangeRatesWhenNoRate() {
    when(fixerClient.getExchangeRates()).thenReturn(Collections.emptyMap());
    when(currencyRepository.findAll())
        .thenReturn(List.of(createCurrencyEntity("USD"), createCurrencyEntity("EUR")));

    currencyService.updateExchangeRates();

    final CurrencyExchangeDto exchangeRate = currencyService.getExchangeRate("USD");
    assertEquals("USD", exchangeRate.getBase());
    assertEquals(0, exchangeRate.getRates().size());
  }

  @Test
  public void testRestoreCacheFromDB() {
    final CurrencyEntity usdCurrency = createCurrencyEntity("USD");
    final CurrencyEntity eurCurrency = createCurrencyEntity("EUR");
    final CurrencyExchangeRateEntity usdToEurRate = new CurrencyExchangeRateEntity();
    usdToEurRate.setFrom(usdCurrency);
    usdToEurRate.setTo(eurCurrency);
    usdToEurRate.setConversionRate(CONVERSION_RATE);
    when(currencyExchangeRateRepository.findAll())
        .thenReturn(Collections.singletonList(usdToEurRate));
    when(currencyRepository.findAll()).thenReturn(List.of(usdCurrency, eurCurrency));

    currencyService.restoreCacheFromDB();

    final CurrencyExchangeDto usdExchangeRate = currencyService.getExchangeRate("USD");
    assertEquals("USD", usdExchangeRate.getBase());
    assertEquals(1, usdExchangeRate.getRates().size());
    final RateDto rateDto = usdExchangeRate.getRates().iterator().next();
    assertEquals("EUR", rateDto.getCurrency());
    assertEquals(CONVERSION_RATE, rateDto.getRate());
  }

  private static CurrencyEntity createCurrencyEntity(final String code) {
    final CurrencyEntity currencyDto = new CurrencyEntity();
    currencyDto.setCode(code);
    return currencyDto;
  }
}
