-- =============================================
-- V7: Update idempotency_keys CHECK constraint for new transaction types
-- =============================================
ALTER TABLE idempotency_keys DROP CONSTRAINT chk_idem_operation;
ALTER TABLE idempotency_keys ADD CONSTRAINT chk_idem_operation
    CHECK (operation IN ('CREDIT', 'DEBIT', 'TOP_UP', 'BONUS', 'SPEND'));
