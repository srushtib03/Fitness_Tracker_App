package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class AchievementsPanel extends JPanel {
    private int userId;
    private JPanel badgesPanel;
    private HashMap<String, Boolean> badgeStatus = new HashMap<>();

    public AchievementsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Achievements & Badges");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(186, 104, 200));
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        badgesPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        badgesPanel.setBackground(new Color(250, 245, 255));
        add(badgesPanel, BorderLayout.CENTER);

        loadBadges();
    }

    private void loadBadges() {
        // Example badges
        badgeStatus.put("10k Steps in a Day", false);
        badgeStatus.put("7-Day Activity Streak", false);
        badgeStatus.put("BMI ≤ 24.9", false);
        badgeStatus.put("First Activity Logged", false);
        badgeStatus.put("100k Steps in a Month", false);
        badgeStatus.put("Perfect Weekly Log", false);

        try (Connection con = DBConnector.connect()) {
            // 10k Steps in a Day
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT MAX(steps) AS maxSteps FROM activities WHERE user_id=" + userId);
            if (rs.next() && rs.getInt("maxSteps") >= 10000) badgeStatus.put("10k Steps in a Day", true);

            // 7-Day Streak
            rs = con.createStatement().executeQuery(
                "SELECT COUNT(DISTINCT date) AS activeDays FROM activities WHERE user_id=" + userId + " AND steps > 0 AND date >= date('now','-6 day')");
            if (rs.next() && rs.getInt("activeDays") == 7) badgeStatus.put("7-Day Activity Streak", true);

            // BMI ≤ 24.9
            rs = con.createStatement().executeQuery(
                "SELECT MAX(bmi) AS bestBMI FROM activities WHERE user_id=" + userId + " AND bmi<=24.9");
            if (rs.next() && rs.getDouble("bestBMI") > 0) badgeStatus.put("BMI ≤ 24.9", true);

            // First Activity Logged
            rs = con.createStatement().executeQuery(
                "SELECT COUNT(*) AS total FROM activities WHERE user_id=" + userId);
            if (rs.next() && rs.getInt("total") > 0) badgeStatus.put("First Activity Logged", true);

            // 100k Steps in a Month
            rs = con.createStatement().executeQuery(
                "SELECT SUM(steps) AS monthSteps FROM activities WHERE user_id=" + userId + " AND date >= date('now','-29 day')");
            if (rs.next() && rs.getInt("monthSteps") >= 100000) badgeStatus.put("100k Steps in a Month", true);

            // Perfect Weekly Log (all days logged, over 5k steps)
            rs = con.createStatement().executeQuery(
                "SELECT COUNT(*) AS perfectDays FROM activities WHERE user_id=" + userId + " AND steps >= 5000 AND date >= date('now','-6 day')");
            if (rs.next() && rs.getInt("perfectDays") == 7) badgeStatus.put("Perfect Weekly Log", true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading badges: " + ex.getMessage());
        }

        badgesPanel.removeAll();
        for (Map.Entry<String, Boolean> badge : badgeStatus.entrySet()) {
            JLabel badgeLabel = new JLabel(
                (badge.getValue() ? "🏅 " : "🔒 ") + badge.getKey(),
                JLabel.CENTER);
            badgeLabel.setOpaque(true);
            badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            badgeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
            badgeLabel.setBackground(badge.getValue() ? new Color(197, 225, 165) : new Color(224, 224, 224));
            badgeLabel.setForeground(badge.getValue() ? Color.BLACK : new Color(167, 117, 195));
            badgeLabel.setPreferredSize(new Dimension(210, 65));
            badgesPanel.add(badgeLabel);
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
    }
}