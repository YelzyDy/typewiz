package com.oop2.typewiz;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

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

        // Adding click events to buttons
        startButton.setOnAction(this::handleStart);
        helpButton.setOnAction(this::handleHelp);
        exitButton.setOnAction(this::handleExit);
    }

    @FXML
    private void handleStart(ActionEvent event) {
        // Show confirmation that the Start button works
        //showAlert("Start Button", "The Start button was clicked!");
        System.out.println("The Start button was clicked!");
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        // Show confirmation that the Help button works
        //showAlert("Help Button", "The Help button was clicked!");
        System.out.println("The Help button was clicked!");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Show confirmation that the Exit button works
        //showAlert("Exit Button", "The Exit button was clicked!");
        System.out.println("The Exit button was clicked!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}