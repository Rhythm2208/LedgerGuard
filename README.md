# LedgerGuard 2.0
Portfolio backend for payment integrity using Java 21, Spring Boot 3, PostgreSQL, Flyway, JWT, Docker and Testcontainers.

## Features
- JWT-authenticated APIs; tenant is derived from the signed token claim, not a client-provided tenant parameter.
- Tenant-scoped payment and refund idempotency keys backed by PostgreSQL unique constraints.
- Refund concurrency protection using a pessimistic write lock on the payment row.
- Atomic payment/refund and ledger writes.
- Flyway schema migration; Hibernate schema mode is `validate`.
- Scheduled reconciliation compares payment refunded totals with persisted refund records.
- Actuator health, Micrometer metrics and Prometheus scrape endpoint.
- Testcontainers PostgreSQL integration tests for duplicate payments, concurrent refunds and tenant access.
- GitHub Actions CI workflow.

> Educational portfolio project, not a real payment processor. Do not use for real funds or sensitive payment data. The demo token endpoint is only a local-development authentication stub.

## Run with Docker
1. Extract ZIP and open terminal in `LedgerGuard-Complete`.
2. Copy `.env.example` to `.env`; replace all sample passwords/secrets.
3. Run `docker compose up --build -d`.
4. Check `docker compose ps` and `docker compose logs -f app`.
5. Health: `curl http://localhost:8085/actuator/health`.
6. Stop: `docker compose down`. To delete local database data too: `docker compose down -v`.

## Web dashboard
Open [http://localhost:8085/](http://localhost:8085/) after starting Docker Compose. Sign in with the demo credentials from `.env` (defaults: `demo` / `demo-change-me`). The dashboard can create payments, look them up by ID, issue refunds, and view ledger entries. Recent payment shortcuts are stored in the current browser.

## Obtain demo token
```bash
curl -s -X POST http://localhost:8085/api/v1/auth/token -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo-change-me"}'
```
Use returned `accessToken` as `$TOKEN`. If you changed credentials in `.env`, use those values.

## API examples
Create payment:
```bash
curl -i -X POST http://localhost:8085/api/v1/payments -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -H 'Idempotency-Key: order-1001' -d '{"amount":1250.00,"currency":"INR"}'
```
Repeat same key and payload to receive same payment. Same key with a different payload returns 409.
Get payment:
```bash
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8085/api/v1/payments/PAYMENT_UUID
```
Refund:
```bash
curl -i -X POST http://localhost:8085/api/v1/payments/PAYMENT_UUID/refunds -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -H 'Idempotency-Key: refund-1001-a' -d '{"amount":250.00}'
```
Ledger:
```bash
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8085/api/v1/payments/PAYMENT_UUID/ledger
```
Monitoring endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

## Tests
Install Java 21 and Maven 3.9+, start Docker, then run `mvn clean verify`.
Testcontainers launches PostgreSQL and verifies:
- Repeated payment request with same key produces one payment row.
- Same key with changed payload is rejected.
- Two simultaneous full refunds cannot exceed captured amount.
- Anonymous API access is rejected; tenant-scoped lookup cannot expose another tenant's data.

## Engineering notes / limitations
- Database uniqueness is the final guard against duplicate idempotency keys. In a simultaneous create race, one caller may receive 409 and should retry with the same key.
- Refund row locking serialises refunds for one payment. Payment and ledger changes commit atomically.
- Demo login maps one configured demo user to one configured tenant. For real multi-user use, integrate a trusted identity provider and server-side tenant membership/role provisioning.
- Reconciliation logs mismatches and exports a gauge; it does not silently repair financial records.
- The Prometheus endpoint is available for a Prometheus scraper; dashboards/alert routing are not bundled.
- Change demo secrets and never commit `.env`.
