package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StatsUIFactory {
    private static int totalCharactersTyped = 0;

    // Add typing statistics tracking
    private static int totalKeystrokes = 0;
    private static int correctKeystrokes = 0;
    private static int incorrectKeystrokes = 0;
    private static int totalWords = 0;
    private static long typingStartTime = 0;
    private static long totalTypingTime = 0;
    private static final int STATS_UPDATE_INTERVAL = 5000; // 5 seconds
    private static long lastStatsUpdate = 0;

    // Keep track of all the consistency of typing (time between keystrokes)
    private static List<Long> keystrokeTimings = new ArrayList<>();
    private static long lastKeystrokeTime = 0;
    // Make these fields static so they can be accessed in static methods
    private static List<Double> wpmOverTime = new ArrayList<>();
    private static List<Double> accuracyOverTime = new ArrayList<>();

    // Add UI theme colors for a cooler look
    private static final Color UI_PRIMARY_COLOR = Color.rgb(70, 130, 230);     // Cool blue
    private static final Color UI_BG_COLOR = Color.rgb(30, 30, 50, 0.8);       // Dark blue/purple background

    private static final Color GOOD_PERFORMANCE = Color.GREEN;
    private static final Color MEDIUM_PERFORMANCE = Color.YELLOW;
    private static final Color POOR_PERFORMANCE = Color.RED;
    private static double fps = 0.0;
    private static Text performanceText;
    private static Rectangle performanceBar;
    private static final double TARGET_FPS = 60.0;

    private static final String FONT_FAMILY = "Arial";
    private static final double UI_CORNER_RADIUS = 15;
    private static final Color STAT_TITLE_COLOR = Color.GOLD;
    private static final Color STAT_VALUE_COLOR = Color.WHITE;
    private static final Color STAT_GOOD_COLOR = Color.LIMEGREEN;
    private static final Color STAT_MEDIUM_COLOR = Color.YELLOW;
    private static final Color STAT_POOR_COLOR = Color.RED;
    private static final Color GRAPH_LINE_COLOR = Color.DEEPSKYBLUE;
    private static final Color GRAPH_BACKGROUND_COLOR = Color.rgb(20, 20, 50, 0.7);

    // Add methods to update factory data from Game
    public static void setTotalCharactersTyped(int count) {
        totalCharactersTyped = count;
        System.out.println("StatsUIFactory: Set total characters typed to " + count);
    }
    
    public static void setWpmData(List<Double> data) {
        wpmOverTime.clear();
        if (data != null) {
            wpmOverTime.addAll(data);
            System.out.println("StatsUIFactory: Set WPM data with " + data.size() + " entries");
        }
    }
    
    public static void setAccuracyData(List<Double> data) {
        accuracyOverTime.clear();
        if (data != null) {
            accuracyOverTime.addAll(data);
            System.out.println("StatsUIFactory: Set accuracy data with " + data.size() + " entries");
        }
    }

    public static VBox createStatsPanel(double wpm, double rawWpm, double accuracy, double consistency) {
        // Create stylish stats panel
        VBox statsPanel = new VBox(10);
        statsPanel.setPadding(new Insets(30));
        statsPanel.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));

        // Create section title
        Text statsTitle = new Text("TYPING STATISTICS");
        statsTitle.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 22));
        statsTitle.setFill(STAT_TITLE_COLOR);
        addTextGlow(statsTitle, STAT_TITLE_COLOR, 0.5);

        // Create grid for stats
        javafx.scene.layout.GridPane statsGrid = new javafx.scene.layout.GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(10);

        // WPM stat
        Text wpmLabel = new Text("WPM:");
        wpmLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        wpmLabel.setFill(STAT_TITLE_COLOR);

        Text wpmValue = new Text(String.format("%.1f", wpm));
        wpmValue.setFont(Font.font(FONT_FAMILY, 18));
        wpmValue.setFill(getColorForWPM(wpm));

        // Raw WPM stat
        Text rawWpmLabel = new Text("Raw WPM:");
        rawWpmLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        rawWpmLabel.setFill(STAT_TITLE_COLOR);

        Text rawWpmValue = new Text(String.format("%.1f", rawWpm));
        rawWpmValue.setFont(Font.font(FONT_FAMILY, 18));
        rawWpmValue.setFill(STAT_VALUE_COLOR);

        // Accuracy stat
        Text accuracyLabel = new Text("Accuracy:");
        accuracyLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        accuracyLabel.setFill(STAT_TITLE_COLOR);

        Text accuracyValue = new Text(String.format("%.1f%%", accuracy));
        accuracyValue.setFont(Font.font(FONT_FAMILY, 18));
        accuracyValue.setFill(getColorForAccuracy(accuracy));

        // Consistency stat
        Text consistencyLabel = new Text("Consistency:");
        consistencyLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        consistencyLabel.setFill(STAT_TITLE_COLOR);

        Text consistencyValue = new Text(String.format("%.1f%%", consistency));
        consistencyValue.setFont(Font.font(FONT_FAMILY, 18));
        consistencyValue.setFill(getColorForConsistency(consistency));
        
        // Add a small info text below consistency to explain
        Text consistencyInfo = new Text("(Develops over multiple games)");
        consistencyInfo.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.NORMAL, 12));
        consistencyInfo.setFill(Color.LIGHTGRAY);

        // Character count
        Text charCountLabel = new Text("Characters Typed:");
        charCountLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        charCountLabel.setFill(STAT_TITLE_COLOR);

        Text charCountValue = new Text(Integer.toString(totalCharactersTyped));
        charCountValue.setFont(Font.font(FONT_FAMILY, 18));
        charCountValue.setFill(STAT_VALUE_COLOR);

        // Add all stats to grid
        statsGrid.add(wpmLabel, 0, 0);
        statsGrid.add(wpmValue, 1, 0);
        statsGrid.add(rawWpmLabel, 0, 1);
        statsGrid.add(rawWpmValue, 1, 1);
        statsGrid.add(accuracyLabel, 0, 2);
        statsGrid.add(accuracyValue, 1, 2);
        statsGrid.add(consistencyLabel, 0, 3);
        statsGrid.add(consistencyValue, 1, 3);
        statsGrid.add(consistencyInfo, 1, 4);
        statsGrid.add(charCountLabel, 0, 5);
        statsGrid.add(charCountValue, 1, 5);

        // Add all elements to panel
        statsPanel.getChildren().addAll(statsTitle, statsGrid);

        return statsPanel;
    }

    // Make these methods static so they can be used in static context
    public static javafx.scene.layout.Background createPanelBackground(Color color, double cornerRadius) {
        return new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        color,
                        new javafx.scene.layout.CornerRadii(cornerRadius),
                        javafx.geometry.Insets.EMPTY
                )
        );
    }

    public static void addTextGlow(Text text, Color color, double intensity) {
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(intensity);
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        text.setEffect(shadow);
    }


    public static javafx.scene.canvas.Canvas createTypingGraph() {
        // Create canvas for graph
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(400, 300);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fill background
        gc.setFill(GRAPH_BACKGROUND_COLOR);
        gc.fillRect(0, 0, 400, 300);

        // Draw border
        gc.setStroke(UI_PRIMARY_COLOR);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, 400, 300);

        // Check if we have data to display
        if (wpmOverTime.isEmpty() || accuracyOverTime.isEmpty()) {
            // No data, draw placeholder text
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(FONT_FAMILY, 16));
            gc.fillText("No typing data available", 120, 150);
            return canvas;
        }

        // Draw title
        gc.setFill(STAT_TITLE_COLOR);
        gc.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        gc.fillText("Typing Performance Over Time", 80, 30);

        // Draw axis labels
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(FONT_FAMILY, 14));
        gc.fillText("Time", 185, 290);

        // Calculate max WPM for scaling
        double maxWPM = 0;
        for (Double wpm : wpmOverTime) {
            maxWPM = Math.max(maxWPM, wpm);
        }
        maxWPM = Math.max(maxWPM, 100); // Minimum scale of 100 WPM

        // Calculate padding and scale
        int padding = 40;
        int graphWidth = 400 - padding * 2;
        int graphHeight = 220;

        // Draw y-axis labels
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(FONT_FAMILY, 12));
        gc.fillText("0", 25, 260);
        gc.fillText(String.format("%.0f", maxWPM / 2), 15, 190);
        gc.fillText(String.format("%.0f", maxWPM), 15, 120);

        // Draw WPM label with rotation
        gc.save();
        gc.translate(20, 180);
        gc.rotate(-90);
        gc.setFill(GRAPH_LINE_COLOR);
        gc.fillText("WPM", 0, 0);
        gc.restore();

        // Draw accuracy label with rotation
        gc.save();
        gc.translate(20, 130);
        gc.rotate(-90);
        gc.setFill(STAT_GOOD_COLOR);
        gc.fillText("Accuracy %", 0, 0);
        gc.restore();

        // Draw horizontal grid lines
        gc.setStroke(Color.rgb(100, 100, 100, 0.5));
        gc.setLineWidth(1);
        gc.setLineDashes(3, 3);
        gc.strokeLine(padding, 260 - 0 * graphHeight / maxWPM, 400 - padding, 260 - 0 * graphHeight / maxWPM);
        gc.strokeLine(padding, 260 - 0.5 * graphHeight / maxWPM * maxWPM, 400 - padding, 260 - 0.5 * graphHeight / maxWPM * maxWPM);
        gc.strokeLine(padding, 260 - 1.0 * graphHeight / maxWPM * maxWPM, 400 - padding, 260 - 1.0 * graphHeight / maxWPM * maxWPM);
        gc.setLineDashes(0);

        // Draw WPM line
        gc.setStroke(GRAPH_LINE_COLOR);
        gc.setLineWidth(2);
        double xStep = (double) graphWidth / (wpmOverTime.size() - 1);

        gc.beginPath();
        for (int i = 0; i < wpmOverTime.size(); i++) {
            double wpm = wpmOverTime.get(i);
            double x = padding + i * xStep;
            double y = 260 - (wpm / maxWPM) * graphHeight;

            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();

        // Draw small circles at data points
        for (int i = 0; i < wpmOverTime.size(); i++) {
            double wpm = wpmOverTime.get(i);
            double x = padding + i * xStep;
            double y = 260 - (wpm / maxWPM) * graphHeight;

            gc.setFill(GRAPH_LINE_COLOR);
            gc.fillOval(x - 3, y - 3, 6, 6);
        }

        // Draw accuracy line if we have enough data points
        if (accuracyOverTime.size() > 1) {
            gc.setStroke(STAT_GOOD_COLOR);
            gc.setLineWidth(2);
            xStep = (double) graphWidth / (accuracyOverTime.size() - 1);

            gc.beginPath();
            for (int i = 0; i < accuracyOverTime.size(); i++) {
                double accuracy = accuracyOverTime.get(i);
                double x = padding + i * xStep;
                double y = 260 - (accuracy / 100.0) * graphHeight;

                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.stroke();

            // Draw small circles at data points
            for (int i = 0; i < accuracyOverTime.size(); i++) {
                double accuracy = accuracyOverTime.get(i);
                double x = padding + i * xStep;
                double y = 260 - (accuracy / 100.0) * graphHeight;

                gc.setFill(STAT_GOOD_COLOR);
                gc.fillOval(x - 3, y - 3, 6, 6);
            }
        }

        return canvas;
    }

    private static Color getColorForWPM(double wpm) {
        if (wpm >= 60) return STAT_GOOD_COLOR;
        if (wpm >= 40) return STAT_MEDIUM_COLOR;
        return STAT_POOR_COLOR;
    }

    private static Color getColorForAccuracy(double accuracy) {
        if (accuracy >= 95) return STAT_GOOD_COLOR;
        if (accuracy >= 85) return STAT_MEDIUM_COLOR;
        return STAT_POOR_COLOR;
    }

    private static Color getColorForConsistency(double consistency) {
        if (consistency >= 85) return STAT_GOOD_COLOR;
        if (consistency >= 70) return STAT_MEDIUM_COLOR;
        return STAT_POOR_COLOR;
    }

}
