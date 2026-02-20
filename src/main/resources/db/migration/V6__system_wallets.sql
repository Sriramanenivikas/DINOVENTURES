-- =============================================
-- V6: System/Treasury Wallets + Double-Entry Support
-- =============================================

-- 1. Add counter_wallet_id for double-entry ledger tracking
ALTER TABLE transactions
    ADD COLUMN counter_wallet_id UUID REFERENCES wallets(id);

-- 2. Expand transaction type constraint to include business flows
ALTER TABLE transactions DROP CONSTRAINT chk_txn_type;
ALTER TABLE transactions ADD CONSTRAINT chk_txn_type
    CHECK (type IN ('CREDIT', 'DEBIT', 'TOP_UP', 'BONUS', 'SPEND'));

-- Index for counter_wallet lookups (reconciliation queries)
CREATE INDEX idx_txn_counter_wallet ON transactions (counter_wallet_id)
    WHERE counter_wallet_id IS NOT NULL;

-- =========================================
-- 3. SYSTEM / TREASURY WALLETS
-- =========================================

-- System Treasury wallets — source/destination for all user transactions
-- These wallets start with a large supply that gets distributed to users
INSERT INTO wallets (id, user_id, asset_type_id, balance, status)
VALUES
    ('aa000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000000', 'a1000000-0000-0000-0000-000000000001', 1000000,    'ACTIVE'),
    ('aa000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000000', 'a1000000-0000-0000-0000-000000000002', 500000,     'ACTIVE'),
    ('aa000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000000', 'a1000000-0000-0000-0000-000000000003', 10000000.00,'ACTIVE');

-- Treasury initial supply transactions (audit trail)
INSERT INTO transactions (id, wallet_id, type, amount, balance_after, reference_id, description, status)
VALUES
    ('cc000000-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001', 'CREDIT', 1000000,     1000000,     'SEED-TREASURY-GC', 'Treasury initial supply: Gold Coins',    'SUCCESS'),
    ('cc000000-0000-0000-0000-000000000002', 'aa000000-0000-0000-0000-000000000002', 'CREDIT', 500000,      500000,      'SEED-TREASURY-DM', 'Treasury initial supply: Diamonds',      'SUCCESS'),
    ('cc000000-0000-0000-0000-000000000003', 'aa000000-0000-0000-0000-000000000003', 'CREDIT', 10000000.00, 10000000.00, 'SEED-TREASURY-RP', 'Treasury initial supply: Reward Points', 'SUCCESS');
