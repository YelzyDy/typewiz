package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class TypeWizApp extends GameApplication {

    public enum ScreenType {
        LOGIN,
        REGISTER,
        LOADING
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setTitle("TypeWiz");
    }

    @Override
    protected void initUI() {
        SceneManager.showScreen(ScreenType.LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
