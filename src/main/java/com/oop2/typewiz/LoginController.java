package com.oop2.typewiz;

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

    @FXML
    private ImageView eyeIcon;
    private boolean isPasswordVisible = false;
    private final Image visibleIcon = new Image(getClass().getResource("assets/password_unhash_icon.png").toExternalForm());
    private final Image hiddenIcon = new Image(getClass().getResource("assets/password_hash_icon.png").toExternalForm());

    @FXML
    private TextField visiblePasswordField;




    @FXML
    public void initialize() {
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
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example: Replace this with actual DB auth logic
        if ("admin".equals(username) && "password".equals(password)) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
            // TODO: Load dashboard or next screen
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
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
}
