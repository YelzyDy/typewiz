package com.oop2.typewiz.toDelete;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class GameController {
//TODO: change this controller that will go after choosing the difficulty level
    @FXML
    private Label gameStatusLabel;

    @FXML
    public void initialize() {
        // Optional: Add behavior or styling tweaks during controller initialization
        System.out.println("Game screen loaded.");
        gameStatusLabel.setText("Welcome to TypeWiz!");
    }

    // Example method to show a game status update
    public void updateGameStatus(String status) {
        gameStatusLabel.setText(status);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}