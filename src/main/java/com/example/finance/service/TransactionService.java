package com.example.finance.service;

import com.example.finance.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    void deleteTransaction(int id);
    void updateTransaction(Transaction transaction);
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    BigDecimal getBalance();
    BigDecimal getTotalIncomeByPeriod(LocalDate start, LocalDate end);
    BigDecimal getTotalExpenseByPeriod(LocalDate start, LocalDate end);
    List<Transaction> getTransactionsByPeriod(LocalDate start, LocalDate end);
    List<Transaction> getTransactionsByTypeAndPeriod(String type, LocalDate start, LocalDate end);
}