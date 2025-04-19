package com.oop2.typewiz;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class MainController {

    @FXML
    private Button startButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        // Optional: Add behavior or styling tweaks during controller initialization
        System.out.println("Main screen loaded.");
    }

    @FXML
    private void handleStart(ActionEvent event) {
        System.out.println("Start button clicked");
        // TODO: Navigate to the start screen
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        System.out.println("Help button clicked");
        // TODO: Show help popup or navigate to help scene
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Exit button clicked");
        Platform.exit();
    }
}
