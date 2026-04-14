package com.example.finance.controller;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service) {
        this.service = service;
    }
    public TransactionService getService() {
        return service;
    }
    public void addTransaction(Transaction t) {
        service.addTransaction(t);
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
            System.out.println("\n--- ВСЕ ОПЕРАЦИИ ---");
            System.out.println("--------------------------------------------------");
            for (Transaction t : all) {
                System.out.println(t);
            }
            System.out.println("--------------------------------------------------");
        }
    }

    public void showBalance() {
        System.out.println("\n--- ТЕКУЩИЙ БАЛАНС ---");
        System.out.printf("Всего доходов: %.2f руб.%n", service.getTotalIncome());
        System.out.printf("Всего расходов: %.2f руб.%n", service.getTotalExpense());
        System.out.printf("Баланс: %.2f руб.%n", service.getBalance());
    }

    public void deleteTransaction(int id) {
        service.deleteTransaction(id);
        System.out.println("Операция с ID " + id + " удалена");
    }

    public void showDetailedReportByPeriod(LocalDate startDate, LocalDate endDate) {
        System.out.println("\n|================================================|");
        System.out.println("|           ПОДРОБНЫЙ ОТЧЕТ ЗА ПЕРИОД            |");
        System.out.println("|================================================|");
        System.out.println("Период: с " + startDate + " по " + endDate);
        System.out.println("--------------------------------------------------");

        BigDecimal totalIncome = service.getTotalIncomeByPeriod(startDate, endDate);
        List<Transaction> incomes = service.getTransactionsByTypeAndPeriod("INCOME", startDate, endDate);

        System.out.println("\nДОХОДЫ:");
        System.out.println("--------------------------------------------------");
        if (incomes.isEmpty()) {
            System.out.println("  Доходов за этот период нет");
        } else {
            Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
            for (Transaction t : incomes) {
                incomeByCategory.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }

            System.out.println("  По категориям:");
            for (Map.Entry<String, BigDecimal> entry : incomeByCategory.entrySet()) {
                System.out.printf("    - %s: %.2f руб.%n", entry.getKey(), entry.getValue());
            }
            System.out.println("  --------------------------------------------------");
            System.out.printf("  ИТОГО ДОХОДОВ: %.2f руб.%n", totalIncome);
        }

        BigDecimal totalExpense = service.getTotalExpenseByPeriod(startDate, endDate);
        List<Transaction> expenses = service.getTransactionsByTypeAndPeriod("EXPENSE", startDate, endDate);

        System.out.println("\nРАСХОДЫ:");
        System.out.println("--------------------------------------------------");
        if (expenses.isEmpty()) {
            System.out.println("  Расходов за этот период нет");
        } else {
            Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
            Map<String, Integer> expenseCountByCategory = new LinkedHashMap<>();

            for (Transaction t : expenses) {
                expenseByCategory.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                expenseCountByCategory.merge(t.getCategory(), 1, Integer::sum);
            }

            System.out.println("  По категориям:");
            for (Map.Entry<String, BigDecimal> entry : expenseByCategory.entrySet()) {
                String category = entry.getKey();
                BigDecimal amount = entry.getValue();
                int count = expenseCountByCategory.get(category);
                System.out.printf("    - %s: %.2f руб. (%d операций)%n", category, amount, count);
            }
            System.out.println("  --------------------------------------------------");
            System.out.printf("  ИТОГО РАСХОДОВ: %.2f руб.%n", totalExpense);
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);
        System.out.println("\nИТОГИ ЗА ПЕРИОД:");
        System.out.println("--------------------------------------------------");
        System.out.printf("  Всего доходов: %.2f руб. (%d операций)%n", totalIncome, incomes.size());
        System.out.printf("  Всего расходов: %.2f руб. (%d операций)%n", totalExpense, expenses.size());
        System.out.printf("  БАЛАНС: %.2f руб.%n", balance);

        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("  Статус: ПРИБЫЛЬ");
        } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("  Статус: УБЫТОК");
        } else {
            System.out.println("  Статус: НОЛЬ");
        }
        System.out.println("==================================================");
    }

    public void showDayReport(LocalDate date) {
        showDetailedReportByPeriod(date, date);
    }

    public void showReportByPeriod(LocalDate startDate, LocalDate endDate) {
        showDetailedReportByPeriod(startDate, endDate);
    }
}