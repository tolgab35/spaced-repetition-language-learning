package com.srll.gamification.document;

public enum Badge {
    FIRST_REVIEW("First Review", "Complete your first review"),
    STREAK_3("3-Day Streak", "Review cards 3 days in a row"),
    STREAK_7("7-Day Streak", "Review cards 7 days in a row"),
    STREAK_30("30-Day Streak", "Review cards 30 days in a row"),
    REVIEWS_100("Century", "Complete 100 reviews"),
    REVIEWS_500("Five Hundred", "Complete 500 reviews"),
    LEVEL_5("Level 5", "Reach level 5"),
    LEVEL_10("Level 10", "Reach level 10");

    private final String title;
    private final String description;

    Badge(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
