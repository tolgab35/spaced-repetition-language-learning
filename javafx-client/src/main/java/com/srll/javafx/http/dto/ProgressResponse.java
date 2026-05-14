package com.srll.javafx.http.dto;

import java.time.LocalDate;
import java.util.List;

public record ProgressResponse(Long userId, int xp, int level, int totalReviews,
                                int totalCorrect, LocalDate lastReviewDate,
                                List<String> earnedBadges, long streakDays) {}
