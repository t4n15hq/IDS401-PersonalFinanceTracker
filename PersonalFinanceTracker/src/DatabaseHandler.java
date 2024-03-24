import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {
    private Connection connectTransactions() {
        // SQLite connection string for transactions
        String url = "jdbc:sqlite:transactions.db"; 
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
    private Connection connectBudgets() {
        // SQLite connection string for budgets
        String url = "jdbc:sqlite:budgets.db"; // Update with your actual database path
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public DatabaseHandler() {
        // Call the method to create tables if they don't exist
        initializeDatabase();
    }

    private void initializeDatabase() {
        // SQL statement for creating a new table for transactions
        String sqlTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
                + "	id integer PRIMARY KEY,"
                + "	amount real NOT NULL,"
                + "	date text NOT NULL,"
                + "	description text,"
                + "	category text NOT NULL"
                + ");";

        // SQL statement for creating a new table for budgets
        String sqlBudgets = "CREATE TABLE IF NOT EXISTS budgets ("
                + " category text PRIMARY KEY,"
                + " \"limit\" real NOT NULL"  // Note the quotes around limit
                + ");";

        try (Connection conn = this.connectTransactions();
             Statement stmt = conn.createStatement()) {
            // Create tables if they don't exist
            stmt.execute(sqlTransactions);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        try (Connection conn = this.connectBudgets();
             Statement stmt = conn.createStatement()) {
            // Create tables if they don't exist
            stmt.execute(sqlBudgets);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertTransaction(double amount, java.util.Date date, String description, String category) {
        String sql = "INSERT INTO transactions(amount, date, description, category) VALUES(?,?,?,?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (Connection conn = this.connectTransactions();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, sdf.format(date)); // Ensure date is converted to String
            pstmt.setString(3, description);
            pstmt.setString(4, category);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Transaction inserted successfully.");
            } else {
                System.out.println("No rows affected.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }


    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, amount, date, description, category FROM transactions";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (Connection conn = this.connectTransactions();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                java.util.Date date = null;

                try {
                    date = sdf.parse(rs.getString("date"));
                } catch (ParseException e) {
                    System.out.println("Error parsing date: " + e.getMessage());
                    // Handle the parse exception, e.g., skip this transaction, use a default date, log error, etc.
                }

                if (date != null) {  // Only add the transaction if the date was successfully parsed
                    String description = rs.getString("description");
                    String category = rs.getString("category");
                    Transaction transaction = new Transaction(amount, date, description, category);
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return transactions;
    }




    public boolean insertBudget(String category, double limit) {
        String sql = "INSERT INTO budgets(category, \"limit\") VALUES(?, ?)";
        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            pstmt.setDouble(2, limit);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean budgetExists(String category) {
        String sql = "SELECT category FROM budgets WHERE category = ?";

        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If a result is returned, the category exists
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this error properly
            return false;
        }
    }

    public void updateBudgetLimit(String category, double limit) {
        String sql = "UPDATE budgets SET \"limit\" = ? WHERE category = ?";

        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, limit);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this error properly
        }
    }
    
    // Method to log all budgets
    public void logAllBudgets() {
        String sql = "SELECT category, \"limit\" FROM budgets";

        try (Connection conn = this.connectBudgets();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String category = rs.getString("category");
                double limit = rs.getDouble("limit");
                System.out.println("Category: " + category + ", Limit: " + limit);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this error properly
        }
    }
    
    public double getBudgetLimit(String category) {
        String sql = "SELECT \"limit\" FROM budgets WHERE category = ?";
        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("limit");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this error properly
        }
        return Double.NaN; // Return NaN if budget is not found or on error
    }
    
    public boolean deleteBudget(String category) {
        String sql = "DELETE FROM budgets WHERE category = ?";

        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void updateBudgetSpent(String category, double spent) {
        String sql = "UPDATE budgets SET spent = ? WHERE category = ?";

        try (Connection conn = this.connectBudgets();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, spent);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT category, \"limit\" FROM budgets";

        try (Connection conn = this.connectBudgets();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("Fetched budget: " + rs.getString("category") + ", limit: " + rs.getDouble("limit")); // Debugging line
                Budget budget = new Budget(rs.getString("category"), rs.getDouble("limit"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching budgets: " + e.getMessage()); // Error logging
            e.printStackTrace();
        }
        return budgets;
    }

    public Map<String, Double> getBudgetLimits() {
        Map<String, Double> budgetLimits = new HashMap<>();
        // Use double quotes for "limit" if it is a reserved keyword in your SQL dialect
        String query = "SELECT category, \"limit\" FROM budgets"; 

        try (Connection conn = this.connectBudgets(); // Validate this method's implementation
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String category = rs.getString("category");
                double limit = rs.getDouble("limit"); // Ensure this matches the column type
                budgetLimits.put(category, limit);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // This will print the whole stack trace
        }

        return budgetLimits;
    }



public Map<String, Double> getMonthlyExpenses() {
    Map<String, Double> monthlyExpenses = new HashMap<>();

    // Database query to get monthly expenses
    String query = "SELECT category, SUM(amount) as total FROM transactions GROUP BY category"; // Replace 'transactions' with your actual table name

    try (Connection conn = this.connectTransactions(); // Use your database connection method
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            String category = rs.getString("category");
            double total = rs.getDouble("total");
            monthlyExpenses.put(category, total);
        }

    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }

    return monthlyExpenses;
}



   
}
