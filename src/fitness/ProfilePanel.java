
package fitness;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProfilePanel extends JPanel {
    private JTextField ageField, heightField, weightField, goalStepsField, goalCaloriesField, goalBMIField;
    private JComboBox<String> genderBox;
    private JButton saveButton, refreshButton;
    private int userId;

    public ProfilePanel(int userId) {
        this.userId = userId;
        setLayout(new GridBagLayout());
        setBackground(new Color(255, 249, 230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(13, 13, 9, 13);

        JLabel title = new JLabel("YOUR PROFILE & GOALS");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        // Age
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Age:"), gbc);
        ageField = new JTextField(8); gbc.gridx = 1; add(ageField, gbc);

        // Gender
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Gender:"), gbc);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"}); gbc.gridx = 1; add(genderBox, gbc);

        // Height
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Height (cm):"), gbc);
        heightField = new JTextField(8); gbc.gridx = 1; add(heightField, gbc);

        // Weight
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Weight (kg):"), gbc);
        weightField = new JTextField(8); gbc.gridx = 1; add(weightField, gbc);

        // Weekly Goal: Steps
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Weekly Goal - Steps:"), gbc);
        goalStepsField = new JTextField(8); gbc.gridx = 1; add(goalStepsField, gbc);

        // Weekly Goal: Calories
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Weekly Goal - Calories:"), gbc);
        goalCaloriesField = new JTextField(8); gbc.gridx = 1; add(goalCaloriesField, gbc);

        // Weekly Goal: BMI
        gbc.gridy++; gbc.gridx = 0; add(new JLabel("Weekly Goal - BMI:"), gbc);
        goalBMIField = new JTextField(8); gbc.gridx = 1; add(goalBMIField, gbc);

        // Buttons
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Save Profile & Goals");
        saveButton.setBackground(new Color(25, 118, 210));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 13));
        btnPanel.add(saveButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(46, 204, 113));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 13));
        btnPanel.add(refreshButton);

        add(btnPanel, gbc);

        saveButton.addActionListener(e -> saveProfileAndGoals());
        refreshButton.addActionListener(e -> loadProfile());

        // Automatically load current user profile at startup
        loadProfile();
    }

    private void saveProfileAndGoals() {
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT OR REPLACE INTO user_profiles(user_id, age, gender, height, weight, goal_steps, goal_calories, goal_bmi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, Integer.parseInt(ageField.getText().trim()));
            ps.setString(3, (String)genderBox.getSelectedItem());
            ps.setDouble(4, Double.parseDouble(heightField.getText().trim()));
            ps.setDouble(5, Double.parseDouble(weightField.getText().trim()));
            ps.setInt(6, Integer.parseInt(goalStepsField.getText().trim()));
            ps.setInt(7, Integer.parseInt(goalCaloriesField.getText().trim()));
            ps.setDouble(8, Double.parseDouble(goalBMIField.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Profile & Goals saved!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + ex.getMessage());
        }
    }

    private void loadProfile() {
        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT * FROM user_profiles WHERE user_id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ageField.setText(rs.getString("age"));
                genderBox.setSelectedItem(rs.getString("gender"));
                heightField.setText(rs.getString("height"));
                weightField.setText(rs.getString("weight"));
                goalStepsField.setText(rs.getString("goal_steps"));
                goalCaloriesField.setText(rs.getString("goal_calories"));
                goalBMIField.setText(rs.getString("goal_bmi"));
            }
        } catch (SQLException e) {
            // Leave fields blank if no profile exists
        }
    }
}