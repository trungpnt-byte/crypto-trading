-- Initial data
INSERT INTO users (username, role, password, email)
VALUES ('trader1', 'TRADER', '$2a$12$/mFKGx.CfMjpW.boZP7KEOMY3OietIBv8Okw2UdZYSth0bSZ5PdGG', 'trader1@gmail.com'),
       ('trader2', 'TRADER', '$2a$12$/mFKGx.CfMjpW.boZP7KEOMY3OietIBv8Okw2UdZYSth0bSZ5PdGG', 'trader2@gmail.com');

INSERT INTO wallets (user_id, currency, balance)
VALUES (1, 'USDT', 50000.00000000),
       (1, 'ETH', 0.00000000),
       (1, 'BTC', 0.00000000);