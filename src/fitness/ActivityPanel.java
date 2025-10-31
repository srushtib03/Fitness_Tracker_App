package fitness;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ActivityPanel extends JPanel {
    private int userId;
    private JTabbedPane dashboardTabs;
    private JTextField dateField, stepsField, durationField;
    private JTable table;

    public ActivityPanel(int userId, JTabbedPane dashboardTabs) {
        this.userId = userId;
        this.dashboardTabs = dashboardTabs;

        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(232, 245, 233));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(232, 245, 233));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 11, 5, 11); gbc.gridx = 0; gbc.gridy = 0;

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc); gbc.gridx = 1;
        dateField = new JTextField(8);
        formPanel.add(dateField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        formPanel.add(new JLabel("Steps:"), gbc); gbc.gridx = 1;
        stepsField = new JTextField(8);
        formPanel.add(stepsField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        formPanel.add(new JLabel("Duration (min):"), gbc); gbc.gridx = 1;
        durationField = new JTextField(8);
        formPanel.add(durationField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout());

        // Add Activity Button
        JButton addButton = new JButton("Add Activity");
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(true);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btnPanel.add(addButton);

        // BMI Calculator Button
        JButton bmiButton = new JButton("BMI Calculator");
        bmiButton.setBackground(new Color(255, 109, 0));
        bmiButton.setForeground(Color.WHITE);
        bmiButton.setOpaque(true);
        bmiButton.setBorderPainted(false);
        bmiButton.setContentAreaFilled(true);
        bmiButton.setFocusPainted(false);
        bmiButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        bmiButton.addActionListener(e -> {
            if (dashboardTabs != null)
                dashboardTabs.setSelectedIndex(2); // BMI Calculator tab index
        });
        btnPanel.add(bmiButton);

        // Show BMI Progress Button
        JButton chartButton = new JButton("Show BMI Progress");
        chartButton.setBackground(new Color(30, 136, 229));
        chartButton.setForeground(Color.WHITE);
        chartButton.setOpaque(true);
        chartButton.setBorderPainted(false);
        chartButton.setContentAreaFilled(true);
        chartButton.setFocusPainted(false);
        chartButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        chartButton.addActionListener(e -> {
            if (dashboardTabs != null)
                dashboardTabs.setSelectedIndex(3); // BMI Progress tab index
        });
        btnPanel.add(chartButton);

        // Profile Button
        JButton profileButton = new JButton("Profile");
        profileButton.setBackground(new Color(25, 118, 210));
        profileButton.setForeground(Color.WHITE);
        profileButton.setOpaque(true);
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(true);
        profileButton.setFocusPainted(false);
        profileButton.setFont(new Font("Arial", Font.BOLD, 13));
        profileButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        profileButton.addActionListener(e -> {
            if (dashboardTabs != null)
                dashboardTabs.setSelectedIndex(0); // Profile tab index
        });
        btnPanel.add(profileButton);

        // Show All Profiles (Debug) Button
        JButton showProfilesButton = new JButton("Show All Profiles (Debug)");
        showProfilesButton.setBackground(new Color(85, 85, 85));
        showProfilesButton.setForeground(Color.WHITE);
        showProfilesButton.setOpaque(true);
        showProfilesButton.setBorderPainted(false);
        showProfilesButton.setContentAreaFilled(true);
        showProfilesButton.setFocusPainted(false);
        showProfilesButton.setFont(new Font("Arial", Font.BOLD, 13));
        showProfilesButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        showProfilesButton.addActionListener(e -> {
            try (Connection con = DBConnector.connect()) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM user_profiles");
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append("UserID: ").append(rs.getInt("user_id"))
                      .append(", Age: ").append(rs.getInt("age"))
                      .append(", Gender: ").append(rs.getString("gender"))
                      .append(", Height: ").append(rs.getDouble("height"))
                      .append(", Weight: ").append(rs.getDouble("weight"))
                      .append(", Goal Steps: ").append(rs.getInt("goal_steps"))
                      .append(", Goal Calories: ").append(rs.getInt("goal_calories"))
                      .append(", Goal BMI: ").append(rs.getDouble("goal_bmi"))
                      .append("\n");
                }
                if (sb.length() == 0) {
                    sb.append("No profiles found.");
                }
                JOptionPane.showMessageDialog(this, sb.toString(), "All User Profiles", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error retrieving profiles: " + ex.getMessage());
            }
        });
        btnPanel.add(showProfilesButton);

        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(new Object[]{
            "Date", "Steps", "Calories Burned", "Duration", "BMI", "Category"
        }, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addActivity());

        add(mainPanel, BorderLayout.CENTER);

        loadTable();
    }

    private void addActivity() {
        try (Connection con = DBConnector.connect()) {
            String date = dateField.getText().trim();
            int steps = Integer.parseInt(stepsField.getText().trim());
            int duration = Integer.parseInt(durationField.getText().trim());

            int calories = (int) Math.round(steps * 0.05);

            String sql = "INSERT INTO activities(user_id, date, steps, calories, duration) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, date);
            ps.setInt(3, steps);
            ps.setInt(4, calories);
            ps.setInt(5, duration);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Activity added successfully!");
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding activity: " + ex.getMessage());
        }
    }

    private void loadTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        System.out.println("Loading activities for userId: " + userId);

        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT date, steps, calories, duration, bmi, bmi_category FROM activities WHERE user_id=? ORDER BY date DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("date"),
                    rs.getInt("steps"),
                    rs.getInt("calories"),
                    rs.getInt("duration"),
                    rs.getObject("bmi"),
                    rs.getString("bmi_category")
                });
                rowCount++;
            }
            System.out.println("Activity log loaded " + rowCount + " rows.");
            if (rowCount == 0) {
                JOptionPane.showMessageDialog(this, "No activity records found for this user.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            table.revalidate();
            table.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}