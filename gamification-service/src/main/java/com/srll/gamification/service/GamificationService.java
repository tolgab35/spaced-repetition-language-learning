package com.srll.gamification.service;

import com.srll.gamification.document.UserProgress;
import com.srll.gamification.event.ReviewCompletedEvent;
import com.srll.gamification.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private static final String LEADERBOARD  = "leaderboard";
    private static final int    XP_PER_PASS  = 10;
    private static final int    XP_PER_FAIL  = 2;
    private static final int    XP_PER_LEVEL = 100;

    private final UserProgressRepository progressRepository;
    private final StringRedisTemplate redisTemplate;
    private final List<BadgeObserver> badgeObservers;

    public void processReview(ReviewCompletedEvent event) {
        UserProgress progress = progressRepository.findByUserId(event.getUserId())
                .orElseGet(() -> UserProgress.builder().userId(event.getUserId()).build());

        int xpGained = event.isPassed() ? XP_PER_PASS : XP_PER_FAIL;
        progress.setXp(progress.getXp() + xpGained);
        progress.setLevel(1 + progress.getXp() / XP_PER_LEVEL);
        progress.setTotalReviews(progress.getTotalReviews() + 1);
        if (event.isPassed()) {
            progress.setTotalCorrect(progress.getTotalCorrect() + 1);
        }

        updateStreak(progress);

        progress.setLastReviewDate(LocalDate.now());
        progress.setUpdatedAt(LocalDateTime.now());

        updateLeaderboard(event.getUserId(), progress.getXp());

        badgeObservers.forEach(o -> o.onProgressUpdated(progress, progress.getCurrentStreak()));

        progressRepository.save(progress);
    }

    public UserProgress getProgress(Long userId) {
        return progressRepository.findByUserId(userId)
                .orElseGet(() -> UserProgress.builder().userId(userId).build());
    }

    public List<Object[]> getLeaderboard(int topN) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD, 0, topN - 1)
                .stream()
                .map(entry -> new Object[]{entry.getValue(), entry.getScore()})
                .toList();
    }

    private void updateStreak(UserProgress progress) {
        LocalDate today = LocalDate.now();
        LocalDate lastReview = progress.getLastReviewDate();

        if (lastReview == null) {
            progress.setCurrentStreak(1);
        } else if (lastReview.equals(today)) {
            // already reviewed today — don't change streak
        } else if (lastReview.equals(today.minusDays(1))) {
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
        } else {
            progress.setCurrentStreak(1);
        }
    }

    private void updateLeaderboard(Long userId, int xp) {
        redisTemplate.opsForZSet().add(LEADERBOARD, String.valueOf(userId), xp);
    }
}
