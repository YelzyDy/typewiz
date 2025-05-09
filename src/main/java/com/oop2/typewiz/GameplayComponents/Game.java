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
import java.util.HashSet;
import java.util.Set;

// Define EntityType enum
enum EntityType {
    PLATFORM,
    PLAYER,
    MOVING_BLOCK
}

public class Game extends GameApplication {
    private LocalTimer blockSpawnTimer;
    private LocalTimer waveTimer;
    private LocalTimer continuousSpawnTimer; // New timer for continuous spawning
    private static final double BLOCK_SPEED = 100; // pixels per second
    private static final double SPAWN_INTERVAL = 3.0; // seconds between block spawns
    private static final double ROW_SPACING = 140; // Spacing between rows
    private static final int NUM_ROWS = 5; // Total number of rows
    private static final int MAX_HEALTH = 100;
    private static final int HEALTH_LOSS_PER_MISS = 20;
    private static final double WAVE_PAUSE_TIME = 5.0; // 5 seconds pause between waves
    private static final int MAX_WAVES = 10; // Maximum number of waves before game completion
    private static final double INITIAL_CONTINUOUS_SPAWN_TIME = 10.0; // Initial time between continuous spawns (seconds)
    
    private int playerHealth = MAX_HEALTH;
    private Text healthText;
    private Rectangle healthBar;
    private List<Entity> activeWordBlocks = new ArrayList<>();
    private Entity selectedWordBlock = null;
    private StringBuilder currentInput = new StringBuilder();
    private Text inputText;
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
        
        // Set up UI elements
        setupUI();
        
        // Initialize timers
        blockSpawnTimer = FXGL.newLocalTimer();
        blockSpawnTimer.capture();
        
        waveTimer = FXGL.newLocalTimer();
        waveTimer.capture();
        
        continuousSpawnTimer = FXGL.newLocalTimer(); // Initialize the continuous spawn timer
        continuousSpawnTimer.capture();
        
        // Set up input handling
        setupInput();
        
        // Show initial wave message and start first wave
        showWaveStartMessage();
        
