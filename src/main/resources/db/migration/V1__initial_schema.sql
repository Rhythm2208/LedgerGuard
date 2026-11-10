CREATE TABLE payments (
 id UUID PRIMARY KEY, tenant_id VARCHAR(80) NOT NULL, idempotency_key VARCHAR(120) NOT NULL,
 amount NUMERIC(19,2) NOT NULL CHECK(amount>0), currency VARCHAR(3) NOT NULL,
 status VARCHAR(30) NOT NULL, refunded_amount NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK(refunded_amount>=0),
 created_at TIMESTAMPTZ NOT NULL, CONSTRAINT uk_payment_tenant_idempotency UNIQUE(tenant_id,idempotency_key),
 CONSTRAINT ck_payment_refund_limit CHECK(refunded_amount<=amount)
);
CREATE INDEX ix_payments_tenant_created ON payments(tenant_id,created_at DESC);
CREATE TABLE refunds (
 id UUID PRIMARY KEY, tenant_id VARCHAR(80) NOT NULL, payment_id UUID NOT NULL REFERENCES payments(id),
 idempotency_key VARCHAR(120) NOT NULL, amount NUMERIC(19,2) NOT NULL CHECK(amount>0),
 currency VARCHAR(3) NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_refund_tenant_idempotency UNIQUE(tenant_id,idempotency_key)
);
CREATE INDEX ix_refunds_tenant_payment ON refunds(tenant_id,payment_id);
CREATE TABLE ledger_entries (
 id UUID PRIMARY KEY, tenant_id VARCHAR(80) NOT NULL, payment_id UUID NOT NULL REFERENCES payments(id),
 reference_id UUID NOT NULL, entry_type VARCHAR(30) NOT NULL, amount NUMERIC(19,2) NOT NULL CHECK(amount>0),
 currency VARCHAR(3) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_ledger_tenant_payment_created ON ledger_entries(tenant_id,payment_id,created_at);
