package com.example.finance.controller;


import com.example.finance.model.Transaction;
import com.example.finance.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;

public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    public void addIncome(String description, String category, BigDecimal amount, String date) {
        Transaction t = new Transaction(amount, com.example.finance.model.TransactionType.INCOME,
                description, category, java.time.LocalDate.parse(date));
        service.addTransaction(t);
        System.out.println("Доход добавлен: " + t);
    }

    public void addExpense(String description, String category, BigDecimal amount, String date) {
        Transaction t = new Transaction(amount, com.example.finance.model.TransactionType.EXPENSE,
                description, category, java.time.LocalDate.parse(date));
        service.addTransaction(t);
        System.out.println("Расход добавлен: " + t);
    }

    public void showAll() {
        List<Transaction> all = service.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("Список транзакций пуст");
        } else {
            System.out.println("    СПИСОК ВСЕХ ОПЕРАЦИЙ");
            for (Transaction t : all) {
                System.out.println(t);
            }
        }
    }

    public void showBalance() {
        System.out.println("    ФИНАНСОВЫЙ ОТЧЁТ");
        System.out.println("Общий доход: " + service.getTotalIncome() + " руб.");
        System.out.println("Общий расход: " + service.getTotalExpense() + " руб.");
        System.out.println("Баланс: " + service.getBalance() + " руб.");
    }

    public void deleteTransaction(int id) {
        service.deleteTransaction(id);
        System.out.println("Операция с ID " + id + " удалена");
    }
}
