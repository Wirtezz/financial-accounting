package com.example.finance.service;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        repository.save(transaction);
        System.out.println("Операция добавлена");
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
    public BigDecimal getTotalIncomeByPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.getTotalIncomeByPeriod(startDate, endDate);
    }

    @Override
    public BigDecimal getTotalExpenseByPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.getTotalExpenseByPeriod(startDate, endDate);
    }

    @Override
    public List<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.findByPeriod(startDate, endDate);
    }

    @Override
    public List<Transaction> getTransactionsByTypeAndPeriod(String type, LocalDate startDate, LocalDate endDate) {
        return repository.findByTypeAndPeriod(type, startDate, endDate);
    }
}