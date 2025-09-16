import java.sql.*;
import java.util.Scanner;

public class BankManagementAssignment {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bank_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "kabeer";

    public static void main(String[] args) {
        BankManagementAssignment assignment = new BankManagementAssignment();

        System.out.println("=== Bank Management System Assignment ===");
        System.out.println("Database: bank_management");
        System.out.println("Tables: transaction_modes, transactions");
        System.out.println();

        // Demonstrate database operations
        assignment.demonstrateOperations();
    }

    public void demonstrateOperations() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connected to MySQL database successfully!");
                System.out.println();

                // 1. Display transaction modes
                displayTransactionModes(conn);

                // 2. Display all transactions
                displayAllTransactions(conn);

                // 3. Add a new transaction
                addNewTransaction(conn);

                // 4. Update a transaction
                updateTransaction(conn);

                // 5. Delete a transaction
                deleteTransaction(conn);

                // 6. Display summary statistics
                displaySummaryStatistics(conn);

            }
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    private void displayTransactionModes(Connection conn) throws SQLException {
        System.out.println("=== Transaction Modes ===");
        String query = "SELECT * FROM transaction_modes ORDER BY id";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.printf("%-5s %-20s %-20s%n", "ID", "Name", "Created At");
            System.out.println("--------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-20s %-20s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getTimestamp("created_at"));
            }
        }
        System.out.println();
    }

    private void displayAllTransactions(Connection conn) throws SQLException {
        System.out.println("=== All Transactions ===");
        String query = "SELECT t.id, t.description, t.amount, tm.name as mode_name, t.transaction_date " +
                      "FROM transactions t " +
                      "JOIN transaction_modes tm ON t.transaction_mode_id = tm.id " +
                      "ORDER BY t.transaction_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.printf("%-5s %-30s %-10s %-15s %-20s%n", "ID", "Description", "Amount", "Mode", "Date");
            System.out.println("--------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-30s $%-9.2f %-15s %-20s%n",
                    rs.getInt("id"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getString("mode_name"),
                    rs.getTimestamp("transaction_date"));
            }
        }
        System.out.println();
    }

    private void addNewTransaction(Connection conn) throws SQLException {
        System.out.println("=== Adding New Transaction ===");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter transaction description: ");
        String description = scanner.nextLine();

        System.out.print("Enter transaction amount: ");
        double amount = scanner.nextDouble();

        System.out.print("Enter transaction mode ID (1=Cash, 2=Online, 3=Cheque): ");
        int modeId = scanner.nextInt();

        String insertQuery = "INSERT INTO transactions (description, amount, transaction_mode_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, description);
            stmt.setDouble(2, amount);
            stmt.setInt(3, modeId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        System.out.println("✅ Transaction added successfully with ID: " + generatedKeys.getInt(1));
                    }
                }
            }
        }
        System.out.println();
    }

    private void updateTransaction(Connection conn) throws SQLException {
        System.out.println("=== Updating Transaction ===");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter transaction ID to update: ");
        int transactionId = scanner.nextInt();

        scanner.nextLine(); // consume newline
        System.out.print("Enter new description: ");
        String newDescription = scanner.nextLine();

        System.out.print("Enter new amount: ");
        double newAmount = scanner.nextDouble();

        String updateQuery = "UPDATE transactions SET description = ?, amount = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, newDescription);
            stmt.setDouble(2, newAmount);
            stmt.setInt(3, transactionId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Transaction updated successfully!");
            } else {
                System.out.println("❌ Transaction not found with ID: " + transactionId);
            }
        }
        System.out.println();
    }

    private void deleteTransaction(Connection conn) throws SQLException {
        System.out.println("=== Deleting Transaction ===");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter transaction ID to delete: ");
        int transactionId = scanner.nextInt();

        String deleteQuery = "DELETE FROM transactions WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, transactionId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Transaction deleted successfully!");
            } else {
                System.out.println("❌ Transaction not found with ID: " + transactionId);
            }
        }
        System.out.println();
    }

    private void displaySummaryStatistics(Connection conn) throws SQLException {
        System.out.println("=== Transaction Summary Statistics ===");

        // Total transactions count
        String countQuery = "SELECT COUNT(*) as total_transactions FROM transactions";
        try (PreparedStatement stmt = conn.prepareStatement(countQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("Total Transactions: " + rs.getInt("total_transactions"));
            }
        }

        // Total amount by transaction mode
        String summaryQuery = "SELECT tm.name as mode_name, COUNT(t.id) as transaction_count, " +
                             "SUM(t.amount) as total_amount " +
                             "FROM transaction_modes tm " +
                             "LEFT JOIN transactions t ON tm.id = t.transaction_mode_id " +
                             "GROUP BY tm.id, tm.name " +
                             "ORDER BY tm.name";

        try (PreparedStatement stmt = conn.prepareStatement(summaryQuery);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nSummary by Transaction Mode:");
            System.out.printf("%-15s %-20s %-15s%n", "Mode", "Transaction Count", "Total Amount");
            System.out.println("----------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-15s %-20d $%-14.2f%n",
                    rs.getString("mode_name"),
                    rs.getInt("transaction_count"),
                    rs.getDouble("total_amount"));
            }
        }

        // Overall totals
        String totalQuery = "SELECT SUM(amount) as total_amount, AVG(amount) as avg_amount, " +
                           "MIN(amount) as min_amount, MAX(amount) as max_amount FROM transactions";

        try (PreparedStatement stmt = conn.prepareStatement(totalQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("\nOverall Statistics:");
                System.out.printf("Total Amount: $%.2f%n", rs.getDouble("total_amount"));
                System.out.printf("Average Amount: $%.2f%n", rs.getDouble("avg_amount"));
                System.out.printf("Minimum Amount: $%.2f%n", rs.getDouble("min_amount"));
                System.out.printf("Maximum Amount: $%.2f%n", rs.getDouble("max_amount"));
            }
        }
        System.out.println();
    }
}