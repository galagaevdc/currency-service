package com.scribe.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CurrencyServiceApplication {

  public static void main(final String[] args) {
    SpringApplication.run(CurrencyServiceApplication.class, args);
  }
}
