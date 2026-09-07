-- Billing is the one place in the platform where correctness matters more than
-- anything else: PostgreSQL + explicit transactions + an idempotency ledger.

CREATE TABLE billing_profiles (
    customer_id     BIGINT PRIMARY KEY,
    msisdn          VARCHAR(20)  NOT NULL,
    plan_code       VARCHAR(30)  NOT NULL,
    monthly_price   NUMERIC(10,2) NOT NULL,
    unbilled_usage_charge NUMERIC(10,2) NOT NULL DEFAULT 0, -- accrues from usage.recorded events
    consecutive_payment_failures INTEGER NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    plan_charge     NUMERIC(10,2) NOT NULL,
    usage_charge    NUMERIC(10,2) NOT NULL,
    total_amount    NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, PAYMENT_FAILED
    generated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    paid_at         TIMESTAMP
);

CREATE INDEX idx_invoices_customer ON invoices(customer_id);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    invoice_id      BIGINT       NOT NULL REFERENCES invoices(id),
    customer_id     BIGINT       NOT NULL,
    amount          NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL, -- SUCCESS, FAILED
    processed_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Idempotency ledger: guards the /pay endpoint against double-charging when a
-- client retries a timed-out request, and guards Kafka consumers against
-- redelivered usage.recorded / subscriber.provisioned messages.
CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(150) PRIMARY KEY,
    request_hash    VARCHAR(64)  NOT NULL,
    response_body   TEXT,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);
