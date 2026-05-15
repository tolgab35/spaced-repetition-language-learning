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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        String currentUserId = String.valueOf(SessionManager.getInstance().getUserId());

        rankColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        userColumn.setCellValueFactory(data ->
                new SimpleStringProperty("User " + data.getValue().userId()));
        xpColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf((int) data.getValue().xp())));

        leaderboardTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LeaderboardEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (!empty && entry != null && entry.userId().equals(currentUserId)) {
                    setStyle("-fx-background-color: #FFE9A8;");
                } else {
                    setStyle("");
                }
            }
        });

        leaderboardTable.getItems().setAll(entries);
    }

    @FXML
    private void handleBack() {
        MainApp.navigate("deck-list.fxml");
    }
}
