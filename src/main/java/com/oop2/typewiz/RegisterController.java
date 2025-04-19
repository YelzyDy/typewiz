package com.oop2.typewiz;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.io.IOException;

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
    private void handleCreateAccount() {
        String username = tfUsername.getText();
        String email = tfEmail.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
        } else if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
        }
    }

    @FXML
    private void handleLogin(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) apRegister.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1550, 800);
        stage.setTitle("Log in");
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
