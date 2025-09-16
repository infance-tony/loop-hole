-- Bank Management Database Schema
-- Assignment: Create bank_management database with transaction_modes and transactions tables

-- Create database
CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;

-- Create transaction_modes table
CREATE TABLE IF NOT EXISTS transaction_modes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_mode_id INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_mode_id) REFERENCES transaction_modes(id)
);

-- Insert sample transaction modes
INSERT INTO transaction_modes (name) VALUES
('Cash'),
('Online'),
('Cheque')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert sample transactions
INSERT INTO transactions (description, amount, transaction_mode_id) VALUES
('ATM Withdrawal', 500.00, 1),
('Online Transfer', 2500.00, 2),
('Cheque Deposit', 10000.00, 3),
('Cash Deposit', 2000.00, 1),
('Online Payment', 1500.00, 2),
('Cheque Payment', 7500.00, 3);

-- Select queries to verify data
SELECT * FROM transaction_modes;
SELECT * FROM transactions;

-- Join query to show transactions with mode names
SELECT
    t.id,
    t.description,
    t.amount,
    tm.name as transaction_mode,
    t.transaction_date
FROM transactions t
JOIN transaction_modes tm ON t.transaction_mode_id = tm.id
ORDER BY t.transaction_date DESC;