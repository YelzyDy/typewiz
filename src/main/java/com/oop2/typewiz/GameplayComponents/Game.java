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
import javafx.geometry.Rectangle2D;
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
                
        // Add a barrier on top of the existing tower in the image
        // Position is based on the tower's location in the image (approximately x=125, y=280)
        double barrierWidth = 120;
        double barrierHeight = 15;
        Rectangle barrierRect = new Rectangle(barrierWidth, barrierHeight, Color.rgb(70, 70, 110));
        
        Entity barrierEntity = FXGL.entityBuilder()
                .at(49, 220) // Moved higher and more to the left
                .view(barrierRect)
                .zIndex(21)
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
            FXGL.getGameWorld().getEntitiesByType(EntityType.MOVING_BLOCK).forEach(entity -> {
                entity.translateX(-BLOCK_SPEED * FXGL.tpf());
                
                // Remove blocks that have gone off screen
                if (entity.getX() < -50) {
                    entity.removeFromWorld();
                }
            });
        }, Duration.seconds(0.016)); // ~60 fps
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
