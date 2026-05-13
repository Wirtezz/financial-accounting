package com.example.finance.repository;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryHibernate implements TransactionRepository {

    @Override
    public void save(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Transaction order by transactionDate desc", Transaction.class).list();
        }
    }

    @Override
    public Optional<Transaction> findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Transaction.class, id));
        }
    }

    // метод updateTransaction
    @Override
    public void updateTransaction(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // метод update (просто вызывает updateTransaction)
    @Override
    public void update(Transaction transaction) {
        updateTransaction(transaction);
    }

    @Override
    public void delete(int id) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Transaction t = session.get(Transaction.class, id);
            if (t != null) {
                session.remove(t);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public BigDecimal getTotalIncome() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COALESCE(SUM(amount), 0) FROM Transaction WHERE type = INCOME", BigDecimal.class)
                    .getSingleResult();
        }
    }

    @Override
    public BigDecimal getTotalExpense() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COALESCE(SUM(amount), 0) FROM Transaction WHERE type = EXPENSE", BigDecimal.class)
                    .getSingleResult();
        }
    }

    @Override
    public BigDecimal getTotalIncomeByPeriod(LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COALESCE(SUM(amount), 0) FROM Transaction WHERE type = INCOME AND transactionDate BETWEEN :start AND :end", BigDecimal.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
        }
    }

    @Override
    public BigDecimal getTotalExpenseByPeriod(LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COALESCE(SUM(amount), 0) FROM Transaction WHERE type = EXPENSE AND transactionDate BETWEEN :start AND :end", BigDecimal.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
        }
    }

    @Override
    public List<Transaction> findByPeriod(LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Transaction WHERE transactionDate BETWEEN :start AND :end ORDER BY transactionDate DESC", Transaction.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
        }
    }

    @Override
    public List<Transaction> findByTypeAndPeriod(String type, LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Transaction WHERE type = :type AND transactionDate BETWEEN :start AND :end ORDER BY transactionDate DESC", Transaction.class)
                    .setParameter("type", TransactionType.valueOf(type))
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .list();
        }
    }
}