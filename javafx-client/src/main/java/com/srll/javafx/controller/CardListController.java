package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.service.CardApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CardListController {

    @FXML private Label deckNameLabel;
    @FXML private TableView<CardResponse> cardTable;
    @FXML private TableColumn<CardResponse, String> frontColumn;
    @FXML private TableColumn<CardResponse, String> backColumn;
    @FXML private TableColumn<CardResponse, String> intervalColumn;
    @FXML private TableColumn<CardResponse, String> nextReviewColumn;
    @FXML private TableColumn<CardResponse, Void> actionsColumn;

    private final CardApiService cardService = new CardApiService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        deckNameLabel.setText(DeckListController.selectedDeckName);
        setupColumns();
        loadCards();
    }

    private void setupColumns() {
        frontColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().front()));
        backColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().back()));
        intervalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().intervalDays() + " days"));
        nextReviewColumn.setCellValueFactory(data -> {
            var dt = data.getValue().nextReview();
            return new SimpleStringProperty(dt != null ? dt.format(DATE_FMT) : "—");
        });
        actionsColumn.setCellFactory(buildActionsColumn());
    }

    private Callback<TableColumn<CardResponse, Void>, TableCell<CardResponse, Void>> buildActionsColumn() {
        return col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   buttons   = new HBox(8, editBtn, deleteBtn);

            {
                deleteBtn.setStyle("-fx-text-fill: #e74c3c;");
                editBtn.setOnAction(e -> handleEditCard(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteCard(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
    }

    private void loadCards() {
        cardTable.getItems().clear();
        cardTable.setPlaceholder(new ProgressIndicator());

        Task<List<CardResponse>> task = new Task<>() {
            @Override
            protected List<CardResponse> call() throws Exception {
                return cardService.getCards(DeckListController.selectedDeckId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<CardResponse> cards = task.getValue();
            cardTable.getItems().setAll(cards);
            if (cards.isEmpty()) {
                cardTable.setPlaceholder(new Label("No cards yet. Click '+ Add Card' to create one."));
            }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
        new Thread(task).start();
    }

    @FXML
    private void handleBack() {
        MainApp.navigate("deck-list.fxml");
    }

    @FXML
    private void handleAddCard() {
        // implemented in Phase 4 Task 4
    }

    private void handleEditCard(CardResponse card) {
        // implemented in Phase 4 Task 5
    }

    private void handleDeleteCard(CardResponse card) {
        // implemented in Phase 4 Task 6
    }

    private void showError(Throwable ex) {
        String message = ex instanceof ApiException api
                ? api.getMessage()
                : "Connection error. Is the backend running?";
        cardTable.setPlaceholder(new Label("Error: " + message));
    }
}
