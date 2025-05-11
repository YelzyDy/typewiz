package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.AnimatedTexture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javafx.scene.Node;

/**
 * Manages game entities including creation, pooling, tracking, and removal.
 * This class implements the Object Pool pattern to efficiently reuse entities.
 */
public class EntityManager {
    
    private List<Entity> activeEntities;
    private List<Entity> gargoylePool;
    private List<Entity> grimougePool;
    private List<Entity> entitiesToRemove;
    private SpatialPartitioning spatialPartitioning;
    private double width;
    private double height;
    
    private static final int BATCH_SIZE = 50;
    private static final int MAX_GARGOYLES = 10;
    private static final int MAX_GRIMOUGES = 10;
    
    // Constants for entity movement
    private static final double GARGOYLE_SPEED = 50.0;
    private static final double GARGOYLE_FRAME_WIDTH = 288;
    private static final double GARGOYLE_FRAME_HEIGHT = 312;
    private static final double GARGOYLE_SCALE = 0.6;
    
    private static final double GRIMOUGE_SPEED = 50.0;
    private static final int GRIMOUGE_SPRITE_SHEET_WIDTH = 7200;
    private static final int GRIMOUGE_FRAME_COUNT = 9;
    private static final double GRIMOUGE_FRAME_WIDTH = GRIMOUGE_SPRITE_SHEET_WIDTH / GRIMOUGE_FRAME_COUNT; // 800px per frame
    private static final double GRIMOUGE_FRAME_HEIGHT = 400;
    private static final double GRIMOUGE_SCALE = 0.6;
    
    /**
     * Creates a new EntityManager with initialized pools and spatial partitioning
     * 
     * @param width Width of the game area
     * @param height Height of the game area
     */
    public EntityManager(double width, double height) {
        this.width = width;
        this.height = height;
        activeEntities = new ArrayList<>();
        gargoylePool = new ArrayList<>(MAX_GARGOYLES);
        grimougePool = new ArrayList<>(MAX_GRIMOUGES);
        entitiesToRemove = new ArrayList<>(BATCH_SIZE);
        spatialPartitioning = new SpatialPartitioning(100, width, height);
        
        // Pre-initialize the entity pools
        initializeGargoylePool();
        initializeGrimougePool();
    }
    
    /**
     * Initializes the gargoyle entity pool
     */
    private void initializeGargoylePool() {
        for (int i = 0; i < MAX_GARGOYLES; i++) {
            Entity gargoyleEntity = FXGL.entityBuilder()
                    .type(Game.EntityType.GARGOYLE)
                    .zIndex(25)
                    .build();
            
            // Add to pool
            gargoylePool.add(gargoyleEntity);
        }
    }
    
    /**
     * Initializes the grimouge entity pool
     */
    private void initializeGrimougePool() {
        for (int i = 0; i < MAX_GRIMOUGES; i++) {
            Entity grimougeEntity = FXGL.entityBuilder()
                    .type(Game.EntityType.GRIMOUGE)
                    .zIndex(25)
                    .build();
            
            // Add to pool
            grimougePool.add(grimougeEntity);
        }
    }
    
    /**
     * Gets a gargoyle entity from the pool or creates a new one if needed
     * 
     * @return An available gargoyle entity
     */
    public Entity getGargoyleFromPool() {
        if (gargoylePool.isEmpty()) {
            // If pool is empty, create a new entity
            Entity gargoyleEntity = FXGL.entityBuilder()
                    .type(Game.EntityType.GARGOYLE)
                    .zIndex(25)
                    .build();
            return gargoyleEntity;
        }
        
        // Get and remove the last entity from the pool
        return gargoylePool.remove(gargoylePool.size() - 1);
    }
    
    /**
     * Gets a grimouge entity from the pool or creates a new one if needed
     * 
     * @return An available grimouge entity
     */
    public Entity getGrimougeFromPool() {
        if (grimougePool.isEmpty()) {
            // If pool is empty, create a new entity
            Entity grimougeEntity = FXGL.entityBuilder()
                    .type(Game.EntityType.GRIMOUGE)
                    .zIndex(25)
                    .build();
            return grimougeEntity;
        }
        
        // Get and remove the last entity from the pool
        return grimougePool.remove(grimougePool.size() - 1);
    }
    
    /**
     * Returns an entity to the gargoyle pool for reuse
     * 
     * @param entity The entity to return to the pool
     */
    public void returnGargoyleToPool(Entity entity) {
        if (entity != null) {
            if (entity.isActive()) {
                entity.removeFromWorld();
            }
            spatialPartitioning.removeEntity(entity);
            gargoylePool.add(entity);
        }
    }
    
    /**
     * Returns an entity to the grimouge pool for reuse
     * 
     * @param entity The entity to return to the pool
     */
    public void returnGrimougeToPool(Entity entity) {
        if (entity != null) {
            if (entity.isActive()) {
                entity.removeFromWorld();
            }
            spatialPartitioning.removeEntity(entity);
            grimougePool.add(entity);
        }
    }
    
