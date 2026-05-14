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

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthApiService authService = new AuthApiService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!validate(username, password)) return;

        setLoading(true);
        errorLabel.setVisible(false);

        Task<AuthResponse> task = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                return authService.login(username, password);
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
    private void handleRegister() {
        MainApp.navigate("register.fxml");
    }

    private boolean validate(String username, String password) {
        if (username.length() < 3 || username.length() > 50) {
            showError("Username must be between 3 and 50 characters.");
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
        loginButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
    }
}
