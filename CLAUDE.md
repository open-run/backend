# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

OpenuR (a.k.a. openrun) backend: a Spring Boot 3.2.2 / Java 17 REST API for a social running app.
Users are identified by their Ethereum wallet address; the app awards NFTs for completing challenges ("도전과제") and coordinates group runs ("벙" / bung).

## Commands

Build (compiles, generates QueryDSL Q-classes, runs the full test suite):

```bash
./gradlew build
```

Run the app locally (serves Swagger UI at http://localhost:8080/docs and /swagger-ui/index.html):

```bash
./gradlew bootRun
```

Tests:

```bash
./gradlew test                                        # all
./gradlew test --tests "io.openur.controller.BungApiTest"        # one class
./gradlew test --tests "io.openur.controller.BungApiTest.methodName"  # one method
```

`./gradlew clean` deletes the generated QueryDSL sources under `src/main/generated`; run it if Q-classes go stale after entity changes.

Local MySQL via Docker (port 3307, seeded from `schema.sql`):

```bash
docker-compose up -d db
```

## Configuration & secrets

- `application.yml` is **gitignored** (`.gitignore`) and holds secrets (DB creds, JWT key, NFT contract private key, RPC URL). It is not in the repo; CI generates it from the base64 GitHub secret `APPLICATION_YML`. Create it locally from `application.yml.sample`. Never commit it.
- `application.properties` holds JPA/springdoc defaults; `ddl-auto=none` in prod (schema is managed by `schema.sql`).
- Local overrides go in `application-local.properties` (see `application-local.example.properties`). Profile images use GCS bucket `openrun-nft` — run `gcloud auth application-default login` once for local dev.
- Tests run against H2 in-memory (`application-test.properties`, `MODE=MYSQL`, `ddl-auto=create-drop`) seeded from `src/test/resources/data.sql`.

## Architecture

### Package layout

Code lives under `io.openur`, split into `domain` and `global`.
Each domain (`bung`, `userbung`, `user`, `challenge`, `userchallenge`, `hashtag`, `bunghashtag`, `NFT`, `admin`) is a self-contained vertical slice with these sub-packages: `controller`, `service`, `repository`, `entity`, `model`, `dto`, `enums`, `exception`.

`global` holds cross-cutting infrastructure: `config` (Security, QueryDsl, Swagger, Web, RestTemplate), `filter` (JWT), `security`, `jwt`, `exception` (global handler), `storage` (GCS), `interceptor`, `common` (schedulers, validation, base controllers), and shared `dto` (`Response`, `PagedResponse`, `ExceptionDto`).

### Three-layer data access (important convention)

Each domain keeps three distinct object types — do not collapse them:

- **Entity** (`entity/XxxEntity`, `@Entity`, tables prefixed `tb_`): JPA persistence only. Entities should not escape the repository layer.
- **Model** (`model/Xxx`, plain Lombok POJO): the domain object that crosses service/controller boundaries. Conversions between entity↔model live on the model (e.g. `Bung.from(...)`).
- **DTO** (`dto/`): request/response shapes for the API.

Repositories follow a **triad pattern**:

- `XxxRepository` — the interface services depend on.
- `XxxJpaRepository extends JpaRepository<XxxEntity, ID>` — Spring Data derived queries and `@EntityGraph` fetch plans.
- `XxxRepositoryImpl` — implements `XxxRepository`, holds the `JPAQueryFactory`, and writes complex/paged queries in **QueryDSL** (using generated `Q…` types). Prefer derived queries on the JpaRepository for simple lookups and QueryDSL for joins/paging/search.

### Web/API conventions

- Controllers are `@RestController` under versioned paths (`/v1/...`), return `ResponseEntity<Response<T>>` or `ResponseEntity<PagedResponse<T>>`, and document endpoints with springdoc `@Operation`/`@ApiResponses` annotations (summaries are often in Korean).
- The authenticated principal is injected via `@AuthenticationPrincipal UserDetailsImpl userDetails`.
- Errors are thrown as domain exceptions and mapped to HTTP status centrally in `global/exception/ExceptionController` (`@RestControllerAdvice`). Add new exception types to the appropriate handler group there rather than catching in controllers.
- Method-level authorization uses `@PreAuthorize` with `MethodSecurityService` (`@EnableMethodSecurity`).

### Authentication

Stateless JWT. `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`, extracts the token, and sets the wallet address as the authenticated subject/username (`UserDetailsServiceImpl` loads users by blockchain address). Unauthenticated endpoints are whitelisted in `SecurityConfig` (login, nickname check, Swagger, health, and `GET /v1/bungs/{bungId}`).

### NFT / blockchain

`domain/NFT` uses **web3j** to talk to an ERC-1155-style contract on the Base chain (config under `nft.*`). Key points:

- Minting is **asynchronous and job-based**: `NftMintJobService` creates a job → `NftMintJobAsyncExecutor` processes it off-thread → `NftMintJobProcessor` transitions status (`PENDING`→`MINTING`→`SUCCESS`/`FAILED`) and submits the on-chain tx via `NftMintClient`.
- `NftMintJobRecoveryScheduler` (`@Scheduled`, every 5 min) re-drives jobs stuck in `PENDING` and flags long-stuck `MINTING` jobs for manual review.
- NFT catalog images resolve through a read-only **Swarm gateway** (`nft.swarm-gateway-url`); `NftAssetUrlResolver` builds URLs. (This replaced the older GCS-backed `tb_nft_items` model — see git history.)
- Because web3j beans do real RPC/ENS resolution on `@PostConstruct`, integration tests **mock** `NFTService`, `NftContractBalanceClient`, and `NftMintClient` (done in `TestSupport`).

### Domain events

Challenge progress is decoupled via Spring application events: `ChallengeEventsPublisher` fires `OnRaise` / `OnEvolution`, consumed by `ChallengeEventsListener`. Bung actions publish these rather than calling challenge logic directly.

### Scheduling

`BungScheduler` and `NftMintJobRecoveryScheduler` (`global/common/scheduler`) run periodic `@Scheduled` jobs (e.g. auto-completing/fading bungs, recovering mint jobs).

## Testing

Integration tests extend `io.openur.config.TestSupport`, a `@SpringBootTest` that loads the real `application.yml` plus test overrides, builds a `MockMvc` with Spring Security, and exposes helpers: `getTestUserToken1/2/3()` (pre-seeded wallet addresses), `jsonify(...)`, and `parseResponse(...)`. GCS `Storage` and the web3j NFT beans are `@MockBean`. Most tests are controller-level API tests under `src/test/java/io/openur/controller`.

## Conventions

- Lombok is used heavily (`@Getter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`); constructor injection via `@RequiredArgsConstructor` is the norm.
- Commits follow Conventional Commits (`type: subject`, e.g. `feat:`, `fix:`, `chore:`, `refactor:`), sometimes prefixed with a ticket id (`BE-102, ...`). Subjects are written in Korean or English.
