package com.oop2.typewiz;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

import java.io.IOException;

public class LoginController {

    public AnchorPane apLogin;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    private Stage stage;

    @FXML
    private ImageView eyeIcon;
    private boolean isPasswordVisible = false;
    private final Image visibleIcon = new Image(getClass().getResource("assets/password_unhash_icon.png").toExternalForm());
    private final Image hiddenIcon = new Image(getClass().getResource("assets/password_hash_icon.png").toExternalForm());

    @FXML
    private TextField visiblePasswordField;




    @FXML
    public void initialize() {
        Platform.runLater(() -> apLogin.requestFocus());
        // Optional: Add input validation or styling on init
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show visible text
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.setImage(visibleIcon);
        } else {
            // Hide password again
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            eyeIcon.setImage(hiddenIcon);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();

        // 1. Check if fields are empty
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username is required.");
            return;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password is required.");
            return;
        }

        // 2. Optional: Basic length validation
        if (username.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username is too short.");
            return;
        }
        if ( password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password is too short.");
            return;
        }

        // 3. Hardcoded check for demo login
        if (!username.equals("test123") || !password.equals("test123")) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            return;
        }

        // 4. Load launch screen
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("launch.fxml"));
        Scene scene = new Scene(loader.load(), 1520, 790);
        scene.getStylesheets().add(getClass().getResource("assets/style.css").toExternalForm());

        LaunchController launchController = loader.getController();
        launchController.setStage(stage);

        stage.setScene(scene);
    }


    @FXML
    public void handleSignUp(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) apLogin.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1550, 800);
        stage.setTitle("Sign up");
//        scene.getStylesheets().add(getClass().getResource("button-hover.css").toExternalForm());
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set the stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
