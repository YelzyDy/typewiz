package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Factory class for creating and configuring vyleye entities.
 * This class follows the Factory pattern to centralize vyleye creation.
 */
public class VyleyeFactory {
    // Constants for vyleye configuration
    private static final int VYLEYE_SPRITE_SHEET_WIDTH = 5600;
    private static final int VYLEYE_FRAME_COUNT = 7;
    private static final int VYLEYE_FRAME_WIDTH = VYLEYE_SPRITE_SHEET_WIDTH / VYLEYE_FRAME_COUNT; // 800px per frame
    private static final int VYLEYE_FRAME_HEIGHT = 400;
    private static final double VYLEYE_SCALE = 0.6;
    private static final double WORD_FONT_SIZE = 40;
    private static final double WORD_VERTICAL_OFFSET = 160;
    private static final double SCREEN_MARGIN = 150;
    private static final String FONT_FAMILY = "Arial";

    // Animation channels
    private static AnimationChannel vyleyeIdleAnimation;
    static AnimationChannel vyleyeFlyAnimation;
    private static final double WING_FLAP_SPEED = 0.2;

    // Color constants for word highlighting
    private static final Color SELECTED_COLOR = Color.LIME;
    private static final Color TYPED_COLOR = Color.DEEPSKYBLUE;
    private static final Color DEFAULT_COLOR = Color.WHITE;

    // Random generator
    private static final Random random = new Random();

