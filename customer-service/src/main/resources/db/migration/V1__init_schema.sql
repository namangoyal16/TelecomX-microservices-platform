-- Customer & Subscription Service schema
-- PostgreSQL chosen here because customer identity and subscription state are
-- transactional facts that must never be eventually-consistent (e.g. two
-- concurrent plan-change requests must not both "win").

CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone_number    VARCHAR(20)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    kyc_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE plans (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(30)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    monthly_price   NUMERIC(10,2) NOT NULL,
    data_limit_gb   INTEGER      NOT NULL,
    voice_minutes   INTEGER      NOT NULL,
    sms_count       INTEGER      NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL REFERENCES customers(id),
    plan_id         BIGINT       NOT NULL REFERENCES plans(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, ACTIVE, SUSPENDED, CANCELLED
    msisdn          VARCHAR(20),                              -- assigned phone number, set by Provisioning Service
    requested_at    TIMESTAMP    NOT NULL DEFAULT now(),
    activated_at    TIMESTAMP
);

CREATE INDEX idx_subscriptions_customer ON subscriptions(customer_id);

INSERT INTO plans (code, name, monthly_price, data_limit_gb, voice_minutes, sms_count) VALUES
  ('BASIC_5G',  'Basic 5G',   299.00, 10,  200, 100),
  ('PLUS_5G',   'Plus 5G',    599.00, 40,  500, 300),
  ('UNLTD_5G',  'Unlimited 5G', 999.00, 150, 2000, 1000);
