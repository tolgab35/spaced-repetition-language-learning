package com.srll.gamification.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProgressResponse {
    private Long userId;
    private int xp;
    private int level;
    private int totalReviews;
    private int totalCorrect;
    private LocalDate lastReviewDate;
    private List<String> earnedBadges;
    private long streakDays;
}