        // Run the game loop
        FXGL.getGameTimer().runAtInterval(() -> {
            if (gameOver) return;
            
            // Handle wave completion
            if (waveCompleted) {
                if (waveTimer.elapsed(Duration.seconds(WAVE_PAUSE_TIME))) {
                    waveCompleted = false;
                    currentWave++;
                    
                    // Check if we've completed all waves
                    if (currentWave > MAX_WAVES) {
                        showGameOverScreen("Game Complete!");
                        return;
                    }
                    
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
            
            // Check for continuous spawning within a wave
            double continuousSpawnInterval = getContinuousSpawnInterval();
            if (continuousSpawnTimer.elapsed(Duration.seconds(continuousSpawnInterval))) {
                spawnAdditionalBlocks();
                continuousSpawnTimer.capture();
            }
            
            // Update all word blocks
            List<Entity> blocksToRemove = new ArrayList<>();
            
            for (Entity entity : activeWordBlocks) {
                // Increase speed based on current wave
                double blockSpeedForWave = BLOCK_SPEED + (currentWave * 5);
                entity.translateX(-blockSpeedForWave * FXGL.tpf());
                
                // If block reaches the left edge, it's a miss
                if (entity.getX() < 0) {
                    decreaseHealth();
                    blocksToRemove.add(entity);
                    entity.removeFromWorld();
                }
            }
            
            // Remove blocks that have gone off screen
            activeWordBlocks.removeAll(blocksToRemove);
            
            // If the selected block was removed, select the next one
            if (blocksToRemove.contains(selectedWordBlock) && !activeWordBlocks.isEmpty()) {
                selectWordBlock(activeWordBlocks.get(0));
            } else if (blocksToRemove.contains(selectedWordBlock)) {
                selectedWordBlock = null;
            }
            
            // Update input display
            updateInputDisplay();
            
            // Check if wave is complete (no blocks left)
            if (activeWordBlocks.isEmpty() && waveInProgress) {
                waveCompleted = true;
                waveInProgress = false;
                waveTimer.capture();
                // Show wave completion message
                showWaveCompletionMessage();
            }
            
        }, Duration.seconds(0.016)); // ~60 fps
    }
    
    // Get spawn interval based on current wave (gets faster with each wave)
    private double getContinuousSpawnInterval() {
        // Start at 10 seconds, decrease by 0.5 seconds per wave (minimum 2 seconds)
        return Math.max(2.0, INITIAL_CONTINUOUS_SPAWN_TIME - (currentWave - 1) * 0.5);
    }
    
    // Method to spawn additional blocks during a wave
    private void spawnAdditionalBlocks() {
        // Keep original bottom row position
        double bottomRowY = FXGL.getAppHeight() - 120;
        
        // Calculate a fixed horizontal position for alignment
        double fixedX = FXGL.getAppWidth() + 10;
        
        // Spawn a random number of blocks (1-3) in random rows
        int blocksToSpawn = 1 + random.nextInt(Math.min(3, currentWave));
        
        // Keep track of which rows we've used to avoid overlap
        Set<Integer> usedRows = new HashSet<>();
        
        for (int i = 0; i < blocksToSpawn; i++) {
            // Try to find an unused row
            int attempts = 0;
            int rowIndex;
            
            // Try up to 10 times to find an unused row
            do {
                rowIndex = random.nextInt(NUM_ROWS);
                attempts++;
            } while (usedRows.contains(rowIndex) && attempts < 10);
            
            // If all rows are used, skip this block spawn
            if (usedRows.contains(rowIndex)) {
                continue;
            }
            
            usedRows.add(rowIndex);
            double rowY = bottomRowY - (rowIndex * ROW_SPACING);
            
            // Check if there are existing blocks close to this position
            boolean canSpawn = true;
            
            for (Entity existingBlock : activeWordBlocks) {
                // Check if the block is in the same row
                if (Math.abs(existingBlock.getY() - rowY) < 10) {
                    // Check if there's overlap (blocks are too close horizontally)
                    // Minimum 150px horizontal spacing
                    if (fixedX - existingBlock.getX() < 150) {
                        canSpawn = false;
                        break;
                    }
                }
            }
            
            if (canSpawn) {
                Entity wordBlock = spawnWordBlock(rowY, rowIndex, fixedX);
                activeWordBlocks.add(wordBlock);
                
                // If this is the first block and no block is selected, select it
                if (selectedWordBlock == null && i == 0) {
                    selectWordBlock(wordBlock);
                }
            }
        }
    }
    
    private void startWave() {
        // Keep original bottom row position
        double bottomRowY = FXGL.getAppHeight() - 120;
        
        // Calculate a fixed horizontal position for alignment
        double fixedX = FXGL.getAppWidth() + 10;
        
        // Spawn blocks in all rows with vertical alignment
        for (int i = 0; i < NUM_ROWS; i++) {
            // No more skipping rows in early waves
            double rowY = bottomRowY - (i * ROW_SPACING);
            
            // Add horizontal offset to stagger the blocks
            double offsetX = fixedX + (i * 100); // Add 100px offset per row to prevent vertical stacking
            
            Entity wordBlock = spawnWordBlock(rowY, i, offsetX);
            activeWordBlocks.add(wordBlock);
        }
        
        // Select the first word block by default if none is selected
        if (selectedWordBlock == null && !activeWordBlocks.isEmpty()) {
            selectWordBlock(activeWordBlocks.get(0));
        }
        
        // Hide instruction text
        instructionText.setVisible(false);
        
        // Reset the continuous spawn timer when starting a new wave
        continuousSpawnTimer.capture();
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
        // This method is no longer needed as we show the text directly above blocks
        // But we keep an empty implementation to avoid errors in existing code
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
                        updateSelectedWordDisplay();
                        
                        // Check if we've completed the word
                        if (currentInput.length() == targetWord.length()) {
                            // Visual feedback that word is complete
                            Node view = selectedWordBlock.getViewComponent().getChildren().get(0);
                            if (view instanceof StackPane) {
                                StackPane stackPane = (StackPane) view;
                                for (Node child : stackPane.getChildren()) {
                                    if (child instanceof TextFlow) {
                                        for (Node letter : ((TextFlow)child).getChildren()) {
                                            if (letter instanceof Text) {
                                                ((Text)letter).setFill(Color.BLUE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Wrong character - clear input and reset display (stronger error trapping)
                        currentInput.setLength(0);
                        createYellowHighlight();
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
                updateSelectedWordDisplay();
            } else if (event.getCode() == KeyCode.SHIFT) {
                // Switch between blocks with Shift
                selectNextWordBlock();
                // Explicitly ensure highlight is updated
                updateSelectedWordDisplay();
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                // Complete word with Space
                checkWordCompletion();
                event.consume();
            }
        });
    }
    
    private void selectWordBlock(Entity wordBlock) {
        // Deselect the previous block
        if (selectedWordBlock != null) {
            // Reset the word to white
            Node view = selectedWordBlock.getViewComponent().getChildren().get(0);
            if (view instanceof StackPane) {
                StackPane stackPane = (StackPane) view;
                
                // Remove any fancy text flow
                for (int i = stackPane.getChildren().size() - 1; i >= 0; i--) {
                    Node child = stackPane.getChildren().get(i);
                    if (child instanceof TextFlow) {
                        stackPane.getChildren().remove(child);
                    }
                }
                
                // Restore simple white text
                Text whiteText = new Text(selectedWordBlock.getString("word"));
                whiteText.setFont(Font.font(22));
                whiteText.setFill(Color.WHITE);
                whiteText.setTranslateY(-25);
                whiteText.setTextAlignment(TextAlignment.CENTER);
                
                stackPane.getChildren().add(whiteText);
                selectedWordBlock.setProperty("wordText", whiteText);
            }
        }
        
        // Select the new block
        selectedWordBlock = wordBlock;
        
        // Always reset input when switching blocks
        currentInput.setLength(0);
        
        // Create initial yellow highlight for the selected block
        createYellowHighlight();
    }
    
    // New method to create yellow highlight for selected block
    private void createYellowHighlight() {
        if (selectedWordBlock == null) return;
        
        String targetWord = selectedWordBlock.getString("word");
        
        // Get the view container
        Node view = selectedWordBlock.getViewComponent().getChildren().get(0);
        if (!(view instanceof StackPane)) return;
        
        StackPane stackPane = (StackPane) view;
        
        // Remove existing texts
        for (int i = stackPane.getChildren().size() - 1; i >= 0; i--) {
            Node child = stackPane.getChildren().get(i);
            if (child instanceof Text || child instanceof TextFlow) {
                stackPane.getChildren().remove(child);
            }
        }
        
        // Create a new TextFlow for multi-colored text
        TextFlow textFlow = new TextFlow();
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow.setTranslateY(-25);
        
        // Add all letters in yellow (selected but not typed)
        for (int i = 0; i < targetWord.length(); i++) {
            Text letterText = new Text(String.valueOf(targetWord.charAt(i)));
            letterText.setFont(Font.font(22));
            letterText.setFill(Color.YELLOW);
            textFlow.getChildren().add(letterText);
        }
        
        // Add the TextFlow to the stackPane
        stackPane.getChildren().add(textFlow);
    }
    
    private void selectNextWordBlock() {
        if (activeWordBlocks.isEmpty()) return;
        
        int currentIndex = activeWordBlocks.indexOf(selectedWordBlock);
        int nextIndex = (currentIndex + 1) % activeWordBlocks.size();
        selectWordBlock(activeWordBlocks.get(nextIndex));
    }
    
    private void checkWordCompletion() {
        if (selectedWordBlock == null) return;
        
        String targetWord = selectedWordBlock.getString("word");
        String typed = currentInput.toString();
        
        // Check if the typed text matches the target word
        if (typed.equals(targetWord)) {
            // Word completed successfully
            Entity completedBlock = selectedWordBlock;
            activeWordBlocks.remove(completedBlock);
            completedBlock.removeFromWorld();
            
            // Calculate score based on word length and current wave
            int wordScore = targetWord.length() * 10 * currentWave;
            score += wordScore;
            scoreText.setText(Integer.toString(score));
            
            // Select next block if available
            if (!activeWordBlocks.isEmpty()) {
                selectWordBlock(activeWordBlocks.get(0));
            } else {
                selectedWordBlock = null;
            }
        }
    }
    
    private void updateSelectedWordDisplay() {
        if (selectedWordBlock == null) return;
         
        String targetWord = selectedWordBlock.getString("word");
        String typed = currentInput.toString();
        
        // Safety check - if the typed text doesn't match the beginning of the target word,
        // reset the input and restart
        boolean mismatch = false;
        for (int i = 0; i < typed.length(); i++) {
            if (i >= targetWord.length() || typed.charAt(i) != targetWord.charAt(i)) {
                mismatch = true;
                break;
            }
        }
        
        if (mismatch) {
            // Clear input and reset display if there's a mismatch
            currentInput.setLength(0);
            typed = "";
            createYellowHighlight();
            return;
        }
         
        // Get the view container
        Node view = selectedWordBlock.getViewComponent().getChildren().get(0);
        if (!(view instanceof StackPane)) return;
        
        StackPane stackPane = (StackPane) view;
        
        // Remove existing texts
        for (int i = stackPane.getChildren().size() - 1; i >= 0; i--) {
            Node child = stackPane.getChildren().get(i);
            if (child instanceof Text || child instanceof TextFlow) {
                stackPane.getChildren().remove(child);
            }
        }
        
        // Create a new TextFlow for multi-colored text
        TextFlow textFlow = new TextFlow();
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow.setTranslateY(-25);
        
        // Add letter by letter with appropriate colors
        for (int i = 0; i < targetWord.length(); i++) {
            Text letterText = new Text(String.valueOf(targetWord.charAt(i)));
            letterText.setFont(Font.font(22));
            
            // Letters that have been typed are blue, the rest are yellow
            if (i < typed.length()) {
                letterText.setFill(Color.BLUE);
            } else {
                letterText.setFill(Color.YELLOW);
            }
            
            textFlow.getChildren().add(letterText);
        }
        
        // Add the TextFlow to the stackPane
        stackPane.getChildren().add(textFlow);
    }
    
    private Entity spawnWordBlock(double yPosition, int rowIndex, double xPosition) {
        // Create a block that will move from right to left
        double blockSize = 40;
        
        // Pick a random word from the list based on current wave
        String word;
        if (currentWave <= 3) {
            word = wordList.get(random.nextInt(wordList.size()));
        } else if (currentWave <= 7) {
            word = random.nextBoolean() 
                ? wordList.get(random.nextInt(wordList.size())) 
                : mediumWordList.get(random.nextInt(mediumWordList.size()));
        } else {
            // Higher chance of hard words in later waves
            double rand = random.nextDouble();
            if (rand < 0.2) {
                word = wordList.get(random.nextInt(wordList.size()));
            } else if (rand < 0.5) {
                word = mediumWordList.get(random.nextInt(mediumWordList.size()));
            } else {
                word = hardWordList.get(random.nextInt(hardWordList.size()));
            }
        }
        
        // Use slightly different colors for different rows
        Color blockColor;
        if (rowIndex == 0) {
            blockColor = Color.rgb(200, 200, 220); // Original bottom row color
        } else if (rowIndex == 1) {
            blockColor = Color.rgb(190, 210, 230); // Slightly bluer
        } else if (rowIndex == 2) {
            blockColor = Color.rgb(180, 220, 240); // More blue
        } else if (rowIndex == 3) {
            blockColor = Color.rgb(170, 230, 250); // Even more blue
        } else {
            blockColor = Color.rgb(160, 240, 255); // Very blue
        }
        
        Rectangle block = new Rectangle(blockSize, blockSize, blockColor);
        
        // Create text for the word - make it larger
        Text wordText = new Text(word);
        wordText.setFont(Font.font(22)); // Increased from 16
        wordText.setFill(Color.WHITE); // Default color is white
        wordText.setTranslateY(-25); // Position above the block - moved up a bit
        wordText.setTextAlignment(TextAlignment.CENTER);
        
        // Create a stack pane to hold both the block and text
        StackPane view = new StackPane(block, wordText);
        view.setAlignment(Pos.CENTER);
        
        Entity blockEntity = FXGL.entityBuilder()
                .type(EntityType.MOVING_BLOCK)
                .at(xPosition, yPosition)
                .view(view)
                .bbox(new HitBox(BoundingShape.box(blockSize, blockSize)))
                .with("word", word)
                .with("wordText", wordText)
                .zIndex(30)
                .buildAndAttach();
        
        return blockEntity;
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
        Text waveText = new Text("Waves completed: " + (currentWave >= MAX_WAVES ? MAX_WAVES : currentWave));
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
        
        // Clear all word blocks
        for (Entity entity : activeWordBlocks) {
            entity.removeFromWorld();
        }
        activeWordBlocks.clear();
        selectedWordBlock = null;
        
        // Remove game over screen
        if (gameOverScreen != null) {
            gameOverScreen.removeFromWorld();
            gameOverScreen = null;
        }
        
        // Reset UI
        updateHealthBar();
        updateInputDisplay();
        scoreText.setText("0");
        waveText.setText("1");
        
        // Show message for first wave
        showWaveStartMessage();
        
        // Reset timers
        blockSpawnTimer.capture();
        waveTimer.capture();
        continuousSpawnTimer.capture(); // Reset continuous spawn timer
    }

    public static void main(String[] args) {
        launch(args);
    }
}
