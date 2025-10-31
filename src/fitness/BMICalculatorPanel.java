package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BMICalculatorPanel extends JPanel {
    private JTextField weightField, heightField, bmiField, categoryField;
    private int userId;

    public BMICalculatorPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 253, 231));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 9, 9, 9);

        JLabel title = new JLabel("BMI CALCULATOR");
        title.setFont(new Font("Verdana", Font.BOLD, 20));
        title.setForeground(new Color(255, 109, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        weightField = new JTextField(10);
        panel.add(weightField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Height (m):"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(10);
        panel.add(heightField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton calculateButton = new JButton("Calculate & Save BMI");
        calculateButton.setBackground(new Color(255, 109, 0));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(calculateButton, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Your BMI:"), gbc);
        gbc.gridx = 1;
        bmiField = new JTextField(10); bmiField.setEditable(false);
        bmiField.setBackground(Color.WHITE);
        panel.add(bmiField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryField = new JTextField(10); categoryField.setEditable(false);
        categoryField.setBackground(Color.WHITE);
        panel.add(categoryField, gbc);

        calculateButton.addActionListener(e -> calculateAndSaveBMI());
        add(panel, BorderLayout.CENTER);
    }

    private void calculateAndSaveBMI() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());
            if (weight <= 0 || height <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter positive values!");
                return;
            }
            double bmi = weight / (height * height);
            bmi = Math.round(bmi * 100.0) / 100.0;
            bmiField.setText(String.valueOf(bmi));

            String category;
            if (bmi < 18.5) category = "Underweight";
            else if (bmi < 24.9) category = "Normal";
            else if (bmi < 29.9) category = "Overweight";
            else category = "Obese";
            categoryField.setText(category);

            saveToDatabase(bmi, category);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values for height and weight.");
        }
    }

    private void saveToDatabase(double bmi, String category) {
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT INTO activities(user_id, date, steps, calories, duration, bmi, bmi_category) VALUES(?, date('now'), 0, 0, 0, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setDouble(2, bmi);
            ps.setString(3, category);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "BMI saved successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving BMI: " + ex.getMessage());
        }
    }
}