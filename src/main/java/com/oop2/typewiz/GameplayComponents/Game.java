package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;

public class Game extends GameApplication {
    private LocalTimer blockSpawnTimer;
    private static final double BLOCK_SPEED = 100; // pixels per second
    private static final double SPAWN_INTERVAL = 3.0; // seconds between block spawns

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
                
        // Create a tower on the left side
        double towerWidth = 120;
        double towerHeight = 300;
        Rectangle towerRect = new Rectangle(towerWidth, towerHeight, Color.rgb(60, 60, 100));
        
        Entity towerEntity = FXGL.entityBuilder()
                .at(100, FXGL.getAppHeight() - towerHeight - 60) // Position tower on the platform
                .view(towerRect)
                .zIndex(20)
                .buildAndAttach();
                
        // Add a placeholder block on top of the tower
        Rectangle placeholderBlock = new Rectangle(50, 50, Color.ORANGE);
        Entity placeholderEntity = FXGL.entityBuilder()
                .at(135, FXGL.getAppHeight() - towerHeight - 110) // Position on top of tower
                .view(placeholderBlock)
                .zIndex(25)
                .buildAndAttach();
                
        // Add barriers/crenellations to the platform
        addBarriersToPath();
        
        // Initialize timer for spawning moving blocks
        blockSpawnTimer = FXGL.newLocalTimer();
        blockSpawnTimer.capture();
        
        // Run the game loop
        FXGL.getGameTimer().runAtInterval(() -> {
            if (blockSpawnTimer.elapsed(Duration.seconds(SPAWN_INTERVAL))) {
                spawnMovingBlock();
                blockSpawnTimer.capture();
            }
            
            // Update moving blocks
            getGameWorld().getEntitiesByType(EntityType.MOVING_BLOCK).forEach(entity -> {
                entity.translateX(-BLOCK_SPEED * FXGL.tpf());
                
                // Remove blocks that have gone off screen
                if (entity.getX() < -50) {
                    entity.removeFromWorld();
                }
            });
        }, Duration.seconds(0.016)); // ~60 fps
    }
    
    private void addBarriersToPath() {
        // Add several barrier/crenellation elements along the platform
        double barrierHeight = 20;
        double platformY = FXGL.getAppHeight() - 150; // Same position as the platform
        
        // Create barriers at intervals
        for (int i = 0; i < 10; i++) {
            double xPos = 200 + i * 120;
            Rectangle barrier = new Rectangle(15, barrierHeight, Color.rgb(80, 80, 120));
            
            Entity barrierEntity = FXGL.entityBuilder()
                    .at(xPos, platformY - barrierHeight)
                    .view(barrier)
                    .zIndex(15)
                    .buildAndAttach();
        }
    }
    
    private void spawnMovingBlock() {
        // Create a block that will move from right to left
        double blockSize = 40;
        Rectangle block = new Rectangle(blockSize, blockSize, Color.rgb(200, 200, 220));
        
        // Start position is just off the right side of the screen
        double startX = FXGL.getAppWidth() + 10;
        double blockY = FXGL.getAppHeight() - 120; // Position on the platform
        
        Entity blockEntity = FXGL.entityBuilder()
                .type(EntityType.MOVING_BLOCK)
                .at(startX, blockY)
                .view(block)
                .bbox(new HitBox(BoundingShape.box(blockSize, blockSize)))
                .zIndex(30)
                .buildAndAttach();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
