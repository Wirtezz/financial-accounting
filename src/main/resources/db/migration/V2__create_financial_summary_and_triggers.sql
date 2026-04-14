CREATE TABLE IF NOT EXISTS financial_summary (
    id INT PRIMARY KEY DEFAULT 1,
    total_income DECIMAL(12,2) DEFAULT 0,
    total_expense DECIMAL(12,2) DEFAULT 0,
    balance DECIMAL(12,2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO financial_summary (id, total_income, total_expense, balance) VALUES (1, 0, 0, 0)
ON DUPLICATE KEY UPDATE id = id;

DROP TRIGGER IF EXISTS after_transaction_insert;
DROP TRIGGER IF EXISTS after_transaction_update;
DROP TRIGGER IF EXISTS after_transaction_delete;

CREATE TRIGGER after_transaction_insert
AFTER INSERT ON transactions
FOR EACH ROW
UPDATE financial_summary
SET total_income = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'),
    total_expense = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    balance = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME') -
              (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    last_updated = NOW()
WHERE id = 1;

CREATE TRIGGER after_transaction_update
AFTER UPDATE ON transactions
FOR EACH ROW
UPDATE financial_summary
SET total_income = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'),
    total_expense = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    balance = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME') -
              (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    last_updated = NOW()
WHERE id = 1;

CREATE TRIGGER after_transaction_delete
AFTER DELETE ON transactions
FOR EACH ROW
UPDATE financial_summary
SET total_income = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'),
    total_expense = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    balance = (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME') -
              (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'),
    last_updated = NOW()
WHERE id = 1;