package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages keyboard input and typing functionality.
 * Implements the Command pattern for key actions.
 */
public class InputManager {
    
    private StringBuilder currentInput;
    private Entity selectedWordBlock;
    private final EntityManager entityManager;
    private final PlayerManager playerManager;
    private final GameStateManager stateManager;
    
    // Callback for when a game should be restarted
    private Consumer<Void> restartGameCallback;
    
    /**
     * Creates a new InputManager
     * 
     * @param entityManager The entity manager
     * @param playerManager The player manager
     * @param stateManager The game state manager
     */
    public InputManager(EntityManager entityManager, PlayerManager playerManager, GameStateManager stateManager) {
        this.entityManager = entityManager;
        this.playerManager = playerManager;
        this.stateManager = stateManager;
        this.currentInput = new StringBuilder();
    }
    
    /**
     * Sets up event handlers for keyboard input
     */
    public void setupInput() {
        // Handle key typing (letters, digits, etc.)
        FXGL.getInput().addEventHandler(KeyEvent.KEY_TYPED, this::handleKeyTyped);
        
        // Handle key presses (backspace, shift, space, enter)
        FXGL.getInput().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }
    
    /**
     * Handles key typed events (letters, digits, etc.)
     * 
     * @param event The key event
     */
    private void handleKeyTyped(KeyEvent event) {
        // Check if game is active
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            System.out.println("Not processing typing - game is not in PLAYING state: " + stateManager.getCurrentState());
            return;
        }
        
        // Skip empty characters or control characters (including Shift)
        if (event.getCharacter().isEmpty() || event.getCharacter().length() == 0 || 
            event.getCharacter().charAt(0) < 32) {
            return;
        }
        
        char typedChar = event.getCharacter().charAt(0);
        
