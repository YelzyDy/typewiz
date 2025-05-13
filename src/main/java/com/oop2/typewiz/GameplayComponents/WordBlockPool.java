package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WordBlockPool {
    private final Queue<Entity> availableBlocks;
    private final List<Entity> activeBlocks;
    private final int initialPoolSize;
    private final double blockSize;
    private final Random random;

    public WordBlockPool(int initialPoolSize, double blockSize) {
        this.initialPoolSize = initialPoolSize;
        this.blockSize = blockSize;
        this.availableBlocks = new ConcurrentLinkedQueue<>();
        this.activeBlocks = new ArrayList<>();
        this.random = new Random();

        // Initialize the pool with some blocks
        for (int i = 0; i < initialPoolSize; i++) {
            Entity block = createNewBlock();
            FXGL.getGameWorld().removeEntity(block); // Make sure it's not visible initially
            availableBlocks.offer(block);
        }
    }

    private Entity createNewBlock() {
        // Create a block with a magical gradient background
        Rectangle block = new Rectangle(blockSize, blockSize);

        // Create magical gradient background
        LinearGradient blockGradient = new LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(45, 0, 75, 0.8)),    // Deep purple
                new Stop(1, Color.rgb(75, 0, 130, 0.8))    // Royal purple
        );
        block.setFill(blockGradient);
        block.setStroke(Color.rgb(255, 215, 0, 0.6));  // Golden stroke
        block.setStrokeWidth(2);
        block.setArcWidth(15);  // Rounded corners
        block.setArcHeight(15);

        // Add magical glow effect
        DropShadow blockGlow = new DropShadow();
        blockGlow.setColor(Color.rgb(138, 43, 226, 0.6));  // Purple glow
        blockGlow.setRadius(10);
        blockGlow.setSpread(0.3);
        block.setEffect(blockGlow);

        // Create a TextFlow for the word with individual letters
        TextFlow textFlow = new TextFlow();
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow.setTranslateY(-30);  // Moved up slightly

        // Create a stack pane to hold both the block and TextFlow
        StackPane view = new StackPane(block, textFlow);
        view.setAlignment(Pos.CENTER);

        // Initialize empty letter nodes list
        List<Text> letterNodes = new ArrayList<>();

        return FXGL.entityBuilder()
                .type(Game.EntityType.MOVING_BLOCK)
                .view(view)
                .bbox(new HitBox(BoundingShape.box(blockSize, blockSize)))
                .with("letterNodes", letterNodes)
                .with("textFlow", textFlow)
                .zIndex(30)
                .build();
    }

    public Entity acquire(double x, double y, String word, int rowIndex) {
        Entity block;

        // Try to get a block from the pool
        if (!availableBlocks.isEmpty()) {
            block = availableBlocks.poll();
        } else {
            // If no blocks available, create a new one
            block = createNewBlock();
        }

        // Configure the block for use
        configureBlock(block, x, y, word, rowIndex);

        // Add to active blocks
        activeBlocks.add(block);

        return block;
    }

    private void configureBlock(Entity block, double x, double y, String word, int rowIndex) {
        if (block == null || word == null || word.isEmpty()) {
            return;
        }

        // Set position
        block.setPosition(x, y);

        // Set word property
        block.setProperty("word", word);

        // Get view component and validate
        if (block.getViewComponent() == null) {
            return;
        }

        List<Node> viewChildren = block.getViewComponent().getChildren();
        if (viewChildren == null || viewChildren.isEmpty()) {
            return;
        }

        Node view = viewChildren.get(0);
        if (!(view instanceof StackPane)) {
            return;
        }

        StackPane stackPane = (StackPane) view;
        List<Node> stackPaneChildren = stackPane.getChildren();
        if (stackPaneChildren == null || stackPaneChildren.size() < 2) {
            return;
        }

        // Update block color based on row with magical gradients
        Node blockNode = stackPaneChildren.get(0);
        if (!(blockNode instanceof Rectangle)) {
            return;
        }

        Rectangle blockRect = (Rectangle) blockNode;
        LinearGradient blockGradient;
        Color glowColor;

        switch (rowIndex) {
            case 0:  // First row - Purple theme
                blockGradient = new LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(45, 0, 75, 0.8)),
                        new Stop(1, Color.rgb(75, 0, 130, 0.8)));
                glowColor = Color.rgb(138, 43, 226, 0.6);
                break;
            case 1:  // Second row - Blue theme
                blockGradient = new LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(25, 25, 112, 0.8)),
                        new Stop(1, Color.rgb(65, 105, 225, 0.8)));
                glowColor = Color.rgb(65, 105, 225, 0.6);
                break;
            case 2:  // Third row - Cyan theme
                blockGradient = new LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(0, 75, 130, 0.8)),
                        new Stop(1, Color.rgb(0, 150, 200, 0.8)));
                glowColor = Color.rgb(0, 150, 200, 0.6);
                break;
            default:  // Other rows - Teal theme
                blockGradient = new LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(0, 100, 100, 0.8)),
                        new Stop(1, Color.rgb(0, 128, 128, 0.8)));
                glowColor = Color.rgb(0, 128, 128, 0.6);
                break;
        }

        blockRect.setFill(blockGradient);
        blockRect.setStroke(Color.rgb(255, 215, 0, 0.6));  // Golden stroke
        blockRect.setStrokeWidth(2);
        blockRect.setArcWidth(15);
        blockRect.setArcHeight(15);

        // Update glow effect
        DropShadow blockGlow = new DropShadow();
        blockGlow.setColor(glowColor);
        blockGlow.setRadius(10);
        blockGlow.setSpread(0.3);
        blockRect.setEffect(blockGlow);

        // Update text flow with new word
        TextFlow textFlow = block.getObject("textFlow");
        if (textFlow == null) {
            return;
        }

        textFlow.getChildren().clear();

        // Create new letter nodes list with enhanced styling
        List<Text> letterNodes = new ArrayList<>();

        // Add letters to text flow and letter nodes list with magical styling
        for (int i = 0; i < word.length(); i++) {
            Text letterText = new Text(String.valueOf(word.charAt(i)));
            letterText.setFont(Font.font("Papyrus", FontWeight.BOLD, 24));
            letterText.setFill(Color.WHITE);

            // Add glow effect to text
            DropShadow textGlow = new DropShadow();
            textGlow.setColor(Color.rgb(255, 215, 0, 0.7));  // Golden glow
            textGlow.setRadius(5);
            textGlow.setSpread(0.3);
            letterText.setEffect(textGlow);

            textFlow.getChildren().add(letterText);
            letterNodes.add(letterText);
        }

        // Update the letter nodes property
        block.setProperty("letterNodes", letterNodes);

        // Make sure the block is in the world
        if (!FXGL.getGameWorld().getEntities().contains(block)) {
            FXGL.getGameWorld().addEntity(block);
        }
    }

    public void release(Entity block) {
        if (block != null) {
            // Remove from active blocks
            activeBlocks.remove(block);

            // Clear the block's properties
            block.setProperty("word", "");

            // Clear the text flow and letter nodes
            TextFlow textFlow = block.getObject("textFlow");
            if (textFlow != null) {
                textFlow.getChildren().clear();
            }

            // Create new empty letter nodes list
            List<Text> emptyLetterNodes = new ArrayList<>();
            block.setProperty("letterNodes", emptyLetterNodes);

            // Remove from world
            FXGL.getGameWorld().removeEntity(block);

            // Add back to available blocks
            availableBlocks.offer(block);
        }
    }

    public void releaseAll() {
        for (Entity block : new ArrayList<>(activeBlocks)) {
            release(block);
        }
    }

    public List<Entity> getActiveBlocks() {
        return activeBlocks;
    }

    public int getAvailableCount() {
        return availableBlocks.size();
    }

    public int getActiveCount() {
        return activeBlocks.size();
    }
} 