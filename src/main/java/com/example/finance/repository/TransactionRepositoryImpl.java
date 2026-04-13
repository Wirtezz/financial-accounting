package com.example.finance.repository;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {
    private final Connection connection;

    public TransactionRepositoryImpl(Connection connection) {
        this.connection = connection;
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
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении транзакции: " + e.getMessage());
            throw new RuntimeException("Ошибка сохранения транзакции", e);
        }
    }

    @Override
    public Optional<Transaction> findById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске транзакции по ID: " + e.getMessage());
            throw new RuntimeException("Ошибка поиска транзакции", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех транзакций: " + e.getMessage());
            throw new RuntimeException("Ошибка получения всех транзакций", e);
        }
        return list;
    }

    @Override
    public List<Transaction> findByType(String type) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске транзакций по типу: " + e.getMessage());
            throw new RuntimeException("Ошибка поиска по типу", e);
        }
        return list;
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
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении транзакции: " + e.getMessage());
            throw new RuntimeException("Ошибка обновления транзакции", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Транзакция с ID " + id + " удалена");
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении транзакции: " + e.getMessage());
            throw new RuntimeException("Ошибка удаления транзакции", e);
        }
    }

    @Override
    public BigDecimal getTotalByType(String type) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчёте суммы: " + e.getMessage());
            throw new RuntimeException("Ошибка подсчёта суммы", e);
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