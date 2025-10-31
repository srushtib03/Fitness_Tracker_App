package fitness;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class SuggestionsPanel extends JPanel {
    private int userId;
    private JTextArea suggestionsArea;

    public SuggestionsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(224, 247, 250));

        JLabel title = new JLabel("Personalized Suggestions (AI Powered by Gemini)");
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setForeground(new Color(30, 136, 229));
        add(title, BorderLayout.NORTH);

        suggestionsArea = new JTextArea(15, 40);
        suggestionsArea.setWrapStyleWord(true);
        suggestionsArea.setLineWrap(true);
        suggestionsArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        suggestionsArea.setEditable(false);
        suggestionsArea.setMargin(new Insets(15,12,15,12));
        add(new JScrollPane(suggestionsArea), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("AI Analyze My Data");
        refreshBtn.setBackground(new Color(0, 150, 136));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 15));
        refreshBtn.addActionListener(e -> fetchAISuggestionsGemini());
        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        fetchAISuggestionsGemini(); // load at start
    }

    private void fetchAISuggestionsGemini() {
        suggestionsArea.setText("Generating your personalized fitness suggestions with Gemini AI...");
        new Thread(() -> {
            String context = buildUserContext();
            String result = getAISuggestionGemini(context);
            SwingUtilities.invokeLater(() -> suggestionsArea.setText(result));
        }).start();
    }

    private String buildUserContext() {
        StringBuilder sb = new StringBuilder();
        try (Connection con = DBConnector.connect()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM user_profiles WHERE user_id=?");
            ps.setInt(1, userId);
            ResultSet profileRs = ps.executeQuery();
            int age = 0, stepsGoal = 0;
            double goalBMI = 0.0, weight = 0.0, height = 0.0;
            String gender = "";
            if (profileRs.next()) {
                age = profileRs.getInt("age");
                gender = profileRs.getString("gender");
                stepsGoal = profileRs.getInt("goal_steps");
                goalBMI = profileRs.getDouble("goal_bmi");
                weight = profileRs.getDouble("weight");
                height = profileRs.getDouble("height");
            }

            int weeklySteps = 0;
            double latestBMI = 0.0;
            String latestBMICategory = "";

            PreparedStatement weekSteps = con.prepareStatement(
                "SELECT SUM(steps) AS totalSteps FROM activities WHERE user_id=? AND date >= date('now','-6 day')");
            weekSteps.setInt(1, userId);
            ResultSet stepRs = weekSteps.executeQuery();
            if (stepRs.next()) weeklySteps = stepRs.getInt("totalSteps");

            PreparedStatement bmiQ = con.prepareStatement(
                "SELECT bmi, bmi_category FROM activities WHERE user_id=? AND bmi IS NOT NULL ORDER BY date DESC LIMIT 1");
            bmiQ.setInt(1, userId);
            ResultSet bmiRs = bmiQ.executeQuery();
            if (bmiRs.next()) {
                latestBMI = bmiRs.getDouble("bmi");
                latestBMICategory = bmiRs.getString("bmi_category");
            }

            sb.append("Profile: ").append(gender).append(", Age ").append(age).append("\n");
            sb.append("Current Weight: ").append(weight).append(" kg, Height: ").append(height).append(" cm\n");
            sb.append("BMI: ").append(latestBMI).append(" (").append(latestBMICategory).append(")\n");
            sb.append("Goal Steps This Week: ").append(stepsGoal).append("\n");
            sb.append("Steps Achieved This Week: ").append(weeklySteps).append("\n\n");
        } catch (Exception ex) {
            sb.append("Could not load profile: ").append(ex.getMessage());
        }
        return sb.toString();
    }

    private String getAISuggestionGemini(String userContext) {
    String apiKey = System.getenv("GEMINI_API_KEY");
// Replace with your Gemini AI API key!
    String geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey;
    String prompt = "Write a highly motivational, 2-sentence health and fitness suggestion for this user: " + userContext;

    try {
        org.json.JSONObject payload = new org.json.JSONObject();
        org.json.JSONArray contentsArr = new org.json.JSONArray();
        org.json.JSONObject content = new org.json.JSONObject();
        org.json.JSONArray partsArr = new org.json.JSONArray();
        org.json.JSONObject part = new org.json.JSONObject();

        part.put("text", prompt);
        partsArr.put(part);
        content.put("parts", partsArr);
        contentsArr.put(content);
        payload.put("contents", contentsArr);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(geminiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        System.out.println("Gemini API raw response: " + responseBody);

        org.json.JSONObject resObj = new org.json.JSONObject(responseBody);
        if (resObj.has("candidates")) {
            String suggestion = resObj.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .optString("text", "No suggestion found.");
            return suggestion.trim();
        }
        if (resObj.has("error")) {
            return "Gemini API error: " + resObj.getJSONObject("error").optString("message");
        }
        return "No suggestion received from Gemini AI.";
    } catch (Exception e) {
        return "AI suggestions unavailable: " + e.getMessage();
    }
}

    }

