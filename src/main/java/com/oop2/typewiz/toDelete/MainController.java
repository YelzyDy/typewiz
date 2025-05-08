package com.oop2.typewiz.toDelete;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.IOException;

public class MainController {

    @FXML
    private Button startButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        System.out.println("Main screen loaded.");
    }

    @FXML
    private void handleStart(ActionEvent event) {
        System.out.println("Start button clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oop2/typewiz/difficulty.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Typing Test - Start");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load start.fxml");
        }
    }

    @FXML
    private void handleHelp(ActionEvent event) {
//        System.out.println("Help button clicked");
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oop2/typewiz/help.fxml"));
//            Parent root = loader.load();
//
//            Stage stage = new Stage(); // Open help in a new window (popup)
//            stage.setScene(new Scene(root));
//            stage.setTitle("Help");
//            stage.setResizable(false);
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Failed to load help.fxml");
//        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Exit button clicked");
        Platform.exit();
    }
}
