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
    private boolean spawnGrimougeNext;  // Flag to alternate between Gargoyles and Grimouges
    
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
        this.spawnGrimougeNext = false;  // Start with Gargoyles
        
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
        
        // Remove all existing enemies before starting a new wave
        entityManager.removeAllEntitiesOfType(Game.EntityType.GARGOYLE);
        entityManager.removeAllEntitiesOfType(Game.EntityType.GRIMOUGE);
        
        // Get wave index (0-based)
        int waveIndex = currentWave - 1;
        
        // Reset wave spawning state with wave-specific parameters
        isSpawningWave = false; // Start as false, set to true after announcement
        spawnFromRight = true; // Always spawn from right side
        spawnGrimougeNext = false; // Start with Gargoyles for each wave
        
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
        List<Entity> activeEnemies = entityManager.getActiveEnemies();
        if (activeEnemies.isEmpty() || waveSpawnTimer.elapsed(Duration.seconds(currentSpawnDelay))) {
            if (totalWaveSpawns > 0) {
                spawnGroup();
            } else if (activeEnemies.isEmpty()) {
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
        
        List<Entity> spawnedEntities;
        
        // Decide whether to spawn Gargoyles or Grimouges
        if (spawnGrimougeNext) {
            System.out.println("Spawning group of " + currentGroupSize + " grimouges from right side");
            
            // Use GrimougeFactory to spawn a group of grimouges
            spawnedEntities = GrimougeFactory.spawnGrimougeGroup(
                currentGroupSize,
                minY,
                maxY,
                spawnFromRight, // Always spawn from right
                spawnPerimeterRight,
                wordSupplier, // Method reference to get random words
                Game.EntityType.GRIMOUGE
            );
            
            System.out.println("GrimougeFactory returned " + spawnedEntities.size() + " entities");
        } else {
            System.out.println("Spawning group of " + currentGroupSize + " gargoyles from right side");
            
            // Use GargoyleFactory to spawn a group of gargoyles
            spawnedEntities = GargoyleFactory.spawnGargoyleGroup(
                currentGroupSize,
                minY,
                maxY,
                spawnFromRight, // Always spawn from right
                spawnPerimeterRight,
                wordSupplier, // Method reference to get random words
                Game.EntityType.GARGOYLE
            );
            
            System.out.println("GargoyleFactory returned " + spawnedEntities.size() + " entities");
        }
        
        // Toggle flag for next spawn
        spawnGrimougeNext = !spawnGrimougeNext;
        
        // Add spawned entities to entity manager and ensure they're attached to the world
        for (Entity entity : spawnedEntities) {
            String entityType = entity.isType(Game.EntityType.GRIMOUGE) ? "grimouge" : "gargoyle";
            System.out.println("Adding " + entityType + " to entity manager with word: " + 
                (entity.getProperties().exists("word") ? entity.getString("word") : "unknown"));
                
            entityManager.addActiveEntity(entity);
            
            // Ensure the entity is attached to the world
            if (!entity.isActive()) {
                System.out.println("Attaching " + entityType + " to world");
                FXGL.getGameWorld().addEntity(entity);
            }
        }
        
        // Check if we need to select a new entity automatically
        InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
        if (inputManager != null && inputManager.getSelectedWordBlock() == null && !spawnedEntities.isEmpty()) {
            // We only want to select one if there are no other active selections
            Entity closestEntity;
            
            if (spawnGrimougeNext) { // We just spawned gargoyles
                closestEntity = GargoyleFactory.findClosestGargoyleToCenter(spawnedEntities);
            } else { // We just spawned grimouges
                closestEntity = GrimougeFactory.findClosestGrimougeToCenter(spawnedEntities);
            }
            
            if (closestEntity != null) {
                System.out.println("WaveManager: Automatically selecting new entity after spawn");
                inputManager.selectWordBlock(closestEntity);
            }
        }
        
        totalWaveSpawns -= spawnedEntities.size();
        
        String entityType = spawnGrimougeNext ? "gargoyles" : "grimouges"; // Because we toggled the flag
        System.out.println("Successfully spawned " + spawnedEntities.size() + 
                " " + entityType + ", " + totalWaveSpawns + " remaining in wave");
        
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
        
        return spawnedEntities;
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
        
        // Reset spawn flag to start with Gargoyles
        spawnGrimougeNext = false;
        
        System.out.println("WaveManager: Spawning first group of enemies, size=" + currentGroupSize);
        
        // Spawn first group right away
        List<Entity> spawned = spawnGroup();
        
        System.out.println("WaveManager: Spawned first group with " + spawned.size() + 
                " enemies, total remaining: " + totalWaveSpawns);
        
        // Automatically select the first entity (closest to center) for targeting
        if (!spawned.isEmpty()) {
            // Find the closest entity to the center of the screen
            Entity closestEntity;
            if (spawned.get(0).isType(Game.EntityType.GARGOYLE)) {
                closestEntity = GargoyleFactory.findClosestGargoyleToCenter(spawned);
            } else {
                closestEntity = GrimougeFactory.findClosestGrimougeToCenter(spawned);
            }
            
            // Get input manager from world properties and select the entity
            InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
            if (inputManager != null && closestEntity != null) {
                System.out.println("WaveManager: Automatically selecting first enemy");
                inputManager.selectWordBlock(closestEntity);
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
        spawnGrimougeNext = false;
    }
} 