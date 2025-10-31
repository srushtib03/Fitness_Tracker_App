package fitness;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel() {
        setTitle("Fitness Tracker - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("FITNESS TRACKER");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setForeground(new Color(0, 150, 136));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(14);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(usernameField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(14);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(passwordField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 150, 136));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        btnPanel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(30, 136, 229));
        registerButton.setForeground(Color.WHITE);
        btnPanel.add(registerButton);

        panel.add(btnPanel, gbc);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        getContentPane().add(panel);
        setSize(370, 280);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void login() {
        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usernameField.getText());
            ps.setString(2, new String(passwordField.getPassword()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
    JOptionPane.showMessageDialog(this, "Login successful!");
    dispose();
    new DashboardFrame(rs.getInt("id")); // <--- open the dashboard tabs for the logged-in user
} else {
    JOptionPane.showMessageDialog(this, "Invalid credentials!");
}

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private void register() {
        try (Connection con = DBConnector.connect()) {
            String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usernameField.getText());
            ps.setString(2, new String(passwordField.getPassword()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User registered successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
        }
    }
}