# What LedgerGuard Is About

## Project summary

LedgerGuard is a backend portfolio project about payment integrity. It demonstrates how a payment API can handle retries safely, prevent refunds from exceeding a payment, keep a record of financial activity, and scope data to a tenant.

LedgerGuard is an educational simulation, not a real payment processor. It does not connect to banks, card networks, or a payment provider, and it does not move or settle real money. A payment marked `SUCCEEDED` means the application recorded a simulated successful payment.

## What the application does

The project uses Java 21, Spring Boot, PostgreSQL, Flyway, JWT authentication, Docker Compose, and Testcontainers. Its main workflow is:

1. **Authenticate:** The configured demo user requests a signed JWT. The token contains the tenant identity used by protected API calls.
2. **Create a payment:** The API stores an amount and currency along with an idempotency key. Repeating the request with the same key and payload returns the original payment. Reusing the key with different payment details is rejected.
3. **Issue a refund:** The API checks the payment's remaining refundable balance. It locks the payment row while applying a refund so simultaneous refund requests cannot refund more than the original amount.
4. **Record ledger activity:** Payment and refund records are accompanied by ledger entries. These writes are performed transactionally so a failed operation does not leave only part of the operation persisted.
5. **Reconcile records:** A scheduled check compares a payment's recorded refunded amount with the sum of its refund records. Mismatches are logged and exposed as a metric for monitoring.
6. **Inspect operations in the UI:** The dashboard supports demo sign-in, payment creation and lookup, refunds, and ledger inspection. Recent payment shortcuts are kept in the current browser.

The service also provides health and metrics endpoints. Docker Compose starts the application and PostgreSQL, and the GitHub Actions workflow is configured to run the Maven verification build with PostgreSQL Testcontainers.

## Main engineering ideas demonstrated

### Idempotency

Clients sometimes retry requests when a network response is delayed or lost. A payment or refund API must avoid processing the same intended operation twice. LedgerGuard scopes idempotency keys by tenant and enforces uniqueness in PostgreSQL, making the database the final protection against duplicate records.

### Transactions and data integrity

A payment/refund operation and its corresponding ledger entry should succeed or fail together. The service uses database transactions for these related writes. Database constraints add checks for positive amounts, refund totals, and unique tenant/key combinations.

### Concurrent refunds

Two refund requests can arrive at nearly the same time. LedgerGuard locks the payment row while checking and updating its remaining balance. An integration test exercises simultaneous full-refund attempts and verifies that the total does not exceed the captured amount.

### Tenant isolation

The tenant is read from the verified JWT rather than accepted as an arbitrary request parameter. Payment lookups and ledger queries are scoped by tenant, helping prevent one tenant from retrieving another tenant's records.

### Operational basics

The project includes schema migrations, containerized local setup, health checks, Micrometer metrics, scheduled reconciliation, and a CI workflow. These make it possible to discuss not only API code but also persistence, operations, and verification.

## What the project is not

LedgerGuard does not currently:

- Authorize or capture actual card or bank payments.
- Receive payment-provider webhooks or handle settlement and chargebacks.
- Provide a production identity system with user registration, password lifecycle, roles, or server-side tenant membership management.
- Offer a paginated payment list or search API; payments are inspected by ID.
- Implement balanced debit/credit accounting. Its ledger is an operational record of positive capture and refund amounts, not a full double-entry general ledger.
- Automatically repair reconciliation mismatches. It detects and reports them for investigation.
- Include production operations such as a hosted deployment, alert routing, backup/restore procedures, or formal incident workflows.

The demo authentication endpoint and credentials are intended for local development. Do not use the project to process real funds or sensitive payment data.

## Honest project assessment

LedgerGuard is more substantial than a basic CRUD tutorial because it addresses idempotency, transaction boundaries, database constraints, tenant scoping, and concurrent updates. Those are valuable backend engineering topics for an SDE-1 portfolio and interview discussion.

A fair description is **a scoped backend portfolio project with some intermediate engineering concepts**. It is not a production payment platform, and the simulated payment flow should be described clearly. The strongest interview topics are retry behavior, refund concurrency, tenant authorization, and how the database protects invariants.

## Ways to strengthen it next

1. Add a tenant-scoped, paginated payment list with useful filters and connect it to the dashboard.
2. Expand API and integration coverage for refund idempotency, over-refunds, invalid payloads, and tenant isolation across payment and ledger endpoints.
3. Decide whether to keep the ledger as an operational event log or evolve it into a balanced double-entry accounting model; document that choice.
4. Put simulated processing behind a payment-provider interface, with a deterministic fake provider for local testing. This would make the boundary between the API and a future provider explicit without pretending to process real funds.
5. Add a hosted demo using non-sensitive sample data, plus architecture documentation and a short walkthrough.
6. For a more production-oriented design, plan for rate limits, access and audit policies, monitoring alerts, and documented backup and recovery procedures.

## Short portfolio description

> LedgerGuard is a Java/Spring Boot payment-integrity API backed by PostgreSQL. It demonstrates tenant-scoped idempotency, transactional payment and refund records, concurrency-safe refund limits, ledger events, scheduled reconciliation, and a small operations dashboard. Payment processing is simulated for educational use; the project does not move real funds.
