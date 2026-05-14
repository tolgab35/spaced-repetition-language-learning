package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.DeckResponse;
import com.srll.javafx.service.DeckApiService;
import com.srll.javafx.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class DeckListController {

    public static Long selectedDeckId;
    public static String selectedDeckName;

    @FXML private VBox deckListContainer;

    private final DeckApiService deckService = new DeckApiService();

    @FXML
    public void initialize() {
        loadDecks();
    }

    private void loadDecks() {
        deckListContainer.getChildren().setAll(new ProgressIndicator());

        Task<List<DeckResponse>> task = new Task<>() {
            @Override
            protected List<DeckResponse> call() throws Exception {
                return deckService.getDecks();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> populateDeckList(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
        new Thread(task).start();
    }

    private void populateDeckList(List<DeckResponse> decks) {
        deckListContainer.getChildren().clear();
        if (decks.isEmpty()) {
            Label empty = new Label("No decks yet. Click '+ New Deck' to create one.");
            empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
            deckListContainer.getChildren().add(empty);
            return;
        }
        for (DeckResponse deck : decks) {
            deckListContainer.getChildren().add(createDeckCard(deck));
        }
    }

    private HBox createDeckCard(DeckResponse deck) {
        Label nameLabel = new Label(deck.name());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label langLabel = new Label(deck.language());
        langLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

        Label countLabel = new Label(deck.cardCount() + " cards");
        countLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

        VBox info = new VBox(4, nameLabel, langLabel, countLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reviewBtn = new Button("Review");
        reviewBtn.setOnAction(e -> MainApp.navigate("review.fxml"));

        Button cardsBtn = new Button("Cards");
        cardsBtn.setOnAction(e -> {
            selectedDeckId = deck.id();
            selectedDeckName = deck.name();
            MainApp.navigate("card-list.fxml");
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-text-fill: #e74c3c;");
        deleteBtn.setOnAction(e -> handleDelete(deck));

        HBox card = new HBox(12, info, spacer, reviewBtn, cardsBtn, deleteBtn);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 12;");
        return card;
    }

    private void handleDelete(DeckResponse deck) {
        // Onay dialogu bir sonraki görevde eklenecek
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                deckService.deleteDeck(deck.id());
                return null;
            }
        };
        deleteTask.setOnSucceeded(e -> Platform.runLater(this::loadDecks));
        deleteTask.setOnFailed(e -> Platform.runLater(() -> showError(deleteTask.getException())));
        new Thread(deleteTask).start();
    }

    @FXML
    private void handleNewDeck() {
        TextField nameField = new TextField();
        nameField.setPromptText("Name (required)");

        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");

        TextField langField = new TextField();
        langField.setPromptText("Language (e.g. English, Spanish)");

        VBox content = new VBox(8,
                new Label("Name:"), nameField,
                new Label("Description:"), descField,
                new Label("Language:"), langField);
        content.setPrefWidth(320);
        content.setPadding(new Insets(12));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Deck");
        dialog.setHeaderText("Create a new deck");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || langField.getText().trim().isEmpty()));
        langField.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || nameField.getText().trim().isEmpty()));

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String lang = langField.getText().trim();

            Task<DeckResponse> createTask = new Task<>() {
                @Override
                protected DeckResponse call() throws Exception {
                    return deckService.createDeck(name, desc, lang);
                }
            };
            createTask.setOnSucceeded(e -> Platform.runLater(this::loadDecks));
            createTask.setOnFailed(e -> Platform.runLater(() -> showError(createTask.getException())));
            new Thread(createTask).start();
        });
    }

    @FXML
    private void handleProgress() {
        MainApp.navigate("progress.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.navigate("login.fxml");
    }

    private void showError(Throwable ex) {
        deckListContainer.getChildren().clear();
        String message = ex instanceof ApiException api
                ? api.getMessage()
                : "Connection error. Is the backend running?";
        Label errorLabel = new Label("Error: " + message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        Button retryBtn = new Button("Retry");
        retryBtn.setOnAction(e -> loadDecks());
        deckListContainer.getChildren().addAll(errorLabel, retryBtn);
    }
}
