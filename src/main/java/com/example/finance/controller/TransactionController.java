package com.example.finance.controller;


import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    public void addIncome(String description, String category, BigDecimal amount, LocalDate date) {
        Transaction t = new Transaction(amount, TransactionType.INCOME, description, category, date);
        service.addTransaction(t);
        System.out.println("Доход добавлен");
    }

    public void addExpense(String description, String category, BigDecimal amount, LocalDate date) {
        Transaction t = new Transaction(amount, TransactionType.EXPENSE, description, category, date);
        service.addTransaction(t);
        System.out.println("Расход добавлен");
    }

    public void showAllTransactions() {
        List<Transaction> all = service.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("Операций пока нет");
        } else {
            System.out.println("\n|-- ВСЕ ОПЕРАЦИИ --|");
            System.out.println("--------------------------------------------------");
            for (Transaction t : all) {
                System.out.println(t);
            }
            System.out.println("--------------------------------------------------");
        }
    }

    public void showBalance() {
        System.out.println("\n=== ТЕКУЩИЙ БАЛАНС ===");
        System.out.printf("Доходы: %.2f руб.%n", service.getTotalIncome());
        System.out.printf("Расходы: %.2f руб.%n", service.getTotalExpense());
        System.out.printf("Баланс: %.2f руб.%n", service.getBalance());
    }

    public void deleteTransaction(int id) {
        service.deleteTransaction(id);
        System.out.println("Операция с ID " + id + " удалена");
    }

    public void showReportByPeriod(LocalDate startDate, LocalDate endDate) {
        System.out.println("\n=== ОТЧЕТ ЗА ПЕРИОД ===");
        System.out.println("Период: с " + startDate + " по " + endDate);
        System.out.println("--------------------------------");

        BigDecimal income = service.getTotalIncomeByPeriod(startDate, endDate);
        BigDecimal expense = service.getTotalExpenseByPeriod(startDate, endDate);
        BigDecimal balance = income.subtract(expense);

        System.out.printf("Доходы: %.2f руб.%n", income);
        System.out.printf("Расходы: %.2f руб.%n", expense);
        System.out.printf("Баланс: %.2f руб.%n", balance);

        System.out.println("\nДетали расходов:");
        List<Transaction> expenses = service.getTransactionsByTypeAndPeriod("EXPENSE", startDate, endDate);
        if (expenses.isEmpty()) {
            System.out.println("Расходов за этот период нет");
        } else {
            for (Transaction t : expenses) {
                System.out.printf("  %s | %s | %.2f руб.%n", t.getTransactionDate(), t.getCategory(), t.getAmount());
            }
        }
    }

    public void showDayReport(LocalDate date) {
        showReportByPeriod(date, date);
    }
}
