package com.oop2.typewiz.GameplayComponents;

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
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.Collections;

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
    private static final double WORD_HEIGHT = WORD_FONT_SIZE + 10; // Font size plus padding
    private static final double WORD_VERTICAL_OFFSET = 160;
    
    // Total height of a gargoyle including its word
    private static final double TOTAL_ENTITY_HEIGHT = (GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE) + Math.abs(WORD_VERTICAL_OFFSET) + WORD_HEIGHT;
    
    // Update spacing constants for strict enforcement including word heights
    private static final double MIN_VERTICAL_SPACING = TOTAL_ENTITY_HEIGHT * 1.1; // Reduced from 1.2 to 1.1 to allow more spawns
    private static final double MIN_HORIZONTAL_SPACING = GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE * 2.0; // Reduced from 2.5 to 2.0
    private static final double SPAWN_BUFFER = 20; // Reduced from 30 to 20 to allow closer spawns

    // Add animation state tracking
    private static final double ANIMATION_TRANSITION_TIME = 0.2; // 200ms transition
    private static final double ANIMATION_FRAME_TIME = 0.016; // 60 FPS
    private static final double ANIMATION_FADE_TIME = 0.1; // 100ms fade

    // Add wave spawning constants
    private static final double WAVE_SPAWN_DELAY = 10.0; // 3 seconds between spawn groups
    private static final int MIN_SPAWNS_PER_GROUP = 2;
    private static final int MAX_SPAWNS_PER_GROUP = 3;
    private static final int MIN_SPAWNS_PER_WAVE = 10;
    private static final int MAX_SPAWNS_PER_WAVE = 15;
    private static final double SPAWN_SPEED_INCREASE = 0.85; // Each group spawns 15% faster than the last
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
        currentGroupSize = random.nextInt(maxSpawns - minSpawns + 1) + minSpawns;
        
        // Set total spawns for this wave
        totalWaveSpawns = WAVE_SPAWNS_PER_WAVE[waveIndex];
        
        // Apply wave-specific spawn delay
        currentSpawnDelay = WAVE_SPAWN_DELAY * SPAWN_DELAY_MULTIPLIERS[waveIndex];
        
        waveSpawnTimer.capture();
        
        System.out.println("Starting wave " + currentWave + " with " + totalWaveSpawns + " total spawns");
        System.out.println("Wave " + currentWave + " settings: Speed multiplier " + 
                WAVE_SPEED_MULTIPLIERS[waveIndex] + ", Delay multiplier " + 
                SPAWN_DELAY_MULTIPLIERS[waveIndex]);
    }
    
    private void spawnGargoyleGroup() {
        // Get wave index (0-based)
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
        
        System.out.println("Spawning group of " + currentGroupSize + " gargoyles from right side");
        
        // Calculate spawn area
        double minY = SCREEN_MARGIN;
        double maxY = FXGL.getAppHeight() - (GARGOYLE_FRAME_HEIGHT * GARGOYLE_SCALE) - SCREEN_MARGIN;
        
        // Get random Y positions
        List<Double> spawnPositions = new ArrayList<>();
        for (int i = 0; i < currentGroupSize; i++) {
            spawnPositions.add(minY + random.nextDouble() * (maxY - minY));
        }
        
        // Sort positions to maintain some visual order
        Collections.sort(spawnPositions);
        
        // Spawn gargoyles
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
        currentGroupSize = random.nextInt(waveMaxSpawns - waveMinSpawns + 1) + waveMinSpawns;
        
        // Apply wave-specific delay reduction for next spawn
        currentSpawnDelay *= SPAWN_SPEED_INCREASE * SPAWN_DELAY_MULTIPLIERS[waveIndex];
        
        waveSpawnTimer.capture();
    }
    
    private String getRandomWordForWave() {
        // Get wave index (0-based) and ensure it's within bounds
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
        
        // Early waves (1-3) - mostly easy words
        if (waveIndex < 3) {
            double rand = random.nextDouble();
            if (rand < 0.7) { // 70% easy words
                return wordList.get(random.nextInt(wordList.size()));
            } else { // 30% medium words
                return mediumWordList.get(random.nextInt(mediumWordList.size()));
            }
        } 
        // Mid waves (4-6) - mix of easy and medium words, few hard words
        else if (waveIndex < 6) {
            double rand = random.nextDouble();
            if (rand < 0.3) { // 30% easy words
                return wordList.get(random.nextInt(wordList.size()));
            } else if (rand < 0.8) { // 50% medium words
                return mediumWordList.get(random.nextInt(mediumWordList.size()));
            } else { // 20% hard words
                return hardWordList.get(random.nextInt(hardWordList.size()));
            }
        } 
        // Late waves (7-10) - mostly medium and hard words
        else {
            double rand = random.nextDouble();
            if (rand < 0.1) { // 10% easy words
                return wordList.get(random.nextInt(wordList.size()));
            } else if (rand < 0.5) { // 40% medium words
                return mediumWordList.get(random.nextInt(mediumWordList.size()));
            } else { // 50% hard words
                return hardWordList.get(random.nextInt(hardWordList.size()));
            }
        }
    }
    
    private void setupUI() {
        // Create health bar
        VBox healthDisplay = new VBox(5);
        healthDisplay.setTranslateX(20);
        healthDisplay.setTranslateY(20);
        
        Text healthLabel = new Text("Health:");
        healthLabel.setFill(Color.WHITE);
        healthLabel.setFont(Font.font(18));
        
        healthBar = new Rectangle(200, 20, Color.GREEN);
        
        healthText = new Text(playerHealth + "/" + MAX_HEALTH);
        healthText.setFill(Color.WHITE);
        healthText.setFont(Font.font(16));
        
        healthDisplay.getChildren().addAll(healthLabel, healthBar, healthText);
        
        // Create score display
        VBox scoreDisplay = new VBox(5);
        scoreDisplay.setTranslateX(20);
        scoreDisplay.setTranslateY(100);
        
        Text scoreLabel = new Text("Score:");
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setFont(Font.font(18));
        
        scoreText = new Text("0");
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font(24));
        
        scoreDisplay.getChildren().addAll(scoreLabel, scoreText);
        
        // Create wave display
        VBox waveDisplay = new VBox(5);
        waveDisplay.setTranslateX(20);
        waveDisplay.setTranslateY(170);
        
        Text waveLabel = new Text("Wave:");
        waveLabel.setFill(Color.WHITE);
        waveLabel.setFont(Font.font(18));
        
        waveText = new Text("1/" + MAX_WAVES);
        waveText.setFill(Color.WHITE);
        waveText.setFont(Font.font(24));
        
        waveDisplay.getChildren().addAll(waveLabel, waveText);
        
        // Create instruction text (invisible by default)
        instructionText = new Text("");
        instructionText.setTranslateX(FXGL.getAppWidth() / 2 - 150);
        instructionText.setTranslateY(FXGL.getAppHeight() / 2);
        instructionText.setFill(Color.YELLOW);
        instructionText.setFont(Font.font(28));
        instructionText.setVisible(false);
        
        // Add controls help text
        Text controlsText = new Text("Controls: Type words | SHIFT to switch targets | SPACE to destroy word");
        controlsText.setTranslateX(FXGL.getAppWidth() / 2 - 240);
        controlsText.setTranslateY(FXGL.getAppHeight() - 20);
        controlsText.setFill(Color.LIGHTGRAY);
        controlsText.setFont(Font.font(16));
        
        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(scoreDisplay);
        FXGL.addUINode(waveDisplay);
        FXGL.addUINode(instructionText);
        FXGL.addUINode(controlsText);
    }
    
    private void updateHealthBar() {
        double healthPercentage = (double) playerHealth / MAX_HEALTH;
        healthBar.setWidth(200 * healthPercentage);
        healthText.setText(playerHealth + "/" + MAX_HEALTH);
        
        // Update color based on health
        if (healthPercentage > 0.6) {
            healthBar.setFill(Color.GREEN);
        } else if (healthPercentage > 0.3) {
            healthBar.setFill(Color.YELLOW);
        } else {
            healthBar.setFill(Color.RED);
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
            if (gameOver || waveCompleted) return;
            
            char typedChar = event.getCharacter().charAt(0);
            if (Character.isLetterOrDigit(typedChar) || typedChar == '-' || typedChar == '\'') {
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
                            
                            // Check if we've completed the word
                            if (currentInput.length() == targetWord.length()) {
                                // Word is fully typed - make all letters blue for visual feedback
                                markWordAsComplete();
                            }
                        } else {
                            // Wrong character - clear input and reset
                            currentInput.setLength(0);
                            resetToYellowHighlight();
                        }
                    } catch (IllegalArgumentException e) {
                        // If word property doesn't exist, ignore and wait for next update
                        currentInput.setLength(0);
                    }
                }
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
        
        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.color(0, 0, 0, 0.7));
        
        // Create game over text
        Text gameOverText = new Text(message);
        gameOverText.setFont(Font.font(48));
        gameOverText.setFill(Color.WHITE);
        gameOverText.setTranslateY(-70);
        
        // Create score text
        Text scoreText = new Text("Final Score: " + score);
        scoreText.setFont(Font.font(30));
        scoreText.setFill(Color.WHITE);
        scoreText.setTranslateY(-20);
        
        // Create health text
        Text healthText = new Text("Health remaining: " + playerHealth);
        healthText.setFont(Font.font(24));
        healthText.setFill(Color.WHITE);
        healthText.setTranslateY(20);
        
        // Create wave text
        Text waveText = new Text("Waves completed: " + (currentWave - 1));
        waveText.setFont(Font.font(24));
        waveText.setFill(Color.WHITE);
        waveText.setTranslateY(50);
        
        // Create retry text
        Text retryText = new Text("Press ENTER to retry");
        retryText.setFont(Font.font(20));
        retryText.setFill(Color.YELLOW);
        retryText.setTranslateY(90);
        
        // Create layout for game over screen
        VBox gameOverLayout = new VBox(10, gameOverText, scoreText, healthText, waveText, retryText);
        gameOverLayout.setAlignment(Pos.CENTER);
        gameOverLayout.setTranslateX(FXGL.getAppWidth() / 2 - 150);
        gameOverLayout.setTranslateY(FXGL.getAppHeight() / 2 - 100);
        
        // Add game over screen to scene
        gameOverScreen = FXGL.entityBuilder()
                .view(new StackPane(overlay, gameOverLayout))
                .zIndex(100) // Above everything else
                .buildAndAttach();
    }
    
    private void restartGame() {
        // Reset game state
        playerHealth = MAX_HEALTH;
        gameOver = false;
        gameCompleted = false;
        waveCompleted = false;
        waveInProgress = false;
        currentInput.setLength(0);
        score = 0;
        currentWave = 1;
        
        // Reset announcement state
        shouldShowWaveAnnouncement = true;
        if (waveAnnouncementOverlay != null) {
            waveAnnouncementOverlay.removeFromWorld();
            waveAnnouncementOverlay = null;
        }
        
        // Release all word blocks back to the pool
        wordBlockPool.releaseAll();
        selectedWordBlock = null;
        
        // Clear spatial partitioning and batch renderer
        spatialPartitioning.clear();
        batchRenderer.clear();
        
        // Remove game over screen
        if (gameOverScreen != null) {
            gameOverScreen.removeFromWorld();
            gameOverScreen = null;
        }
        
        // Update UI
        updateHealthBar();
        scoreText.setText("0");
        waveText.setText("Wave: " + currentWave + "/" + MAX_WAVES);
        
        // Start the game immediately but don't start spawning yet
        waveInProgress = true;
        startWave();
        
        // Reset timers
        blockSpawnTimer.capture();
        waveTimer.capture();
        announcementTimer.capture();
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
        
        // Adjust width for longer words
        double wordLength = word.length() * WORD_FONT_SIZE * 0.6;  // Approximate width based on character count
        textFlow.setMaxWidth(Math.max(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE * 1.5, wordLength));
        textFlow.setPrefWidth(Math.max(GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE * 1.5, wordLength));
        
        // Disable wrapping to keep text in one line
        textFlow.setLineSpacing(0);

        // Create letter nodes
        List<Text> letterNodes = new ArrayList<>();
        for (char c : word.toCharArray()) {
            Text letterText = new Text(String.valueOf(c));
            letterText.setFont(Font.font("Arial", WORD_FONT_SIZE));
            letterText.setFill(Color.WHITE);
            
            // Add stroke to make text more visible
            letterText.setStroke(Color.BLACK);
            letterText.setStrokeWidth(1.5);
            
            // Add a slight glow effect for better visibility
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.3);
            letterText.setEffect(glow);
            
            letterNodes.add(letterText);
            textFlow.getChildren().add(letterText);
        }

        // Store letter nodes for later use
        gargoyle.setProperty("letterNodes", letterNodes);
    }

    private void showVictoryScreen() {
        gameCompleted = true;
        
        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.color(0, 0, 0, 0.7));
        
        // Create victory text
        Text victoryText = new Text("Victory! All Waves Completed!");
        victoryText.setFont(Font.font(48));
        victoryText.setFill(Color.GOLD);
        victoryText.setTranslateY(-70);
        
        // Create score text
        Text scoreText = new Text("Final Score: " + score);
        scoreText.setFont(Font.font(30));
        scoreText.setFill(Color.WHITE);
        scoreText.setTranslateY(-20);
        
        // Create health text
        Text healthText = new Text("Health remaining: " + playerHealth);
        healthText.setFont(Font.font(24));
        healthText.setFill(Color.WHITE);
        healthText.setTranslateY(20);
        
        // Create waves completed text
        Text waveText = new Text("All " + MAX_WAVES + " waves completed!");
        waveText.setFont(Font.font(24));
        waveText.setFill(Color.LIGHTGREEN);
        waveText.setTranslateY(50);
        
        // Create retry text
        Text retryText = new Text("Press ENTER to restart");
        retryText.setFont(Font.font(20));
        retryText.setFill(Color.YELLOW);
        retryText.setTranslateY(90);
        
        // Create layout for victory screen
        VBox victoryLayout = new VBox(10, victoryText, scoreText, healthText, waveText, retryText);
        victoryLayout.setAlignment(Pos.CENTER);
        victoryLayout.setTranslateX(FXGL.getAppWidth() / 2 - 200);
        victoryLayout.setTranslateY(FXGL.getAppHeight() / 2 - 100);
        
        // Add victory screen to scene
        gameOverScreen = FXGL.entityBuilder()
                .view(new StackPane(overlay, victoryLayout))
                .zIndex(100) // Above everything else
                .buildAndAttach();
    }

    private void showWaveAnnouncement() {
        // Create semi-transparent overlay that doesn't block input
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.color(0, 0, 0, 0.4));
        
        // Create wave announcement text
        Text waveText = new Text("Wave " + currentWave + " of " + MAX_WAVES);
        waveText.setFont(Font.font(48));
        waveText.setFill(Color.YELLOW);
        
        // Create difficulty text
        String difficultyLevel;
        if (currentWave <= 3) {
            difficultyLevel = "Easy";
        } else if (currentWave <= 6) {
            difficultyLevel = "Medium";
        } else if (currentWave <= 9) {
            difficultyLevel = "Hard";
        } else {
            difficultyLevel = "BOSS WAVE";
        }
        
        Text difficultyText = new Text("Difficulty: " + difficultyLevel);
        difficultyText.setFont(Font.font(36));
        difficultyText.setFill(Color.WHITE);
        
        // Create layout for announcement
        VBox announcementLayout = new VBox(20, waveText, difficultyText);
        announcementLayout.setAlignment(Pos.CENTER);
        
        // Set a lower zIndex to ensure it doesn't block interaction with gargoyles
        waveAnnouncementOverlay = FXGL.entityBuilder()
                .view(new StackPane(overlay, announcementLayout))
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

    public static void main(String[] args) {
        launch(args);
    }
}