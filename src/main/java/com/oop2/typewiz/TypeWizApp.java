package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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

    @Override
    protected void initUI() {
        // Create a particle emitter
        ParticleEmitter emitter = new ParticleEmitter();
        emitter.setNumParticles(5);
        emitter.setSize(6, 12);
        emitter.setColor(Color.GOLD);
        emitter.setEndColor(Color.PURPLE);
        emitter.setExpireFunction(i -> Duration.seconds(0.6));
        emitter.setVelocityFunction(i -> new Point2D(Math.random() * 10 - 5, Math.random() * 10 - 5));
        emitter.setBlendMode(javafx.scene.effect.BlendMode.ADD);

        // Create one entity for the cursor trail
        var trail = FXGL.entityBuilder()
                .with(new ParticleComponent(emitter))
                .buildAndAttach();

        // Move the trail to follow the cursor on each frame
        FXGL.getGameScene().getInput().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            trail.setPosition(event.getX(), event.getY());
        });

        // Show initial screen
        SceneManager.showScreen(ScreenType.LOGIN);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
