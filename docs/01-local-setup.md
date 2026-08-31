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

## Schema

`spring.jpa.hibernate.ddl-auto=create` recreates the schema at every startup, dropping existing data. Set it to `update` in `application.properties` to keep data between restarts.

## Build

    ./mvnw clean package

Produces an executable JAR in `target/`.