    /**
     * Initializes the vyleye animations.
     * Must be called before using the factory to create vyleyes.
     */
    public static void initializeAnimations() {
        // Create animation channels for vyleyes
        try {
            // Create animation channel for idle animation (first row)
            vyleyeIdleAnimation = new AnimationChannel(
                    FXGL.image("mobs/vyleye/vyleye.png"),
                    VYLEYE_FRAME_COUNT, // 7 frames per row
                    VYLEYE_FRAME_WIDTH, VYLEYE_FRAME_HEIGHT,
                    Duration.seconds(WING_FLAP_SPEED * 1.5), // Even slower for idle
                    0, VYLEYE_FRAME_COUNT - 1); // First row, frames 0-6

            // Create animation channel for flying animation (same row)
            vyleyeFlyAnimation = new AnimationChannel(
                    FXGL.image("mobs/vyleye/vyleye.png"),
                    VYLEYE_FRAME_COUNT, // 7 frames per row
                    VYLEYE_FRAME_WIDTH, VYLEYE_FRAME_HEIGHT,
                    Duration.seconds(WING_FLAP_SPEED), // Slower for flying
                    0, VYLEYE_FRAME_COUNT - 1); // Same row, frames 0-6
        } catch (Exception e) {
            System.err.println("Error initializing vyleye animations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a vyleye entity at the specified position.
     *
     * @param index      Index of the vyleye for positioning
     * @param yPos       Y-position of the vyleye
     * @param fromRight  Whether the vyleye should spawn from the right side
     * @param spawnPerimeterRight Distance from right edge where vyleyes spawn
     * @param entityType Entity type enum value for the vyleye
     * @return The created vyleye entity
     */
    public static Entity spawnVyleye(int index, double yPos, boolean fromRight, double spawnPerimeterRight, Enum<?> entityType) {
        if (vyleyeFlyAnimation == null || vyleyeIdleAnimation == null) {
            // Initialize animations if they haven't been yet
            initializeAnimations();
        }

        // Calculate spawn position
        double xPos;
        if (fromRight) {
            xPos = FXGL.getAppWidth() - spawnPerimeterRight;
        } else {
            xPos = spawnPerimeterRight;
        }

        StackPane wordBlockView = new StackPane();
        AnimatedTexture texture = new AnimatedTexture(vyleyeFlyAnimation);
        texture.loop();
        texture.setScaleX(fromRight ? VYLEYE_SCALE : -VYLEYE_SCALE); // Flip sprite if spawning from left
        texture.setScaleY(VYLEYE_SCALE);
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(VYLEYE_FRAME_WIDTH * VYLEYE_SCALE);
        textFlow.setMaxHeight(VYLEYE_FRAME_HEIGHT * VYLEYE_SCALE);
        wordBlockView.getChildren().addAll(texture, textFlow);

        // Create new entity with all required properties
        Entity vyleye = FXGL.entityBuilder()
                .type(entityType)
                .at(xPos, yPos)
                .view(wordBlockView)
                .scale(VYLEYE_SCALE, VYLEYE_SCALE)
                .bbox(new HitBox(BoundingShape.box(VYLEYE_FRAME_WIDTH * VYLEYE_SCALE, VYLEYE_FRAME_HEIGHT * VYLEYE_SCALE)))
                .zIndex(25)
                .with("word", "") // Initialize with empty string
                .with("letterNodes", new ArrayList<Text>())
                .with("row", index)
                .with("animationTime", 0.0)
                .with("textFlow", textFlow)
                .with("hasBeenVisible", false)
                .with("isActive", false)
                .with("movingRight", !fromRight)
                .buildAndAttach();

        System.out.println("Created vyleye entity at position: (" + xPos + ", " + yPos + "), active: " + vyleye.isActive());
        return vyleye;
    }

    /**
     * Configures a word for a vyleye entity.
     *
     * @param vyleye The vyleye entity
     * @param word     The word to assign to the vyleye
     * @param yPos     Y-position for calculation purposes
     */
    public static void configureVyleyeWord(Entity vyleye, String word, double yPos) {
        if (vyleye == null || word == null || word.isEmpty()) {
            return;
        }

        // Set word property
        vyleye.setProperty("word", word);

        // Get view component and validate
        if (vyleye.getViewComponent() == null || vyleye.getViewComponent().getChildren().isEmpty()) {
            return;
        }

        StackPane view = (StackPane) vyleye.getViewComponent().getChildren().get(0);
        if (view == null || view.getChildren().isEmpty()) {
            return;
        }

        // Get or create TextFlow
        TextFlow textFlow = vyleye.getObject("textFlow");
        if (textFlow == null) {
            textFlow = new TextFlow();
            textFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            // Position the text flow at the top center of the entity
            textFlow.setTranslateY(WORD_VERTICAL_OFFSET * 0.8); // Reduced vertical offset to move word higher
            vyleye.setProperty("textFlow", textFlow);
            view.getChildren().add(textFlow);
        }

        // Clear existing text
        textFlow.getChildren().clear();
        
        // Create container for word with background
        StackPane wordContainer = new StackPane();
        
        // Determine font size based on word length - longer words get smaller font
        double fontSize = WORD_FONT_SIZE;
        if (word.length() > 8) {
            fontSize *= 0.8; // 20% smaller for long words
        } else if (word.length() > 12) {
            fontSize *= 0.7; // 30% smaller for very long words
        }
        
        // Create word background for better visibility
        Rectangle wordBackground = new Rectangle();
        double padding = 10;
        double wordLength = word.length() * fontSize * 0.55 + padding * 2;  // Even more compact width
        wordBackground.setWidth(Math.max(VYLEYE_FRAME_WIDTH * VYLEYE_SCALE * 0.6, wordLength));
        wordBackground.setHeight(fontSize + padding);  // Compact height
        wordBackground.setArcWidth(10);  // Smaller corners
        wordBackground.setArcHeight(10);
        wordBackground.setFill(Color.rgb(0, 0, 0, 0.8));
        wordBackground.setStroke(Color.rgb(200, 200, 200, 0.6));
        wordBackground.setStrokeWidth(1.0);
        
        // Smaller drop shadow 
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setRadius(5);
        dropShadow.setSpread(0.2);
        wordBackground.setEffect(dropShadow);
        
        // Create compact HBox for text with center alignment
        HBox wordBox = new HBox(1); // Minimal spacing between letters
        wordBox.setAlignment(Pos.CENTER);
        
        // Create letter nodes
        List<Text> letterNodes = new ArrayList<>();
        for (char c : word.toCharArray()) {
            Text letterText = new Text(String.valueOf(c));
            letterText.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontWeight.BOLD, fontSize));
            letterText.setFill(Color.WHITE);
            
            // Add minimal stroke
            letterText.setStroke(Color.BLACK);
            letterText.setStrokeWidth(0.5);
            
            // Less glow
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.2);
            letterText.setEffect(glow);
            
            letterNodes.add(letterText);
            wordBox.getChildren().add(letterText);
        }
        
        // Add word background and text to the container
        wordContainer.getChildren().addAll(wordBackground, wordBox);
        wordContainer.setAlignment(Pos.CENTER); // Ensure everything is centered
        
        // Add the container to the text flow
        textFlow.getChildren().add(wordContainer);
        textFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); // Ensure text flow is also centered

        // Store letter nodes for later use
        vyleye.setProperty("letterNodes", letterNodes);
        
        // Add simpler connecting line 
        Line connectionLine = new Line();
        connectionLine.setStartX(VYLEYE_FRAME_WIDTH * VYLEYE_SCALE / 2);
        connectionLine.setStartY(VYLEYE_FRAME_HEIGHT * VYLEYE_SCALE / 2);
        connectionLine.setEndX(VYLEYE_FRAME_WIDTH * VYLEYE_SCALE / 2);
        connectionLine.setEndY(WORD_VERTICAL_OFFSET * 0.8);  // Match the reduced vertical offset
        connectionLine.setStroke(Color.rgb(200, 200, 200, 0.3));
        connectionLine.setStrokeWidth(0.75);
        connectionLine.getStrokeDashArray().addAll(2.0, 2.0);  // Smaller dashes
        
