package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.SoundManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Parent;

import java.util.List;

/**
 * Factory class for creating game prompts, announcements, and screens.
 * This class follows the Factory Method pattern to centralize UI creation.
 */
public class GamePromptFactory {
    // UI theme colors
    private static final Color UI_PRIMARY_COLOR = Color.rgb(70, 130, 230);     // Cool blue
    private static final Color UI_SECONDARY_COLOR = Color.rgb(210, 60, 160);   // Magenta/purple
    private static final Color UI_ACCENT_COLOR = Color.rgb(255, 215, 0);       // Gold
    private static final Color UI_BG_COLOR = Color.rgb(30, 30, 50, 0.8);       // Dark blue/purple background
    private static final Color UI_TEXT_PRIMARY = Color.WHITE;
    private static final Color UI_TEXT_SECONDARY = Color.LIGHTGRAY;
    
    // UI style constants
    private static final String FONT_FAMILY = "Arial";
    private static final double UI_CORNER_RADIUS = 15;
    
    // Statistic colors
    private static final Color STAT_TITLE_COLOR = Color.GOLD;
    private static final Color STAT_VALUE_COLOR = Color.WHITE;
    private static final Color STAT_GOOD_COLOR = Color.LIMEGREEN;
    private static final Color STAT_MEDIUM_COLOR = Color.YELLOW;
    private static final Color STAT_POOR_COLOR = Color.RED;
    private static final Color GRAPH_LINE_COLOR = Color.DEEPSKYBLUE;
    private static final Color GRAPH_BACKGROUND_COLOR = Color.rgb(20, 20, 50, 0.7);

