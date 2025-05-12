package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
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

public class LoadingScreen extends FXGLMenu {

    private Rectangle progressBar;
    private StackPane root;

    public LoadingScreen() {
        super(MenuType.MAIN_MENU);
        // Gradient background
            FXGL.getAudioPlayer().playMusic(FXGL.getAssetLoader().loadMusic("magic.wav"));

        Pane background = new Pane();
        background.setPrefSize(getAppWidth(), getAppHeight());
        background.setStyle(
                "-fx-background-image: url('/assets/textures/background-and-platforms/loadingscreenbg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );


        // Logo
        ImageView logo = new ImageView(new Image(getClass().getResource("assets/typewiz_logo.png").toExternalForm()));
        logo.setFitHeight(469);
        logo.setFitWidth(669);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(30, Color.web("#c85bff")));


//        FadeTransition logoPulse = new FadeTransition(Duration.seconds(1.5), logo);
//        logoPulse.setFromValue(0.8);
//        logoPulse.setToValue(1.0);
//        logoPulse.setCycleCount(Animation.INDEFINITE);
//        logoPulse.setAutoReverse(true);
//        logoPulse.play();

        // Progress bar
        Rectangle progressBarBg = new Rectangle(568, 39);
        progressBarBg.setFill(Color.web("#1a0638"));
        progressBarBg.setStroke(Color.web("#b388ff"));
        progressBarBg.setStrokeWidth(2);
        progressBarBg.setArcWidth(30);
        progressBarBg.setArcHeight(30);
        progressBarBg.setEffect(new DropShadow(8, Color.web("#b388ff")));

        progressBar = new Rectangle(0, 30);
        progressBar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b388ff")),
                new Stop(0.5, Color.web("#ffeb3b")),
                new Stop(1, Color.web("#b388ff"))));
        progressBar.setArcWidth(25);
        progressBar.setArcHeight(25);
        progressBar.setEffect(new Glow(0.6));

        StackPane bgWrapper = new StackPane(progressBarBg);
        bgWrapper.setMaxWidth(568);
        bgWrapper.setPrefHeight(39);

        HBox barWrapper = new HBox(progressBar);
        barWrapper.setMaxWidth(568);
        barWrapper.setPrefHeight(30);
        barWrapper.setAlignment(Pos.CENTER_LEFT);
        barWrapper.setPadding(new Insets(4));

        StackPane progressContainer = new StackPane(bgWrapper, barWrapper);
        progressContainer.setAlignment(Pos.CENTER);

        // Loading text
        Text loadingText = new Text("Casting spells...");
        loadingText.setFont(javafx.scene.text.Font.font("Papyrus", 24));
        loadingText.setFill(Color.web("#ffeb3b"));
        loadingText.setEffect(new DropShadow(10, Color.web("#b388ff")));

        VBox centerBox = new VBox(40, logo, progressContainer, loadingText);
        centerBox.setAlignment(Pos.CENTER);

        root = new StackPane(background, centerBox);
        getContentRoot().getChildren().setAll(root);

        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);

        startSmoothLoading();
    }

    private void startSmoothLoading() {
        Timeline timeline = new Timeline();
        final double durationSeconds = 3.0;
        final int fps = 60;
        final int totalFrames = (int) (durationSeconds * fps);

        for (int i = 0; i <= totalFrames; i++) {
            double t = (double) i / totalFrames;
            double easedT = easeInOut(t); // smoother than linear
            KeyFrame frame = new KeyFrame(Duration.seconds(i * (1.0 / fps)), e -> {
                progressBar.setWidth(easedT * 568);
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(e -> {
            FXGL.getSceneService().pushSubScene(new MainMenuScreen());
        });

        timeline.play();
    }

    private double easeInOut(double t) {
        return t < 0.5
                ? 2 * t * t
                : -1 + (4 - 2 * t) * t;
    }
}
