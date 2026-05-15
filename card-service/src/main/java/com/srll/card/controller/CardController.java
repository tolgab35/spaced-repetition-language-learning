package com.srll.card.controller;

import com.srll.card.dto.CardRequest;
import com.srll.card.dto.CardResponse;
import com.srll.card.dto.ReviewRequest;
import com.srll.card.service.CardService;
import com.srll.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(@PathVariable Long deckId) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCards(deckId)));
    }

    @GetMapping("/api/cards/due")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getDueCards(
            @RequestParam(required = false) Long deckId,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        List<CardResponse> cards = deckId != null
                ? cardService.getDueCards(userId, deckId)
                : cardService.getDueCards(userId);
        return ResponseEntity.ok(ApiResponse.ok(cards));
    }

    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@PathVariable Long deckId,
                                                                 @Valid @RequestBody CardRequest body,
                                                                 HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card created", cardService.createCard(deckId, body, userId)));
    }

    @PutMapping("/api/cards/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> updateCard(@PathVariable Long id,
                                                                 @Valid @RequestBody CardRequest body,
                                                                 HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(cardService.updateCard(id, body, userId)));
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable Long id,
                                                         HttpServletRequest request) {
        Long userId = extractUserId(request);
        cardService.deleteCard(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }

    @PostMapping("/api/cards/{id}/review")
    public ResponseEntity<ApiResponse<CardResponse>> review(@PathVariable Long id,
                                                             @Valid @RequestBody ReviewRequest body,
                                                             HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(cardService.review(id, body, userId)));
    }

    private Long extractUserId(HttpServletRequest request) {
        return Long.parseLong(request.getHeader("X-User-Id"));
    }
}
