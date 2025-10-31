package fitness;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private int userId;

    public DashboardFrame(int userId) {
        this.userId = userId;
        setTitle("Fitness Tracker - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);

        // Profile tab
        tabs.addTab("Profile", new ProfilePanel(userId));

        // Activity tab: PASS THE TABS SO BUTTONS CAN SWITCH
        tabs.addTab("Activity Log", new ActivityPanel(userId, tabs));

        // BMI Calculator tab
        tabs.addTab("BMI Calculator", new BMICalculatorPanel(userId));

        // BMI Progress Chart tab
        tabs.addTab("BMI Progress", new BMIProgressPanel(userId));

        // Suggestions tab
        tabs.addTab("Suggestions", new SuggestionsPanel(userId));

        // weekly activity 
        tabs.addTab("Weekly Report", new WeeklyAnalyticsPanel(userId));

        //Achievements
        tabs.addTab("Achievements", new AchievementsPanel(userId));

        //water detail
        tabs.addTab("Water Tracker", new WaterPanel(userId));

        tabs.addTab("Sleep Tracker", new SleepTrackerPanel(userId));

        tabs.addTab("Trend Prediction", new TrendPredictionPanel(userId));



        add(tabs);
        setVisible(true);
    }
}