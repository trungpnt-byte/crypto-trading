-- Description: Seed initial data for traders and their wallets
-- Seed users
INSERT INTO users (username, password, email, simple_role, public_id)
VALUES ('trader1', '$2a$12$/mFKGx.CfMjpW.boZP7KEOMY3OietIBv8Okw2UdZYSth0bSZ5PdGG', 'trader1@app.com', 'TRADER',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
       ('trader2', '{bcrypt}$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC', 'trader2@app.com', 'TRADER',
        'b8f2a5b6-7d3c-4e8f-8d2a-4a6c9e0a1b32');

-- Seed wallets for trader1
INSERT INTO wallets (user_id, currency, balance)
VALUES (1, 'USDT', 50000.00000000),
       (1, 'ETH', 0.00000000),
       (1, 'BTC', 0.00000000);