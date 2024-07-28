package com.scribe.currency.service;

import com.scribe.currency.client.FixerClient;
import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.dto.CurrencyExchangeDto;
import com.scribe.currency.dto.RateDto;
import com.scribe.currency.entity.CurrencyEntity;
import com.scribe.currency.entity.CurrencyExchangeRateEntity;
import com.scribe.currency.mapper.CurrencyMapper;
import com.scribe.currency.repository.CurrencyExchangeRepository;
import com.scribe.currency.repository.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CurrencyService {
  private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

  private final CurrencyRepository currencyRepository;
  private final CurrencyExchangeRepository currencyExchangeRepository;
  private final CurrencyMapper currencyMapper;
  private final FixerClient fixerClient;
  private final Map<String, CurrencyExchangeDto> exchangeRatesCache = new ConcurrentHashMap<>();

  @PostConstruct
  public void restoreCacheFromDB() {
    List<CurrencyExchangeRateEntity> exchangeRates = currencyExchangeRepository.findAll();
    Map<String, CurrencyExchangeDto> tempCache = new ConcurrentHashMap<>();

    for (CurrencyExchangeRateEntity rateEntity : exchangeRates) {
      String baseCurrency = rateEntity.getFrom().getCode();
      CurrencyExchangeDto exchangeDto =
          tempCache.computeIfAbsent(baseCurrency, k -> new CurrencyExchangeDto());
      exchangeDto.setBase(baseCurrency);
      exchangeDto.setActualizationDate(rateEntity.getActualizationDate());

      for (CurrencyExchangeRateEntity rate : exchangeRates) {
        if (rate.getFrom().getCode().equals(baseCurrency)) {
          RateDto rateDto = new RateDto();
          rateDto.setCurrency(rate.getTo().getCode());
          rateDto.setRate(rate.getConversionRate());
          exchangeDto.getRates().add(rateDto);
        }
      }

      exchangeRatesCache.put(baseCurrency, exchangeDto);
    }
  }

  public List<CurrencyDto> getSupportedCurrencies() {
    return currencyRepository.findAll().stream()
        .map(currencyMapper::toDto)
        .collect(Collectors.toList());
  }

  public CurrencyExchangeDto getExchangeRate(String code) {
    if (currencyRepository.findAll().stream()
        .noneMatch(currency -> currency.getCode().equals(code))) {
      throw new IllegalArgumentException("Currency " + code + " is not supported");
    }
    return exchangeRatesCache.get(code);
  }

  public CurrencyDto addCurrency(CurrencyDto currencyDto) {
    isSupported(currencyDto);
    CurrencyEntity entity = currencyMapper.toEntity(currencyDto);
    CurrencyEntity savedEntity = currencyRepository.save(entity);
    return currencyMapper.toDto(savedEntity);
  }

  private void isSupported(final CurrencyDto currencyDto) {
    if (!fixerClient.getSupportedCurrencies().contains(currencyDto.getCode())) {
      throw new IllegalArgumentException("Currency " + currencyDto.getCode() + " is not supported");
    }
  }

  @Scheduled(fixedRateString = "${currency.update.fixedRateString}")
  public void updateExchangeRates() {
    logger.info("Updating exchange rates");
    final Map<String, BigDecimal> exchangeRates = fixerClient.getExchangeRates();
    List<CurrencyEntity> currencies = currencyRepository.findAll();
    for (CurrencyEntity fromCurrency : currencies) {
      CurrencyExchangeDto exchangeDto = new CurrencyExchangeDto();
      exchangeDto.setBase(fromCurrency.getCode());
      for (CurrencyEntity toCurrency : currencies) {
        if (!fromCurrency.equals(toCurrency)) {
          final BigDecimal rate = exchangeRates.get(toCurrency.getCode());
          if (rate == null) {
            logger.error(
                "Exchange rate from {} to {} not found",
                fromCurrency.getCode(),
                toCurrency.getCode());
            continue;
          }
          saveRate(fromCurrency, toCurrency, rate);
          addRateForCache(toCurrency, rate, exchangeDto);
        }
      }
      exchangeDto.setActualizationDate(LocalDateTime.now());
      exchangeRatesCache.put(fromCurrency.getCode(), exchangeDto);
    }
    logger.info("Exchange rates updated");
  }

  private static void addRateForCache(
      final CurrencyEntity toCurrency,
      final BigDecimal rate,
      final CurrencyExchangeDto exchangeDto) {
    RateDto rateDto = new RateDto();
    rateDto.setCurrency(toCurrency.getCode());
    rateDto.setRate(rate);
    exchangeDto.getRates().add(rateDto);
  }

  private void saveRate(
      final CurrencyEntity fromCurrency, final CurrencyEntity toCurrency, final BigDecimal rate) {
    CurrencyExchangeRateEntity exchangeEntity =
        currencyExchangeRepository
            .findByFromAndTo(fromCurrency, toCurrency)
            .orElse(new CurrencyExchangeRateEntity());
    exchangeEntity.setFrom(fromCurrency);
    exchangeEntity.setTo(toCurrency);
    exchangeEntity.setConversionRate(rate);
    exchangeEntity.setActualizationDate(LocalDateTime.now());
    currencyExchangeRepository.save(exchangeEntity);
  }
}
