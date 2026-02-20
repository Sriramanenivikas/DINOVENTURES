-- =============================================
-- V3: Transactions — immutable ledger entries
-- =============================================
CREATE TABLE transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id       UUID            NOT NULL,
    type            VARCHAR(10)     NOT NULL,
    amount          DECIMAL(19, 4)  NOT NULL,
    balance_after   DECIMAL(19, 4)  NOT NULL,
    reference_id    VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),
    status          VARCHAR(10)     NOT NULL DEFAULT 'SUCCESS',
    metadata        JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_txn_wallet FOREIGN KEY (wallet_id)
        REFERENCES wallets (id) ON DELETE RESTRICT,

    -- Each transaction has a globally unique reference
    CONSTRAINT uq_txn_reference_id UNIQUE (reference_id),

    -- Amount must be positive (type determines direction)
    CONSTRAINT chk_txn_amount_positive CHECK (amount > 0),

    -- Type must be CREDIT or DEBIT
    CONSTRAINT chk_txn_type CHECK (type IN ('CREDIT', 'DEBIT')),

    -- Status must be valid
    CONSTRAINT chk_txn_status CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING'))
);

-- Primary query pattern: transaction history for a wallet, newest first
CREATE INDEX idx_txn_wallet_created ON transactions (wallet_id, created_at DESC);

-- Reference ID lookups (already unique, but explicit index for clarity)
CREATE INDEX idx_txn_reference_id ON transactions (reference_id);
