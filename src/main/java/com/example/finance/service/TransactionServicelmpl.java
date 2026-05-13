package com.example.finance.service;

import com.example.finance.model.Transaction;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.TransactionRepositoryHibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionServicelmpl implements TransactionService {
    private final TransactionRepository repository;

    public TransactionServicelmpl() {
        this.repository = new TransactionRepositoryHibernate();
    }

    public TransactionServicelmpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        repository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    @Override
    public void deleteTransaction(int id) {
        repository.delete(id);
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);   // ← updateTransaction
    }

    @Override
    public BigDecimal getTotalIncome() {
        return repository.getTotalIncome();
    }

    @Override
    public BigDecimal getTotalExpense() {
        return repository.getTotalExpense();
    }

    @Override
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    @Override
    public BigDecimal getTotalIncomeByPeriod(LocalDate start, LocalDate end) {
        return repository.getTotalIncomeByPeriod(start, end);
    }

    @Override
    public BigDecimal getTotalExpenseByPeriod(LocalDate start, LocalDate end) {
        return repository.getTotalExpenseByPeriod(start, end);
    }

    @Override
    public List<Transaction> getTransactionsByPeriod(LocalDate start, LocalDate end) {
        return repository.findByPeriod(start, end);
    }

    @Override
    public List<Transaction> getTransactionsByTypeAndPeriod(String type, LocalDate start, LocalDate end) {
        return repository.findByTypeAndPeriod(type, start, end);
    }
}