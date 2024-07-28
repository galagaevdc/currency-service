package com.scribe.currency.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FixerIoRestTemplateConfig {

    @Bean
    public RestTemplate fixerIoRestTemplate() {
        return new RestTemplate();
    }
}
