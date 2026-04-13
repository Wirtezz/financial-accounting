package com.example.finance.repository;

import com.example.finance.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findAll();
    Optional<Transaction> findById(int id);
    void update(Transaction transaction);
    void delete(int id);

    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    BigDecimal getTotalIncomeByPeriod(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalExpenseByPeriod(LocalDate startDate, LocalDate endDate);
    List<Transaction> findByPeriod(LocalDate startDate, LocalDate endDate);
    List<Transaction> findByTypeAndPeriod(String type, LocalDate startDate, LocalDate endDate);
}
