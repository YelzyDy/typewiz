package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Manages game waves including spawning, progression, and difficulty scaling.
 * Implements the Strategy pattern for different wave behaviors.
 */
public class WaveManager {
    
    // Wave settings
    private static final int MAX_WAVES = 10;
    private static final double WAVE_SPAWN_DELAY = 7.0; // seconds between spawn groups
    private static final double SPAWN_SPEED_INCREASE = 0.9; // each group spawns 10% faster than the last
    private static final double SCREEN_MARGIN = 150;
    
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
    
    private int currentWave;
    private boolean waveInProgress;
    private boolean isSpawningWave;
    private int totalWaveSpawns;
    private int currentGroupSize;
    private double currentSpawnDelay;
    private boolean spawnFromRight;
    
    private LocalTimer waveSpawnTimer;
    private final Random random;
    private final EntityManager entityManager;
    private final GameStateManager stateManager;
    
    // Function to get random words based on wave difficulty
    private Supplier<String> wordSupplier;
    
    private double minY;
    private double maxY;
    private double spawnPerimeterRight;
    
    /**
     * Creates a new WaveManager
     * 
     * @param entityManager The entity manager
     * @param stateManager The game state manager
     * @param wordSupplier Function that supplies random words
     * @param screenHeight Height of the game screen
     */
    public WaveManager(EntityManager entityManager, GameStateManager stateManager, 
                        Supplier<String> wordSupplier, double screenHeight) {
        this.entityManager = entityManager;
        this.stateManager = stateManager;
        this.wordSupplier = wordSupplier;
        this.random = new Random();
        
        // Initialize wave state
        this.currentWave = 1;
        this.waveInProgress = false;
        this.isSpawningWave = false;
        
        // Initialize spawn area
        this.spawnPerimeterRight = 100; // Distance from right edge where entities spawn
        this.minY = SCREEN_MARGIN;
        this.maxY = screenHeight - SCREEN_MARGIN;
        
        // Initialize timers
        this.waveSpawnTimer = FXGL.newLocalTimer();
        this.waveSpawnTimer.capture();
    }
    
    /**
     * Gets the current wave number
     * 
     * @return The current wave number (1-based)
     */
    public int getCurrentWave() {
        return currentWave;
    }
    
    /**
     * Gets the maximum number of waves
     * 
     * @return The maximum number of waves
     */
    public int getMaxWaves() {
        return MAX_WAVES;
    }
    
    /**
     * Checks if all waves have been completed
     * 
     * @return true if all waves are completed
     */
    public boolean areAllWavesCompleted() {
        return currentWave > MAX_WAVES;
    }
    
    /**
     * Starts a new wave
     */
    public void startWave() {
        // Check if all waves are completed
        if (areAllWavesCompleted()) {
            stateManager.victory(null);
            return;
        }
        
        // Remove all existing gargoyles before starting a new wave
        entityManager.removeAllEntitiesOfType(Game.EntityType.GARGOYLE);
        
        // Get wave index (0-based)
        int waveIndex = currentWave - 1;
        
        // Reset wave spawning state with wave-specific parameters
        isSpawningWave = false; // Start as false, set to true after announcement
        spawnFromRight = true; // Always spawn from right side
        
        // Apply wave-specific difficulty settings
        int minSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        int maxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        
        System.out.println("Wave " + currentWave + " settings: minSpawns=" + minSpawns + ", maxSpawns=" + maxSpawns);
        
        // Cap max spawns based on screen height to prevent overcrowding
        double availableHeight = maxY - minY;
        int maxPossibleSpawns = Math.max(1, (int)(availableHeight / (SCREEN_MARGIN * 0.3)));
        
        // Adjust group size if necessary
        maxSpawns = Math.min(maxSpawns, maxPossibleSpawns);
        minSpawns = Math.min(minSpawns, maxSpawns);
        
        System.out.println("After adjustment: minSpawns=" + minSpawns + ", maxSpawns=" + maxSpawns + ", maxPossibleSpawns=" + maxPossibleSpawns);
        
        // Set the current group size
        int newGroupSize;
        if (minSpawns == maxSpawns) {
            newGroupSize = minSpawns;
        } else {
            newGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
        }
        currentGroupSize = newGroupSize;
        
        System.out.println("Set currentGroupSize to " + currentGroupSize);
        
        // Set total spawns for this wave
        totalWaveSpawns = WAVE_SPAWNS_PER_WAVE[waveIndex];
        
        // Apply wave-specific spawn delay
        currentSpawnDelay = WAVE_SPAWN_DELAY * SPAWN_DELAY_MULTIPLIERS[waveIndex];
        
        waveSpawnTimer.capture();
        waveInProgress = true;
        
        System.out.println("Starting wave " + currentWave + " with " + totalWaveSpawns + 
                " total spawns, speed multiplier " + WAVE_SPEED_MULTIPLIERS[waveIndex] + ", group size " + currentGroupSize);
    }
    
