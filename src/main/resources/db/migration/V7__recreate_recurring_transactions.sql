DROP TABLE IF EXISTS recurring_transactions;

CREATE TABLE recurring_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,
    category VARCHAR(100),
    description VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    day_of_month INT NOT NULL,
    last_executed DATE,
    period_type VARCHAR(10) DEFAULT 'MONTHLY',
    month INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);