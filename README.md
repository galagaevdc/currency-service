# Currency Exchange Service

## API Documentation

The API documentation is available at [Swagger UI](http://localhost:8080/swagger-ui.html).

# Test coverage
- Jacoco plugin is used for test coverage. You can find the report in the `build/reports/jacoco/test/html/index.html` directory.

# Functional Requirements
- Get a list of currencies used in the project;
- Get exchange rates for a currency;
- Add new currency for getting exchange rates.

## How to run the application
- Define FIXER_API_KEY in the environment variables. You can get it [here](https://fixer.io/). This value is not committed to the repository due to security reasons.
- Start up dependencies using `docker compose up`
-  Run application with `./gradlew bootRun`

## Point that can be improved
- Liquibase migration should be disabled/enabled by flag `liquibase.enabled=true/false`. Two separate docker images should be prepared: the actual one and the one with Liquibase migration. The container with Liquibase migration should be run only once. For example, it could be implemented via Kubernetes Init Container. It's required to prevent database lock during the migration process.
- Use distributed cache for currency exchange rates. For example, it could be implemented via Redis. It will help to reduce the number of requests to the external service.
- Prevent running scheduled task by several microservices. For example, it could be implemented via Kubernetes CronJob and special container or distributed lock. Moreover it requires to use distributed cache for currency exchange rates.