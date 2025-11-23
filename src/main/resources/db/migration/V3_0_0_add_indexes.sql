-- Index for trade transactions
CREATE INDEX IF NOT EXISTS idx_user_created
    ON trade_transactions (user_id, created_at DESC);