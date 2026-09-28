# City Voice API

Backend of the City Voice civic platform.

This repository contains the API. The frontend lives in a separate repository.

## Documentation

- [Stack and architecture](docs/00-stack-and-architecture.md)
- [Local setup](docs/01-local-setup.md)
- [Scoring and badges](docs/02-scoring-and-badges.md)
- [Districts](docs/03-districts.md)

## Quick start

    ./mvnw spring-boot:run

Requires Java 21, Docker, and the `JWT_SECRET`, `DB_PASSWORD` and `POSTGRES_PASSWORD` environment variables. See [local setup](docs/01-local-setup.md) for details.

## Project status

In development. Structure and features change frequently.