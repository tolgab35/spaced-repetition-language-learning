package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.service.CardApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

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
        // Task 2: load due cards
    }

    @FXML
    private void handleEndSession() {
        MainApp.navigate("deck-list.fxml");
    }

    @FXML
    private void handleShowAnswer() {
        // Task 3: flip animation
    }

    @FXML private void handleAgain() { submitRating(0); }
    @FXML private void handleHard()  { submitRating(3); }
    @FXML private void handleGood()  { submitRating(4); }
    @FXML private void handleEasy()  { submitRating(5); }

    private void submitRating(int rating) {
        // Task 5: POST /api/cards/{id}/review
    }
}
