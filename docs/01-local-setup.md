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

On startup, `spring-boot-docker-compose` reads `compose.yaml` and connects the application to the container, ignoring `spring.datasource.url`. The URL is only used when Docker Compose support is disabled.

Data lives in the `cityvoice-data` volume and survives `docker compose down`; `docker compose down -v` deletes it.

Categories, badges and districts are reference data loaded by hand into the development database. Seed files are not in the repository yet.

## Run

    ./mvnw spring-boot:run

On Windows:

    mvnw.cmd spring-boot:run

The API listens on `http://localhost:8080`. No Maven profiles are defined.

## Testing

Docker must be running: integration tests start a throwaway PostgreSQL 17 container through Testcontainers, separate from the development database.

    ./mvnw test

`IntegrationTestBase` starts a single container shared by every test class and injects its credentials via `@DynamicPropertySource`. The schema is recreated from scratch on every run (`ddl-auto=create-drop` in `src/test/resources/application.properties`) and each test runs in a transaction that is rolled back at the end, so tests do not interfere with each other.

Categories, badges and three districts are seeded from `src/test/resources/data.sql`. Two districts share a municipio (Monti and Trastevere, I) and one belongs to another (Garbatella, VIII), to test neighborhood counting. The categories and badges mirror the rows inserted by hand in the development database and must be updated whenever those change, otherwise tests fail on mismatched thresholds or names.

Tests extend `IntegrationTestBase` and call the endpoints through MockMvc, authenticating per request with `.with(user(username))`. Users are created with `registerUser()`, which generates random usernames using Datafaker.

## Schema

`spring.jpa.hibernate.ddl-auto=update` — Hibernate applies additive changes to the schema at startup and preserves existing data, including seeded badges and categories. `update` never drops columns or tables, cannot add a `NOT NULL` column to a table that already has rows, and cannot remove an existing `NOT NULL` constraint: those changes require a manual SQL statement.

`schema.sql` runs after Hibernate (`spring.jpa.defer-datasource-initialization=true`) and creates the partial unique indexes that JPA cannot express. It is idempotent and runs on every startup.

## Build

    ./mvnw clean package

Produces an executable JAR in `target/`.