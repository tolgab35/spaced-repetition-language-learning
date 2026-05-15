package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.DeckResponse;
import com.srll.javafx.service.DeckApiService;
import com.srll.javafx.session.SessionManager;
import com.srll.javafx.ui.Dialogs;
import com.srll.javafx.ui.Icons;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
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
            empty.getStyleClass().add("secondary-label");
            deckListContainer.getChildren().add(empty);
            return;
        }
        for (DeckResponse deck : decks) {
            deckListContainer.getChildren().add(createDeckCard(deck));
        }
    }

    private HBox createDeckCard(DeckResponse deck) {
        Label langTile = new Label(languageCode(deck.language()));
        langTile.getStyleClass().add("lang-tile");
        langTile.setStyle("-fx-background-color: " + languageColor(deck.language()) + ";");

        Label nameLabel = new Label(deck.name());
        nameLabel.getStyleClass().add("deck-name");

        Label langBadge = new Label(deck.language());
        langBadge.getStyleClass().add("deck-pill");

        Label countLabel = new Label(deck.cardCount() + " cards");
        countLabel.getStyleClass().add("deck-count");

        HBox meta = new HBox(8, langBadge, countLabel);
        meta.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4, nameLabel, meta);
        info.setAlignment(Pos.CENTER_LEFT);

        HBox left = new HBox(14, langTile, info);
        left.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reviewBtn = new Button("Review");
        reviewBtn.getStyleClass().add("btn-mini");
        reviewBtn.setGraphic(Icons.of(Icons.PLAY, 13, "#1A1A2E"));
        reviewBtn.setDisable(deck.cardCount() == 0);
        reviewBtn.setOnAction(e -> {
            selectedDeckId = deck.id();
            selectedDeckName = deck.name();
            MainApp.navigate("review.fxml");
        });

        Button cardsBtn = new Button("Cards");
        cardsBtn.getStyleClass().add("btn-mini-alt");
        cardsBtn.setGraphic(Icons.of(Icons.GRID, 13, "#1A1A2E"));
        cardsBtn.setOnAction(e -> {
            selectedDeckId = deck.id();
            selectedDeckName = deck.name();
            MainApp.navigate("card-list.fxml");
        });

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("btn-mini-danger");
        deleteBtn.setGraphic(Icons.of(Icons.TRASH, 15, "#1A1A2E"));
        deleteBtn.setOnAction(e -> handleDelete(deck));

        HBox buttons = new HBox(8, reviewBtn, cardsBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER);

        HBox card = new HBox(12, left, spacer, buttons);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("deck-card");
        return card;
    }

    /** Two-letter code shown on the coloured deck tile. */
    private String languageCode(String language) {
        if (language == null || language.isBlank()) return "?";
        return switch (language.toLowerCase().trim()) {
            case "spanish", "español"      -> "ES";
            case "english"                 -> "EN";
            case "french", "français"      -> "FR";
            case "german", "deutsch"       -> "DE";
            case "japanese", "日本語"       -> "JA";
            case "italian", "italiano"     -> "IT";
            case "portuguese", "português" -> "PT";
            case "chinese", "中文"         -> "ZH";
            case "korean", "한국어"        -> "KO";
            case "arabic", "عربي"         -> "AR";
            case "turkish", "türkçe"       -> "TR";
            case "russian", "русский"      -> "RU";
            default -> language.trim().substring(0, Math.min(2, language.trim().length()))
                               .toUpperCase();
        };
    }

    /** Tile background colour, distinct per language. */
    private String languageColor(String language) {
        if (language == null) return "#1A1A2E";
        return switch (language.toLowerCase().trim()) {
            case "spanish", "español"      -> "#FF6B6B";
            case "english"                 -> "#4ECDC4";
            case "french", "français"      -> "#5C6BC0";
            case "german", "deutsch"       -> "#FF9F1C";
            case "japanese", "日本語"       -> "#EF5350";
            case "italian", "italiano"     -> "#6BCB77";
            case "portuguese", "português" -> "#26A69A";
            case "chinese", "中文"         -> "#E0457B";
            case "korean", "한국어"        -> "#AB47BC";
            case "arabic", "عربي"         -> "#8D6E63";
            case "turkish", "türkçe"       -> "#42A5F5";
            case "russian", "русский"      -> "#78909C";
            default -> "#1A1A2E";
        };
    }

    private void handleDelete(DeckResponse deck) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Deck");
        confirm.setHeaderText("Delete \"" + deck.name() + "\"?");
        confirm.setContentText("This will permanently delete the deck and all its cards. This action cannot be undone.");
        confirm.initOwner(MainApp.getPrimaryStage());
        Dialogs.style(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

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
        });
    }

    @FXML
    private void handleNewDeck() {
        TextField nameField = new TextField();
        nameField.setPromptText("Name (required)");

        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");

        TextField langField = new TextField();
        langField.setPromptText("Language (e.g. English, Spanish)");

        Label nameLabel = new Label("NAME");
        nameLabel.getStyleClass().add("field-label");
        Label descLabel = new Label("DESCRIPTION");
        descLabel.getStyleClass().add("field-label");
        Label langLabel = new Label("LANGUAGE");
        langLabel.getStyleClass().add("field-label");

        VBox content = new VBox(8,
                nameLabel, nameField,
                descLabel, descField,
                langLabel, langField);
        content.setPrefWidth(340);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Deck");
        dialog.setHeaderText("Create a new deck");
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogs.style(dialog.getDialogPane());

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
        errorLabel.getStyleClass().add("error-label");
        Button retryBtn = new Button("Retry");
        retryBtn.getStyleClass().add("btn-mini-alt");
        retryBtn.setOnAction(e -> loadDecks());
        deckListContainer.getChildren().addAll(errorLabel, retryBtn);
    }
}
