package com.oop2.typewiz.GameplayComponents;

import com.oop2.typewiz.GameplayComponents.StatsUIFactory;
import com.oop2.typewiz.GameplayComponents.GargoyleFactory;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.Collections;

import static com.oop2.typewiz.GameplayComponents.StatsUIFactory.createStatsPanel;
import com.oop2.typewiz.GameplayComponents.GamePromptFactory;
import com.oop2.typewiz.GameplayComponents.UIFactory;

public class Game extends GameApplication {
    // Define EntityType enum inside the Game class
    public enum EntityType {
        PLATFORM,
        PLAYER,
        MOVING_BLOCK,
        GARGOYLE
    }

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

    private int playerHealth = MAX_HEALTH;
    private Text healthText;
    private Rectangle healthBar;
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
    private Text scoreText;

    // Add wave tracking
    private int currentWave = 1;
    private Text waveText;
    private boolean waveCompleted = false;
    private LocalTimer waveTimer;
    private boolean waveInProgress = false;
    private Text instructionText;

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

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("TypeWiz - Winter Theme");
        settings.setManualResizeEnabled(false);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(false);
        settings.setGameMenuEnabled(false);
        settings.setIntroEnabled(false);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(false);
    }

    @Override
    protected void initGame() {
        // Initialize spawn constants first
        spawnPerimeterRight = 100; // Distance from right edge where gargoyles spawn
        visibleThreshold = FXGL.getAppWidth() - spawnPerimeterRight; // Point where gargoyles become visible

        // Initialize wave spawn timer first
        waveSpawnTimer = FXGL.newLocalTimer();
        waveSpawnTimer.capture();

        // Initialize wave state
        currentSpawnCount = 0;
        isSpawningWave = false;
        currentGroupSize = 0;
        currentSpawnDelay = WAVE_SPAWN_DELAY;

        // Set up the winter background
        Image backgroundImage = FXGL.image("background-and-platforms/bg-winter_1280_720.png");
        Rectangle background = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        background.setFill(new ImagePattern(backgroundImage));

        Entity backgroundEntity = FXGL.entityBuilder()
                .at(0, 0)
                .view(background)
                .zIndex(-100) // To ensure it's behind everything else
                .buildAndAttach();

        // Set up the platform image with increased size
        Image platformImage = FXGL.image("background-and-platforms/hdlargerplatform-winter.png");
        ImageView platformView = new ImageView(platformImage);

        // Resize the platform to be slightly larger than the background
        double platformWidth = 1450; // Increased from 1280
        double platformHeight = 820; // Increased from 720
        platformView.setFitWidth(platformWidth);
        platformView.setFitHeight(platformHeight);
        platformView.setPreserveRatio(false); // Allow exact resizing

        // Position the platform a little lower and centered
        double platformOffsetY = 100; // Move it 100 pixels lower
        // Center the platform horizontally since it's now wider than the screen
        double platformOffsetX = (platformWidth - FXGL.getAppWidth()) / -2;

        Entity platformEntity = FXGL.entityBuilder()
                .type(EntityType.PLATFORM)
                .at(platformOffsetX, platformOffsetY) // Center horizontally and move down
                .view(platformView)
                .bbox(new HitBox(BoundingShape.box(platformWidth, platformHeight)))
                .with(new PhysicsComponent())
                .zIndex(10) // Higher z-index to be in front of background
                .buildAndAttach();

        // Add the wizard sprite with animation
        Image wizardImage = FXGL.image("wizard/wizard.png");

        // Each frame is 500x500 pixels (2000/4 = 500)
        int frameWidth = 500;
        int frameHeight = 500;

        // Create animation channel for idle animation (assuming first row is idle)
        // Wizard sheet is 4x4 frames, so we use the first row for idle
        AnimationChannel idleAnimation = new AnimationChannel(wizardImage,
                4, // 4 frames per row
                frameWidth, frameHeight,
                Duration.seconds(0.25), // 4 frames per second (matching 144 FPS)
                0, 3); // First row, frames 0-3

        // Create the animated texture
        AnimatedTexture wizardTexture = new AnimatedTexture(idleAnimation);
        wizardTexture.loop();

        // Scale the wizard to a proper size for the scene
        double wizardScale = 0.30; // Scale to 15% of original size

        // Create the wizard entity
        Entity wizardEntity = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(-5, 50) // Position directly on top of barrier
                .view(wizardTexture)
                .scale(wizardScale, wizardScale) // Scale down the wizard
                .bbox(new HitBox(BoundingShape.box(frameWidth * wizardScale, frameHeight * wizardScale)))
                .zIndex(25)
                .buildAndAttach();

        // Initialize word block pool with enough blocks for maximum concurrent blocks
        wordBlockPool = new WordBlockPool(NUM_ROWS * 2, 40); // Double the number of rows to ensure we have enough blocks

        // Initialize spatial partitioning with cell size of 100 pixels
        spatialPartitioning = new SpatialPartitioning(100, FXGL.getAppWidth(), FXGL.getAppHeight());

        // Initialize batch renderer
        batchRenderer = new BatchRenderer(spatialPartitioning);

        // Add performance monitoring UI
        setupPerformanceUI();

        // Initialize gargoyle animations
        GargoyleFactory.initializeAnimations();

        // Initialize gargoyle pool
        for (int i = 0; i < MAX_GARGOYLES; i++) {
            Entity gargoyleEntity = FXGL.entityBuilder()
                    .type(EntityType.GARGOYLE)
                    .zIndex(25)
                    .build();

            // Add to pool
            gargoylePool.add(gargoyleEntity);
        }

        // Set up UI elements
        setupUI();

        // Initialize timers
        blockSpawnTimer = FXGL.newLocalTimer();
        blockSpawnTimer.capture();

        waveTimer = FXGL.newLocalTimer();
        waveTimer.capture();

        // Set up input handling
        setupInput();

        // Show initial wave message and start first wave
        showWaveStartMessage();

        // Initialize announcement timer
        announcementTimer = FXGL.newLocalTimer();
        announcementTimer.capture();

        // Initialize typing statistics
        typingStartTime = System.currentTimeMillis();
        lastStatsUpdate = typingStartTime;
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gameOver || gameCompleted) return;

        // Use FXGL's built-in timing
        double frameTime = FXGL.tpf();

        if (playerHealth <= 0) {
            showGameOverScreen("Game Over!");
            return;
        }

        // Handle wave announcement if needed
        if (shouldShowWaveAnnouncement) {
            showWaveAnnouncement();
            shouldShowWaveAnnouncement = false;
            // We don't return here to ensure game updates continue and highlighting works
        }

        // Check if we need to hide the announcement
        if (waveAnnouncementOverlay != null && announcementTimer.elapsed(Duration.seconds(ANNOUNCEMENT_DURATION))) {
            waveAnnouncementOverlay.removeFromWorld();
            waveAnnouncementOverlay = null;
            // Continue spawning after announcement
            isSpawningWave = true;
        }

        // Handle wave spawning
        if (isSpawningWave) {
            // Spawn new group if all gargoyles are defeated or timer elapsed
            if (activeGargoyles.isEmpty() || waveSpawnTimer.elapsed(Duration.seconds(currentSpawnDelay))) {
                if (totalWaveSpawns > 0) {
                    spawnGargoyleGroup();
                } else if (activeGargoyles.isEmpty()) {
                    // Wave completed - prepare for next wave
                    currentWave++;
                    waveText.setText("Wave: " + currentWave + "/" + MAX_WAVES);
                    waveInProgress = false;

                    // Check if all waves are completed
                    if (currentWave > MAX_WAVES) {
                        showVictoryScreen();
                        return;
                    }

                    // Set flag to show announcement for next wave
                    shouldShowWaveAnnouncement = true;
                }
            }
        }

        // Spawn word blocks for current wave if not in progress
        if (!waveInProgress && !gameOver && !gameCompleted) {
            waveInProgress = true;
            startWave();
            return;
        }

        // Get wave index (0-based)
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);

        // Calculate current wave speed in pixels per second with wave-specific multiplier
        double currentSpeed = GARGOYLE_SPEED * WAVE_SPEED_MULTIPLIERS[waveIndex];

        // Process gargoyles in batches
        if (!activeGargoyles.isEmpty()) {
            List<Entity> gargoylesToProcess = new ArrayList<>(activeGargoyles);

            int totalGargoyles = gargoylesToProcess.size();
            for (int i = 0; i < totalGargoyles; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalGargoyles);

                for (int j = i; j < end; j++) {
                    if (j >= gargoylesToProcess.size()) break;

                    Entity gargoyle = gargoylesToProcess.get(j);
                    if (gargoyle == null) continue;

                    // Check visibility and activity state
                    boolean isVisible = isEntityVisible(gargoyle);
                    boolean hasBeenVisible = gargoyle.getBoolean("hasBeenVisible");
                    boolean isActive = gargoyle.getBoolean("isActive");
                    boolean movingRight = gargoyle.getBoolean("movingRight");

                    // Mark as visible once it enters the screen
                    if (isVisible && !hasBeenVisible) {
                        gargoyle.setProperty("hasBeenVisible", true);
                        hasBeenVisible = true;
                    }

                    // Activate when fully visible
                    if (isVisible && !isActive) {
                        gargoyle.setProperty("isActive", true);
                        isActive = true;
                    }

                    // Only move and check for removal if the gargoyle is active
                    if (isActive) {
                        // Update position
                        double movement = currentSpeed * frameTime;
                        movement = Math.max(movement, MIN_MOVEMENT);
                        gargoyle.translateX(movingRight ? movement : -movement);

                        // Check if gargoyle has left the screen
                        if ((movingRight && gargoyle.getX() > FXGL.getAppWidth()) ||
                                (!movingRight && gargoyle.getX() < -GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE)) {
                            if (hasBeenVisible) {
                                decreaseHealth();
                            }
                            gargoyle.removeFromWorld();
                            spatialPartitioning.removeEntity(gargoyle);
                            activeGargoyles.remove(gargoyle);
                            gargoylePool.add(gargoyle);

                            // Update selection if needed
                            if (gargoyle == selectedWordBlock) {
                                selectedWordBlock = null;
                                if (!activeGargoyles.isEmpty()) {
                                    Entity closest = GargoyleFactory.findClosestGargoyleToCenter(activeGargoyles);
                                    if (closest != null) {
                                        selectWordBlock(closest);
                                    }
                                }
                            }
                            continue;
                        }
                    }

                    // Update animation
                    if (gargoyle.getViewComponent() != null && !gargoyle.getViewComponent().getChildren().isEmpty()) {
                        StackPane view = (StackPane) gargoyle.getViewComponent().getChildren().get(0);
                        if (view != null && !view.getChildren().isEmpty()) {
                            Node viewNode = view.getChildren().get(0);
                            if (viewNode instanceof AnimatedTexture) {
                                AnimatedTexture texture = (AnimatedTexture) viewNode;
                                double animationTime = gargoyle.getDouble("animationTime") + frameTime;
                                gargoyle.setProperty("animationTime", animationTime);

                                // Don't try to set the animation channel directly here
                                // Just update the animation frame - the texture is already
                                // set up with correct animation channel by GargoyleFactory
                                texture.onUpdate(frameTime);
                            }
                        }
                    }

                    // Update spatial partitioning
                    spatialPartitioning.updateEntity(gargoyle);
                }
            }
        }

        // Check wave completion
        if (activeGargoyles.isEmpty() && waveInProgress && !isSpawningWave) {
            waveCompleted = true;
            waveInProgress = false;
            waveTimer.capture();
            showWaveCompletionMessage();
        }

        // Update performance display using FXGL's timing
        if (frameCount++ % 30 == 0) {
            fps = 1.0 / frameTime;
            updatePerformanceDisplay();

            if (fps < TARGET_FPS * 0.9) {
                System.out.println("Performance Warning: FPS dropped to " + String.format("%.1f", fps));
                System.out.println("Frame time: " + String.format("%.3f", frameTime * 1000) + "ms");
            }
        }

        // Update batch renderer for all visible entities
        if (batchRenderer != null) {
            batchRenderer.update();
        }
    }

    private boolean isEntityVisible(Entity entity) {
        double x = entity.getX();
        // Consider entity visible only when it's within the actual screen bounds
        return x >= 0 && x <= FXGL.getAppWidth();
    }

    private void startWave() {
        // Check if all waves are completed
        if (currentWave > MAX_WAVES) {
            showVictoryScreen();
            return;
        }

        // Remove all existing gargoyles before starting a new wave
        for (Entity g : new ArrayList<>(activeGargoyles)) {
            g.removeFromWorld();
            spatialPartitioning.removeEntity(g);
            gargoylePool.add(g);
        }
        activeGargoyles.clear();
        newBlocks.clear();

        // Get wave index (0-based)
        int waveIndex = currentWave - 1;

        // Reset wave spawning state with wave-specific parameters
        isSpawningWave = false; // Start as false, set to true after announcement
        spawnFromRight = true; // Always spawn from right side

        // Apply wave-specific difficulty settings
        int minSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        int maxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];

        // Cap max spawns based on screen height to prevent overcrowding
        double minY = SCREEN_MARGIN;
        double maxY = FXGL.getAppHeight() - (GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE) - SCREEN_MARGIN;
        double availableHeight = maxY - minY;
        int maxPossibleSpawns = Math.max(1, (int)(availableHeight / MIN_VERTICAL_SPACING));

        // Adjust group size if necessary
        maxSpawns = Math.min(maxSpawns, maxPossibleSpawns);
        minSpawns = Math.min(minSpawns, maxSpawns);

        // Set the current group size
        currentGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);

        // Set total spawns for this wave
        totalWaveSpawns = WAVE_SPAWNS_PER_WAVE[waveIndex];

        // Apply wave-specific spawn delay
        currentSpawnDelay = WAVE_SPAWN_DELAY * SPAWN_DELAY_MULTIPLIERS[waveIndex];

        waveSpawnTimer.capture();

        System.out.println("Starting wave " + currentWave + " with " + totalWaveSpawns + " total spawns");
        System.out.println("Wave " + currentWave + " settings: Speed multiplier " +
                WAVE_SPEED_MULTIPLIERS[waveIndex] + ", Delay multiplier " +
                SPAWN_DELAY_MULTIPLIERS[waveIndex] + ", Max group size: " + maxSpawns);
    }

    private void spawnGargoyleGroup() {
        // Get wave index (0-based)
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);

        System.out.println("Spawning group of " + currentGroupSize + " gargoyles from right side");

        // Calculate spawn area
        double minY = SCREEN_MARGIN;
        double maxY = FXGL.getAppHeight() - (GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE) - SCREEN_MARGIN;

        // Use GargoyleFactory to spawn a group of gargoyles
        List<Entity> spawnedGargoyles = GargoyleFactory.spawnGargoyleGroup(
                currentGroupSize,
                minY,
                maxY,
                spawnFromRight, // Always spawn from right
                spawnPerimeterRight,
                this::getRandomWordForWave, // Method reference to get random words
                EntityType.GARGOYLE
        );

        // Add spawned gargoyles to active list and update counts
        activeGargoyles.addAll(spawnedGargoyles);
        newBlocks.addAll(spawnedGargoyles);
        totalWaveSpawns -= spawnedGargoyles.size();

        System.out.println("Successfully spawned " + spawnedGargoyles.size() + " gargoyles");

        // Update spatial partitioning
        if (!newBlocks.isEmpty()) {
            spatialPartitioning.batchUpdate(newBlocks);
            newBlocks.clear();
        }

        // Select first gargoyle if none selected
        if (selectedWordBlock == null && !activeGargoyles.isEmpty()) {
            selectWordBlock(activeGargoyles.get(0));
        }

        // Prepare for next group with wave-specific parameters
        int waveMinSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        int waveMaxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];

        // Do not limit based on vertical spacing - allow full spawn groups
        currentGroupSize = random.nextInt(waveMaxSpawns - waveMinSpawns + 1) + waveMinSpawns;

        // Apply wave-specific delay reduction for next spawn
        currentSpawnDelay *= SPAWN_SPEED_INCREASE * SPAWN_DELAY_MULTIPLIERS[waveIndex];

        waveSpawnTimer.capture();
    }

    private void selectWordBlock(Entity wordBlock) {
        if (wordBlock == null) return;

        // Deselect the previous block if there is one
        if (selectedWordBlock != null) {
            try {
                // Reset the previous block's word color to default white
                GargoyleFactory.resetBlockToDefaultColor(selectedWordBlock);
            } catch (Exception e) {
                // Ignore errors when resetting colors
            }
        }

        // Select the new block
        selectedWordBlock = wordBlock;

        // Always reset input when switching blocks
        currentInput.setLength(0);

        // Set initial yellow highlight for the selected block
        try {
            GargoyleFactory.selectWordBlock(selectedWordBlock);
        } catch (Exception e) {
            // If we can't highlight, just continue
        }
    }

    private void updateLetterColors() {
        if (selectedWordBlock == null) return;

        try {
            GargoyleFactory.updateLetterColors(selectedWordBlock, currentInput.toString());
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    private void markWordAsComplete() {
        if (selectedWordBlock == null) return;

        try {
            GargoyleFactory.markWordAsComplete(selectedWordBlock);
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    private void showWaveAnnouncement() {
        // Create wave announcement using GamePromptFactory
        Node announcementNode = GamePromptFactory.createWaveAnnouncement(currentWave, MAX_WAVES);

        // Set a lower zIndex to ensure it doesn't block interaction with gargoyles
        waveAnnouncementOverlay = FXGL.entityBuilder()
                .view(announcementNode)
                .zIndex(50) // Lower z-index so it doesn't block interaction
                .buildAndAttach();

        // Reset announcement timer
        announcementTimer.capture();

        // Initialize totalWaveSpawns here to ensure it's set properly
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
        totalWaveSpawns = WAVE_SPAWNS_PER_WAVE[waveIndex];

        // Also spawn gargoyles right away to ensure player can interact with them
        if (!isSpawningWave) {
            isSpawningWave = true;
            spawnGargoyleGroup();
        }
    }

    private void showWaveCompletionMessage() {
        // Do nothing - waves transition immediately
    }

    private void showGameOverScreen(String message) {
        // Set game over flag
        gameOver = true;

        // Use GamePromptFactory to create the game over screen
        Node screenView = GamePromptFactory.createGameOverScreen(
                message,
                score,
                calculateWPM(),
                calculateRawWPM(),
                calculateAccuracy(),
                calculateConsistency(),
                wpmOverTime,
                accuracyOverTime
        );

        gameOverScreen = FXGL.entityBuilder()
                .view(screenView)
                .zIndex(100)
                .buildAndAttach();
    }

    private void showVictoryScreen() {
        // Set game completed flag
        gameCompleted = true;

        // Use GamePromptFactory to create the victory screen (reusing the game over screen with a different message)
        Node screenView = GamePromptFactory.createGameOverScreen(
                "Victory! Game Complete!",
                score,
                calculateWPM(),
                calculateRawWPM(),
                calculateAccuracy(),
                calculateConsistency(),
                wpmOverTime,
                accuracyOverTime
        );

        gameOverScreen = FXGL.entityBuilder()
                .view(screenView)
                .zIndex(100)
                .buildAndAttach();
    }

    private StackPane createStylishButton(String text, double width, double height, Color color) {
        // Just delegate to the UIFactory
        return UIFactory.createStylishButton(text, width, height, color);
    }

    private void setupPerformanceUI() {
        // Create performance display using the UIFactory
        VBox performanceDisplay = UIFactory.createPerformanceDisplay();
        FXGL.addUINode(performanceDisplay);
    }

    private void updatePerformanceDisplay() {
        // Find the performance display by searching through UI nodes
        VBox performanceDisplay = null;
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                // Check safely if the VBox has children
                if (!vbox.getChildren().isEmpty() && vbox.getChildren().get(0) instanceof Text) {
                    Text titleText = (Text) vbox.getChildren().get(0);
                    if ("Performance:".equals(titleText.getText())) {
                        performanceDisplay = vbox;
                        break;
                    }
                }
            }
        }

        // Only update if we found the performance display
        if (performanceDisplay != null) {
            UIFactory.updatePerformanceDisplay(performanceDisplay, fps);
        }
    }

    private String getRandomWordForWave() {
        // Get wave index (0-based) and ensure it's within bounds
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);

        // Get recently used words from active gargoyles to avoid repetition
        List<String> recentlyUsedWords = new ArrayList<>();
        for (Entity gargoyle : activeGargoyles) {
            try {
                String word = gargoyle.getString("word");
                if (word != null && !word.isEmpty()) {
                    recentlyUsedWords.add(word);
                }
            } catch (Exception e) {
                // Ignore errors
            }
        }

        // Count how many long words are already active
        int activeLongWords = 0;
        for (String word : recentlyUsedWords) {
            if (word.length() >= 8) {
                activeLongWords++;
            }
        }

        // Determine if we should avoid long words based on active count
        boolean avoidLongWords = activeLongWords >= 2;

        // Set up a list of candidate words based on wave difficulty
        List<String> candidateWords = new ArrayList<>();
        double rand = random.nextDouble();

        // Early waves (1-3) - mostly easy words
        if (waveIndex < 3) {
            if (rand < 0.7) { // 70% easy words
                candidateWords.addAll(wordList);
            } else { // 30% medium words
                candidateWords.addAll(mediumWordList);
            }
        }
        // Mid waves (4-6) - mix of easy and medium words, few hard words
        else if (waveIndex < 6) {
            if (rand < 0.3) { // 30% easy words
                candidateWords.addAll(wordList);
            } else if (rand < 0.8) { // 50% medium words
                candidateWords.addAll(mediumWordList);
            } else { // 20% hard words
                candidateWords.addAll(hardWordList);
            }
        }
        // Late waves (7-10) - mostly medium and hard words
        else {
            if (rand < 0.1) { // 10% easy words
                candidateWords.addAll(wordList);
            } else if (rand < 0.5) { // 40% medium words
                candidateWords.addAll(mediumWordList);
            } else { // 50% hard words
                candidateWords.addAll(hardWordList);
            }
        }

        // Filter out recently used words
        candidateWords.removeAll(recentlyUsedWords);

        // Filter out long words if there are already too many active
        if (avoidLongWords) {
            candidateWords = candidateWords.stream()
                    .filter(word -> word.length() < 8)
                    .collect(Collectors.toList());

            // If we filtered out all candidates, add back the short words
            if (candidateWords.isEmpty()) {
                candidateWords.addAll(wordList.stream()
                        .filter(word -> word.length() < 6)
                        .collect(Collectors.toList()));
            }
        }

        // If still no candidates, use any word from appropriate list
        if (candidateWords.isEmpty()) {
            if (waveIndex < 3) {
                return wordList.get(random.nextInt(wordList.size()));
            } else if (waveIndex < 6) {
                return mediumWordList.get(random.nextInt(mediumWordList.size()));
            } else {
                return hardWordList.get(random.nextInt(hardWordList.size()));
            }
        }

        // Return a random word from candidates
        return candidateWords.get(random.nextInt(candidateWords.size()));
    }

    // Add methods to calculate and update typing statistics
    private void updateTypingStats() {
        long currentTime = System.currentTimeMillis();

        // Only update stats periodically to avoid overhead
        if (currentTime - lastStatsUpdate < STATS_UPDATE_INTERVAL) {
            return;
        }

        // Update total typing time
        totalTypingTime = currentTime - typingStartTime;

        // Calculate current WPM and accuracy
        double currentWPM = calculateWPM();
        double currentAccuracy = calculateAccuracy();

        // Store in history for graph
        wpmOverTime.add(currentWPM);
        accuracyOverTime.add(currentAccuracy);

        // Update last stats update time
        lastStatsUpdate = currentTime;
    }

    private double calculateWPM() {
        // If no time has elapsed, return 0
        if (totalTypingTime <= 0) return 0;

        // WPM = (characters typed / 5) / (time in minutes)
        // 5 characters is the standard word length
        double minutes = totalTypingTime / 60000.0;
        return (totalCharactersTyped / 5.0) / minutes;
    }

    private double calculateRawWPM() {
        // If no time has elapsed, return 0
        if (totalTypingTime <= 0) return 0;

        // Raw WPM = (total keystrokes / 5) / (time in minutes)
        double minutes = totalTypingTime / 60000.0;
        return (totalKeystrokes / 5.0) / minutes;
    }

    private double calculateAccuracy() {
        // If no keystrokes, return 0
        if (totalKeystrokes <= 0) return 0;

        return (double) correctKeystrokes / totalKeystrokes * 100.0;
    }

    private double calculateConsistency() {
        // If less than 2 keystroke timings, return 0
        if (keystrokeTimings.size() < 2) return 0;

        // Calculate standard deviation of keystroke timings
        double mean = keystrokeTimings.stream().mapToLong(Long::valueOf).average().getAsDouble();
        double variance = keystrokeTimings.stream()
                .mapToDouble(timing -> Math.pow(timing - mean, 2))
                .average()
                .getAsDouble();
        double stdDev = Math.sqrt(variance);

        // Calculate coefficient of variation (lower is more consistent)
        double cv = stdDev / mean;

        // Convert to a percentage (100% = perfect consistency, 0% = terrible)
        // Cap at 100% for very consistent typing
        return Math.max(0, Math.min(100, (1 - cv) * 100));
    }

    private void restartGame() {
        // Remove game over screen
        if (gameOverScreen != null) {
            gameOverScreen.removeFromWorld();
            gameOverScreen = null;
        }

        // Remove all existing gargoyles
        for (Entity g : new ArrayList<>(activeGargoyles)) {
            g.removeFromWorld();
            spatialPartitioning.removeEntity(g);
            gargoylePool.add(g);
        }
        activeGargoyles.clear();

        // Reset game state
        gameOver = false;
        gameCompleted = false;
        waveCompleted = false;
        currentWave = 1;
        waveText.setText("1/" + MAX_WAVES);

        // Reset health
        playerHealth = MAX_HEALTH;
        updateHealthBar();

        // Reset score
        score = 0;
        scoreText.setText("0");

        // Reset typing stats
        totalKeystrokes = 0;
        correctKeystrokes = 0;
        incorrectKeystrokes = 0;
        totalCharactersTyped = 0;
        totalWords = 0;
        typingStartTime = System.currentTimeMillis();
        lastStatsUpdate = typingStartTime;
        wpmOverTime.clear();
        accuracyOverTime.clear();
        keystrokeTimings.clear();
        lastKeystrokeTime = 0;

        // Reset input
        currentInput.setLength(0);
        selectedWordBlock = null;

        // Start first wave
        showWaveStartMessage();
    }

    // Add a method to find and update the wave text in the UI
    private void updateWaveText(int wave) {
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (Node child : hbox.getChildren()) {
                    if (child instanceof VBox) {
                        VBox vbox = (VBox) child;
                        for (Node vboxChild : vbox.getChildren()) {
                            if (vboxChild instanceof Text) {
                                Text text = (Text) vboxChild;
                                if (text.getText() != null && text.getText().startsWith(String.valueOf(wave - 1) + "/")) {
                                    text.setText(wave + "/" + MAX_WAVES);
                                    System.out.println("Updated wave text: " + text.getText());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Could not find wave text to update");
    }

    private void updateHealthBar() {
        // Find the health display by searching through UI nodes
        VBox healthDisplay = null;
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof VBox && node.getUserData() != null) {
                VBox vbox = (VBox) node;
                if (!vbox.getChildren().isEmpty() &&
                        vbox.getChildren().get(0) instanceof Text &&
                        "HEALTH".equals(((Text)vbox.getChildren().get(0)).getText())) {
                    healthDisplay = vbox;
                    break;
                }
            }
        }

        // Only update if we found the health display
        if (healthDisplay != null) {
            UIFactory.updateHealthBar(healthDisplay, playerHealth);
        }
    }

    private void decreaseHealth() {
        playerHealth = Math.max(0, playerHealth - HEALTH_LOSS_PER_MISS);
        updateHealthBar();
    }

    private void updateInputDisplay() {
        // Keep empty implementation since we don't need it anymore
    }

    private void setupInput() {
        FXGL.getInput().addEventHandler(KeyEvent.KEY_TYPED, event -> {
            if (gameOver || gameCompleted || waveCompleted) return;

            char typedChar = event.getCharacter().charAt(0);
            if (Character.isLetterOrDigit(typedChar) || typedChar == '-' || typedChar == '\'') {
                // Update keystroke timing for consistency calculation
                long currentTime = System.currentTimeMillis();
                if (lastKeystrokeTime > 0) {
                    keystrokeTimings.add(currentTime - lastKeystrokeTime);
                }
                lastKeystrokeTime = currentTime;

                // Track total keystrokes
                totalKeystrokes++;

                if (selectedWordBlock != null) {
                    try {
                        String targetWord = selectedWordBlock.getString("word");

                        // Make sure we're not exceeding the word length
                        if (currentInput.length() >= targetWord.length()) {
                            return;
                        }

                        // Check if this would be a valid next character
                        if (typedChar == targetWord.charAt(currentInput.length())) {
                            // Only add if it's correct (part of error trapping)
                            currentInput.append(typedChar);
                            updateLetterColors();

                            // Record correct keystroke and character typed
                            correctKeystrokes++;
                            totalCharactersTyped++;

                            // Check if we've completed the word
                            if (currentInput.length() == targetWord.length()) {
                                // Word is fully typed - make all letters blue for visual feedback
                                markWordAsComplete();
                            }
                        } else {
                            // Wrong character - clear input and reset
                            currentInput.setLength(0);
                            resetToYellowHighlight();

                            // Record incorrect keystroke
                            incorrectKeystrokes++;
                        }
                    } catch (IllegalArgumentException e) {
                        // If word property doesn't exist, ignore and wait for next update
                        currentInput.setLength(0);
                    }
                }

                // Update typing statistics periodically
                updateTypingStats();
            }
        });

        FXGL.getInput().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (gameOver) {
                // Handle retry on game over screen
                if (event.getCode() == KeyCode.ENTER) {
                    if (gameOverScreen != null) {
                        restartGame();
                    }
                }
                return;
            }

            if (waveCompleted) return;

            if (event.getCode() == KeyCode.BACK_SPACE && currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                updateLetterColors();
            } else if (event.getCode() == KeyCode.SHIFT) {
                // Switch between blocks with Shift
                selectNextWordBlock();
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                // Complete word with Space
                checkWordCompletion();
                event.consume();
            }
        });
    }

    private void selectNextWordBlock() {
        if (activeGargoyles.isEmpty()) return;

        int currentIndex = activeGargoyles.indexOf(selectedWordBlock);
        if (currentIndex == -1) {
            // Current selection not found, select first valid one
            for (Entity gargoyle : activeGargoyles) {
                try {
                    gargoyle.getString("word"); // Check if it has the word property
                    selectWordBlock(gargoyle);
                    return;
                } catch (Exception e) {
                    continue;
                }
            }
            return;
        }

        // Try to find the next valid gargoyle
        for (int i = 1; i <= activeGargoyles.size(); i++) {
            int nextIndex = (currentIndex + i) % activeGargoyles.size();
            Entity nextGargoyle = activeGargoyles.get(nextIndex);

            try {
                nextGargoyle.getString("word"); // Check if it has the word property
                selectWordBlock(nextGargoyle);
                return;
            } catch (Exception e) {
                continue;
            }
        }
    }

    private void checkWordCompletion() {
        if (selectedWordBlock == null) return;

        try {
            String targetWord = selectedWordBlock.getString("word");
            String typed = currentInput.toString();

            // Check if the typed text matches the target word
            if (typed.equals(targetWord)) {
                // Word completed successfully
                Entity completedBlock = selectedWordBlock;

                // Calculate score based on word length and current wave
                int wordScore = targetWord.length() * 10 * currentWave;
                score += wordScore;
                scoreText.setText(Integer.toString(score));

                // Update total words completed
                totalWords++;

                // Clear the selection before we remove the entity
                selectedWordBlock = null;

                // Remove the completed block
                completedBlock.removeFromWorld();
                spatialPartitioning.removeEntity(completedBlock);
                activeGargoyles.remove(completedBlock);
                gargoylePool.add(completedBlock);

                // Select a new gargoyle if any available
                if (!activeGargoyles.isEmpty()) {
                    // Find the closest one to the center of the screen
                    Entity closest = GargoyleFactory.findClosestGargoyleToCenter(activeGargoyles);
                    if (closest != null) {
                        selectWordBlock(closest);
                    }
                }
            } else {
                // Word not completed - reset input and maintain green highlight
                currentInput.setLength(0);
                resetToYellowHighlight();
            }
        } catch (IllegalArgumentException e) {
            // Handle missing property gracefully
            currentInput.setLength(0);

            // Try to select another entity if available
            if (!activeGargoyles.isEmpty() && selectedWordBlock != null) {
                // Try to find any valid gargoyle to select
                for (Entity gargoyle : activeGargoyles) {
                    try {
                        // Verify this gargoyle has the word property
                        gargoyle.getString("word");
                        selectWordBlock(gargoyle);
                        break;
                    } catch (Exception ex) {
                        // Skip this one if it has errors
                        continue;
                    }
                }
            }
        }
    }

    // New method to reset block to default white color
    private void resetBlockToDefaultColor(Entity block) {
        if (block == null) return;

        try {
            List<Text> letterNodes = block.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(DEFAULT_COLOR);
                    // Preserve the stroke and glow effect for visibility
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    // New method to reset the current selected block to yellow highlight
    private void resetToYellowHighlight() {
        if (selectedWordBlock == null) return;

        try {
            List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(SELECTED_COLOR);

                    // Increase glow effect for better visibility when selected
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    // Add setupUI method
    private void setupUI() {
        // Create health display using UIFactory
        VBox healthDisplay = UIFactory.createHealthDisplay(playerHealth, MAX_HEALTH);

        // Create top bar using UIFactory
        HBox topBar = UIFactory.createTopBar(score, currentWave, MAX_WAVES);

        // Store references to score and wave text
        // Use utility methods to find the elements instead of direct casting
        for (Node child : topBar.getChildren()) {
            if (child instanceof VBox) {
                VBox box = (VBox) child;
                if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Text) {
                    Text label = (Text) box.getChildren().get(0);
                    if (label.getText().equals("SCORE") && box.getChildren().size() > 1) {
                        scoreText = (Text) box.getChildren().get(1);
                    } else if (label.getText().equals("WAVE") && box.getChildren().size() > 1) {
                        waveText = (Text) box.getChildren().get(1);
                    }
                }
            }
        }

        // Create instruction text
        instructionText = UIFactory.createInstructionText();

        // Create controls help text
        Text controlsText = UIFactory.createControlsText();

        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(topBar);
        FXGL.addUINode(instructionText);
        FXGL.addUINode(controlsText);
    }

    // Add showWaveStartMessage method
    private void showWaveStartMessage() {
        // Reset and hide instruction text
        instructionText.setVisible(false);

        // Set wave in progress and prepare for announcement
        waveInProgress = true;
        shouldShowWaveAnnouncement = true;

        // Start the wave initialization without spawning yet
        startWave();
    }

    public static void main(String[] args) {
        launch(args);
    }
}