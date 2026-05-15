package com.srll.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.srll.javafx.http.ApiClient;
import com.srll.javafx.http.dto.ApiResponse;
import com.srll.javafx.http.dto.CardResponse;
import com.srll.javafx.http.dto.ReviewRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardApiService {

    public List<CardResponse> getCards(Long deckId) {
        ApiResponse<List<CardResponse>> response = ApiClient.get(
                "/api/decks/" + deckId + "/cards",
                new TypeReference<>() {}
        );
        return response.data();
    }

    public CardResponse createCard(Long deckId, String front, String back) {
        Map<String, String> body = new HashMap<>();
        body.put("front", front);
        body.put("back", back);
        ApiResponse<CardResponse> response = ApiClient.post(
                "/api/decks/" + deckId + "/cards",
                body,
                new TypeReference<>() {}
        );
        return response.data();
    }

    public CardResponse updateCard(Long cardId, String front, String back) {
        Map<String, String> body = new HashMap<>();
        body.put("front", front);
        body.put("back", back);
        ApiResponse<CardResponse> response = ApiClient.put(
                "/api/cards/" + cardId,
                body,
                new TypeReference<>() {}
        );
        return response.data();
    }

    public void deleteCard(Long cardId) {
        ApiClient.delete("/api/cards/" + cardId);
    }

    public List<CardResponse> getDueCards() {
        ApiResponse<List<CardResponse>> response = ApiClient.get(
                "/api/cards/due",
                new TypeReference<>() {}
        );
        return response.data();
    }

    public List<CardResponse> getDueCards(Long deckId) {
        ApiResponse<List<CardResponse>> response = ApiClient.get(
                "/api/cards/due?deckId=" + deckId,
                new TypeReference<>() {}
        );
        return response.data();
    }

    public CardResponse reviewCard(Long cardId, int rating) {
        ApiResponse<CardResponse> response = ApiClient.post(
                "/api/cards/" + cardId + "/review",
                new ReviewRequest(rating),
                new TypeReference<>() {}
        );
        return response.data();
    }
}
