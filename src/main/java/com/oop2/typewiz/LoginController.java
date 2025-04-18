package com.oop2.typewiz;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        // TODO: Add your login logic
        System.out.println("Logging in with " + username + " / " + password);
    }

    public void togglePasswordVisibility() {
        // Optional: implement show/hide password feature
    }
}
