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
    ├── config/               # Spring configuration (security, scheduling)
    ├── shared/
    │   └── exceptions/       # Custom exceptions and the global handler
    └── features/
        ├── auth/             # registration, login, recovery, JWT
        ├── comments/
        ├── impact/           # shared impact scoring service
        ├── profile/
        │   ├── badges/
        │   ├── categories/
        │   ├── user_badge/
        │   └── user_rome/    # counters, visual identity, continuity scheduler
        ├── reactions/
        └── stories/

Package-by-feature: each feature owns its `controllers`, `services`, `entity`, `repositories` and `dto` subpackages, plus `enums` where needed. Cross-cutting Spring configuration lives in `config/`; shared exception handling in `shared/exceptions/`.

Two packages are not features in the usual sense. `impact/` holds a single service used by both `comments` and `reactions` to compute score deltas, placed outside either domain so neither depends on the other. `profile/` groups everything about a user's own progression and appearance, while content domains (`stories`, `comments`, `reactions`) stay top-level even though they reference `UserRome` as author.

## Endpoints

All paths are prefixed with `/api/cityvoice`. Controllers carry class-level `@PreAuthorize`; any future public endpoint goes in its own controller rather than mixing authenticated and anonymous methods in one class.

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | no | Creates a user, returns the recovery key |
| POST | `/auth/login` | no | Authenticates, sets the JWT cookie |
| POST | `/auth/recovery` | no | Resets the password using the recovery key |
| GET | `/auth/me` | yes | Returns the authenticated user |
| POST | `/auth/refresh-token` | yes | Refreshes the JWT |
| GET | `/profile/me` | yes | Returns the profile and visual identity |
| PUT | `/profile/visual-identity` | yes | Updates symbol and colour |
| GET | `/badge/progress` | yes | Badge progress for all four categories |
| POST | `/stories` | yes | Submits a story, returns updated badge progress |
| POST | `/comments` | yes | Posts a comment or a reply |
| POST | `/reactions` | yes | Adds, switches or removes a reaction (toggle) |

`POST /reactions` handles three cases in one endpoint: no prior reaction creates one, the same type removes it, a different type switches the vote. It returns 200 rather than 201 because it does not always create a resource.

springdoc-openapi is declared as a dependency but does not start under Spring Boot 4.1: version 2.8.13 targets Spring Boot 3.x. No OpenAPI UI is currently available.

## Authentication

JWT stored in an httpOnly cookie. Token lifetime is 24 hours (`jwt.expiration=86400000`).

`JwtRequestFilter` runs before `UsernamePasswordAuthenticationFilter` and populates the security context from the cookie. Sessions are stateless.

Passwords and recovery keys are hashed with BCrypt.

## Recovery keys

Registration generates a recovery key: six words drawn with `SecureRandom` from `src/main/resources/wordlist-it.txt` (2019 Italian words), joined by hyphens and lowercased.

The plaintext key is returned once in the registration response and never stored. Only its BCrypt hash is persisted in `users.recovery_key_hash`.

`POST /api/cityvoice/auth/recovery` verifies the key with `PasswordEncoder.matches` and resets the password.

`RecoveryAttemptLimiter` blocks an account for 5 minutes after 5 failed attempts. It is in-memory: counters reset on restart and are not shared across instances.

## Error handling

Controllers contain no try/catch. Services throw, and `ExceptionHandlerClass`
(a `@RestControllerAdvice` in `it.cityvoice.api.shared.exceptions`) maps
exceptions to HTTP status codes.

Custom exceptions carry their message to the client: those strings are written
in the service layer and are safe by construction. The catch-all handler returns
a fixed string instead, because `ex.getMessage()` on an unexpected exception
tends to leak column names, constraint names and framework internals. Stack
traces always go to the log.

Two mappings are worth explaining.

`BadCredentialsException` returns 401 with the same message whether the username
does not exist or the password is wrong. Distinguishing the two would tell an
attacker which usernames are registered.

`/recovery` follows the same rule, but the protection is partial: registration
returns 409 when a username is taken, which reveals the same information. The
real defence against enumeration here is `RecoveryAttemptLimiter`, not the
error message.

Exceptions thrown inside the Spring Security filter chain never reach the
advice, because they happen before the controller is invoked. A missing or
expired token is handled by `JwtAuthenticationEntryPoint` instead.

| Exception | Status |
|---|---|
| `ConstraintViolationException` | 400, body maps field to message |
| `BadRequestException`, `IllegalArgumentException`, `DataIntegrityViolationException` | 400 |
| `BadCredentialsException` | 401 |
| `AccessDeniedException`, `UnauthorizedException` | 403 |
| `ResourceNotFoundException` | 404 |
| `ConflictException` | 409 |
| everything else | 500 |

Response bodies are always JSON: `{"message": "..."}`.

## Request validation

Validation runs in the service layer, not the controller. Controllers are
pass-through: `@RequestBody` in, call the service, nothing else.

DTOs carry `@NotBlank`/`@Size` constraints (username 3–14 chars, password 6–12
chars). Services are annotated `@Validated` and take `@Valid` parameters, which
makes Spring trigger validation on the method call and throw
`ConstraintViolationException` on failure — the same exception already used
for path/query parameter validation, so one handler in `ExceptionHandlerClass`
covers both cases.

## Data model

`spring.jpa.hibernate.ddl-auto=update` — Hibernate derives the schema from the entities and applies additive changes at startup. There is no migration tool: `schema.sql` covers only what Hibernate cannot express.

See the entity-relationship diagram and the schema constraints in [02-scoring-and-badges.md](02-scoring-and-badges.md#entity-relationship-diagram).

