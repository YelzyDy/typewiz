package com.oop2.typewiz.GameplayComponents;

import com.oop2.typewiz.GameplayComponents.StatsUIFactory;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.Collections;

import static com.oop2.typewiz.GameplayComponents.StatsUIFactory.createStatsPanel;

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

        // Create animation channels for gargoyles
        Image gargoyleImage = FXGL.image("mobs/gargoyle/gargoyle.png");

        // Create animation channel for idle animation (first row)
        gargoyleIdleAnimation = new AnimationChannel(gargoyleImage,
                4, // 4 frames per row
                GARGOYLE_FRAME_WIDTH, GARGOYLE_FRAME_HEIGHT,
                Duration.seconds(WING_FLAP_SPEED * 1.5), // Even slower for idle
                0, 3); // First row, frames 0-3

        // Create animation channel for flying animation (second row)
        gargoyleFlyAnimation = new AnimationChannel(gargoyleImage,
                4, // 4 frames per row
                GARGOYLE_FRAME_WIDTH, GARGOYLE_FRAME_HEIGHT,
                Duration.seconds(WING_FLAP_SPEED), // Slower for flying
                4, 7); // Second row, frames 4-7

        // Initialize gargoyle pool
        for (int i = 0; i < MAX_GARGOYLES; i++) {
            // Create a new texture instance for each gargoyle
            AnimatedTexture individualTexture = new AnimatedTexture(gargoyleFlyAnimation);
            individualTexture.loop();

            // Create the gargoyle entity
            Entity gargoyleEntity = FXGL.entityBuilder()
                    .type(EntityType.GARGOYLE)
                    .view(individualTexture)
                    .scale(GARGOYLE_SCALE, GARGOYLE_SCALE)
                    .bbox(new HitBox(BoundingShape.box(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE, GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE)))
                    .zIndex(25)
                    .build();

            // Add animation state tracking
            gargoyleEntity.setProperty("currentAnimation", gargoyleFlyAnimation);
            gargoyleEntity.setProperty("targetAnimation", gargoyleFlyAnimation);
            gargoyleEntity.setProperty("transitionProgress", 1.0);
            gargoyleEntity.setProperty("animationTime", 0.0);

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
                                    Entity closest = findClosestGargoyleToCenter();
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

                                // Ensure flying animation is playing
                                if (texture.getAnimationChannel() != gargoyleFlyAnimation) {
                                    texture.loopAnimationChannel(gargoyleFlyAnimation);
                                }

                                // Update animation frame
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
        double availableHeight = maxY - minY;

        // Generate random positions with minimal spacing checks
        List<Double> spawnPositions = new ArrayList<>();

        // Original behavior - generate random positions
        for (int i = 0; i < currentGroupSize; i++) {
            double yPos = minY + random.nextDouble() * (maxY - minY);
            spawnPositions.add(yPos);
        }

        // Sort positions to keep some visual order
        Collections.sort(spawnPositions);

        // Spawn gargoyles at positions
        int spawned = 0;
        for (double yPos : spawnPositions) {
            String word = getRandomWordForWave();
            spawnGargoyle(spawned, yPos, true); // Always spawn from right

            if (!activeGargoyles.isEmpty()) {
                Entity gargoyle = activeGargoyles.get(activeGargoyles.size() - 1);
                configureGargoyleWord(gargoyle, word, yPos);
                newBlocks.add(gargoyle);
                spawned++;
                totalWaveSpawns--;
            }
        }

        System.out.println("Successfully spawned " + spawned + " gargoyles");

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

    private void setupUI() {

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
    private javafx.scene.layout.Background createPanelBackground(Color color, double cornerRadius) {
        return new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        color,
                        new javafx.scene.layout.CornerRadii(cornerRadius),
                        javafx.geometry.Insets.EMPTY
                )
        );
    }

    private void addPanelBorder(javafx.scene.layout.Region panel, Color color, double cornerRadius) {
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

    private void addTextGlow(Text text, Color color, double intensity) {
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(intensity);
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        text.setEffect(shadow);
    }

    private void updateHealthBar() {
        double healthPercentage = (double) playerHealth / MAX_HEALTH;
        healthBar.setWidth(200 * healthPercentage);
        healthText.setText(playerHealth + "/" + MAX_HEALTH);

        // Update color based on health with smoother gradient
        if (healthPercentage > 0.6) {
            healthBar.setFill(Color.rgb(50, 220, 50)); // Bright green
        } else if (healthPercentage > 0.3) {
            healthBar.setFill(Color.rgb(220, 220, 50)); // Yellow
        } else {
            healthBar.setFill(Color.rgb(220, 50, 50)); // Red
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

    private void selectWordBlock(Entity wordBlock) {
        if (wordBlock == null) return;

        // Deselect the previous block if there is one
        if (selectedWordBlock != null) {
            try {
                // Reset the previous block's word color to default white
                resetBlockToDefaultColor(selectedWordBlock);
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
            resetToYellowHighlight();
        } catch (Exception e) {
            // If we can't highlight, just continue
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

    // New method to update letter colors based on typing progress
    private void updateLetterColors() {
        if (selectedWordBlock == null) return;

        try {
            List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
            if (letterNodes == null) return;

            String typed = currentInput.toString();

            // Update colors - typed letters blue, remaining letters yellow
            for (int i = 0; i < letterNodes.size(); i++) {
                Text letter = letterNodes.get(i);

                if (i < typed.length()) {
                    letter.setFill(TYPED_COLOR);
                    // Add stronger glow for typed letters
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.7);
                    letter.setEffect(glow);
                } else {
                    letter.setFill(SELECTED_COLOR);
                    // Normal glow for untyped letters
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    // New method to mark all letters in a word as complete (all blue)
    private void markWordAsComplete() {
        if (selectedWordBlock == null) return;

        try {
            List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(TYPED_COLOR);

                    // Add strong glow effect for completed words
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
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
                    Entity closest = findClosestGargoyleToCenter();
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

    // New method to find closest gargoyle to center of screen
    private Entity findClosestGargoyleToCenter() {
        if (activeGargoyles.isEmpty()) return null;

        double centerX = FXGL.getAppWidth() / 2.0;
        double centerY = FXGL.getAppHeight() / 2.0;

        Entity closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity gargoyle : activeGargoyles) {
            try {
                // Make sure it has the required property
                gargoyle.getString("word");

                double dx = gargoyle.getX() - centerX;
                double dy = gargoyle.getY() - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = gargoyle;
                }
            } catch (Exception e) {
                // Skip entities with issues
                continue;
            }
        }

        return closest;
    }

    private void showWaveStartMessage() {
        // Reset and hide instruction text
        instructionText.setVisible(false);

        // Set wave in progress and prepare for announcement
        waveInProgress = true;
        shouldShowWaveAnnouncement = true;

        // Start the wave initialization without spawning yet
        startWave();
    }

    private void showWaveCompletionMessage() {
        // Do nothing - waves transition immediately
    }

    private void showGameOverScreen(String message) {
        gameOver = true;

        // Wizardy overlay with violet-gold magical gradient
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(60, 0, 90, 0.85)),   // Top: mystical violet
                new Stop(1, Color.rgb(20, 0, 40, 0.85))    // Bottom: deep arcane purple
        );
        overlay.setFill(gradient);

        // Game Over text with wizardy golden glow
        Text gameOverText = new Text(message);
        gameOverText.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 60));
        gameOverText.setFill(Color.web("#FFD700")); // Golden yellow

        Glow glow = new Glow(0.7);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#9B59B6")); // Enchanted violet shadow
        shadow.setRadius(25);
        shadow.setInput(glow);
        gameOverText.setEffect(shadow);

        // Pulsing animation for magical effect
        FadeTransition pulse = new FadeTransition(Duration.seconds(1.5), gameOverText);
        pulse.setFromValue(0.65);
        pulse.setToValue(1.0);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Calculate stats
        double finalWPM = calculateWPM();
        double finalRawWPM = calculateRawWPM();
        double finalAccuracy = calculateAccuracy();
        double finalConsistency = calculateConsistency();

        // Transfer data to StatsUIFactory before creating UI components
        StatsUIFactory.setTotalCharactersTyped(totalCharactersTyped);
        StatsUIFactory.setWpmData(wpmOverTime);
        StatsUIFactory.setAccuracyData(accuracyOverTime);

        VBox statsPanel = StatsUIFactory.createStatsPanel(finalWPM, finalRawWPM, finalAccuracy, finalConsistency);
        addPanelBorder(statsPanel, Color.web("#E1C16E"), UI_CORNER_RADIUS); // Light golden border

        Canvas graphCanvas = StatsUIFactory.createTypingGraph();

        // Score Text
        Text scoreText = new Text("FINAL SCORE: " + score);
        scoreText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 34));
        scoreText.setFill(Color.web("#FFD700"));
        addTextGlow(scoreText, Color.web("#FFD700"), 0.5);

        // Wave Text
        Text waveText = new Text("Waves completed: " + (currentWave - 1));
        waveText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        waveText.setFill(Color.web("#E6DAF0")); // Pale wizardy text
        addTextGlow(waveText, Color.web("#9B59B6"), 0.3);

        // Retry Button
        StackPane retryButton = createStylishButton("ENTER to Retry", 200, 50, Color.web("#FFD700"));
        ScaleTransition buttonPulse = new ScaleTransition(Duration.seconds(1.2), retryButton);
        buttonPulse.setFromX(0.95);
        buttonPulse.setFromY(0.95);
        buttonPulse.setToX(1.05);
        buttonPulse.setToY(1.05);
        buttonPulse.setCycleCount(Animation.INDEFINITE);
        buttonPulse.setAutoReverse(true);
        buttonPulse.play();

        // Layouts
        HBox gameStatsLayout = new HBox(40);
        gameStatsLayout.setAlignment(Pos.CENTER);
        gameStatsLayout.setPadding(new Insets(20));

        VBox leftColumn = new VBox(20, gameOverText, scoreText, waveText, statsPanel);
        leftColumn.setAlignment(Pos.CENTER_LEFT);
        leftColumn.setPadding(new Insets(50));

        Rectangle leftBg = new Rectangle(400, 520);
        leftBg.setArcWidth(UI_CORNER_RADIUS);
        leftBg.setArcHeight(UI_CORNER_RADIUS);
        leftBg.setFill(Color.rgb(50, 0, 80, 0.75)); // Violet panel
        leftBg.setStroke(Color.web("#FFD700"));    // Golden border
        leftBg.setStrokeWidth(2);

        VBox rightColumn = new VBox(15, graphCanvas);
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPadding(new Insets(10));

        Rectangle rightBg = new Rectangle(420, 340);
        rightBg.setArcWidth(UI_CORNER_RADIUS);
        rightBg.setArcHeight(UI_CORNER_RADIUS);
        rightBg.setFill(Color.rgb(50, 0, 80, 0.75)); // Same violet glass
        rightBg.setStroke(Color.web("#E1C16E"));     // Light gold border
        rightBg.setStrokeWidth(2);

        StackPane leftStack = new StackPane(leftBg, leftColumn);
        StackPane rightStack = new StackPane(rightBg, rightColumn);

        gameStatsLayout.getChildren().addAll(leftStack, rightStack);

        VBox fullLayout = new VBox(30, gameStatsLayout, retryButton);
        fullLayout.setAlignment(Pos.CENTER);

        gameOverScreen = FXGL.entityBuilder()
                .view(new StackPane(overlay, fullLayout))
                .zIndex(100)
                .buildAndAttach();
    }

    private void showVictoryScreen() {
        gameCompleted = true;

        // Violet-Gold celebratory magical overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(70, 0, 120, 0.8)),  // Deep violet top
                new Stop(1, Color.rgb(30, 0, 60, 0.8))    // Midnight purple bottom
        );
        overlay.setFill(gradient);

        // Victory text
        Text victoryText = new Text("VICTORY!");
        victoryText.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 72));
        victoryText.setFill(Color.web("#FFD700")); // Golden yellow

        Glow glow = new Glow(0.9);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#D4AF37")); // Gold shadow
        shadow.setRadius(25);
        shadow.setInput(glow);
        victoryText.setEffect(shadow);

        // Magical pulsing animation
        ScaleTransition celebrateScale = new ScaleTransition(Duration.seconds(1.0), victoryText);
        celebrateScale.setFromX(1.0);
        celebrateScale.setFromY(1.0);
        celebrateScale.setToX(1.1);
        celebrateScale.setToY(1.1);
        celebrateScale.setCycleCount(Animation.INDEFINITE);
        celebrateScale.setAutoReverse(true);
        celebrateScale.play();

        // Subtitle
        Text subtitleText = new Text("All Waves Completed!");
        subtitleText.setFont(Font.font(FONT_FAMILY, 28));
        subtitleText.setFill(Color.web("#EDE6FF")); // Light violet-white
        addTextGlow(subtitleText, Color.web("#B388EB"), 0.4);

        // Stats
        double finalWPM = calculateWPM();
        double finalRawWPM = calculateRawWPM();
        double finalAccuracy = calculateAccuracy();
        double finalConsistency = calculateConsistency();

        // Transfer data to StatsUIFactory before creating UI components
        StatsUIFactory.setTotalCharactersTyped(totalCharactersTyped);
        StatsUIFactory.setWpmData(wpmOverTime);
        StatsUIFactory.setAccuracyData(accuracyOverTime);

        VBox statsPanel = StatsUIFactory.createStatsPanel(finalWPM, finalRawWPM, finalAccuracy, finalConsistency);
        addPanelBorder(statsPanel, Color.web("#E1C16E"), UI_CORNER_RADIUS); // Light gold border

        Canvas graphCanvas = StatsUIFactory.createTypingGraph();

        // Final score text
        Text scoreText = new Text("FINAL SCORE: " + score);
        scoreText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 34));
        scoreText.setFill(Color.web("#FFD700"));
        addTextGlow(scoreText, Color.web("#FFD700"), 0.5);

        // Health text
        Text healthText = new Text("Health remaining: " + playerHealth);
        healthText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        healthText.setFill(Color.web("#F0EFFF"));
        addTextGlow(healthText, Color.web("#9B59B6"), 0.3);

        // Waves text
        Text waveText = new Text("All " + MAX_WAVES + " waves completed!");
        waveText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        waveText.setFill(Color.LIGHTGREEN);
        addTextGlow(waveText, Color.LIGHTGREEN, 0.4);

        // Play again button
        StackPane restartButton = createStylishButton("PLAY AGAIN", 180, 50, Color.web("#FFD700"));
        ScaleTransition buttonPulse = new ScaleTransition(Duration.seconds(1.2), restartButton);
        buttonPulse.setFromX(0.95);
        buttonPulse.setFromY(0.95);
        buttonPulse.setToX(1.05);
        buttonPulse.setToY(1.05);
        buttonPulse.setCycleCount(Animation.INDEFINITE);
        buttonPulse.setAutoReverse(true);
        buttonPulse.play();

        // Title layout
        VBox titleBox = new VBox(10, victoryText, subtitleText);
        titleBox.setAlignment(Pos.CENTER);

        // Layout for stats and graph
        HBox gameStatsLayout = new HBox(40);
        gameStatsLayout.setAlignment(Pos.CENTER);
        gameStatsLayout.setPadding(new Insets(20));

        // Left column (score and stats)
        VBox leftColumn = new VBox(15, scoreText, healthText, waveText, statsPanel);
        leftColumn.setAlignment(Pos.CENTER_LEFT);

        Rectangle leftBg = new Rectangle(400, 520);
        leftBg.setArcWidth(UI_CORNER_RADIUS);
        leftBg.setArcHeight(UI_CORNER_RADIUS);
        leftBg.setFill(Color.rgb(50, 0, 80, 0.75)); // Violet panel
        leftBg.setStroke(Color.web("#FFD700"));     // Golden stroke
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

        gameOverScreen = FXGL.entityBuilder()
                .view(new StackPane(overlay, fullLayout))
                .zIndex(100)
                .buildAndAttach();
    }


    // Helper method to create stylish button-like UI elements
    private StackPane createStylishButton(String text, double width, double height, Color color) {
        // Create button background with rounded corners
        Rectangle buttonBg = new Rectangle(width, height);
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setFill(Color.rgb(60, 60, 80, 0.8));
        buttonBg.setStroke(color);
        buttonBg.setStrokeWidth(3);

        // Add glow effect to button
        javafx.scene.effect.DropShadow buttonGlow = new javafx.scene.effect.DropShadow();
        buttonGlow.setColor(color);
        buttonGlow.setRadius(15);
        buttonGlow.setSpread(0.2);
        buttonBg.setEffect(buttonGlow);

        // Create button text
        Text buttonText = new Text(text);
        buttonText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 24));
        buttonText.setFill(Color.WHITE);

        // Add glow to text
        javafx.scene.effect.Glow textGlow = new javafx.scene.effect.Glow(0.6);
        buttonText.setEffect(textGlow);

        // Stack text on background
        StackPane button = new StackPane(buttonBg, buttonText);

        return button;
    }

    private void setupPerformanceUI() {
        // Create performance display
        VBox performanceDisplay = new VBox(5);
        performanceDisplay.setTranslateX(FXGL.getAppWidth() - 200);
        performanceDisplay.setTranslateY(20);

        Text performanceLabel = new Text("Performance:");
        performanceLabel.setFill(Color.WHITE);
        performanceLabel.setFont(Font.font(16));

        performanceBar = new Rectangle(150, 10, GOOD_PERFORMANCE);

        performanceText = new Text("FPS: 144");
        performanceText.setFill(Color.WHITE);
        performanceText.setFont(Font.font(14));

        performanceDisplay.getChildren().addAll(performanceLabel, performanceBar, performanceText);
        FXGL.addUINode(performanceDisplay);
    }

    private void updatePerformanceDisplay() {
        // Update FPS text
        performanceText.setText(String.format("FPS: %.1f", fps));

        // Calculate performance percentage
        double performancePercentage = Math.min(fps / TARGET_FPS, 1.0);

        // Update performance bar
        performanceBar.setWidth(150 * performancePercentage);

        // Update performance bar color
        if (performancePercentage >= 0.9) {
            performanceBar.setFill(GOOD_PERFORMANCE);
        } else if (performancePercentage >= 0.7) {
            performanceBar.setFill(MEDIUM_PERFORMANCE);
        } else {
            performanceBar.setFill(POOR_PERFORMANCE);
        }
    }

    private double getAverageFrameTime() {
        double sum = 0;
        for (double frameTime : frameTimeHistory) {
            sum += frameTime;
        }
        return (sum / PERFORMANCE_HISTORY_SIZE) * 1000; // Convert to milliseconds
    }

    private void spawnGargoyle(int index, double yPos, boolean fromRight) {
        if (gargoylePool.isEmpty() || spatialPartitioning == null) return;

        // Calculate spawn position
        double xPos;
        if (fromRight) {
            xPos = FXGL.getAppWidth() - spawnPerimeterRight;
        } else {
            xPos = spawnPerimeterRight;
        }

        StackPane wordBlockView = new StackPane();
        AnimatedTexture texture = new AnimatedTexture(gargoyleFlyAnimation);
        texture.loop();
        texture.setScaleX(fromRight ? GARGOYLE_SCALE : -GARGOYLE_SCALE); // Flip sprite if spawning from left
        texture.setScaleY(GARGOYLE_SCALE);
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE);
        textFlow.setMaxHeight(GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE);
        wordBlockView.getChildren().addAll(texture, textFlow);

        // Create new entity with all required properties
        Entity gargoyle = FXGL.entityBuilder()
                .type(EntityType.GARGOYLE)
                .at(xPos, yPos)
                .view(wordBlockView)
                .scale(GARGOYLE_SCALE, GARGOYLE_SCALE)
                .bbox(new HitBox(BoundingShape.box(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE, GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE)))
                .zIndex(25)
                .with("word", "") // Initialize with empty string
                .with("letterNodes", new ArrayList<Text>())
                .with("row", index)
                .with("animationTime", 0.0)
                .with("textFlow", textFlow)
                .with("hasBeenVisible", false)
                .with("isActive", false)
                .with("movingRight", !fromRight)
                .buildAndAttach();

        // Remove one from pool to maintain count
        gargoylePool.remove(gargoylePool.size() - 1);

        activeGargoyles.add(gargoyle);
        spatialPartitioning.updateEntity(gargoyle);
    }

    private void configureGargoyleWord(Entity gargoyle, String word, double yPos) {
        if (gargoyle == null || word == null || word.isEmpty()) {
            return;
        }

        // Set word property
        gargoyle.setProperty("word", word);

        // Get view component and validate
        if (gargoyle.getViewComponent() == null || gargoyle.getViewComponent().getChildren().isEmpty()) {
            return;
        }

        StackPane view = (StackPane) gargoyle.getViewComponent().getChildren().get(0);
        if (view == null || view.getChildren().isEmpty()) {
            return;
        }

        // Get or create TextFlow
        TextFlow textFlow = gargoyle.getObject("textFlow");
        if (textFlow == null) {
            textFlow = new TextFlow();
            textFlow.setTextAlignment(TextAlignment.CENTER);
            textFlow.setTranslateY(WORD_VERTICAL_OFFSET);
            gargoyle.setProperty("textFlow", textFlow);
            view.getChildren().add(textFlow);
        }

        // Clear existing text
        textFlow.getChildren().clear();

        // Create container for word with background
        StackPane wordContainer = new StackPane();

        // Determine font size based on word length - longer words get smaller font
        double fontSize = WORD_FONT_SIZE;
        if (word.length() > 8) {
            fontSize *= 0.8; // 20% smaller for long words
        } else if (word.length() > 12) {
            fontSize *= 0.7; // 30% smaller for very long words
        }

        // Create word background for better visibility
        Rectangle wordBackground = new Rectangle();
        double padding = 10;
        double wordLength = word.length() * fontSize * 0.55 + padding * 2;  // Even more compact width
        wordBackground.setWidth(Math.max(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE * 0.6, wordLength));
        wordBackground.setHeight(fontSize + padding);  // Compact height
        wordBackground.setArcWidth(10);  // Smaller corners
        wordBackground.setArcHeight(10);
        wordBackground.setFill(Color.rgb(0, 0, 0, 0.8));
        wordBackground.setStroke(Color.rgb(200, 200, 200, 0.6));
        wordBackground.setStrokeWidth(1.0);

        // Smaller drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setRadius(5);
        dropShadow.setSpread(0.2);
        wordBackground.setEffect(dropShadow);

        // Create compact HBox for text
        HBox wordBox = new HBox(1); // Minimal spacing between letters
        wordBox.setAlignment(Pos.CENTER);

        // Create letter nodes
        List<Text> letterNodes = new ArrayList<>();
        for (char c : word.toCharArray()) {
            Text letterText = new Text(String.valueOf(c));
            letterText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, fontSize));
            letterText.setFill(Color.WHITE);

            // Add minimal stroke
            letterText.setStroke(Color.BLACK);
            letterText.setStrokeWidth(0.5);

            // Less glow
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.2);
            letterText.setEffect(glow);

            letterNodes.add(letterText);
            wordBox.getChildren().add(letterText);
        }

        // Add word background and text to the container
        wordContainer.getChildren().addAll(wordBackground, wordBox);

        // Add the container to the text flow
        textFlow.getChildren().add(wordContainer);

        // Store letter nodes for later use
        gargoyle.setProperty("letterNodes", letterNodes);

        // Add simpler connecting line
        Line connectionLine = new Line();
        connectionLine.setStartX(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE / 2);
        connectionLine.setStartY(GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE / 2);
        connectionLine.setEndX(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE / 2);
        connectionLine.setEndY(WORD_VERTICAL_OFFSET);
        connectionLine.setStroke(Color.rgb(200, 200, 200, 0.3));
        connectionLine.setStrokeWidth(0.75);
        connectionLine.getStrokeDashArray().addAll(2.0, 2.0);  // Smaller dashes

        // Add to view before the text to keep text on top
        view.getChildren().add(1, connectionLine);
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

    private void showWaveAnnouncement() {
        // Create semi-transparent overlay with a gradient effect
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.rgb(60, 20, 120, 0.7)),
                new javafx.scene.paint.Stop(1, Color.rgb(20, 30, 70, 0.7))
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
        javafx.scene.effect.DropShadow panelGlow = new javafx.scene.effect.DropShadow();
        panelGlow.setColor(UI_ACCENT_COLOR);
        panelGlow.setRadius(20);
        panelGlow.setSpread(0.2);
        announcementPanel.setEffect(panelGlow);

        // Create wave announcement text with exciting styling
        Text waveText = new Text("WAVE " + currentWave);
        waveText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 64));
        waveText.setFill(UI_ACCENT_COLOR);

        // Add text glow and effects
        javafx.scene.effect.DropShadow textShadow = new javafx.scene.effect.DropShadow();
        textShadow.setColor(Color.rgb(255, 150, 0));
        textShadow.setRadius(15);
        textShadow.setSpread(0.5);
        waveText.setEffect(textShadow);

        Text ofText = new Text("OF " + MAX_WAVES);
        ofText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 32));
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
        difficultyText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, 46));
        difficultyText.setFill(difficultyColor);

        // Add glow effect to difficulty text
        javafx.scene.effect.Glow difficultyGlow = new javafx.scene.effect.Glow(0.8);
        javafx.scene.effect.DropShadow difficultyTextShadow = new javafx.scene.effect.DropShadow();
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
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(Duration.seconds(0.3), announcementPane);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.play();

        // Set a lower zIndex to ensure it doesn't block interaction with gargoyles
        waveAnnouncementOverlay = FXGL.entityBuilder()
                .view(new StackPane(overlay, announcementPane))
                .zIndex(50) // Lower z-index so it doesn't block interaction
                .buildAndAttach();

        // Reset announcement timer
        announcementTimer.capture();

        // Also spawn gargoyles right away to ensure player can interact with them
        if (!isSpawningWave) {
            isSpawningWave = true;
            spawnGargoyleGroup();
        }
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



    public static void main(String[] args) {
        launch(args);
    }
}