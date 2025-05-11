package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class TypeWizApp extends GameApplication {

    public enum ScreenType {
        LOGIN,
        REGISTER,
        LOADING,
        MAIN_MENU,
        DIFFICULTY_SELECTION
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setTitle("TypeWiz");
    }

    static void setupCustomCursor() {
        Image cursorImg = new Image(TypeWizApp.class.getResource("assets/magicbook.png").toExternalForm());

        Image scaledImg = new Image(cursorImg.getUrl(), 32, 32, true, true);

        // Use the scaled image for the custom cursor
        FXGL.getGameScene().setCursor(new ImageCursor(scaledImg, 16, 16)); // Hotspot at top-left
    }

    @Override
    protected void initUI() {
        // Initialize custom cursor for the first screen
        setupCustomCursor();
        // Show login screen (or any other screen based on your logic)
        SceneManager.showScreen(ScreenType.LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
