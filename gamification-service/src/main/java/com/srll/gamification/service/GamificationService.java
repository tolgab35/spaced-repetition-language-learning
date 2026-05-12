package com.srll.gamification.service;

import com.srll.gamification.document.UserProgress;
import com.srll.gamification.event.ReviewCompletedEvent;
import com.srll.gamification.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private static final String STREAK_KEY   = "streak:";
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
        progress.setLastReviewDate(LocalDate.now());
        progress.setUpdatedAt(LocalDateTime.now());

        progressRepository.save(progress);

        updateStreak(event.getUserId());
        updateLeaderboard(event.getUserId(), progress.getXp());

        long streakDays = getStreak(event.getUserId());
        badgeObservers.forEach(o -> o.onProgressUpdated(progress, streakDays));

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

    private void updateStreak(Long userId) {
        String key = STREAK_KEY + userId;
        redisTemplate.opsForValue().set(key, String.valueOf(
                getStreak(userId) + 1), Duration.ofDays(2));
    }

    private long getStreak(Long userId) {
        String val = redisTemplate.opsForValue().get(STREAK_KEY + userId);
        return val == null ? 0L : Long.parseLong(val);
    }

    private void updateLeaderboard(Long userId, int xp) {
        redisTemplate.opsForZSet().add(LEADERBOARD, String.valueOf(userId), xp);
    }
}
