package com.example.finance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String category;
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "day_of_month", nullable = false)
    private int dayOfMonth;

    @Column(name = "last_executed")
    private LocalDate lastExecuted;

    @Column(name = "period_type")
    private String periodType;

    private int month;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    public RecurringTransaction() {}

    public RecurringTransaction(TransactionType type, String category, String description,
                                BigDecimal amount, int dayOfMonth, LocalDate lastExecuted) {
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.lastExecuted = lastExecuted;
        this.periodType = "MONTHLY";
        this.month = 0;
        this.createdAt = java.time.LocalDateTime.now();
    }

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

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