    /**
     * Updates the wave spawning logic
     * 
     * @return true if wave is completed
     */
    public boolean update() {
        // Don't do anything if the wave is not in progress or not spawning
        if (!waveInProgress || !isSpawningWave) {
            return false;
        }
        
        // Check if we need to spawn a new group
        List<Entity> activeGargoyles = entityManager.getActiveGargoyles();
        if (activeGargoyles.isEmpty() || waveSpawnTimer.elapsed(Duration.seconds(currentSpawnDelay))) {
            if (totalWaveSpawns > 0) {
                spawnGroup();
            } else if (activeGargoyles.isEmpty()) {
                // Wave completed
                waveCompleted();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Spawns a group of entities
     */
    private List<Entity> spawnGroup() {
        // Get wave index (0-based)
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
        
        // Ensure we don't try to spawn 0 entities
        if (currentGroupSize <= 0) {
            int minSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
            int maxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
            if (minSpawns == maxSpawns) {
                currentGroupSize = minSpawns;
            } else {
                currentGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
            }
            System.out.println("Fixed zero group size to: " + currentGroupSize);
        }
        
        System.out.println("Spawning group of " + currentGroupSize + " gargoyles from right side");
        
        // Use GargoyleFactory to spawn a group of gargoyles
        List<Entity> spawnedGargoyles = GargoyleFactory.spawnGargoyleGroup(
            currentGroupSize,
            minY,
            maxY,
            spawnFromRight, // Always spawn from right
            spawnPerimeterRight,
            wordSupplier, // Method reference to get random words
            Game.EntityType.GARGOYLE
        );
        
        System.out.println("GargoyleFactory returned " + spawnedGargoyles.size() + " entities");
        
        // Add spawned gargoyles to entity manager and ensure they're attached to the world
        for (Entity gargoyle : spawnedGargoyles) {
            System.out.println("Adding gargoyle to entity manager with word: " + 
                (gargoyle.getProperties().exists("word") ? gargoyle.getString("word") : "unknown"));
                
            entityManager.addActiveEntity(gargoyle);
            
            // Ensure the entity is attached to the world
            if (!gargoyle.isActive()) {
                System.out.println("Attaching gargoyle to world");
                FXGL.getGameWorld().addEntity(gargoyle);
            }
        }
        
        // Check if we need to select a new gargoyle automatically
        // This happens in two cases:
        // 1. This is the first group of a wave (already handled in startSpawning)
        // 2. The player has defeated all previous gargoyles and this is a new group
        InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
        if (inputManager != null && inputManager.getSelectedWordBlock() == null && !spawnedGargoyles.isEmpty()) {
            // We only want to select one if there are no other active selections
            Entity closestGargoyle = GargoyleFactory.findClosestGargoyleToCenter(spawnedGargoyles);
            if (closestGargoyle != null) {
                System.out.println("WaveManager: Automatically selecting new gargoyle after spawn");
                inputManager.selectWordBlock(closestGargoyle);
            }
        }
        
        totalWaveSpawns -= spawnedGargoyles.size();
        
        System.out.println("Successfully spawned " + spawnedGargoyles.size() + 
                " gargoyles, " + totalWaveSpawns + " remaining in wave");
        
        // Prepare for next group with wave-specific parameters
        int waveMinSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        int waveMaxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
        
        // Calculate next group size
        if (waveMinSpawns == waveMaxSpawns) {
            currentGroupSize = waveMinSpawns;
        } else {
            currentGroupSize = random.nextInt(waveMaxSpawns - waveMinSpawns + 1) + waveMinSpawns;
        }
        
        // Apply wave-specific delay reduction for next spawn
        currentSpawnDelay *= SPAWN_SPEED_INCREASE * SPAWN_DELAY_MULTIPLIERS[waveIndex];
        
        waveSpawnTimer.capture();
        
        return spawnedGargoyles;
    }
    
    /**
     * Handles wave completion
     */
    private void waveCompleted() {
        waveInProgress = false;
        isSpawningWave = false;
        
        // Increment wave number
        currentWave++;
        
        // Notify game state manager of wave completion
        stateManager.completeWave(null);
    }
    
    /**
     * Starts spawning for the current wave
     */
    public void startSpawning() {
        System.out.println("WaveManager: startSpawning called");
        
        // Make sure the wave is properly started first
        if (!waveInProgress) {
            System.out.println("WaveManager: Wave not in progress, starting wave first");
            startWave();
        }
        
        // Ensure group size is not zero
        if (currentGroupSize <= 0) {
            int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
            int minSpawns = MIN_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
            int maxSpawns = MAX_SPAWNS_PER_GROUP_BY_WAVE[waveIndex];
            currentGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
            System.out.println("WaveManager: Corrected group size to: " + currentGroupSize);
        }
        
        isSpawningWave = true;
        waveSpawnTimer.capture();
        
        System.out.println("WaveManager: Spawning first group of gargoyles, size=" + currentGroupSize);
        
        // Spawn first group right away
        List<Entity> spawned = spawnGroup();
        
        System.out.println("WaveManager: Spawned first group with " + spawned.size() + 
                " gargoyles, total remaining: " + totalWaveSpawns);
        
        // Automatically select the first gargoyle (closest to center) for targeting
        if (!spawned.isEmpty()) {
            // Find the closest gargoyle to the center of the screen
            Entity closestGargoyle = GargoyleFactory.findClosestGargoyleToCenter(spawned);
            
            // Get input manager from world properties and select the gargoyle
            InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
            if (inputManager != null && closestGargoyle != null) {
                System.out.println("WaveManager: Automatically selecting first gargoyle");
                inputManager.selectWordBlock(closestGargoyle);
            }
        }
    }
    
    /**
     * Gets the speed multiplier for the current wave
     * 
     * @return The speed multiplier
     */
    public double getCurrentWaveSpeedMultiplier() {
        int waveIndex = Math.min(currentWave - 1, MAX_WAVES - 1);
        return WAVE_SPEED_MULTIPLIERS[waveIndex];
    }
    
    /**
     * Resets the wave manager for a new game
     */
    public void reset() {
        currentWave = 1;
        waveInProgress = false;
        isSpawningWave = false;
    }

    /**
     * Returns a random word appropriate for the current wave's difficulty
     * @param currentWave The current wave number
     * @param easyWords List of easy words
     * @param mediumWords List of medium words
     * @param hardWords List of hard words
     * @return A randomly selected word with difficulty appropriate for the current wave
     */
    public static String getRandomWordForCurrentWave(int currentWave, List<String> easyWords, 
                                                        List<String> mediumWords, List<String> hardWords) {
        // Get wave index and calculate difficulty level
        int waveIndex = currentWave - 1;
        double rand = Math.random();
        
        // Choose words based on wave number and randomness
        if (waveIndex < 3) {
            // Early waves - mostly easy words
            if (rand < 0.7) return getRandomWord(easyWords);
            else return getRandomWord(mediumWords);
        } 
        else if (waveIndex < 6) {
            // Middle waves - mix of easy and medium words
            if (rand < 0.3) return getRandomWord(easyWords);
            else if (rand < 0.8) return getRandomWord(mediumWords);
            else return getRandomWord(hardWords);
        }
        else {
            // Late waves - mostly medium and hard words
            if (rand < 0.1) return getRandomWord(easyWords);
            else if (rand < 0.5) return getRandomWord(mediumWords);
            else return getRandomWord(hardWords);
        }
    }

    /**
     * Helper method to get a random word from a list
     * @param words List of words to choose from
     * @return A randomly selected word from the list
     */
    private static String getRandomWord(List<String> words) {
        return words.get((int)(Math.random() * words.size()));
    }
} 