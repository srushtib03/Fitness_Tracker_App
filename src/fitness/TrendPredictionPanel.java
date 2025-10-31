package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TrendPredictionPanel extends JPanel {
    private int userId;
    private JTextArea predictionArea;
    private JButton refreshBtn;

    public TrendPredictionPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(255, 248, 225));

        JLabel title = new JLabel("Trend Prediction & Fitness Coach");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(255, 109, 0));
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        predictionArea = new JTextArea(8, 40);
        predictionArea.setEditable(false);
        predictionArea.setLineWrap(true);
        predictionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        predictionArea.setMargin(new Insets(12,16,12,16));
        add(predictionArea, BorderLayout.CENTER);

        refreshBtn = new JButton("Analyze & Predict");
        refreshBtn.setBackground(new Color(255, 183, 77));
        refreshBtn.setForeground(Color.BLACK);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> predictTrends());
        add(refreshBtn, BorderLayout.SOUTH);

        predictTrends();
    }

    private void predictTrends() {
        int stepsGoal = 0, caloriesGoal = 0;
        double bmiGoal = 0;
        int totalSteps = 0, totalCalories = 0, logDays = 0;
        double lastBMI = 0;

        try (Connection con = DBConnector.connect()) {
            // Current goals:
            PreparedStatement p1 = con.prepareStatement("SELECT goal_steps, goal_calories, goal_bmi FROM user_profiles WHERE user_id=?");
            p1.setInt(1, userId);
            ResultSet r1 = p1.executeQuery();
            if (r1.next()) {
                stepsGoal = r1.getInt("goal_steps");
                caloriesGoal = r1.getInt("goal_calories");
                bmiGoal = r1.getDouble("goal_bmi");
            }

            // Last 7 days log
            PreparedStatement p2 = con.prepareStatement("SELECT steps, calories, bmi FROM activities WHERE user_id=? AND date >= date('now','-6 day')");
            p2.setInt(1, userId);
            ResultSet r2 = p2.executeQuery();
            while (r2.next()) {
                totalSteps += r2.getInt("steps");
                totalCalories += r2.getInt("calories");
                double b = r2.getDouble("bmi");
                if (b > 0) lastBMI = b;
                logDays++;
            }
        } catch (Exception e) {
            predictionArea.setText("Prediction unavailable: " + e.getMessage());
            return;
        }

        StringBuilder sb = new StringBuilder();
        double projectedSteps = (logDays > 0) ? totalSteps * 7.0 / logDays : 0;
        double projectedCalories = (logDays > 0) ? totalCalories * 7.0 / logDays : 0;

        sb.append("Your current weekly goals:\n");
        sb.append(" • Steps: ").append(stepsGoal).append("\n • Calories: ").append(caloriesGoal).append("\n • BMI: ").append(bmiGoal).append("\n\n");
        sb.append("Based on your recent activity (" + logDays + " days logged):\n");
        sb.append(" • Projected weekly steps: ").append((int)projectedSteps).append("\n");
        sb.append(" • Projected weekly calories: ").append((int)projectedCalories).append("\n");

        // Steps advice
        if (stepsGoal > 0) {
            int diff = (int)(stepsGoal - projectedSteps);
            if (diff <= 0)
                sb.append("✅ At current pace you'll HIT your steps goal!\n");
            else
                sb.append("⚡ Increase daily steps by ").append(diff/7 + 1).append(" to reach your goal!\n");
        }
        // Calories advice
        if (caloriesGoal > 0) {
            int diff = (int)(caloriesGoal - projectedCalories);
            if (diff <= 0)
                sb.append("✅ You are on track for your calories-burned goal!\n");
            else
                sb.append("\uD83D\uDCAA Try an extra cardio activity to reach calories goal.\n");
        }
        // BMI advice
        if (bmiGoal > 0 && lastBMI > 0) {
            double delta = lastBMI - bmiGoal;
            if (Math.abs(delta) <= 0.5)
                sb.append("👏 Your BMI trend is on target!\n");
            else if (delta < 0)
                sb.append("🍲 BMI is below your target. Add healthy calories.\n");
            else
                sb.append("🏃 Stay active! Your BMI is above goal, keep moving!\n");
        }

        sb.append("\n👾 Fitness Coach: \"Small changes add up. Keep pushing and you'll see results!\"");

        predictionArea.setText(sb.toString());
    }
}