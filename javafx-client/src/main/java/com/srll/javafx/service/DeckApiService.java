package com.srll.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.srll.javafx.http.ApiClient;
import com.srll.javafx.http.dto.ApiResponse;
import com.srll.javafx.http.dto.DeckResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeckApiService {

    public List<DeckResponse> getDecks() {
        ApiResponse<List<DeckResponse>> response = ApiClient.get(
                "/api/decks",
                new TypeReference<>() {}
        );
        return response.data();
    }

    public DeckResponse createDeck(String name, String description, String language) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("language", language);
        body.put("description", description != null ? description : "");
        ApiResponse<DeckResponse> response = ApiClient.post(
                "/api/decks",
                body,
                new TypeReference<>() {}
        );
        return response.data();
    }

    public void deleteDeck(Long id) {
        ApiClient.delete("/api/decks/" + id);
    }
}
