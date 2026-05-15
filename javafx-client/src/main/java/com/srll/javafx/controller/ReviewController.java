package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.service.CardApiService;
import com.srll.javafx.ui.Icons;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;

public class ReviewController {

    /** Neo-retro styling for the session-summary / empty-state overlay. */
    private static final String SUMMARY_CARD =
        "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 40; "
        + "-fx-border-color: #1A1A2E; -fx-border-width: 3; -fx-border-radius: 12; "
        + "-fx-effect: dropshadow(gaussian, #1A1A2E, 0, 1.0, 7, 7);";
    private static final String SUMMARY_BTN_BASE =
        "-fx-text-fill: #1A1A2E; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand; "
        + "-fx-background-radius: 8; -fx-border-color: #1A1A2E; -fx-border-width: 2.5; "
        + "-fx-border-radius: 8; -fx-padding: 10 22;";
    private static final String SUMMARY_BTN_TEAL =
        SUMMARY_BTN_BASE + "-fx-background-color: #4ECDC4;";
    private static final String SUMMARY_BTN_YELLOW =
        SUMMARY_BTN_BASE + "-fx-background-color: #FFC93C;";

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
        progressLabel.setText("Complete!");

        int accuracy      = reviewedCount == 0 ? 0
                : (int) Math.round((double) correctCount / reviewedCount * 100);
        int estimatedXp   = correctCount * 10;

        Region trophyIcon = Icons.of(Icons.TROPHY, 56, "#FFC93C");

        Label titleLabel = new Label("Session Complete!");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Label reviewedLabel = new Label("Reviewed: " + reviewedCount + " cards");
        reviewedLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #1A1A2E;");

        Label correctLabel = new Label("Correct: " + correctCount + "  (" + accuracy + "%)");
        correctLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #1A1A2E;");

        Label xpLabel = new Label("XP earned: ~" + estimatedXp + " pts");
        xpLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #FF6B6B; -fx-font-weight: bold;");

        Button progressBtn = new Button("View Progress");
        progressBtn.setStyle(SUMMARY_BTN_TEAL);
        progressBtn.setOnAction(e -> MainApp.navigate("progress.fxml"));

        Button decksBtn = new Button("Back to Decks");
        decksBtn.setStyle(SUMMARY_BTN_YELLOW);
        decksBtn.setOnAction(e -> MainApp.navigate("deck-list.fxml"));

        HBox btnRow = new HBox(16, progressBtn, decksBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox summary = new VBox(16, trophyIcon, titleLabel, reviewedLabel, correctLabel, xpLabel, btnRow);
        summary.setAlignment(Pos.CENTER);
        summary.setStyle(SUMMARY_CARD);

        cardContainer.getChildren().setAll(summary);

        showAnswerBtn.setVisible(false);
        showAnswerBtn.setManaged(false);
        ratingBox.setVisible(false);
        ratingBox.setManaged(false);
    }

    private void showEmptyState() {
        sessionProgress.setProgress(1.0);
        progressLabel.setText("All done!");

        Region doneIcon = Icons.of(Icons.CHECK_CIRCLE, 60, "#6BCB77");

        Label titleLabel = new Label("You're all caught up!");
        titleLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Label subLabel = new Label("No cards are due for review today.\nCome back later!");
        subLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6B6B7B;");
        subLabel.setTextAlignment(TextAlignment.CENTER);
        subLabel.setWrapText(true);

        Button backBtn = new Button("Back to Decks");
        backBtn.setStyle(SUMMARY_BTN_YELLOW);
        backBtn.setOnAction(e -> MainApp.navigate("deck-list.fxml"));

        VBox emptyBox = new VBox(16, doneIcon, titleLabel, subLabel, backBtn);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle(SUMMARY_CARD);

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
