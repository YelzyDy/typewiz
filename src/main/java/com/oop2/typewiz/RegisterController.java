package com.oop2.typewiz;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML private AnchorPane apRegister;
    @FXML private TextField tfUsername;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private ImageView eyeIcon;
    @FXML private ImageView eyeIcon2;

    @FXML private TextField visiblePasswordField;
    @FXML private TextField visibleConfirmPasswordField;

    private boolean isPasswordVisible = false;

    private final Image visibleIcon = new Image(getClass().getResource("assets/password_unhash_icon.png").toExternalForm());
    private final Image hiddenIcon = new Image(getClass().getResource("assets/password_hash_icon.png").toExternalForm());

    @FXML
    private void initialize() {
        setupPasswordFieldToggle(tfPassword, visiblePasswordField);
        setupPasswordFieldToggle(tfConfirmPassword, visibleConfirmPasswordField);
        Platform.runLater(() -> apRegister.requestFocus());

        // Add KeyEvent listener to the fields
        tfUsername.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnterKey);
        tfPassword.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnterKey);
        tfConfirmPassword.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnterKey);
    }

    private void setupPasswordFieldToggle(PasswordField passwordField, TextField visibleField) {
        visibleField.setManaged(false);
        visibleField.setVisible(false);
        visibleField.setPrefSize(passwordField.getPrefWidth(), passwordField.getPrefHeight());
        visibleField.setStyle(passwordField.getStyle());
        visibleField.setFont(passwordField.getFont());
        visibleField.setPromptText(passwordField.getPromptText());

        visibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            tfPassword.setVisible(false);
            tfPassword.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            eyeIcon.setImage(visibleIcon);
        } else {
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            eyeIcon.setImage(hiddenIcon);
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            tfConfirmPassword.setVisible(false);
            tfConfirmPassword.setManaged(false);
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);

            eyeIcon2.setImage(visibleIcon);
        } else {
            tfConfirmPassword.setVisible(true);
            tfConfirmPassword.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);

            eyeIcon2.setImage(hiddenIcon);
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        String username = tfUsername.getText();
        String email = tfEmail.getText();  // Get the email
        String password = tfPassword.getText();  // Remember to hash the password in real-world apps

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Username, email, and password cannot be empty.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, email);  // Set email in the query
                stmt.setString(3, password);  // In practice, hash the password before storing it

                int rowsAffected = stmt.executeUpdate();  // Executes the INSERT query
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Account successfully created.");

                    // Switch to login page after account creation
                    Stage stage = (Stage) tfUsername.getScene().getWindow();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), 1550, 800);
                    stage.setTitle("Login");
                    stage.setScene(scene);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error occurred while creating the account.");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error occurred while creating an account.");
        }
    }


    @FXML
    private void handleLogin(ActionEvent actionEvent) throws IOException {
        redirectToLoginPage();
    }

    private void redirectToLoginPage() throws IOException {
        Stage stage = (Stage) apRegister.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1520, 790);
        stage.setTitle("Log in");
        stage.setScene(scene);
    }

    // Handle Enter key event to navigate to login page
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleCreateAccount(new ActionEvent());
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
