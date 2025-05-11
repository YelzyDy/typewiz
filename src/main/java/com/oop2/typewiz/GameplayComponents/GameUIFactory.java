package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameUIFactory {

    private LocalTimer blockSpawnTimer;
    private static final double BLOCK_SPEED = 60.0; // Base speed in pixels per second
    private static final double SPAWN_INTERVAL = 3.0; // seconds between block spawns
    private static final double ROW_SPACING = 140; // Spacing between rows
    private static final int NUM_ROWS = 5; // Total number of rows
    private static final int MAX_HEALTH = 100;
    private static final int HEALTH_LOSS_PER_MISS = 20;
    private static final double WAVE_PAUSE_TIME = 5.0; // 5 seconds pause between waves

    // Performance monitoring and optimization constants
    private static final double TARGET_FPS = 60.0;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS; // ~16.67ms per frame
    private static final double MAX_FRAME_TIME = 0.1; // Cap at 10 FPS minimum
    private static final int MAX_PHYSICS_STEPS = 3; // Reduced to prevent overshooting
    private static final int BATCH_SIZE = 50;
    private static final int PERFORMANCE_HISTORY_SIZE = 60;

    // Movement constants
    private static final double WAVE_SPEED_INCREMENT = 20.0; // Doubled speed increment
    private static final double MIN_MOVEMENT = 1.0; // Increased to prevent micro-stuttering

    private static int playerHealth = MAX_HEALTH;
    static Text healthText;
    static Rectangle healthBar;
    private WordBlockPool wordBlockPool;
    private Entity selectedWordBlock = null;
    private StringBuilder currentInput = new StringBuilder();
    private Text inputText;
    private boolean gameOver = false;
    private Entity gameOverScreen;
    private final Random random = new Random();

    // Color constants for word highlighting
    private static final Color SELECTED_COLOR = Color.LIME;
    private static final Color TYPED_COLOR = Color.DEEPSKYBLUE;
    private static final Color DEFAULT_COLOR = Color.WHITE;

    // Add UI theme colors for a cooler look
    private static final Color UI_PRIMARY_COLOR = Color.rgb(70, 130, 230);     // Cool blue
    private static final Color UI_SECONDARY_COLOR = Color.rgb(210, 60, 160);   // Magenta/purple
    private static final Color UI_ACCENT_COLOR = Color.rgb(255, 215, 0);       // Gold
    private static final Color UI_BG_COLOR = Color.rgb(30, 30, 50, 0.8);       // Dark blue/purple background
    private static final Color UI_TEXT_PRIMARY = Color.WHITE;
    private static final Color UI_TEXT_SECONDARY = Color.LIGHTGRAY;

    // Add UI style-related constants
    private static final String FONT_FAMILY = "Arial";
    private static final double UI_CORNER_RADIUS = 15;
    private static final double UI_PANEL_OPACITY = 0.85;
    private static final double UI_BORDER_WIDTH = 2.0;

    // Add score tracking
    private int score = 0;
    static Text scoreText;

    // Add wave tracking
    private int currentWave = 1;
    static Text waveText;
    private boolean waveCompleted = false;
    private LocalTimer waveTimer;
    private boolean waveInProgress = false;
    static Text instructionText;

    // Add max waves constant and completion tracking
    private static final int MAX_WAVES = 10;
    private boolean gameCompleted = false;

    // Wave difficulty progression parameters
    private static final int[] WAVE_SPAWNS_PER_WAVE = {
            10, 12, 15, 18, 20, 22, 25, 28, 30, 35 // Increasing spawns per wave
    };
    private static final double[] WAVE_SPEED_MULTIPLIERS = {
            1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.8, 2.0, 2.2 // Increasing speed per wave
    };
    private static final int[] MIN_SPAWNS_PER_GROUP_BY_WAVE = {
            1, 1, 2, 2, 2, 3, 3, 3, 4, 4 // Increasing minimum spawns per group
    };
    private static final int[] MAX_SPAWNS_PER_GROUP_BY_WAVE = {
            2, 3, 3, 4, 4, 5, 5, 6, 6, 7 // Increasing maximum spawns per group
    };
    private static final double[] SPAWN_DELAY_MULTIPLIERS = {
            1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.5 // Decreasing delay between groups
    };

    // Word list for typing
    private final List<String> wordList = Arrays.asList(
            "code", "java", "type", "game", "block",
            "winter", "wizard", "magic", "spell", "potion",
            "frost", "snow", "ice", "cold", "programming",
            "keyboard", "screen", "input", "output", "variable",
            "function", "class", "method", "array", "string"
    );

    // Add medium and hard word lists for difficulty progression
    private final List<String> mediumWordList = Arrays.asList(
            "variable", "function", "method", "algorithm", "interface",
            "inheritance", "polymorphism", "abstraction", "encapsulation", "iteration",
            "recursion", "exception", "debugging", "framework", "compiler",
            "library", "component", "parameter", "structure", "observer"
    );

    private final List<String> hardWordList = Arrays.asList(
            "synchronization", "multithreading", "serialization", "optimization", "implementation",
            "initialization", "authentication", "configuration", "virtualization", "documentation",
            "architecture", "dependency", "infrastructure", "persistence", "transaction",
            "asynchronous", "development", "integration", "management", "deployment"
    );

    // Add spatial partitioning and batch rendering
    private SpatialPartitioning spatialPartitioning;
    private BatchRenderer batchRenderer;

    // Frame timing variables
    private double accumulator = 0.0;
    private double lastFrameTime = 0.0;
    private double lastUpdateTime = 0.0;
    private int frameCount = 0;
    private double fps = 0.0;
    private double lastFpsUpdate = 0.0;
    private static final double FPS_UPDATE_INTERVAL = 0.5;
    private double alpha = 0.0; // Interpolation factor
    private static final double ALPHA_SMOOTHING = 0.1; // Smoothing factor for interpolation

    // Performance history tracking
    private final double[] frameTimeHistory = new double[PERFORMANCE_HISTORY_SIZE];
    private int historyIndex = 0;
    private Text performanceText;
    private Rectangle performanceBar;
    private static final Color GOOD_PERFORMANCE = Color.GREEN;
    private static final Color MEDIUM_PERFORMANCE = Color.YELLOW;
    private static final Color POOR_PERFORMANCE = Color.RED;

    // Object pooling for frequently created objects
    private final List<Entity> blocksToRemove = new ArrayList<>(BATCH_SIZE);
    private final List<Entity> activeBlocks = new ArrayList<>(BATCH_SIZE);
    private final List<Entity> newBlocks = new ArrayList<>(NUM_ROWS);

    // Add gargoyle pool and tracking
    private static final int MAX_GARGOYLES = 10;
    private static final int MAX_SIMULTANEOUS_GARGOYLES = 5;
    private List<Entity> gargoylePool = new ArrayList<>(MAX_GARGOYLES);
    private List<Entity> activeGargoyles = new ArrayList<>(MAX_GARGOYLES);
    private static final double GARGOYLE_SPEED = 50.0; // Doubled base speed
    private static final double WING_FLAP_SPEED = 0.2;
    private AnimationChannel gargoyleIdleAnimation;
    private AnimationChannel gargoyleFlyAnimation;
    private static final int GARGOYLE_FRAME_WIDTH = 288;
    private static final int GARGOYLE_FRAME_HEIGHT = 312;
    private static final double GARGOYLE_SCALE = 0.6;
    private static final double GARGOYLE_SPACING = 400;
    private static final double SCREEN_MARGIN = 150;

    // Word display constants
    private static final double WORD_FONT_SIZE = 40;
    private static final double WORD_HEIGHT = WORD_FONT_SIZE + 15; // Increased padding
    private static final double WORD_VERTICAL_OFFSET = 160;

    // Total height of a gargoyle including its word
    private static final double TOTAL_ENTITY_HEIGHT = (GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE) + Math.abs(WORD_VERTICAL_OFFSET) + WORD_HEIGHT;

    // Update spacing constants for strict enforcement including word heights
    private static final double MIN_VERTICAL_SPACING = TOTAL_ENTITY_HEIGHT * 0.3; // Drastically reduced to allow more crowding
    private static final double MIN_HORIZONTAL_SPACING = GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE * 1.5;
    private static final double SPAWN_BUFFER = 10;

    // Add animation state tracking
    private static final double ANIMATION_TRANSITION_TIME = 0.2; // 200ms transition
    private static final double ANIMATION_FRAME_TIME = 0.016; // 60 FPS
    private static final double ANIMATION_FADE_TIME = 0.1; // 100ms fade

    // Add wave spawning constants
    private static final double WAVE_SPAWN_DELAY = 7.0; // Reduced from 10.0 to 7.0 seconds between spawn groups
    private static final int MIN_SPAWNS_PER_GROUP = 2;
    private static final int MAX_SPAWNS_PER_GROUP = 3;
    private static final int MIN_SPAWNS_PER_WAVE = 10;
    private static final int MAX_SPAWNS_PER_WAVE = 15;
    private static final double SPAWN_SPEED_INCREASE = 0.9; // Adjusted from 0.85 to 0.9 (each group spawns 10% faster than the last)
    private LocalTimer waveSpawnTimer;
    private boolean isSpawningWave = false;
    private int currentGroupSize = 0;
    private int totalWaveSpawns = 0;
    private double currentSpawnDelay = WAVE_SPAWN_DELAY;
    private boolean spawnFromRight = true; // Toggle between right and left spawning
    private int currentSpawnCount = 0; // Add currentSpawnCount field

    // Update spawn constants
    private double spawnPerimeterRight;
    private double visibleThreshold;

    private boolean shouldShowWaveAnnouncement = true;
    private Entity waveAnnouncementOverlay;
    private LocalTimer announcementTimer;
    private static final double ANNOUNCEMENT_DURATION = 2.0; // seconds to show the announcement

    // Add typing statistics tracking
    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int incorrectKeystrokes = 0;
    private int totalCharactersTyped = 0;
    private int totalWords = 0;
    private long typingStartTime = 0;
    private long totalTypingTime = 0;
    private List<Double> wpmOverTime = new ArrayList<>();
    private List<Double> accuracyOverTime = new ArrayList<>();
    private static final int STATS_UPDATE_INTERVAL = 5000; // 5 seconds
    private long lastStatsUpdate = 0;

    // Keep track of all the consistency of typing (time between keystrokes)
    private List<Long> keystrokeTimings = new ArrayList<>();
    private long lastKeystrokeTime = 0;

    // Add color constants for statistics
    private static final Color STAT_TITLE_COLOR = Color.GOLD;
    private static final Color STAT_VALUE_COLOR = Color.WHITE;
    private static final Color STAT_GOOD_COLOR = Color.LIMEGREEN;
    private static final Color STAT_MEDIUM_COLOR = Color.YELLOW;
    private static final Color STAT_POOR_COLOR = Color.RED;
    private static final Color GRAPH_LINE_COLOR = Color.DEEPSKYBLUE;
    private static final Color GRAPH_BACKGROUND_COLOR = Color.rgb(20, 20, 50, 0.7);


    static void setupUI() {

        // Create health bar panel with cool styling
        VBox healthDisplay = new VBox(8);
        healthDisplay.setTranslateX(20);
        healthDisplay.setTranslateY(20);
        healthDisplay.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        // Add background and styling to health panel
        healthDisplay.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(healthDisplay, UI_PRIMARY_COLOR, UI_CORNER_RADIUS);

        Text healthLabel = new Text("HEALTH");
        healthLabel.setFill(UI_PRIMARY_COLOR);
        healthLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        addTextGlow(healthLabel, UI_PRIMARY_COLOR, 0.4);

        healthBar = new Rectangle(200, 20, Color.GREEN);
        healthBar.setArcWidth(10);
        healthBar.setArcHeight(10);
        // Add drop shadow to health bar
        healthBar.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));

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

        healthText = new Text(playerHealth + "/" + MAX_HEALTH);
        healthText.setFill(UI_TEXT_PRIMARY);
        healthText.setFont(Font.font(FONT_FAMILY, 16));

        healthDisplay.getChildren().addAll(healthLabel, healthBarPane, healthText);

        // Create top bar with score and wave info
        HBox topBar = new HBox(20);
        topBar.setTranslateX(FXGL.getAppWidth() / 2 - 200);
        topBar.setTranslateY(20);
        topBar.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        topBar.setAlignment(Pos.CENTER);

        // Add background and styling to top bar
        topBar.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(topBar, UI_ACCENT_COLOR, UI_CORNER_RADIUS);

        // Create score display with cool styling
        VBox scoreDisplay = new VBox(5);
        scoreDisplay.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        scoreDisplay.setAlignment(Pos.CENTER);

        Text scoreLabel = new Text("SCORE");
        scoreLabel.setFill(UI_SECONDARY_COLOR);
        scoreLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        addTextGlow(scoreLabel, UI_SECONDARY_COLOR, 0.4);

        scoreText = new Text("0");
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 28));
        addTextGlow(scoreText, UI_SECONDARY_COLOR, 0.3);

        scoreDisplay.getChildren().addAll(scoreLabel, scoreText);

        // Create wave display with cool styling
        VBox waveDisplay = new VBox(5);
        waveDisplay.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        waveDisplay.setAlignment(Pos.CENTER);

        Text waveLabel = new Text("WAVE");
        waveLabel.setFill(UI_ACCENT_COLOR);
        waveLabel.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 18));
        addTextGlow(waveLabel, UI_ACCENT_COLOR, 0.4);

        waveText = new Text("1/" + MAX_WAVES);
        waveText.setFill(Color.WHITE);
        waveText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 28));
        addTextGlow(waveText, UI_ACCENT_COLOR, 0.3);

        waveDisplay.getChildren().addAll(waveLabel, waveText);

        // Add score and wave displays to top bar
        topBar.getChildren().addAll(scoreDisplay, waveDisplay);

        // Create instruction text (invisible by default)
        instructionText = new Text("");
        instructionText.setTranslateX(FXGL.getAppWidth() / 2 - 150);
        instructionText.setTranslateY(FXGL.getAppHeight() / 2);
        instructionText.setFill(Color.YELLOW);
        instructionText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 28));
        instructionText.setVisible(false);
        addTextGlow(instructionText, Color.ORANGE, 0.6);

        // Add controls help text
        Text controlsText = new Text("Controls: Type words | SHIFT to switch targets | SPACE to destroy word");
        controlsText.setTranslateX(FXGL.getAppWidth() / 2 - 240);
        controlsText.setTranslateY(FXGL.getAppHeight() - 20);
        controlsText.setFill(UI_TEXT_SECONDARY);
        controlsText.setFont(Font.font(FONT_FAMILY, 16));
        addTextGlow(controlsText, Color.WHITE, 0.2);

        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(topBar);
        FXGL.addUINode(instructionText);
        FXGL.addUINode(controlsText);
    }

    // Helper methods for UI styling
    private static javafx.scene.layout.Background createPanelBackground(Color color, double cornerRadius) {
        return new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        color,
                        new javafx.scene.layout.CornerRadii(cornerRadius),
                        javafx.geometry.Insets.EMPTY
                )
        );
    }

    static void addPanelBorder(javafx.scene.layout.Region panel, Color color, double cornerRadius) {
        panel.setBorder(new javafx.scene.layout.Border(
                new javafx.scene.layout.BorderStroke(
                        color,
                        javafx.scene.layout.BorderStrokeStyle.SOLID,
                        new javafx.scene.layout.CornerRadii(cornerRadius),
                        new javafx.scene.layout.BorderWidths(UI_BORDER_WIDTH)
                )
        ));

        // Add a subtle glow around the panel
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        glow.setSpread(0.4);
        panel.setEffect(glow);
    }

    static void addTextGlow(Text text, Color color, double intensity) {
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(intensity);
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        text.setEffect(shadow);
    }
}
