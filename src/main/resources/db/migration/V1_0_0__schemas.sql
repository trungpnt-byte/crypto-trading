CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    public_id UUID DEFAULT gen_random_uuid() NOT NULL,
    tenant_id VARCHAR(50) DEFAULT 'trader' NOT NULL,
    simple_role VARCHAR(50) DEFAULT 'TRADER' NOT NULL,
    email VARCHAR(100),
    preferred_timezone VARCHAR(100) DEFAULT 'UTC' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
ALTER TABLE users
    ADD CONSTRAINT uk_public_id UNIQUE (public_id);

-- Wallets table
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    balance DECIMAL(20, 8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, currency)
);

-- Price aggregations table
CREATE TABLE IF NOT EXISTS price_aggregations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trading_pair VARCHAR(20) NOT NULL,
    best_bid_price DECIMAL(20, 8) NOT NULL,
    best_ask_price DECIMAL(20, 8) NOT NULL,
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trade transactions table
CREATE TABLE IF NOT EXISTS trading_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    trade_type VARCHAR(10) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    price DECIMAL(20, 8) NOT NULL,
    total_amount DECIMAL(20, 8) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

