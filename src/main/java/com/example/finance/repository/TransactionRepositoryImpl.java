package com.example.finance.repository;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class TransactionRepositoryImpl implements TransactionRepository {
    private final Connection connection;

    public TransactionRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void updateTransaction(Transaction transaction) {

    }

    @Override
    public void save(Transaction t) {
        String sql = "INSERT INTO transactions (amount, type, description, category, transaction_date) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBigDecimal(1, t.getAmount());
            stmt.setString(2, t.getType().name());
            stmt.setString(3, t.getDescription());
            stmt.setString(4, t.getCategory());
            stmt.setDate(5, Date.valueOf(t.getTransactionDate()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) t.setId(rs.getInt(1));
            connection.commit(); // добавлено
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Transaction> findById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void update(Transaction t) {
        String sql = "UPDATE transactions SET amount=?, type=?, description=?, category=?, transaction_date=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, t.getAmount());
            stmt.setString(2, t.getType().name());
            stmt.setString(3, t.getDescription());
            stmt.setString(4, t.getCategory());
            stmt.setDate(5, Date.valueOf(t.getTransactionDate()));
            stmt.setInt(6, t.getId());
            int rows = stmt.executeUpdate();
            connection.commit(); // принудительный коммит
            System.out.println("Транзакция обновлена, затронуто строк: " + rows);
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Ошибка обновления: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }

    @Override
    public BigDecimal getTotalIncome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'";
        return getTotal(sql);
    }

    @Override
    public BigDecimal getTotalExpense() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'";
        return getTotal(sql);
    }

    @Override
    public BigDecimal getTotalIncomeByPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND transaction_date BETWEEN ? AND ?";
        return getTotalByPeriod(sql, start, end);
    }

    @Override
    public BigDecimal getTotalExpenseByPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND transaction_date BETWEEN ? AND ?";
        return getTotalByPeriod(sql, start, end);
    }

    @Override
    public List<Transaction> findByPeriod(LocalDate start, LocalDate end) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Transaction> findByTypeAndPeriod(String type, LocalDate start, LocalDate end) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE type = ? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setDate(2, Date.valueOf(start));
            stmt.setDate(3, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private BigDecimal getTotal(String sql) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getTotalByPeriod(String sql, LocalDate start, LocalDate end) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setType(TransactionType.valueOf(rs.getString("type")));
        t.setDescription(rs.getString("description"));
        t.setCategory(rs.getString("category"));
        t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        return t;
    }
}