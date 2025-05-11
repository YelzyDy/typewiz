package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Node;

/**
 * Factory class for creating UI components.
 * This class follows the Factory Method pattern to centralize UI creation.
 */
public class UIFactory {
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
    private static final double UI_PANEL_OPACITY = 0.85;
    private static final double UI_BORDER_WIDTH = 2.0;
    
    // Performance colors
    private static final Color GOOD_PERFORMANCE = Color.GREEN;
    private static final Color MEDIUM_PERFORMANCE = Color.YELLOW;
    private static final Color POOR_PERFORMANCE = Color.RED;

    /**
     * Creates a health bar panel
     * @param initialHealth Initial health value
     * @param maxHealth Maximum health value
     * @return A VBox containing the health display
     */
    public static VBox createHealthDisplay(int initialHealth, int maxHealth) {
        VBox healthDisplay = new VBox(8);
        healthDisplay.setTranslateX(20);
        healthDisplay.setTranslateY(20);
        healthDisplay.setPadding(new Insets(10, 15, 10, 15));

        // Add background and styling to health panel
        healthDisplay.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(healthDisplay, UI_PRIMARY_COLOR, UI_CORNER_RADIUS);

        Text healthLabel = new Text("HEALTH");
        healthLabel.setFill(UI_PRIMARY_COLOR);
        healthLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        addTextGlow(healthLabel, UI_PRIMARY_COLOR, 0.4);

        Rectangle healthBar = new Rectangle(200, 20, getHealthColor(initialHealth, maxHealth));
        healthBar.setArcWidth(10);
        healthBar.setArcHeight(10);
        // Add drop shadow to health bar
        healthBar.setEffect(new DropShadow(5, Color.BLACK));

        // Add background for health bar
        Rectangle healthBarBg = new Rectangle(200, 20, Color.rgb(50, 50, 50, 0.6));
        healthBarBg.setArcWidth(10);
        healthBarBg.setArcHeight(10);

        // Change the health bar from middle to right to left
        Pane healthBarPane = new Pane();
        healthBarPane.setPrefSize(200, 20);
        healthBarPane.setMaxSize(200, 20);
        healthBarPane.getChildren().addAll(healthBarBg, healthBar);

        // Anchor health bar to the left
        healthBar.setTranslateX(0);
        healthBar.setTranslateY(0);

        Text healthText = new Text(initialHealth + "/" + maxHealth);
        healthText.setFill(UI_TEXT_PRIMARY);
        healthText.setFont(Font.font(FONT_FAMILY, 16));

        healthDisplay.getChildren().addAll(healthLabel, healthBarPane, healthText);
        healthDisplay.setUserData(new HealthDisplayData(healthBar, healthText, maxHealth));
        
        return healthDisplay;
    }
    
    /**
     * Updates the health bar with a new health value
     * @param healthDisplay The health display container
     * @param currentHealth Current health value
     */
    public static void updateHealthBar(VBox healthDisplay, int currentHealth) {
        if (healthDisplay == null || healthDisplay.getUserData() == null) return;
        
        HealthDisplayData data = (HealthDisplayData) healthDisplay.getUserData();
        double healthPercentage = (double) currentHealth / data.maxHealth;
        data.healthBar.setWidth(200 * healthPercentage);
        data.healthText.setText(currentHealth + "/" + data.maxHealth);

        // Update color based on health with smoother gradient
        data.healthBar.setFill(getHealthColor(currentHealth, data.maxHealth));
    }
    
    private static Color getHealthColor(int health, int maxHealth) {
        double healthPercentage = (double) health / maxHealth;
        if (healthPercentage > 0.6) {
            return Color.rgb(50, 220, 50); // Bright green
        } else if (healthPercentage > 0.3) {
            return Color.rgb(220, 220, 50); // Yellow
        } else {
            return Color.rgb(220, 50, 50); // Red
        }
    }
    
