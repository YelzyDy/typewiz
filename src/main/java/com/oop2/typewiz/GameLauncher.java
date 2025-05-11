package com.oop2.typewiz;

import com.oop2.typewiz.GameplayComponents.Game;

/**
 * Simple launcher for TypeWiz game.
 * This class serves as the only entry point to the game.
 */
public class GameLauncher {
    /**
     * Application entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("TypeWiz Game Launcher starting...");
        
        // Directly launch the game
        Game.launch(args);
    }
} 