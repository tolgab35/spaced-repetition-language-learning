package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.AuthResponse;
import com.srll.javafx.service.AuthApiService;
import com.srll.javafx.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthApiService authService = new AuthApiService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (!validate(username, email, password)) return;

        setLoading(true);
        errorLabel.setVisible(false);

        Task<AuthResponse> task = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                return authService.register(username, email, password);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            SessionManager.getInstance().setSession(task.getValue());
            MainApp.navigate("deck-list.fxml");
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            Throwable ex = task.getException();
            showError(ex instanceof ApiException api
                    ? api.getMessage()
                    : "Connection error. Is the backend running?");
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleLoginLink() {
        MainApp.navigate("login.fxml");
    }

    private boolean validate(String username, String email, String password) {
        if (username.length() < 3 || username.length() > 50) {
            showError("Username must be between 3 and 50 characters.");
            return false;
        }
        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
    }
}
