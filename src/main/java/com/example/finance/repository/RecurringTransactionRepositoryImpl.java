package com.example.finance.repository;

import com.example.finance.model.RecurringTransaction;
import com.example.finance.model.TransactionType;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurringTransactionRepositoryImpl {
    private final Connection connection;

    public RecurringTransactionRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    public void save(RecurringTransaction rt) {
        String sql = "INSERT INTO recurring_transactions (type, category, description, amount, day_of_month, last_executed) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, rt.getType().name());
            stmt.setString(2, rt.getCategory());
            stmt.setString(3, rt.getDescription());
            stmt.setBigDecimal(4, rt.getAmount());
            stmt.setInt(5, rt.getDayOfMonth());
            stmt.setDate(6, rt.getLastExecuted() != null ? Date.valueOf(rt.getLastExecuted()) : null);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) rt.setId(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RecurringTransaction> findAll() {
        List<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions ORDER BY id";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                RecurringTransaction rt = new RecurringTransaction();
                rt.setId(rs.getInt("id"));
                rt.setType(TransactionType.valueOf(rs.getString("type")));
                rt.setCategory(rs.getString("category"));
                rt.setDescription(rs.getString("description"));
                rt.setAmount(rs.getBigDecimal("amount"));
                rt.setDayOfMonth(rs.getInt("day_of_month"));
                Date last = rs.getDate("last_executed");
                if (last != null) rt.setLastExecuted(last.toLocalDate());
                list.add(rt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM recurring_transactions WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastExecuted(int id, LocalDate date) {
        String sql = "UPDATE recurring_transactions SET last_executed=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}