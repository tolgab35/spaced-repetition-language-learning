package com.srll.gamification.service;

import com.srll.gamification.document.Badge;
import com.srll.gamification.document.UserProgress;
import org.springframework.stereotype.Component;

/**
 * Observer that awards badges based on milestones in user progress.
 */
@Component
public class BadgeAwarder implements BadgeObserver {

    @Override
    public void onProgressUpdated(UserProgress progress, long streakDays) {
        awardIf(progress, Badge.FIRST_REVIEW, progress.getTotalReviews() >= 1);
        awardIf(progress, Badge.REVIEWS_100,  progress.getTotalReviews() >= 100);
        awardIf(progress, Badge.REVIEWS_500,  progress.getTotalReviews() >= 500);
        awardIf(progress, Badge.STREAK_3,     streakDays >= 3);
        awardIf(progress, Badge.STREAK_7,     streakDays >= 7);
        awardIf(progress, Badge.STREAK_30,    streakDays >= 30);
        awardIf(progress, Badge.LEVEL_5,      progress.getLevel() >= 5);
        awardIf(progress, Badge.LEVEL_10,     progress.getLevel() >= 10);
    }

    private void awardIf(UserProgress progress, Badge badge, boolean condition) {
        if (condition && !progress.getEarnedBadges().contains(badge.name())) {
            progress.getEarnedBadges().add(badge.name());
        }
    }
}
