package com.srll.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("SRLL — Spaced Repetition Language Learning");
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(480);
        navigate("login.fxml");
        primaryStage.show();
    }

    public static void navigate(String fxmlFile) {
        try {
            URL resource = MainApp.class.getResource("/com/srll/javafx/fxml/" + fxmlFile);
            if (resource == null) {
                throw new IOException("FXML not found: " + fxmlFile);
            }
            Parent root = FXMLLoader.load(resource);
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 800, 600);
                URL css = MainApp.class.getResource("/com/srll/javafx/css/app.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to navigate to " + fxmlFile, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
