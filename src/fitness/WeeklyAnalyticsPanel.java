package fitness;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WeeklyAnalyticsPanel extends JPanel {
    private int userId;
    private JLabel titleLabel;
    private JTable weekTable;
    private JTable waterTable;
    private JTextArea summaryArea;

    public WeeklyAnalyticsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(255, 250, 240));

        titleLabel = new JLabel("Your Weekly Activity & Hydration Report");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 150, 136));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Split activity & water tables in the center with vertical split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        weekTable = new JTable(new DefaultTableModel(
            new Object[]{"Date", "Steps", "Calories", "Duration", "BMI"}, 0));
        weekTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        weekTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        JScrollPane weekScroll = new JScrollPane(weekTable);
        weekScroll.setBorder(BorderFactory.createTitledBorder("Activity"));

        waterTable = new JTable(new DefaultTableModel(
            new Object[]{"Date", "Water Intake (glasses)"}, 0));
        waterTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        waterTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        JScrollPane waterScroll = new JScrollPane(waterTable);
        waterScroll.setBorder(BorderFactory.createTitledBorder("Hydration"));

        splitPane.setLeftComponent(weekScroll);
        splitPane.setRightComponent(waterScroll);
        splitPane.setResizeWeight(0.6);

        add(splitPane, BorderLayout.CENTER);

        summaryArea = new JTextArea(5, 40);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        summaryArea.setMargin(new Insets(10, 12, 10, 12));
        summaryArea.setBackground(new Color(228, 255, 242));
        add(summaryArea, BorderLayout.SOUTH);

        loadWeeklyData();
    }

    private void loadWeeklyData() {
        DefaultTableModel model = (DefaultTableModel) weekTable.getModel();
        DefaultTableModel wmodel = (DefaultTableModel) waterTable.getModel();
        model.setRowCount(0);
        wmodel.setRowCount(0);

        int totalSteps = 0, totalCalories = 0, totalDuration = 0, daysActive = 0;
        double sumBMI = 0.0;
        int totalWater = 0;
        String bestDay = "", worstDay = "";
        int maxSteps = Integer.MIN_VALUE, minSteps = Integer.MAX_VALUE;

        try (Connection con = DBConnector.connect()) {
            String weekSql = "SELECT date, steps, calories, duration, bmi FROM activities WHERE user_id=? AND date >= date('now','-6 day') ORDER BY date ASC";
            PreparedStatement ps = con.prepareStatement(weekSql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String date = rs.getString("date");
                int steps = rs.getInt("steps");
                int calories = rs.getInt("calories");
                int duration = rs.getInt("duration");
                double bmi = rs.getDouble("bmi");
                model.addRow(new Object[]{date, steps, calories, duration, bmi > 0 ? bmi : ""});
                totalSteps += steps;
                totalCalories += calories;
                totalDuration += duration;
                if (steps > 0) {
                    daysActive++;
                    sumBMI += bmi;
                }
                if (steps > maxSteps) { maxSteps = steps; bestDay = date; }
                if (steps < minSteps && steps > 0) { minSteps = steps; worstDay = date; }
            }

            // Water logs for the week
            String waterSql = "SELECT date, water FROM water_log WHERE user_id=? AND date >= date('now','-6 day') ORDER BY date ASC";
            PreparedStatement ps2 = con.prepareStatement(waterSql);
            ps2.setInt(1, userId);
            ResultSet wrs = ps2.executeQuery();
            while (wrs.next()) {
                String date = wrs.getString("date");
                int water = wrs.getInt("water");
                wmodel.addRow(new Object[]{date, water});
                totalWater += water;
            }
        } catch (Exception ex) {
            summaryArea.setText("Error retrieving weekly data: " + ex.getMessage());
            return;
        }

        double avgBMI = daysActive > 0 ? Math.round((sumBMI / daysActive) * 100.0) / 100.0 : 0.0;
        StringBuilder sb = new StringBuilder();
        sb.append("Total Steps: ").append(totalSteps).append("\n");
        sb.append("Total Calories Burned: ").append(totalCalories).append("\n");
        sb.append("Total Duration: ").append(totalDuration).append(" min\n");
        sb.append("Active Days: ").append(daysActive).append("/7\n");
        sb.append("Average BMI (active days): ").append(avgBMI > 0 ? avgBMI : "N/A").append("\n");
        sb.append("Total Water Intake: ").append(totalWater).append(" glasses\n");
        if (!bestDay.isEmpty()) sb.append("🏅 Best Day: ").append(bestDay).append(" (").append(maxSteps).append(" steps)\n");
        if (!worstDay.isEmpty() && !worstDay.equals(bestDay)) sb.append("😴 Least Active Day: ").append(worstDay).append(" (").append(minSteps).append(" steps)\n");
        if (totalWater < 56) sb.append("\n💧 Hydration: Try to drink at least 8 glasses/day for max benefit!");
        if (totalSteps == 0) sb.append("\nLet's get moving! Aim for a walk today.");
        else if (daysActive < 3) sb.append("\nTry for more consistency next week!");
        else sb.append("\nImpressive! Keep the momentum going!");

        summaryArea.setText(sb.toString());
    }
}