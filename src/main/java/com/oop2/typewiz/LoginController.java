package com.oop2.typewiz;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private AnchorPane apLogin;
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

        // Add Enter key listener to the username and password fields
        usernameField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnterKey);
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnterKey);
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
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection connection = DatabaseConnection.connect()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);  // Again, use hashed password in a real system
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    // User found, proceed to the next screen
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("launch.fxml"));
                    Scene scene = new Scene(loader.load(), 1550, 800);

                    LaunchController launchController = loader.getController();
                    launchController.setStage(stage);

                    stage.setScene(scene);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error occurred while logging in.");
        }
    }

    @FXML
    public void handleSignUp(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) apLogin.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1550, 800);
        stage.setTitle("Sign up");
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set the stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Handle Enter key press for login action
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode().getName().equals("Enter")) {
            try {
                handleLogin(new ActionEvent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
