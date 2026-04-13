package com.example.finance.repository;


import com.example.finance.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void save(Transaction transaction);
    Optional<Transaction> findById(int id);
    List<Transaction> findAll();
    List<Transaction> findByType(String type);
    void update(Transaction transaction);
    void delete(int id);
    BigDecimal getTotalByType(String type);
}
