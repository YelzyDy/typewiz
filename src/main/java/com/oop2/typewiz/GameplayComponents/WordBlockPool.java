package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.Node;

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
        // Create a block that will move from right to left
        Rectangle block = new Rectangle(blockSize, blockSize, Color.rgb(200, 200, 220));
        
        // Create a TextFlow for the word with individual letters
        TextFlow textFlow = new TextFlow();
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow.setTranslateY(-25);
        
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
        
        // Update block color based on row
        Node blockNode = stackPaneChildren.get(0);
        if (!(blockNode instanceof Rectangle)) {
            return;
        }
        
        Rectangle blockRect = (Rectangle) blockNode;
        Color blockColor;
        if (rowIndex == 0) {
            blockColor = Color.rgb(200, 200, 220);
        } else if (rowIndex == 1) {
            blockColor = Color.rgb(190, 210, 230);
        } else if (rowIndex == 2) {
            blockColor = Color.rgb(180, 220, 240);
        } else if (rowIndex == 3) {
            blockColor = Color.rgb(170, 230, 250);
        } else {
            blockColor = Color.rgb(160, 240, 255);
        }
        blockRect.setFill(blockColor);
        
        // Update text flow with new word
        TextFlow textFlow = block.getObject("textFlow");
        if (textFlow == null) {
            return;
        }
        
        textFlow.getChildren().clear();
        
        // Create new letter nodes list
        List<Text> letterNodes = new ArrayList<>();
        
        // Add letters to text flow and letter nodes list
        for (int i = 0; i < word.length(); i++) {
            Text letterText = new Text(String.valueOf(word.charAt(i)));
            letterText.setFont(Font.font(22));
            letterText.setFill(Color.WHITE);
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