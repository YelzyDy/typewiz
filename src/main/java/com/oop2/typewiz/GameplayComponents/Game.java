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
    private static final double WAVE_SPEED_INCREMENT = 10.0; // Speed increase per wave in pixels per second
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
    private static final Color SELECTED_COLOR = Color.YELLOW;
    private static final Color TYPED_COLOR = Color.BLUE;
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
        
        // Add gargoyle animation
        Image gargoyleImage = FXGL.image("mobs/gargoyle/gargoyle.png");
        
        // Calculate frame size based on sprite sheet dimensions
        int gargoyleFrameWidth = 288;  // 864 ÷ 3 columns
        int gargoyleFrameHeight = 312; // 936 ÷ 3 rows
        
        // Create animation channel for idle animation (first row)
        AnimationChannel gargoyleIdleAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.2), // 5 frames per second (matching 144 FPS)
                0, 2); // First row, frames 0-2
        
        // Create animation channel for attack animation (second row)
        AnimationChannel gargoyleAttackAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.15), // ~6.67 frames per second (matching 144 FPS)
                3, 5); // Second row, frames 3-5
        
        // Create animation channel for flying animation (third row)
        AnimationChannel gargoyleFlyAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.18), // ~5.56 frames per second (matching 144 FPS)
                6, 8); // Third row, frames 6-8
        
        // Create the animated texture with idle animation as default
        AnimatedTexture gargoyleTexture = new AnimatedTexture(gargoyleIdleAnimation);
        gargoyleTexture.loop();
        
        // Scale the gargoyle to a proper size for the scene
        double gargoyleScale = 0.25; // Scale to 25% of original size
        
        // Create the gargoyle entity positioned on the right side
        Entity gargoyleEntity = FXGL.entityBuilder()
                .type(EntityType.GARGOYLE)
                .at(FXGL.getAppWidth() - (gargoyleFrameWidth * gargoyleScale) - 10, 80) // Right side position
                .view(gargoyleTexture)
                .scale(gargoyleScale, gargoyleScale) // Scale down the gargoyle
                .bbox(new HitBox(BoundingShape.box(gargoyleFrameWidth * gargoyleScale, gargoyleFrameHeight * gargoyleScale)))
                .zIndex(25)
                .buildAndAttach();
        
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
        
        // Initialize word block pool with enough blocks for maximum concurrent blocks
        wordBlockPool = new WordBlockPool(NUM_ROWS * 2, 40); // Double the number of rows to ensure we have enough blocks
        
        // Initialize spatial partitioning with cell size of 100 pixels
        spatialPartitioning = new SpatialPartitioning(100, FXGL.getAppWidth(), FXGL.getAppHeight());
        
        // Initialize batch renderer
        batchRenderer = new BatchRenderer(spatialPartitioning);
        
        // Add performance monitoring UI
        setupPerformanceUI();
        
        // Run the game loop
        FXGL.getGameTimer().runAtInterval(() -> {
            if (gameOver) return;
            
            // Handle wave completion
            if (waveCompleted) {
                if (waveTimer.elapsed(Duration.seconds(WAVE_PAUSE_TIME))) {
                    waveCompleted = false;
                    currentWave++;
                    waveText.setText("Wave: " + currentWave);
                    blockSpawnTimer.capture(); // Reset spawn timer for next wave
                    waveInProgress = false;
                    
                    // Show message for next wave
                    showWaveStartMessage();
                }
                return;
            }
            
            if (playerHealth <= 0) {
                showGameOverScreen("Game Over!");
                return;
            }
            
            // Spawn word blocks for current wave if not in progress
            if (!waveInProgress) {
                waveInProgress = true;
                startWave();
                return;
            }
            
            // Update all word blocks
            List<Entity> blocksToRemove = new ArrayList<>();
            
            for (Entity entity : wordBlockPool.getActiveBlocks()) {
                // Calculate movement based on fixed time step with synchronized wave speed
                double blockSpeedForWave = BLOCK_SPEED + (currentWave * WAVE_SPEED_INCREMENT);
                double movement = blockSpeedForWave * FXGL.tpf();
                entity.translateX(-movement);
                
                // Update spatial partitioning
                spatialPartitioning.updateEntity(entity);
                
                // If block reaches the left edge, it's a miss
                if (entity.getX() < 0) {
                    decreaseHealth();
                    blocksToRemove.add(entity);
                }
            }
            
            // Release blocks that have gone off screen
            for (Entity block : blocksToRemove) {
                wordBlockPool.release(block);
                spatialPartitioning.removeEntity(block);
            }
            
            // If the selected block was removed, select the next one
            if (blocksToRemove.contains(selectedWordBlock) && !wordBlockPool.getActiveBlocks().isEmpty()) {
                selectWordBlock(wordBlockPool.getActiveBlocks().get(0));
            } else if (blocksToRemove.contains(selectedWordBlock)) {
                selectedWordBlock = null;
            }
            
            // Check if wave is complete (no blocks left)
            if (wordBlockPool.getActiveCount() == 0 && waveInProgress) {
                waveCompleted = true;
                waveInProgress = false;
                waveTimer.capture();
                // Show wave completion message
                showWaveCompletionMessage();
            }
            
            // Update batch renderer
            batchRenderer.update();
        }, Duration.seconds(0.016)); // ~60 fps
    }
    
    @Override
    protected void onUpdate(double tpf) {
        if (gameOver) return;
        
        // Use FXGL's built-in timing
        double frameTime = FXGL.tpf();
        
        // Handle wave completion
        if (waveCompleted) {
            if (waveTimer.elapsed(Duration.seconds(WAVE_PAUSE_TIME))) {
                waveCompleted = false;
                currentWave++;
                waveText.setText("Wave: " + currentWave);
                blockSpawnTimer.capture();
                waveInProgress = false;
                showWaveStartMessage();
            }
            return;
        }
        
        if (playerHealth <= 0) {
            showGameOverScreen("Game Over!");
            return;
        }
        
        // Spawn word blocks for current wave if not in progress
        if (!waveInProgress) {
            waveInProgress = true;
            startWave();
            return;
        }
        
        // Clear lists for reuse
        blocksToRemove.clear();
        activeBlocks.clear();
        
        // Safely get active blocks
        List<Entity> activeBlocksList = wordBlockPool.getActiveBlocks();
        if (activeBlocksList != null && !activeBlocksList.isEmpty()) {
            activeBlocks.addAll(activeBlocksList);
        }
        
        // Calculate current wave speed in pixels per second
        double currentSpeed = BLOCK_SPEED + (currentWave * WAVE_SPEED_INCREMENT);
        
        // Process blocks in batches with improved performance
        int totalBlocks = activeBlocks.size();
        for (int i = 0; i < totalBlocks; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, totalBlocks);
            
            for (int j = i; j < end; j++) {
                Entity entity = activeBlocks.get(j);
                
                // Skip processing if entity is already out of bounds
                if (entity.getX() < -200) {
                    blocksToRemove.add(entity);
                    continue;
                }
                
                // Calculate movement using FXGL's timing
                double movement = currentSpeed * frameTime;
                
                // Ensure minimum movement to prevent stuttering
                movement = Math.max(movement, MIN_MOVEMENT);
                
                // Update position using FXGL's entity methods
                entity.translateX(-movement);
                
                // Only update spatial partitioning if entity is still visible
                if (isEntityVisible(entity)) {
                    spatialPartitioning.updateEntity(entity);
                } else {
                    // If entity is no longer visible, mark it for removal
                    blocksToRemove.add(entity);
                }
                
                // Check if block should be removed
                if (entity.getX() < 0) {
                    decreaseHealth();
                    blocksToRemove.add(entity);
                }
            }
        }
        
        // Batch process removals
        if (!blocksToRemove.isEmpty()) {
            for (Entity block : blocksToRemove) {
                wordBlockPool.release(block);
                spatialPartitioning.removeEntity(block);
            }
            
            // Update selection if needed
            if (blocksToRemove.contains(selectedWordBlock)) {
                List<Entity> remainingBlocks = wordBlockPool.getActiveBlocks();
                if (remainingBlocks != null && !remainingBlocks.isEmpty()) {
                    selectWordBlock(remainingBlocks.get(0));
                } else {
                    selectedWordBlock = null;
                }
            }
        }
        
        // Check wave completion
        if (wordBlockPool.getActiveCount() == 0 && waveInProgress) {
            waveCompleted = true;
            waveInProgress = false;
            waveTimer.capture();
            showWaveCompletionMessage();
        }
        
        // Update performance display using FXGL's timing
        if (frameCount++ % 30 == 0) { // Update every 30 frames
            fps = 1.0 / frameTime;
            updatePerformanceDisplay();
            
            // Log performance warnings
            if (fps < TARGET_FPS * 0.9) {
                System.out.println("Performance Warning: FPS dropped to " + String.format("%.1f", fps));
                System.out.println("Frame time: " + String.format("%.3f", frameTime * 1000) + "ms");
            }
        }
        
        // Only update batch renderer if there are active blocks
        if (batchRenderer != null && !activeBlocks.isEmpty()) {
            batchRenderer.update();
        }
    }
    
    private boolean isEntityVisible(Entity entity) {
        double x = entity.getX();
        return x >= -200 && x <= FXGL.getAppWidth() + 200; // Increased padding for smoother transitions
    }
    
    private void startWave() {
        // Clear and reuse the newBlocks list
        newBlocks.clear();
        
        // Keep original bottom row position
        double bottomRowY = FXGL.getAppHeight() - 120;
        double fixedX = FXGL.getAppWidth() + 10;
        
        // Spawn blocks in all rows with vertical alignment
        for (int i = 0; i < NUM_ROWS; i++) {
            double rowY = bottomRowY - (i * ROW_SPACING);
            
            // Pick a random word from the list based on current wave
            String word = getRandomWordForWave();
            
            // Acquire a block from the pool
            Entity wordBlock = wordBlockPool.acquire(fixedX, rowY, word, i);
            newBlocks.add(wordBlock);
        }
        
        // Batch update spatial partitioning with improved performance
        if (!newBlocks.isEmpty()) {
            spatialPartitioning.batchUpdate(newBlocks);
        }
        
        // Select the first word block by default if none is selected
        if (selectedWordBlock == null && !newBlocks.isEmpty()) {
            selectWordBlock(newBlocks.get(0));
        }
        
        // Hide instruction text
        if (instructionText != null) {
            instructionText.setVisible(false);
        }
    }
    
    private String getRandomWordForWave() {
        if (currentWave <= 3) {
            return wordList.get(random.nextInt(wordList.size()));
        } else if (currentWave <= 7) {
            return random.nextBoolean() 
                ? wordList.get(random.nextInt(wordList.size())) 
                : mediumWordList.get(random.nextInt(mediumWordList.size()));
        } else {
            double rand = random.nextDouble();
            if (rand < 0.2) {
                return wordList.get(random.nextInt(wordList.size()));
            } else if (rand < 0.5) {
                return mediumWordList.get(random.nextInt(mediumWordList.size()));
            } else {
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
        
        waveText = new Text("1");
        waveText.setFill(Color.WHITE);
        waveText.setFont(Font.font(24));
        
        waveDisplay.getChildren().addAll(waveLabel, waveText);
        
        // Create instruction text
        instructionText = new Text("Press ENTER to start wave");
        instructionText.setTranslateX(FXGL.getAppWidth() / 2 - 150);
        instructionText.setTranslateY(FXGL.getAppHeight() / 2);
        instructionText.setFill(Color.YELLOW);
        instructionText.setFont(Font.font(28));
        
        // Add controls help text
        Text controlsText = new Text("Controls: SHIFT to switch words | SPACE to destroy word");
        controlsText.setTranslateX(FXGL.getAppWidth() / 2 - 200);
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
            if (!waveInProgress) return;
            
            char typedChar = event.getCharacter().charAt(0);
            if (Character.isLetterOrDigit(typedChar) || typedChar == '-' || typedChar == '\'') {
                if (selectedWordBlock != null) {
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
            
            // Start wave when Enter is pressed
            if (!waveInProgress && !waveCompleted && event.getCode() == KeyCode.ENTER) {
                startWave();
                return;
            }
            
            if (waveCompleted) return;
            if (!waveInProgress) return;
            
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
        // Deselect the previous block if there is one
        if (selectedWordBlock != null) {
            // Reset the previous block's word color to default white
            resetBlockToDefaultColor(selectedWordBlock);
        }
        
        // Select the new block
        selectedWordBlock = wordBlock;
        
        // Always reset input when switching blocks
        currentInput.setLength(0);
        
        // Set initial yellow highlight for the selected block
        resetToYellowHighlight();
    }
    
    // New method to reset block to default white color
    private void resetBlockToDefaultColor(Entity block) {
        List<Text> letterNodes = block.getObject("letterNodes");
        if (letterNodes != null) {
            for (Text letter : letterNodes) {
                letter.setFill(DEFAULT_COLOR);
            }
        }
    }
    
    // New method to reset the current selected block to yellow highlight
    private void resetToYellowHighlight() {
        if (selectedWordBlock == null) return;
        
        List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
        if (letterNodes != null) {
            for (Text letter : letterNodes) {
                letter.setFill(SELECTED_COLOR);
            }
        }
    }
    
    // New method to update letter colors based on typing progress
    private void updateLetterColors() {
        if (selectedWordBlock == null) return;
        
        List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
        if (letterNodes == null) return;
        
        String typed = currentInput.toString();
        
        // Update colors - typed letters blue, remaining letters yellow
        for (int i = 0; i < letterNodes.size(); i++) {
            if (i < typed.length()) {
                letterNodes.get(i).setFill(TYPED_COLOR);
            } else {
                letterNodes.get(i).setFill(SELECTED_COLOR);
            }
        }
    }
    
    // New method to mark all letters in a word as complete (all blue)
    private void markWordAsComplete() {
        if (selectedWordBlock == null) return;
        
        List<Text> letterNodes = selectedWordBlock.getObject("letterNodes");
        if (letterNodes != null) {
            for (Text letter : letterNodes) {
                letter.setFill(TYPED_COLOR);
            }
        }
    }
    
    private void selectNextWordBlock() {
        if (wordBlockPool.getActiveBlocks().isEmpty()) return;
        
        int currentIndex = wordBlockPool.getActiveBlocks().indexOf(selectedWordBlock);
        int nextIndex = (currentIndex + 1) % wordBlockPool.getActiveBlocks().size();
        selectWordBlock(wordBlockPool.getActiveBlocks().get(nextIndex));
    }
    
    private void checkWordCompletion() {
        if (selectedWordBlock == null) return;
        
        String targetWord = selectedWordBlock.getString("word");
        String typed = currentInput.toString();
        
        // Check if the typed text matches the target word
        if (typed.equals(targetWord)) {
            // Word completed successfully
            Entity completedBlock = selectedWordBlock;
            wordBlockPool.release(completedBlock);
            
            // Calculate score based on word length and current wave
            int wordScore = targetWord.length() * 10 * currentWave;
            score += wordScore;
            scoreText.setText(Integer.toString(score));
            
            // Select next block if available
            if (!wordBlockPool.getActiveBlocks().isEmpty()) {
                selectWordBlock(wordBlockPool.getActiveBlocks().get(0));
            } else {
                selectedWordBlock = null;
            }
        }
    }
    
    private void showWaveStartMessage() {
        instructionText.setText("Wave " + currentWave + " - Press ENTER to start");
        instructionText.setVisible(true);
    }
    
    private void showWaveCompletionMessage() {
        Text waveCompleteText = new Text("Wave " + currentWave + " Complete!");
        waveCompleteText.setFont(Font.font(48));
        waveCompleteText.setFill(Color.YELLOW);
        waveCompleteText.setTranslateX(FXGL.getAppWidth() / 2 - 200);
        waveCompleteText.setTranslateY(FXGL.getAppHeight() / 2);
        
        Entity waveMessageEntity = FXGL.entityBuilder()
                .view(waveCompleteText)
                .with("removeAfter", WAVE_PAUSE_TIME) // Remove after pause time
                .zIndex(90)
                .buildAndAttach();
        
        // Schedule removal
        FXGL.getGameTimer().runOnceAfter(() -> {
            waveMessageEntity.removeFromWorld();
        }, Duration.seconds(WAVE_PAUSE_TIME));
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
        waveCompleted = false;
        waveInProgress = false;
        currentInput.setLength(0);
        score = 0;
        currentWave = 1;
        
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
        waveText.setText("Wave: 1");
        
        // Show wave start message
        showWaveStartMessage();
        
        // Reset timers
        blockSpawnTimer.capture();
        waveTimer.capture();
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

    public static void main(String[] args) {
        launch(args);
    }
}