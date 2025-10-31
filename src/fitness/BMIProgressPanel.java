package fitness;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;

public class BMIProgressPanel extends JPanel {
    private int userId;

    public BMIProgressPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("BMI Progress Over Time");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setForeground(new Color(25, 118, 210));
        add(titleLabel, BorderLayout.NORTH);

        TimeSeriesCollection dataset = createDataset();

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "BMI Progress Chart",
            "Date",
            "BMI",
            dataset,
            false, // legend off
            true,
            false
        );

        // Make chart more dynamic/glorious
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(245,250,255));
        plot.setDomainGridlinePaint(new Color(180, 200, 230));
        plot.setRangeGridlinePaint(new Color(180, 200, 230));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, new Color(76, 175, 80)); // green curve
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 7, 7));
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator(
            "{1}: {2}", new SimpleDateFormat("MMM dd"), new DecimalFormat("##.00")
        ));
        plot.setRenderer(renderer);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM dd"));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(new Color(232, 245, 233));
        add(chartPanel, BorderLayout.CENTER);

        JLabel motivationLabel = new JLabel();
        motivationLabel.setHorizontalAlignment(JLabel.CENTER);
        motivationLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        motivationLabel.setForeground(new Color(0, 150, 136));
        add(motivationLabel, BorderLayout.SOUTH);

        // Highlight message
        double minBMI = Double.MAX_VALUE, maxBMI = Double.MIN_VALUE, last = 0, first = 0;
        int count = dataset.getSeries(0).getItemCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                double v = dataset.getSeries(0).getValue(i).doubleValue();
                minBMI = Math.min(minBMI, v);
                maxBMI = Math.max(maxBMI, v);
                if (i == 0) first = v;
                if (i == count-1) last = v;
            }
            String msg = "";
            if (last < first)
                msg = "🔥 Excellent! BMI is improving—keep it up!";
            else if (last > first)
                msg = "⚡ BMI increased—focus on nutrition and exercise.";
            else
                msg = "👌 BMI is stable—great work maintaining!";
            motivationLabel.setText(String.format(
                "<html>Lowest BMI: <b>%.2f</b> &nbsp; | &nbsp; Highest BMI: <b>%.2f</b><br>%s</html>", minBMI, maxBMI, msg));
        } else {
            motivationLabel.setText("No BMI data yet. Use the BMI Calculator tab to log your BMI!");
        }
    }

    private TimeSeriesCollection createDataset() {
        TimeSeries bmiSeries = new TimeSeries("BMI");
        try (Connection con = DBConnector.connect()) {
            String sql = "SELECT date, bmi FROM activities WHERE user_id = ? AND bmi IS NOT NULL AND bmi > 0 ORDER BY date";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            HashSet<String> seenDates = new HashSet<>();
            while (rs.next()) {
                String dateStr = rs.getString("date");
                double bmi = rs.getDouble("bmi");
                if (!seenDates.contains(dateStr) && bmi > 0) {
                    String[] parts = dateStr.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    bmiSeries.add(new Day(day, month, year), bmi);
                    seenDates.add(dateStr);
                }
            }
        } catch (SQLException e) {
            // Silently ignore - show message at bottom instead
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(bmiSeries);
        return dataset;
    }
}