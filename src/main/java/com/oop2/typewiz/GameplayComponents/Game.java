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

/**
 * Main Game class implementing MVC pattern:
 * - Model: GameStateManager, EntityManager, PlayerManager, WaveManager
 * - View: UIFactory, GamePromptFactory, GargoyleFactory
 * - Controller: InputManager, Game (as coordinator)
 */
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
    
    // Manager instances (MVC components)
    private GameStateManager stateManager;     // Model
    private EntityManager entityManager;       // Model
    private WaveManager waveManager;           // Model
    private PlayerManager playerManager;       // Model
    private InputManager inputManager;         // Controller

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("TypeWiz - Winter Theme");
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setGameMenuEnabled(false);
        settings.setIntroEnabled(false);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(false);
    }

    @Override
    protected void initGame() {
        // Initialize managers first (Model and Controller components)
        initializeManagers();
        
        // Add global event filter to properly handle shift key
        setupGlobalKeyHandling();
        
        // Initialize the game environment (View setup)
        setupGameEnvironment();
        
        // Set up state handlers
        setupStateHandlers();
        
        // Start the first wave
        stateManager.announceWave(1);
    }
    
    /**
     * Sets up the game environment (View component)
     */
    private void setupGameEnvironment() {
        // Set up background and platform
        UIFactory.createBackground(FXGL.getAppWidth(), FXGL.getAppHeight());
        UIFactory.createPlatform();
        
        // Add the wizard character
        UIFactory.createWizard();
        
        // Initialize animations for entities
        GargoyleFactory.initializeAnimations();
        
        // Set up UI elements
        UIFactory.createUI(this);
    }
    
    /**
     * Initializes all manager classes (Model and Controller components)
     */
    private void initializeManagers() {
        // Create model components
        stateManager = new GameStateManager();
        entityManager = new EntityManager(FXGL.getAppWidth(), FXGL.getAppHeight());
        playerManager = new PlayerManager();
        
        // Create controller components
        inputManager = new InputManager(entityManager, playerManager, stateManager);
        
        // Create and configure the wave manager
        waveManager = new WaveManager(entityManager, stateManager, this::getRandomWordForWave, FXGL.getAppHeight());
        
        // Register managers in world properties for access from other components
        FXGL.getWorldProperties().setValue("playerManager", playerManager);
        FXGL.getWorldProperties().setValue("inputManager", inputManager);
        
        // Set up input handling
        inputManager.setupInput();
        inputManager.setRestartGameCallback(v -> restartGame());
    }
    
    /**
     * Word generator based on wave difficulty
     */
    private String getRandomWordForWave() {
        // Use the WordFactory to generate an appropriate word
        return WordFactory.getInstance().getWordForWave(waveManager.getCurrentWave());
    }

    /**
     * Sets up handlers for different game states (Controller component)
     */
    private void setupStateHandlers() {
        // Set actions for state transitions
        stateManager.setStateEntryAction(GameStateManager.GameState.WAVE_ANNOUNCEMENT, wave -> {
            // Start the wave to initialize parameters
            waveManager.startWave();
            
            // Reset any current input state to prepare for the new wave
            inputManager.reset();
            
            // Update the wave counter in the UI
            updateWaveUI(waveManager.getCurrentWave());
            
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
        
        stateManager.setStateEntryAction(GameStateManager.GameState.GAME_OVER, message -> 
            showEndGameScreen("Game Over!", false));
        
        stateManager.setStateEntryAction(GameStateManager.GameState.VICTORY, message -> 
            showEndGameScreen("Victory! Game Complete!", true));
    }
    
    /**
     * Shows game over or victory screen
     */
    private void showEndGameScreen(String title, boolean isVictory) {
        // Pass character count to statistics factory
        StatsUIFactory.setTotalCharactersTyped(playerManager.getTotalCharactersTyped());
        
        // Create game over screen with all statistics
        Node endGameScreen = GamePromptFactory.createGameOverScreen(
            title,
            playerManager.getScore(),
            playerManager.calculateWPM(),
            playerManager.calculateRawWPM(),
            playerManager.calculateAccuracy(),
            playerManager.calculateConsistency(),
            playerManager.getWpmOverTime(),
            playerManager.getAccuracyOverTime()
        );
        
        // Add a unique ID to the screen for easy identification
        endGameScreen.setId(isVictory ? "victory-screen" : "game-over-screen");
        
        // Set up the play again button
        GamePromptFactory.setupPlayAgainButton(endGameScreen, this::restartGame);
        
        FXGL.entityBuilder()
            .view(endGameScreen)
            .zIndex(100)
            .buildAndAttach();
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
        entityManager.updateEntities(tpf, waveManager.getCurrentWaveSpeedMultiplier());
        
        // Process entity removals
        entityManager.processRemovals();
        
        // Update performance display
        UIFactory.updatePerformanceDisplay(tpf);
    }

    /**
     * Restarts the game by resetting all components
     */
    private void restartGame() {
        // Remove game over or victory screens
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getZIndex() >= 100) {  // Game over screens have high Z-index
                entitiesToRemove.add(entity);
            }
        }
        
        // Remove them outside the iteration to avoid concurrent modification
        for (Entity entity : entitiesToRemove) {
            entity.removeFromWorld();
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
        }
        
        // Reset all managers
        entityManager.clear();
        playerManager.reset();
        playerManager.resetHealth();
        inputManager.reset();
        waveManager.reset();
        
        // Reset UI to wave 1
        updateWaveUI(1);
        
        // Force game state to PLAYING before starting the wave
        stateManager.startPlaying(null);
        
        // Small delay before starting a new game to ensure clean state
        FXGL.getGameTimer().runOnceAfter(() -> {
            // Start a new wave
            stateManager.announceWave(1);
        }, javafx.util.Duration.millis(200));
    }

    /**
     * Gets the player manager instance
     * @return The player manager
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the input manager instance
     * @return The input manager
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * Updates the wave counter in the UI
     * @param wave Current wave number
     */
    private void updateWaveUI(int wave) {
        // Find the top bar HBox in the UI
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof HBox) {
                HBox topBar = (HBox) node;
                
                // Check if this is the top bar by looking for wave display
                for (Node child : topBar.getChildren()) {
                    if (child instanceof VBox) {
                        VBox box = (VBox) child;
                        if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Text) {
                            Text label = (Text) box.getChildren().get(0);
                            if (label.getText().equals("WAVE")) {
                                // Found the wave display, update it
                                UIFactory.updateWave(topBar, wave);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets up global key event handling for special keys
     */
    private void setupGlobalKeyHandling() {
        // Add a global filter to intercept shift key events before they're processed by other handlers
        FXGL.getInput().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.SHIFT) {
                // If in playing state, cycle through targets
                if (stateManager.isInState(GameStateManager.GameState.PLAYING)) {
                    inputManager.cycleToNextWordBlock();
                    event.consume(); // Prevent event from being processed further
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}