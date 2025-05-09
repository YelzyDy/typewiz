package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.oop2.typewiz.GameplayComponents.EntityType.*;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
//import com.almasb.fxgl.entity.level.tmx.TMXLevelLoader;


public class Medieval extends GameApplication {


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Game App");
        settings.setVersion("0.1");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new SceneFactory());
        settings.setApplicationMode(ApplicationMode.DEVELOPER);

    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MedievalFactory());
        Level level = getAssetLoader().loadLevel("Medieval.tmx", new TMXLevelLoader());
        getGameWorld().setLevel(level);

        spawn("PLATFORM");


    }


    public static void main(String[] args) {
        launch(args);
    }


}


