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

    CONSTRAINT uq_wallet_user_asset UNIQUE (user_id, asset_type_id),

    CONSTRAINT chk_wallet_balance_non_negative CHECK (balance >= 0),

    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

CREATE INDEX idx_wallets_user_id ON wallets (user_id);

CREATE INDEX idx_wallets_asset_type_id ON wallets (asset_type_id);
