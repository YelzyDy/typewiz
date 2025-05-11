package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;


public class TypeWizApp extends GameApplication {

    public enum ScreenType {
        LOGIN,
        REGISTER,
        LOADING,
        MAIN_MENU,
        DIFFICULTY_SELECTION
    }
    public static ImageCursor CLOSED_BOOK_CURSOR;
    public static ImageCursor OPEN_BOOK_CURSOR;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setTitle("TypeWiz");
    }

    public static void setupCustomCursor() {
        Image closedBookImg = FXGL.image("magicbook.png");
        Image openBookImg = FXGL.image("magicbook_hover.png");

        Image scaledClosedBookImg = new Image(closedBookImg.getUrl(), 32, 32, true, true);
        Image scaledOpenBookImg = new Image(openBookImg.getUrl(), 32, 32, true, true);

        CLOSED_BOOK_CURSOR = new ImageCursor(scaledClosedBookImg, 16, 16); // Centered hotspot
        OPEN_BOOK_CURSOR = new ImageCursor(scaledOpenBookImg, 16, 16); // Centered hotspot

        // default cursor
        FXGL.getGameScene().getRoot().setCursor(CLOSED_BOOK_CURSOR);
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