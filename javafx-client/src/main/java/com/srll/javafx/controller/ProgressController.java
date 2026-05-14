package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.LeaderboardEntry;
import com.srll.javafx.http.dto.ProgressResponse;
import com.srll.javafx.service.GamificationApiService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class ProgressController {

    @FXML private VBox      levelPanel;
    @FXML private Label     levelLabel;
    @FXML private ProgressBar xpBar;
    @FXML private Label     xpLabel;
    @FXML private GridPane  statsGrid;
    @FXML private Label     badgeTitle;
    @FXML private FlowPane  badgePane;
    @FXML private TableView<LeaderboardEntry>          leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, String> rankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> userColumn;
    @FXML private TableColumn<LeaderboardEntry, String> xpColumn;

    private final GamificationApiService gamificationService = new GamificationApiService();

    @FXML
    public void initialize() {
        loadProgress();
        loadLeaderboard();
    }

    private void loadProgress() {
        Task<ProgressResponse> task = new Task<>() {
            @Override
            protected ProgressResponse call() throws Exception {
                return gamificationService.getProgress();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            ProgressResponse p = task.getValue();
            populateLevelPanel(p);
            populateStats(p);
            populateBadges(p);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            String msg = ex instanceof ApiException api
                    ? api.getMessage()
                    : "Connection error. Is the backend running?";
            levelLabel.setText("Error: " + msg);
        }));

        new Thread(task).start();
    }

    private void loadLeaderboard() {
        Task<List<LeaderboardEntry>> task = new Task<>() {
            @Override
            protected List<LeaderboardEntry> call() throws Exception {
                return gamificationService.getLeaderboard();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> populateLeaderboard(task.getValue())));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            String msg = ex instanceof ApiException api
                    ? api.getMessage()
                    : "Connection error. Is the backend running?";
            leaderboardTable.setPlaceholder(new Label("Error: " + msg));
        }));

        new Thread(task).start();
    }

    private void populateLevelPanel(ProgressResponse p) {
        levelLabel.setText("Level " + p.level());
        int xpInLevel = p.xp() % 100;
        xpLabel.setText(xpInLevel + " / 100 XP to next level");

        double target = xpInLevel / 100.0;
        xpBar.setProgress(0);
        Timeline xpAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(xpBar.progressProperty(), 0)),
                new KeyFrame(Duration.millis(800),  new KeyValue(xpBar.progressProperty(), target))
        );
        xpAnimation.play();
    }

    private void populateStats(ProgressResponse p) {
        int accuracy = p.totalReviews() == 0 ? 0
                : (int) Math.round((double) p.totalCorrect() / p.totalReviews() * 100);
        String lastReview = p.lastReviewDate() != null ? p.lastReviewDate().toString() : "—";

        addStatRow(0, "Total Reviews",  String.valueOf(p.totalReviews()));
        addStatRow(1, "Correct",        p.totalCorrect() + "  (" + accuracy + "%)");
        addStatRow(2, "Current Streak", p.streakDays() + " days 🔥");
        addStatRow(3, "Last Review",    lastReview);
    }

    private void addStatRow(int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13; -fx-min-width: 140;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        statsGrid.add(keyLabel,   0, row);
        statsGrid.add(valueLabel, 1, row);
    }

    private void populateBadges(ProgressResponse p) {
        // implemented in Task 5
    }

    private void populateLeaderboard(List<LeaderboardEntry> entries) {
        // implemented in Task 6
    }

    @FXML
    private void handleBack() {
        MainApp.navigate("deck-list.fxml");
    }
}
