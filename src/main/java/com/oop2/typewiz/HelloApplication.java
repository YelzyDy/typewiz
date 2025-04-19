package com.oop2.typewiz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML for the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1520, 790);

        // Apply global stylesheet
        scene.getStylesheets().add(getClass().getResource("assets/style.css").toExternalForm());

        // Get controller and pass stage
        LoginController controller = fxmlLoader.getController();
        controller.setStage(stage);

        // Set up the stage and show the login screen
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
