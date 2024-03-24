public class Budget {
    private String category;
    private double limit;
    private double spent;

    public Budget(String category, double limit) {
        this.category = category;
        this.limit = limit;
        this.spent = 0;
    }

    // Method to add spending to the budget
    public void addSpending(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.spent += amount;
    }

    // Check if the budget limit is exceeded
    public boolean isOverLimit() {
        return spent > limit;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public double getSpent() {
        return spent;
    }

    public void setLimit(double limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        this.limit = limit;
    }

    public void setSpent(double spent) {
        if (spent < 0) {
            throw new IllegalArgumentException("Spent amount cannot be negative");
        }
        this.spent = spent;
    }

    @Override
    public String toString() {
        return String.format("Budget{category='%s', limit=%.2f, spent=%.2f}", category, limit, spent);
    }
}
