import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class FinanceTrackerGUI {
    private Account account;
    private DatabaseHandler databaseHandler;
    private JFrame frame;
    private JDialog transactionDialog, budgetDialog;
    private JTextField amountField, descriptionField, categoryField, budgetCategoryField, budgetLimitField;
    private JXDatePicker datePicker;
    private JTextArea outputArea;
    private JLabel totalSpentLabel;
    private JButton viewTransactionsButton, exportTransactionsButton; 
    private double totalSpent = 0.0;
    private SimpleDateFormat dateFormat;
    private JButton viewChartsButton; 


    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String filePath) {
           backgroundImage = new ImageIcon(filePath).getImage();
            setOpaque(false);
    }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public FinanceTrackerGUI(String backgroundImagePath) {
        account = new Account();
        databaseHandler = new DatabaseHandler();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        frame = new JFrame("Finance Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImagePath);
        frame.setContentPane(backgroundPanel);

        JLabel titleLabel = new JLabel("Finance Tracker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK); 
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.WHITE); 
        titleLabel.setPreferredSize(new Dimension(frame.getWidth(), 60)); 

        frame.add(titleLabel, BorderLayout.NORTH); 
        
        
        JButton manageTransactionsButton = new JButton("Manage Transactions");
        manageTransactionsButton.addActionListener(this::showTransactionDialog);

        JButton manageBudgetsButton = new JButton("Manage Budgets");
        manageBudgetsButton.addActionListener(this::showBudgetDialog);

        viewTransactionsButton = new JButton("View Transactions"); 
        viewTransactionsButton.addActionListener(this::viewTransactions); 
        
        exportTransactionsButton = new JButton("Export Transactions"); 
        exportTransactionsButton.addActionListener(this::exportTransactions);

        viewChartsButton = new JButton("View Charts");
        viewChartsButton.addActionListener(this::openChartWindow);
      
        
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5)); 
        buttonPanel.setOpaque(false);
        buttonPanel.add(manageTransactionsButton);
        buttonPanel.add(manageBudgetsButton);
        buttonPanel.add(viewTransactionsButton);
        buttonPanel.add(exportTransactionsButton);
        buttonPanel.add(viewChartsButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Initialize transactions JTextArea
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Initialize totalSpentLabel
        totalSpentLabel = new JLabel("Total Spent: $0.00");
        totalSpentLabel.setForeground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(outputArea, BorderLayout.CENTER);
        mainPanel.add(totalSpentLabel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showTransactionDialog(ActionEvent e) {
        if (transactionDialog == null) {
            transactionDialog = new JDialog(frame, "Transactions", true);
            transactionDialog.setLayout(new BorderLayout());

            amountField = new JTextField(10);
            datePicker = new JXDatePicker();
            datePicker.setFormats(dateFormat);
            descriptionField = new JTextField(10);
            categoryField = new JTextField(10);
            JButton addTransactionButton = new JButton("Add Transaction");

            addTransactionButton.addActionListener(ae -> addTransaction());

            JPanel transactionInputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            transactionInputPanel.setOpaque(false);
            transactionInputPanel.add(new JLabel("Amount:"));
            transactionInputPanel.add(amountField);
            transactionInputPanel.add(new JLabel("Date:"));
            transactionInputPanel.add(datePicker);
            transactionInputPanel.add(new JLabel("Description:"));
            transactionInputPanel.add(descriptionField);
            transactionInputPanel.add(new JLabel("Category:"));
            transactionInputPanel.add(categoryField);
            transactionInputPanel.add(addTransactionButton);

            JScrollPane scrollPane = new JScrollPane(outputArea);
            transactionDialog.add(scrollPane, BorderLayout.CENTER);
            transactionDialog.add(transactionInputPanel, BorderLayout.NORTH);

            transactionDialog.pack();
            transactionDialog.setLocationRelativeTo(frame);
        }
        transactionDialog.setVisible(true);
    }

    private void addTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            Date date = datePicker.getDate();
            String description = descriptionField.getText();
            String category = categoryField.getText();

            // Create a new Transaction object
            Transaction transaction = new Transaction(amount, date, description, category);
            
            // Add the transaction to the account and log it to the database
            account.addTransaction(transaction);
            databaseHandler.insertTransaction(amount, date, description, category); // This line logs the transaction to the database

            // Update the spent amount in the in-memory budget object
            Budget budget = account.getBudgets().get(category);
            if (budget != null) {
                budget.addSpending(amount); // Update the spent amount
                databaseHandler.updateBudgetSpent(category, budget.getSpent()); // Persist the change to the database

                // Check if the spending exceeds the budget and alert the user
                if (budget.isOverLimit()) {
                    JOptionPane.showMessageDialog(frame, 
                        "Warning: The spending for " + category + " has exceeded the budget limit!",
                        "Budget Limit Exceeded", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // Optionally, handle the case where there is no budget for the category
                System.out.println("No budget found for category: " + category);
            }

            // Update the total spent label and the transactions display
            totalSpent += amount;
            totalSpentLabel.setText("Total Spent: $" + String.format("%.2f", totalSpent));
            refreshTransactions(); // Update the transactions display
            checkBudgets(); // Refresh the budget status display

            JOptionPane.showMessageDialog(transactionDialog, "Transaction added successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(transactionDialog, "Error adding transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void refreshTransactions() {
        List<Transaction> transactions = account.getTransactions();
        StringBuilder transactionText = new StringBuilder("Transactions:\n");
        for (Transaction transaction : transactions) {
            transactionText.append(transaction.toString()).append("\n");
        }
        outputArea.setText(transactionText.toString());
    }


    private void showBudgetDialog(ActionEvent e) {
        if (budgetDialog == null) {
            budgetDialog = new JDialog(frame, "Budgets", true);
            budgetDialog.setLayout(new BorderLayout());

            JPanel budgetInputPanel = new JPanel();
            budgetInputPanel.setLayout(new BoxLayout(budgetInputPanel, BoxLayout.Y_AXIS));
            budgetInputPanel.setOpaque(false);

            JPanel inputRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            budgetCategoryField = new JTextField(10);
            budgetLimitField = new JTextField(10);

            inputRowPanel.add(new JLabel("Category:"));
            inputRowPanel.add(budgetCategoryField);
            inputRowPanel.add(new JLabel("Limit:"));
            inputRowPanel.add(budgetLimitField);

            JButton addBudgetButton = new JButton("Add/Update Budget");
            addBudgetButton.addActionListener(ae -> addBudget());

            JButton deleteBudgetButton = new JButton("Delete Budget");
            deleteBudgetButton.addActionListener(ae -> {
                String categoryToDelete = budgetCategoryField.getText();
                if (!categoryToDelete.isEmpty() && budgetExists(categoryToDelete)) {
                    int confirm = JOptionPane.showConfirmDialog(
                        budgetDialog, 
                        "Are you sure you want to delete the budget for category: " + categoryToDelete + "?",
                        "Delete Budget",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteBudget(categoryToDelete);
                    }
                } else {
                    JOptionPane.showMessageDialog(budgetDialog, "Budget category does not exist or field is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton checkBudgetsButton = new JButton("Check Budgets");
            checkBudgetsButton.addActionListener(ae -> checkBudgets());

            // Adding buttons to the input panel
            budgetInputPanel.add(inputRowPanel);
            budgetInputPanel.add(addBudgetButton);
            budgetInputPanel.add(deleteBudgetButton);
            budgetInputPanel.add(checkBudgetsButton);

            budgetDialog.add(budgetInputPanel, BorderLayout.CENTER);
            budgetDialog.pack();
            budgetDialog.setLocationRelativeTo(frame);
        }
        budgetDialog.setVisible(true);
    }


    private void addBudget() {
        try {
            String category = budgetCategoryField.getText();
            double limit = Double.parseDouble(budgetLimitField.getText());

            if (budgetExists(category)) {
                updateBudgetLimit(category, limit);
                JOptionPane.showMessageDialog(budgetDialog, "Budget updated successfully!");
            } else {
                boolean isInserted = databaseHandler.insertBudget(category, limit);
                if (isInserted) {
                    account.addBudget(category, limit);  // Assuming account needs to be updated too
                    JOptionPane.showMessageDialog(budgetDialog, "Budget added successfully!");
                } else {
                    JOptionPane.showMessageDialog(budgetDialog, "Failed to add new budget.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(budgetDialog, "Error adding budget: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private boolean budgetExists(String category) {
        // Check if the budget category already exists in the "budgets" table
        return databaseHandler.budgetExists(category);
    }
    
    private void updateBudgetLimit(String category, double limit) {
        // Update the budget's limit in the "budgets" table
        databaseHandler.updateBudgetLimit(category, limit);
    }
    
    private void checkBudgets() {
        List<Budget> budgets = databaseHandler.getAllBudgets();
        StringBuilder message = new StringBuilder();
        
        if (budgets.isEmpty()) {
            message.append("No budgets to display.");
        } else {
            message.append("Budgets:\n");
            for (Budget budget : budgets) {
                // Assuming 'isOverLimit' checks if the spent amount exceeds the limit
                if (budget.isOverLimit()) {
                    message.append("Category: ").append(budget.getCategory())
                           .append(", Limit: $").append(String.format("%.2f", budget.getLimit()));
                           
                } else {
                    message.append("Category: ").append(budget.getCategory())
                           .append(", Limit: $").append(String.format("%.2f", budget.getLimit()))
                           .append("\n");
                }
            }
        }

        JOptionPane.showMessageDialog(budgetDialog, message.toString(), "Budget Status", JOptionPane.INFORMATION_MESSAGE);
    }


    private void viewTransactions(ActionEvent e) {
        displayTransactions();
        updateTotalSpent();
    }

    private void displayTransactions() {
        // Fetch transactions from the database using the DatabaseHandler
        List<Transaction> transactions = databaseHandler.getAllTransactions();
        double totalSpent = 0;

        // Display transactions in the JTextArea
        StringBuilder transactionText = new StringBuilder("Transactions:\n");
        for (Transaction transaction : transactions) {
            transactionText.append(transaction.toString()).append("\n");
            totalSpent += transaction.getAmount();
        }
        outputArea.setText(transactionText.toString());
        totalSpentLabel.setText("Total Spent: $" + String.format("%.2f", totalSpent));
    }
    
    private void updateTotalSpent() {
        List<Transaction> transactions = databaseHandler.getAllTransactions();
        double totalSpent = 0; // Initialize total spent
        for (Transaction transaction : transactions) {
            totalSpent += transaction.getAmount(); // Accumulate the amount spent
        }
        // Update the total spent label
        totalSpentLabel.setText("Total Spent: $" + String.format("%.2f", totalSpent));
    }

    private void exportTransactions(ActionEvent e) {
        List<Transaction> transactions = databaseHandler.getAllTransactions();
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No transactions to export.", "Export Transactions", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showSaveDialog(frame);

        if (choice == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            try (FileWriter writer = new FileWriter(filePath)) {
                for (Transaction transaction : transactions) {
                    writer.write(transaction.getAmount() + "," + dateFormat.format(transaction.getDate()) + "," +
                            transaction.getDescription() + "," + transaction.getCategory() + "\n");
                }
                JOptionPane.showMessageDialog(frame, "Transactions exported successfully.", "Export Transactions", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error exporting transactions: " + ex.getMessage(), "Export Transactions", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBudget(String category) {
        boolean isDeleted = databaseHandler.deleteBudget(category);
        if (isDeleted) {
            JOptionPane.showMessageDialog(frame, "Budget deleted successfully!", "Delete Budget", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to delete budget. It may not exist or an error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        checkBudgets(); // Refresh the UI to reflect the deletion
    }
    
    private void openChartWindow(ActionEvent e) {
        PieChart pieChartWindow = new PieChart("Budget and Expense Visualization");
        pieChartWindow.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String backgroundImagePath = "images/bg.jpg"; // Update with your image path
            new FinanceTrackerGUI(backgroundImagePath);
        });
    }
}
