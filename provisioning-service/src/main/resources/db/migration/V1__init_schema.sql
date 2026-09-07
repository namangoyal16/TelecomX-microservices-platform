-- Provisioning state must be strongly consistent (a SIM cannot be "half activated"),
-- hence PostgreSQL rather than Redis or Mongo for the source of truth here.

CREATE TABLE provisioning_records (
    id              BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT       NOT NULL UNIQUE,
    customer_id     BIGINT       NOT NULL,
    plan_code       VARCHAR(30)  NOT NULL,
    msisdn          VARCHAR(20)  NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DEACTIVATED
    provisioned_at  TIMESTAMP    NOT NULL DEFAULT now(),
    suspended_at    TIMESTAMP
);

CREATE INDEX idx_provisioning_customer ON provisioning_records(customer_id);

-- Idempotency ledger: guards against duplicate processing when the same Kafka
-- message is redelivered (at-least-once delivery) or a client retries a timed-out
-- provisioning request.
CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    request_hash    VARCHAR(64)  NOT NULL,
    response_body   TEXT,
    status          VARCHAR(20)  NOT NULL, -- IN_PROGRESS, COMPLETED
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);
