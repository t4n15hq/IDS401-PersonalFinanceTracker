import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class PieChart extends JFrame {
    private DatabaseHandler databaseHandler;

    public PieChart(String title) {
        super(title);

        this.databaseHandler = new DatabaseHandler(); // Initialize your DatabaseHandler here

        setLayout(new GridLayout(1, 2)); // Layout to display two charts side by side

        // Add Pie Chart
        JPanel pieChartPanel = createBudgetPieChartPanel();
        add(pieChartPanel);

        // Add Bar Chart
        JPanel barChartPanel = createExpenseBarChartPanel();
        add(barChartPanel);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createBudgetPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Double> budgets = databaseHandler.getBudgetLimits(); // Method to fetch budget data

        for (Map.Entry<String, Double> entry : budgets.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Budget Distribution",
                dataset,
                true, true, false);

        return new ChartPanel(chart);
    }

    private JPanel createExpenseBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> expenses = databaseHandler.getMonthlyExpenses(); // Method to fetch expense data

        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), "Expenses");
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Expenses",
                "Category",
                "Amount",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true, true, false);

        return new ChartPanel(barChart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PieChart demo = new PieChart("Budget and Expense Visualization");
            demo.setVisible(true);
        });
    }
}
