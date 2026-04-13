package com.example.finance;

import com.example.finance.controller.TransactionController;
import com.example.finance.repository.TransactionRepositoryImpl;
import com.example.finance.service.TransactionServiceImpl;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;

public class FinancialApplication {
    public static void main(String[] args) {
        System.out.println("=== ЗАПУСК СИСТЕМЫ УЧЁТА ФИНАНСОВ ===");

        // Настройки базы данных
        String url = "jdbc:mysql://localhost:3306/finance_db";
        String user = "root";
        String password = "password";

        System.out.println("Подключение к базе данных: " + url);

        // Выполнение миграций Flyway
        System.out.println("Выполнение миграций Flyway...");
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("filesystem:src/main/resources/db/migration")
                .load();
        flyway.migrate();
        System.out.println("Миграции выполнены успешно");

        // Запуск приложения
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Соединение с БД установлено");

            TransactionRepositoryImpl repo = new TransactionRepositoryImpl(conn);
            TransactionServiceImpl service = new TransactionServiceImpl(repo);
            TransactionController controller = new TransactionController(service);

            // Демонстрация работы
            System.out.println("\n--- Добавление тестовых операций ---");
            controller.addIncome("Зарплата", "Работа", new BigDecimal("2500.00"), "2026-04-13");
            controller.addExpense("Продукты", "Еда", new BigDecimal("150.50"), "2026-04-13");
            controller.addExpense("Кино", "Развлечения", new BigDecimal("25.00"), "2026-04-12");
            controller.addExpense("Такси", "Транспорт", new BigDecimal("18.50"), "2026-04-11");

            System.out.println();
            controller.showAll();

            System.out.println();
            controller.showBalance();

        } catch (Exception e) {
            System.err.println("ОШИБКА: Не удалось запустить приложение");
            System.err.println("Детали ошибки: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== РАБОТА ПРОГРАММЫ ЗАВЕРШЕНА ===");
    }
}