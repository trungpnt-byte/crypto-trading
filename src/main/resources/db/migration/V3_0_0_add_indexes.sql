-- Description: Add indexes to improve query performance on key tables
-- Index for users table
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_email
    ON users (email);

-- Index for trade transactions
CREATE INDEX IF NOT EXISTS idx_user_created
    ON trade_transactions (user_id, created_at DESC);

-- Index for price aggregations
CREATE INDEX IF NOT EXISTS idx_trading_pair_created
    ON price_aggregations (trading_pair, created_at DESC);

-- Index for wallets
CREATE INDEX IF NOT EXISTS idx_user_currency
    ON wallets (user_id, currency);