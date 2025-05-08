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
            default -> throw new IllegalStateException("Unexpected screen: " + screenType);
        };

        FXGL.getGameScene().addUINode(screen);
    }
}