    /**
     * Creates a top bar with score and wave display
     * @param initialScore Initial score value
     * @param initialWave Initial wave value
     * @param maxWaves Maximum number of waves
     * @return An HBox containing the top bar
     */
    public static HBox createTopBar(int initialScore, int initialWave, int maxWaves) {
        HBox topBar = new HBox(20);
        topBar.setTranslateX(FXGL.getAppWidth() / 2 - 200);
        topBar.setTranslateY(20);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setAlignment(Pos.CENTER);

        // Add background and styling to top bar
        topBar.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(topBar, UI_ACCENT_COLOR, UI_CORNER_RADIUS);

        // Create score display with cool styling
        VBox scoreDisplay = new VBox(5);
        scoreDisplay.setPadding(new Insets(5, 10, 5, 10));
        scoreDisplay.setAlignment(Pos.CENTER);

        Text scoreLabel = new Text("SCORE");
        scoreLabel.setFill(UI_SECONDARY_COLOR);
        scoreLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        addTextGlow(scoreLabel, UI_SECONDARY_COLOR, 0.4);

        Text scoreText = new Text(Integer.toString(initialScore));
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        addTextGlow(scoreText, UI_SECONDARY_COLOR, 0.3);

        scoreDisplay.getChildren().addAll(scoreLabel, scoreText);
        scoreDisplay.setUserData(scoreText);

        // Create wave display with cool styling
        VBox waveDisplay = new VBox(5);
        waveDisplay.setPadding(new Insets(5, 10, 5, 10));
        waveDisplay.setAlignment(Pos.CENTER);

        Text waveLabel = new Text("WAVE");
        waveLabel.setFill(UI_ACCENT_COLOR);
        waveLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        addTextGlow(waveLabel, UI_ACCENT_COLOR, 0.4);

        Text waveText = new Text(initialWave + "/" + maxWaves);
        waveText.setFill(Color.WHITE);
        waveText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        addTextGlow(waveText, UI_ACCENT_COLOR, 0.3);

        waveDisplay.getChildren().addAll(waveLabel, waveText);
        waveDisplay.setUserData(waveText);

        // Add score and wave displays to top bar
        topBar.getChildren().addAll(scoreDisplay, waveDisplay);
        
        // Store both texts in the userdata
        topBar.setUserData(new TopBarData(scoreText, waveText, maxWaves));
        
        return topBar;
    }
    
    /**
     * Updates the score display
     * @param topBar The top bar container
     * @param score New score value
     */
    public static void updateScore(HBox topBar, int score) {
        if (topBar == null || topBar.getUserData() == null) return;
        
        TopBarData data = (TopBarData) topBar.getUserData();
        data.scoreText.setText(Integer.toString(score));
    }
    
    /**
     * Updates the wave display
     * @param topBar The top bar container
     * @param wave New wave value
     */
    public static void updateWave(HBox topBar, int wave) {
        if (topBar == null || topBar.getUserData() == null) return;
        
        TopBarData data = (TopBarData) topBar.getUserData();
        data.waveText.setText(wave + "/" + data.maxWaves);
    }
    
    /**
     * Creates an instruction text display
     * @return A Text node for instructions
     */
    public static Text createInstructionText() {
        Text instructionText = new Text("");
        instructionText.setTranslateX(FXGL.getAppWidth() / 2 - 150);
        instructionText.setTranslateY(FXGL.getAppHeight() / 2);
        instructionText.setFill(Color.YELLOW);
        instructionText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        instructionText.setVisible(false);
        addTextGlow(instructionText, Color.ORANGE, 0.6);
        
        return instructionText;
    }
    
    /**
     * Creates a controls help text display
     * @return A Text node for controls help
     */
    public static Text createControlsText() {
        Text controlsText = new Text("Controls: Type words | SHIFT to switch targets | SPACE to destroy word");
        controlsText.setTranslateX(FXGL.getAppWidth() / 2 - 240);
        controlsText.setTranslateY(FXGL.getAppHeight() - 20);
        controlsText.setFill(UI_TEXT_SECONDARY);
        controlsText.setFont(Font.font(FONT_FAMILY, 16));
        addTextGlow(controlsText, Color.WHITE, 0.2);
        
        return controlsText;
    }
    
