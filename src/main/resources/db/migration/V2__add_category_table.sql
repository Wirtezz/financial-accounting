ALTER TABLE transactions ADD COLUMN category VARCHAR(50);

CREATE TABLE IF NOT EXISTS financial_summary (
    id INT PRIMARY KEY AUTO_INCREMENT,
    total_income DECIMAL(12,2) DEFAULT 0,
    total_expense DECIMAL(12,2) DEFAULT 0,
    balance DECIMAL(12,2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO financial_summary (total_income, total_expense, balance)
VALUES (0, 0, 0);