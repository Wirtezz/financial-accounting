package com.example.finance.service;

import com.example.finance.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    BigDecimal getBalance();
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    void deleteTransaction(int id);
}
