package com.oop2.typewiz;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.Node;

public class SceneManager {
    public static void showScreen(TypeWizApp.ScreenType screenType) {
        FXGL.getGameScene().clearUINodes();  // Clear existing nodes first

        Node screen = switch (screenType) {
            case LOGIN -> new LoginScreen().getRoot();
            case REGISTER -> new RegisterScreen().getRoot();
            case LOADING -> new LoadingScreen().getRoot();
            case MAIN_MENU -> new MainMenuScreen().getRoot();
            case DIFFICULTY_SELECTION -> new DifficultyMenuScreen(
                    () -> showScreen(TypeWizApp.ScreenType.MAIN_MENU),
                    () -> startGame(Difficulty.APPRENTICE),
                    () -> startGame(Difficulty.WIZARD),
                    () -> startGame(Difficulty.ARCHMAGE)
            ).getContentRoot();
            default -> throw new IllegalStateException("Unexpected screen: " + screenType);
        };

        FXGL.getGameScene().addUINode(screen);
        TypeWizApp.setupCustomCursor();
    }

    private static void startGame(Difficulty difficulty) {
        // Store the selected difficulty
        FXGL.getWorldProperties().setValue("difficulty", difficulty);

        // Start the game
        FXGL.getGameController().startNewGame();
    }
}
