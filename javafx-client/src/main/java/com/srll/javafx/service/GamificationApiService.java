package com.srll.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.srll.javafx.http.ApiClient;
import com.srll.javafx.http.dto.ApiResponse;
import com.srll.javafx.http.dto.LeaderboardEntry;
import com.srll.javafx.http.dto.ProgressResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<LeaderboardEntry> entries = mapLeaderboard(response.data());

        List<Long> userIds = entries.stream()
                .map(e -> Long.parseLong(e.userId()))
                .collect(Collectors.toList());

        Map<String, String> usernameMap = fetchUsernames(userIds);

        return entries.stream()
                .map(e -> new LeaderboardEntry(
                        e.userId(),
                        usernameMap.getOrDefault(e.userId(), "User " + e.userId()),
                        e.xp()
                ))
                .toList();
    }

    private Map<String, String> fetchUsernames(List<Long> userIds) {
        try {
            ApiResponse<Map<String, String>> response = ApiClient.post(
                    "/api/auth/users/batch",
                    userIds,
                    new TypeReference<>() {}
            );
            return response.data();
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<LeaderboardEntry> mapLeaderboard(List<List<Object>> raw) {
        return raw.stream()
                .map(row -> new LeaderboardEntry(
                        String.valueOf(row.get(0)),
                        null,
                        ((Number) row.get(1)).doubleValue()
                ))
                .toList();
    }
}
