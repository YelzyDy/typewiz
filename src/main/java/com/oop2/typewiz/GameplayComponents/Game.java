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
import com.almasb.fxgl.input.UserAction;
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
        GARGOYLE
    }
    
    private LocalTimer gargoyleSpawnTimer;
    private static final double GARGOYLE_SPAWN_INTERVAL = 2.0; // seconds between gargoyle spawns
    private static final int NUM_GARGOYLES = 5; // Number of gargoyles to spawn
    private static final int MAX_HEALTH = 100;
    private static final int HEALTH_LOSS_PER_MISS = 20;
    private static final double WAVE_PAUSE_TIME = 5.0; // 5 seconds pause between waves
    
    private int playerHealth = MAX_HEALTH;
    private Text healthText;
    private Rectangle healthBar;
    private List<Entity> activeGargoyles = new ArrayList<>();
    private boolean gameOver = false;
    private Entity gameOverScreen;
    private final Random random = new Random();
    
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
    
    // Typing game variables
    private List<String> wordList = Arrays.asList(
        "wizard", "magic", "spell", "potion", "wand", 
        "dragon", "scroll", "mystic", "arcane", "book",
        "winter", "frost", "ice", "snow", "cold",
        "storm", "blizzard", "crystal", "enchant", "rune",
        "power", "element", "mana", "fireball", "freeze"
    );
    private int currentTargetIndex = -1;
    private String currentTypedWord = "";
    private Text typingFeedbackText;
    
    // Gargoyle control variables
    private static final double GARGOYLE_SPEED = 300; // Movement speed for player-controlled gargoyle
    private static final double GARGOYLE_AUTO_SPEED = 80; // Automatic left movement speed
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    
    // Gargoyle animation assets
    private AnimationChannel gargoyleIdleAnimation;
    private AnimationChannel gargoyleAttackAnimation;
    private AnimationChannel gargoyleFlyAnimation;
    
    // Gargoyle dimensions
    private int gargoyleFrameWidth = 288;
    private int gargoyleFrameHeight = 312;
    private double gargoyleScale = 0.4;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("TypeWiz - Winter Theme");
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
                Duration.seconds(1), // Half second per frame
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
        
        // Set up gargoyle animations
        setupGargoyleAnimations();
        
        // Set up UI elements
        setupUI();
        
        // Initialize timers
        gargoyleSpawnTimer = FXGL.newLocalTimer();
        gargoyleSpawnTimer.capture();
        
        waveTimer = FXGL.newLocalTimer();
        waveTimer.capture();
        
        // Set up input handling
        setupInput();
        
        // Show initial wave message and start first wave
        showWaveStartMessage();
        
        // Run the game loop with improved performance
        FXGL.getGameTimer().runAtInterval(() -> {
            if (gameOver) return;
            
            // Handle wave completion
            if (waveCompleted) {
                if (waveTimer.elapsed(Duration.seconds(WAVE_PAUSE_TIME))) {
                    waveCompleted = false;
                    currentWave++;
                    waveText.setText("Wave: " + currentWave);
                    gargoyleSpawnTimer.capture();
                    waveInProgress = false;
                    showWaveStartMessage();
                }
                return;
            }
            
            if (playerHealth <= 0) {
                showGameOverScreen("Game Over!");
                return;
            }
            
            // Spawn gargoyles for current wave if not in progress
            if (!waveInProgress) {
                waveInProgress = true;
                startWave();
                return;
            }
            
            // Update all gargoyles
            updateGargoyles();
            
            // Check if we need to spawn more gargoyles
            if (activeGargoyles.size() < NUM_GARGOYLES && gargoyleSpawnTimer.elapsed(Duration.seconds(GARGOYLE_SPAWN_INTERVAL))) {
                spawnGargoyle();
                gargoyleSpawnTimer.capture();
            }
            
            // Check if wave is complete (based on score)
            if (score >= currentWave * 50 && waveInProgress) {
                waveCompleted = true;
                waveInProgress = false;
                waveTimer.capture();
                showWaveCompletionMessage();
            }
            
            // Update typing feedback text position if there's an active target
            updateTypingFeedbackPosition();
            
        }, Duration.seconds(0.016)); // ~60 fps
    }
    
    private void setupGargoyleAnimations() {
        // Load gargoyle image
        Image gargoyleImage = FXGL.image("mobs/gargoyle/gargoyle.png");
        
        // Create animation channel for idle animation (first row)
        gargoyleIdleAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.8), // Duration for complete animation cycle
                0, 2); // First row, frames 0-2
        
        // Create animation channel for attack animation (second row)
        gargoyleAttackAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.6), // Duration for complete animation cycle
                3, 5); // Second row, frames 3-5
        
        // Create animation channel for flying animation (third row)
        gargoyleFlyAnimation = new AnimationChannel(gargoyleImage, 
                3, // 3 frames per row
                gargoyleFrameWidth, gargoyleFrameHeight, 
                Duration.seconds(0.7), // Duration for complete animation cycle
                6, 8); // Third row, frames 6-8
    }
    
    private void startWave() {
        // Clear any remaining gargoyles
        for (Entity gargoyle : activeGargoyles) {
            gargoyle.removeFromWorld();
        }
        activeGargoyles.clear();
        
        // Spawn initial gargoyles for the wave
        for (int i = 0; i < NUM_GARGOYLES; i++) {
            spawnGargoyle();
        }
        
        // Hide instruction text
        instructionText.setVisible(false);
        
        // Reset current target index
        currentTargetIndex = -1;
        currentTypedWord = "";
        updateTypingFeedback();
        
        // Select first gargoyle as target if available
        if (!activeGargoyles.isEmpty()) {
            currentTargetIndex = 0;
            highlightCurrentTarget();
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
        
        // Add typing feedback text
        typingFeedbackText = new Text("");
        typingFeedbackText.setFill(Color.WHITE);
        typingFeedbackText.setFont(Font.font(22));
        typingFeedbackText.setVisible(false);
        
        // Add controls help text for typing mechanics
        Text controlsText = new Text("Type words to destroy gargoyles | SHIFT to switch targets | SPACE to submit | ENTER to restart");
        controlsText.setTranslateX(FXGL.getAppWidth() / 2 - 320);
        controlsText.setTranslateY(FXGL.getAppHeight() - 20);
        controlsText.setFill(Color.LIGHTGRAY);
        controlsText.setFont(Font.font(16));
        
        // Add UI elements to the scene
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(scoreDisplay);
        FXGL.addUINode(waveDisplay);
        FXGL.addUINode(instructionText);
        FXGL.addUINode(controlsText);
        FXGL.addUINode(typingFeedbackText);
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
    
    private void setupInput() {
        // Handle keyboard input for typing
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
            
            // Only process typing input when wave is in progress
            if (!waveInProgress || waveCompleted || activeGargoyles.isEmpty()) return;
            
            // Switch target with SHIFT
            if (event.getCode() == KeyCode.SHIFT) {
                switchTarget();
                return;
            }
            
            // Submit word with SPACE
            if (event.getCode() == KeyCode.SPACE) {
                submitWord();
                return;
            }
            
            // Backspace to remove last character
            if (event.getCode() == KeyCode.BACK_SPACE) {
                if (!currentTypedWord.isEmpty()) {
                    currentTypedWord = currentTypedWord.substring(0, currentTypedWord.length() - 1);
                    updateTypingFeedback();
                }
                return;
            }
            
            // Add character to current typed word if it's a letter
            String character = event.getText();
            if (!character.isEmpty() && Character.isLetter(character.charAt(0))) {
                currentTypedWord += character.toLowerCase();
                updateTypingFeedback();
            }
        });
    }
    
    private void spawnGargoyle() {
        // Create the animated texture with fly animation
        AnimatedTexture gargoyleTexture = new AnimatedTexture(gargoyleFlyAnimation);
        gargoyleTexture.loop();
        
        // Calculate a random Y position for the gargoyle
        double yPosition = 50 + random.nextDouble() * (FXGL.getAppHeight() - 150);
        
        // Get a random word for this gargoyle
        String word = wordList.get(random.nextInt(wordList.size()));
        
        // Create a text object for the word
        Text wordText = new Text(word);
        wordText.setFill(Color.WHITE);
        wordText.setFont(Font.font(18));
        wordText.setTextOrigin(javafx.geometry.VPos.TOP);
        
        // Add a backing rectangle for better visibility
        Rectangle textBack = new Rectangle(
                wordText.getLayoutBounds().getWidth() + 10,
                wordText.getLayoutBounds().getHeight() + 6,
                Color.color(0, 0, 0, 0.5));
        
        StackPane wordDisplay = new StackPane(textBack, wordText);
        wordDisplay.setTranslateY(-35); // Position further above gargoyle (adjusted from -25)
        
        // Create the gargoyle entity positioned on the right side
        Entity gargoyleEntity = FXGL.entityBuilder()
                .type(EntityType.GARGOYLE)
                .at(FXGL.getAppWidth(), yPosition) // Right side of screen
                .view(gargoyleTexture)
                .view(wordDisplay)
                .scale(gargoyleScale, gargoyleScale) // Scale down the gargoyle
                .bbox(new HitBox(BoundingShape.box(gargoyleFrameWidth * gargoyleScale, gargoyleFrameHeight * gargoyleScale)))
                .with("speed", GARGOYLE_AUTO_SPEED * (1 + (currentWave * 0.2))) // Speed increases with wave
                .with("word", word) // Store the word with the gargoyle
                .with("targeted", false) // Track if this gargoyle is the current target
                .zIndex(25)
                .buildAndAttach();
        
        // Add to active gargoyles list
        activeGargoyles.add(gargoyleEntity);
        
        // If this is the first gargoyle and no target is selected, make it the target
        if (currentTargetIndex == -1) {
            currentTargetIndex = 0;
            highlightCurrentTarget();
        }
    }
    
    private void updateGargoyles() {
        // Process all gargoyles
        for (int i = activeGargoyles.size() - 1; i >= 0; i--) {
            Entity gargoyle = activeGargoyles.get(i);
            
            // Move gargoyle to the left
            double speed = gargoyle.getDouble("speed");
            gargoyle.translateX(-speed * FXGL.tpf());
            
            // If gargoyle reaches the left edge, it's a miss
            if (gargoyle.getX() < -gargoyle.getWidth()) {
                decreaseHealth();
                
                // If this was the target, update target index
                if (i == currentTargetIndex) {
                    currentTargetIndex = -1;
                    currentTypedWord = "";
                    updateTypingFeedback();
                } else if (i < currentTargetIndex) {
                    // If a gargoyle before the current target is removed, adjust the index
                    currentTargetIndex--;
                }
                
                activeGargoyles.remove(i);
                gargoyle.removeFromWorld();
                
                // Find a new target if needed
                if (currentTargetIndex == -1 && !activeGargoyles.isEmpty()) {
                    currentTargetIndex = 0;
                    highlightCurrentTarget();
                }
            }
        }
    }
    
    private void destroyGargoyle(Entity gargoyle) {
        int index = activeGargoyles.indexOf(gargoyle);
        
        // Remove from active list
        activeGargoyles.remove(gargoyle);
        
        // Add to score based on wave and word length
        String word = gargoyle.getString("word");
        score += 10 * currentWave * Math.max(1, word.length() / 3);
        scoreText.setText(Integer.toString(score));
        
        // Remove from world
        gargoyle.removeFromWorld();
        
        // Update target index if needed
        if (index == currentTargetIndex) {
            // The current target was destroyed
            currentTypedWord = "";
            if (activeGargoyles.isEmpty()) {
                currentTargetIndex = -1;
                typingFeedbackText.setVisible(false);
            } else {
                // Select next target, wrapping around if needed
                currentTargetIndex = currentTargetIndex % activeGargoyles.size();
                highlightCurrentTarget();
            }
        } else if (index < currentTargetIndex) {
            // A gargoyle before the current target was destroyed, adjust index
            currentTargetIndex--;
        }
        
        updateTypingFeedback();
    }
    
    private void switchTarget() {
        if (activeGargoyles.isEmpty()) return;
        
        // Reset current gargoyle targeted status
        if (currentTargetIndex >= 0 && currentTargetIndex < activeGargoyles.size()) {
            Entity gargoyle = activeGargoyles.get(currentTargetIndex);
            gargoyle.getProperties().setValue("targeted", false);
        }
        
        // Move to next target, wrap around if at the end
        currentTargetIndex = (currentTargetIndex + 1) % activeGargoyles.size();
        
        // Reset typed word
        currentTypedWord = "";
        
        // Highlight the new target
        highlightCurrentTarget();
        updateTypingFeedback();
    }
    
    private void highlightCurrentTarget() {
        if (currentTargetIndex < 0 || currentTargetIndex >= activeGargoyles.size()) return;
        
        // Reset all gargoyles' targeted status
        for (Entity gargoyle : activeGargoyles) {
            gargoyle.getProperties().setValue("targeted", false);
            
            // Find the word text and update it to white (non-targeted color)
            StackPane wordView = (StackPane) gargoyle.getViewComponent().getChildren().get(1);
            Text wordText = (Text) wordView.getChildren().get(1);
            wordText.setFill(Color.WHITE);
            
            Rectangle backing = (Rectangle) wordView.getChildren().get(0);
            backing.setFill(Color.color(0, 0, 0, 0.5));
        }
        
        // Set current target
        Entity targetGargoyle = activeGargoyles.get(currentTargetIndex);
        targetGargoyle.getProperties().setValue("targeted", true);
        
        // Find the word text and update it to yellow (targeted color)
        StackPane wordView = (StackPane) targetGargoyle.getViewComponent().getChildren().get(1);
        Text wordText = (Text) wordView.getChildren().get(1);
        wordText.setFill(Color.YELLOW);
        
        Rectangle backing = (Rectangle) wordView.getChildren().get(0);
        backing.setFill(Color.color(0.2, 0.2, 0.6, 0.7));
        
        updateTypingFeedback();
    }
    
    private void updateTypingFeedback() {
        if (currentTargetIndex < 0 || currentTargetIndex >= activeGargoyles.size()) {
            typingFeedbackText.setVisible(false);
            return;
        }
        
        Entity targetGargoyle = activeGargoyles.get(currentTargetIndex);
        String targetWord = targetGargoyle.getString("word");
        
        // Create highlighted text for typed portion
        TextFlow textFlow = new TextFlow();
        
        // Check if what's been typed so far matches the target word
        boolean isCorrect = targetWord.startsWith(currentTypedWord);
        
        // Show the typed part in green if correct, red if incorrect
        if (!currentTypedWord.isEmpty()) {
            Text typedPart = new Text(currentTypedWord);
            typedPart.setFont(Font.font(22));
            typedPart.setFill(isCorrect ? Color.LIGHTGREEN : Color.RED);
            textFlow.getChildren().add(typedPart);
        }
        
        // Show the rest of the word if any is left to type
        if (currentTypedWord.length() < targetWord.length() && isCorrect) {
            Text remainingPart = new Text(targetWord.substring(currentTypedWord.length()));
            remainingPart.setFont(Font.font(22));
            remainingPart.setFill(Color.LIGHTGRAY);
            textFlow.getChildren().add(remainingPart);
        }
        
        // Update the typing feedback UI
        typingFeedbackText = new Text(textFlow.toString());
        typingFeedbackText.setVisible(true);
        
        // Position the feedback text below the target gargoyle
        updateTypingFeedbackPosition();
    }
    
    private void updateTypingFeedbackPosition() {
        if (currentTargetIndex < 0 || currentTargetIndex >= activeGargoyles.size()) {
            typingFeedbackText.setVisible(false);
            return;
        }
        
        Entity targetGargoyle = activeGargoyles.get(currentTargetIndex);
        
        // Position the typing feedback below the gargoyle
        double offsetX = targetGargoyle.getX() + (gargoyleFrameWidth * gargoyleScale) / 2 - 50;
        double offsetY = targetGargoyle.getY() + (gargoyleFrameHeight * gargoyleScale) + 20;
        
        typingFeedbackText.setTranslateX(offsetX);
        typingFeedbackText.setTranslateY(offsetY);
        typingFeedbackText.setVisible(true);
    }
    
    private void submitWord() {
        if (currentTargetIndex < 0 || currentTargetIndex >= activeGargoyles.size()) return;
        
        Entity targetGargoyle = activeGargoyles.get(currentTargetIndex);
        String targetWord = targetGargoyle.getString("word");
        
        // Check if the typed word matches the target
        if (currentTypedWord.equals(targetWord)) {
            destroyGargoyle(targetGargoyle);
        } else {
            // Reset typed word on incorrect submission
            currentTypedWord = "";
            updateTypingFeedback();
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
        score = 0;
        currentWave = 1;
        currentTargetIndex = -1;
        currentTypedWord = "";
        
        // Clear all gargoyles
        for (Entity entity : activeGargoyles) {
            entity.removeFromWorld();
        }
        activeGargoyles.clear();
        
        // Remove game over screen
        if (gameOverScreen != null) {
            gameOverScreen.removeFromWorld();
            gameOverScreen = null;
        }
        
        // Reset UI
        updateHealthBar();
        scoreText.setText("0");
        waveText.setText("1");
        typingFeedbackText.setVisible(false);
        
        // Show message for first wave
        showWaveStartMessage();
        
        // Reset timers
        gargoyleSpawnTimer.capture();
        waveTimer.capture();
    }

    public static void main(String[] args) {
        launch(args);
    }
}