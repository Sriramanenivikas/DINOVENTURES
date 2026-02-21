CREATE TABLE asset_types (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    symbol          VARCHAR(20)  NOT NULL,
    description     VARCHAR(500),
    decimal_places  INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_asset_type_name UNIQUE (name),
    CONSTRAINT chk_decimal_places CHECK (decimal_places >= 0 AND decimal_places <= 8)
);

CREATE INDEX idx_asset_types_active ON asset_types (is_active) WHERE is_active = TRUE;
