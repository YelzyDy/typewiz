package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.ProgressBar;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoadingScreen extends LoadingScene {

    private double progress = 0.0;
    private Rectangle progressBar;
    private Rectangle progressBarBg;
    private StackPane root;

    public LoadingScreen() {
        // Create magical gradient background
        Rectangle background = new Rectangle(getAppWidth(), getAppHeight());
        background.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2a0845")),
                new Stop(0.5, Color.web("#4a148c")),
                new Stop(1, Color.web("#1a0638"))));
        background.setEffect(new Bloom(0.1));

        // Twinkling stars
        Pane stars = createStarParticles(30);

        // Logo
        ImageView logo = new ImageView(new Image(getClass().getResource("assets/typewiz_logo.png").toExternalForm()));
        logo.setFitHeight(469);
        logo.setFitWidth(669);
        logo.setPreserveRatio(true);
        logo.setEffect(new Glow(0.7));

        FadeTransition logoPulse = new FadeTransition(Duration.seconds(1.5), logo);
        logoPulse.setFromValue(0.8);
        logoPulse.setToValue(1.0);
        logoPulse.setCycleCount(FadeTransition.INDEFINITE);
        logoPulse.setAutoReverse(true);
        logoPulse.play();

        // Progress bar
        progressBarBg = new Rectangle(568, 39);
        progressBarBg.setFill(Color.web("#1a0638"));
        progressBarBg.setStroke(Color.web("#b388ff"));
        progressBarBg.setStrokeWidth(2);
        progressBarBg.setArcWidth(30);
        progressBarBg.setArcHeight(30);
        progressBarBg.setEffect(new DropShadow(8, Color.web("#b388ff")));

        progressBar = new Rectangle(0, 30); // start with 0 width
        progressBar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b388ff")),
                new Stop(0.5, Color.web("#ffeb3b")),
                new Stop(1, Color.web("#b388ff"))));
        progressBar.setArcWidth(25);
        progressBar.setArcHeight(25);
        progressBar.setEffect(new Glow(0.6));

        StackPane bgWrapper = new StackPane(progressBarBg); // background centered
        bgWrapper.setMaxWidth(568);
        bgWrapper.setPrefHeight(39);

        HBox barWrapper = new HBox(progressBar); // aligns bar to left
        barWrapper.setMaxWidth(568);
        barWrapper.setPrefHeight(30);
        barWrapper.setAlignment(Pos.CENTER_LEFT);
        barWrapper.setPadding(new Insets(4, 4, 4, 4)); // optional padding to match height

        StackPane progressContainer = new StackPane(bgWrapper, barWrapper);
        progressContainer.setAlignment(Pos.CENTER);


        // Loading text
        Text loadingText = new Text("Casting spells...");
        loadingText.setFont(javafx.scene.text.Font.font("Papyrus", 24));
        loadingText.setFill(Color.web("#ffeb3b"));
        loadingText.setEffect(new DropShadow(10, Color.web("#b388ff")));

        // Center content
        VBox centerBox = new VBox(40, logo, progressContainer, loadingText); // slightly more spacing
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setTranslateY(0); // centered vertically


        // Root
        root = new StackPane(background, stars, centerBox);
        getContentRoot().getChildren().setAll(root);

        // Start loading animation
        startLoading();
    }

    private void startLoading() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            progress += 0.03;
            progressBar.setWidth(progress * 568); // dynamically update width

            if (progress >= 1.0) {
                FXGL.getGameScene().removeUINode(root);
                MainMenuScreen mainMenu = new MainMenuScreen();
                FXGL.getGameScene().getContentRoot().getChildren().add(mainMenu.getContentRoot());
            }
        }));
        timeline.setCycleCount(100);
        timeline.play();

    }

    private Pane createStarParticles(int count) {
        Pane stars = new Pane();
        for (int i = 0; i < count; i++) {
            Rectangle star = new Rectangle(2, 2, Color.web("#ffeb3b", 0.7));
            star.setLayoutX(Math.random() * getAppWidth());
            star.setLayoutY(Math.random() * getAppHeight());
            star.setEffect(new Glow(0.8));

            FadeTransition ft = new FadeTransition(Duration.seconds(2 + Math.random() * 3), star);
            ft.setFromValue(0.3);
            ft.setToValue(0.9);
            ft.setCycleCount(FadeTransition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();

            stars.getChildren().add(star);
        }
        return stars;
    }
}
