# Local Setup

## Prerequisites

- Java 21
- Docker (for PostgreSQL)

## Environment variables

| Variable | Description |
|---|---|
| `JWT_SECRET` | Signing key for JWT tokens |
| `DB_PASSWORD` | PostgreSQL password used by the application |
| `POSTGRES_PASSWORD` | PostgreSQL password used by Docker Compose |

`DB_PASSWORD` and `POSTGRES_PASSWORD` must be identical.

## Database

PostgreSQL 17 is defined in `compose.yaml`. The `spring-boot-docker-compose` dependency starts the container automatically when the application starts.

Database `cityvoice`, user `postgres`, port 5432.

The datasource URL is `jdbc:postgresql://postgres:5432/cityvoice`, which resolves the container by service name. Running the application outside the Docker network requires changing the host to `localhost`.

## Run

    ./mvnw spring-boot:run

On Windows:

    mvnw.cmd spring-boot:run

The API listens on `http://localhost:8080`. No Maven profiles are defined.

## Testing

Docker must be running: integration tests start a throwaway PostgreSQL 17 container through Testcontainers, separate from the development database.

    ./mvnw test

`IntegrationTestBase` starts a single container shared by every test class and injects its credentials via `@DynamicPropertySource`. The schema is recreated from scratch on every run (`ddl-auto=create-drop` in `src/test/resources/application.properties`) and each test runs in a transaction that is rolled back at the end, so tests do not interfere with each other.

Categories and badges are seeded from `src/test/resources/data.sql`. It mirrors the rows inserted by hand in the development database and must be updated whenever those change, otherwise tests fail on mismatched thresholds or names.

Tests extend `IntegrationTestBase` and call the endpoints through MockMvc, authenticating per request with `.with(user(username))`. Users are created with `registerUser()`, which generates random usernames using Datafaker.

## Schema

`spring.jpa.hibernate.ddl-auto=update` — Hibernate applies additive changes to the schema at startup and preserves existing data, including seeded badges and categories.

`schema.sql` runs after Hibernate (`spring.jpa.defer-datasource-initialization=true`) and creates the partial unique indexes that JPA cannot express. It is idempotent and runs on every startup.

## Build

    ./mvnw clean package

Produces an executable JAR in `target/`.