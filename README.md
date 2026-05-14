# Timeseries

Prerequisit:
- create the database see [Database](#database)

## Database
See [create database](database.md)

## API
See https://editor.swagger.io/ using [openapi spec](src/main/resources/openapi/timeseries.yaml)

## Building

```bash
mvn flyway:migrate -X -Dflyway.url=jdbc:postgresql://localhost:5432/timeseries -Dflyway.user=testuser -Dflyway.password=12345
mvn clean compile
mvn spring-boot:run
```
