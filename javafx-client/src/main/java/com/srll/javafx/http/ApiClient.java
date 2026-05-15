package com.srll.javafx.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.srll.javafx.MainApp;
import com.srll.javafx.session.SessionManager;
import com.srll.javafx.ui.Dialogs;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── GET ──────────────────────────────────────────────────────────────────

    public static <T> T get(String path, TypeReference<T> responseType) {
        HttpRequest request = baseRequest(path).GET().build();
        return execute(request, responseType);
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    public static <T> T post(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = baseRequest(path)
                .POST(jsonBody(body))
                .header("Content-Type", "application/json")
                .build();
        return execute(request, responseType);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public static void delete(String path) {
        HttpRequest request = baseRequest(path).DELETE().build();
        execute(request, new TypeReference<Void>() {});
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    public static <T> T put(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = baseRequest(path)
                .PUT(jsonBody(body))
                .header("Content-Type", "application/json")
                .build();
        return execute(request, responseType);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");

        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            builder.header("Authorization", session.getAuthHeader());
        }
        return builder;
    }

    private static HttpRequest.BodyPublisher jsonBody(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body));
        } catch (IOException e) {
            throw new ApiException("Failed to serialize request body", e);
        }
    }

    private static <T> T execute(HttpRequest request, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 401) {
                boolean hadSession = SessionManager.getInstance().isLoggedIn();
                SessionManager.getInstance().clearSession();
                if (hadSession) {
                    Platform.runLater(() -> MainApp.navigate("login.fxml"));
                    throw new ApiException(401, "Session expired. Please log in again.");
                }
                throw new ApiException(401, "Invalid credentials. Please check your username and password.");
            }
            if (status == 204 || responseType.getType() == Void.class) {
                if (status < 200 || status >= 300) {
                    throw new ApiException(status, userFriendlyMessage(status, response.body()));
                }
                return null;
            }
            if (status < 200 || status >= 300) {
                throw new ApiException(status, userFriendlyMessage(status, response.body()));
            }
            return MAPPER.readValue(response.body(), responseType);
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            String msg = "Cannot connect to server at localhost:8080.\nStart docker-compose first.";
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Backend is unreachable");
                alert.setContentText(msg);
                alert.initOwner(MainApp.getPrimaryStage());
                Dialogs.style(alert.getDialogPane());
                alert.showAndWait();
            });
            throw new ApiException(msg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted", e);
        }
    }

    public static String userFriendlyMessage(int statusCode, String body) {
        return switch (statusCode) {
            case 401 -> "Session expired. Please log in again.";
            case 403 -> "You don't have permission to perform this action.";
            case 404 -> "The requested resource was not found.";
            case 500 -> "Server error. Is the backend running?";
            default  -> extractMessage(body);
        };
    }

    private static String extractMessage(String body) {
        try {
            var node = MAPPER.readTree(body);
            if (node.has("message") && !node.get("message").isNull()) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {}
        return body;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
