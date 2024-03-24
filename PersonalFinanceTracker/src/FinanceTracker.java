import java.util.Scanner;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FinanceTracker {
    private static Scanner scanner = new Scanner(System.in);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws ParseException {
        Account account = new Account();

        boolean keepRunning = true;
        while (keepRunning) {
            System.out.println("Choose an option: \n1. Log Transaction \n2. Add Budget \n3. Show Balance \n4. Check Budgets \n5. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            switch (choice) {
                case 1:
                    logTransaction(account);
                    break;
                case 2:
                    addBudget(account);
                    break;
                case 3:
                    System.out.println("Total Balance: " + account.calculateTotalBalance());
                    break;
                case 4:
                    account.checkBudgets();
                    break;
                case 5:
                    keepRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static void logTransaction(Account account) throws ParseException {
        System.out.println("Enter amount:");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline

        System.out.println("Enter date (yyyy-mm-dd):");
        String dateString = scanner.nextLine();
        Date date = dateFormat.parse(dateString);

        System.out.println("Enter description:");
        String description = scanner.nextLine();

        System.out.println("Enter category:");
        String category = scanner.nextLine();

        Transaction transaction = new Transaction(amount, date, description, category);
        account.addTransaction(transaction);
        System.out.println("Transaction added successfully!");
    }

    private static void addBudget(Account account) {
        System.out.println("Enter category:");
        String category = scanner.nextLine();

        System.out.println("Enter budget limit:");
        double limit = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline

        account.addBudget(category, limit);
        System.out.println("Budget added successfully for " + category);
    }
}