    /**
     * Creates a wave announcement overlay
     * @param currentWave Current wave number
     * @param maxWaves Maximum number of waves
     * @return Node containing the announcement overlay
     */
    public static Node createWaveAnnouncement(int currentWave, int maxWaves) {

        SoundManager.getInstance().playWaveAnnounce();

        // Create semi-transparent overlay with a gradient effect
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(60, 20, 120, 0.7)),
                new Stop(1, Color.rgb(20, 30, 70, 0.7))
        );
        overlay.setFill(gradient);

        // Create stylish wave announcement panel
        Rectangle announcementPanel = new Rectangle(500, 250);
        announcementPanel.setArcWidth(30);
        announcementPanel.setArcHeight(30);
        announcementPanel.setFill(UI_BG_COLOR);
        announcementPanel.setStroke(UI_ACCENT_COLOR);
        announcementPanel.setStrokeWidth(3);

        // Add glow effect to the panel
        DropShadow panelGlow = new DropShadow();
        panelGlow.setColor(UI_ACCENT_COLOR);
        panelGlow.setRadius(20);
        panelGlow.setSpread(0.2);
        announcementPanel.setEffect(panelGlow);

        // Create wave announcement text with exciting styling
        Text waveText = new Text("WAVE " + currentWave);
        waveText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 64));
        waveText.setFill(UI_ACCENT_COLOR);

        // Add text glow and effects
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 150, 0));
        textShadow.setRadius(15);
        textShadow.setSpread(0.5);
        waveText.setEffect(textShadow);

        Text ofText = new Text("OF " + maxWaves);
        ofText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 32));
        ofText.setFill(Color.WHITE);
        ofText.setTranslateY(10); // Adjust vertical position

        // Create difficulty text
        String difficultyLevel;
        Color difficultyColor;
        if (currentWave <= 3) {
            difficultyLevel = "EASY";
            difficultyColor = Color.rgb(60, 220, 60); // Green
        } else if (currentWave <= 6) {
            difficultyLevel = "MEDIUM";
            difficultyColor = Color.rgb(220, 220, 60); // Yellow
        } else if (currentWave <= 9) {
            difficultyLevel = "HARD";
            difficultyColor = Color.rgb(220, 100, 60); // Orange
        } else {
            difficultyLevel = "BOSS WAVE";
            difficultyColor = Color.rgb(220, 60, 60); // Red
        }

        Text difficultyText = new Text(difficultyLevel);
        difficultyText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 46));
        difficultyText.setFill(difficultyColor);

        // Add glow effect to difficulty text
        Glow difficultyGlow = new Glow(0.8);
        DropShadow difficultyTextShadow = new DropShadow();
        difficultyTextShadow.setColor(difficultyColor);
        difficultyTextShadow.setRadius(10);
        difficultyTextShadow.setInput(difficultyGlow);
        difficultyText.setEffect(difficultyTextShadow);

        // Create subtitle text
        Text subtitleText = new Text("Get ready to type!");
        subtitleText.setFont(Font.font(FONT_FAMILY, 24));
        subtitleText.setFill(UI_TEXT_SECONDARY);
        subtitleText.setTranslateY(30);

        // Create horizontal box for wave and "of max" text
        HBox waveNumBox = new HBox(10, waveText, ofText);
        waveNumBox.setAlignment(Pos.CENTER);

        // Create layout for announcement
        VBox announcementLayout = new VBox(15);
        announcementLayout.setAlignment(Pos.CENTER);
        announcementLayout.getChildren().addAll(waveNumBox, difficultyText, subtitleText);

        // Combine panel and content
        StackPane announcementPane = new StackPane(announcementPanel, announcementLayout);

        // Add scale-up animation for the panel
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.3), announcementPane);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.play();
        
        // Return the full view
        return new StackPane(overlay, announcementPane);
    }
    
    /**
     * Creates a game over screen
     * @param message Message to display
     * @param score Final score
     * @param wpm Words per minute
     * @param accuracy Typing accuracy
     * @param consistency Typing consistency
     * @param wpmData WPM data over time
     * @param accuracyData Accuracy data over time
     * @return Node containing the game over screen
     */
    public static Node createGameOverScreen(String message, int score, double wpm, 
                                           double rawWpm, double accuracy, double consistency,
                                           List<Double> wpmData, List<Double> accuracyData) {

        // Play appropriate sound
        if (message.contains("Victory")) {
            SoundManager.getInstance().playVictory();
        } else {
            SoundManager.getInstance().playGameOver();
        }
        // Create full-screen semi-transparent overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setFill(Color.rgb(0, 0, 0, 0.85));

        // Create title with glow effect
        Text titleText = new Text(message);
        titleText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 64));
        
        // Set color based on message
        if (message.contains("Victory") || message.contains("Complete")) {
            titleText.setFill(Color.GOLD);
            
            // Add stronger glow for victory
            Glow glow = new Glow(0.8);
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.ORANGE);
            shadow.setRadius(25);
            shadow.setSpread(0.5);
            shadow.setInput(glow);
            titleText.setEffect(shadow);
        } else {
            titleText.setFill(Color.TOMATO);
            
            // Add glow for game over
            Glow glow = new Glow(0.6);
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.DARKRED);
            shadow.setRadius(15);
            shadow.setInput(glow);
            titleText.setEffect(shadow);
        }

        // Create title box
        HBox titleBox = new HBox(titleText);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));

        // Create score text
        Text scoreText = new Text("Final Score: " + score);
        scoreText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 36));
        scoreText.setFill(Color.WHITE);
        
        // Add glow to score
        Glow scoreGlow = new Glow(0.5);
        DropShadow scoreShadow = new DropShadow();
        scoreShadow.setColor(UI_ACCENT_COLOR);
        scoreShadow.setRadius(10);
        scoreShadow.setInput(scoreGlow);
        scoreText.setEffect(scoreShadow);

        // Create stats panel using StatsUIFactory
        VBox statsPanel = StatsUIFactory.createStatsPanel(wpm, rawWpm, accuracy, consistency);
        
        // Update stats data in factory
        StatsUIFactory.setWpmData(wpmData);
        StatsUIFactory.setAccuracyData(accuracyData);
        
        // Create graph using StatsUIFactory
        javafx.scene.canvas.Canvas graphCanvas = StatsUIFactory.createTypingGraph();
        
        // Create restart button using UIFactory
        StackPane restartButton = UIFactory.createStylishButton("Play Again", 200, 60, UI_PRIMARY_COLOR);
        restartButton.setId("play-again-button"); // Add a unique ID to the button for easy identification
        
        // Create two columns for stats layout
        HBox gameStatsLayout = new HBox(20);
        gameStatsLayout.setAlignment(Pos.CENTER);
        gameStatsLayout.setPadding(new Insets(20));

        // Left column (stats)
        VBox leftColumn = new VBox(15, scoreText, statsPanel);
        leftColumn.setAlignment(Pos.CENTER);
        leftColumn.setPadding(new Insets(10));

        Rectangle leftBg = new Rectangle(420, 340);
        leftBg.setArcWidth(UI_CORNER_RADIUS);
        leftBg.setArcHeight(UI_CORNER_RADIUS);
        leftBg.setFill(Color.rgb(0, 40, 80, 0.75));
        leftBg.setStroke(Color.web("#1E90FF"));
        leftBg.setStrokeWidth(2);

        // Right column (graph)
        VBox rightColumn = new VBox(15, graphCanvas);
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPadding(new Insets(10));

        Rectangle rightBg = new Rectangle(420, 340);
        rightBg.setArcWidth(UI_CORNER_RADIUS);
        rightBg.setArcHeight(UI_CORNER_RADIUS);
        rightBg.setFill(Color.rgb(50, 0, 80, 0.75));
        rightBg.setStroke(Color.web("#E1C16E"));
        rightBg.setStrokeWidth(2);

        StackPane leftStack = new StackPane(leftBg, leftColumn);
        StackPane rightStack = new StackPane(rightBg, rightColumn);

        gameStatsLayout.getChildren().addAll(leftStack, rightStack);

        VBox fullLayout = new VBox(15, titleBox, gameStatsLayout, restartButton);
        fullLayout.setAlignment(Pos.CENTER);

        // Return the full view
        return new StackPane(overlay, fullLayout);
    }
    
    /**
     * Creates a fade-in text instruction
     * @param text The text to display
     * @param duration Duration to display in seconds
     * @return Node containing the instruction overlay
     */
    public static Node createInstructionOverlay(String text, double duration) {
        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setFill(Color.rgb(0, 0, 0, 0.4));
        
        // Create instruction text
        Text instructionText = new Text(text);
        instructionText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 36));
        instructionText.setFill(Color.YELLOW);
        
        // Add glow to text
        Glow glow = new Glow(0.6);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.ORANGE);
        shadow.setRadius(10);
        shadow.setInput(glow);
        instructionText.setEffect(shadow);
        
        // Create container for text
        StackPane textContainer = new StackPane(instructionText);
        textContainer.setPadding(new Insets(20));
        
        // Return the full view
        return new StackPane(overlay, textContainer);
    }

    /**
     * Finds and sets up the Play Again button in an end game screen
     * @param endGameScreen The game over or victory screen node
     * @param restartAction Action to execute when the button is clicked
     */
    public static void setupPlayAgainButton(Node endGameScreen, Runnable restartAction) {
        // Search for the button in the hierarchy
        if (endGameScreen instanceof StackPane) {
            findButtonInChildren(endGameScreen, restartAction);
        }
    }

    /**
     * Recursively searches for the Play Again button in the node hierarchy
     * @param node The current node to search in
     * @param restartAction Action to execute when the button is clicked
     * @return true if button was found and configured
     */
    private static boolean findButtonInChildren(Node node, Runnable restartAction) {
        // Base case: found a StackPane that contains "Play Again" text
        if (node instanceof StackPane) {
            StackPane stackPane = (StackPane) node;
            
            // Search for the text node
            for (Node child : stackPane.getChildren()) {
                if (child instanceof Text) {
                    Text text = (Text) child;
                    if (text.getText().equals("Play Again")) {
                        // Assign an ID to the button for easier identification
                        stackPane.setId("play-again-button");
                        
                        // This is the button, add click handler with event consumption
                        stackPane.setOnMouseClicked(event -> {
                            event.consume(); // Prevent event bubbling
                            
                            // Schedule restart on next frame to avoid concurrent modification
                            FXGL.getGameTimer().runOnceAfter(() -> {
                                restartAction.run();
                            }, javafx.util.Duration.millis(100));
                        });
                        return true;
                    }
                }
            }
        }
        
        // Recursively search in children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (findButtonInChildren(child, restartAction)) {
                    return true;
                }
            }
        }
        
        return false;
    }
} 