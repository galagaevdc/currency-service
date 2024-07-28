package com.scribe.currency.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "fixer.api")
public class FixerApiConfig {
  private String url;
  private String key;

  @PostConstruct
  public void init() {
    if (url == null || key == null) {
      throw new IllegalArgumentException("Fixer API URL and key must be provided");
    }
  }
}
