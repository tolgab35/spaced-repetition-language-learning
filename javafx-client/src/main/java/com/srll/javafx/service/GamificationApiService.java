package com.srll.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.srll.javafx.http.ApiClient;
import com.srll.javafx.http.dto.ApiResponse;
import com.srll.javafx.http.dto.LeaderboardEntry;
import com.srll.javafx.http.dto.ProgressResponse;

import java.util.List;

public class GamificationApiService {

    public ProgressResponse getProgress() {
        ApiResponse<ProgressResponse> response = ApiClient.get(
                "/api/gamification/progress",
                new TypeReference<>() {}
        );
        return response.data();
    }

    public List<LeaderboardEntry> getLeaderboard() {
        ApiResponse<List<List<Object>>> response = ApiClient.get(
                "/api/gamification/leaderboard",
                new TypeReference<>() {}
        );
        return mapLeaderboard(response.data());
    }

    private List<LeaderboardEntry> mapLeaderboard(List<List<Object>> raw) {
        return raw.stream()
                .map(row -> new LeaderboardEntry(
                        String.valueOf(row.get(0)),
                        ((Number) row.get(1)).doubleValue()
                ))
                .toList();
    }
}
