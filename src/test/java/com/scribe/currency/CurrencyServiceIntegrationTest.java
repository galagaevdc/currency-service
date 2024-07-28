package com.scribe.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.dto.CurrencyExchangeDto;
import com.scribe.currency.dto.RateDto;
import com.scribe.currency.service.CurrencyService;
import java.math.BigDecimal;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CurrencyServiceIntegrationTest {
  private static final String TEST_KEY = "test";
  private static final String BASE_URL = "http://localhost:%d/api/%s";
  private static final String CURRENCIES_URL = "currencies";
  private static final String EXCHANGE_URL = "currencies/exchange/USD";
  private static final String EUR_CODE = "EUR";
  private static final String EUR_NAME = "Euro";
  private static final String USD_CODE = "USD";
  private static final String USD_NAME = "United States Dollar";
  private static final int HTTP_STATUS_OK = 200;
  private static final int HTTP_STATUS_CREATED = 201;
  private static final int WIREMOCK_PORT = 8080;
  private static final int EXPECTED_CURRENCIES_COUNT = 2;
  private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(0.85);
  private static final int ORDER_THREE = 3;
  private static final int ORDER_TWO = 2;
  private static final int ORDER_ONE = 1;
  private static final int EXPECTED_RATES_COUNT = 1;
  @Autowired private CurrencyService currencyService;
  @Autowired private TestRestTemplate restTemplate;
  @LocalServerPort private int port;

  @Container
  private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Container
  private static final WireMockContainer WIRE_MOCK_CONTAINER =
      new WireMockContainer(
              DockerImageName.parse("wiremock/wiremock:latest")
                  .asCompatibleSubstituteFor("wiremock/wiremock"))
          .withExposedPorts(WIREMOCK_PORT)
          .waitingFor(
              org.testcontainers.containers.wait.strategy.Wait.forHttp("/__admin/health")
                  .forStatusCode(HTTP_STATUS_OK))
          .withMappingFromResource("symbols", "wiremock/supported-currencies.json")
          .withMappingFromResource("rates", "wiremock/exchange-rates.json");

  @DynamicPropertySource
  static void setProperties(final DynamicPropertyRegistry properties) {
    properties.add("fixer.api.url", WIRE_MOCK_CONTAINER::getBaseUrl);
    properties.add("fixer.api.key", () -> TEST_KEY);
  }

  @Test
  @Order(ORDER_ONE)
  public void testAddCurrency() {
    CurrencyDto firstCurrency = new CurrencyDto();
    firstCurrency.setCode(EUR_CODE);
    firstCurrency.setName(EUR_NAME);
    addCurrency(firstCurrency);

    CurrencyDto secondCurrency = new CurrencyDto();
    secondCurrency.setCode(USD_CODE);
    secondCurrency.setName(USD_NAME);
    addCurrency(secondCurrency);
  }

  @Test
  @Order(ORDER_TWO)
  public void testGetSupportedCurrencies() {
    ResponseEntity<CurrencyDto[]> response =
        restTemplate.getForEntity(createURL(CURRENCIES_URL), CurrencyDto[].class);
    assertEquals(HTTP_STATUS_OK, response.getStatusCode().value());
    CurrencyDto[] currencies = response.getBody();
    assertNotNull(currencies);
    assertEquals(EXPECTED_CURRENCIES_COUNT, currencies.length);

    CurrencyDto eurCurrency = findCurrencyByCode(currencies, EUR_CODE);
    assertNotNull(eurCurrency);
    assertEquals(EUR_NAME, eurCurrency.getName());

    CurrencyDto usdCurrency = findCurrencyByCode(currencies, USD_CODE);
    assertNotNull(usdCurrency);
    assertEquals(USD_NAME, usdCurrency.getName());
  }

  @Test
  @Order(ORDER_THREE)
  public void testGetExchangeRate() {
    currencyService.updateExchangeRates();

    ResponseEntity<CurrencyExchangeDto> response =
        restTemplate.getForEntity(createURL(EXCHANGE_URL), CurrencyExchangeDto.class);

    assertEquals(HTTP_STATUS_OK, response.getStatusCode().value());
    CurrencyExchangeDto exchangeRate = response.getBody();
    assertNotNull(exchangeRate);

    // Assert base field
    assertEquals(USD_CODE, exchangeRate.getBase());

    assertNotNull(exchangeRate.getActualizationDate());
    assertNotNull(exchangeRate.getRates());
    assertEquals(EXPECTED_RATES_COUNT, exchangeRate.getRates().size());
    RateDto rateDto = exchangeRate.getRates().iterator().next();
    assertEquals(EUR_CODE, rateDto.getCurrency());
    assertEquals(EXCHANGE_RATE, rateDto.getRate());
  }

  private void addCurrency(final CurrencyDto currency) {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<CurrencyDto> entity = new HttpEntity<>(currency, headers);

    ResponseEntity<CurrencyDto> response =
        restTemplate.exchange(
            createURL(CURRENCIES_URL), HttpMethod.POST, entity, CurrencyDto.class);
    assertEquals(HTTP_STATUS_CREATED, response.getStatusCode().value());
    CurrencyDto addedCurrency = response.getBody();
    assertNotNull(addedCurrency);
    assertEquals(currency.getCode(), addedCurrency.getCode());
  }

  private String createURL(final String uri) {
    return BASE_URL.formatted(port, uri);
  }

  private CurrencyDto findCurrencyByCode(final CurrencyDto[] currencies, final String code) {
    for (CurrencyDto currency : currencies) {
      if (currency.getCode().equals(code)) {
        return currency;
      }
    }
    return null;
  }
}
