package fitness;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class CalorieTrackerPanel extends JPanel {
    private JTextField foodField, calField;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, netLabel;
    private int userId;

    public CalorieTrackerPanel(int userId) {
        this.userId = userId;
        setBackground(new Color(255, 248, 225));
        setLayout(new BorderLayout(12,10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(255, 248, 225));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,10,5,10);

        JLabel title = new JLabel("Calorie Tracker");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(255,140,0));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        inputPanel.add(title, gbc);

        gbc.gridwidth=1; gbc.gridy++;
        gbc.gridx=0; inputPanel.add(new JLabel("Food/Drink:"), gbc);
        gbc.gridx=1; foodField = new JTextField(12); inputPanel.add(foodField, gbc);

        gbc.gridy++; gbc.gridx=0; inputPanel.add(new JLabel("Calories:"), gbc);
        gbc.gridx=1; calField = new JTextField(6); inputPanel.add(calField, gbc);

        gbc.gridy++; gbc.gridx=0; gbc.gridwidth=2;
        JButton addBtn = new JButton("Add Food Log");
        addBtn.setBackground(new Color(255,140,0));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Arial", Font.BOLD, 13));
        inputPanel.add(addBtn, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[] { "Food", "Calories", "Date" }, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN,14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD,14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Summary Panel
        JPanel sumPanel = new JPanel(new GridLayout(2,1));
        sumPanel.setBackground(new Color(255, 248, 225));
        totalLabel = new JLabel("Total Calories Today: 0 kcal");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 15));
        netLabel = new JLabel(""); // Will update later if you want Net Calories
        sumPanel.add(totalLabel);
        sumPanel.add(netLabel);
        add(sumPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addFoodLog());
        loadTableAndTotal();
    }

    private void addFoodLog() {
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT INTO calorie_logs(user_id, food_name, calories, date) VALUES(?,?,?, date('now'))";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, foodField.getText());
            ps.setInt(3, Integer.parseInt(calField.getText().trim()));
            ps.executeUpdate();

            foodField.setText("");
            calField.setText("");
            loadTableAndTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding food: " + ex.getMessage());
        }
    }

    private void loadTableAndTotal() {
        DefaultTableModel model = (DefaultTableModel) tableModel;
        model.setRowCount(0);
        int total = 0;
        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT food_name, calories, date FROM calorie_logs WHERE user_id=? AND date=date('now')";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("food_name"),
                        rs.getInt("calories"),
                        rs.getString("date")
                });
                total += rs.getInt("calories");
            }
            totalLabel.setText("Total Calories Today: " + total + " kcal");

            // Optional: Get Calories Burned (from activity table)
            int burned = getTodayCaloriesBurned();
            if (burned >= 0) {
                netLabel.setText("Net Calories (Consumed - Burned): " + (total - burned) + " kcal");
            } else {
                netLabel.setText("");
            }

        } catch (Exception ex) {
            totalLabel.setText("Total Calories Today: 0 kcal");
        }
    }

    private int getTodayCaloriesBurned() {
        int total = 0;
        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT SUM(calories) FROM activities WHERE user_id=? AND date=date('now')";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getInt(1);
        } catch (Exception e) {
            return -1;
        }
        return total;
    }
}
