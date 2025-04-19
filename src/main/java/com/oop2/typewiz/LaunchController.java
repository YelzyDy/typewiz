package com.oop2.typewiz;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;

public class LaunchController {

    @FXML
    private ProgressBar progressBar;

    private Stage stage;  // Store the Stage

    // Set the stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private double progress = 0;

    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> {
                    progress += 0.04;
                    progressBar.setProgress(progress);

                    if (progress >= 1.0) {
                        switchToMainScreen();
                    }
                })
        );
        timeline.setCycleCount(100);
        timeline.play();
    }

    private void switchToMainScreen() {
        if (stage != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                Scene scene = new Scene(loader.load(), 1550, 800);
                stage.setScene(scene);  // Switch to the main screen
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Stage is not initialized.");
        }
    }
}