    /**
     * Adds an entity to the active entities list and updates spatial partitioning
     * 
     * @param entity The entity to add
     */
    public void addActiveEntity(Entity entity) {
        if (entity != null) {
            activeEntities.add(entity);
            spatialPartitioning.updateEntity(entity);
            
            // Ensure entity is attached to world
            if (!entity.isActive()) {
                System.out.println("Entity was not active, attaching to world: " + entity);
                FXGL.getGameWorld().addEntity(entity);
            } else {
                System.out.println("Entity is already active in the world: " + entity);
            }
        }
    }
    
    /**
     * Removes an entity from the active entities list and returns it to the pool if applicable
     * 
     * @param entity The entity to remove
     */
    public void removeEntity(Entity entity) {
        if (entity != null) {
            activeEntities.remove(entity);
            
            if (entity.isActive()) {
                entity.removeFromWorld();
            }
            
            spatialPartitioning.removeEntity(entity);
            
            // Return entity to appropriate pool based on type
            if (entity.isType(Game.EntityType.GARGOYLE)) {
                gargoylePool.add(entity);
            } else if (entity.isType(Game.EntityType.GRIMOUGE)) {
                grimougePool.add(entity);
            }
        }
    }
    
    /**
     * Marks an entity for removal in the next update cycle
     * 
     * @param entity The entity to mark for removal
     */
    public void markForRemoval(Entity entity) {
        if (entity != null) {
            entitiesToRemove.add(entity);
        }
    }
    
    /**
     * Processes all entities marked for removal
     */
    public void processRemovals() {
        for (Entity entity : entitiesToRemove) {
            removeEntity(entity);
        }
        entitiesToRemove.clear();
    }
    
    /**
     * Gets the list of active entities
     * 
     * @return List of active entities
     */
    public List<Entity> getActiveEntities() {
        return new ArrayList<>(activeEntities);
    }
    
