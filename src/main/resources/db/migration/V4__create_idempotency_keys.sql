CREATE TABLE idempotency_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255)    NOT NULL,
    operation       VARCHAR(10)     NOT NULL,
    response_code   INT             NOT NULL,
    response_body   TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP       NOT NULL,

    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key),

    CONSTRAINT chk_idem_operation CHECK (operation IN ('CREDIT', 'DEBIT'))
);

CREATE INDEX idx_idem_expires_at ON idempotency_keys (expires_at);
