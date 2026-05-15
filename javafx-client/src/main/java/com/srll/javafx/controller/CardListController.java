package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.service.CardApiService;
import com.srll.javafx.ui.Dialogs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CardListController {

    @FXML private Label deckNameLabel;
    @FXML private VBox  cardContainer;

    private final CardApiService cardService = new CardApiService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] BADGE_COLORS = {
        "#4ECDC4", "#6BCB77", "#FF9F1C", "#FF6B6B", "#C77DFF"
    };

    @FXML
    public void initialize() {
        deckNameLabel.setText(DeckListController.selectedDeckName);
        loadCards();
    }

    private void loadCards() {
        cardContainer.getChildren().clear();
        Label loading = new Label("Loading...");
        loading.getStyleClass().add("secondary-label");
        loading.setStyle("-fx-padding: 32; -fx-font-size: 14;");
        cardContainer.getChildren().add(loading);

        Task<List<CardResponse>> task = new Task<>() {
            @Override
            protected List<CardResponse> call() throws Exception {
                return cardService.getCards(DeckListController.selectedDeckId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<CardResponse> cards = task.getValue();
            cardContainer.getChildren().clear();
            if (cards.isEmpty()) {
                Label empty = new Label("No cards yet. Click '+ Add Card' to create one.");
                empty.getStyleClass().add("secondary-label");
                empty.setStyle("-fx-padding: 32; -fx-font-size: 14;");
                cardContainer.getChildren().add(empty);
            } else {
                for (int i = 0; i < cards.size(); i++) {
                    cardContainer.getChildren().add(buildCardItem(cards.get(i), i + 1));
                }
            }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
        new Thread(task).start();
    }

    private HBox buildCardItem(CardResponse card, int index) {
        String badgeColor = BADGE_COLORS[(index - 1) % BADGE_COLORS.length];

        Label indexBadge = new Label(String.valueOf(index));
        indexBadge.getStyleClass().add("card-index-badge");
        indexBadge.setStyle("-fx-background-color: " + badgeColor + ";");

        Label frontLabel = new Label(card.front());
        frontLabel.getStyleClass().add("card-item-front");
        frontLabel.setWrapText(true);
        frontLabel.setMaxWidth(Double.MAX_VALUE);

        Label backLabel = new Label(card.back());
        backLabel.getStyleClass().add("card-item-back");
        backLabel.setWrapText(true);
        backLabel.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(4, frontLabel, backLabel);
        textBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label intervalPill = new Label(card.intervalDays() + " days");
        intervalPill.getStyleClass().add("card-meta-pill");

        String nextReviewStr = card.nextReview() != null ? card.nextReview().format(DATE_FMT) : "—";
        Label nextReviewPill = new Label(nextReviewStr);
        nextReviewPill.getStyleClass().add("card-meta-pill");

        HBox metaRow = new HBox(6, intervalPill, nextReviewPill);
        metaRow.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-mini-alt");
        editBtn.setOnAction(e -> handleEditCard(card));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-mini-danger");
        deleteBtn.setOnAction(e -> handleDeleteCard(card));

        HBox actionRow = new HBox(8, editBtn, deleteBtn);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox rightBox = new VBox(8, metaRow, actionRow);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox item = new HBox(14, indexBadge, textBox, rightBox);
        item.getStyleClass().add("card-list-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setMaxWidth(Double.MAX_VALUE);

        return item;
    }

    @FXML
    private void handleBack() {
        MainApp.navigate("deck-list.fxml");
    }

    @FXML
    private void handleAddCard() {
        TextArea frontArea = new TextArea();
        frontArea.setPromptText("Front (question / word)");
        frontArea.setPrefRowCount(3);
        frontArea.setWrapText(true);

        TextArea backArea = new TextArea();
        backArea.setPromptText("Back (answer / translation)");
        backArea.setPrefRowCount(3);
        backArea.setWrapText(true);

        Label frontLabel = new Label("FRONT");
        frontLabel.getStyleClass().add("field-label");
        Label backLabel = new Label("BACK");
        backLabel.getStyleClass().add("field-label");

        VBox content = new VBox(8, frontLabel, frontArea, backLabel, backArea);
        content.setPrefWidth(380);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Card");
        dialog.setHeaderText("Create a new card");
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogs.style(dialog.getDialogPane());

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        frontArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || backArea.getText().trim().isEmpty()));
        backArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || frontArea.getText().trim().isEmpty()));

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            String front = frontArea.getText().trim();
            String back  = backArea.getText().trim();

            Task<CardResponse> createTask = new Task<>() {
                @Override
                protected CardResponse call() throws Exception {
                    return cardService.createCard(DeckListController.selectedDeckId, front, back);
                }
            };
            createTask.setOnSucceeded(e -> Platform.runLater(this::loadCards));
            createTask.setOnFailed(e -> Platform.runLater(() -> showError(createTask.getException())));
            new Thread(createTask).start();
        });
    }

    private void handleEditCard(CardResponse card) {
        TextArea frontArea = new TextArea(card.front());
        frontArea.setPromptText("Front (question / word)");
        frontArea.setPrefRowCount(3);
        frontArea.setWrapText(true);

        TextArea backArea = new TextArea(card.back());
        backArea.setPromptText("Back (answer / translation)");
        backArea.setPrefRowCount(3);
        backArea.setWrapText(true);

        Label frontLabel = new Label("FRONT");
        frontLabel.getStyleClass().add("field-label");
        Label backLabel = new Label("BACK");
        backLabel.getStyleClass().add("field-label");

        VBox content = new VBox(8, frontLabel, frontArea, backLabel, backArea);
        content.setPrefWidth(380);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Card");
        dialog.setHeaderText("Edit card");
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogs.style(dialog.getDialogPane());

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        frontArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || backArea.getText().trim().isEmpty()));
        backArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty() || frontArea.getText().trim().isEmpty()));

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            String front = frontArea.getText().trim();
            String back  = backArea.getText().trim();

            Task<CardResponse> updateTask = new Task<>() {
                @Override
                protected CardResponse call() throws Exception {
                    return cardService.updateCard(card.id(), front, back);
                }
            };
            updateTask.setOnSucceeded(e -> Platform.runLater(this::loadCards));
            updateTask.setOnFailed(e -> Platform.runLater(() -> showError(updateTask.getException())));
            new Thread(updateTask).start();
        });
    }

    private void handleDeleteCard(CardResponse card) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Card");
        confirm.setHeaderText("Delete this card?");
        confirm.setContentText("Front: " + card.front() + "\n\nThis action cannot be undone.");
        confirm.initOwner(MainApp.getPrimaryStage());
        Dialogs.style(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    cardService.deleteCard(card.id());
                    return null;
                }
            };
            deleteTask.setOnSucceeded(e -> Platform.runLater(this::loadCards));
            deleteTask.setOnFailed(e -> Platform.runLater(() -> showError(deleteTask.getException())));
            new Thread(deleteTask).start();
        });
    }

    private void showError(Throwable ex) {
        String message = ex instanceof ApiException api
                ? api.getMessage()
                : "Connection error. Is the backend running?";
        Label errorLabel = new Label("Error: " + message);
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setStyle("-fx-padding: 32;");
        cardContainer.getChildren().clear();
        cardContainer.getChildren().add(errorLabel);
    }
}