    /**
     * Gets active entities of a specific type
     * 
     * @param type The entity type to filter by
     * @return List of active entities of the specified type
     */
    public List<Entity> getActiveEntitiesByType(Game.EntityType type) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : activeEntities) {
            if (entity.isType(type)) {
                result.add(entity);
            }
        }
        return result;
    }
    
    /**
     * Gets the active gargoyle entities
     * 
     * @return List of active gargoyle entities
     */
    public List<Entity> getActiveGargoyles() {
        return getActiveEntitiesByType(Game.EntityType.GARGOYLE);
    }
    
    /**
     * Gets the active grimouge entities
     * 
     * @return List of active grimouge entities
     */
    public List<Entity> getActiveGrimouges() {
        return getActiveEntitiesByType(Game.EntityType.GRIMOUGE);
    }
    
    /**
     * Gets all active enemy entities (gargoyles and grimouges)
     * 
     * @return List of all active enemy entities
     */
    public List<Entity> getActiveEnemies() {
        List<Entity> enemies = new ArrayList<>();
        enemies.addAll(getActiveGargoyles());
        enemies.addAll(getActiveGrimouges());
        return enemies;
    }
    
    /**
     * Removes all active entities of a specified type
     * 
     * @param type The entity type to remove
     */
    public void removeAllEntitiesOfType(Game.EntityType type) {
        List<Entity> entities = getActiveEntitiesByType(type);
        for (Entity entity : entities) {
            removeEntity(entity);
        }
    }
    
    /**
     * Finds entities matching a predicate
     * 
     * @param predicate The predicate to match entities against
     * @return List of matching entities
     */
    public List<Entity> findEntities(Predicate<Entity> predicate) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : activeEntities) {
            if (predicate.test(entity)) {
                result.add(entity);
            }
        }
        return result;
    }
    
    /**
     * Updates the spatial partitioning for all active entities
     */
    public void updateSpatialPartitioning() {
        spatialPartitioning.batchUpdate(activeEntities);
    }
    
    /**
     * Gets the spatial partitioning system
     * 
     * @return The spatial partitioning system
     */
    public SpatialPartitioning getSpatialPartitioning() {
        return spatialPartitioning;
    }
    
    /**
     * Clears all entities and resets the manager
     */
    public void clear() {
        // Remove all active entities from the world
        for (Entity entity : activeEntities) {
            if (entity.isActive()) {
                entity.removeFromWorld();
            }
        }
        
        // Clear all lists
        activeEntities.clear();
        entitiesToRemove.clear();
        
        // Reinitialize the pools
        gargoylePool.clear();
        grimougePool.clear();
        initializeGargoylePool();
        initializeGrimougePool();
        
        // Reset spatial partitioning
        spatialPartitioning.clear();
    }
    
    /**
     * Updates all active entities
     * @param tpf Time per frame
     * @param speedMultiplier Speed multiplier for the current wave
     */
    public void updateEntities(double tpf, double speedMultiplier) {
        // Process all active gargoyles
        updateGargoyles(tpf, speedMultiplier);
        
        // Process all active grimouges
        updateGrimouges(tpf, speedMultiplier);
    }
    
    /**
     * Updates all active gargoyle entities
     * @param tpf Time per frame
     * @param speedMultiplier Speed multiplier for the current wave
     */
    private void updateGargoyles(double tpf, double speedMultiplier) {
        List<Entity> gargoyles = getActiveGargoyles();
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
                if ((movingRight && gargoyle.getX() > this.width) ||
                        (!movingRight && gargoyle.getX() < -GARGOYLE_FRAME_WIDTH * GARGOYLE_SCALE)) {
                    if (hasBeenVisible) {
                        // Decrease player health when gargoyle leaves screen
                        PlayerManager playerManager = FXGL.getWorldProperties().getObject("playerManager");
                        if (playerManager != null) {
                            playerManager.decreaseHealth();
                        }
                    }
                    // Mark for removal in the next cycle
                    markForRemoval(gargoyle);

                    // Update selection if needed
                    InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
                    if (inputManager != null && gargoyle == inputManager.getSelectedWordBlock()) {
                        Entity closest = GargoyleFactory.findClosestGargoyleToCenter(getActiveGargoyles());
                        if (closest != null) {
                            inputManager.selectWordBlock(closest);
                        }
                    }
                    continue;
                }
            }

            // Update animation
            updateEntityAnimation(gargoyle, tpf);

            // Update spatial partitioning
            spatialPartitioning.updateEntity(gargoyle);
        }
    }
    
    /**
     * Updates all active grimouge entities
     * @param tpf Time per frame
     * @param speedMultiplier Speed multiplier for the current wave
     */
    private void updateGrimouges(double tpf, double speedMultiplier) {
        List<Entity> grimouges = getActiveGrimouges();
        for (Entity grimouge : grimouges) {
            if (grimouge == null) continue;

            // Check visibility and activity state
            boolean isVisible = isEntityVisible(grimouge);
            boolean hasBeenVisible = grimouge.getBoolean("hasBeenVisible");
            boolean isActive = grimouge.getBoolean("isActive");
            boolean movingRight = grimouge.getBoolean("movingRight");

            // Mark as visible once it enters the screen
            if (isVisible && !hasBeenVisible) {
                grimouge.setProperty("hasBeenVisible", true);
                hasBeenVisible = true;
            }

            // Activate when fully visible
            if (isVisible && !isActive) {
                grimouge.setProperty("isActive", true);
                isActive = true;
            }

            // Only move and check for removal if the grimouge is active
            if (isActive) {
                // Update position
                double movement = GRIMOUGE_SPEED * speedMultiplier * tpf;
                movement = Math.max(movement, 1.0); // Prevent micro-stuttering
                grimouge.translateX(movingRight ? movement : -movement);

                // Check if grimouge has left the screen
                if ((movingRight && grimouge.getX() > this.width) ||
                        (!movingRight && grimouge.getX() < -GRIMOUGE_FRAME_WIDTH * GRIMOUGE_SCALE)) {
                    if (hasBeenVisible) {
                        // Decrease player health when grimouge leaves screen
                        PlayerManager playerManager = FXGL.getWorldProperties().getObject("playerManager");
                        if (playerManager != null) {
                            playerManager.decreaseHealth();
                        }
                    }
                    // Mark for removal in the next cycle
                    markForRemoval(grimouge);

                    // Update selection if needed
                    InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
                    if (inputManager != null && grimouge == inputManager.getSelectedWordBlock()) {
                        Entity closest = GrimougeFactory.findClosestGrimougeToCenter(getActiveGrimouges());
                        if (closest != null) {
                            inputManager.selectWordBlock(closest);
                        }
                    }
                    continue;
                }
            }

            // Update animation
            updateEntityAnimation(grimouge, tpf);

            // Update spatial partitioning
            spatialPartitioning.updateEntity(grimouge);
        }
    }
    
    /**
     * Updates animation for an entity
     * @param entity The entity to update
     * @param tpf Time per frame
     */
    private void updateEntityAnimation(Entity entity, double tpf) {
        if (entity.getViewComponent() != null && !entity.getViewComponent().getChildren().isEmpty()) {
            Node view = entity.getViewComponent().getChildren().get(0);
            if (view != null) {
                Node viewNode = view instanceof javafx.scene.layout.StackPane ? 
                    ((javafx.scene.layout.StackPane) view).getChildren().get(0) : view;
                
                if (viewNode instanceof AnimatedTexture) {
                    AnimatedTexture texture = (AnimatedTexture) viewNode;
                    double animationTime = 0.0;
                    if (entity.getProperties().exists("animationTime")) {
                        animationTime = entity.getDouble("animationTime");
                    }
                    animationTime += tpf;
                    entity.setProperty("animationTime", animationTime);

                    // Update the animation frame
                    texture.onUpdate(tpf);
                }
            }
        }
    }
    
    /**
     * Checks if an entity is visible on screen
     * @param entity The entity to check
     * @return True if the entity is within screen bounds
     */
    private boolean isEntityVisible(Entity entity) {
        double x = entity.getX();
        // Consider entity visible only when it's within the actual screen bounds
        return x >= 0 && x <= this.width;
    }
} 