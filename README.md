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

## JWT
View content of generated JWT Token using https://www.jwt.io/ 

## Authentication

**Privileges**

|id| Name            | hash        |
|--|-----------------|-------------|
|1 | ALL             | 64897       |
|2 | APP_MAINTENANCE | 1562765749  |
|3 | APP_WRITE       | 1253932033  |
|4 | APP_READ        | 1979950356  |
|5 | APP_READ_JWT    | -756069636  |

**Roles**

|id |   rolename    |            description            | hash        | 
|---|---------------|-----------------------------------|-------------|
| 1 | ADMIN         | Administrator                     | 2072793375  |
| 2 | FUNC_OPERATOR | Functional Operator               | -281572255  |
| 3 | TECH_OPERATOR | Technical Operator                | 439955957   |
| 4 | USER          | Application user with read access | -1051810471 |
| 5 | JWT-TOKEN     | Registered user with read access  | -1051810471 |
| 6 | DEVELOPER     | Developer with all privileges     | -1051810471 |

**Role - Privilege**

| id |   rolename    | id | name            |
|----|---------------|----|-----------------|
| 1  | ADMIN         |  1 | ALL             |
| 2  | FUNC_OPERATOR |  3 | APP_WRITE       |
| 3  | TECH_OPERATOR |  2 | APP_MAINTENANCE |
| 4  | USER          |  4 | APP_READ        |
| 5  | JWT-TOKEN     |  5 | APP_READ_JWT    |
| 6  | DEVELOPER     |  1 | ALL             |
