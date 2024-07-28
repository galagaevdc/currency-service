package com.scribe.currency;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class CurrencyServiceApplicationTests {
  private static final String POSTGRES_IMAGE = "postgres:latest";
  private static final String DB_NAME = "testdb";
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @Container
  private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>(POSTGRES_IMAGE)
          .withDatabaseName(DB_NAME)
          .withUsername(USERNAME)
          .withPassword(PASSWORD);

  @Test
  void contextLoads() {}
}
