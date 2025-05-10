package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Rectangle2D;

import java.util.*;

public class SpatialPartitioning {
    private final int cellSize;
    private final Map<Long, Set<Entity>> grid;
    private final Rectangle2D bounds;
    private final double screenWidth;
    private final double screenHeight;

    public SpatialPartitioning(int cellSize, double screenWidth, double screenHeight) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.bounds = new Rectangle2D(0, 0, screenWidth, screenHeight);
    }

    private long getCellKey(double x, double y) {
        int cellX = (int) (x / cellSize);
        int cellY = (int) (y / cellSize);
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    public void updateEntity(Entity entity) {
        // Remove entity from all cells first
        removeEntity(entity);

        // Only add if entity is within screen bounds
        if (isEntityVisible(entity)) {
            double x = entity.getX();
            double y = entity.getY();
            long key = getCellKey(x, y);
            grid.computeIfAbsent(key, k -> new HashSet<>()).add(entity);
        }
    }

    private boolean isEntityVisible(Entity entity) {
        double x = entity.getX();
        double y = entity.getY();
        return x >= -100 && x <= screenWidth + 100 && // Add some padding for smooth transitions
               y >= -100 && y <= screenHeight + 100;
    }

    public void removeEntity(Entity entity) {
        // Remove entity from all cells
        grid.values().forEach(cell -> cell.remove(entity));
    }

    public List<Entity> getEntitiesInArea(Rectangle2D area) {
        Set<Entity> result = new HashSet<>();
        
        // Calculate cell range
        int startX = (int) (area.getMinX() / cellSize);
        int startY = (int) (area.getMinY() / cellSize);
        int endX = (int) (area.getMaxX() / cellSize);
        int endY = (int) (area.getMaxY() / cellSize);

        // Check all cells in range
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
                Set<Entity> cell = grid.get(key);
                if (cell != null) {
                    result.addAll(cell);
                }
            }
        }

        return new ArrayList<>(result);
    }

    public List<Entity> getVisibleEntities() {
        return getEntitiesInArea(bounds);
    }

    public void clear() {
        grid.clear();
    }

    public void batchUpdate(List<Entity> entities) {
        for (Entity entity : entities) {
            updateEntity(entity);
        }
    }
} 