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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
        ratingBox.setVisible(false);
        ratingBox.setManaged(false);

        updateProgress();
    }

    private void updateProgress() {
        int total = dueCards.size();
        progressLabel.setText("Card " + (currentIndex + 1) + " / " + total);
        sessionProgress.setProgress(total == 0 ? 0 : (double) currentIndex / total);
    }

    private void showEmptyState() {
        frontLabel.setText("No cards due for review today!");
        sessionProgress.setProgress(1.0);
        progressLabel.setText("All done!");
        showAnswerBtn.setVisible(false);
        showAnswerBtn.setManaged(false);
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
        // Task 5: POST /api/cards/{id}/review
    }
}
