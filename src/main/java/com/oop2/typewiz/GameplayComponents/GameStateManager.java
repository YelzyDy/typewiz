package com.oop2.typewiz.GameplayComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages the different states of the game and handles transitions between them.
 * This class implements the State Pattern to separate state-specific behaviors
 * from the main Game class.
 */
public class GameStateManager {
    
    /**
     * Enum representing the different possible game states
     */
    public enum GameState {
        PLAYING,           // Active gameplay
        WAVE_ANNOUNCEMENT, // Announcing a new wave
        WAVE_COMPLETED,    // Wave has been completed
        GAME_OVER,         // Game over (player died)
        VICTORY            // Player completed all waves
    }
    
    private GameState currentState;
    private Map<GameState, Consumer<Object>> stateEntryActions;
    private Map<GameState, Consumer<Object>> stateExitActions;
    
    /**
     * Creates a new GameStateManager with PLAYING as the initial state
     */
    public GameStateManager() {
        this.currentState = GameState.PLAYING;
        this.stateEntryActions = new HashMap<>();
        this.stateExitActions = new HashMap<>();
    }
    
    /**
     * Gets the current game state
     * @return The current GameState
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Checks if the game is in the specified state
     * @param state The state to check
     * @return true if the current state matches the specified state
     */
    public boolean isInState(GameState state) {
        return currentState == state;
    }
    
    /**
     * Transitions to a new game state
     * 
     * @param newState The state to transition to
     * @param context Optional context object to pass to entry/exit actions
     */
    public void transitionTo(GameState newState, Object context) {
        if (newState == currentState) {
            return; // Already in this state
        }
        
        // Execute exit action for current state if defined
        if (stateExitActions.containsKey(currentState)) {
            stateExitActions.get(currentState).accept(context);
        }
        
        // Change state
        GameState oldState = currentState;
        currentState = newState;
        
        // Execute entry action for new state if defined
        if (stateEntryActions.containsKey(newState)) {
            stateEntryActions.get(newState).accept(context);
        }
        
        System.out.println("Game state changed from " + oldState + " to " + newState);
    }
    
    /**
     * Sets an action to be executed when entering a specific state
     * 
     * @param state The state to set the entry action for
     * @param action The action to execute
     */
    public void setStateEntryAction(GameState state, Consumer<Object> action) {
        stateEntryActions.put(state, action);
    }
    
    /**
     * Sets an action to be executed when exiting a specific state
     * 
     * @param state The state to set the exit action for
     * @param action The action to execute
     */
    public void setStateExitAction(GameState state, Consumer<Object> action) {
        stateExitActions.put(state, action);
    }
    
    /**
     * Transitions to the game over state
     * @param context Optional context object to pass to entry/exit actions
     */
    public void gameOver(Object context) {
        transitionTo(GameState.GAME_OVER, context);
    }
    
    /**
     * Transitions to the victory state
     * @param context Optional context object to pass to entry/exit actions
     */
    public void victory(Object context) {
        transitionTo(GameState.VICTORY, context);
    }
    
    /**
     * Transitions to the wave announcement state
     * @param context Optional context object to pass to entry/exit actions
     */
    public void announceWave(Object context) {
        transitionTo(GameState.WAVE_ANNOUNCEMENT, context);
    }
    
    /**
     * Transitions to the wave completed state
     * @param context Optional context object to pass to entry/exit actions
     */
    public void completeWave(Object context) {
        transitionTo(GameState.WAVE_COMPLETED, context);
    }
    
    /**
     * Transitions to the playing state
     * @param context Optional context object to pass to entry/exit actions
     */
    public void startPlaying(Object context) {
        transitionTo(GameState.PLAYING, context);
    }
} 