package com.example.finance.service;

import com.example.finance.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    void deleteTransaction(int id);

    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    BigDecimal getBalance();

    BigDecimal getTotalIncomeByPeriod(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalExpenseByPeriod(LocalDate startDate, LocalDate endDate);
    List<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate);
    List<Transaction> getTransactionsByTypeAndPeriod(String type, LocalDate startDate, LocalDate endDate);
}