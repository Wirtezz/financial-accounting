package com.example.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private Integer id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String category;
    private LocalDate transactionDate;

    public Transaction() {}

    public Transaction(BigDecimal amount, TransactionType type, String description,
                       String category, LocalDate transactionDate) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.category = category;
        this.transactionDate = transactionDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", date=" + transactionDate +
                '}';
    }
}