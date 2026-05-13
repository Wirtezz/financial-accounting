package com.example.finance.repository;

import com.example.finance.model.RecurringTransaction;
import com.example.finance.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class RecurringTransactionRepositoryHibernate {

    public void save(RecurringTransaction rt) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(rt);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public List<RecurringTransaction> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from RecurringTransaction", RecurringTransaction.class).list();
        }
    }

    public void delete(int id) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            RecurringTransaction rt = session.get(RecurringTransaction.class, id);
            if (rt != null) {
                session.remove(rt);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void updateLastExecuted(int id, LocalDate date) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            RecurringTransaction rt = session.get(RecurringTransaction.class, id);
            if (rt != null) {
                rt.setLastExecuted(date);
                session.merge(rt);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}