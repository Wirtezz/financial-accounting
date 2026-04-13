package com.example.finance;

import com.example.finance.controller.TransactionController;
import com.example.finance.repository.TransactionRepositoryImpl;
import com.example.finance.service.TransactionServiceImpl;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Scanner;

public class FinancialApplication {
    private static TransactionController controller;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("    СИСТЕМА УЧЕТА ФИНАНСОВ");

        String url = "jdbc:mysql://localhost:3306/finance_db";
        String user = "root";
        String password = "Leon63088.";

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            TransactionRepositoryImpl repo = new TransactionRepositoryImpl(conn);
            TransactionServiceImpl service = new TransactionServiceImpl(repo);
            controller = new TransactionController(service);

            runMenu();

        } catch (Exception e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
        }

        scanner.close();
    }

    private static void runMenu() {
        while (true) {
            printMenu();
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addIncome();
                    break;
                case "2":
                    addExpense();
                    break;
                case "3":
                    controller.showAllTransactions();
                    break;
                case "4":
                    controller.showBalance();
                    break;
                case "5":
                    deleteTransaction();
                    break;
                case "6":
                    showDayReport();
                    break;
                case "7":
                    showPeriodReport();
                    break;
                case "8":
                    System.out.println("До свидания!");
                    return;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("\n=========================================");
        System.out.println("ГЛАВНОЕ МЕНЮ");
        System.out.println("=========================================");
        System.out.println("1. Добавить ДОХОД");
        System.out.println("2. Добавить РАСХОД");
        System.out.println("3. Показать ВСЕ операции");
        System.out.println("4. Показать БАЛАНС");
        System.out.println("5. УДАЛИТЬ операцию");
        System.out.println("6. Отчет за ДЕНЬ (подробный)");
        System.out.println("7. Отчет за ПЕРИОД (подробный)");
        System.out.println("8. ВЫХОД");
        System.out.println("=========================================");
    }

    private static LocalDate readDateFromInput() {
        while (true) {
            System.out.print("Введите дату (день месяц год, например: 13 04 2026): ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");

            if (parts.length == 3) {
                try {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    LocalDate date = LocalDate.of(year, month, day);
                    return date;
                } catch (Exception e) {
                    System.out.println("Ошибка! Неверная дата. Попробуйте снова.");
                }
            } else {
                System.out.println("Ошибка! Введите 3 числа через пробел: день месяц год");
            }
        }
    }

    private static void addIncome() {
        System.out.println("\n--- ДОБАВЛЕНИЕ ДОХОДА ---");

        System.out.print("Описание: ");
        String description = scanner.nextLine();

        System.out.print("Категория: ");
        String category = scanner.nextLine();

        System.out.print("Сумма: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().replace(',', '.'));

        LocalDate date = readDateFromInput();

        controller.addIncome(description, category, amount, date);
    }

    private static void addExpense() {
        System.out.println("\n--- ДОБАВЛЕНИЕ РАСХОДА ---");

        System.out.print("Описание: ");
        String description = scanner.nextLine();

        System.out.print("Категория: ");
        String category = scanner.nextLine();

        System.out.print("Сумма: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().replace(',', '.'));

        LocalDate date = readDateFromInput();

        controller.addExpense(description, category, amount, date);
    }

    private static void deleteTransaction() {
        System.out.println("\n--- УДАЛЕНИЕ ОПЕРАЦИИ ---");
        controller.showAllTransactions();
        System.out.print("\nВведите ID операции для удаления: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            controller.deleteTransaction(id);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: нужно ввести число (ID операции)");
        }
    }

    private static void showDayReport() {
        System.out.println("\n--- ОТЧЕТ ЗА ДЕНЬ ---");
        LocalDate date = readDateFromInput();
        controller.showDayReport(date);
    }

    private static void showPeriodReport() {
        System.out.println("\n--- ОТЧЕТ ЗА ПЕРИОД ---");
        System.out.println("Введите НАЧАЛЬНУЮ дату:");
        LocalDate startDate = readDateFromInput();
        System.out.println("Введите КОНЕЧНУЮ дату:");
        LocalDate endDate = readDateFromInput();
        controller.showReportByPeriod(startDate, endDate);
    }
}