package com.srll.gamification.service;

import com.srll.gamification.document.UserProgress;
import com.srll.gamification.event.ReviewCompletedEvent;
import com.srll.gamification.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock private UserProgressRepository progressRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ZSetOperations<String, String> zSetOps;
    @Mock private BadgeAwarder badgeAwarder;

    private GamificationService gamificationService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        gamificationService = new GamificationService(
                progressRepository, redisTemplate, List.of(badgeAwarder));
    }

    @Test
    void processReview_newUser_createsProgressWithXp() {
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 4, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        UserProgress saved = captor.getValue();
        assertThat(saved.getXp()).isEqualTo(10);
        assertThat(saved.getTotalReviews()).isEqualTo(1);
        assertThat(saved.getTotalCorrect()).isEqualTo(1);
    }

    @Test
    void processReview_failedReview_awardsLessXp() {
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 1, false);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        UserProgress saved = captor.getValue();
        assertThat(saved.getXp()).isEqualTo(2);
        assertThat(saved.getTotalCorrect()).isEqualTo(0);
    }

    @Test
    void processReview_notifiesBadgeObservers() {
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        verify(badgeAwarder).onProgressUpdated(any(UserProgress.class), anyLong());
    }

    @Test
    void processReview_levelUpWhenXpThresholdReached() {
        UserProgress existing = UserProgress.builder().userId(1L).xp(95).level(1).build();
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        assertThat(captor.getValue().getLevel()).isEqualTo(2);
    }

    @Test
    void processReview_firstReview_streakBecomesOne() {
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        assertThat(captor.getValue().getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void processReview_reviewedYesterday_streakIncrements() {
        UserProgress existing = UserProgress.builder()
                .userId(1L).currentStreak(3).lastReviewDate(LocalDate.now().minusDays(1)).build();
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        assertThat(captor.getValue().getCurrentStreak()).isEqualTo(4);
    }

    @Test
    void processReview_reviewedToday_streakUnchanged() {
        UserProgress existing = UserProgress.builder()
                .userId(1L).currentStreak(5).lastReviewDate(LocalDate.now()).build();
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        assertThat(captor.getValue().getCurrentStreak()).isEqualTo(5);
    }

    @Test
    void processReview_missedDay_streakResetsToOne() {
        UserProgress existing = UserProgress.builder()
                .userId(1L).currentStreak(7).lastReviewDate(LocalDate.now().minusDays(3)).build();
        ReviewCompletedEvent event = new ReviewCompletedEvent(1L, 10L, 5, true);
        when(progressRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gamificationService.processReview(event);

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(progressRepository).save(captor.capture());

        assertThat(captor.getValue().getCurrentStreak()).isEqualTo(1);
    }
}
