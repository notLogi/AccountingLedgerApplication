package com.pluralsight.data;

import com.pluralsight.models.Transaction;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private final DataSource dataSource;

    public TransactionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // CREATE - Add a new transaction
    public Transaction createTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (date, time, description, vendor, amount, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDate(1, Date.valueOf(transaction.getDate()));
            statement.setTime(2, Time.valueOf(transaction.getTime()));
            statement.setString(3, transaction.getDescription());
            statement.setString(4, transaction.getVendor());
            statement.setDouble(5, transaction.getAmount());
            statement.setInt(6, transaction.getUserId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setTransactionId(generatedKeys.getInt(1));
                        return transaction;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // READ - Get all transactions for a specific user
    public List<Transaction> getTransactionsByUserId(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id, date, time, description, vendor, amount, user_id FROM transactions WHERE user_id = ? ORDER BY date DESC, time DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    // READ - Get deposits only (amount >= 0) for a user
    public List<Transaction> getDepositsByUserId(int userId) {
        List<Transaction> deposits = new ArrayList<>();
        String sql = "SELECT transaction_id, date, time, description, vendor, amount, user_id FROM transactions WHERE user_id = ? AND amount >= 0 ORDER BY date DESC, time DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    deposits.add(mapResultSetToTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving deposits: " + e.getMessage());
            e.printStackTrace();
        }
        return deposits;
    }

    // READ - Get payments only (amount < 0) for a user
    public List<Transaction> getPaymentsByUserId(int userId) {
        List<Transaction> payments = new ArrayList<>();
        String sql = "SELECT transaction_id, date, time, description, vendor, amount, user_id FROM transactions WHERE user_id = ? AND amount < 0 ORDER BY date DESC, time DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapResultSetToTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving payments: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    // READ - Filter transactions by date range for a user
    public List<Transaction> getTransactionsByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id, date, time, description, vendor, amount, user_id FROM transactions WHERE user_id = ? AND date >= ? AND date <= ? ORDER BY date DESC, time DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    // READ - Filter transactions by vendor for a user
    public List<Transaction> getTransactionsByVendor(int userId, String vendor) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id, date, time, description, vendor, amount, user_id FROM transactions WHERE user_id = ? AND vendor = ? ORDER BY date DESC, time DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, vendor);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions by vendor: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    // UPDATE - Update transaction
    public boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET date = ?, time = ?, description = ?, vendor = ?, amount = ? WHERE transaction_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(transaction.getDate()));
            statement.setTime(2, Time.valueOf(transaction.getTime()));
            statement.setString(3, transaction.getDescription());
            statement.setString(4, transaction.getVendor());
            statement.setDouble(5, transaction.getAmount());
            statement.setInt(6, transaction.getTransactionId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Transaction updated successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Delete transaction by ID
    public boolean deleteTransaction(int transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, transactionId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Transaction deleted successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to map ResultSet to Transaction object
    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("transaction_id"),
                resultSet.getDate("date").toLocalDate(),
                resultSet.getTime("time").toLocalTime(),
                resultSet.getString("description"),
                resultSet.getString("vendor"),
                resultSet.getDouble("amount"),
                resultSet.getInt("user_id")
        );
    }
}