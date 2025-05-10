package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class BatchRenderer {
    private final Group renderGroup;
    private final List<Entity> visibleEntities;
    private final SpatialPartitioning spatialPartitioning;

    public BatchRenderer(SpatialPartitioning spatialPartitioning) {
        this.spatialPartitioning = spatialPartitioning;
        this.renderGroup = new Group();
        this.visibleEntities = new ArrayList<>();
        
        // Add the render group to the game world
        FXGL.getGameScene().addUINode(renderGroup);
    }

    public void update() {
        // Clear previous frame
        renderGroup.getChildren().clear();
        visibleEntities.clear();

        // Get visible entities from spatial partitioning
        List<Entity> entities = spatialPartitioning.getVisibleEntities();
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        visibleEntities.addAll(entities);

        // Batch render all visible entities
        for (Entity entity : entities) {
            if (entity == null || entity.getViewComponent() == null) {
                continue;
            }
            
            List<Node> viewChildren = entity.getViewComponent().getChildren();
            if (viewChildren == null || viewChildren.isEmpty()) {
                continue;
            }
            
            Node view = viewChildren.get(0);
            if (!(view instanceof StackPane)) {
                continue;
            }
            
            StackPane stackPane = (StackPane) view;
            List<Node> stackPaneChildren = stackPane.getChildren();
            if (stackPaneChildren == null || stackPaneChildren.size() < 2) {
                continue;
            }
            
            // Create a copy of the view for rendering
            StackPane renderView = new StackPane();
            renderView.setLayoutX(entity.getX());
            renderView.setLayoutY(entity.getY());
            
            // Copy the block rectangle
            Node blockNode = stackPaneChildren.get(0);
            if (!(blockNode instanceof Rectangle)) {
                continue;
            }
            
            Rectangle blockRect = (Rectangle) blockNode;
            Rectangle renderRect = new Rectangle(
                blockRect.getWidth(),
                blockRect.getHeight(),
                blockRect.getFill()
            );
            
            // Copy the text flow
            Node textNode = stackPaneChildren.get(1);
            if (!(textNode instanceof TextFlow)) {
                continue;
            }
            
            TextFlow textFlow = (TextFlow) textNode;
            TextFlow renderTextFlow = new TextFlow();
            renderTextFlow.setTextAlignment(textFlow.getTextAlignment());
            renderTextFlow.setTranslateY(textFlow.getTranslateY());
            
            // Copy all text nodes
            for (Node node : textFlow.getChildren()) {
                if (node instanceof Text) {
                    Text originalText = (Text) node;
                    Text renderText = new Text(originalText.getText());
                    renderText.setFont(originalText.getFont());
                    renderText.setFill(originalText.getFill());
                    renderTextFlow.getChildren().add(renderText);
                }
            }
            
            // Add to render group
            renderView.getChildren().addAll(renderRect, renderTextFlow);
            renderGroup.getChildren().add(renderView);

            Boolean entered = entity.getPropertyOptional("hasEnteredScreen").map(v -> (Boolean) v).orElse(false);
            if (!entered && entity.getX() < FXGL.getAppWidth()) {
                entity.setProperty("hasEnteredScreen", true);
                entered = true;
            }

            if (entity.getX() < 0 && entered) {
                // decreaseHealth();
                // ... rest of removal logic ...
            }
        }
    }

    public List<Entity> getVisibleEntities() {
        return visibleEntities;
    }

    public void clear() {
        renderGroup.getChildren().clear();
        visibleEntities.clear();
    }
} 