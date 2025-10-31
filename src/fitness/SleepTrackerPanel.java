package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import javax.swing.table.DefaultTableModel;


public class SleepTrackerPanel extends JPanel {
    private int userId;
    private JTextField sleepTimeField, wakeTimeField;
    private JTable table;
    private JTextArea summaryArea;

    public SleepTrackerPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        JLabel title = new JLabel("Sleep Tracker: Log & Analyze");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(41, 128, 185));
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Sleep Time (HH:mm):"));
        sleepTimeField = new JTextField(5);
        inputPanel.add(sleepTimeField);
        inputPanel.add(new JLabel("Wake Time (HH:mm):"));
        wakeTimeField = new JTextField(5);
        inputPanel.add(wakeTimeField);

        JButton logBtn = new JButton("Log Sleep");
        logBtn.setBackground(new Color(76, 175, 80));
        logBtn.setForeground(Color.WHITE);
        inputPanel.add(logBtn);

        add(inputPanel, BorderLayout.NORTH);

        table = new JTable(new javax.swing.table.DefaultTableModel(
                new Object[]{"Date", "Sleep Time", "Wake Time", "Duration (hrs)"}, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        add(new JScrollPane(table), BorderLayout.CENTER);

        summaryArea = new JTextArea(3, 32);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        summaryArea.setBackground(new Color(220, 248, 219));
        add(summaryArea, BorderLayout.SOUTH);

        logBtn.addActionListener(e -> logSleep());
        loadWeekSleep();
    }

    private void logSleep() {
        String sleep = sleepTimeField.getText();
        String wake = wakeTimeField.getText();
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT OR REPLACE INTO sleep_log(user_id, date, sleep_time, wake_time) VALUES(?, date('now'), ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, sleep);
            ps.setString(3, wake);
            ps.executeUpdate();
            loadWeekSleep();
        } catch (Exception e) {
            summaryArea.setText("Failed to log sleep: " + e.getMessage());
        }
    }

    private void loadWeekSleep() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        double totalSleep = 0;
        int days = 0;
        String bestDay = "", worstDay = "";
        double maxSleep = -1, minSleep = Double.MAX_VALUE;

        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT date, sleep_time, wake_time FROM sleep_log WHERE user_id=? AND date >= date('now','-6 day') ORDER BY date ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String date = rs.getString("date");
                String sleepStr = rs.getString("sleep_time");
                String wakeStr = rs.getString("wake_time");

                double duration = calcDuration(sleepStr, wakeStr);
                model.addRow(new Object[]{date, sleepStr, wakeStr, String.format("%.2f", duration)});
                totalSleep += duration;
                days++;

                if (duration > maxSleep) { maxSleep = duration; bestDay = date; }
                if (duration < minSleep) { minSleep = duration; worstDay = date; }
            }
        } catch (Exception e) {}

        double avgSleep = days > 0 ? totalSleep / days : 0;
        StringBuilder sb = new StringBuilder("Average Sleep Duration: ");
        sb.append(String.format("%.2f", avgSleep)).append(" hrs/night\n");
        if (!bestDay.isEmpty()) sb.append("😴 Best Sleep: ").append(bestDay).append(" (").append(String.format("%.2f", maxSleep)).append(" hrs)\n");
        if (!worstDay.isEmpty() && !worstDay.equals(bestDay)) sb.append("💤 Shortest Sleep: ").append(worstDay).append(" (").append(String.format("%.2f", minSleep)).append(" hrs)\n");
        if (avgSleep < 7) sb.append("⏰ Try to sleep at least 7 hrs/night for best health!");
        else sb.append("🌟 You're sleeping well this week!");

        summaryArea.setText(sb.toString());
    }

    private double calcDuration(String sleep, String wake) {
        try {
            LocalTime st = LocalTime.parse(sleep, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime wt = LocalTime.parse(wake, DateTimeFormatter.ofPattern("HH:mm"));
            double hours = (wt.toSecondOfDay() - st.toSecondOfDay()) / 3600.0;
            if (hours < 0) hours += 24.0; // handle overnight sleep
            return hours;
        } catch (Exception e) {
            return 0.0;
        }
    }
}