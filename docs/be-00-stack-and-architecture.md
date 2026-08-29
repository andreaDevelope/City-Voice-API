# Stack and Architecture

| | |
|---|---|
| Framework | Spring Boot 4.1 |
| Language | Java 21 |
| Database | PostgreSQL 17 |
| Persistence | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT via jjwt 0.12.6 |
| Build | Maven |

## Package structure

    it/cityvoice/api/
    ├── config/            # Spring configuration
    └── features/
        ├── auth/
        │   ├── controller/
        │   ├── dto/
        │   ├── entity/
        │   ├── repository/
        │   ├── security/
        │   ├── service/
        │   └── util/
        └── stories/

Package-by-feature: each feature contains its own controllers, services, entities and repositories. Cross-cutting Spring configuration lives in `config/`.

## Endpoints

Base path `/api/auth`:

| Method | Path | Auth required | Description |
|---|---|---|---|
| POST | `/register` | no | Creates a user, returns the recovery key |
| POST | `/login` | no | Authenticates, sets the JWT cookie |
| POST | `/recovery` | no | Resets the password using the recovery key |
| GET | `/me` | yes | Returns the authenticated user |
| POST | `/refresh-token` | yes | Refreshes the JWT |

springdoc-openapi is declared as a dependency but does not start under Spring Boot 4.1: version 2.8.13 targets Spring Boot 3.x. No OpenAPI UI is currently available.

## Authentication

JWT stored in an httpOnly cookie. Token lifetime is 24 hours (`jwt.expiration=86400000`).

`JwtRequestFilter` runs before `UsernamePasswordAuthenticationFilter` and populates the security context from the cookie. Sessions are stateless.

Passwords and recovery keys are hashed with BCrypt.

## Recovery keys

Registration generates a recovery key: six words drawn with `SecureRandom` from `src/main/resources/wordlist-it.txt` (2019 Italian words), joined by hyphens and lowercased.

The plaintext key is returned once in the registration response and never stored. Only its BCrypt hash is persisted in `users.recovery_key_hash`.

`POST /api/auth/recovery` verifies the key with `PasswordEncoder.matches` and resets the password. Keys do not expire and remain valid after use.

`RecoveryAttemptLimiter` blocks an account for 5 minutes after 5 failed attempts. It is in-memory: counters reset on restart and are not shared across instances.

## Schema

`spring.jpa.hibernate.ddl-auto=create` — Hibernate generates the schema from the entities at every startup. There are no migration files.