    /**
     * Creates a stylish button
     * @param text Button text
     * @param width Button width
     * @param height Button height
     * @param color Button color
     * @return A StackPane containing the button
     */
    public static StackPane createStylishButton(String text, double width, double height, Color color) {
        // Create button background with rounded corners
        Rectangle buttonBg = new Rectangle(width, height);
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setFill(Color.rgb(60, 60, 80, 0.8));
        buttonBg.setStroke(color);
        buttonBg.setStrokeWidth(3);

        // Add glow effect to button
        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(color);
        buttonGlow.setRadius(15);
        buttonGlow.setSpread(0.2);
        buttonBg.setEffect(buttonGlow);

        // Create button text
        Text buttonText = new Text(text);
        buttonText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        buttonText.setFill(Color.WHITE);

        // Add glow to text
        Glow textGlow = new Glow(0.6);
        buttonText.setEffect(textGlow);

        // Stack text on background
        StackPane button = new StackPane(buttonBg, buttonText);
        
        // Make button responsive and interactive
        button.setCursor(javafx.scene.Cursor.HAND); // Change cursor on hover
        
        // Store original values for animations
        Color originalFill = (Color) buttonBg.getFill();
        Color originalStroke = (Color) buttonBg.getStroke();
        double originalRadius = buttonGlow.getRadius();
        double originalSpread = buttonGlow.getSpread();
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            // Brighten background
            buttonBg.setFill(Color.rgb(80, 80, 100, 0.9));
            // Increase glow
            buttonGlow.setRadius(25);
            buttonGlow.setSpread(0.4);
            // Scale button slightly
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        // Reset on mouse exit
        button.setOnMouseExited(e -> {
            buttonBg.setFill(originalFill);
            buttonGlow.setRadius(originalRadius);
            buttonGlow.setSpread(originalSpread);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        // Add click effect
        button.setOnMousePressed(e -> {
            // Darken background
            buttonBg.setFill(Color.rgb(40, 40, 60, 0.9));
            // Change stroke color
            buttonBg.setStroke(Color.WHITE);
            // Scale button down
            button.setScaleX(0.95);
            button.setScaleY(0.95);
            // Add vibration effect for feedback
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(50), button);
            tt.setByX(3);
            tt.setByY(0);
            tt.setCycleCount(2);
            tt.setAutoReverse(true);
            tt.play();
        });
        
        // Reset on release
        button.setOnMouseReleased(e -> {
            buttonBg.setFill(originalFill);
            buttonBg.setStroke(originalStroke);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        return button;
    }

    /**
     * Creates a performance monitoring display
     * @return A VBox containing the performance display
     */
    public static VBox createPerformanceDisplay() {
        VBox performanceDisplay = new VBox(5);
        performanceDisplay.setTranslateX(FXGL.getAppWidth() - 200);
        performanceDisplay.setTranslateY(20);

        Text performanceLabel = new Text("Performance:");
        performanceLabel.setFill(Color.WHITE);
        performanceLabel.setFont(Font.font(16));

        Rectangle performanceBar = new Rectangle(150, 10, GOOD_PERFORMANCE);

        Text performanceText = new Text("FPS: 144");
        performanceText.setFill(Color.WHITE);
        performanceText.setFont(Font.font(14));

        performanceDisplay.getChildren().addAll(performanceLabel, performanceBar, performanceText);
        performanceDisplay.setUserData(new PerformanceData(performanceBar, performanceText));
        
        return performanceDisplay;
    }
    
    /**
     * Updates the performance display
     * @param performanceDisplay The performance display container
     * @param fps Current FPS value
     */
    public static void updatePerformanceDisplay(VBox performanceDisplay, double fps) {
        if (performanceDisplay == null || performanceDisplay.getUserData() == null) return;
        
        PerformanceData data = (PerformanceData) performanceDisplay.getUserData();
        final double TARGET_FPS = 60.0;
        
        // Update FPS text
        data.performanceText.setText(String.format("FPS: %.1f", fps));

        // Calculate performance percentage
        double performancePercentage = Math.min(fps / TARGET_FPS, 1.0);

        // Update performance bar
        data.performanceBar.setWidth(150 * performancePercentage);

        // Update performance bar color
        if (performancePercentage >= 0.9) {
            data.performanceBar.setFill(GOOD_PERFORMANCE);
        } else if (performancePercentage >= 0.7) {
            data.performanceBar.setFill(MEDIUM_PERFORMANCE);
        } else {
            data.performanceBar.setFill(POOR_PERFORMANCE);
        }
    }
    
    // Helper methods for UI styling
    public static Background createPanelBackground(Color color, double cornerRadius) {
        return new Background(
                new BackgroundFill(
                        color,
                        new CornerRadii(cornerRadius),
                        Insets.EMPTY
                )
        );
    }

    public static void addPanelBorder(Region panel, Color color, double cornerRadius) {
        panel.setBorder(new Border(
                new BorderStroke(
                        color,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(cornerRadius),
                        new BorderWidths(UI_BORDER_WIDTH)
                )
        ));

        // Add a subtle glow around the panel
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        glow.setSpread(0.4);
        panel.setEffect(glow);
    }

    public static void addTextGlow(Text text, Color color, double intensity) {
        Glow glow = new Glow(intensity);
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        text.setEffect(shadow);
    }
    
    // Inner classes to store UI component references
    private static class HealthDisplayData {
        final Rectangle healthBar;
        final Text healthText;
        final int maxHealth;
        
        HealthDisplayData(Rectangle healthBar, Text healthText, int maxHealth) {
            this.healthBar = healthBar;
            this.healthText = healthText;
            this.maxHealth = maxHealth;
        }
    }
    
    private static class TopBarData {
        final Text scoreText;
        final Text waveText;
        final int maxWaves;
        
        TopBarData(Text scoreText, Text waveText, int maxWaves) {
            this.scoreText = scoreText;
            this.waveText = waveText;
            this.maxWaves = maxWaves;
        }
    }
    
    private static class PerformanceData {
        final Rectangle performanceBar;
        final Text performanceText;
        
        PerformanceData(Rectangle performanceBar, Text performanceText) {
            this.performanceBar = performanceBar;
            this.performanceText = performanceText;
        }
    }

    /**
     * Creates the background for the game
     * @param width Screen width
     * @param height Screen height
     */
    public static void createBackground(int width, int height) {
        Image backgroundImage = FXGL.image("background-and-platforms/bg-winter_1280_720.png");
        Rectangle background = new Rectangle(width, height);
        background.setFill(new ImagePattern(backgroundImage));

        FXGL.entityBuilder()
            .at(0, 0)
            .view(background)
            .zIndex(-100)
            .buildAndAttach();
    }

    /**
     * Creates the platform for the game
     */
    public static void createPlatform() {
        Image platformImage = FXGL.image("background-and-platforms/hdlargerplatform-winter.png");
        ImageView platformView = new ImageView(platformImage);

        double platformWidth = 1450;
        double platformHeight = 820;
        platformView.setFitWidth(platformWidth);
        platformView.setFitHeight(platformHeight);
        platformView.setPreserveRatio(false);

        double platformOffsetY = 100;
        double platformOffsetX = (platformWidth - FXGL.getAppWidth()) / -2;

        FXGL.entityBuilder()
            .type(Game.EntityType.PLATFORM)
            .at(platformOffsetX, platformOffsetY)
            .view(platformView)
            .bbox(new HitBox(BoundingShape.box(platformWidth, platformHeight)))
            .with(new PhysicsComponent())
            .zIndex(10)
            .buildAndAttach();
    }

    /**
     * Creates the wizard character
     */
    public static void createWizard() {
        Image wizardImage = FXGL.image("wizard/wizard.png");

        int frameWidth = 500;
        int frameHeight = 500;

        AnimationChannel idleAnimation = new AnimationChannel(wizardImage,
                4,
                frameWidth, frameHeight,
                Duration.seconds(0.25),
                0, 3);

        AnimatedTexture wizardTexture = new AnimatedTexture(idleAnimation);
        wizardTexture.loop();

        double wizardScale = 0.30;

        FXGL.entityBuilder()
            .type(Game.EntityType.PLAYER)
            .at(-5, 50)
            .view(wizardTexture)
            .scale(wizardScale, wizardScale)
            .bbox(new HitBox(BoundingShape.box(frameWidth * wizardScale, frameHeight * wizardScale)))
            .zIndex(25)
            .buildAndAttach();
    }

    /**
     * Creates and sets up all UI elements
     * @param game Reference to the main Game class
     */
    public static void createUI(Game game) {
        // Get the player manager directly from the game
        PlayerManager playerManager = game.getPlayerManager();
        
        // Create health display with player's current health
        VBox healthDisplay = createHealthDisplay(
            playerManager.getHealth(),
            playerManager.getMaxHealth()
        );
        
        // Set health display reference directly
        playerManager.setHealthDisplay(healthDisplay);
        
        // Find and set the health text reference
        for (Node child : healthDisplay.getChildren()) {
            if (child instanceof Text) {
                Text text = (Text) child;
                if (text.getText().contains("/")) {
                    playerManager.setHealthText(text);
                    break;
                }
            }
        }
        
        // Create top bar with initial score and wave
        HBox topBar = createTopBar(playerManager.getScore(), 1, 10);
        
        // Find and set score text reference
        for (Node child : topBar.getChildren()) {
            if (child instanceof VBox) {
                VBox box = (VBox) child;
                if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Text) {
                    Text label = (Text) box.getChildren().get(0);
                    if (label.getText().equals("SCORE") && box.getChildren().size() > 1) {
                        playerManager.setScoreText((Text) box.getChildren().get(1));
                    }
                }
            }
        }
        
        // Create performance display
        VBox performanceDisplay = createPerformanceDisplay();
        
        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(topBar);
        FXGL.addUINode(performanceDisplay);
    }

    /**
     * Updates the performance display with the current FPS
     * @param tpf Time per frame
     */
    public static void updatePerformanceDisplay(double tpf) {
        // Calculate FPS
        double fps = 1.0 / Math.max(tpf, 0.0001);
        
        // Find and update the performance display
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                if (!vbox.getChildren().isEmpty() && vbox.getChildren().get(0) instanceof Text) {
                    Text titleText = (Text) vbox.getChildren().get(0);
                    if ("Performance:".equals(titleText.getText())) {
                        updatePerformanceDisplay(vbox, fps);
                        break;
                    }
                }
            }
        }
    }
} 