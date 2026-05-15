package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.service.CardApiService;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;

public class ReviewController {

    @FXML private Label       progressLabel;
    @FXML private ProgressBar sessionProgress;
    @FXML private StackPane   cardContainer;
    @FXML private StackPane   frontFace;
    @FXML private StackPane   backFace;
    @FXML private Label       frontLabel;
    @FXML private Label       backLabel;
    @FXML private Button      showAnswerBtn;
    @FXML private HBox        ratingBox;

    private final CardApiService cardService = new CardApiService();

    private List<CardResponse> dueCards;
    private int currentIndex = 0;
    private int reviewedCount = 0;
    private int correctCount  = 0;

    @FXML
    public void initialize() {
        loadDueCards();
    }

    private void loadDueCards() {
        frontLabel.setText("Loading...");
        showAnswerBtn.setDisable(true);

        Task<List<CardResponse>> task = new Task<>() {
            @Override
            protected List<CardResponse> call() throws Exception {
                return cardService.getDueCards();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            dueCards = task.getValue();
            Collections.shuffle(dueCards);
            currentIndex = 0;
            showAnswerBtn.setDisable(false);
            if (dueCards.isEmpty()) {
                showEmptyState();
            } else {
                showCard(0);
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            String msg = ex instanceof ApiException api
                    ? api.getMessage()
                    : "Connection error. Is the backend running?";
            frontLabel.setText("Error: " + msg);
            showAnswerBtn.setDisable(true);
        }));

        new Thread(task).start();
    }

    private void showCard(int index) {
        CardResponse card = dueCards.get(index);
        frontLabel.setText(card.front());
        backLabel.setText(card.back());

        frontFace.setVisible(true);
        frontFace.setManaged(true);
        backFace.setVisible(false);
        backFace.setManaged(false);

        showAnswerBtn.setVisible(true);
        showAnswerBtn.setManaged(true);
        showAnswerBtn.setDisable(false);
        ratingBox.setVisible(false);
        ratingBox.setManaged(false);

        updateProgress();
    }

    private void updateProgress() {
        int total = dueCards.size();
        progressLabel.setText("Card " + (currentIndex + 1) + " / " + total);
        sessionProgress.setProgress(total == 0 ? 0 : (double) currentIndex / total);
    }

    private void showSessionSummary() {
        sessionProgress.setProgress(1.0);
        progressLabel.setText("Complete! ✓");

        int accuracy      = reviewedCount == 0 ? 0
                : (int) Math.round((double) correctCount / reviewedCount * 100);
        int estimatedXp   = correctCount * 10;

        Label titleLabel = new Label("Session Complete! 🎉");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label reviewedLabel = new Label("Reviewed: " + reviewedCount + " cards");
        reviewedLabel.setStyle("-fx-font-size: 16;");

        Label correctLabel = new Label("Correct: " + correctCount + "  (" + accuracy + "%)");
        correctLabel.setStyle("-fx-font-size: 16;");

        Label xpLabel = new Label("XP earned: ~" + estimatedXp + " pts");
        xpLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #3498db; -fx-font-weight: bold;");

        Button progressBtn = new Button("View Progress");
        progressBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                             "-fx-font-size: 14; -fx-padding: 8 24;");
        progressBtn.setOnAction(e -> MainApp.navigate("progress.fxml"));

        Button decksBtn = new Button("Back to Decks");
        decksBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                          "-fx-font-size: 14; -fx-padding: 8 24;");
        decksBtn.setOnAction(e -> MainApp.navigate("deck-list.fxml"));

        HBox btnRow = new HBox(16, progressBtn, decksBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox summary = new VBox(16, titleLabel, reviewedLabel, correctLabel, xpLabel, btnRow);
        summary.setAlignment(Pos.CENTER);
        summary.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 40; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");

        cardContainer.getChildren().setAll(summary);

        showAnswerBtn.setVisible(false);
        showAnswerBtn.setManaged(false);
        ratingBox.setVisible(false);
        ratingBox.setManaged(false);
    }

    private void showEmptyState() {
        sessionProgress.setProgress(1.0);
        progressLabel.setText("All done!");

        Label emojiLabel = new Label("🎉");
        emojiLabel.setStyle("-fx-font-size: 48;");

        Label titleLabel = new Label("You're all caught up!");
        titleLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subLabel = new Label("No cards are due for review today.\nCome back later!");
        subLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
        subLabel.setTextAlignment(TextAlignment.CENTER);
        subLabel.setWrapText(true);

        Button backBtn = new Button("Back to Decks");
        backBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                         "-fx-font-size: 14; -fx-padding: 8 24;");
        backBtn.setOnAction(e -> MainApp.navigate("deck-list.fxml"));

        VBox emptyBox = new VBox(16, emojiLabel, titleLabel, subLabel, backBtn);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 40; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");

        cardContainer.getChildren().setAll(emptyBox);

        showAnswerBtn.setVisible(false);
        showAnswerBtn.setManaged(false);
        ratingBox.setVisible(false);
        ratingBox.setManaged(false);
    }

    @FXML
    private void handleEndSession() {
        MainApp.navigate("deck-list.fxml");
    }

    @FXML
    private void handleShowAnswer() {
        showAnswerBtn.setDisable(true);

        RotateTransition rotateOut = new RotateTransition(Duration.millis(150), cardContainer);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setAxis(new Point3D(0, 1, 0));

        rotateOut.setOnFinished(e -> {
            frontFace.setVisible(false);
            frontFace.setManaged(false);
            backFace.setVisible(true);
            backFace.setManaged(true);

            RotateTransition rotateIn = new RotateTransition(Duration.millis(150), cardContainer);
            rotateIn.setFromAngle(-90);
            rotateIn.setToAngle(0);
            rotateIn.setAxis(new Point3D(0, 1, 0));
            rotateIn.setOnFinished(ev -> {
                showAnswerBtn.setVisible(false);
                showAnswerBtn.setManaged(false);
                ratingBox.setVisible(true);
                ratingBox.setManaged(true);
            });
            rotateIn.play();
        });

        rotateOut.play();
    }

    @FXML private void handleAgain() { submitRating(0); }
    @FXML private void handleHard()  { submitRating(3); }
    @FXML private void handleGood()  { submitRating(4); }
    @FXML private void handleEasy()  { submitRating(5); }

    private void submitRating(int rating) {
        CardResponse current = dueCards.get(currentIndex);
        ratingBox.setDisable(true);

        Task<CardResponse> task = new Task<>() {
            @Override
            protected CardResponse call() throws Exception {
                return cardService.reviewCard(current.id(), rating);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            reviewedCount++;
            if (rating >= 4) correctCount++;

            currentIndex++;
            ratingBox.setDisable(false);

            if (currentIndex >= dueCards.size()) {
                showSessionSummary();
            } else {
                showCard(currentIndex);
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            ratingBox.setDisable(false);
            Throwable ex = task.getException();
            String msg = ex instanceof ApiException api
                    ? api.getMessage()
                    : "Connection error. Is the backend running?";
            frontLabel.setText("Error: " + msg);
        }));

        new Thread(task).start();
    }
}
