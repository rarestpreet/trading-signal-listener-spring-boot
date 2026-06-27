# Trading Signal Generator

Spring Boot backend that stores BUY/SELL signals, reads live Binance prices, evaluates target/stop-loss/expiry states, and calculates ROI. Final statuses are immutable. It includes JWT authentication, Redis price caching, scheduled evaluation, Swagger, Docker, PostgreSQL, and CI. Flyway is intentionally excluded as requested.

> [!IMPORTANT]
> This is a skill-evaluation/demo project, not a production trading platform. Authentication is mocked with one username and password loaded from configuration; there is no user database, registration, account lifecycle, refresh-token flow, token revocation, authorization model, or secure secrets management. The JWT code demonstrates request protection only and must not be presented as production-level authentication.
>
> The optional infrastructure features are also reference implementations: Redis uses a short-lived price cache without a resilience/fallback policy, the scheduler assumes a single application instance, Hibernate `ddl-auto` manages the schema because migrations were excluded, Docker uses development credentials, Binance integration has no production-grade retry/circuit-breaker/rate-limit strategy, and GitHub Actions performs only a basic Maven verification. Do not use real credentials, funds, or trading decisions with this application.

## Quick start with Docker

Requirements: Docker Desktop.

```bash
docker compose up --build
```

The API runs at `http://localhost:8080`; Swagger is at `http://localhost:8080/docs`. PostgreSQL and Redis are started automatically.

## Run locally

Requirements: Java 21+, Maven, PostgreSQL, and Redis.

1. Create PostgreSQL database `trading_signals`.
2. Set `DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `JWT_SECRET` (at least 32 characters), `APP_USER`, and `APP_PASSWORD` as needed. Defaults are in `application.yml` for local development only.
3. Run `mvn spring-boot:run`.
4. Run tests with `mvn test`.

## Authentication and API

Authentication is intentionally mocked for demonstration. The configured development user is not stored in PostgreSQL and should not be treated as a real account system.

Obtain a JWT (development defaults are `admin` / `admin123`):

```http
POST /api/auth/login
Content-Type: application/json

{"username":"admin","password":"admin123"}
```

Use `Authorization: Bearer <accessToken>` for:

- `POST /api/signals`
- `GET /api/signals`
- `GET /api/signals/{id}`
- `GET /api/signals/{id}/status`
- `DELETE /api/signals/{id}`

Example create request:

```json
{
  "symbol": "BTCUSDT",
  "direction": "BUY",
  "entryPrice": 60000,
  "stopLoss": 58000,
  "targetPrice": 65000,
  "entryTime": "2026-06-27T08:00:00Z",
  "expiryTime": "2026-06-28T08:00:00Z"
}
```

## Architecture and workflow

<p align="center">
  <img src="assets/architecture.jpg" alt="HearMeOut QnA ER Diagram" width="900" />
</p>

The controller accepts validated DTOs and delegates to `TradingSignalService`. The service applies domain rules through `SignalCalculator`, requests prices through the `BinancePriceClient` abstraction, and persists through Spring Data JPA. `WebClientBinancePriceClient` calls Binance at the service boundary; Redis caches each symbol briefly to limit duplicate calls. A scheduler reevaluates OPEN records every 60 seconds. Optimistic locking and terminal-state checks protect state consistency. Exceptions are translated to structured HTTP responses by the global handler. JWT-protected requests are stateless, while the identity source itself remains mocked.

## Business rules

- BUY: stop loss < entry < target; target triggers at price >= target, stop at price <= stop loss.
- SELL: target < entry < stop loss; target triggers at price <= target, stop at price >= stop loss.
- Entry may be no more than 24 hours old and cannot be in the future; expiry must follow entry.
- TARGET_HIT, STOPLOSS_HIT, and EXPIRED never transition again.
- ROI is rounded to two decimal places and persisted when a signal closes.

## Production gaps

Before production use, replace the mocked login with persistent users, hashed stored credentials, role/ownership checks, refresh-token rotation and revocation, and managed secrets. Add explicit database migrations, Binance timeouts/retries/circuit breaking, Redis fallback behavior, distributed scheduler locking or a job queue, rate limiting, observability, audit logs, stronger integration/security tests, and production deployment hardening.
