package com.example.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringTransaction {
    private Integer id;
    private TransactionType type;
    private String category;
    private String description;
    private BigDecimal amount;
    private int dayOfMonth;
    private LocalDate lastExecuted;

    // Конструкторы
    public RecurringTransaction() {}

    public RecurringTransaction(TransactionType type, String category, String description,
                                BigDecimal amount, int dayOfMonth, LocalDate lastExecuted) {
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.lastExecuted = lastExecuted;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public LocalDate getLastExecuted() { return lastExecuted; }
    public void setLastExecuted(LocalDate lastExecuted) { this.lastExecuted = lastExecuted; }
}
