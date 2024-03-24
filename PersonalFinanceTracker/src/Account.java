import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Account {
    private List<Transaction> transactions;
    private Map<String, Budget> budgets;

    public Account() {
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        String category = transaction.getCategory();
        if (budgets.containsKey(category)) {
            budgets.get(category).addSpending(transaction.getAmount());
        }
    }

    public double calculateTotalBalance() {
        double balance = 0;
        for (Transaction transaction : transactions) {
            balance += transaction.getAmount();
        }
        return balance;
    }

    public void addBudget(String category, double limit) {
        budgets.put(category, new Budget(category, limit));
    }

    public void checkBudgets() {
        for (Budget budget : budgets.values()) {
            if (budget.isOverLimit()) {
                System.out.println("Budget exceeded for category: " + budget.getCategory());
            } else {
                System.out.println("Budget for " + budget.getCategory() + " is within limit.");
            }
        }
    }

    public Map<String, Budget> getBudgets() {
        return budgets;
    }

    // Getters and setters for transactions
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
