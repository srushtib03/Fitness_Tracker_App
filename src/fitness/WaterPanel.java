package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class WaterPanel extends JPanel {
    private int userId;
    private JSpinner waterSpinner;
    private JTextArea logArea;

    public WaterPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(226, 239, 255));

        JLabel title = new JLabel("Daily Water Tracker");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(110, 198, 253));
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel();
        JLabel waterLabel = new JLabel("Glasses Today:");
        waterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        waterSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        inputPanel.add(waterLabel);
        inputPanel.add(waterSpinner);

        JButton logBtn = new JButton("Log Water");
        logBtn.setBackground(new Color(41, 182, 246));
        logBtn.setForeground(Color.WHITE);
        inputPanel.add(logBtn);

        add(inputPanel, BorderLayout.CENTER);

        logArea = new JTextArea(4, 28);
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        logArea.setBackground(new Color(234, 247, 255));
        add(logArea, BorderLayout.SOUTH);

        logBtn.addActionListener(e -> logWater());
        loadWaterLog();
    }

    private void logWater() {
        int glasses = (Integer) waterSpinner.getValue();
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT OR REPLACE INTO water_log(user_id,date,water) VALUES(?,date('now'),?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, glasses);
            ps.executeUpdate();
            logArea.setText("Logged " + glasses + " glasses today.\nKeep hydrated!");
        } catch (Exception e) {
            logArea.setText("Failed to log water: " + e.getMessage());
        }
    }

    private void loadWaterLog() {
        int water = 0;
        try (Connection con = DBConnector.connect()) {
            PreparedStatement ps = con.prepareStatement("SELECT water FROM water_log WHERE user_id=? AND date=date('now')");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) water = rs.getInt("water");
        } catch (Exception e) {}
        logArea.setText("Today's water: " + water + " glasses.\nAim for 8+ every day!");
    }
}