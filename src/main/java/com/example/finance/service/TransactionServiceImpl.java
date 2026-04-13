package com.example.finance.service;


import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Ошибка: сумма должна быть положительной");
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        repository.save(transaction);
        System.out.println("Транзакция успешно сохранена");
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    @Override
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    @Override
    public BigDecimal getTotalIncome() {
        return repository.getTotalByType(TransactionType.INCOME.name());
    }

    @Override
    public BigDecimal getTotalExpense() {
        return repository.getTotalByType(TransactionType.EXPENSE.name());
    }

    @Override
    public void deleteTransaction(int id) {
        repository.delete(id);
    }
}