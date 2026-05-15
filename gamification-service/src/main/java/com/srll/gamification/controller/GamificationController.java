package com.srll.gamification.controller;

import com.srll.gamification.document.UserProgress;
import com.srll.gamification.dto.ProgressResponse;
import com.srll.gamification.service.GamificationService;
import com.srll.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<ProgressResponse>> getProgress(HttpServletRequest request) {
        Long userId = extractUserId(request);
        UserProgress progress = gamificationService.getProgress(userId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(progress)));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<Object[]>>> getLeaderboard() {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getLeaderboard(10)));
    }

    private ProgressResponse toResponse(UserProgress progress) {
        ProgressResponse r = new ProgressResponse();
        r.setUserId(progress.getUserId());
        r.setXp(progress.getXp());
        r.setLevel(progress.getLevel());
        r.setTotalReviews(progress.getTotalReviews());
        r.setTotalCorrect(progress.getTotalCorrect());
        r.setLastReviewDate(progress.getLastReviewDate());
        r.setEarnedBadges(progress.getEarnedBadges());
        r.setStreakDays(progress.getCurrentStreak());
        return r;
    }

    private Long extractUserId(HttpServletRequest request) {
        return Long.parseLong(request.getHeader("X-User-Id"));
    }
}
