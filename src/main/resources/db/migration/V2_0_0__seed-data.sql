---- CREATE USER TABLE
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(30) DEFAULT 'TRADER',
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- INSERT TEST DATA INTO USER TABLE
INSERT INTO users (username, role, password, email) VALUES
('testuser1', 'TRADER', '123', 'test@gmail.com'),
('testuser2', 'TRADER', '123', 'test2@gmail.com')