package com.srll.gamification.listener;

import com.srll.gamification.event.ReviewCompletedEvent;
import com.srll.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventListener {

    private final GamificationService gamificationService;

    @KafkaListener(topics = "review-completed", groupId = "gamification-group")
    public void onReviewCompleted(ReviewCompletedEvent event) {
        log.debug("Received review event for user {}", event.getUserId());
        gamificationService.processReview(event);
    }
}
