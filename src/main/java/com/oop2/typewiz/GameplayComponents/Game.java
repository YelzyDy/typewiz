package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Game extends GameApplication {
    // Define EntityType enum inside the Game class
    public enum EntityType {
        PLATFORM,
        PLAYER,
        MOVING_BLOCK,
        GARGOYLE
    }

    // Core game constants
    private static final int MAX_WAVES = 10;
    private static final double GARGOYLE_SPEED = 50.0;
    private static final double GARGOYLE_FRAME_WIDTH = 288;
    private static final double GARGOYLE_FRAME_HEIGHT = 312;
    private static final double GARGOYLE_SCALE = 0.6;
    
    // Word lists for typing
    private final List<String> wordList = Arrays.asList(
            "code", "java", "type", "game", "block",
            "winter", "wizard", "magic", "spell", "potion",
            "frost", "snow", "ice", "cold", "programming",
            "keyboard", "screen", "input", "output", "variable",
            "function", "class", "method", "array", "string"
    );

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

    // Manager instances
    private GameStateManager stateManager;
    private EntityManager entityManager;
    private WaveManager waveManager;
    private PlayerManager playerManager;
    private InputManager inputManager;

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
        setupBackground();
        
        // Set up the game platform
        setupPlatform();
        
        // Add the wizard character
        setupWizard();

        // Initialize managers
        stateManager = new GameStateManager();
        entityManager = new EntityManager(FXGL.getAppWidth(), FXGL.getAppHeight());
        playerManager = new PlayerManager();
        inputManager = new InputManager(entityManager, playerManager, stateManager);
        waveManager = new WaveManager(entityManager, stateManager, this::getRandomWordForWave, FXGL.getAppHeight());
        
        // Initialize GargoyleFactory for entity creation
        GargoyleFactory.initializeAnimations();
        
        // Set up UI elements
        setupUI();
        
        // Set up input handling
        inputManager.setupInput();
        inputManager.setRestartGameCallback(v -> restartGame());
        
        // Set up state handlers
        setupStateHandlers();
        
        // Start the first wave
        stateManager.announceWave(1);
    }

    private void setupBackground() {
        Image backgroundImage = FXGL.image("background-and-platforms/bg-winter_1280_720.png");
        Rectangle background = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        background.setFill(new ImagePattern(backgroundImage));

        FXGL.entityBuilder()
                .at(0, 0)
                .view(background)
            .zIndex(-100)
                .buildAndAttach();
    }

    private void setupPlatform() {
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
                .type(EntityType.PLATFORM)
            .at(platformOffsetX, platformOffsetY)
                .view(platformView)
                .bbox(new HitBox(BoundingShape.box(platformWidth, platformHeight)))
                .with(new PhysicsComponent())
            .zIndex(10)
                .buildAndAttach();
    }

    private void setupWizard() {
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
                .type(EntityType.PLAYER)
            .at(-5, 50)
                .view(wizardTexture)
            .scale(wizardScale, wizardScale)
                .bbox(new HitBox(BoundingShape.box(frameWidth * wizardScale, frameHeight * wizardScale)))
                .zIndex(25)
                .buildAndAttach();
    }

    private void setupUI() {
        // Create health display
        VBox healthDisplay = UIFactory.createHealthDisplay(playerManager.getHealth(), playerManager.getMaxHealth());
        playerManager.setHealthDisplay(healthDisplay);  // Set the health display reference
        
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
        
        // Create top bar
        HBox topBar = UIFactory.createTopBar(0, 1, MAX_WAVES);
        
        // Find and set references to UI elements
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
        VBox performanceDisplay = UIFactory.createPerformanceDisplay();
        
        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(topBar);
        FXGL.addUINode(performanceDisplay);
    }

    private void setupStateHandlers() {
        // Set actions for state transitions
        stateManager.setStateEntryAction(GameStateManager.GameState.WAVE_ANNOUNCEMENT, wave -> {
            // Start the wave to initialize parameters
            waveManager.startWave();
            
            // Show wave announcement and start wave
            Node announcementNode = GamePromptFactory.createWaveAnnouncement(
                waveManager.getCurrentWave(), waveManager.getMaxWaves());
            
            Entity announcement = FXGL.entityBuilder()
                .view(announcementNode)
                .zIndex(50)
                .buildAndAttach();
            
            // Delay the transition to PLAYING state
            FXGL.runOnce(() -> {
                announcement.removeFromWorld();
                stateManager.startPlaying(null);
                waveManager.startSpawning();
            }, Duration.seconds(2.0));
        });
        
        stateManager.setStateEntryAction(GameStateManager.GameState.GAME_OVER, message -> {
            // Pass character count to statistics factory
            StatsUIFactory.setTotalCharactersTyped(playerManager.getTotalCharactersTyped());
            
            // Debug keystroke timings
            System.out.println("DEBUG - Keystroke timings size: " + playerManager.getKeystrokeTimingsSize());
            System.out.println("DEBUG - Consistency value: " + playerManager.calculateConsistency());
            playerManager.dumpKeystrokeTimings();
            
            // Create game over screen with all statistics
            Node gameOverScreen = GamePromptFactory.createGameOverScreen(
                "Game Over!",
                playerManager.getScore(),
                playerManager.calculateWPM(),
                playerManager.calculateRawWPM(),
                playerManager.calculateAccuracy(),
                playerManager.calculateConsistency(),
                playerManager.getWpmOverTime(),
                playerManager.getAccuracyOverTime()
            );
            
            // Add a unique ID to the game over entity for easy identification
            gameOverScreen.setId("game-over-screen");
            
            // Add a "Play Again" button click handler
            findAndSetupPlayAgainButton(gameOverScreen);
            
            FXGL.entityBuilder()
                .view(gameOverScreen)
                .zIndex(100)
                .buildAndAttach();
            
            System.out.println("Game over screen created with ID: game-over-screen");
        });
        
        stateManager.setStateEntryAction(GameStateManager.GameState.VICTORY, message -> {
            // Pass character count to statistics factory
            StatsUIFactory.setTotalCharactersTyped(playerManager.getTotalCharactersTyped());
            
            // Create victory screen with all statistics
            Node victoryScreen = GamePromptFactory.createGameOverScreen(
                "Victory! Game Complete!",
                playerManager.getScore(),
                playerManager.calculateWPM(),
                playerManager.calculateRawWPM(),
                playerManager.calculateAccuracy(),
                playerManager.calculateConsistency(),
                playerManager.getWpmOverTime(),
                playerManager.getAccuracyOverTime()
            );
            
            // Add a unique ID to the victory entity for easy identification
            victoryScreen.setId("victory-screen");
            
            // Add a "Play Again" button click handler
            findAndSetupPlayAgainButton(victoryScreen);
            
            FXGL.entityBuilder()
                .view(victoryScreen)
                .zIndex(100)
                .buildAndAttach();
            
            System.out.println("Victory screen created with ID: victory-screen");
        });
    }
    
    /**
     * Finds and sets up the Play Again button in the game over screen
     * @param gameOverScreen The game over screen node
     */
    private void findAndSetupPlayAgainButton(Node gameOverScreen) {
        // Search for the button in the hierarchy
        if (gameOverScreen instanceof StackPane) {
            System.out.println("Found game over screen StackPane");
            findButtonInChildren(gameOverScreen);
        }
    }
    
    /**
     * Recursively searches for the Play Again button in the node hierarchy
     * @param node The current node to search in
     * @return true if button was found and configured
     */
    private boolean findButtonInChildren(Node node) {
        // Base case: found a StackPane that contains "Play Again" text
        if (node instanceof StackPane) {
            StackPane stackPane = (StackPane) node;
            
            // Search for the text node
            for (Node child : stackPane.getChildren()) {
                if (child instanceof Text) {
                    Text text = (Text) child;
                    if (text.getText().equals("Play Again")) {
                        System.out.println("Found Play Again button!");
                        // Assign an ID to the button for easier identification
                        stackPane.setId("play-again-button");
                        
                        // This is the button, add click handler with event consumption
                        stackPane.setOnMouseClicked(event -> {
                            System.out.println("Play Again clicked!");
                            event.consume(); // Prevent event bubbling
                            
                            // Schedule restart on next frame to avoid concurrent modification
                            FXGL.getGameTimer().runOnceAfter(() -> {
                                restartGame();
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
                if (findButtonInChildren(child)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    protected void onUpdate(double tpf) {
        // Skip update if game is not active
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            return;
        }

        // Check player health
        if (playerManager.getHealth() <= 0) {
            stateManager.gameOver(null);
                        return;
                    }

        // Update wave spawning
        boolean waveCompleted = waveManager.update();
        if (waveCompleted) {
            if (waveManager.areAllWavesCompleted()) {
                stateManager.victory(null);
            } else {
                stateManager.completeWave(null);
                stateManager.announceWave(waveManager.getCurrentWave());
            }
        }
        
        // Update entity positions
        updateEntityPositions(tpf);
        
        // Process entity removals
        entityManager.processRemovals();
        
        // Update performance display
        updatePerformanceDisplay(tpf);
    }

    /**
     * Updates entity positions based on time delta and wave speed
     */
    private void updateEntityPositions(double tpf) {
        // Get current wave speed multiplier
        double speedMultiplier = waveManager.getCurrentWaveSpeedMultiplier();
        
        // Process all active gargoyles
        List<Entity> gargoyles = entityManager.getActiveGargoyles();
        for (Entity gargoyle : gargoyles) {
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
                double movement = GARGOYLE_SPEED * speedMultiplier * tpf;
                movement = Math.max(movement, 1.0); // Prevent micro-stuttering
                        gargoyle.translateX(movingRight ? movement : -movement);

                        // Check if gargoyle has left the screen
                        if ((movingRight && gargoyle.getX() > FXGL.getAppWidth()) ||
                                (!movingRight && gargoyle.getX() < -GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE)) {
                            if (hasBeenVisible) {
                        playerManager.decreaseHealth();
                    }
                    // Mark for removal in the next cycle
                    entityManager.markForRemoval(gargoyle);

                            // Update selection if needed
                    if (gargoyle == inputManager.getSelectedWordBlock()) {
                        Entity closest = GargoyleFactory.findClosestGargoyleToCenter(
                            entityManager.getActiveGargoyles());
                                    if (closest != null) {
                            inputManager.selectWordBlock(closest);
                                }
                            }
                            continue;
                        }
                    }

                    // Update animation
                    if (gargoyle.getViewComponent() != null && !gargoyle.getViewComponent().getChildren().isEmpty()) {
                Node view = gargoyle.getViewComponent().getChildren().get(0);
                if (view != null) {
                    Node viewNode = view instanceof javafx.scene.layout.StackPane ? 
                        ((javafx.scene.layout.StackPane) view).getChildren().get(0) : view;
                    
                            if (viewNode instanceof AnimatedTexture) {
                                AnimatedTexture texture = (AnimatedTexture) viewNode;
                        double animationTime = 0.0;
                        if (gargoyle.getProperties().exists("animationTime")) {
                            animationTime = gargoyle.getDouble("animationTime");
                        }
                        animationTime += tpf;
                                gargoyle.setProperty("animationTime", animationTime);

                        // Update the animation frame
                        texture.onUpdate(tpf);
                            }
                        }
                    }

                    // Update spatial partitioning
            entityManager.getSpatialPartitioning().updateEntity(gargoyle);
        }
    }

    private boolean isEntityVisible(Entity entity) {
        double x = entity.getX();
        // Consider entity visible only when it's within the actual screen bounds
        return x >= 0 && x <= FXGL.getAppWidth();
    }

    private String getRandomWordForWave() {
        // Get wave index and calculate difficulty level
        int waveIndex = waveManager.getCurrentWave() - 1;
        double rand = Math.random();
        
        // Choose words based on wave number and randomness
        if (waveIndex < 3) {
            // Early waves - mostly easy words
            if (rand < 0.7) return getRandomWord(wordList);
            else return getRandomWord(mediumWordList);
        } 
        else if (waveIndex < 6) {
            // Middle waves - mix of easy and medium words
            if (rand < 0.3) return getRandomWord(wordList);
            else if (rand < 0.8) return getRandomWord(mediumWordList);
            else return getRandomWord(hardWordList);
        }
        else {
            // Late waves - mostly medium and hard words
            if (rand < 0.1) return getRandomWord(wordList);
            else if (rand < 0.5) return getRandomWord(mediumWordList);
            else return getRandomWord(hardWordList);
        }
    }
    
    private String getRandomWord(List<String> words) {
        return words.get((int)(Math.random() * words.size()));
    }

    private void updatePerformanceDisplay(double tpf) {
        // Calculate FPS
        double fps = 1.0 / Math.max(tpf, 0.0001);
        
        // Find and update the performance display
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                if (!vbox.getChildren().isEmpty() && vbox.getChildren().get(0) instanceof Text) {
                    Text titleText = (Text) vbox.getChildren().get(0);
                    if ("Performance:".equals(titleText.getText())) {
                        UIFactory.updatePerformanceDisplay(vbox, fps);
                        break;
                    }
                }
            }
        }
    }

    private void restartGame() {
        System.out.println("Restarting game...");
        System.out.println("Current game state before restart: " + stateManager.getCurrentState());
        
        // Find all game over related entities first
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getZIndex() >= 100) {  // Game over screens have high Z-index
                entitiesToRemove.add(entity);
            }
        }
        
        // Remove them outside the iteration to avoid concurrent modification
        for (Entity entity : entitiesToRemove) {
            entity.removeFromWorld();
            System.out.println("Removed entity with high Z-index: " + entity);
        }
        
        // Also check UI nodes
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node.getId() != null && 
               (node.getId().equals("game-over-screen") || 
                node.getId().equals("victory-screen"))) {
                nodesToRemove.add(node);
            }
        }
        
        // Remove UI nodes
        for (Node node : nodesToRemove) {
            FXGL.removeUINode(node);
            System.out.println("Removed UI node with ID: " + node.getId());
        }
        
        // Clear all entities
        entityManager.clear();
        
        // Reset all managers
        playerManager.reset();
        playerManager.resetHealth(); // Make sure health is fully reset with visual update
        inputManager.reset();
        waveManager.reset();
        
        // Force game state to PLAYING before starting the wave
        System.out.println("Current game state before reset: " + stateManager.getCurrentState());
        stateManager.startPlaying(null);
        System.out.println("Current game state after reset: " + stateManager.getCurrentState());
        
        // Small delay before starting a new game to ensure clean state
        FXGL.getGameTimer().runOnceAfter(() -> {
            // Start a new wave
            stateManager.announceWave(1);
            System.out.println("Wave announcement triggered, game restarted successfully");
        }, javafx.util.Duration.millis(200));
    }

    public static void main(String[] args) {
        launch(args);
    }
}