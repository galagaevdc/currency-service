package com.scribe.currency.controller;

import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.dto.CurrencyExchangeDto;
import com.scribe.currency.service.CurrencyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {
  private final CurrencyService currencyService;

  @GetMapping
  public List<CurrencyDto> getAllCurrencies() {
    return currencyService.getSupportedCurrencies();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CurrencyDto addCurrency(@RequestBody final CurrencyDto currencyDto) {
    return currencyService.addCurrency(currencyDto);
  }

  @GetMapping("/exchange/{code}")
  public CurrencyExchangeDto getExchangeRate(@PathVariable final String code) {
    return currencyService.getExchangeRate(code);
  }
}
