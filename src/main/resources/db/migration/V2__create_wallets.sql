-- =============================================
-- V2: Wallets — one per user per asset type
-- =============================================
CREATE TABLE wallets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    asset_type_id   UUID            NOT NULL,
    balance         DECIMAL(19, 4)  NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    version         BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT fk_wallet_asset_type FOREIGN KEY (asset_type_id)
        REFERENCES asset_types (id) ON DELETE RESTRICT,

    -- Business rule: one wallet per user per asset type
    CONSTRAINT uq_wallet_user_asset UNIQUE (user_id, asset_type_id),

    -- Balance must never go negative (DB-level safety net)
    CONSTRAINT chk_wallet_balance_non_negative CHECK (balance >= 0),

    -- Wallet status must be a valid enum value
    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

-- Index for fetching all wallets for a user
CREATE INDEX idx_wallets_user_id ON wallets (user_id);

-- Index for FK lookups
CREATE INDEX idx_wallets_asset_type_id ON wallets (asset_type_id);