        // Add to view before the text to keep text on top
        view.getChildren().add(1, connectionLine);
    }

    /**
     * Spawns a group of vyleyes with random positions and words.
     *
     * @param currentGroupSize Number of vyleyes to spawn in this group
     * @param minY Minimum Y position for spawning
     * @param maxY Maximum Y position for spawning
     * @param fromRight Whether to spawn from right side
     * @param spawnPerimeterRight Distance from right edge where vyleyes spawn
     * @param wordSupplier Function to get a random word
     * @param entityType Entity type enum value for the vyleyes
     * @return List of spawned vyleye entities
     */
    public static List<Entity> spawnVyleyeGroup(int currentGroupSize, double minY, double maxY, 
                                              boolean fromRight, double spawnPerimeterRight,
                                              java.util.function.Supplier<String> wordSupplier,
                                              Enum<?> entityType) {
        List<Entity> newVyleyes = new ArrayList<>();
        
        // Calculate spawn area
        double availableHeight = maxY - minY;
        
        // Generate random positions with minimal spacing checks
        List<Double> spawnPositions = new ArrayList<>();
        
        // Generate random positions
        for (int i = 0; i < currentGroupSize; i++) {
            double yPos = minY + random.nextDouble() * availableHeight;
            spawnPositions.add(yPos);
        }
        
        // Sort positions to keep some visual order
        Collections.sort(spawnPositions);
        
        // Spawn vyleyes at positions
        int spawned = 0;
        for (double yPos : spawnPositions) {
            String word = wordSupplier.get();
            Entity vyleye = spawnVyleye(spawned, yPos, fromRight, spawnPerimeterRight, entityType);
            configureVyleyeWord(vyleye, word, yPos);
            newVyleyes.add(vyleye);
            spawned++;
        }
        
        return newVyleyes;
    }

    /**
     * Highlights the selected vyleye's word.
     *
     * @param vyleye The vyleye entity to select
     */
    public static void selectWordBlock(Entity vyleye) {
        if (vyleye == null) return;
        
        try {
            List<Text> letterNodes = vyleye.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(SELECTED_COLOR);
                    
                    // Increase glow effect for better visibility when selected
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
            System.err.println("Error selecting word block: " + e.getMessage());
        }
    }
    
    /**
     * Resets the vyleye's word to default white color.
     *
     * @param vyleye The vyleye entity to reset
     */
    public static void resetBlockToDefaultColor(Entity vyleye) {
        if (vyleye == null) return;
        
        try {
            List<Text> letterNodes = vyleye.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(DEFAULT_COLOR);
                    // Preserve the stroke and glow effect for visibility
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
            System.err.println("Error resetting block color: " + e.getMessage());
        }
    }
    
    /**
     * Updates letter colors based on typing progress.
     *
     * @param vyleye The vyleye entity
     * @param currentInput Current typed input
     */
    public static void updateLetterColors(Entity vyleye, String currentInput) {
        if (vyleye == null) return;
        
        try {
            List<Text> letterNodes = vyleye.getObject("letterNodes");
            if (letterNodes == null) return;
            
            // Update colors - typed letters blue, remaining letters yellow
            for (int i = 0; i < letterNodes.size(); i++) {
                Text letter = letterNodes.get(i);
                
                if (i < currentInput.length()) {
                    letter.setFill(TYPED_COLOR);
                    // Add stronger glow for typed letters
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.7);
                    letter.setEffect(glow);
                } else {
                    letter.setFill(SELECTED_COLOR);
                    // Normal glow for untyped letters
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.5);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
            System.err.println("Error updating letter colors: " + e.getMessage());
        }
    }
    
    /**
     * Marks all letters in the vyleye's word as complete (all blue).
     *
     * @param vyleye The vyleye entity
     */
    public static void markWordAsComplete(Entity vyleye) {
        if (vyleye == null) return;
        
        try {
            List<Text> letterNodes = vyleye.getObject("letterNodes");
            if (letterNodes != null) {
                for (Text letter : letterNodes) {
                    letter.setFill(TYPED_COLOR);
                    
                    // Add strong glow effect for completed words
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
                    letter.setEffect(glow);
                }
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
            System.err.println("Error marking word as complete: " + e.getMessage());
        }
    }
    
    /**
     * Finds the closest vyleye to the center of the screen.
     *
     * @param activeVyleyes List of active vyleye entities
     * @return The closest vyleye, or null if none available
     */
    public static Entity findClosestVyleyeToCenter(List<Entity> activeVyleyes) {
        if (activeVyleyes.isEmpty()) return null;
        
        double centerX = FXGL.getAppWidth() / 2.0;
        double centerY = FXGL.getAppHeight() / 2.0;
        
        Entity closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Entity vyleye : activeVyleyes) {
            try {
                // Make sure it has the required property
                vyleye.getString("word");
                
                double dx = vyleye.getX() - centerX;
                double dy = vyleye.getY() - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = vyleye;
                }
            } catch (Exception e) {
                // Skip entities with issues
                continue;
            }
        }
        
        return closest;
    }
} 