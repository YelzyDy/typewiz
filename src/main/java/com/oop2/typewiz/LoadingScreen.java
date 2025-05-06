package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class LoadingScreen extends GameApplication {

    private ProgressBar progressBar;
    private double progress = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setTitle("TypeWiz Launch");
    }

    @Override
    protected void initUI() {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(1550, 800);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f001a, #2a0055);");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(1450, 700);
        AnchorPane.setTopAnchor(vbox, 50.0);
        AnchorPane.setBottomAnchor(vbox, 50.0);
        AnchorPane.setLeftAnchor(vbox, 50.0);
        AnchorPane.setRightAnchor(vbox, 50.0);

        ImageView logo = new ImageView(new Image(getClass().getResource("assets/typewiz_logo.png").toExternalForm()));
        logo.setFitHeight(469);
        logo.setFitWidth(669);
        logo.setPreserveRatio(true);

        progressBar = new ProgressBar(0);
        progressBar.setPrefSize(568, 39);
        progressBar.setStyle("-fx-accent: #c85bff; -fx-control-inner-background: #2b1d3a;");

        vbox.getChildren().addAll(logo, progressBar);
        root.getChildren().add(vbox);
        FXGL.getGameScene().addUINode(root);

        startLoading();
    }

    private void startLoading() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            progress += 0.03;
            progressBar.setProgress(progress);

            if (progress >= 1.0) {
                switchToMainScreen();
            }
        }));
        timeline.setCycleCount(100);
        timeline.play();
    }

    private void switchToMainScreen() {
        // Example: switch scene or load next UI
        System.out.println("Loading complete! Proceed to main screen...");
        // You could call FXGL.getGameScene().clearUINodes() and add a new scene here
    }

    public static void main(String[] args) {
        launch(args);
    }
}
