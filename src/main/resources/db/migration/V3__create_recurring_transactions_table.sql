CREATE TABLE IF NOT EXISTS recurring_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category VARCHAR(100),
    description VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    day_of_month INT NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    last_executed DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);