        // Only process letter, digit, hyphen, or apostrophe
        if (Character.isLetterOrDigit(typedChar) || typedChar == '-' || typedChar == '\'') {
            System.out.println("Processing typed character: " + typedChar);
            processTypedCharacter(typedChar);
            
            // Consume the event to prevent it from bubbling up
            event.consume();
        }
    }
    
    /**
     * Handles key pressed events (backspace, shift, space, enter)
     * 
     * @param event The key event
     */
    private void handleKeyPressed(KeyEvent event) {
        // Handle retry on game over screen
        if (stateManager.isInState(GameStateManager.GameState.GAME_OVER) ||
                stateManager.isInState(GameStateManager.GameState.VICTORY)) {
            if ((event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) && 
                restartGameCallback != null) {
                System.out.println("Restarting game via keyboard...");
                restartGameCallback.accept(null);
                event.consume();
                return;
            }
            return;
        }
        
        // Don't process keys if not in playing state
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            return;
        }
        
        if (event.getCode() == KeyCode.BACK_SPACE && currentInput.length() > 0) {
            // Delete last character
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateLetterColors();
            event.consume(); // Consume backspace to prevent it from triggering browser back navigation
        } 
        // Shift key is now handled by the global event filter in Game.java
        else if (event.getCode() == KeyCode.SPACE) {
            // Complete word with Space
            checkWordCompletion();
            event.consume(); // Consume space to prevent default scrolling behavior
        }
    }
    
    /**
     * Processes a typed character
     * 
     * @param typedChar The character that was typed
     */
    private void processTypedCharacter(char typedChar) {
        if (selectedWordBlock == null) {
            System.out.println("No word block selected, attempting to find one...");
            // Select a word block if none is selected
            List<Entity> gargoyles = entityManager.getActiveGargoyles();
            System.out.println("Found " + gargoyles.size() + " active gargoyles");
            
            if (!gargoyles.isEmpty()) {
                // Find the most urgent gargoyle to target
                Entity mostUrgent = findMostUrgentGargoyle(gargoyles);
                if (mostUrgent != null) {
                    System.out.println("Selected most urgent gargoyle with word: " + mostUrgent.getString("word"));
                    selectWordBlock(mostUrgent);
                    // Now that we've selected a gargoyle, continue processing the character
                    // instead of returning
                }
                else {
                    System.out.println("No valid gargoyle found after searching");
                    return; // No valid gargoyles found
                }
            }
            else {
                System.out.println("No gargoyles available to select");
                return; // No gargoyles available
            }
        }
        
        try {
            String targetWord = selectedWordBlock.getString("word");
            System.out.println("Processing character '" + typedChar + "' for word '" + targetWord + "', current input: '" + currentInput.toString() + "'");
            
            // Make sure we're not exceeding the word length
            if (currentInput.length() >= targetWord.length()) {
                System.out.println("Input length exceeded target word length");
                return;
            }
            
            // Check if this would be a valid next character
            boolean isCorrect = typedChar == targetWord.charAt(currentInput.length());
            System.out.println("Character is " + (isCorrect ? "correct" : "incorrect"));
            
            // Record the keystroke in player stats
            playerManager.recordKeystroke(typedChar, isCorrect);
            
            if (isCorrect) {
                // Only add if it's correct (part of error trapping)
                currentInput.append(typedChar);
                updateLetterColors();
                System.out.println("Updated input to: '" + currentInput.toString() + "'");
                
                // Check if we've completed the word
                if (currentInput.length() == targetWord.length()) {
                    // Word is fully typed - make all letters blue for visual feedback
                    System.out.println("Word fully typed, marking as complete");
                    markWordAsComplete();
                }
            } else {
                // Wrong character - clear input but still count keystroke for consistency
                currentInput.setLength(0);
                resetToYellowHighlight();
                System.out.println("Incorrect character, reset input");
            }
        } catch (IllegalArgumentException e) {
            // If word property doesn't exist, ignore and wait for next update
            System.out.println("Error processing character: " + e.getMessage());
            currentInput.setLength(0);
        }
    }
    
    /**
     * Selects a word block for typing
     * 
     * @param wordBlock The word block to select
     */
    public void selectWordBlock(Entity wordBlock) {
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
    
    /**
     * Updates the letter colors based on current input
     */
    private void updateLetterColors() {
        if (selectedWordBlock == null) return;
        
        try {
            GargoyleFactory.updateLetterColors(selectedWordBlock, currentInput.toString());
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }
    
    /**
     * Marks the current word as complete
     */
    private void markWordAsComplete() {
        if (selectedWordBlock == null) return;
        
        try {
            GargoyleFactory.markWordAsComplete(selectedWordBlock);
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
        
        // The actual removal and selection of a new entity will happen in checkWordCompletion()
    }
    
    /**
     * Resets the selected block to yellow highlight
     */
    private void resetToYellowHighlight() {
        if (selectedWordBlock == null) return;
        
        try {
            GargoyleFactory.selectWordBlock(selectedWordBlock);
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }
    
    /**
     * Selects the next word block in sequence
     */
    private void selectNextWordBlock() {
        List<Entity> gargoyles = entityManager.getActiveGargoyles();
        if (gargoyles.isEmpty()) {
            System.out.println("No gargoyles available to cycle through");
            return;
        }
        
        System.out.println("Cycling through " + gargoyles.size() + " available gargoyles");
        
        // If no block is currently selected, just pick the most urgent one
        if (selectedWordBlock == null) {
            Entity mostUrgent = findMostUrgentGargoyle(gargoyles);
            if (mostUrgent != null) {
                selectWordBlock(mostUrgent);
                System.out.println("Selected most urgent gargoyle: " + mostUrgent.getString("word"));
            }
            return;
        }
        
        // Find the index of the currently selected gargoyle
        int currentIndex = -1;
        for (int i = 0; i < gargoyles.size(); i++) {
            if (gargoyles.get(i) == selectedWordBlock) {
                currentIndex = i;
                break;
            }
        }
        
        // Move to the next gargoyle in the list
        if (currentIndex != -1) {
            int nextIndex = (currentIndex + 1) % gargoyles.size();
            Entity nextGargoyle = gargoyles.get(nextIndex);
            selectWordBlock(nextGargoyle);
            System.out.println("Cycled from gargoyle #" + currentIndex + " to #" + nextIndex + 
                             ": " + nextGargoyle.getString("word"));
        } else {
            // Current selection not found in list, select the first one
            if (!gargoyles.isEmpty()) {
                Entity firstGargoyle = gargoyles.get(0);
                selectWordBlock(firstGargoyle);
                System.out.println("Selected first gargoyle: " + firstGargoyle.getString("word"));
            }
        }
    }
    
    /**
     * Checks if the current word is completed and processes it
     */
    private void checkWordCompletion() {
        if (selectedWordBlock == null) {
            System.out.println("Cannot check word completion - no block selected");
            return;
        }
        
        try {
            String targetWord = selectedWordBlock.getString("word");
            String typed = currentInput.toString();
            
            System.out.println("Checking word completion: typed='" + typed + "', target='" + targetWord + "'");
            
            // Check if the typed text matches the target word
            if (typed.equals(targetWord)) {
                System.out.println("Word completed successfully!");
                
                // Word completed successfully
                Entity completedBlock = selectedWordBlock;
                
                // Record the completed word
                int waveNumber = stateManager.isInState(GameStateManager.GameState.PLAYING) ? 1 : 0;
                playerManager.recordCompletedWord(targetWord, waveNumber);
                
                // Clear the selection before we remove the entity
                selectedWordBlock = null;
                
                // Get all active gargoyles BEFORE removing the completed one
                List<Entity> gargoyles = entityManager.getActiveGargoyles();
                gargoyles.remove(completedBlock);
                
                // Remove the completed block
                entityManager.removeEntity(completedBlock);
                System.out.println("Removed completed gargoyle, remaining gargoyles: " + gargoyles.size());
                
                // Select a new gargoyle if any available
                if (!gargoyles.isEmpty()) {
                    // Find the closest one to the right edge of the screen (most urgent)
                    Entity mostUrgent = findMostUrgentGargoyle(gargoyles);
                    if (mostUrgent != null) {
                        System.out.println("Selected new most urgent gargoyle: " + mostUrgent.getString("word"));
                        selectWordBlock(mostUrgent);
                    }
                } else {
                    System.out.println("No more gargoyles to select");
                }
            } else {
                // Word not completed - reset input and maintain yellow highlight
                System.out.println("Word does not match target, resetting input");
                currentInput.setLength(0);
                resetToYellowHighlight();
            }
        } catch (IllegalArgumentException e) {
            // Handle missing property gracefully
            System.out.println("Error checking word completion: " + e.getMessage());
            currentInput.setLength(0);
            
            // Try to select another entity if available
            List<Entity> gargoyles = entityManager.getActiveGargoyles();
            if (!gargoyles.isEmpty() && selectedWordBlock != null) {
                System.out.println("Trying to select another valid gargoyle");
                // Try to find any valid gargoyle to select
                for (Entity gargoyle : gargoyles) {
                    try {
                        // Verify this gargoyle has the word property
                        gargoyle.getString("word");
                        selectWordBlock(gargoyle);
                        System.out.println("Selected alternative gargoyle: " + gargoyle.getString("word"));
                        break;
                    } catch (Exception ex) {
                        // Skip this one if it has errors
                        continue;
                    }
                }
            }
        }
    }
    
    /**
     * Finds the most urgent gargoyle (closest to exiting the screen)
     * 
     * @param gargoyles List of active gargoyles
     * @return The most urgent gargoyle to target
     */
    private Entity findMostUrgentGargoyle(List<Entity> gargoyles) {
        if (gargoyles.isEmpty()) return null;
        
        Entity mostUrgent = null;
        double screenWidth = FXGL.getAppWidth();
        double minDistance = Double.MAX_VALUE;
        
        for (Entity gargoyle : gargoyles) {
            try {
                // Make sure it has required properties
                gargoyle.getString("word");
                boolean movingRight = gargoyle.getBoolean("movingRight");
                
                // Calculate how close it is to the edge it's moving toward
                double distance;
                if (movingRight) {
                    distance = screenWidth - gargoyle.getX();
                } else {
                    distance = gargoyle.getX();
                }
                
                // Keep the one with the smallest distance to edge
                if (distance < minDistance) {
                    minDistance = distance;
                    mostUrgent = gargoyle;
                }
            } catch (Exception e) {
                // Skip entities with issues
                continue;
            }
        }
        
        return mostUrgent;
    }
    
    /**
     * Gets the current typed input
     * 
     * @return The current input string
     */
    public String getCurrentInput() {
        return currentInput.toString();
    }
    
    /**
     * Gets the currently selected word block
     * 
     * @return The selected word block
     */
    public Entity getSelectedWordBlock() {
        return selectedWordBlock;
    }
    
    /**
     * Sets the callback for game restart
     * 
     * @param callback The restart game callback
     */
    public void setRestartGameCallback(Consumer<Void> callback) {
        this.restartGameCallback = callback;
    }
    
    /**
     * Resets input state
     */
    public void reset() {
        currentInput.setLength(0);
        selectedWordBlock = null;
    }
    
    /**
     * Public method to cycle to the next word block (for use by other components)
     */
    public void cycleToNextWordBlock() {
        selectNextWordBlock();
    }
} 