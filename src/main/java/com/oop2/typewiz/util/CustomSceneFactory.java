package com.oop2.typewiz.util;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.oop2.typewiz.LoginScreen;
import org.jetbrains.annotations.NotNull;

public class CustomSceneFactory extends SceneFactory {
    @NotNull
    @Override
    public FXGLMenu newMainMenu() {
        return new LoginScreen();
    }
}
