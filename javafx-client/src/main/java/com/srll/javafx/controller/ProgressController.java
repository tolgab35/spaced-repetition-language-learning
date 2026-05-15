package com.srll.javafx.controller;

import com.srll.javafx.MainApp;
import com.srll.javafx.http.ApiException;
import com.srll.javafx.http.dto.LeaderboardEntry;
import com.srll.javafx.http.dto.ProgressResponse;
import com.srll.javafx.service.GamificationApiService;
import com.srll.javafx.session.SessionManager;
import com.srll.javafx.ui.Icons;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    @FXML private VBox      leaderboardRows;

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
            Label errLabel = new Label("Error: " + msg);
            errLabel.getStyleClass().add("error-label");
            errLabel.setStyle("-fx-padding: 16;");
            leaderboardRows.getChildren().setAll(errLabel);
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

        addStatRow(0, "Total Reviews",  String.valueOf(p.totalReviews()), null);
        addStatRow(1, "Correct",        p.totalCorrect() + "  (" + accuracy + "%)", null);
        addStatRow(2, "Current Streak", p.streakDays() + " days",
                   Icons.of(Icons.FLAME, 16, "#FF9F1C"));
        addStatRow(3, "Last Review",    lastReview, null);
    }

    private void addStatRow(int row, String key, String value, Region icon) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill: #6B6B7B; -fx-font-size: 13; -fx-min-width: 140;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        statsGrid.add(keyLabel, 0, row);
        if (icon == null) {
            statsGrid.add(valueLabel, 1, row);
        } else {
            HBox valueBox = new HBox(6, valueLabel, icon);
            valueBox.setAlignment(Pos.CENTER_LEFT);
            statsGrid.add(valueBox, 1, row);
        }
    }

    private void populateBadges(ProgressResponse p) {
        badgeTitle.setText("Badges (" + p.earnedBadges().size() + ")");
        badgePane.getChildren().clear();

        String[][] allBadges = {
            {"FIRST_REVIEW", "First Review"},
            {"STREAK_3",     "3-Day Streak"},
            {"STREAK_7",     "7-Day Streak"},
            {"STREAK_30",    "30-Day Streak"},
            {"REVIEWS_100",  "Century"},
            {"REVIEWS_500",  "Five Hundred"},
            {"LEVEL_5",      "Level 5"},
            {"LEVEL_10",     "Level 10"},
        };

        for (String[] badge : allBadges) {
            boolean earned = p.earnedBadges().contains(badge[0]);
            badgePane.getChildren().add(buildBadgeBox(badge[0], badge[1], earned));
        }
    }

    private VBox buildBadgeBox(String key, String name, boolean earned) {
        Region icon = Icons.of(badgeIcon(key), 30, earned ? badgeColor(key) : "#A8A496");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add(earned ? "badge-name" : "badge-name-locked");

        VBox box = new VBox(8, icon, nameLabel);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add(earned ? "badge-tile" : "badge-tile-locked");
        if (!earned) {
            box.setOpacity(0.6);
        }
        return box;
    }

    private String badgeIcon(String key) {
        return switch (key) {
            case "FIRST_REVIEW"         -> Icons.STAR;
            case "STREAK_3", "STREAK_7" -> Icons.FLAME;
            case "STREAK_30"            -> Icons.DIAMOND;
            case "REVIEWS_100"          -> Icons.CHECK_CIRCLE;
            case "REVIEWS_500"          -> Icons.TROPHY;
            case "LEVEL_5"              -> Icons.SHIELD;
            case "LEVEL_10"             -> Icons.CROWN;
            default                     -> Icons.STAR;
        };
    }

    private String badgeColor(String key) {
        return switch (key) {
            case "FIRST_REVIEW" -> "#FFC93C";
            case "STREAK_3"     -> "#FF6B6B";
            case "STREAK_7"     -> "#FF9F1C";
            case "STREAK_30"    -> "#4ECDC4";
            case "REVIEWS_100"  -> "#6BCB77";
            case "REVIEWS_500"  -> "#FFC93C";
            case "LEVEL_5"      -> "#4ECDC4";
            case "LEVEL_10"     -> "#FF9F1C";
            default             -> "#FFC93C";
        };
    }

    private void populateLeaderboard(List<LeaderboardEntry> entries) {
        String currentUserId = String.valueOf(SessionManager.getInstance().getUserId());
        leaderboardRows.getChildren().clear();
        leaderboardRows.getChildren().add(buildLeaderboardHeader());

        List<LeaderboardEntry> top10 = entries.stream().limit(10).toList();
        for (int i = 0; i < top10.size(); i++) {
            leaderboardRows.getChildren().add(
                buildLeaderboardRow(i + 1, top10.get(i), currentUserId, i == top10.size() - 1)
            );
        }

        if (top10.isEmpty()) {
            Label empty = new Label("No entries yet.");
            empty.setStyle("-fx-padding: 16; -fx-text-fill: #6B6B7B;");
            leaderboardRows.getChildren().add(empty);
        }
    }

    private HBox buildLeaderboardHeader() {
        Label hRank = new Label("#");
        hRank.getStyleClass().add("lb-header-cell");
        hRank.setMinWidth(50);

        Label hUser = new Label("User");
        hUser.getStyleClass().add("lb-header-cell");
        HBox.setHgrow(hUser, Priority.ALWAYS);

        Label hXp = new Label("XP");
        hXp.getStyleClass().add("lb-header-cell");
        hXp.setMinWidth(70);

        HBox header = new HBox(12, hRank, hUser, hXp);
        header.getStyleClass().add("lb-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildLeaderboardRow(int rank, LeaderboardEntry entry, String currentUserId, boolean isLast) {
        boolean isCurrentUser = entry.userId().equals(currentUserId);

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.getStyleClass().addAll("lb-rank-badge", rankStyleClass(rank));
        rankLabel.setMinWidth(34);
        rankLabel.setMaxWidth(34);
        rankLabel.setAlignment(Pos.CENTER);

        Label userLabel = new Label(entry.username() != null ? entry.username() : "User " + entry.userId());
        userLabel.getStyleClass().add("lb-username");
        if (isCurrentUser) userLabel.getStyleClass().add("lb-username-current");
        HBox.setHgrow(userLabel, Priority.ALWAYS);

        Label xpLabel = new Label((int) entry.xp() + " XP");
        xpLabel.getStyleClass().add("lb-xp-pill");
        if (rank <= 3) xpLabel.getStyleClass().add("lb-xp-pill-top");

        HBox row = new HBox(12, rankLabel, userLabel, xpLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("lb-row");
        if (isCurrentUser) row.getStyleClass().add("lb-row-current");
        if (isLast) row.getStyleClass().add("lb-row-last");
        return row;
    }

    private String rankStyleClass(int rank) {
        return switch (rank) {
            case 1 -> "lb-rank-gold";
            case 2 -> "lb-rank-silver";
            case 3 -> "lb-rank-bronze";
            default -> "lb-rank-default";
        };
    }

    @FXML
    private void handleBack() {
        MainApp.navigate("deck-list.fxml");
    }
}
